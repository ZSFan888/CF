package com.zsfan.cfapp

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class PagesUiState(
    val loading: Boolean = false,
    val error: String = "",
    val projects: List<PagesProject> = emptyList(),
    val deployments: List<PagesDeployment> = emptyList(),
    val selectedProject: String = "",
    val selectedDeploymentId: String = "",
    val deploymentLogs: String = ""
)

class PagesViewModel : ViewModel() {
    private val repo = CloudflareRepository()
    private val _ui = MutableStateFlow(PagesUiState())
    val ui: StateFlow<PagesUiState> = _ui

    fun load(token: String, accountId: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = "")
            runCatching {
                withContext(Dispatchers.IO) {
                    val projects = repo.loadPagesProjects(token, accountId)
                    val first = projects.firstOrNull()
                    val deployments = if (first?.name.isNullOrBlank()) emptyList() else repo.loadPagesDeployments(token, accountId, first!!.name!!)
                    Triple(projects, deployments, first?.name.orEmpty())
                }
            }.onSuccess { (projects, deployments, selected) ->
                _ui.value = PagesUiState(
                    loading = false,
                    projects = projects,
                    deployments = deployments,
                    selectedProject = selected,
                    selectedDeploymentId = deployments.firstOrNull()?.id.orEmpty()
                )
            }.onFailure { e ->
                _ui.value = PagesUiState(loading = false, error = e.message ?: "Pages load failed")
            }
        }
    }

    fun selectProject(token: String, accountId: String, projectName: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = "", selectedProject = projectName, deploymentLogs = "")
            runCatching {
                withContext(Dispatchers.IO) { repo.loadPagesDeployments(token, accountId, projectName) }
            }.onSuccess { deployments ->
                _ui.value = _ui.value.copy(loading = false, deployments = deployments, selectedDeploymentId = deployments.firstOrNull()?.id.orEmpty())
            }.onFailure { e ->
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Deployments load failed")
            }
        }
    }

    fun loadLogs(token: String, accountId: String, projectName: String, deploymentId: String) {
        viewModelScope.launch {
            _ui.value = _ui.value.copy(loading = true, error = "", selectedDeploymentId = deploymentId)
            runCatching {
                withContext(Dispatchers.IO) { repo.loadDeploymentLogs(token, accountId, projectName, deploymentId) }
            }.onSuccess { logs ->
                _ui.value = _ui.value.copy(loading = false, deploymentLogs = logs)
            }.onFailure { e ->
                _ui.value = _ui.value.copy(loading = false, error = e.message ?: "Logs load failed")
            }
        }
    }
}
