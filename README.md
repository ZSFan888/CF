# CFApp

Cloudflare third-party Android client foundation.

## Structure
- `app/src/main/assets/console/index.html` - WebView console shell.
- `MainActivity.kt` - WebView host.
- `CFBridge.kt` - JS bridge for native features.
- `TokenStore.kt` - encrypted token storage.
- `ThemeStore.kt` - theme persistence.

## Next step
- Connect token verify.
- Load memberships and account context.
- Add DNS and Pages modules.
