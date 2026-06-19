# CFApp V10 Fix 5

- Fixed GitHub Actions build command to use `gradle` instead of `./gradlew` because the repository does not contain Gradle wrapper files.
- Added `--stacktrace` to both build and emulator test commands so future CI failures include more useful diagnostics.
- Kept artifact uploads on `always()` for easier post-failure inspection.
