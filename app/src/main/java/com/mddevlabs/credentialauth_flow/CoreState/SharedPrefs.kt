package com.mddevlabs.credentialauth_flow.CoreState

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

object SharedPrefs{

    private val ProfileChecking="profile_complete"
    private fun Prefs(context: Context): SharedPreferences {
        return context.getSharedPreferences("prefs", Context.MODE_PRIVATE)
    }
    fun setProfileComplete(context: Context,value: Boolean){
        Prefs(context).edit{
        putBoolean(ProfileChecking,value)
        }
    }
    fun getProfileComplete(context: Context): Boolean{
        return Prefs(context).getBoolean(ProfileChecking,false)
    }
}