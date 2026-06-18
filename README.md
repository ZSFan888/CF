# CFApp

Cloudflare third-party Android client foundation.

## Current scope
- Native connect flow with Cloudflare API token verify.
- Membership bootstrap for default account context.
- Encrypted token storage.
- Local WebView console shell with restricted asset-only loading.
- Pages project list, deployment list, and deployment log viewer.
- Zone list, DNS records, and DNS record detail panel.
- Analytics preset query using GraphQL.

## Main files
- `ConnectActivity.kt`
- `AuthViewModel.kt`
- `CloudflareRepository.kt`
- `PagesViewModel.kt`
- `DnsViewModel.kt`
- `AnalyticsViewModel.kt`
- `MainActivity.kt`
- `assets/console/index.html`

## Next step
- Add DNS create/edit flows.
- Add Pages deployment detail actions.
- Replace analytics list with chart rendering.
