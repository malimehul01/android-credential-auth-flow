package com.mddevlabs.credentialauth_flow.data.repository

import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.mddevlabs.credentialauth_flow.data.remote.GoogleAuthClient
import com.mddevlabs.credentialauth_flow.domain.model.SignInResult
import com.mddevlabs.credentialauth_flow.domain.model.UserData
import com.mddevlabs.credentialauth_flow.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val googleAuthClient: GoogleAuthClient = GoogleAuthClient(),
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override val currentUserName: String?
        get() = firebaseAuth.currentUser?.displayName ?: "User"

    override val currentUserEmail: String?
        get() = firebaseAuth.currentUser?.email ?: ""

    override suspend fun signInWithGoogle(context: Context): SignInResult =
        googleAuthClient.signIn(context)

    override suspend fun saveUserProfile(name: String, email: String, phone: String): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("User not authenticated"))
        return try {
            val map = hashMapOf(
                "name" to name,
                "email" to email,
                "phone" to phone
            )
            firestore.collection("users").document(uid).set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun saveActiveDevice(context: Context): Result<Unit> {
        val uid = currentUserId ?: return Result.failure(Exception("User not authenticated"))
        return try {
            val map = hashMapOf(
                "userEnable" to true,
                "loginAt" to FieldValue.serverTimestamp()
            )
            firestore.collection("users").document(uid).set(map, SetOptions.merge()).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun getUserProfile(uid: String): Flow<Result<UserData>> = callbackFlow {
        val listener = firestore.collection("users").document(uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Result.failure(error))
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val userData = UserData(
                        uid = uid,
                        name = snapshot.getString("name") ?: "N/A",
                        email = snapshot.getString("email") ?: "N/A",
                        phone = snapshot.getString("phone") ?: "N/A",
                        loginAt = snapshot.getTimestamp("loginAt")?.toDate()?.toLocaleString() ?: "N/A"
                    )
                    trySend(Result.success(userData))
                }
            }
        awaitClose { listener.remove() }
    }

    override suspend fun deleteUserAndSignOut(uid: String): Result<Unit> {
        return try {
            firestore.collection("users").document(uid).delete().await()
            firebaseAuth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}