# Repository working agreement

- Keep the default branch stable. Implement work on a named feature or development branch.
- Before editing, inspect this file, the README, and `docs/SESSION_CHECKPOINT.md`.
- Preserve the no-root and no-privileged-permission design.
- Never claim global processing of other apps. The in-app DSP only processes the A06 Audio Engine playback pipeline.
- Keep EQ gains conservative and retain the peak limiter.
- Add or update unit tests for DSP math and profile invariants.
- Run `gradle testDebugUnitTest assembleDebug lintDebug` before declaring a build ready.
- Update `docs/SESSION_CHECKPOINT.md` with changes, validation evidence, and remaining work.
- Do not merge to `main` without a successful CI run and review.
