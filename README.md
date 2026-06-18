# CFApp

Cloudflare third-party Android client foundation.

## Current scope
- Native connect flow with Cloudflare API token verify.
- Membership bootstrap for default account context.
- Encrypted token storage.
- Local WebView console shell with restricted asset-only loading.
- Pages project list, deployment list, and deployment log viewer.
- Zone list, DNS records, DNS record detail panel, DNS create, and DNS edit.
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
- Add DNS delete flow.
- Add richer field validation by record type.
- Replace analytics list with chart rendering.
