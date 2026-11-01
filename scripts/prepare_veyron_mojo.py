#!/usr/bin/env python3
"""Apply VEYRON identity and native Android UI to the pinned launcher source."""
from pathlib import Path
import re

ROOT = Path(__file__).resolve().parents[1]
MOJO = ROOT / "vendor" / "mojolauncher"
APP = MOJO / "app_mojo"
RES = APP / "src" / "main" / "res"

def replace_once(path: Path, old: str, new: str) -> None:
    source = path.read_text(encoding="utf-8")
    if old not in source:
        raise SystemExit(f"Expected text not found in {path.relative_to(ROOT)}: {old!r}")
    path.write_text(source.replace(old, new), encoding="utf-8")

def write_resource(relative: str, content: str) -> None:
    target = RES / relative
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(content.strip() + "\n", encoding="utf-8")

# Lightweight springy press feedback for VEYRON controls.
jelly_java = APP / "src" / "main" / "java" / "git" / "artdeell" / "mojo" / "utils" / "VeyronJelly.java"
jelly_java.parent.mkdir(parents=True, exist_ok=True)
jelly_java.write_text(r"""
package git.artdeell.mojo.utils;

import android.view.MotionEvent;
import android.view.View;
import android.view.animation.OvershootInterpolator;

public final class VeyronJelly {
    private VeyronJelly() {}

    public static void attach(View view) {
        if (view == null) return;
        view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        view.setOnTouchListener((target, event) -> {
            switch (event.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    target.animate().cancel();
                    target.animate().scaleX(0.94f).scaleY(0.94f)
                            .setDuration(85L).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    target.animate().cancel();
                    target.animate().scaleX(1f).scaleY(1f)
                            .setDuration(390L)
                            .setInterpolator(new OvershootInterpolator(2.1f))
                            .start();
                    break;
            }
            return false;
        });
    }
}
""".strip() + "\n", encoding="utf-8")

# VEYRON emblem: a compact vector mark, used by every Android launcher icon size.
write_resource("drawable/ic_veyron_mark.xml", r"""
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp"
    android:viewportWidth="256" android:viewportHeight="256">
    <path android:fillColor="#16C7B4"
        android:pathData="M24,36 L78,36 L128,156 L178,36 L232,36 L128,232 Z" />
    <path android:fillColor="#0B171A"
        android:pathData="M98,36 L158,36 L128,105 Z" />
    <path android:fillColor="#DFFBF7"
        android:pathData="M111,166 L128,204 L145,166 L163,166 L128,240 L93,166 Z" />
</vector>
""")

write_resource("drawable/veyron_shell_background.xml", r"""
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient android:type="linear" android:angle="0"
        android:startColor="#0A1216" android:centerColor="#101C20" android:endColor="#0A171A" />
</shape>
""")

write_resource("drawable/veyron_home_background.xml", r"""
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <gradient android:type="linear" android:angle="0"
        android:startColor="#132329" android:centerColor="#0B151A" android:endColor="#10191D" />
</shape>
""")

write_resource("drawable/veyron_panel_background.xml", r"""
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#E6111D22" />
    <corners android:radius="16dp" />
    <stroke android:width="1dp" android:color="#314A5055" />
</shape>
""")

