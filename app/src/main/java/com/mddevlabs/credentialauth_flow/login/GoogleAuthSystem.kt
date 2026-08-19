package com.mddevlabs.shaktitap.ui.login

import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.CustomCredential
import androidx.credentials.exceptions.GetCredentialException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch
import java.security.MessageDigest
import java.util.UUID
import com.mddevlabs.credentialauth_flow.R

@Composable
fun GoogleLoginScreen(
    onLoginSuccess: (uid: String) -> Unit,
    onLoginFailure: (String) -> Unit,
    isCheckboxChecked: Boolean
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val firebaseAuth = remember { FirebaseAuth.getInstance() }
    var isLoading by remember { mutableStateOf(false) }

    val credentialManager = remember { CredentialManager.create(context) }

    fun performGoogleLogin() {
        if (isLoading) return
        isLoading = true

        coroutineScope.launch {
            try {
                // Nonce generation for security
                val rawNonce = UUID.randomUUID().toString()
                val bytes = MessageDigest.getInstance("SHA-256").digest(rawNonce.toByteArray())
                val hashedNonce = bytes.joinToString("") { "%02x".format(it) }

                // Configure Google ID Option (Replace with your actual Web Client ID)
                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId("106081968483-am1b0bhrttho1sgit9v9do58b050pact.apps.googleusercontent.com")
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                // Request Credentials from Google Play Services
                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken

                    // Convert ID Token to Firebase Credential
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)

                    firebaseAuth.signInWithCredential(firebaseCredential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val user = firebaseAuth.currentUser
                                if (user != null) {
                                    onLoginSuccess(user.uid)
                                }
                            } else {
                                val error = authTask.exception?.localizedMessage ?: "Firebase Auth Failed"
                                onLoginFailure(error)
                                isLoading = false
                            }
                        }
                } else {
                    onLoginFailure("Invalid credential type received.")
                    isLoading = false
                }
            } catch (e: GetCredentialException) {
                onLoginFailure("Google Sign-In Failed: ${e.localizedMessage}")
                isLoading = false
            } catch (e: Exception) {
                onLoginFailure("An unexpected error occurred: ${e.localizedMessage}")
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(
            onClick = { performGoogleLogin() },
            enabled = !isLoading && isCheckboxChecked,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(26.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.onPrimary,
            ),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.5.dp,
                    color = MaterialTheme.colorScheme.primary
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_launcher_foreground),
                        contentDescription = "Google Icon",
                        modifier = Modifier.size(35.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Continue with Google",
                        color = Color(0xFF9AA0A6),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}