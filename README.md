# A06 Audio Engine

A lightweight Android audio-processing app tailored for the Samsung Galaxy A06 (SM-A065M), with a no-root, hybrid architecture.

## Current scope

- In-app PCM processing for audio played by the A06 Audio Engine.
- Five-band peaking EQ, conservative dynamics control, and a sample-peak safety limiter.
- Six selectable sound profiles.
- Local audio playback through Android's Storage Access Framework and Media3.
- Device/effect diagnostics and a shortcut to Android sound settings.

## Important platform boundary

Android audio effects and Media3 audio processors are attached to an app's own playback pipeline/session. A normal third-party app without privileged system access or root cannot apply a universal DSP chain to audio produced by YouTube, games, or other apps. The system-settings shortcut is provided so users can configure Samsung's own system-wide sound options where available; this app does not silently modify those settings.

## Target

- Device: Samsung Galaxy A06 SM-A065M
- OS target: Android 16 (API 36)
- minSdk: 28
- Kotlin / Android Gradle Plugin / Gradle
- Package: io.github.astromg01.a06audioengine

## Status

Initial repository bootstrap. Implementation is developed on `develop/audio-engine-core`; the default branch is kept as the stable integration branch.

## Safety notes

- No root, Shizuku, ADB, hidden system properties, or write-settings permission.
- No automatic volume amplification above unity.
- DSP output includes a peak limiter and conservative profile gains.
- Device-specific effects are detected at runtime; unavailable vendor effects are not assumed to exist.

## Build

The GitHub Actions workflow installs Gradle and Android SDK API 36, runs unit tests, and assembles the debug APK.
