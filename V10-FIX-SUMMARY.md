# CFApp V10 Fix

- Pushed commit: c369b748d834c1084d6dbf31b31ec1ed3c459e48.
- Rewrote MainActivity.escape() to use ch.code and avoid broken Kotlin character literals.
- This fix targets the Build-And-Pack failure around MainActivity.kt lines 256-257.
