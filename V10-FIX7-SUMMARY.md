# CFApp V10 Fix 7

- Changed workflow to upload APK artifacts immediately after build, BEFORE running emulator smoke test.
- Added two user-download artifacts:
  - `app-debug-for-user`: main debug APK
  - `app-debug-androidTest-for-user`: androidTest APK
- Users can now download and install the APK even if the emulator test later fails.
- Emulator smoke test still runs after upload, with same gradle checks and explicit install steps.
