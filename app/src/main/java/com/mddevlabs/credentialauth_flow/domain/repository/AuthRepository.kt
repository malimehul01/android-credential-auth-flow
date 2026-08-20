package com.mddevlabs.credentialauth_flow.domain.repository

import android.content.Context
import com.mddevlabs.credentialauth_flow.domain.model.SignInResult
import com.mddevlabs.credentialauth_flow.domain.model.UserData
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUserId: String?
    val currentUserName: String?
    val currentUserEmail: String?

    suspend fun signInWithGoogle(context: Context): SignInResult
    suspend fun saveUserProfile(name: String, email: String, phone: String): Result<Unit>
    suspend fun saveActiveDevice(context: Context): Result<Unit>
    fun getUserProfile(uid: String): Flow<Result<UserData>>
    suspend fun deleteUserAndSignOut(uid: String): Result<Unit>
}