# Landscape app shell: persistent Home / Settings rail, account control and full-height content.
write_resource("layout/activity_mojo_ui.xml", r"""
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:orientation="horizontal" android:background="@drawable/veyron_shell_background">
    <LinearLayout
        android:id="@+id/veyron_navigation_rail"
        android:layout_width="76dp" android:layout_height="match_parent"
        android:orientation="vertical" android:gravity="center_horizontal"
        android:paddingTop="12dp" android:paddingBottom="12dp"
        android:background="#E90D171A">
        <ImageView
            android:layout_width="40dp" android:layout_height="40dp"
            android:src="@drawable/ic_veyron_mark" android:contentDescription="VEYRON logo" />
        <TextView
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:layout_marginTop="5dp" android:text="VEYRON"
            android:textColor="#EAFBFA" android:textStyle="bold"
            android:textSize="9sp" android:letterSpacing="0.16" />
        <View
            android:layout_width="32dp" android:layout_height="1dp"
            android:layout_marginTop="18dp" android:background="#31505A" />
        <ImageButton
            android:id="@+id/home_nav_button"
            android:layout_width="48dp" android:layout_height="48dp"
            android:layout_marginTop="18dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@drawable/ic_px_home" android:scaleType="centerInside"
            android:padding="12dp" android:tint="#55D8C8"
            android:contentDescription="Home" />
        <TextView
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="HOME" android:textSize="8sp" android:textStyle="bold"
            android:letterSpacing="0.10" android:textColor="#55D8C8" />
        <ImageButton
            android:id="@+id/setting_button"
            android:layout_width="48dp" android:layout_height="48dp"
            android:layout_marginTop="14dp"
            android:background="?attr/selectableItemBackgroundBorderless"
            android:src="@drawable/ic_px_sliders" android:scaleType="centerInside"
            android:padding="12dp" android:tint="#A7B9BB"
            android:contentDescription="Settings" />
        <TextView
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="SETTINGS" android:textSize="8sp" android:textStyle="bold"
            android:letterSpacing="0.06" android:textColor="#A7B9BB" />
        <View
            android:layout_width="1dp" android:layout_height="0dp"
            android:layout_weight="1" />
        <TextView
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="JAVA" android:textSize="8sp"
            android:letterSpacing="0.12" android:textColor="#668087" />
    </LinearLayout>

    <androidx.constraintlayout.widget.ConstraintLayout
        android:layout_width="0dp" android:layout_height="match_parent"
        android:layout_weight="1" android:background="@drawable/veyron_shell_background">
        <LinearLayout
            android:id="@+id/veyron_header"
            android:layout_width="0dp" android:layout_height="62dp"
            android:orientation="horizontal" android:gravity="center_vertical"
            android:paddingStart="16dp" android:paddingEnd="14dp"
            android:background="#CC0D191D"
            app:layout_constraintTop_toTopOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent">
            <ImageView android:layout_width="30dp" android:layout_height="30dp"
                android:src="@drawable/ic_veyron_mark" android:contentDescription="VEYRON" />
            <LinearLayout android:layout_width="0dp" android:layout_height="wrap_content"
                android:layout_weight="1" android:orientation="vertical" android:layout_marginStart="9dp">
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                    android:text="VEYRON LAUNCHER" android:textColor="#F0F7F7"
                    android:textSize="12sp" android:textStyle="bold" android:letterSpacing="0.10" />
                <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
                    android:text="JAVA EDITION  •  GAME HUB" android:textColor="#769397"
                    android:textSize="8sp" android:letterSpacing="0.08" />
            </LinearLayout>
            <com.kdt.mcgui.AccountSpinner
                android:id="@+id/account_spinner"
                android:layout_width="220dp" android:layout_height="46dp"
                android:background="@drawable/veyron_account_chip"
                android:dropDownWidth="240dp" android:dropDownVerticalOffset="4dp"
                android:paddingStart="8dp" android:paddingEnd="8dp"
                tools:ignore="RtlSymmetry" />
        </LinearLayout>
        <androidx.fragment.app.FragmentContainerView
            android:id="@+id/container_fragment"
            android:name="git.artdeell.mojo.fragments.MainMenuFragment"
            android:layout_width="0dp" android:layout_height="0dp"
            app:layout_constraintTop_toBottomOf="@id/veyron_header"
            app:layout_constraintBottom_toTopOf="@id/progress_layout"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />
        <com.kdt.mcgui.ProgressLayout
            android:id="@+id/progress_layout"
            android:layout_width="0dp" android:layout_height="wrap_content"
            android:visibility="gone"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toEndOf="parent" />
    </androidx.constraintlayout.widget.ConstraintLayout>
</LinearLayout>
""")

# The app is forced to landscape, so Android selects layout-land at runtime.
# Keep the landscape-qualified activity shell identical to the base shell.
write_resource("layout-land/activity_mojo_ui.xml",
               (RES / "layout" / "activity_mojo_ui.xml").read_text(encoding="utf-8"))

select_auth = MOJO / "app_mojo" / "src" / "main" / "java" / "git" / "artdeell" / "mojo" / "fragments" / "SelectAuthFragment.java"
replace_once(select_auth, '        mElyByButton.setOnClickListener(v -> launchAuthFragment(ElyByLoginFragment.class, ElyByLoginFragment.TAG));', '        mElyByButton.setOnClickListener(v -> launchAuthFragment(ElyByLoginFragment.class, ElyByLoginFragment.TAG));\n        git.artdeell.mojo.utils.VeyronJelly.attach(mMicrosoftButton);\n        git.artdeell.mojo.utils.VeyronJelly.attach(mLocalButton);\n        git.artdeell.mojo.utils.VeyronJelly.attach(mElyByButton);')

