package com.mddevlabs.credentialauth_flow.domain.model

data class SignInResult(
    val isSuccess: Boolean,
    val uid: String? = null,
    val errorMessage: String? = null
)

data class UserData(
    val uid: String,
    val name: String,
    val email: String,
    val phone: String,
    val loginAt: String? = null
)