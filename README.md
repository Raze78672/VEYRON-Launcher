<!--
  VEYRON Launcher • Project README
  Keep this document consistent across every maintained branch.
-->

<div align="center">

<img src="https://readme-typing-svg.demolab.com?font=Orbitron&weight=700&size=34&duration=2800&pause=900&color=8B7CFF&center=true&vCenter=true&width=720&lines=V+E+Y+R+O+N+%20LAUNCHER;Minecraft%3A+Java+Edition+on+Android;Built+for+the+next+session." alt="VEYRON Launcher animated title" />

<p><strong>A modern Android launcher project for Minecraft: Java Edition.</strong></p>

<p>
  <a href="https://github.com/Raze78672/VEYRON-Launcher"><img src="https://img.shields.io/badge/PROJECT-VEYRON-8B7CFF?style=for-the-badge&logo=android&logoColor=white" alt="VEYRON project" /></a>
  <img src="https://img.shields.io/badge/PLATFORM-ANDROID-3DDC84?style=for-the-badge&logo=android&logoColor=white" alt="Android" />
  <img src="https://img.shields.io/badge/GAME-MINECRAFT%20JAVA-5B8C5A?style=for-the-badge" alt="Minecraft Java Edition" />
  <img src="https://img.shields.io/badge/STATUS-IN%20DEVELOPMENT-FFB86B?style=for-the-badge" alt="In development" />
</p>

<img src="https://capsule-render.vercel.app/api?type=rect&height=2&color=8B7CFF" alt="" width="88%" />

</div>

## ✦ About VEYRON

**VEYRON Launcher** is an Android launcher project for **Minecraft: Java Edition**. It combines a VEYRON-owned identity and interface with a pinned Android launcher source foundation.

This repository is actively developed. The README describes the current source setup; it does not imply that every launcher feature has been fully validated on every device.

## ◈ Project overview

| Area | Details |
|---|---|
| Platform | Android |
| Target game | Minecraft: Java Edition |
| Application source | `vendor/mojolauncher/app_mojo` |
| Branding layer | `scripts/prepare_veyron_mojo.py` |
| Build system | Gradle |
| Android SDK | 36 |
| Android NDK | 29.0.14206865 |

## ⚙️ Source architecture

- **Launcher application:** `vendor/mojolauncher/app_mojo`
- **Pinned upstream revision:** `417bc82cc3d505e6ade6901040ffd4238af2df45` (`v3_openjdk`)
- **VEYRON branding overlay:** `scripts/prepare_veyron_mojo.py`
- **Legacy prototype:** `app/` — not the application module used by the current build.
- **Third-party notices:** `licenses/` and notices included with the upstream source.

The pinned source revision and its nested native and installer modules are used to support reproducible builds.

## 🚀 Build from source

### Requirements

- Android SDK 36
- Android NDK 29.0.14206865
- Python 3
- Gradle and the repository's checked-out submodules

### Build commands

Run these commands from the repository root:

```bash
python3 scripts/prepare_veyron_mojo.py
gradle :app:assembleFullDebug
```

The GitHub Actions workflow builds the debug variant and uploads the resulting APK as the **`VEYRON-Launcher-debug`** artifact.

## 🧪 Testing & compatibility

The upstream implementation is the engine foundation. Features must be tested under the VEYRON application identity on real devices before being considered verified.

Areas that need device validation include:

- Account sign-in
- Game and asset downloads
- Instance migration
- Runtime setup
- Rendering
- Game launch

If you find a bug, include your Android version, device model, steps to reproduce, and relevant logs when opening an issue.

## 🌌 Development

VEYRON is being developed step by step. Changes may land on different branches while features are being worked on.

- Keep changes focused and buildable.
- Preserve upstream notices and third-party licenses.
- Test changes on a physical Android device where possible.
- Do not describe an untested feature as complete.

## 📜 Licensing & acknowledgements

This project incorporates an upstream launcher source foundation. **Keep the LGPL-3.0 license and all third-party notices intact** when modifying or redistributing the project. Check the repository's `licenses/` directory and included notices for the applicable terms.

VEYRON Launcher is an independent community project and is **not affiliated with Mojang or Microsoft**. Minecraft is a trademark of Microsoft.

---

<div align="center">

<img src="https://capsule-render.vercel.app/api?type=rect&height=2&color=8B7CFF" alt="" width="88%" />

**VEYRON · BUILD. PLAY. REPEAT.**

<sub>Made for the Minecraft community • Android project</sub>

<br />

<img src="https://komarev.com/ghpvc/?username=Raze78672&repo=VEYRON-Launcher&style=flat-square&color=8B7CFF" alt="Repository profile views" />

</div>
