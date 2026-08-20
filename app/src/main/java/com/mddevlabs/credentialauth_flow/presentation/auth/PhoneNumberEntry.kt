package com.mddevlabs.credentialauth_flow.presentation.auth

import android.app.Activity
import android.content.Context
import android.telephony.TelephonyManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity

fun isSimCardPresent(context: Context): Boolean {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return telephonyManager.simState != TelephonyManager.SIM_STATE_ABSENT &&
            telephonyManager.simState != TelephonyManager.SIM_STATE_UNKNOWN
}

@Composable
fun PhoneNumberEntryScreen(
    onDone: () -> Unit,
    onBlock: () -> Unit,
    viewModel: AuthViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()

    var manualPhoneNumber by remember { mutableStateOf("") }
    var isApiFailed by remember { mutableStateOf(false) }
    var showSettingsRedirectFallback by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf(false) }

        LaunchedEffect(uiState.isProfileSaved) {
            if (uiState.isProfileSaved) {
                onDone()
            }
        }

    LaunchedEffect(uiState.errorMessage) {
        uiState.errorMessage?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
        }
    }

    val phoneNumberHintLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                val phoneNum = Identity.getSignInClient(context).getPhoneNumberFromIntent(result.data)
                if (!phoneNum.isNullOrEmpty()) {
                    val digits = phoneNum.filter { it.isDigit() }
                    val tenDigits = if (digits.length >= 10) digits.substring(digits.length - 10) else digits
                    viewModel.saveFullProfile(context, "+91$tenDigits")
                } else {
                    isApiFailed = true
                    showSettingsRedirectFallback = isSimCardPresent(context)
                    if (!showSettingsRedirectFallback) onBlock()
                }
            } catch (e: Exception) {
                isApiFailed = true
                showSettingsRedirectFallback = isSimCardPresent(context)
                if (!showSettingsRedirectFallback) onBlock()
            }
        } else {
            if (isApiFailed) {
                showSettingsRedirectFallback = isSimCardPresent(context)
                if (!showSettingsRedirectFallback) {
                    Toast.makeText(context, "Security Alert: Valid SIM Card required.", Toast.LENGTH_LONG).show()
                    onBlock()
                }
            }
        }
    }

    val triggerNumberHint = {
        if (!isSimCardPresent(context)) {
            Toast.makeText(context, "No SIM card detected. Please insert a SIM to secure profile.", Toast.LENGTH_LONG).show()
            onBlock()
        } else {
            val request = GetPhoneNumberHintIntentRequest.builder().build()
            Identity.getSignInClient(context)
                .getPhoneNumberHintIntent(request)
                .addOnSuccessListener { result ->
                    try {
                        val intentSenderRequest = IntentSenderRequest.Builder(result.intentSender).build()
                        phoneNumberHintLauncher.launch(intentSenderRequest)
                    } catch (e: Exception) {
                        isApiFailed = true
                        showSettingsRedirectFallback = true
                    }
                }
                .addOnFailureListener {
                    isApiFailed = true
                    showSettingsRedirectFallback = true
                }
        }
    }

    LaunchedEffect(Unit) {
        triggerNumberHint()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Complete Your Profile",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.secondary
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (uiState.isLoading) {
            CircularProgressIndicator(color = Color(0xFFFF3B30))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Saving your profile details...",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp
            )
        } else if (showSettingsRedirectFallback) {
            Text(
                text = "Please enter your 10-digit mobile number manually to continue.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = manualPhoneNumber,
                onValueChange = { input ->
                    if (input.all { it.isDigit() } && input.length <= 10) {
                        manualPhoneNumber = input
                        inputError = false
                    }
                },
                label = { Text("Phone Number") },
                leadingIcon = { Text("+91", modifier = Modifier.padding(start = 8.dp)) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                isError = inputError,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            if (inputError) {
                Text(
                    text = "Please enter a valid 10-digit mobile number",
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val regex = "^[6-9]\\d{9}$".toRegex()
                    val isRepetitiveFake = manualPhoneNumber.isNotEmpty() &&
                            (manualPhoneNumber.all { it == manualPhoneNumber[0] } || manualPhoneNumber == "1234567890")

                    if (manualPhoneNumber.length == 10 && manualPhoneNumber.matches(regex) && !isRepetitiveFake) {
                        viewModel.saveFullProfile(context, "+91$manualPhoneNumber")
                    } else {
                        inputError = true
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Save & Continue", fontWeight = FontWeight.Bold, color = Color.White)
            }
        } else {
            Text(
                text = "Verify your phone number quickly using Google/System hint picker.",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { triggerNumberHint() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(30.dp),
                colors = ButtonDefaults.buttonColors(MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Select Phone Number",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}