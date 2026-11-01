# MojoLauncher source integration

## Source of truth

- User-provided archive in this repository: `MojoLauncher-3_openjdk (1).zip` (25,216,098 bytes; retained unchanged).
- Build source: the matching public MojoLauncher `v3_openjdk` source, pinned as submodule at `vendor/mojolauncher`.
- Pinned upstream commit: `417bc82cc3d505e6ade6901040ffd4238af2df45`.
- License: GNU LGPL-3.0; preserve upstream and bundled third-party notices.

The GitHub file API exposes repository ZIPs as binary metadata rather than extractable text. The submodule therefore provides the same source revision in a reproducible checkout, including its nested GLFW, SDL, mojoexec, NoBlocklist, and installer dependencies. The uploaded ZIP is not deleted or modified.

## Build integration

The Android application module `:app` now maps directly to `vendor/mojolauncher/app_mojo`. Its required installer and native binding modules are mapped from the same pinned checkout in `settings.gradle`. The former VEYRON prototype under `/app` is retained as legacy reference code and is no longer the application being assembled.

Before building, `scripts/prepare_veyron_mojo.py` applies the VEYRON application ID and display name to the checked-out source. This keeps the upstream submodule clean and makes branding changes reproducible. The Android workflow checks out submodules recursively, installs the upstream Android SDK/NDK requirements, and builds the `fullDebug` variant.

## Functional baseline

Using Mojo as the application base brings its existing launcher architecture into the build: instance/profile management, version and artifact installation, supported authentication flows, runtime preparation, loader installers, game activity/lifecycle, and native renderer/input integrations. Each feature still needs verification in the VEYRON package and on physical devices; source presence is not proof that every feature works on every device.

## Next VEYRON work

1. Replace the default Mojo home presentation with VEYRON's landscape-first UI while keeping its real instance, account, install, and launch callbacks.
2. Apply VEYRON branding to settings, dialogs, icons, notifications, and game UI entry points.
3. Audit permissions, package authorities, renderer availability, and all third-party notices.
4. Build and test the full debug APK on ARM64 Android devices before declaring launch support ready.
