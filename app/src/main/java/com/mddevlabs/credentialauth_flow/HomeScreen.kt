package com.mddevlabs.credentialauth_flow

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun HomeScreen(
navController: NavController

){

 val uid = FirebaseAuth.getInstance().currentUser?.uid

 var name by remember { mutableStateOf("Loading...") }
    var phone by remember { mutableStateOf("Loading...") }
    var email by remember { mutableStateOf("Loading...") }
    var loginAt by remember { mutableStateOf("Loading...") }


    LaunchedEffect(uid) {

        if(uid==null)return@LaunchedEffect
        val db= FirebaseFirestore.getInstance()
        db.collection("users").document(uid)
            .get()
            .addOnSuccessListener { documet->
                if(!documet.exists()&&documet==null) return@addOnSuccessListener
                name=documet.getString("name")?:"N/A"
                phone=documet.getString("phone")?:"N/A"
                email =documet.getString("email")?:"N/A"
                loginAt = documet.getTimestamp("loginAt")?.toDate()?.toLocaleString() ?: "N/A"

            }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    UserDetailItem(label = "Name", value = name)
                    Divider()
                    UserDetailItem(label = "Email", value = email)
                    Divider()
                    UserDetailItem(label = "Phone", value = phone)
                    Divider()
                    UserDetailItem(label = "Logged In At", value = loginAt.toString())
                }
            }
            Spacer(modifier = Modifier.width(10.dp))
            TextButton(
                onClick = {
                    Firebase.firestore.collection("users").document(uid.toString()).delete().addOnSuccessListener {

                        FirebaseAuth.getInstance().signOut()
                        navController.navigate("login"){
                            popUpTo(0){inclusive=true }
                        }
                    }
                }
            ) {
                Text("Logout && Delete All data")
            }
        }


    }

}

@Composable
fun UserDetailItem(label: String, value: String) {
    Column {
        Text(
            text = label,
            fontSize = 12.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}