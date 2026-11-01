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
import org.json.JSONArray;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public final class MainActivity extends Activity {
    private static final String PREFS = "veyron_launcher";
    private static final String INSTANCES = "instances";
    private static final String SELECTED = "selected_instance";
    private final String[] pages = {"Home","Instances","Versions","Downloads","Mods","Worlds","Accounts","Java & Runtime","Settings","Console"};
    private LinearLayout navItems, content;
    private String currentPage = "Home";

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.activity_main);
        navItems = findViewById(R.id.navItems);
        content = findViewById(R.id.screenContent);
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
            case "Versions": listing("VERSIONS", "Install and manage game versions", "Version manifest integration is not connected yet.", "Vanilla, snapshots, Fabric, Forge and NeoForge are planned."); break;
            case "Downloads": listing("DOWNLOAD CENTER", "Track launcher downloads", "No active downloads.", "Version files, libraries, assets, mods, modpacks, resource packs and shaders will use a shared queue."); break;
            case "Mods": listing("MOD MANAGER", "Manage mods per instance", "Choose an instance to manage its mods.", "Enable, disable, update and remove compatible mods. Mod discovery providers are not connected yet."); break;
            case "Worlds": listing("WORLDS", "Your local Minecraft worlds", "World scanning is not connected yet.", "Worlds will be listed per instance with safe folder-aware management."); break;
            case "Accounts": accounts(); break;
            case "Java & Runtime": listing("JAVA & RUNTIME", "Runtime compatibility and selection", "No Java runtime has been configured.", "Runtime installation, architecture checks and device compatibility diagnostics are planned."); break;
            case "Settings": settings(); break;
            case "Console": listing("CONSOLE & LOGS", "Launcher and game diagnostics", "No game process is running.", "Live process output, log export and crash-report inspection will appear here."); break;
        }
    }
    private void home() {
        heading("YOUR WORLD. YOUR WAY.", "VEYRON Launcher • Android / Minecraft Java Edition");
        card("ACTIVE INSTANCE", selectedInstance() + "  •  Local profile");
        card("ACCOUNT", "No Microsoft account connected. Sign-in integration is not configured.");
        content.addView(action("MANAGE INSTANCES", () -> showPage("Instances")));
        content.addView(action("BROWSE VERSIONS", () -> showPage("Versions")));
        Button play = action("PLAY  •  RUNTIME NOT READY", () -> Toast.makeText(this, "Game runtime integration is not ready yet.", Toast.LENGTH_LONG).show());
        play.setEnabled(false); content.addView(play);
        card("DEVELOPMENT STATUS", "UI and local instance profiles are available. Authentication, downloads, Java runtime and renderer are still integration tasks.");
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
        card("MICROSOFT", "OAuth sign-in with authorization code + PKCE is planned. No password fields will be used.");
        content.addView(action("MICROSOFT SIGN-IN • NOT CONFIGURED", () -> new AlertDialog.Builder(this).setTitle("Microsoft sign-in").setMessage("OAuth app registration and the Xbox/Minecraft token exchange are not implemented yet.").setPositiveButton("OK",null).show()));
        card("LOCAL PROFILE", "No authenticated Minecraft profile is available.");
    }
    private void settings() {
        heading("SETTINGS", "Launcher preferences");
        card("GAME MEMORY", "Memory allocation controls will be enabled after runtime integration.");
        card("RESOLUTION", "Device resolution and game window scaling are not connected yet.");
        card("CONTROLS", "Touch controls, keyboard and mouse mapping are planned.");
        card("RENDERER", "Renderer selection will only show options that are actually bundled and compatible.");
        card("APPEARANCE", "VEYRON dark theme • Cyan accent");
        card("STORAGE", "Instance profiles are stored locally. Game directory management is not implemented yet.");
    }
    private void listing(String title,String subtitle,String status,String detail) {
        heading(title,subtitle); card("STATUS",status); card("PLANNED",detail);
    }
    private void toast(String value){Toast.makeText(this,value,Toast.LENGTH_SHORT).show();}
}