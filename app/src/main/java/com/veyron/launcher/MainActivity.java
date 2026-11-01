package com.veyron.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.text.Editable;
import android.text.TextWatcher;
import org.json.JSONArray;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends Activity {
    private static final String PREFS = "veyron_launcher";
    private static final String INSTANCES = "instances";
    private static final String SELECTED = "selected_instance";
    private static final String SELECTED_VERSION = "selected_version";
    private final String[] pages = {"Home","Instances","Versions","Downloads","Mods","Worlds","Accounts","Java & Runtime","Settings","Console"};
    private LinearLayout navItems, content;
    private String currentPage = "Home";
    private VersionManager versionManager;
    private List<VersionManager.Version> versionCatalog = new ArrayList<>();
    private String latestRelease = "";
    private String latestSnapshot = "";
    private String versionSearch = "";
    private TextView versionStatus;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        navItems = findViewById(R.id.navItems);
        content = findViewById(R.id.screenContent);
        versionManager = new VersionManager(this);
        seedInstances();
        buildNavigation();
        showPage("Home");
    }

    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    private void buildNavigation() {
        navItems.removeAllViews();
        for (String page : pages) {
            Button b = new Button(this);
            b.setAllCaps(false); b.setText(page); b.setTextSize(12);
            b.setGravity(Gravity.CENTER_VERTICAL);
            b.setTextColor(getColor(page.equals(currentPage) ? R.color.veyron_accent : R.color.veyron_text));
            b.setBackgroundColor(page.equals(currentPage) ? 0x3320D8C4 : 0x00000000);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(44));
            lp.bottomMargin = dp(4); navItems.addView(b, lp);
            b.setOnClickListener(v -> showPage(page));
        }
    }
    private TextView text(String value, int size, boolean bold, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size);
        t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, dp(5), 0, dp(5)); return t;
    }
    private void heading(String title, String subtitle) {
        content.addView(text(title, 27, true, getColor(R.color.veyron_text)));
        content.addView(text(subtitle, 12, false, getColor(R.color.veyron_muted)));
        View gap = new View(this); content.addView(gap, new LinearLayout.LayoutParams(1, dp(15)));
    }
    private LinearLayout card(String title, String body) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(16), dp(13), dp(16), dp(13)); box.setBackgroundResource(R.drawable.bg_panel);
        TextView h = text(title, 15, true, getColor(R.color.veyron_text));
        TextView d = text(body, 12, false, getColor(R.color.veyron_muted));
        box.addView(h); box.addView(d);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = dp(10);
        content.addView(box, lp); return box;
    }
    private Button action(String label, Runnable run) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(12);
        b.setOnClickListener(v -> run.run()); return b;
    }
    private void showPage(String page) {
        currentPage = page; buildNavigation(); content.removeAllViews();
        switch (page) {
            case "Home": home(); break;
            case "Instances": instances(); break;
            case "Versions": versions(); break;
            case "Downloads": downloads(); break;
            case "Mods": listing("MOD MANAGER", "Manage mods per instance", "Choose an instance to manage its mods.", "Mod discovery, compatibility checks and per-instance mod file management are not connected yet."); break;
            case "Worlds": listing("WORLDS", "Your local Minecraft worlds", "World scanning is not connected yet.", "Worlds will be listed per instance with safe folder-aware management."); break;
            case "Accounts": accounts(); break;
            case "Java & Runtime": listing("JAVA & RUNTIME", "Runtime compatibility and selection", "No Java runtime has been configured.", "A compatible ARM64 Java runtime, native library loading and device checks are still required before launch."); break;
            case "Settings": settings(); break;
            case "Console": listing("CONSOLE & LOGS", "Launcher and game diagnostics", "No game process is running.", "Live process output and crash-report inspection will be connected with the game process manager."); break;
        }
    }
    private void home() {
        String selected = selectedVersion();
        boolean installed = versionManager.isCoreInstalled(selected);
        heading("YOUR WORLD. YOUR WAY.", "VEYRON Launcher • Android / Minecraft Java Edition");
        card("ACTIVE INSTANCE", selectedInstance() + "  •  Local profile");
        card("SELECTED VERSION", selected + (installed ? "  •  Core files installed" : "  •  Not installed"));
        card("ACCOUNT", "No Microsoft account connected. Minecraft authentication is not configured.");
        content.addView(action("MANAGE INSTANCES", () -> showPage("Instances")));
        content.addView(action("BROWSE VERSIONS", () -> showPage("Versions")));
        content.addView(action(installed ? "PLAY  •  SET UP RUNTIME TO CONTINUE" : "INSTALL SELECTED VERSION", () -> {
            if (!installed) {
                showPage("Versions");
                return;
            }
            new AlertDialog.Builder(this).setTitle("Game launch is not ready")
                .setMessage("The Minecraft version metadata and client JAR are installed. VEYRON still needs the required libraries, assets, a compatible Java runtime, renderer/native integration and a verified Microsoft account before it can safely launch the game.")
                .setPositiveButton("OPEN JAVA & RUNTIME", (d,w) -> showPage("Java & Runtime"))
                .setNegativeButton("OK", null).show();
        }));
        card("INSTALLER STATUS", "Official Mojang version metadata and SHA-1-verified client JAR downloads are available. Full game assembly and Android runtime launch are still in development.");
    }

    private void versions() {
        heading("MINECRAFT VERSIONS", "Official Mojang version manifest • Vanilla catalogue");
        versionStatus = text("Loading official version catalogue…", 12, false, getColor(R.color.veyron_muted));
        content.addView(versionStatus);
        EditText search = new EditText(this);
        search.setSingleLine(true); search.setTextSize(13); search.setHint("Search version (e.g. 1.21.4)");
        search.setText(versionSearch);
        LinearLayout.LayoutParams searchLp = new LinearLayout.LayoutParams(-1, dp(48));
        searchLp.bottomMargin = dp(10); content.addView(search, searchLp);
        LinearLayout results = new LinearLayout(this);
        results.setOrientation(LinearLayout.VERTICAL);
        content.addView(results, new LinearLayout.LayoutParams(-1, -2));
        search.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s,int st,int count,int after) {}
            @Override public void onTextChanged(CharSequence s,int start,int before,int count) {
                versionSearch = s.toString();
                renderVersionRows(results);
            }
            @Override public void afterTextChanged(Editable e) {}
        });
        loadVersionCatalog(results);
    }

    private void loadVersionCatalog(LinearLayout results) {
        if (!versionCatalog.isEmpty()) {
            renderVersionRows(results);
            versionStatus.setText("Latest release: " + latestRelease + "  •  Latest snapshot: " + latestSnapshot);
            return;
        }
        versionManager.fetchVersions(new VersionManager.VersionsCallback() {
            @Override public void onSuccess(List<VersionManager.Version> versions, String release, String snapshot) {
                runOnUiThread(() -> {
                    versionCatalog = versions; latestRelease = release; latestSnapshot = snapshot;
                    if (!isFinishing() && "Versions".equals(currentPage)) {
                        versionStatus.setText("Latest release: " + latestRelease + "  •  Latest snapshot: " + latestSnapshot);
                        renderVersionRows(results);
                    }
                });
            }
            @Override public void onError(Exception error) {
                runOnUiThread(() -> {
                    if (!isFinishing() && "Versions".equals(currentPage)) {
                        versionStatus.setText("Could not load the official catalogue: " + safeMessage(error));
                        results.removeAllViews();
                        results.addView(action("RETRY", () -> { versionCatalog.clear(); loadVersionCatalog(results); }));
                    }
                });
            }
        });
    }

    private void renderVersionRows(LinearLayout target) {
        target.removeAllViews();
        if (versionCatalog.isEmpty()) return;
        int shown = 0;
        for (VersionManager.Version version : versionCatalog) {
            if (!versionSearch.trim().isEmpty() &&
                    !version.id.toLowerCase().contains(versionSearch.trim().toLowerCase())) continue;
            LinearLayout row = card(version.id,
                    version.type.toUpperCase() +
                    (version.id.equals(latestRelease) ? "  •  LATEST RELEASE" : "") +
                    (version.id.equals(latestSnapshot) ? "  •  LATEST SNAPSHOT" : "") +
                    (versionManager.isCoreInstalled(version.id) ? "  •  CORE INSTALLED" : ""));
            row.addView(action(version.id.equals(selectedVersion()) ? "SELECTED" : "SELECT VERSION",
                    () -> {
                        getPreferences(0).edit().putString(SELECTED_VERSION, version.id).apply();
                        toast("Selected " + version.id);
                        showPage("Versions");
                    }));
            if (!versionManager.isCoreInstalled(version.id)) {
                row.addView(action("DOWNLOAD CORE FILES", () -> installVersion(version)));
            }
            target.addView(row);
            shown++;
            if (shown >= 45 && versionSearch.trim().isEmpty()) break;
        }
        if (shown == 0) target.addView(text("No matching versions found.", 12, false, getColor(R.color.veyron_muted)));
        else if (versionSearch.trim().isEmpty()) target.addView(text("Showing the 45 newest versions. Search to find another release.", 11, false, getColor(R.color.veyron_muted)));
    }

    private void installVersion(VersionManager.Version version) {
        new AlertDialog.Builder(this).setTitle("Install " + version.id + "?")
            .setMessage("VEYRON will download the official version metadata and client JAR, then verify the published SHA-1 hashes. Libraries, assets and runtime are separate unfinished steps.")
            .setPositiveButton("DOWNLOAD", (d,w) -> {
                Toast.makeText(this, "Downloading " + version.id + "…", Toast.LENGTH_LONG).show();
                versionManager.install(version, new VersionManager.InstallCallback() {
                    @Override public void onProgress(String message) {
                        runOnUiThread(() -> { if (!isFinishing()) toast(message); });
                    }
                    @Override public void onSuccess(File directory) {
                        runOnUiThread(() -> {
                            if (isFinishing()) return;
                            getPreferences(0).edit().putString(SELECTED_VERSION, version.id).apply();
                            new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Core files installed")
                                .setMessage(version.id + " metadata and client JAR downloaded and verified. Full game installation and Play support are not ready yet.")
                                .setPositiveButton("OK", (dialog,which) -> showPage("Versions")).show();
                        });
                    }
                    @Override public void onError(Exception error) {
                        runOnUiThread(() -> {
                            if (!isFinishing()) new AlertDialog.Builder(MainActivity.this)
                                .setTitle("Installation failed").setMessage(safeMessage(error))
                                .setPositiveButton("OK", null).show();
                        });
                    }
                });
            }).setNegativeButton("CANCEL", null).show();
    }

    private void downloads() {
        heading("DOWNLOAD CENTER", "Minecraft version core files stored in app-private storage.");
        File root = new File(getFilesDir(), "game/versions");
        File[] directories = root.listFiles();
        if (directories == null || directories.length == 0) {
            card("NO INSTALLED VERSIONS", "Open Versions to browse the official catalogue and download a version.");
            content.addView(action("BROWSE VERSIONS", () -> showPage("Versions")));
            return;
        }
        int count = 0;
        for (File directory : directories) {
            if (!directory.isDirectory()) continue;
            String id = directory.getName();
            boolean complete = versionManager.isCoreInstalled(id);
            LinearLayout item = card(id, complete ? "Metadata + client JAR present" : "Incomplete download");
            item.addView(action("SELECT VERSION", () -> {
                getPreferences(0).edit().putString(SELECTED_VERSION, id).apply();
                toast("Selected " + id);
                showPage("Home");
            }));
            count++;
        }
        if (count == 0) card("NO INSTALLED VERSIONS", "Browse Versions to start an install.");
    }

    private String selectedVersion() { return getPreferences(0).getString(SELECTED_VERSION, "latest"); }
    private String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty() ? error.getClass().getSimpleName() : message;
    }

    private void instances() {
        heading("INSTANCES", "Independent profiles for versions, mods and game directories.");
        content.addView(action("+ CREATE INSTANCE", this::createInstance));
        for (String name : getInstances()) {
            LinearLayout c = card(name, name.equals(selectedInstance()) ? "SELECTED INSTANCE" : "Separate profile • Not selected");
            c.addView(action("SELECT", () -> { getPreferences(0).edit().putString(SELECTED, name).apply(); showPage("Instances"); }));
            c.addView(action("RENAME", () -> renameInstance(name)));
            c.addView(action("DELETE", () -> deleteInstance(name)));
        }
    }
    private void createInstance() {
        EditText input = new EditText(this); input.setSingleLine(true); input.setHint("e.g. Survival 1.21");
        new AlertDialog.Builder(this).setTitle("New instance").setView(input)
            .setPositiveButton("CREATE", (d,w) -> { String n=input.getText().toString().trim(); if(n.isEmpty()) return; List<String> a=getInstances(); if(a.contains(n)){toast("An instance with that name already exists.");return;} a.add(n); saveInstances(a); getPreferences(0).edit().putString(SELECTED,n).apply(); showPage("Instances"); })
            .setNegativeButton("CANCEL", null).show();
    }
    private void renameInstance(String old) {
        EditText input=new EditText(this); input.setText(old); input.setSingleLine(true);
        new AlertDialog.Builder(this).setTitle("Rename instance").setView(input)
            .setPositiveButton("SAVE",(d,w)->{String n=input.getText().toString().trim(); if(n.isEmpty())return; List<String>a=getInstances(); if(a.contains(n)&&!n.equals(old)){toast("Name already exists.");return;} a.set(a.indexOf(old),n); saveInstances(a); if(old.equals(selectedInstance()))getPreferences(0).edit().putString(SELECTED,n).apply(); showPage("Instances");})
            .setNegativeButton("CANCEL",null).show();
    }
    private void deleteInstance(String name) {
        new AlertDialog.Builder(this).setTitle("Delete instance?").setMessage("Remove this local profile entry? Game files are not deleted by this foundation build.")
            .setPositiveButton("DELETE",(d,w)->{List<String>a=getInstances();a.remove(name);saveInstances(a); if(name.equals(selectedInstance()))getPreferences(0).edit().putString(SELECTED,a.isEmpty()?"":a.get(0)).apply();showPage("Instances");})
            .setNegativeButton("CANCEL",null).show();
    }
    private List<String> getInstances() {
        ArrayList<String> out=new ArrayList<>();
        try { JSONArray a=new JSONArray(getPreferences(0).getString(INSTANCES,"[]")); for(int i=0;i<a.length();i++)out.add(a.getString(i)); } catch(Exception ignored) {}
        return out;
    }
    private void saveInstances(List<String> items) { JSONArray a=new JSONArray(); for(String s:items)a.put(s); getPreferences(0).edit().putString(INSTANCES,a.toString()).apply(); }
    private void seedInstances() { if(!getPreferences(0).contains(INSTANCES)){List<String>a=new ArrayList<>();a.add("Default");saveInstances(a);getPreferences(0).edit().putString(SELECTED,"Default").apply();} }
    private String selectedInstance(){return getPreferences(0).getString(SELECTED,"Default");}
    private void accounts() {
        heading("ACCOUNTS", "Connect and switch game profiles.");
        card("MICROSOFT", "OAuth sign-in with authorization code + PKCE is not implemented yet. No password fields will be used.");
        content.addView(action("SIGN-IN NOT AVAILABLE YET", () -> new AlertDialog.Builder(this).setTitle("Microsoft sign-in").setMessage("The supported OAuth flow and Xbox/Minecraft entitlement checks still need implementation.").setPositiveButton("OK",null).show()));
        card("LOCAL PROFILE", "No authenticated Minecraft profile is available.");
    }
    private void settings() {
        heading("SETTINGS", "Launcher preferences");
        card("GAME MEMORY", "Memory allocation controls will be enabled after runtime integration.");
        card("RESOLUTION", "Device resolution and game window scaling are not connected yet.");
        card("CONTROLS", "Touch controls, keyboard and mouse mapping are planned.");
        card("RENDERER", "Renderer selection will only show options that are actually bundled and compatible.");
        card("APPEARANCE", "VEYRON dark theme • Cyan accent");
        card("STORAGE", "Version metadata and client JARs are stored in app-private storage under game/versions.");
    }
    private void listing(String title,String subtitle,String status,String detail) {
        heading(title,subtitle); card("STATUS",status); card("DETAIL",detail);
    }
    private void toast(String value){Toast.makeText(this,value,Toast.LENGTH_SHORT).show();}
}
