package com.example.neptune.ttsapp;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.StrictMode;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class TTSLoginActivity extends AppCompatActivity {

    private EditText userName, password;
    private Button btnLogin, btnRegister;
    private ToggleButton togglePassword;

    private SessionManager sessionManager;
    private ProgressBar progressBarInLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttslogin);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        userName = (EditText) findViewById(R.id.editTextLoginUsername);
        password = (EditText) findViewById(R.id.editTextLoginPassword);
        btnLogin = (Button) findViewById(R.id.buttonSignin);
        btnRegister = (Button) findViewById(R.id.buttonRegister);

        togglePassword = (ToggleButton) findViewById(R.id.login_password_visibility);

        progressBarInLogin=(ProgressBar)findViewById(R.id.progressBarInLogin);
        progressBarInLogin.setVisibility(View.INVISIBLE);


            btnLogin.setOnClickListener(view ->
            {
                btnLogin.setBackgroundColor(Color.GRAY);
                userLogin();
                btnLogin.setBackgroundResource(android.R.drawable.btn_default);
            });

        // Code for goto Registration page
        btnRegister.setOnClickListener(view -> {
        //    btnRegister.setBackgroundColor(Color.LTGRAY);
            Intent i = new Intent(getApplicationContext(), TTSRegistrationActivity.class);
            startActivity(i);
//                sendNotification();
        });


        // Code for show/hide togglePassword Button
        togglePassword.setVisibility(View.INVISIBLE);
        password.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s)
            {
                togglePassword.setVisibility(View.VISIBLE);
                String pass = password.getText().toString().trim().replaceAll("\\s+", "");
                if (pass.length()==0){togglePassword.setVisibility(View.INVISIBLE);}
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {togglePassword.setVisibility(View.INVISIBLE); }
            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });

        // Code for show/hide Password
        togglePassword.setOnClickListener(v -> {
            if(togglePassword.isChecked())
            {
                //Button is ON
                //Show Password
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//                    password.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.icon_password_eye));
                // Code for set focus to right of text in EditText
                int pos = password.getText().length();
                password.setSelection(pos);
            }
            else
            {
                //Button is OFF
                // hide password
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                    password.setBackground(getApplicationContext().getResources().getDrawable(R.drawable.icon_password_cross_eye));
                // Code for set focus to right of text in EditText
                int pos = password.getText().length();
                password.setSelection(pos);
            }
        });


        // code for user fill password then press button of DONE on keyboard they get logged in
        password.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            {
                userLogin();
            }
            return false;
        });


    }


    public void userLogin()
    {

        if (InternetConnectivity.isConnected()) {
            if (isValidUserId().isEmpty()) { userName.setError("User Name Cannot Be Empty"); }
            else if (isValidPassword().length() < 8 || isValidPassword().isEmpty())
            {
                if (isValidPassword().isEmpty()) { password.setError("Password Cannot Be Empty"); }
                if (isValidPassword().length() < 8) { password.setError("Please Enter Valid Password"); }
            }
            else
             {
                progressBarInLogin.setVisibility(View.VISIBLE);
                boolean result = loginUser(isValidUserId(), isValidPassword());
                if (result) {
                    String userID = userName.getText().toString().trim().replaceAll("\\s+", "");
                    sessionManager = new SessionManager(getApplicationContext());
                    sessionManager.setUserID(userID);
                    Intent i = new Intent(TTSLoginActivity.this, TTSMainActivity.class);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(TTSLoginActivity.this, "Incorrect Details", Toast.LENGTH_LONG).show();
                    progressBarInLogin.setVisibility(View.INVISIBLE);
                }
             }
        } else {
            Toast.makeText(TTSLoginActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
            progressBarInLogin.setVisibility(View.INVISIBLE);
        }
    }


    public void sendNotification()
    {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this);
        builder.setSmallIcon(android.R.drawable.ic_dialog_alert);
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.journaldev.com/"));
        Intent intent = new Intent(this, TTSRegistrationActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        builder.setContentIntent(pendingIntent);
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher));
        builder.setContentTitle("Notifications Title");
        builder.setContentText("Your notification content here.");
        builder.setSubText("Tap to view the Task.");

        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Will display the notification in the notification bar
        notificationManager.notify(1, builder.build());
    }


    public void cancelNotification()
    {
        NotificationManager nMgr = (NotificationManager) getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nMgr.cancel(1);
    }



    // Checking UserName valid Or Not
    public String isValidUserId() {
        String uName = userName.getText().toString().trim().replaceAll("\\s+", "");
        if (uName.isEmpty()) { userName.setError("User Name Cannot Be Empty"); }
        return uName;
    }

    // Checking Password valid Or Not
    public String isValidPassword() {

        String passwordck = password.getText().toString().trim().replaceAll("\\s+", "");

        if (passwordck.isEmpty()) { password.setError("Password Cannot Be Empty"); }
        else if (passwordck.length() < 8) { password.setError("Please Enter Valid Password"); }

        return passwordck;
    }

    // Method For Login User
    public boolean loginUser(String userName, String password) {
        boolean result = false;
        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from AUTHENTICATION where USER_ID=? and PASSWORD=?");
            ps.setString(1, userName);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) { result = true; }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return result;
    }



}
