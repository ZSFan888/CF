package com.zsfan.cfapp

class CloudflareApi {
    companion object {
        const val BASE = "https://api.cloudflare.com/client/v4"
        const val VERIFY = "$BASE/user/tokens/verify"
        const val MEMBERSHIPS = "$BASE/memberships"
    }
}