main_menu = MOJO / "app_mojo" / "src" / "main" / "java" / "git" / "artdeell" / "mojo" / "fragments" / "MainMenuFragment.java"
replace_once(main_menu, '        mOpenDirectoryButton.setOnClickListener((v)-> openGameDirectory(v.getContext()));', '        mOpenDirectoryButton.setOnClickListener((v)-> openGameDirectory(v.getContext()));\n        git.artdeell.mojo.utils.VeyronJelly.attach(mDiscordButton);\n        git.artdeell.mojo.utils.VeyronJelly.attach(mCustomControlButton);\n        git.artdeell.mojo.utils.VeyronJelly.attach(mInstallJarButton);\n        git.artdeell.mojo.utils.VeyronJelly.attach(mShareLogsButton);\n        git.artdeell.mojo.utils.VeyronJelly.attach(mOpenDirectoryButton);\n        git.artdeell.mojo.utils.VeyronJelly.attach(mEditProfileButton);\n        git.artdeell.mojo.utils.VeyronJelly.attach(mPlayButton);')

# Landscape home: workspace/action panel on the left and selected-version / Play panel on the right.
write_resource("layout-land/fragment_launcher.xml", r"""
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    xmlns:tools="http://schemas.android.com/tools"
    android:id="@+id/fragment_menu_main"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:background="@drawable/veyron_home_background">
    <androidx.constraintlayout.widget.Guideline
        android:id="@+id/veyron_content_split"
        android:layout_width="wrap_content" android:layout_height="wrap_content"
        android:orientation="vertical" app:layout_constraintGuide_percent="0.59" />

    <androidx.constraintlayout.widget.ConstraintLayout
        android:id="@+id/veyron_workspace_panel"
        android:layout_width="0dp" android:layout_height="0dp"
        android:layout_marginStart="12dp" android:layout_marginTop="8dp"
        android:layout_marginEnd="7dp" android:layout_marginBottom="10dp"
        android:padding="14dp" android:background="@drawable/veyron_panel_background"
        app:layout_constraintTop_toTopOf="parent" app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent" app:layout_constraintEnd_toStartOf="@id/veyron_content_split">
        <TextView
            android:id="@+id/veyron_home_eyebrow"
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="VEYRON  /  JAVA EDITION" android:textColor="#55D8C8"
            android:textSize="9sp" android:textStyle="bold" android:letterSpacing="0.15"
            app:layout_constraintTop_toTopOf="parent" app:layout_constraintStart_toStartOf="parent" />
        <TextView
            android:id="@+id/veyron_home_title"
            android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_marginTop="5dp" android:text="YOUR MINECRAFT SPACE"
            android:textColor="#F0F7F7" android:textSize="22sp" android:textStyle="bold"
            app:layout_constraintTop_toBottomOf="@id/veyron_home_eyebrow"
            app:layout_constraintStart_toStartOf="parent" app:layout_constraintEnd_toEndOf="parent" />
        <TextView
            android:id="@+id/veyron_home_subtitle"
            android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_marginTop="3dp"
            android:text="Manage your setup, customize controls and jump back in."
            android:textColor="#9EB3B7" android:textSize="10sp"
            app:layout_constraintTop_toBottomOf="@id/veyron_home_title"
            app:layout_constraintStart_toStartOf="parent" app:layout_constraintEnd_toEndOf="parent" />
        <LinearLayout
            android:id="@+id/veyron_actions"
            android:layout_width="0dp" android:layout_height="0dp"
            android:layout_marginTop="12dp" android:orientation="vertical"
            app:layout_constraintTop_toBottomOf="@id/veyron_home_subtitle"
            app:layout_constraintBottom_toBottomOf="parent"
            app:layout_constraintStart_toStartOf="parent" app:layout_constraintEnd_toEndOf="parent">
            <LinearLayout
                android:layout_width="match_parent" android:layout_height="0dp"
                android:layout_weight="1" android:orientation="horizontal">
                <com.kdt.mcgui.LauncherMenuButton
                    android:id="@+id/news_button" style="@style/LauncherMenuButton.Universal"
                    android:visibility="gone" android:layout_width="0dp" android:layout_height="match_parent"
                    android:layout_weight="1" android:layout_margin="3dp"
                    android:drawableStart="@drawable/ic_px_book" android:text="@string/mcl_tab_wiki"
                    android:textSize="10sp" />
                <com.kdt.mcgui.LauncherMenuButton
                    android:id="@+id/social_media_button" style="@style/LauncherMenuButton.Universal"
                    android:layout_width="0dp" android:layout_height="match_parent"
                    android:layout_weight="1" android:layout_margin="3dp"
                    android:drawableStart="@drawable/ic_social_media" android:text="@string/mcl_button_social_media"
                    android:textSize="10sp" />
            </LinearLayout>
            <LinearLayout
                android:layout_width="match_parent" android:layout_height="0dp"
                android:layout_weight="1" android:orientation="horizontal">
                <com.kdt.mcgui.LauncherMenuButton
                    android:id="@+id/custom_control_button" style="@style/LauncherMenuButton.Universal"
                    android:layout_width="0dp" android:layout_height="match_parent"
                    android:layout_weight="1" android:layout_margin="3dp"
                    android:drawableStart="@drawable/ic_px_gamepad" android:text="@string/mcl_option_customcontrol"
                    android:textSize="10sp" />
                <com.kdt.mcgui.LauncherMenuButton
                    android:id="@+id/install_jar_button" style="@style/LauncherMenuButton.Universal"
                    android:layout_width="0dp" android:layout_height="match_parent"
                    android:layout_weight="1" android:layout_margin="3dp"
                    android:drawableStart="@drawable/ic_px_java_run" android:text="@string/main_install_jar_file"
                    android:textSize="10sp" />
            </LinearLayout>
            <LinearLayout
                android:layout_width="match_parent" android:layout_height="0dp"
                android:layout_weight="1" android:orientation="horizontal">
                <com.kdt.mcgui.LauncherMenuButton
                    android:id="@+id/share_logs_button" style="@style/LauncherMenuButton.Universal"
                    android:layout_width="0dp" android:layout_height="match_parent"
                    android:layout_weight="1" android:layout_margin="3dp"
                    android:drawableStart="@drawable/ic_px_sharelog" android:text="@string/main_share_logs"
                    android:textSize="10sp" />
                <com.kdt.mcgui.LauncherMenuButton
                    android:id="@+id/open_files_button" style="@style/LauncherMenuButton.Universal"
                    android:layout_width="0dp" android:layout_height="match_parent"
                    android:layout_weight="1" android:layout_margin="3dp"
                    android:drawableStart="@drawable/ic_px_folder" android:text="@string/mcl_button_open_directory"
                    android:textSize="10sp" />
            </LinearLayout>
        </LinearLayout>
    </androidx.constraintlayout.widget.ConstraintLayout>

    <androidx.constraintlayout.widget.ConstraintLayout
        android:id="@+id/veyron_launch_panel"
        android:layout_width="0dp" android:layout_height="0dp"
        android:layout_marginStart="7dp" android:layout_marginTop="8dp"
        android:layout_marginEnd="12dp" android:layout_marginBottom="10dp"
        android:padding="16dp" android:background="@drawable/veyron_panel_background"
        app:layout_constraintTop_toTopOf="parent" app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toEndOf="@id/veyron_content_split" app:layout_constraintEnd_toEndOf="parent">
        <TextView
            android:id="@+id/veyron_launch_eyebrow"
            android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="GAME SESSION" android:textColor="#55D8C8"
            android:textSize="9sp" android:textStyle="bold" android:letterSpacing="0.15"
            app:layout_constraintTop_toTopOf="parent" app:layout_constraintStart_toStartOf="parent" />
        <TextView
            android:id="@+id/veyron_launch_title"
            android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_marginTop="6dp" android:text="READY TO PLAY?"
            android:textColor="#F0F7F7" android:textSize="20sp" android:textStyle="bold"
            app:layout_constraintTop_toBottomOf="@id/veyron_launch_eyebrow"
            app:layout_constraintStart_toStartOf="parent" app:layout_constraintEnd_toEndOf="parent" />
        <TextView
            android:id="@+id/veyron_launch_subtitle"
            android:layout_width="0dp" android:layout_height="wrap_content"
            android:layout_marginTop="3dp" android:text="Choose your profile and launch Minecraft."
            android:textColor="#9EB3B7" android:textSize="10sp"
            app:layout_constraintTop_toBottomOf="@id/veyron_launch_title"
            app:layout_constraintStart_toStartOf="parent" app:layout_constraintEnd_toEndOf="parent" />
        <View
            android:id="@+id/veyron_launch_divider"
            android:layout_width="0dp" android:layout_height="1dp"
            android:layout_marginTop="14dp" android:background="#29444A"
            app:layout_constraintTop_toBottomOf="@id/veyron_launch_subtitle"
            app:layout_constraintStart_toStartOf="parent" app:layout_constraintEnd_toEndOf="parent" />
        <com.kdt.mcgui.mcVersionSpinner
            android:id="@+id/mc_version_spinner"
            android:layout_width="0dp" android:layout_height="48dp"
            android:layout_marginTop="12dp" android:background="@android:color/transparent"
            android:drawableEnd="@drawable/spinner_arrow"
            app:drawableEndPadding="2dp" app:drawableEndSize="12dp"
            app:drawableStartIntegerScaling="true" app:drawableStartSize="36dp"
            app:layout_constraintTop_toBottomOf="@id/veyron_launch_divider"
            app:layout_constraintStart_toStartOf="parent"
            app:layout_constraintEnd_toStartOf="@id/edit_profile_button" />
        <ImageButton
            android:id="@+id/edit_profile_button"
            android:layout_width="42dp" android:layout_height="42dp"
            android:background="?android:attr/selectableItemBackground"
            android:src="@drawable/ic_px_edit" android:tint="#AFC5C7"
            android:contentDescription="Edit selected profile"
            app:layout_constraintTop_toTopOf="@id/mc_version_spinner"
            app:layout_constraintBottom_toBottomOf="@id/mc_version_spinner"
            app:layout_constraintEnd_toEndOf="parent" />
        <View
            android:id="@+id/_background_display_view"
            android:layout_width="0dp" android:layout_height="1dp"
            android:background="#29444A"
            app:layout_constraintTop_toTopOf="@id/play_button"
            app:layout_constraintStart_toStartOf="parent" app:layout_constraintEnd_toEndOf="parent" />
        <com.kdt.mcgui.MineButton
            android:id="@+id/play_button"
            android:layout_width="0dp" android:layout_height="54dp"
            android:layout_marginTop="12dp" android:text="@string/main_play"
            android:textAllCaps="true" android:textStyle="bold"
            app:layout_constraintTop_toBottomOf="@id/mc_version_spinner"
            app:layout_constraintStart_toStartOf="parent" app:layout_constraintEnd_toEndOf="parent" />
    </androidx.constraintlayout.widget.ConstraintLayout>
</androidx.constraintlayout.widget.ConstraintLayout>
""")

