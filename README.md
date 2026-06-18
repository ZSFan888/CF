# CFApp

Cloudflare third-party Android client foundation.

## Current scope
- Native connect flow with Cloudflare API token verify.
- Membership bootstrap for default account context.
- Encrypted token storage.
- Local WebView console shell with restricted asset-only loading.
- Pages project list, deployment list, and deployment log viewer.
- Zone list, DNS records, DNS record detail panel, DNS create, DNS edit, and DNS delete.
- Analytics preset query using GraphQL.

## Main files
- `CloudflareRepository.kt`
- `PagesViewModel.kt`
- `DnsViewModel.kt`
- `AnalyticsViewModel.kt`
- `MainActivity.kt`
- `CFBridge.kt`
- `assets/console/index.html`

## Next step
- Add richer Pages deployment detail actions.
- Add analytics chart rendering.
- Add DNS search and filtering.
