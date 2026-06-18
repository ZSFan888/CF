# CFApp

Cloudflare third-party Android client foundation.

## Current scope
- Native connect flow with Cloudflare API token verify.
- Membership bootstrap for default account context.
- Encrypted token storage.
- Local WebView console shell with restricted asset-only loading.
- Pages project list, deployment list, and deployment log viewer.
- Zone list, DNS records, DNS record detail panel, DNS create, DNS edit, DNS delete, DNS search, type filter, and sort.
- Analytics preset query using GraphQL with chart rendering and time range selection.

## Main files
- `CloudflareRepository.kt`
- `PagesViewModel.kt`
- `DnsViewModel.kt`
- `AnalyticsViewModel.kt`
- `MainActivity.kt`
- `CFBridge.kt`
- `assets/console/index.html`

## Next step
- Add richer Pages deployment actions.
- Add analytics dimension filtering.
- Add DNS record tags display.