write_resource("drawable/veyron_account_chip.xml", r"""
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#18282D" /><corners android:radius="13dp" />
    <stroke android:width="1dp" android:color="#38545A" />
</shape>
""")
write_resource("drawable/veyron_account_item.xml", r"""
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#142126" /><corners android:radius="10dp" />
    <stroke android:width="1dp" android:color="#29444A" />
</shape>
""")
write_resource("layout/item_account.xml", r"""
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:tools="http://schemas.android.com/tools" xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="48dp"
    android:orientation="horizontal" android:gravity="center_vertical"
    android:paddingStart="8dp" android:paddingEnd="5dp"
    android:background="@drawable/veyron_account_item">
    <fr.spse.extended_view.ExtendedTextView
        android:id="@+id/account_item" android:layout_width="0dp"
        android:layout_height="match_parent" android:layout_weight="1"
        android:gravity="center_vertical" android:textColor="#EAFBFA"
        android:textSize="12sp" android:drawablePadding="9dp"
        android:paddingStart="5dp" android:drawableTint="#55D8C8"
        app:drawableStartSize="28dp" app:drawableEndSize="18dp"
        tools:text="Add account" tools:drawableStart="@drawable/ic_add"
        tools:ignore="RtlSymmetry" />
    <ImageView android:id="@+id/delete_account_button"
        android:layout_width="40dp" android:layout_height="match_parent"
        android:background="?attr/selectableItemBackground"
        android:src="@drawable/ic_px_trash" android:tint="#8FA6AA"
        android:padding="11dp" />
</LinearLayout>
""")


