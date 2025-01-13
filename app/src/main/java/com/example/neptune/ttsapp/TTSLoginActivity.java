package com.example.neptune.ttsapp;



import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import android.graphics.BitmapFactory;
import android.graphics.Color;


import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import android.os.Bundle;

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

import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

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
    private boolean isRequestInProgress = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttslogin);

        userName = findViewById(R.id.editTextLoginUsername);
        password = findViewById(R.id.editTextLoginPassword);
        btnLogin = findViewById(R.id.buttonSignin);
        btnRegister = findViewById(R.id.buttonRegister);
        togglePassword = findViewById(R.id.login_password_visibility);
        progressBarInLogin= findViewById(R.id.progressBarInLogin);
        progressBarInLogin.setVisibility(View.INVISIBLE);


            btnLogin.setOnClickListener(view -> {if( userLogin()){
                Toast.makeText(TTSLoginActivity.this, "Your request is in process ",Toast.LENGTH_LONG).show();
                btnLogin.setEnabled(false);
            }else{
                Toast.makeText(TTSLoginActivity.this, "Your request is completed ",Toast.LENGTH_LONG).show();
            }
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

    @Override
    public void onBackPressed() {
        finish();
    }

    public boolean userLogin()
    {
        btnLogin.setBackgroundColor(Color.GRAY);
        if (InternetConnectivity.isConnected()) {
            if (isValidUserId().isEmpty()) { userName.setError("User Name Cannot Be Empty");
                btnLogin.setBackgroundResource(android.R.drawable.btn_default);
                return false;
            }
             if (isValidPassword().length() < 8 || isValidPassword().isEmpty())
            {
                if (isValidPassword().isEmpty()) { password.setError("Password Cannot Be Empty"); btnLogin.setBackgroundResource(android.R.drawable.btn_default);return false; }
                if (isValidPassword().length() < 8) { password.setError("Please Enter Valid Password"); btnLogin.setBackgroundResource(android.R.drawable.btn_default);return false;}
                btnLogin.setBackgroundResource(android.R.drawable.btn_default);
                return false;
            }
                appExecutors.getNetworkIO().execute(() -> {

                  if(!makeUserLogin(isValidUserId(),isValidPassword()).isDone()) {
                      appExecutors.getMainThread().execute(() -> {
                          progressBarInLogin.setVisibility(View.INVISIBLE);
                          Toast.makeText(TTSLoginActivity.this, "Your request is in process ",Toast.LENGTH_LONG).show();
                          btnLogin.setBackgroundResource(android.R.drawable.btn_default);
                          btnLogin.setEnabled(false);

                      });
                      isRequestInProgress = false;

                  }

              makeUserLogin(isValidUserId(),isValidPassword()).thenAccept(isCredentialsValid -> {
                          if(isCredentialsValid){
                              appExecutors.getMainThread().execute(() -> {
                                  String userId = userName
                                          .getText()
                                          .toString()
                                          .trim()
                                          .replaceAll("\\s+", "");
                                  sessionManager = new SessionManager(getApplicationContext());
                                  sessionManager.setUserID(userId);
                                  Toast.makeText(TTSLoginActivity.this, "You're logged in now", Toast.LENGTH_SHORT).show();
                                  Intent i = new Intent(TTSLoginActivity.this, TTSMainActivity.class);
                                  startActivity(i);
                                  finish();
                                  btnLogin.setBackgroundResource(android.R.drawable.btn_default);
                                  isRequestInProgress = false;
                              });
                          }else{
                              appExecutors
                                      .getMainThread()
                                      .execute(() -> {
                                          progressBarInLogin.setVisibility(View.INVISIBLE);
                                          Toast.makeText(TTSLoginActivity.this, "You entered incorrect details ", Toast.LENGTH_SHORT).show();
                                          btnLogin.setBackgroundResource(android.R.drawable.btn_default);
                                          isRequestInProgress = false;

                                      });}
                      }).exceptionally(e -> {
                          appExecutors.getMainThread().execute(() -> {
                              progressBarInLogin.setVisibility(View.INVISIBLE);
                              Toast.makeText(TTSLoginActivity.this, "Error while making you logged in "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
                              btnLogin.setBackgroundResource(android.R.drawable.btn_default);
                              isRequestInProgress = false;

                          }); return null;
                      }).whenComplete((result,throwable) -> {
                   isRequestInProgress = false;
               });
                });

//                 appExecutors.getNetworkIO().execute(() -> makeUserLogin(isValidUserId(),isValidPassword()).thenAccept(isCredentialsValid -> {
//                    if (isCredentialsValid){
//                         appExecutors.getMainThread().execute(() -> {
//                             String userId = userName
//                                     .getText()
//                                     .toString()
//                                     .trim()
//                                     .replaceAll("\\s+", "");
//                             sessionManager = new SessionManager(getApplicationContext());
//                             sessionManager.setUserID(userId);
//                             Toast.makeText(TTSLoginActivity.this, "You're logged in now", Toast.LENGTH_SHORT).show();
//                             Intent i = new Intent(TTSLoginActivity.this, TTSMainActivity.class);
//                             startActivity(i);
//                             finish();
//                             btnLogin.setBackgroundResource(android.R.drawable.btn_default);
//                         });
//                     }else {
//                             appExecutors
//                                     .getMainThread()
//                                     .execute(() -> {
//                                 progressBarInLogin.setVisibility(View.INVISIBLE);
//                                 Toast.makeText(TTSLoginActivity.this, "You entered incorrect details ", Toast.LENGTH_SHORT).show();
//                                 btnLogin.setBackgroundResource(android.R.drawable.btn_default);
//                             });}
//
//                 }).exceptionally(e -> {
//                                appExecutors.getMainThread().execute(() -> {
//                                   progressBarInLogin.setVisibility(View.INVISIBLE);
//                                   Toast.makeText(TTSLoginActivity.this, "Error while making you logged in "+e.getLocalizedMessage(),Toast.LENGTH_LONG).show();
//                                   btnLogin.setBackgroundResource(android.R.drawable.btn_default);
//
//                               });
//
//                     return null;
//                 }));

        } else {
            Toast.makeText(TTSLoginActivity.this, "No Internet Connection", Toast.LENGTH_LONG).show();
            progressBarInLogin.setVisibility(View.INVISIBLE);
            isRequestInProgress = false;
        }
        return isRequestInProgress;
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
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            ResponseBody responseBody = ((APISuccessResponse<ResponseBody>) apiResponse).getBody();
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
}
