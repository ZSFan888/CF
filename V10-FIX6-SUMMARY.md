# CFApp V10 Fix 6

- Pushed commit: fb1bfde180e886d5072c0a4a0e32eb9c9b31a0ad
- Fixed Android emulator smoke test by adding explicit gradle environment checks and explicit `installDebug installDebugAndroidTest` before running connected tests.
- Added `gradle --version` and `command -v gradle` checks in emulator script to confirm gradle is available in emulator-runner environment.
- Added explicit `installDebug installDebugAndroidTest` to ensure test APK is installed before `connectedDebugAndroidTest`.
- This should reveal whether the root cause is "gradle not available in emulator script" or "test installation/run failure".
