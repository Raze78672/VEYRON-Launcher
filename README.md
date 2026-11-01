# VEYRON Launcher

Original Android launcher project for Minecraft: Java Edition. Native Java + Android XML, landscape-first.

## Current foundation
- Android app module and Gradle build setup.
- Landscape launcher shell with navigation for Home, Instances, Versions, Downloads, Mods, Worlds, Accounts, Java & Runtime, Settings, and Console.
- Local instance profile create / select / rename / delete, persisted with app preferences.
- Clear feature status labels; unfinished authentication and game launch are not presented as working.

## Not implemented yet
Microsoft OAuth/PKCE and Xbox/Minecraft token exchange, official version metadata and installer, per-instance game directories, downloads, mod-loader installation, Java runtime integration, renderer, game process launch, worlds scanning, and live console output.

The Play action is deliberately disabled until the Android runtime and renderer are integrated and tested. Microsoft credentials must never be collected in an in-app password form; use Microsoft's supported browser-based sign-in.

## Build
Open the project in Android Studio with JDK 17 and Android SDK 35, then run `gradle assembleDebug`.

## Architecture direction
Keep UI, account authentication, version metadata, downloads, instance storage, runtime, renderer, and game lifecycle as separate components. Do not bundle private OAuth secrets in the APK. Verify official metadata and downloaded file hashes. Test renderer/runtime changes on physical Android devices.

## Project status
Early development. UI and local instance-profile foundation only; not yet a functional Minecraft launcher.
