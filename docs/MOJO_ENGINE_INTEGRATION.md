# MojoLauncher engine integration

## Pinned source

- Upstream: https://github.com/MojoLauncher/MojoLauncher
- Branch: `v3_openjdk`
- Pinned commit: `417bc82cc3d505e6ade6901040ffd4238af2df45`
- License: GNU LGPL-3.0
- Local path: `vendor/mojolauncher`

The original ZIP uploaded to the VEYRON branch remains unchanged. The submodule points to the matching public source branch so the complete source and its nested native submodules can be checked out reproducibly without trying to decode a binary ZIP through the GitHub text-file API.

## What the engine contains

The pinned source includes Android Java/XML launcher code, instance/profile installation, Minecraft version and artifact download tasks, Microsoft/Xbox/Minecraft authentication, Java runtime management, mod-loader installation flows, game activity/lifecycle handling, and JNI/native integration through GLFW, SDL, and mojoexec.

Its Gradle build is a separate multi-module Android project. It uses its own Android Gradle Plugin and native toolchain configuration, so it must not be blindly mixed into VEYRON's existing `:app` build.

## Integration plan

1. Keep VEYRON's existing `:app` UI, package identity, and instance UX intact.
2. Map Mojo's engine responsibilities behind VEYRON-owned Java interfaces for account, installation, runtime, and launch operations.
3. Resolve Gradle module boundaries and native submodules before wiring engine classes into the APK.
4. Keep upstream and third-party license notices with any reused or modified components. Review each bundled component's own license; the top-level LGPL notice does not replace third-party notices.
5. Verify downloads, account ownership, renderer setup, and launch lifecycle on physical Android devices before exposing a feature as ready.

## Current integration status

This commit only pins and checks out the upstream source. It does not yet compile Mojo's modules into the VEYRON APK or activate the features listed above. The existing VEYRON build remains on its original `:app` module.