write_resource("layout/fragment_select_auth_method.xml", r"""
<FrameLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:background="@drawable/veyron_home_background"
    android:padding="22dp">
    <LinearLayout
        android:layout_width="match_parent" android:layout_height="wrap_content"
        android:layout_gravity="center" android:orientation="vertical"
        android:padding="22dp" android:background="@drawable/veyron_auth_panel">
        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="VEYRON ACCOUNT CENTER" android:textColor="#55D8C8"
            android:textSize="9sp" android:textStyle="bold" android:letterSpacing="0.16" />
        <TextView android:layout_width="match_parent" android:layout_height="wrap_content"
            android:layout_marginTop="7dp" android:text="Add your account"
            android:textColor="#F0F7F7" android:textSize="23sp" android:textStyle="bold" />
        <TextView android:layout_width="match_parent" android:layout_height="wrap_content"
            android:layout_marginTop="4dp" android:layout_marginBottom="18dp"
            android:text="Choose how you want to sign in to VEYRON Launcher."
            android:textColor="#9EB3B7" android:textSize="11sp" />
        <LinearLayout android:layout_width="match_parent" android:layout_height="wrap_content"
            android:orientation="horizontal" android:gravity="center_vertical">
            <com.kdt.mcgui.MineButton
                android:id="@+id/button_microsoft_authentication"
                android:layout_width="0dp" android:layout_height="68dp" android:layout_weight="1"
                android:layout_marginEnd="7dp" android:drawableStart="@drawable/ic_auth_ms"
                android:drawablePadding="9dp" android:gravity="center"
                android:text="MICROSOFT" android:textSize="10sp" android:textStyle="bold" />
            <com.kdt.mcgui.MineButton
                android:id="@+id/button_elyby_authentication"
                android:layout_width="0dp" android:layout_height="68dp" android:layout_weight="1"
                android:layout_marginHorizontal="7dp" android:drawableStart="@drawable/ic_auth_elyby"
                android:drawablePadding="9dp" android:gravity="center"
                android:text="ELY.BY" android:textSize="10sp" android:textStyle="bold" />
            <com.kdt.mcgui.MineButton
                android:id="@+id/button_local_authentication"
                android:layout_width="0dp" android:layout_height="68dp" android:layout_weight="1"
                android:layout_marginStart="7dp" android:drawableStart="@drawable/ic_veyron_mark"
                android:drawablePadding="9dp" android:gravity="center"
                android:text="OFFLINE" android:textSize="10sp" android:textStyle="bold" />
        </LinearLayout>
        <TextView android:layout_width="match_parent" android:layout_height="wrap_content"
            android:layout_marginTop="16dp"
            android:text="Your sign-in is handled securely by the selected account provider."
            android:textColor="#718A8F" android:textSize="9sp" />
    </LinearLayout>
</FrameLayout>
""")

