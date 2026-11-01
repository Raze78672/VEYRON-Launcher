package com.veyron.launcher;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public final class InstanceManager {
    private static final String PREFS = "veyron_launcher";
    private static final String KEY = "instances";

    public static final class Profile {
        public final String id;
        public String name;
        public String version;
        public String loader;

        Profile(String id, String name, String version, String loader) {
            this.id=id; this.name=name; this.version=version; this.loader=loader;
        }
    }

    private final Context context;
    private final SharedPreferences preferences;

    public InstanceManager(Context context) {
        this.context=context.getApplicationContext();
        preferences=this.context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public List<Profile> all() {
        ArrayList<Profile> profiles=new ArrayList<>();
        try {
            JSONArray array=new JSONArray(preferences.getString(KEY,"[]"));
            for(int i=0;i<array.length();i++) {
                Object value=array.get(i);
                if(value instanceof JSONObject) {
                    JSONObject row=(JSONObject)value;
                    profiles.add(new Profile(row.optString("id",UUID.randomUUID().toString()),
                        row.optString("name","Minecraft"),row.optString("version","latest"),
                        row.optString("loader","Vanilla")));
                } else if(value instanceof String) {
                    String name=(String)value;
                    profiles.add(new Profile(slug(name),name,
                        preferences.getString("selected_version","latest"),"Vanilla"));
                }
            }
        } catch(Exception ignored) {}
        return profiles;
    }

    public Profile create(String name,String version,String loader) throws Exception {
        String id=slug(name)+"-"+UUID.randomUUID().toString().substring(0,8);
        Profile profile=new Profile(id,name,version,loader);
        List<Profile> profiles=all();
        profiles.add(profile);
        save(profiles);
        File directory=gameDirectory(profile);
        if(!directory.exists()&&!directory.mkdirs()) throw new IllegalStateException("Could not create instance folder.");
        writeMetadata(profile);
        return profile;
    }

    public void save(List<Profile> profiles) {
        JSONArray array=new JSONArray();
        for(Profile p:profiles) {
            JSONObject row=new JSONObject();
            try {
                row.put("id",p.id); row.put("name",p.name);
                row.put("version",p.version); row.put("loader",p.loader);
                array.put(row);
            } catch(Exception ignored) {}
        }
        preferences.edit().putString(KEY,array.toString()).apply();
    }

    public void rename(Profile profile,String newName) throws Exception {
        profile.name=newName; save(all());
        writeMetadata(profile);
    }

    public void delete(Profile profile) {
        List<Profile> profiles=all();
        for(int i=profiles.size()-1;i>=0;i--) if(profiles.get(i).id.equals(profile.id)) profiles.remove(i);
        save(profiles);
        // Keep instance data on disk as a safety measure; deletion is profile-only.
    }

    public File gameDirectory(Profile profile) {
        return new File(new File(context.getFilesDir(),"game/instances"),profile.id);
    }

    public File versionDirectory(Profile profile) {
        return new File(new File(gameDirectory(profile),"versions"),profile.version);
    }

    public boolean isInstalled(Profile profile) {
        File folder=versionDirectory(profile);
        return new File(folder,profile.version+".json").isFile()
            && new File(folder,profile.version+".jar").isFile();
    }

    public void writeMetadata(Profile profile) throws Exception {
        File folder=gameDirectory(profile);
        if(!folder.exists()&&!folder.mkdirs()) throw new IllegalStateException("Could not create instance folder.");
        JSONObject json=new JSONObject();
        json.put("id",profile.id); json.put("name",profile.name);
        json.put("version",profile.version); json.put("loader",profile.loader);
        json.put("gameDirectory",folder.getAbsolutePath());
        File temporary=new File(folder,"instance.json.tmp");
        try(FileOutputStream out=new FileOutputStream(temporary)) {
            out.write(json.toString(2).getBytes("UTF-8")); out.getFD().sync();
        }
        File destination=new File(folder,"instance.json");
        if(destination.exists()&&!destination.delete()) throw new IllegalStateException("Could not update instance details.");
        if(!temporary.renameTo(destination)) throw new IllegalStateException("Could not save instance details.");
    }

    private static String slug(String value) {
        String safe=value.toLowerCase().replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)","");
        return safe.isEmpty()?"instance":safe;
    }
}
