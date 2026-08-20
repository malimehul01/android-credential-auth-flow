package com.mddevlabs.credentialauth_flow.data.remote

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.mddevlabs.credentialauth_flow.domain.model.SignInResult
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

class GoogleAuthClient(
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
) {
    suspend fun signIn(context: Context): SignInResult {
        return try {
            val credentialManager = CredentialManager.create(context)

            // Nonce generation
            val rawNonce = UUID.randomUUID().toString()
            val bytes = MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray())
            val hashedNonce = bytes.joinToString("") { "%02x".format(it) }

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId("106081968483-am1b0bhrttho1sgit9v9do58b050pact.apps.googleusercontent.com")
                .setNonce(hashedNonce)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(request = request, context = context)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                val authResult = firebaseAuth.signInWithCredential(firebaseCredential).await()
                val user = authResult.user

                if (user != null) {
                    SignInResult(isSuccess = true, uid = user.uid)
                } else {
                    SignInResult(isSuccess = false, errorMessage = "User not found")
                }
            } else {
                SignInResult(isSuccess = false, errorMessage = "Invalid credential type")
            }
        } catch (e: GetCredentialException) {
            SignInResult(isSuccess = false, errorMessage = e.localizedMessage ?: "Google Sign-In Failed")
        } catch (e: Exception) {
            SignInResult(isSuccess = false, errorMessage = e.localizedMessage ?: "Unexpected error")
        }
    }
}