write_resource("drawable/veyron_auth_panel.xml", r"""
<shape xmlns:android="http://schemas.android.com/apk/res/android" android:shape="rectangle">
    <solid android:color="#F0111E23" />
    <corners android:radius="20dp" />
    <stroke android:width="1dp" android:color="#38545A" />
</shape>
""")

write_resource("layout/fragment_local_login.xml", r"""
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent" android:layout_height="match_parent"
    android:background="@drawable/veyron_home_background" android:padding="22dp">
    <LinearLayout
        android:id="@+id/veyron_local_login_panel"
        android:layout_width="0dp" android:layout_height="wrap_content"
        android:orientation="vertical" android:padding="22dp"
        android:background="@drawable/veyron_auth_panel"
        app:layout_constraintWidth_percent="0.72"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent">
        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:text="VEYRON OFFLINE PROFILE" android:textColor="#55D8C8"
            android:textSize="9sp" android:textStyle="bold" android:letterSpacing="0.15" />
        <TextView android:layout_width="match_parent" android:layout_height="wrap_content"
            android:layout_marginTop="6dp" android:text="Create local account"
            android:textColor="#F0F7F7" android:textSize="22sp" android:textStyle="bold" />
        <TextView android:layout_width="match_parent" android:layout_height="wrap_content"
            android:layout_marginTop="4dp" android:text="Choose an in-game username for offline play."
            android:textColor="#9EB3B7" android:textSize="11sp" />
        <TextView android:layout_width="wrap_content" android:layout_height="wrap_content"
            android:layout_marginTop="16dp" android:text="@string/login_online_username_hint"
            android:textColor="#C8D8D9" android:textSize="10sp" />
        <com.kdt.mcgui.MineEditText
            android:id="@+id/login_edit_email" android:layout_width="match_parent"
            android:layout_height="44dp" android:layout_marginTop="5dp"
            android:imeOptions="flagNoExtractUi" android:inputType="textPersonName"
            android:textSize="14sp" />
        <com.kdt.mcgui.MineButton
            android:id="@+id/login_button" android:layout_width="match_parent"
            android:layout_height="48dp" android:layout_marginTop="14dp"
            android:onClick="loginMC" android:text="CREATE OFFLINE PROFILE"
            android:textColor="@android:color/white" android:textStyle="bold" />
    </LinearLayout>
</androidx.constraintlayout.widget.ConstraintLayout>
""")

