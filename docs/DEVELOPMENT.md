# VEYRON Launcher — Development Setup

## Current state

This commit establishes the original Android Java/XML application foundation and a landscape home screen. It does **not** yet implement Microsoft authentication, Minecraft installation, or game execution.

## Build

Open the repository in Android Studio with Android SDK 35 installed. Use JDK 17 and Gradle 8.9 or a compatible Gradle installation. The Android Gradle Plugin is 8.7.3.

The application module is `:app`; application ID: `com.veyron.launcher`.

## Microsoft sign-in integration

Before enabling sign-in:

1. Register VEYRON as a native/public-client application in Microsoft Entra.
2. Configure the supported mobile redirect URI and required Minecraft/Xbox API permissions/scopes.
3. Implement authorization-code flow with PKCE using the system browser.
4. Exchange the authorization code securely, then perform the Xbox Live, XSTS, and Minecraft Services token/profile/entitlement steps.
5. Keep tokens out of source control and logs; protect persisted credentials with Android Keystore-backed encryption.

Never collect a Microsoft password in an in-app form. Do not mark a user as authenticated until Minecraft profile and entitlement checks succeed.

## Minecraft execution integration

Minecraft Java Edition does not run on Android as an ordinary desktop process. A compatible Java runtime, native-library loading strategy, graphics renderer, input handling, audio integration, and lifecycle management are required. These must be implemented and tested on real devices before enabling the Play action.

Use official version metadata and verify downloaded artifacts. Do not bundle copyrighted game assets or bypass ownership checks.
