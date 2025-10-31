package com.example.neptune.ttsapp;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;


public class SessionManager {

    // LogCat tag
    private static String TAG = SessionManager.class.getSimpleName();

    // Shared preferences file name
    private static final String PREF_NAME = "UserSession";
    private static final String KEY_TOKEN = "jwt";
    private static final String ROLES = "roles";
    
    private static final String USER_ID = "userid";
    private static final String KEY_IS_LOGGED_IN = "IS_LOGGED_IN";



    // Shared Preferences
    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
    Context _context;

    // Shared pref mode
    int PRIVATE_MODE = 0;


//    private static final String PREF_NAME = "AndroidLogin";
//
//    private static final String KEY_IS_USER_ID="userid";


    public SessionManager(Context context) {
        this._context = context;
        sharedPreferences = _context.getSharedPreferences(PREF_NAME,Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveSession(String userId, ArrayList<String> join, String jwt){
        editor.putString(USER_ID,userId);
        Set<String> roles = new HashSet<>(join);
        editor.putStringSet(ROLES,roles);
        editor.putString(KEY_TOKEN,jwt);
        editor.putBoolean(KEY_IS_LOGGED_IN,true);
        editor.apply();
    }

    
    public void logout(){
        editor.clear();
        editor.apply();
    }

//    public void setUserID(String userID) {
//
//        editor.putString(KEY_IS_USER_ID, userID);
//
//        // commit changes
//        editor.commit();
//
//        Log.d(TAG, "User login session modified!");
//    }

    //Check if user is logged in
    public boolean isLoggedIn(){
        return sharedPreferences.getBoolean(KEY_IS_LOGGED_IN,false);
    }

    public String getUsername(){
        return sharedPreferences.getString(USER_ID,null);
    }
    
    public String getJWT(){
        return sharedPreferences.getString(KEY_TOKEN,null);
    }

    public Set<String> getRoles(){
        return sharedPreferences.getStringSet(ROLES,null);
    }

//
//    public String getUsername(){
//        return pref.getString(KEY_IS_USER_ID, null);
//    }
}

