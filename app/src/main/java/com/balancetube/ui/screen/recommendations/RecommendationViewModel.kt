package com.balancetube.ui.screen.recommendations

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balancetube.data.repository.BalanceTubeRepository
import com.balancetube.domain.model.Category
import com.balancetube.domain.model.Video
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecommendationViewModel @Inject constructor(
    private val repository: BalanceTubeRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<RecommendationUiState>(RecommendationUiState.Loading)
    val uiState: StateFlow<RecommendationUiState> = _uiState.asStateFlow()

    private val _category = MutableStateFlow<Category?>(null)
    val category: StateFlow<Category?> = _category.asStateFlow()

    fun loadRecommendations(category: Category) {
        _category.value = category
        viewModelScope.launch {
            _uiState.value = RecommendationUiState.Loading
            repository.getRecommendations(category)
                .onSuccess { videos ->
                    _uiState.value = if (videos.isEmpty()) {
                        RecommendationUiState.Empty
                    } else {
                        RecommendationUiState.Success(videos)
                    }
                }
                .onFailure { error ->
                    _uiState.value = RecommendationUiState.Error(
                        error.message ?: "Failed to load recommendations"
                    )
                }
        }
    }

    fun retry() {
        _category.value?.let { loadRecommendations(it) }
    }
}

sealed class RecommendationUiState {
    object Loading : RecommendationUiState()
    data class Success(val videos: List<Video>) : RecommendationUiState()
    object Empty : RecommendationUiState()
    data class Error(val message: String) : RecommendationUiState()
}
