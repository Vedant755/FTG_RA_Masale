package com.ftg.famasale.Utils

import android.content.Context
import com.ftg.famasale.Models.LoggedInUserDetails
import com.ftg.famasale.Utils.Constant.PREF_FILE
import com.ftg.famasale.Utils.Constant.USER_TOKEN
import dagger.hilt.android.qualifiers.ApplicationContext
import okhttp3.internal.userAgent
import javax.inject.Inject

class SharedPrefManager  @Inject constructor(@ApplicationContext context: Context) {

    private var pref = context.getSharedPreferences(PREF_FILE, Context.MODE_PRIVATE)

    fun saveToken(token: String?) {
        val editor = pref.edit()
        editor.putString(USER_TOKEN, token)
        editor.apply()
    }

    fun getToken(): String? {
        return pref.getString(USER_TOKEN, null)
    }

    fun saveUserDetails(details: LoggedInUserDetails?){
        val editor = pref.edit()
        editor.putString("createdAt", details?.createdAt ?: "")
        editor.putString("guard_added_by", details?.guard_added_by ?: "")
        editor.putInt("guard_added_by_id", details?.guard_added_by_id ?: 0)
        editor.putString("guard_address", details?.guard_address ?: "")
        editor.putBoolean("guard_deleted", details?.guard_deleted ?: false)
        editor.putString("guard_email", details?.guard_email ?: "")
        editor.putString("guard_gender", details?.guard_gender ?: "")
        editor.putInt("guard_id", details?.guard_id ?: 0)
        editor.putLong("guard_mobile", details?.guard_mobile ?: 0)
        editor.putString("guard_name", details?.guard_name ?: "")
        editor.putBoolean("guard_status", details?.guard_status ?: true)
        editor.putString("guard_username", details?.guard_username ?: "")
        editor.putString("updatedAt", details?.updatedAt ?: "")
        editor.apply()
    }

    fun getUserDetails(): LoggedInUserDetails{
        return LoggedInUserDetails(
            pref.getString("", null),
            pref.getString("", null),
            pref.getInt("", 0),
            pref.getString("", null),
            pref.getBoolean("", false),
            pref.getString("", null),
            pref.getString("", null),
            pref.getInt("", 0),
            pref.getLong("", 0),
            pref.getString("", null),
            pref.getBoolean("", true),
            pref.getString("", null),
            pref.getString("", null)
        )
    }
}