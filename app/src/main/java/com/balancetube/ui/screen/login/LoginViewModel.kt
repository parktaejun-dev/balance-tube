package com.balancetube.ui.screen.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.balancetube.util.AuthManager
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authManager: AuthManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<LoginUiState>(LoginUiState.Idle)
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    init {
        checkExistingLogin()
    }

    private fun checkExistingLogin() {
        if (authManager.isSignedIn()) {
            _uiState.value = LoginUiState.Success
        }
    }

    fun handleSignInResult(account: GoogleSignInAccount?) {
        viewModelScope.launch {
            if (account != null) {
                account.email?.let { email ->
                    authManager.saveUserEmail(email)
                }
                _uiState.value = LoginUiState.Success
            } else {
                _uiState.value = LoginUiState.Error("Sign in failed")
            }
        }
    }

    fun handleSignInError(errorMessage: String) {
        _uiState.value = LoginUiState.Error(errorMessage)
    }

    fun resetState() {
        _uiState.value = LoginUiState.Idle
    }
}

sealed class LoginUiState {
    object Idle : LoginUiState()
    object Loading : LoginUiState()
    object Success : LoginUiState()
    data class Error(val message: String) : LoginUiState()
}
