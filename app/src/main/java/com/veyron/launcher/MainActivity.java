package com.veyron.launcher;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.WindowManager;
import android.graphics.drawable.GradientDrawable;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.app.ProgressDialog;
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
    private InstanceManager instanceManager;
    private List<VersionManager.Version> versionCatalog = new ArrayList<>();
    private String latestRelease = "";
    private String latestSnapshot = "";
    private String versionSearch = "";
    private TextView versionStatus;

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        setImmersiveMode();
        setContentView(R.layout.activity_main);
        navItems = findViewById(R.id.navItems);
        content = findViewById(R.id.screenContent);
        versionManager = new VersionManager(this);
        instanceManager = new InstanceManager(this);
        seedInstances();
        buildNavigation();
        showPage("Home");
    }

    private void setImmersiveMode() {
        getWindow().setStatusBarColor(getColor(R.color.veyron_bg));
        getWindow().setNavigationBarColor(getColor(R.color.veyron_bg));
        getWindow().getDecorView().setSystemUiVisibility(
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY |
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
            View.SYSTEM_UI_FLAG_FULLSCREEN);
    }
    @Override public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) setImmersiveMode();
    }
    private int dp(float value) { return (int)(value * getResources().getDisplayMetrics().density + 0.5f); }
    private GradientDrawable rounded(int color, int stroke) {
        GradientDrawable d=new GradientDrawable(); d.setColor(color); d.setCornerRadius(dp(12));
        if(stroke!=0)d.setStroke(dp(1),stroke); return d;
    }
    private void buildNavigation() {
        navItems.removeAllViews();
        for (String page : pages) {
            Button b = new Button(this);
            b.setAllCaps(false); b.setText(page); b.setTextSize(12);
            b.setTypeface(Typeface.DEFAULT, page.equals(currentPage) ? Typeface.BOLD : Typeface.NORMAL);
            b.setGravity(Gravity.CENTER_VERTICAL); b.setPadding(dp(14),0,dp(8),0);
            b.setTextColor(getColor(page.equals(currentPage) ? R.color.veyron_accent : R.color.veyron_muted));
            b.setBackground(rounded(page.equals(currentPage) ? 0x3320D8C4 : 0x00111A22,
                page.equals(currentPage) ? 0x6630D6C4 : 0x00000000));
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, dp(42));
            lp.bottomMargin = dp(5); navItems.addView(b, lp);
            b.setOnClickListener(v -> showPage(page));
        }
    }
    private TextView text(String value, int size, boolean bold, int color) {
        TextView t = new TextView(this); t.setText(value); t.setTextSize(size);
        t.setTextColor(color); if (bold) t.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        t.setPadding(0, dp(5), 0, dp(5)); return t;
    }
    private void heading(String title, String subtitle) {
        TextView titleView=text(title,25,true,getColor(R.color.veyron_text));
        titleView.setLetterSpacing(0.025f); content.addView(titleView);
        content.addView(text(subtitle,11,false,getColor(R.color.veyron_muted)));
        View gap = new View(this); content.addView(gap, new LinearLayout.LayoutParams(1, dp(18)));
    }
    private LinearLayout createCard(String title, String body) {
        LinearLayout box = new LinearLayout(this); box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(dp(17), dp(14), dp(17), dp(14)); box.setBackgroundResource(R.drawable.bg_panel);
        TextView h = text(title, 15, true, getColor(R.color.veyron_text));
        TextView d = text(body, 12, false, getColor(R.color.veyron_muted));
        box.addView(h); box.addView(d);
        return box;
    }
    private LinearLayout card(String title, String body) {
        LinearLayout box = createCard(title, body);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(-1, -2); lp.bottomMargin = dp(10);
        content.addView(box, lp); return box;
    }
    private Button action(String label, Runnable run) {
        Button b = new Button(this); b.setText(label); b.setAllCaps(false); b.setTextSize(11);
        b.setTextColor(getColor(R.color.veyron_text)); b.setMinHeight(dp(42));
        b.setPadding(dp(16),0,dp(16),0); b.setBackground(rounded(0xFF17232D,0xFF263844));
        LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,dp(44)); lp.bottomMargin=dp(7);
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
        InstanceManager.Profile active=selectedProfile();
        String selected=active==null?selectedVersion():active.version;
        boolean installed=active!=null&&instanceManager.isInstalled(active);
        heading("YOUR WORLD. YOUR WAY.", "VEYRON Launcher  •  Android / Minecraft Java Edition");
        card("ACTIVE INSTANCE", active==null?"No instance created":active.name+"  •  "+active.loader);
        card("SELECTED VERSION", selected + (installed ? "  •  Core files installed" : "  •  Not downloaded"));
        card("ACCOUNT", "No Microsoft account connected. Minecraft authentication is not configured.");
        content.addView(action("MANAGE INSTANCES", () -> showPage("Instances")));
        content.addView(action("BROWSE VERSIONS", () -> showPage("Versions")));
        content.addView(action(installed ? "PLAY  •  SET UP RUNTIME TO CONTINUE" : "DOWNLOAD ACTIVE INSTANCE", () -> {
            if (!installed) {
                if(active!=null) downloadInstance(active); else showPage("Instances");
                return;
            }
            new AlertDialog.Builder(this).setTitle("Game launch is not ready")
                .setMessage("The Minecraft version metadata and client JAR are installed. VEYRON still needs the required libraries, assets, a compatible Java runtime, renderer/native integration and a verified Microsoft account before it can safely launch the game.")
                .setPositiveButton("OPEN JAVA & RUNTIME", (d,w) -> showPage("Java & Runtime"))
                .setNegativeButton("OK", null).show();
        }));
        card("INSTALLER STATUS", "Per-instance folders and verified Minecraft core downloads are enabled. Libraries, assets, loaders, Java runtime and actual game launch are still in development.");
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
                    if ("latest".equals(selectedVersion()) && !release.isEmpty()) {
                        getPreferences(0).edit().putString(SELECTED_VERSION, release).apply();
                    }
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
            LinearLayout row = createCard(version.id,
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
        heading("DOWNLOAD CENTER", "Track Minecraft core files for each isolated instance.");
        List<InstanceManager.Profile> profiles=instanceManager.all();
        if(profiles.isEmpty()) {
            card("NO INSTANCES", "Create an instance before downloading its Minecraft version.");
            content.addView(action("CREATE INSTANCE",()->showPage("Instances")));
            return;
        }
        for(InstanceManager.Profile profile:profiles) {
            boolean installed=instanceManager.isInstalled(profile);
            LinearLayout panel=createCard(profile.name,
                profile.version+"  •  "+profile.loader+"  •  "+(installed?"CORE FILES READY":"WAITING FOR DOWNLOAD"));
            panel.addView(text(installed?"Minecraft metadata and client JAR verified":"This instance has not downloaded its Minecraft core yet.",11,false,getColor(R.color.veyron_muted)));
            panel.addView(action("OPEN INSTANCE",()->{
                getPreferences(0).edit().putString(SELECTED,profile.name).apply();
                showPage("Instances");
            }));
            panel.addView(action(installed?"DOWNLOAD AGAIN":"DOWNLOAD VERSION",()->downloadInstance(profile)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=dp(11); content.addView(panel,lp);
        }
    }

    private String selectedVersion() { return getPreferences(0).getString(SELECTED_VERSION, "latest"); }
    private String safeMessage(Exception error) {
        String message = error.getMessage();
        return message == null || message.trim().isEmpty() ? error.getClass().getSimpleName() : message;
    }

    private void instances() {
        heading("YOUR INSTANCES", "Separate Minecraft profiles • isolated game folders • independent versions");
        List<InstanceManager.Profile> profiles=instanceManager.all();
        card("INSTANCE LIBRARY", profiles.size()+" profile"+(profiles.size()==1?"":"s")+" created on this device");
        content.addView(action("+  CREATE NEW INSTANCE", this::openCreateInstance));
        if(profiles.isEmpty()) {
            card("NO INSTANCES YET","Create your first instance to choose a Minecraft version and prepare its own game folder.");
            return;
        }
        for(InstanceManager.Profile profile:profiles) {
            boolean selected=profile.name.equals(selectedInstance());
            boolean installed=instanceManager.isInstalled(profile);
            LinearLayout panel=createCard(profile.name, (selected?"ACTIVE INSTANCE  •  ":"")+
                profile.version+"  •  "+profile.loader+"  •  "+(installed?"CORE FILES READY":"NOT DOWNLOADED"));
            panel.addView(text("GAME FOLDER  /game/instances/"+profile.id,10,false,getColor(R.color.veyron_muted)));
            panel.addView(action(selected?"ACTIVE":"SET AS ACTIVE",()->{
                getPreferences(0).edit().putString(SELECTED,profile.name).apply();
                toast(profile.name+" selected"); showPage("Instances");
            }));
            panel.addView(action(installed?"REINSTALL VERSION CORE":"DOWNLOAD INSTANCE VERSION",()->downloadInstance(profile)));
            panel.addView(action("RENAME INSTANCE",()->renameInstance(profile)));
            panel.addView(action("DELETE PROFILE",()->deleteInstance(profile)));
            LinearLayout.LayoutParams lp=new LinearLayout.LayoutParams(-1,-2);
            lp.bottomMargin=dp(12); content.addView(panel,lp);
        }
    }

    private void openCreateInstance() {
        if(!versionCatalog.isEmpty()) { showCreateInstanceDialog(); return; }
        Toast.makeText(this,"Loading Minecraft versions…",Toast.LENGTH_SHORT).show();
        versionManager.fetchVersions(new VersionManager.VersionsCallback() {
            @Override public void onSuccess(List<VersionManager.Version> versions,String release,String snapshot) {
                runOnUiThread(()->{
                    versionCatalog=versions; latestRelease=release; latestSnapshot=snapshot;
                    if("latest".equals(selectedVersion())&&!release.isEmpty())
                        getPreferences(0).edit().putString(SELECTED_VERSION,release).apply();
                    if(!isFinishing()) showCreateInstanceDialog();
                });
            }
            @Override public void onError(Exception error) {
                runOnUiThread(()->new AlertDialog.Builder(MainActivity.this)
                    .setTitle("Version catalogue unavailable").setMessage(safeMessage(error))
                    .setPositiveButton("RETRY", (d,w)->openCreateInstance())
                    .setNegativeButton("CANCEL",null).show());
            }
        });
    }

    private void showCreateInstanceDialog() {
        LinearLayout form=new LinearLayout(this); form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(8),dp(6),dp(8),dp(2));
        EditText name=new EditText(this); name.setSingleLine(true);
        name.setText("Minecraft "+(instanceManager.all().size()+1));
        name.setHint("Instance name");
        form.addView(text("INSTANCE NAME",10,true,getColor(R.color.veyron_muted)));
        form.addView(name);
        form.addView(text("MINECRAFT VERSION",10,true,getColor(R.color.veyron_muted)));
        ArrayList<String> versionNames=new ArrayList<>();
        for(VersionManager.Version v:versionCatalog) if("release".equals(v.type)) versionNames.add(v.id);
        if(versionNames.isEmpty()&&!latestRelease.isEmpty())versionNames.add(latestRelease);
        if(versionNames.isEmpty())versionNames.add("latest");
        Spinner versionPicker=new Spinner(this);
        ArrayAdapter<String> versionAdapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,versionNames);
        versionPicker.setAdapter(versionAdapter);
        int defaultIndex=versionNames.indexOf(latestRelease);
        if(defaultIndex>=0)versionPicker.setSelection(defaultIndex);
        form.addView(versionPicker);
        form.addView(text("MOD LOADER",10,true,getColor(R.color.veyron_muted)));
        Spinner loaderPicker=new Spinner(this);
        ArrayAdapter<String> loaderAdapter=new ArrayAdapter<>(this,android.R.layout.simple_spinner_dropdown_item,
            new String[]{"Vanilla","Fabric (coming soon)","Forge (coming soon)","NeoForge (coming soon)"});
        loaderPicker.setAdapter(loaderAdapter); form.addView(loaderPicker);
        new AlertDialog.Builder(this).setTitle("Create Minecraft instance").setView(form)
            .setPositiveButton("CREATE INSTANCE",(d,w)->{
                String instanceName=name.getText().toString().trim();
                if(instanceName.isEmpty()){toast("Enter an instance name.");return;}
                for(InstanceManager.Profile p:instanceManager.all()) if(p.name.equalsIgnoreCase(instanceName)){toast("An instance with that name already exists.");return;}
                String loader=(String)loaderPicker.getSelectedItem();
                if(!"Vanilla".equals(loader)){toast("This loader is not available yet. Choose Vanilla for now.");return;}
                String version=(String)versionPicker.getSelectedItem();
                try {
                    InstanceManager.Profile created=instanceManager.create(instanceName,version,"Vanilla");
                    getPreferences(0).edit().putString(SELECTED,created.name).apply();
                    showPage("Instances");
                    new AlertDialog.Builder(this).setTitle("Instance created")
                        .setMessage(instanceName+" is ready to download its Minecraft "+version+" core files. Libraries, assets and runtime are still separate setup stages.")
                        .setPositiveButton("DOWNLOAD NOW",(dialog,which)->downloadInstance(created))
                        .setNegativeButton("LATER",null).show();
                } catch(Exception error){toast("Could not create instance: "+safeMessage(error));}
            }).setNegativeButton("CANCEL",null).show();
    }

    private VersionManager.Version findVersion(String id) {
        for(VersionManager.Version v:versionCatalog) if(v.id.equals(id)) return v;
        return null;
    }

    private void downloadInstance(InstanceManager.Profile profile) {
        VersionManager.Version version=findVersion(profile.version);
        if(version==null) {
            versionManager.fetchVersions(new VersionManager.VersionsCallback() {
                @Override public void onSuccess(List<VersionManager.Version> versions,String release,String snapshot) {
                    runOnUiThread(()->{versionCatalog=versions;latestRelease=release;latestSnapshot=snapshot;
                        if(!isFinishing())downloadInstance(profile);});
                }
                @Override public void onError(Exception error) {
                    runOnUiThread(()->new AlertDialog.Builder(MainActivity.this).setTitle("Could not load version")
                        .setMessage(safeMessage(error)).setPositiveButton("OK",null).show());
                }
            });
            return;
        }
        ProgressDialog progress=new ProgressDialog(this);
        progress.setTitle("Preparing "+profile.name);
        progress.setMessage("Connecting to official Minecraft version files…");
        progress.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        progress.setCancelable(false); progress.show();
        versionManager.install(version,instanceManager.gameDirectory(profile),new VersionManager.InstallCallback() {
            @Override public void onProgress(String message){runOnUiThread(()->{if(!isFinishing())progress.setMessage(message);});}
            @Override public void onSuccess(File directory){runOnUiThread(()->{
                if(progress.isShowing())progress.dismiss();
                if(isFinishing())return;
                new AlertDialog.Builder(MainActivity.this).setTitle("Core download complete")
                    .setMessage(profile.name+" now has the verified Minecraft "+profile.version+" metadata and client JAR in its own game folder. Libraries, assets, loader setup and Java runtime are not installed yet.")
                    .setPositiveButton("DONE",(d,w)->showPage("Instances")).show();
            });}
            @Override public void onError(Exception error){runOnUiThread(()->{
                if(progress.isShowing())progress.dismiss();
                if(!isFinishing())new AlertDialog.Builder(MainActivity.this).setTitle("Instance download failed")
                    .setMessage(safeMessage(error)).setPositiveButton("OK",null).show();
            });}
        });
    }

    private void renameInstance(InstanceManager.Profile profile) {
        EditText input=new EditText(this); input.setSingleLine(true); input.setText(profile.name);
        new AlertDialog.Builder(this).setTitle("Rename instance").setView(input)
            .setPositiveButton("SAVE",(d,w)->{
                String value=input.getText().toString().trim(); if(value.isEmpty())return;
                for(InstanceManager.Profile p:instanceManager.all())if(!p.id.equals(profile.id)&&p.name.equalsIgnoreCase(value)){toast("Name already exists.");return;}
                String old=profile.name;
                try {instanceManager.rename(profile,value); if(old.equals(selectedInstance()))getPreferences(0).edit().putString(SELECTED,value).apply(); showPage("Instances");}
                catch(Exception error){toast(safeMessage(error));}
            }).setNegativeButton("CANCEL",null).show();
    }

    private void deleteInstance(InstanceManager.Profile profile) {
        new AlertDialog.Builder(this).setTitle("Delete "+profile.name+"?")
            .setMessage("This removes the instance from your library. Its files are kept safely on storage for now; no game data will be erased.")
            .setPositiveButton("REMOVE",(d,w)->{
                instanceManager.delete(profile);
                if(profile.name.equals(selectedInstance())) {
                    List<InstanceManager.Profile> remaining=instanceManager.all();
                    getPreferences(0).edit().putString(SELECTED,remaining.isEmpty()?"":remaining.get(0).name).apply();
                }
                showPage("Instances");
            }).setNegativeButton("CANCEL",null).show();
    }

    private InstanceManager.Profile selectedProfile() {
        String selected=getPreferences(0).getString(SELECTED,"Default");
        List<InstanceManager.Profile> all=instanceManager.all();
        for(InstanceManager.Profile p:all)if(p.name.equals(selected)||p.id.equals(selected))return p;
        return all.isEmpty()?null:all.get(0);
    }
    private String selectedInstance() {
        InstanceManager.Profile p=selectedProfile();
        return p==null?"No instance selected":p.name;
    }
    private void seedInstances() {
        List<InstanceManager.Profile> profiles=instanceManager.all();
        if(profiles.isEmpty()) {
            try {
                InstanceManager.Profile created=instanceManager.create("Default",selectedVersion(),"Vanilla");
                getPreferences(0).edit().putString(SELECTED,created.name).apply();
            } catch(Exception ignored) {}
        } else {
            instanceManager.save(profiles); // migrate old name-only profiles to structured instance records
            String selected=getPreferences(0).getString(SELECTED,profiles.get(0).name);
            boolean found=false;
            for(InstanceManager.Profile p:profiles)if(p.name.equals(selected)||p.id.equals(selected))found=true;
            if(!found)getPreferences(0).edit().putString(SELECTED,profiles.get(0).name).apply();
        }
    }

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
