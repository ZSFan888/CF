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
    val timeRange: Int = 7
)

class AnalyticsViewModel : ViewModel() {
    private val repo = CloudflareRepository()
    private val _ui = MutableStateFlow(AnalyticsUiState())
    val ui: StateFlow<AnalyticsUiState> = _ui

    fun load(token: String, zoneId: String) {
        if (zoneId.isBlank()) return
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = "")
            runCatching {
                withContext(Dispatchers.IO) { repo.loadHttpRequestsTrend(token, zoneId) }
            }.onSuccess { points ->
                _ui.value = AnalyticsUiState(loading = false, points = points, timeRange = 7)
            }.onFailure { e ->
                _ui.value = AnalyticsUiState(loading = false, error = e.message ?: "Analytics load failed")
            }
        }
    }

    fun setTimeRange(range: Int) {
        _ui.value = _ui.value.copy(timeRange = range)
        if (_ui.value.selectedZoneId.isNotBlank()) {
            load(_ui.value.selectedZoneId, _ui.value.selectedZoneId)
        }
    }
}