replace_once(APP / "src" / "main" / "res" / "values" / "strings.xml", '<string name="social_media_invite" translatable="false">https://discord.gg/VHdwQFsaGX</string>', '<string name="social_media_invite" translatable="false">https://discord.gg/A8NwCBEe52</string>')
replace_once(APP / "src" / "main" / "res" / "values" / "strings.xml", '<string name="mcl_button_social_media">Discord</string>', '<string name="mcl_button_social_media">Discord Community</string>')

# Apply VEYRON colors to the existing launcher and preference theme.
colors = APP / "src" / "main" / "res" / "values" / "colors.xml"
for old, new in [
    ('<color name="minebutton_color">#91dffb</color>', '<color name="minebutton_color">#16C7B4</color>'),
    ('<color name="minebutton_secondary">#2a2a2a</color>', '<color name="minebutton_secondary">#18282D</color>'),
    ('<color name="background_app">#181818</color>', '<color name="background_app">#0B151A</color>'),
    ('<color name="background_overlay">#464646</color>', '<color name="background_overlay">#263A40</color>'),
    ('<color name="background_status_bar">#242424</color>', '<color name="background_status_bar">#0A1216</color>'),
    ('<color name="background_bottom_bar">#232323</color>', '<color name="background_bottom_bar">#101E23</color>'),
    ('<color name="secondary_text">#B2B2B2</color>', '<color name="secondary_text">#9EB3B7</color>'),
]:
    replace_once(colors, old, new)
styles = APP / "src" / "main" / "res" / "values" / "styles.xml"
style_data = styles.read_text(encoding="utf-8")
style_data = style_data.replace('<item name="windowNoTitle">true</item>',
    '<item name="windowNoTitle">true</item>' + chr(10) + '        <item name="colorAccent">#16C7B4</item>' + chr(10) + '        <item name="android:colorAccent">#16C7B4</item>' + chr(10) + '        <item name="android:textColorPrimary">#EAFBFA</item>' + chr(10) + '        <item name="android:textColorSecondary">#9EB3B7</item>')
styles.write_text(style_data, encoding="utf-8")
# Point any legacy MJ/Pojav logo resource references at the VEYRON emblem.
for path in APP.rglob("*"):
    if not path.is_file() or path.suffix.lower() not in {".xml", ".java", ".kt"}:
        continue
    try:
        data = path.read_text(encoding="utf-8")
    except UnicodeDecodeError:
        continue
    data = data.replace("R.drawable.ic_mojo_full", "R.drawable.ic_veyron_mark")
    data = data.replace("@drawable/ic_mojo_full", "@drawable/ic_veyron_mark")
    data = data.replace("R.drawable.notif_icon", "R.drawable.ic_veyron_mark")
    data = data.replace("@drawable/notif_icon", "@drawable/ic_veyron_mark")
    data = data.replace("R.drawable.ic_pojav", "R.drawable.ic_veyron_mark")
    data = data.replace("@drawable/ic_pojav", "@drawable/ic_veyron_mark")
    path.write_text(data, encoding="utf-8")
