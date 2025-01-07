package com.example.neptune.ttsapp;

import static android.app.ProgressDialog.show;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.StrictMode;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.os.Bundle;

import android.telecom.CallScreeningService;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.UserServiceInterface;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@AndroidEntryPoint
public class TTSLoginActivity extends AppCompatActivity {

    @Inject
    UserServiceInterface userServiceInterface;

    @Inject
    AppExecutors appExecutors;

    private EditText userName, password;
    private Button btnLogin, btnRegister;
    private ToggleButton togglePassword;

    private SessionManager sessionManager;
    private ProgressBar progressBarInLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttslogin);
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);


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
                if (pass.isEmpty()){togglePassword.setVisibility(View.INVISIBLE);}
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
//                progressBarInLogin.setVisibility(View.VISIBLE);
//                boolean result = loginUser(isValidUserId(), isValidPassword());
//                if (result) {
//                    String userID = userName.getText().toString().trim().replaceAll("\\s+", "");
//                    sessionManager = new SessionManager(getApplicationContext());
//                    sessionManager.setUserID(userID);
//                    Intent i = new Intent(TTSLoginActivity.this, TTSMainActivity.class);
//                    startActivity(i);
//                    finish();
//                } else {
//                    Toast.makeText(TTSLoginActivity.this, "Incorrect Details", Toast.LENGTH_LONG).show();
//                    progressBarInLogin.setVisibility(View.INVISIBLE);
//                }
                 appExecutors.getNetworkIO().execute(() -> makeUserLogin(isValidUserId(),isValidPassword()).thenAccept(isCredentialsValid -> {
                     if(isCredentialsValid){
                        appExecutors.getMainThread().execute(()-> {
                            String userId = userName.getText().toString().trim().replaceAll("\\s+","");
                            sessionManager = new SessionManager(getApplicationContext());
                            sessionManager.setUserID(userId);
                            Toast.makeText(TTSLoginActivity.this, "You're logged in now", Toast.LENGTH_SHORT).show();
                            Intent i = new Intent(TTSLoginActivity.this, TTSMainActivity.class);
                            startActivity(i);
                            finish();
                        });
                     }else {
                        appExecutors.getMainThread().execute(() -> {
                            progressBarInLogin.setVisibility(View.INVISIBLE);
                         Toast.makeText(TTSLoginActivity.this, "You entered incorrect details ", Toast.LENGTH_SHORT).show();
                        });
                     }
                 }).exceptionally(e -> {
                    appExecutors.getMainThread().execute(() -> {
                        progressBarInLogin.setVisibility(View.INVISIBLE);
                        Toast.makeText(TTSLoginActivity.this, "Error: "+e.getMessage(),Toast.LENGTH_LONG).show();

                    });
                     return null;
                 }));
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


    public CompletableFuture<Boolean> makeUserLogin(String username, String password){
        CompletableFuture<Boolean> isLoggedIn = new CompletableFuture<>();
        Call<ResponseBody> call = userServiceInterface.login(username,password);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    APIResponse apiResponse = APIResponse.create(response);
                    Log.e("apiResponse", ""+apiResponse);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            ResponseBody responseBody = ((APISuccessResponse<ResponseBody>) apiResponse).getBody();
                            Log.e("responBody", ""+responseBody);
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                            if ("Successful".equals(message)) {
                                isLoggedIn.complete(true);
                            }
                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            isLoggedIn.completeExceptionally(new Throwable(erMsg));

                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            isLoggedIn.completeExceptionally(new Throwable("empty response"));
                        }
                    }
                }
                catch (ClassCastException e){
                    isLoggedIn.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    isLoggedIn.completeExceptionally(new Throwable("Exception occured while getting assigned tasks due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    isLoggedIn.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                isLoggedIn.completeExceptionally(new Throwable(t.getMessage()));
            }
        });
        return isLoggedIn;
    }
    // Method For Login User
//    public boolean loginUser(String userName, String password) {
//        boolean result = false;
//        Connection con;
//        try {
//            con = DatabaseHelper.getDBConnection();
//
//            PreparedStatement ps = con.prepareStatement("select * from AUTHENTICATION where USER_ID=? and PASSWORD=?");
//            ps.setString(1, userName);
//            ps.setString(2, password);
//
//            ResultSet rs = ps.executeQuery();
//
//            if (rs.next()) { result = true; }
//
//            rs.close();
//            ps.close();
//            con.close();
//        } catch (Exception e) { e.printStackTrace(); }
//
//        return result;
//    }



}
