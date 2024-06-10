package com.example.neptune.ttsapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.util.Log;



public class SessionManager {

    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared Preferences
    SharedPreferences pref;

    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;

    // Shared preferences file name
    private static final String PREF_NAME = "AndroidLogin";

    private static final String KEY_IS_USER_ID="userid";


    public SessionManager(Context context) {
        this._context = context;
        pref = _context.getSharedPreferences(PREF_NAME, PRIVATE_MODE);
        editor = pref.edit();
    }


    public void setUserID(String userID) {

        editor.putString(KEY_IS_USER_ID, userID);

        // commit changes
        editor.commit();

        Log.d(TAG, "User login session modified!");
    }

    public String getUserID(){
        return pref.getString(KEY_IS_USER_ID, null);
    }



}

