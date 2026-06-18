package com.zsfan.cfapp

import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class CloudflareRepository {
    private val client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build()
    private val moshi = Moshi.Builder().build()
    private val verifyAdapter = moshi.adapter(VerifyEnvelope::class.java)
    private val membershipsAdapter = moshi.adapter(MembershipEnvelope::class.java)
    private val projectsAdapter = moshi.adapter(PagesProjectsEnvelope::class.java)
    private val deploymentsAdapter = moshi.adapter(PagesDeploymentsEnvelope::class.java)
    private val zonesAdapter = moshi.adapter(ZonesEnvelope::class.java)
    private val dnsAdapter = moshi.adapter(DnsRecordsEnvelope::class.java)

    private fun get(url: String, token: String) = Request.Builder().url(url).header("Authorization", "Bearer $token").get().build()
    private fun jsonRequest(url: String, token: String, method: String, payload: JSONObject): Request {
        val body = payload.toString().toRequestBody("application/json".toMediaType())
        return Request.Builder().url(url).header("Authorization", "Bearer $token").method(method, body).build()
    }

    fun verifyToken(token: String): VerifyResult {
        client.newCall(get(CloudflareApi.VERIFY, token)).execute().use { response ->
            if (!response.isSuccessful) error("Verify failed: HTTP ${response.code}")
            val envelope = verifyAdapter.fromJson(response.body?.string().orEmpty()) ?: error("Invalid verify response")
            if (!envelope.success || envelope.result == null) error("Token verify failed")
            return envelope.result
        }
    }

    fun loadMemberships(token: String): List<Membership> {
        client.newCall(get(CloudflareApi.MEMBERSHIPS, token)).execute().use { response ->
            if (!response.isSuccessful) error("Memberships failed: HTTP ${response.code}")
            val envelope = membershipsAdapter.fromJson(response.body?.string().orEmpty()) ?: error("Invalid memberships response")
            if (!envelope.success) error("Memberships request failed")
            return envelope.result.orEmpty()
        }
    }

    fun loadPagesProjects(token: String, accountId: String): List<PagesProject> {
        val url = "${CloudflareApi.BASE}/accounts/$accountId/pages/projects"
        client.newCall(get(url, token)).execute().use { response ->
            if (!response.isSuccessful) error("Projects failed: HTTP ${response.code}")
            val envelope = projectsAdapter.fromJson(response.body?.string().orEmpty()) ?: error("Invalid projects response")
            if (!envelope.success) error("Projects request failed")
            return envelope.result.orEmpty()
        }
    }

    fun loadPagesDeployments(token: String, accountId: String, projectName: String): List<PagesDeployment> {
        val url = "${CloudflareApi.BASE}/accounts/$accountId/pages/projects/$projectName/deployments"
        client.newCall(get(url, token)).execute().use { response ->
            if (!response.isSuccessful) error("Deployments failed: HTTP ${response.code}")
            val envelope = deploymentsAdapter.fromJson(response.body?.string().orEmpty()) ?: error("Invalid deployments response")
            if (!envelope.success) error("Deployments request failed")
            return envelope.result.orEmpty()
        }
    }

    fun loadDeploymentLogs(token: String, accountId: String, projectName: String, deploymentId: String): String {
        val url = "${CloudflareApi.BASE}/accounts/$accountId/pages/projects/$projectName/deployments/$deploymentId/history/logs"
        client.newCall(get(url, token)).execute().use { response ->
            if (!response.isSuccessful) error("Logs failed: HTTP ${response.code}")
            return response.body?.string().orEmpty().ifBlank { "No logs returned." }
        }
    }

    fun loadZones(token: String): List<Zone> {
        client.newCall(get("${CloudflareApi.BASE}/zones", token)).execute().use { response ->
            if (!response.isSuccessful) error("Zones failed: HTTP ${response.code}")
            val envelope = zonesAdapter.fromJson(response.body?.string().orEmpty()) ?: error("Invalid zones response")
            if (!envelope.success) error("Zones request failed")
            return envelope.result.orEmpty()
        }
    }

    fun loadDnsRecords(token: String, zoneId: String): List<DnsRecord> {
        client.newCall(get("${CloudflareApi.BASE}/zones/$zoneId/dns_records", token)).execute().use { response ->
            if (!response.isSuccessful) error("DNS failed: HTTP ${response.code}")
            val envelope = dnsAdapter.fromJson(response.body?.string().orEmpty()) ?: error("Invalid DNS response")
            if (!envelope.success) error("DNS request failed")
            return envelope.result.orEmpty()
        }
    }

    fun createDnsRecord(token: String, zoneId: String, input: DnsRecordInput) {
        val payload = JSONObject().put("type", input.type).put("name", input.name).put("content", input.content).put("ttl", input.ttl)
        if (input.proxied != null) payload.put("proxied", input.proxied)
        val request = jsonRequest("${CloudflareApi.BASE}/zones/$zoneId/dns_records", token, "POST", payload)
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Create DNS failed: HTTP ${response.code}")
        }
    }

    fun updateDnsRecord(token: String, zoneId: String, recordId: String, input: DnsRecordInput) {
        val payload = JSONObject().put("type", input.type).put("name", input.name).put("content", input.content).put("ttl", input.ttl)
        if (input.proxied != null) payload.put("proxied", input.proxied)
        val request = jsonRequest("${CloudflareApi.BASE}/zones/$zoneId/dns_records/$recordId", token, "PATCH", payload)
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Update DNS failed: HTTP ${response.code}")
        }
    }

    fun loadHttpRequestsTrend(token: String, zoneId: String): List<AnalyticsPoint> {
        val query = "query(${'$'}zoneTag: String!) { viewer { zones(filter: { zoneTag: ${'$'}zoneTag }) { httpRequests1dGroups(limit: 7, orderBy: [date_ASC]) { dimensions { date } sum { requests bytes cachedRequests threats } } } } }"
        val payload = JSONObject().put("query", query).put("variables", JSONObject().put("zoneTag", zoneId))
        val request = Request.Builder().url(CloudflareApi.GRAPHQL).header("Authorization", "Bearer $token").post(payload.toString().toRequestBody("application/json".toMediaType())).build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) error("Analytics failed: HTTP ${response.code}")
            val body = JSONObject(response.body?.string().orEmpty())
            if (body.has("errors")) error(body.getJSONArray("errors").optJSONObject(0)?.optString("message") ?: "Analytics query failed")
            val groups = body.optJSONObject("data")?.optJSONObject("viewer")?.optJSONArray("zones")?.optJSONObject(0)?.optJSONArray("httpRequests1dGroups") ?: JSONArray()
            return buildList {
                for (i in 0 until groups.length()) {
                    val item = groups.optJSONObject(i) ?: continue
                    val dimensions = item.optJSONObject("dimensions")
                    val sum = item.optJSONObject("sum")
                    add(AnalyticsPoint(dimensions?.optString("date").orEmpty(), sum?.optLong("requests") ?: 0L, sum?.optLong("bytes") ?: 0L, sum?.optLong("cachedRequests") ?: 0L, sum?.optLong("threats") ?: 0L))
                }
            }
        }
    }
}
