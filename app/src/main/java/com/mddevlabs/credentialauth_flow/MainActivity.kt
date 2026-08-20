package com.mddevlabs.credentialauth_flow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.google.firebase.auth.FirebaseAuth
import com.mddevlabs.credentialauth_flow.data.local.SessionPreferences
import com.mddevlabs.credentialauth_flow.presentation.auth.LoginScreen
import com.mddevlabs.credentialauth_flow.presentation.auth.PhoneNumberEntryScreen
import com.mddevlabs.credentialauth_flow.presentation.home.HomeScreen
import com.mddevlabs.credentialauth_flow.ui.theme.CredentialAuthFlowTheme

sealed class Screen(val route: String) {
    data object Login : Screen("login")
    data object PhoneEntry : Screen("phone_entry")
    data object Home : Screen("home")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CredentialAuthFlowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    App(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun App(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val navController = rememberNavController()
    val isUserLoggedIn = remember { FirebaseAuth.getInstance().currentUser != null }
    val isProfileCompleted = remember { SessionPreferences.getProfileComplete(context) }

    val startDestination = remember(isUserLoggedIn, isProfileCompleted) {
        when {
            !isUserLoggedIn -> Screen.Login.route
            !isProfileCompleted -> Screen.PhoneEntry.route
            else -> Screen.Home.route
        }
    }

    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }

        composable(Screen.PhoneEntry.route) {
            PhoneNumberEntryScreen(
                onDone = {
                    SessionPreferences.setProfileComplete(context, true)
                    navController.navigate(Screen.Home.route) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onBlock = {
                    SessionPreferences.setProfileComplete(context, false)
                    navController.navigate(Screen.Login.route) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
    }
} 