for path in APP.rglob("*"):
    if path.is_file() and (path.name == "ic_mojo_full.webp" or path.name == "notif_icon.png" or path.name.lower().startswith("ic_pojav")):
        path.unlink()

# Keep adaptive launcher icons and the pre-Android-26 vector icon consistent.
write_resource("mipmap-anydpi-v26/ic_launcher.xml", r"""
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_veyron_mark" />
</adaptive-icon>
""")
write_resource("mipmap-anydpi-v26/ic_launcher_round.xml", r"""
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/ic_launcher_background" />
    <foreground android:drawable="@drawable/ic_veyron_mark" />
</adaptive-icon>
""")
write_resource("values/ic_launcher_background.xml", r"""
<resources><color name="ic_launcher_background">#0B171A</color></resources>
""")

gradle = APP / "build.gradle"
replace_once(gradle, 'applicationId "git.artdeell.mjlaunch"', 'applicationId "com.veyron.launcher"')
replace_once(gradle, "'git.artdeell.mjlaunch.debug'", "'com.veyron.launcher.debug'")
replace_once(gradle, "'git.artdeell.mjlaunch.scoped.gamefolder.debug'", "'com.veyron.launcher.scoped.gamefolder.debug'")
replace_once(gradle, "'git.artdeell.mjlaunch.scoped.gamefolder'", "'com.veyron.launcher.scoped.gamefolder'")

manifest = APP / "src" / "main" / "AndroidManifest.xml"
replace_once(manifest, 'android:icon="@mipmap/ic_launcher"', 'android:icon="@drawable/ic_veyron_mark"')
replace_once(manifest, 'android:roundIcon="@mipmap/ic_launcher_round"', 'android:roundIcon="@drawable/ic_veyron_mark"')
replace_once(manifest,
    '<activity\n            android:name=".LauncherActivity"\n            android:label="@string/app_short_name"\n            android:windowSoftInputMode="adjustResize"/>',
    '<activity\n            android:name=".LauncherActivity"\n            android:label="@string/app_short_name"\n            android:screenOrientation="sensorLandscape"\n            android:windowSoftInputMode="adjustResize"/>')

account_spinner = APP / "src" / "main" / "java" / "com" / "kdt" / "mcgui" / "AccountSpinner.java"
replace_once(account_spinner, 'setBackgroundColor(getResources().getColor(R.color.background_status_bar));',
             'setBackgroundResource(R.drawable.veyron_account_chip);')

# A single product name across all translations and devices.
for strings in (APP / "src" / "main" / "res").glob("values*/strings.xml"):
    data = strings.read_text(encoding="utf-8")
    data = re.sub(r'(<string\s+name="app_short_name"[^>]*>).*?(</string>)',
                  r'\1VEYRON Launcher\2', data, flags=re.DOTALL)
    data = data.replace("MJLauncher has unexpectedly crashed", "VEYRON Launcher has unexpectedly crashed")
    data = data.replace("MojoLauncher", "VEYRON Launcher").replace("MJLauncher", "VEYRON Launcher")
    strings.write_text(data, encoding="utf-8")

launcher = APP / "src" / "main" / "java" / "git" / "artdeell" / "mojo" / "LauncherActivity.java"
replace_once(launcher, 'public boolean setFullscreen() {\n        return false;\n    }',
             'public boolean setFullscreen() {\n        return true;\n    }')
replace_once(launcher, 'private ImageButton mSettingsButton;\n',
             'private ImageButton mSettingsButton;\n    private ImageButton mHomeButton;\n')
replace_once(launcher, 'mSettingsButton.setOnClickListener(mSettingButtonListener);',
             'mSettingsButton.setOnClickListener(mSettingButtonListener);\n        mHomeButton.setOnClickListener(v -> Tools.backToMainMenu(this));\n        git.artdeell.mojo.utils.VeyronJelly.attach(mSettingsButton);\n        git.artdeell.mojo.utils.VeyronJelly.attach(mHomeButton);\n        git.artdeell.mojo.utils.VeyronJelly.attach(findViewById(R.id.account_spinner));')
replace_once(launcher, 'mSettingsButton = findViewById(R.id.setting_button);',
             'mSettingsButton = findViewById(R.id.setting_button);\n        mHomeButton = findViewById(R.id.home_nav_button);')
print("VEYRON app identity, universal icon, full-screen landscape shell and redesigned home applied.")
