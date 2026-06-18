package com.zsfan.cfapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AnalyticsUiState(
    val loading: Boolean = false,
    val error: String = "",
    val points: List<AnalyticsPoint> = emptyList(),
    val timeRange: Int = 7,
    val selectedZoneId: String = ""
)

class AnalyticsViewModel : ViewModel() {
    private val repo = CloudflareRepository()
    private val _ui = MutableStateFlow(AnalyticsUiState())
    val ui: StateFlow<AnalyticsUiState> = _ui

    private var lastToken: String = ""
    private var lastZoneId: String = ""

    fun load(token: String, zoneId: String) {
        if (zoneId.isBlank()) return
        lastToken = token
        lastZoneId = zoneId
        val range = _ui.value.timeRange
        _ui.value = _ui.value.copy(selectedZoneId = zoneId)
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = "")
            runCatching {
                withContext(Dispatchers.IO) { repo.loadHttpRequestsTrend(token, zoneId) }
            }.onSuccess { points ->
                _ui.value = AnalyticsUiState(loading = false, points = points, timeRange = range, selectedZoneId = zoneId)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Analytics load failed")
            }
        }
    }

    fun setTimeRange(range: Int) {
        _ui.value = _ui.value.copy(timeRange = range)
        if (lastToken.isNotBlank() && lastZoneId.isNotBlank()) {
            load(lastToken, lastZoneId)
        }
    }
}
