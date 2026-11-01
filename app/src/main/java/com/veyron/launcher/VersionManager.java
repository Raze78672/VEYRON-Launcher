package com.veyron.launcher;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;

public final class VersionManager {
    private static final String MANIFEST_URL =
            "https://piston-meta.mojang.com/mc/game/version_manifest_v2.json";

    public static final class Version {
        public final String id;
        public final String type;
        public final String url;
        public final String sha1;

        Version(String id, String type, String url, String sha1) {
            this.id = id;
            this.type = type;
            this.url = url;
            this.sha1 = sha1;
        }
    }

    public interface VersionsCallback {
        void onSuccess(List<Version> versions, String latestRelease, String latestSnapshot);
        void onError(Exception error);
    }

    public interface InstallCallback {
        void onProgress(String message);
        void onSuccess(File versionDirectory);
        void onError(Exception error);
    }

    private final Context context;

    public VersionManager(Context context) {
        this.context = context.getApplicationContext();
    }

    public void fetchVersions(VersionsCallback callback) {
        new Thread(() -> {
            try {
                JSONObject manifest = new JSONObject(readUrl(MANIFEST_URL));
                JSONObject latest = manifest.getJSONObject("latest");
                JSONArray rows = manifest.getJSONArray("versions");
                List<Version> versions = new ArrayList<>();
                for (int i = 0; i < rows.length(); i++) {
                    JSONObject row = rows.getJSONObject(i);
                    versions.add(new Version(
                            row.getString("id"),
                            row.getString("type"),
                            row.getString("url"),
                            row.optString("sha1", "")
                    ));
                }
                callback.onSuccess(versions,
                        latest.optString("release", ""),
                        latest.optString("snapshot", ""));
            } catch (Exception e) {
                callback.onError(e);
            }
        }, "veyron-version-catalog").start();
    }

    public void install(Version version, InstallCallback callback) {
        new Thread(() -> {
            try {
                File root = new File(context.getFilesDir(), "game/versions");
                File directory = new File(root, version.id);
                if (!directory.exists() && !directory.mkdirs()) {
                    throw new IllegalStateException("Could not create version directory.");
                }

                callback.onProgress("Downloading " + version.id + " metadata…");
                byte[] metadataBytes = readUrlBytes(version.url);
                if (!version.sha1.isEmpty() &&
                        !version.sha1.equalsIgnoreCase(sha1(metadataBytes))) {
                    throw new IllegalStateException("Version metadata SHA-1 verification failed.");
                }

                JSONObject metadata = new JSONObject(new String(metadataBytes, "UTF-8"));
                JSONObject downloads = metadata.optJSONObject("downloads");
                if (downloads == null || !downloads.has("client")) {
                    throw new IllegalStateException("This version has no downloadable client JAR.");
                }
                JSONObject client = downloads.getJSONObject("client");
                String clientUrl = client.getString("url");
                String clientSha1 = client.optString("sha1", "");
                File jsonFile = new File(directory, version.id + ".json");
                File jarFile = new File(directory, version.id + ".jar");

                callback.onProgress("Downloading Minecraft client files…");
                downloadVerified(clientUrl, jarFile, clientSha1);

                File tempJson = new File(directory, version.id + ".json.tmp");
                try (FileOutputStream out = new FileOutputStream(tempJson)) {
                    out.write(metadataBytes);
                    out.getFD().sync();
                }
                if (!tempJson.renameTo(jsonFile)) {
                    tempJson.delete();
                    throw new IllegalStateException("Could not save version metadata.");
                }

                callback.onSuccess(directory);
            } catch (Exception e) {
                callback.onError(e);
            }
        }, "veyron-version-install").start();
    }

    public File getVersionDirectory(String id) {
        return new File(new File(context.getFilesDir(), "game/versions"), id);
    }

    public boolean isCoreInstalled(String id) {
        File dir = getVersionDirectory(id);
        return new File(dir, id + ".json").isFile() &&
                new File(dir, id + ".jar").isFile();
    }

    private static String readUrl(String address) throws Exception {
        return new String(readUrlBytes(address), "UTF-8");
    }

    private static byte[] readUrlBytes(String address) throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(30000);
        connection.setRequestProperty("User-Agent", "VEYRON-Launcher/0.1.0");
        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Download server returned HTTP " + status);
            }
            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                 java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream()) {
                byte[] buffer = new byte[8192];
                int count;
                while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                return out.toByteArray();
            }
        } finally {
            connection.disconnect();
        }
    }

    private static void downloadVerified(String address, File destination, String expectedSha1)
            throws Exception {
        HttpURLConnection connection = (HttpURLConnection) new URL(address).openConnection();
        connection.setConnectTimeout(15000);
        connection.setReadTimeout(60000);
        connection.setRequestProperty("User-Agent", "VEYRON-Launcher/0.1.0");
        File temporary = new File(destination.getParentFile(), destination.getName() + ".part");
        try {
            int status = connection.getResponseCode();
            if (status < 200 || status >= 300) {
                throw new IllegalStateException("Client download returned HTTP " + status);
            }
            try (InputStream in = new BufferedInputStream(connection.getInputStream());
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(temporary))) {
                byte[] buffer = new byte[16384];
                int count;
                while ((count = in.read(buffer)) != -1) out.write(buffer, 0, count);
                out.flush();
            }
            if (!expectedSha1.isEmpty()) {
                String actual = sha1File(temporary);
                if (!expectedSha1.equalsIgnoreCase(actual)) {
                    throw new IllegalStateException("Minecraft client JAR SHA-1 verification failed.");
                }
            }
            if (destination.exists() && !destination.delete()) {
                throw new IllegalStateException("Could not replace previous client JAR.");
            }
            if (!temporary.renameTo(destination)) {
                throw new IllegalStateException("Could not finalize client JAR download.");
            }
        } catch (Exception e) {
            temporary.delete();
            throw e;
        } finally {
            connection.disconnect();
        }
    }

    private static String sha1(byte[] bytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        return hex(digest.digest(bytes));
    }

    private static String sha1File(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        try (InputStream in = new FileInputStream(file)) {
            byte[] buffer = new byte[16384];
            int count;
            while ((count = in.read(buffer)) != -1) digest.update(buffer, 0, count);
        }
        return hex(digest.digest());
    }

    private static String hex(byte[] bytes) {
        StringBuilder out = new StringBuilder();
        for (byte b : bytes) out.append(String.format("%02x", b & 0xff));
        return out.toString();
    }
}
