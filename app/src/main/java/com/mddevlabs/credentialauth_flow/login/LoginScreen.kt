package com.mddevlabs.credentialauth_flow.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.mddevlabs.credentialauth_flow.CoreState.SharedPrefs
import com.mddevlabs.credentialauth_flow.R
import com.mddevlabs.credentialauth_flow.utils.FirebaseHelper
import com.mddevlabs.shaktitap.ui.login.GoogleLoginScreen


@Composable
fun LoginScreen(
  navController: NavController

){
    val context = LocalContext.current
    var ischecked by remember { mutableStateOf(false) }
    var isAuthLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.systemBars)
    ) {
        if (!isAuthLoading) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center // 🔥 Asli Fix: Poore content ko vertically center fit karega
            ) {

                // 1. APP LOGO
                Image(
                    painter = painterResource(id=R.drawable.ic_launcher_foreground),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(90.dp)
                )

                Spacer(Modifier.height(20.dp))

                // 2. BRAND NAME & SUBTITLE
                Text(
                    text = "App name",
                    color = MaterialTheme.colorScheme.secondary,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 0.5.sp
                )

                Spacer(Modifier.height(8.dp))

                Text(
                    text = stringResource(R.string.login_subtitle),
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f),
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                Spacer(Modifier.height(40.dp))

                // 3. PRIVACY CHECKBOX
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                    Checkbox(
                        checked = ischecked,
                        onCheckedChange = { ischecked = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = MaterialTheme.colorScheme.primary,
                            uncheckedColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                        )
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.privacy_checkbox),
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 🔥 FIX: Checkbox aur Google Button ke beech ka ajeeb gap ekdum tight kiya
                Spacer(Modifier.height(28.dp))

                // 4. GOOGLE LOGIN COMPONENT
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp)
                ) {
                  GoogleLoginScreen(
                      onLoginSuccess = {
                          navController.navigate("phone_entry"){
                              popUpTo(0){inclusive=true}
                          }
                      },
                      onLoginFailure = {
                          navController.navigate("login"){
                              popUpTo(0) {inclusive=true}
                          }
                          },
                      isCheckboxChecked = ischecked
                  )
                }
                Spacer(Modifier.height(80.dp))
            }

            // 5. STICKY BOTTOM LINKS
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 20.dp)
            ) {
                Text(
                    text = "terms",
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                )
                Spacer(Modifier.width(28.dp))
                Text(
                    text = "privacy",
                    color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,

                )
            }
        } else {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_launcher_background),
                        contentDescription = "APP Logo",
                        modifier = Modifier.size(140.dp)
                    )
                    Spacer(Modifier.height(32.dp))
                    CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.secondary,
                        strokeWidth = 3.dp
                    )
                }
            }
        }
    }

}