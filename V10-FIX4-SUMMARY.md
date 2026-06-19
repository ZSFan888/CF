# CFApp V10 Fix 4

- Pushed commit: 293014d8cd4f416ec75ba59eebbee2db08fe522f
- Added Android emulator smoke testing to GitHub Actions using ReactiveCircus/android-emulator-runner.
- Workflow now builds both debug APK and androidTest APK, runs connectedDebugAndroidTest, and uploads adb logcat plus activity/package dumps as artifacts.
- Added instrumentation smoke tests that launch ConnectActivity and MainActivity to catch startup crashes in CI.
- This enables automatic detection and log capture, but not safe fully automatic code repair.
