package com.mddevlabs.credentialauth_flow.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mddevlabs.credentialauth_flow.data.repository.AuthRepositoryImpl
import com.mddevlabs.credentialauth_flow.domain.model.UserData
import com.mddevlabs.credentialauth_flow.domain.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface HomeUiState {
    data object Loading : HomeUiState
    data class Success(val user: UserData) : HomeUiState
    data class Error(val message: String) : HomeUiState
}

class HomeViewModel(
    private val repository: AuthRepository = AuthRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow<HomeUiState>(HomeUiState.Loading)
    val uiState = _uiState.asStateFlow()

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        val uid = repository.currentUserId
        if (uid == null) {
            _uiState.value = HomeUiState.Error("User session not found")
            return
        }

        viewModelScope.launch {
            repository.getUserProfile(uid).collect { result ->
                result.onSuccess { data ->
                    _uiState.value = HomeUiState.Success(data)
                }.onFailure { error ->
                    _uiState.value = HomeUiState.Error(error.localizedMessage ?: "Failed to fetch user profile")
                }
            }
        }
    }

    fun logoutAndDelete(onComplete: () -> Unit) {
        val uid = repository.currentUserId ?: return
        viewModelScope.launch {
            repository.deleteUserAndSignOut(uid)
            onComplete()
        }
    }
}