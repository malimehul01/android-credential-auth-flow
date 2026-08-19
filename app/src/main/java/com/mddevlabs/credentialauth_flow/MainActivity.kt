package com.mddevlabs.credentialauth_flow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
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
import com.mddevlabs.credentialauth_flow.CoreState.SharedPrefs
import com.mddevlabs.credentialauth_flow.login.LoginScreen
import com.mddevlabs.credentialauth_flow.login.PhoneNumberEntryScreen
import com.mddevlabs.credentialauth_flow.ui.theme.CredentialAuthFlowTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
            enableEdgeToEdge()
            setContent {
            CredentialAuthFlowTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)){
                        App()
                    }
                }
            }
        }
    }
}

@Composable
fun App() {
    val context= LocalContext.current
    val navController= rememberNavController()
    val user= FirebaseAuth.getInstance().currentUser
    val startScreen= remember(user){  when{
         user==null -> "login"
         !SharedPrefs.getProfileComplete(context) -> "phone_entry"
         else -> "home"
    }
        }
    NavHost(
      navController = navController,
        startDestination = startScreen
    ){
        composable("login"){
            LoginScreen(
                navController
            )
        }
        composable("phone_entry"){
            PhoneNumberEntryScreen(
                onDone = {
                    navController.navigate("home"){
                        popUpTo(0) {inclusive=true}
                    }
                    SharedPrefs.setProfileComplete(context,true)
                  },
                onBlock = {
                    navController.navigate("login"){
                    popUpTo(0) {inclusive=true} }
                    },

            )
        }
        composable("home"){
            HomeScreen(navController)
        }


    }


}
