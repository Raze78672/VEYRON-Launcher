#!/usr/bin/env python3
"""Apply VEYRON package identity to the pinned MojoLauncher Android app before build.

The upstream source remains an unmodified, pinned git submodule. These deterministic
patches are applied only in the build workspace and are version-controlled here.
"""
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
MOJO = ROOT / "vendor" / "mojolauncher"
APP = MOJO / "app_mojo"

def replace_once(path: Path, old: str, new: str) -> None:
    text = path.read_text(encoding="utf-8")
    count = text.count(old)
    if count == 0:
        raise SystemExit(f"Expected text not found in {path.relative_to(ROOT)}: {old!r}")
    path.write_text(text.replace(old, new), encoding="utf-8")

gradle = APP / "build.gradle"
replace_once(gradle, 'applicationId "git.artdeell.mjlaunch"', 'applicationId "com.veyron.launcher"')
replace_once(gradle, "'git.artdeell.mjlaunch.debug'", "'com.veyron.launcher.debug'")
replace_once(gradle, "'git.artdeell.mjlaunch.scoped.gamefolder.debug'", "'com.veyron.launcher.scoped.gamefolder.debug'")
replace_once(gradle, "'git.artdeell.mjlaunch.scoped.gamefolder'", "'com.veyron.launcher.scoped.gamefolder'")

strings = APP / "src" / "main" / "res" / "values" / "strings.xml"
replace_once(strings, '<string name="app_short_name">MJLauncher</string>',
             '<string name="app_short_name">VEYRON Launcher</string>')
replace_once(strings, 'MJLauncher has unexpectedly crashed',
             'VEYRON Launcher has unexpectedly crashed')

print("VEYRON identity applied to MojoLauncher source.")
