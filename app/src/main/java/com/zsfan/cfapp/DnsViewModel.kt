package com.zsfan.cfapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class DnsUiState(
    val loading: Boolean = false,
    val error: String = "",
    val zones: List<Zone> = emptyList(),
    val records: List<DnsRecord> = emptyList(),
    val selectedZoneId: String = "",
    val selectedRecordId: String = ""
) {
    val selectedRecord: DnsRecord?
        get() = records.firstOrNull { it.id == selectedRecordId }
}

class DnsViewModel : ViewModel() {
    private val repo = CloudflareRepository()
    private val _ui = MutableStateFlow(DnsUiState())
    val ui: StateFlow<DnsUiState> = _ui

    fun load(token: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = "")
            runCatching {
                withContext(Dispatchers.IO) {
                    val zones = repo.loadZones(token)
                    val first = zones.firstOrNull()
                    val records = if (first?.id.isNullOrBlank()) emptyList() else repo.loadDnsRecords(token, first!!.id!!)
                    Triple(zones, records, first?.id.orEmpty())
                }
            }.onSuccess { (zones, records, selectedId) ->
                _ui.value = DnsUiState(
                    loading = false,
                    zones = zones,
                    records = records,
                    selectedZoneId = selectedId,
                    selectedRecordId = records.firstOrNull()?.id.orEmpty()
                )
            }.onFailure { e ->
                _ui.value = DnsUiState(loading = false, error = e.message ?: "DNS load failed")
            }
        }
    }

    fun selectZone(token: String, zoneId: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = "", selectedZoneId = zoneId)
            runCatching {
                withContext(Dispatchers.IO) { repo.loadDnsRecords(token, zoneId) }
            }.onSuccess { records ->
                _ui.value = _ui.value.copy(loading = false, records = records, selectedRecordId = records.firstOrNull()?.id.orEmpty())
            }.onFailure { e ->
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "DNS records load failed")
            }
        }
    }

    fun selectRecord(recordId: String) {
        _ui.value = _ui.value.copy(selectedRecordId = recordId)
    }
}
