package com.mddevlabs.credentialauth_flow.utils
import android.content.Context
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import java.util.Date

object FirebaseHelper {


     private val firestore: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private  fun getUserId(): String?{
        return FirebaseAuth.getInstance().currentUser?.uid
    }


    fun saveUserProfile(name: String,email: String,phone:String,onComplete:()-> Unit,onError:(String)-> Unit){
        val uid=getUserId()?:return

        val mapof=hashMapOf(
            "name" to name,
            "email" to email,
             "phone" to phone
        )
        firestore.collection("users").document(uid).set(mapof, SetOptions.merge())
            .addOnCompleteListener { onComplete() }
            .addOnFailureListener { e->onError(e.localizedMessage?:"profile saved failed") }
    }
    fun saveActiveDevice(context: Context, onComplete:()-> Unit) {
        val uid = getUserId() ?: return
              val mapof=hashMapOf(
            "userEnable" to true,
            "loginAt" to FieldValue.serverTimestamp()

        )
        firestore.collection("users")
               .document(uid)
               .set(mapof, SetOptions.merge())
               .addOnSuccessListener {
                   onComplete()
               }

    }
}

