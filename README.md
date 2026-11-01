# VEYRON Launcher

VEYRON Launcher is an Android launcher for Minecraft: Java Edition, built on the pinned MojoLauncher Android source base and developed with a VEYRON-owned identity and interface.

## Source architecture

- Android application: MojoLauncher source at `vendor/mojolauncher/app_mojo`.
- Upstream source revision: `417bc82cc3d505e6ade6901040ffd4238af2df45` (`v3_openjdk`).
- VEYRON branding overlay: `scripts/prepare_veyron_mojo.py`.
- Legacy prototype: `app/` (not the application module used by the current build).
- Third-party licensing: see `licenses/` and the notices shipped with the upstream source.

The uploaded source archive is retained in the repository when present. The pinned Android launcher source revision is used for reproducible builds, including its nested native and installer modules.

## Build

The GitHub Actions workflow checks out all submodules, installs Android SDK 36 and NDK 29.0.14206865, applies VEYRON package branding, then runs:

```bash
python3 scripts/prepare_veyron_mojo.py
gradle :app:assembleFullDebug
```

The resulting APK is uploaded as the `VEYRON-Launcher-debug` workflow artifact.

## Important

Mojo's upstream implementation is the engine foundation, not a claim that every function has already been tested under the VEYRON application ID. Validate sign-in, downloads, instance migration, runtime setup, rendering, and game launch on physical devices. Keep the LGPL-3.0 and all third-party notices intact.

VEYRON Launcher is an independent community project and is not affiliated with Mojang or Microsoft.
