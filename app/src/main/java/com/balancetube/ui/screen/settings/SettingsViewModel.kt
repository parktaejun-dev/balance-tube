package com.balancetube.ui.screen.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balancetube.data.repository.BalanceTubeRepository
import com.balancetube.util.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: BalanceTubeRepository,
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<SettingsUiState>(SettingsUiState.Idle)
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _userEmail = MutableStateFlow<String?>(null)
    val userEmail: StateFlow<String?> = _userEmail.asStateFlow()

    init {
        loadUserInfo()
    }

    private fun loadUserInfo() {
        val account = authManager.getLastSignedInAccount()
        _userEmail.value = account?.email
    }

    fun deleteAllData() {
        viewModelScope.launch {
            _uiState.value = SettingsUiState.DeletingData
            try {
                repository.deleteAllData()
                _uiState.value = SettingsUiState.DataDeleted
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(
                    e.message ?: "Failed to delete data"
                )
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            try {
                authManager.signOut()
                repository.deleteAllData()
                _uiState.value = SettingsUiState.LoggedOut
            } catch (e: Exception) {
                _uiState.value = SettingsUiState.Error(
                    e.message ?: "Failed to logout"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = SettingsUiState.Idle
    }
}

sealed class SettingsUiState {
    object Idle : SettingsUiState()
    object DeletingData : SettingsUiState()
    object DataDeleted : SettingsUiState()
    object LoggedOut : SettingsUiState()
    data class Error(val message: String) : SettingsUiState()
}
