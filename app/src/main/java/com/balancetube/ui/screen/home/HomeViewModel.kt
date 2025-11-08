package com.balancetube.ui.screen.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balancetube.data.repository.BalanceTubeRepository
import com.balancetube.domain.model.BalanceReport
import com.balancetube.domain.model.Period
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: BalanceTubeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow(Period.LAST_7_DAYS)
    val selectedPeriod: StateFlow<Period> = _selectedPeriod.asStateFlow()

    init {
        loadBalanceReport()
    }

    fun syncWatchHistory() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.syncWatchHistory()
                .onSuccess {
                    loadBalanceReport()
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Failed to sync watch history"
                    )
                }
        }
    }

    fun changePeriod(period: Period) {
        _selectedPeriod.value = period
        loadBalanceReport()
    }

    private fun loadBalanceReport() {
        viewModelScope.launch {
            _uiState.value = HomeUiState.Loading
            repository.getBalanceReport(_selectedPeriod.value)
                .onSuccess { report ->
                    _uiState.value = HomeUiState.Success(report)
                }
                .onFailure { error ->
                    _uiState.value = HomeUiState.Error(
                        error.message ?: "Failed to load balance report"
                    )
                }
        }
    }

    fun retry() {
        loadBalanceReport()
    }
}

sealed class HomeUiState {
    object Loading : HomeUiState()
    data class Success(val report: BalanceReport) : HomeUiState()
    data class Error(val message: String) : HomeUiState()
}
