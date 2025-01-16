package com.example.neptune.ttsapp;

import android.content.Intent;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.neptune.ttsapp.Network.APIEmptyResponse;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.JSONConfig;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.UserServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@AndroidEntryPoint
public class TTSRegistrationActivity extends AppCompatActivity {

    @Inject
    UserServiceInterface userServiceInterface;

    @Inject
    AppExecutors appExecutors;

    private EditText fullName, userName, password, retypePassword, email, mobileNo;
    private Button btnCancel, btnSubmit;
    private ToggleButton togglePassword, toggleRetypePassword;

    private ProgressBar progressBar;

    private boolean isRequestInProcess = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttsregistration);
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);


        fullName =  findViewById(R.id.editTextName);
        userName =  findViewById(R.id.editTextRegUserName);
        password =  findViewById(R.id.editTextRegPassword);
        retypePassword =  findViewById(R.id.editTextRetypePassword);
        email =  findViewById(R.id.editTextEmail);
        mobileNo =  findViewById(R.id.editTextMobileNo);

        btnCancel =  findViewById(R.id.buttonCancel);
        btnSubmit =  findViewById(R.id.buttonSubmit);

        togglePassword =  findViewById(R.id.reg_password_visibility);
        toggleRetypePassword =  findViewById(R.id.reg_retype_password_visibility);

        progressBar = findViewById(R.id.progressBarInReg);
        progressBar.setVisibility(View.INVISIBLE);


        btnSubmit.setOnClickListener(view -> {
            processUserRegistration();

        });



        btnCancel.setOnClickListener(view -> {
            Intent i = new Intent(getApplicationContext(), TTSLoginActivity.class);
            startActivity(i);
            finish();

        });

        // Code for show/hide togglePassword Button
        togglePassword.setVisibility(View.INVISIBLE);
        password.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                togglePassword.setVisibility(View.VISIBLE);
                String pass = password.getText().toString().trim().replaceAll("\\s+", "");
                if (pass.isEmpty()) {
                    togglePassword.setVisibility(View.INVISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                togglePassword.setVisibility(View.INVISIBLE);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Code for show/hide Password
        togglePassword.setOnClickListener(v -> {
            if (togglePassword.isChecked()) {
                //Button is ON
                //Show Password
                password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                // Code for set focus to right of text in EditText
                int pos = password.getText().length();
                password.setSelection(pos);
            } else {
                //Button is OFF
                // hide password
                password.setTransformationMethod(PasswordTransformationMethod.getInstance());

                // Code for set focus to right of text in EditText
                int pos = password.getText().length();
                password.setSelection(pos);
            }
        });


        // Code for show/hide togglePassword Button
        toggleRetypePassword.setVisibility(View.INVISIBLE);
        retypePassword.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                toggleRetypePassword.setVisibility(View.VISIBLE);
                String retypePass = retypePassword.getText().toString().trim().replaceAll("\\s+", "");
                if (retypePass.length() == 0) {
                    toggleRetypePassword.setVisibility(View.INVISIBLE);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                toggleRetypePassword.setVisibility(View.INVISIBLE);
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        // Code for show/hide Retype Password
        toggleRetypePassword.setOnClickListener(v -> {
            if (toggleRetypePassword.isChecked()) {
                //Button is ON
                //Show Password
                retypePassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                // Code for set focus to right of text in EditText
                int pos = retypePassword.getText().length();
                retypePassword.setSelection(pos);
            } else {
                //Button is OFF
                // hide password
                retypePassword.setTransformationMethod(PasswordTransformationMethod.getInstance());

                // Code for set focus to right of text in EditText
                int pos = retypePassword.getText().length();
                retypePassword.setSelection(pos);
            }
        });
    }



    public void processUserRegistration(){
            btnSubmit.setEnabled(false);
            if (InternetConnectivity.isConnected()) {
                if (!isValidFullName() || !isValidUserId() || !checkPassword() || !isValidEmail() || !isValidMobileNo()) {
                    appExecutors.getMainThread().execute(() -> Toast
                            .makeText(TTSRegistrationActivity
                                    .this, "Details entered aren't validated, Please Entered Details Again", Toast.LENGTH_LONG)
                            .show());
                    btnSubmit.setEnabled(true);
                    return;
                }
                progressBar.setVisibility(View.VISIBLE);
                if(isRequestInProcess){
                    Toast.makeText(TTSRegistrationActivity.this,"Request in process",Toast.LENGTH_LONG).show();
                    return;
                }
                //   String result = registerUser(isValidFullName(), isValidUserId(), checkPassword(), isValidEmail(), isValidMobileNo(), delegationTime());
                User newUser= new User(fName, uName, passwordck,mail,mobile);



                    isRequestInProcess = true;
                    registerUser(newUser).thenAccept(result -> {
                        if(result.equals("successful")){
                            appExecutors.getMainThread().execute(() -> {
                                Toast.makeText(TTSRegistrationActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
                                startActivity(new Intent(TTSRegistrationActivity.this,TTSLoginActivity.class));
                                finish();

                            });
                        }else{
                            appExecutors.getMainThread().execute(() -> {Toast.makeText(TTSRegistrationActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();  btnSubmit.setEnabled(true);});
                            isRequestInProcess = false;
                        }
                    }).exceptionally(e -> {
                        appExecutors.getMainThread().execute(() -> {
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(TTSRegistrationActivity.this, "Registration Failed due to " +e.getMessage(), Toast.LENGTH_LONG).show();
                            btnSubmit.setEnabled(true);
                        });
                        isRequestInProcess = false;

                        return null;
                    }).whenComplete((result,throwable)-> isRequestInProcess = false);


            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                progressBar.setVisibility(View.INVISIBLE);
                btnSubmit.setEnabled(true);

            }
    }

    String fName;
    //Start Checking FullName valid Or Not
    public boolean isValidFullName() {
//        String fName = fullName.getText().toString().trim();
        fName = fullName.getText().toString().trim();
        if (fName.isEmpty() ) {
            fullName.setError("Full Name Cannot Be Empty");
            return false;
        }
        if(fName.length()<=6){
            fullName.setError("Enter Valid Full Name");
            return false;
        }
        return true;
    }
    String uName;
    //Start Checking UserID valid Or Not
    public boolean isValidUserId() {

        uName = userName.getText().toString().trim();
        if (uName.isEmpty()) {
            userName.setError("User Name Cannot Be Empty");
            return false;
        }
        if (uName.length() <=5){
            fullName.setError("Enter Valid Full Name");
            return false;
        }
        return true;
    }

    String mail;
    //Start Checking Email valid Or Not
    public boolean isValidEmail() {

        mail = email.getText().toString().trim().replaceAll("\\s+", "");
        if (mail.isEmpty()) {
            email.setError("Email Cannot Be Empty");
            return false;
        }
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        CharSequence inputStr = mail;
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(inputStr);
        if (!matcher.matches()) {
                email.setError("Email Is Not Valid");
                return false;
            }
            return true;
    }



    //Start Checking Password valid Or Not
    private  boolean isValidPassword(final String password) {

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }
    String passwordck;
    public boolean checkPassword() {
        //String passwordck = password.getText().toString().trim();
        passwordck = password.getText().toString().trim();
        if (passwordck.isEmpty()) {
            password.setError("Password Cannot Be Empty");
            return false;
        }
        if (passwordck.length() < 8 && !isValidPassword(passwordck)) {
             password.setError("Please Enter Valid Password");
             return false;
         }
        return true;
    }

    public void isValidRetypePassword() {
        String strPass1 = password.getText().toString().trim().replaceAll("\\s+", "");
        String strPass2 = retypePassword.getText().toString().trim().replaceAll("\\s+", "");

        if (strPass2.isEmpty()) {
            retypePassword.setError("Password Cannot Be Empty");
        } else if (strPass1.equals(strPass2)) {
            Toast.makeText(TTSRegistrationActivity.this, "Password Matched", Toast.LENGTH_SHORT).show();
        } else {
            retypePassword.setError("Password Not Matched");
        }

    }
    String mobile;
    //Start Checking MobileNo valid Or Not
    public boolean isValidMobileNo() {
       // String mobile = mobileNo.getText().toString().trim().replaceAll("\\s+", "");
        mobile  = mobileNo.getText().toString().trim().replaceAll("\\s+", "");
        String regexStr = "^[0-9]{10}$";

        Pattern pattern = Pattern.compile(regexStr);
        Matcher matcher = pattern.matcher(mobile);

        if (mobile.isEmpty()) {
            mobileNo.setError("Mobile No Cannot Be Empty");
            return false;
        }
        if (!matcher.matches()) {
            mobileNo.setError("Please Enter Valid Mobile Number");
            return false;
        }
        return true;
    }

    private String delegationTime() {
       return DateConverter.getCurrentDateTime();
    }

    public CompletableFuture<String> registerUser(User inputUser){
        CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = userServiceInterface.registerUser(inputUser);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    APIResponse apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof APISuccessResponse){
                        Log.e("update","going inside of api success block");
                        String msg = ((APISuccessResponse<ResponseBody>) apiResponse)
                                .getBody()
                                .getMessage()
                                .getAsString();
                        Log.e("Response","msg - "+msg);
                        future.complete(msg);
                    }


                    if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        future.completeExceptionally(new Throwable(erMsg));

                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        future.completeExceptionally(new Throwable("empty response"));
                    }
                }
                catch (ClassCastException e){
                    future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Throwable("Exception occurred while  your registration due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                future.completeExceptionally(new Throwable(t.getMessage()));
            }
        });
        return future;
    }
}


