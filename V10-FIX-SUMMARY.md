# CFApp V10 Fix

- Rewrote MainActivity.escape() to use ch.code and avoid broken Kotlin character literals.
- This fix targets the Build-And-Pack failure around MainActivity.kt lines 256-257.
