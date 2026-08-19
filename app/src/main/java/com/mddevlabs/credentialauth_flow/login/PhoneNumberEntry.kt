package com.mddevlabs.credentialauth_flow.login

import android.app.Activity
import android.content.Context
import android.telephony.TelephonyManager
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.firebase.auth.FirebaseAuth
import com.mddevlabs.credentialauth_flow.utils.FirebaseHelper

fun isSimCardPresent(context: Context): Boolean {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    return telephonyManager.simState != TelephonyManager.SIM_STATE_ABSENT &&
            telephonyManager.simState != TelephonyManager.SIM_STATE_UNKNOWN
}

@Composable
fun PhoneNumberEntryScreen(
    onDone: () -> Unit,
    onBlock: () -> Unit
) {
    val context = LocalContext.current
    val uid = FirebaseAuth.getInstance().currentUser?.uid
    val googleName = FirebaseAuth.getInstance().currentUser?.displayName ?: "User"
    val email = FirebaseAuth.getInstance().currentUser?.email ?: ""

    var rawPhoneNumber by remember { mutableStateOf("") }
    var manualPhoneNumber by remember { mutableStateOf("") }
    var isSavingProfile by remember { mutableStateOf(false) }
    var isSaveExecuted by remember { mutableStateOf(false) }

    // Core Navigation & UX States
    var isApiFailed by remember { mutableStateOf(false) }
    var showSettingsRedirectFallback by remember { mutableStateOf(false) }
    var inputError by remember { mutableStateOf(false) }


    val phoneNumberHintLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ){ result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            try {
                val phoneNum = Identity.getSignInClient(context).getPhoneNumberFromIntent(result.data)
                if (!phoneNum.isNullOrEmpty()) {
                    rawPhoneNumber = phoneNum
                    showSettingsRedirectFallback = false
                    isApiFailed = false
                } else {
                    // Dialog open hua par number blank mila (Operator Configuration Error)
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
            if (!isApiFailed) {
                showSettingsRedirectFallback = false
            } else {
                // Agar API hardware par fail chal rahi hai pehle se
                showSettingsRedirectFallback = isSimCardPresent(context)
                if (!showSettingsRedirectFallback) {
                    Toast.makeText(context, "Security Alert: Valid SIM Card required.", Toast.LENGTH_LONG).show()
                    onBlock()
                }
            }
        }
    }

    // Trigger Logic function
    val triggerNumberHint = {
        // Strict Hardware Shield: No SIM = Immediate Block
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
                .addOnFailureListener { exception ->
                    Log.e("PhoneHint", "Google API Failure: ${exception.message}")
                    // Genuine SIM present hai par internal configuration missing hai
                    isApiFailed = true
                    showSettingsRedirectFallback = true
                }
        }
    }

    // Auto-trigger on Screen Setup
    LaunchedEffect(Unit) {
        triggerNumberHint()
    }

    // Database Sync Lock Execution
    LaunchedEffect(rawPhoneNumber, uid) {
        if (rawPhoneNumber.isNotEmpty() && uid != null && !isSaveExecuted) {
            isSaveExecuted = true
            isSavingProfile = true

            val onlyDigits = rawPhoneNumber.filter { it.isDigit() }
            val pureTenDigits = if (onlyDigits.length >= 10) {
                onlyDigits.substring(onlyDigits.length - 10)
            } else {
                onlyDigits
            }
            val finalValidatedNumber = "+91$pureTenDigits"

            FirebaseHelper.saveUserProfile(
                name = googleName,
                phone = finalValidatedNumber,
                email = email,
                onComplete = {
                    FirebaseHelper.saveActiveDevice(context, onComplete = {
                        isSavingProfile = false

                        onDone()
                    },)
                },
                onError = { error ->
                    isSavingProfile = false
                    isSaveExecuted = false
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                }
            )
        }
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

        if (isSavingProfile) {
            CircularProgressIndicator(color = Color(0xFFFF3B30))
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Saving your profile details...",
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

            // Pure Manual Variable Binding without any side-effects
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
                    text = if (manualPhoneNumber.length != 10) {
                        "Please enter a valid 10-digit mobile number"
                    } else {
                        "Invalid phone number or repetitive pattern"
                    },
                    color = Color.Red,
                    fontSize = 12.sp,
                    modifier = Modifier.align(Alignment.Start).padding(start = 4.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    val indianMobileRegex = "^[6-9]\\d{9}$".toRegex()
                    val isRepetitiveFake = manualPhoneNumber.isNotEmpty() &&
                            (manualPhoneNumber.all { it == manualPhoneNumber[0] } || manualPhoneNumber == "1234567890")

                    // Pure manual verification validation execution
                    if (manualPhoneNumber.length == 10 && manualPhoneNumber.matches(indianMobileRegex) && !isRepetitiveFake && !isSaveExecuted) {
                        isSaveExecuted = true
                        isSavingProfile = true
                        inputError = false
                        val finalValidatedManualNumber = "+91$manualPhoneNumber"
                        FirebaseHelper.saveUserProfile(
                            name = googleName,
                            phone =finalValidatedManualNumber,
                            email = email,
                            onComplete = {
                                FirebaseHelper.saveActiveDevice(context, onComplete = {
                                    isSavingProfile = false
                                    onDone()
                                  },)
                            },
                            onError = { error ->
                                isSavingProfile = false
                                isSaveExecuted = false
                                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                            }
                        )
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
            // Default View state
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
                        text ="Select Phone Number",
                        color = MaterialTheme.colorScheme.onPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}