# Architecture

## Hybrid model

1. **In-app path:** Media3 decodes selected local audio to PCM. `A06DspProcessor` applies a five-band RBJ peaking EQ, a low-ratio envelope compressor, and a sample-peak limiter before audio is sent to the Android output.
2. **System path:** The app opens Android sound settings and reports effects/output devices exposed by `AudioManager` and `AudioEffect.queryEffects()`. The app does not modify hidden Samsung settings and does not inject processing into third-party audio sessions.

## Processing order

PCM input → five peaking biquads (per channel) → optional gentle compressor → safety limiter → PCM output.

The limiter ceiling is 0.94 (-0.54 dBFS), with fast attack and a smooth release. No make-up gain or automatic volume increase is applied.

## Profiles

- Natural
- Voice Clarity
- Balanced
- Bass Control
- Dynamic
- Gaming / Cinema

All profile gains are deliberately modest for a small phone speaker. A low-frequency boost cannot overcome the physical limits of the speaker and can increase distortion.

## Compatibility

- minSdk 28
- compileSdk / targetSdk 36
- Media3 1.11.1
- Android 16 target, with runtime detection of audio outputs and exposed effects.

AudioProcessor chains run on PCM playback. Encoded passthrough/offload paths are not processed; the initial player uses Media3's regular decoded playback pipeline.
