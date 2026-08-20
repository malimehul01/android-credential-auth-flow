package com.mddevlabs.credentialauth_flow.presentation.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mddevlabs.credentialauth_flow.data.repository.AuthRepositoryImpl
import com.mddevlabs.credentialauth_flow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoginSuccess: Boolean = false,
    val isProfileSaved: Boolean = false,
    val errorMessage: String? = null
)

class AuthViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState = _uiState.asStateFlow()

    val currentUserName: String get() = repository.currentUserName ?: "User"
    val currentUserEmail: String get() = repository.currentUserEmail ?: ""
    val currentUserId: String? get() = repository.currentUserId

    fun loginWithGoogle(context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = repository.signInWithGoogle(context)
            if (result.isSuccess) {
                _uiState.update { it.copy(isLoading = false, isLoginSuccess = true) }
            } else {
                _uiState.update { it.copy(isLoading = false, errorMessage = result.errorMessage) }
            }
        }
    }

    fun saveFullProfile(context: Context, phoneNumber: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val profileResult = repository.saveUserProfile(
                name = currentUserName,
                email = currentUserEmail,
                phone = phoneNumber
            )

            if (profileResult.isSuccess) {
                val deviceResult = repository.saveActiveDevice(context)
                if (deviceResult.isSuccess) {
                    _uiState.update { it.copy(isLoading = false, isProfileSaved = true) }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = deviceResult.exceptionOrNull()?.localizedMessage ?: "Failed to save device"
                        )
                    }
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = profileResult.exceptionOrNull()?.localizedMessage ?: "Failed to save profile"
                    )
                }
            }
        }
    }
}