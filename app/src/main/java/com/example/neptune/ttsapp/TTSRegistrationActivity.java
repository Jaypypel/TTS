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

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.Calendar;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttsregistration);
//        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
//        StrictMode.setThreadPolicy(policy);


        fullName = (EditText) findViewById(R.id.editTextName);
        userName = (EditText) findViewById(R.id.editTextRegUserName);
        password = (EditText) findViewById(R.id.editTextRegPassword);
        retypePassword = (EditText) findViewById(R.id.editTextRetypePassword);
        email = (EditText) findViewById(R.id.editTextEmail);
        mobileNo = (EditText) findViewById(R.id.editTextMobileNo);

        btnCancel = (Button) findViewById(R.id.buttonCancel);
        btnSubmit = (Button) findViewById(R.id.buttonSubmit);

        togglePassword = (ToggleButton) findViewById(R.id.reg_password_visibility);
        toggleRetypePassword = (ToggleButton) findViewById(R.id.reg_retype_password_visibility);

        progressBar = (ProgressBar) findViewById(R.id.progressBarInReg);
        progressBar.setVisibility(View.INVISIBLE);


        btnSubmit.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                try {
                    if (InternetConnectivity.isConnected() == true) {
                        if (isValidFullName().isEmpty()) {
                            fullName.setError("Full Name Cannot Be Empty");
                        } else if (isValidUserId().isEmpty()) {
                            userName.setError("User Name Cannot Be Empty");
                        } else if (checkPassword().isEmpty() || checkPassword().length() < 8) {
                            Log.d("Reg", "In checkPassword If");
                            if (checkPassword().isEmpty()) {
                                password.setError("Password Cannot Be Empty");
                            }
                            if (checkPassword().length() < 8 && !isValidPassword(checkPassword())) {
                                password.setError("Please Enter Valid Password");
                            }
                        } else if (isValidEmail().isEmpty()) {
                            email.setError("Email Cannot Be Empty");
                        } else if (isValidMobileNo().isEmpty()) {
                            mobileNo.setError("Mobile No Cannot Be Empty");
                        } else {
                            Log.d("Reg", "In else");
                            progressBar.setVisibility(View.VISIBLE);
                         //   String result = registerUser(isValidFullName(), isValidUserId(), checkPassword(), isValidEmail(), isValidMobileNo(), delegationTime());
                            User userRegistration = new User(isValidFullName(), isValidUserId(), checkPassword(), isValidEmail(), isValidMobileNo());
                            appExecutors.getNetworkIO().execute(() -> {
                                Call<ResponseBody> userRegCall = userServiceInterface.registerUser(userRegistration);
                                userRegCall.enqueue(new Callback<ResponseBody>() {
                                    @Override
                                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                        runOnUiThread(() -> {
                                            progressBar.setVisibility(View.INVISIBLE);
//                                            if(response.isSuccessful() && response.body() !=null){
//                                                Log.d("Network Request", "User registered successfully: " + response.body());
//                                                Toast.makeText(TTSRegistrationActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
//                                                startActivity(new Intent(TTSRegistrationActivity.this,TTSLoginActivity.class));
//                                                finish();
//                                                return;
//                                            }
                                            APIResponse apiResponse = null;

                                            try {
                                               apiResponse = APIResponse.create(response);
                                               if(apiResponse instanceof APISuccessResponse){
                                                   String responseBody = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().toString();
                                                   JSONConfig jsonConfig = new JSONConfig();
                                                   String message = jsonConfig.extractMessageFromBodyFromJson(responseBody);
                                                   Log.d("Network Request", "Result" + message);
                                                   Toast.makeText(TTSRegistrationActivity.this,"Registration Successful",Toast.LENGTH_SHORT).show();
                                                   startActivity(new Intent(TTSRegistrationActivity.this,TTSLoginActivity.class));
                                                   finish();
                                                   return;
                                               }
                                               if(apiResponse instanceof APIErrorResponse){
                                                   Log.e("Network Request", "Error: " + ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage());
                                                   Toast.makeText(TTSRegistrationActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();

                                               }
                                               if(apiResponse instanceof APIEmptyResponse){
                                                   Log.d("Network Request", "Result: "+ apiResponse +"empty response");
                                                   Toast.makeText(TTSRegistrationActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                                               }
                                            }

                                            catch (IOException e) {
                                                throw new RuntimeException(e);
                                            }

                                        });
                                    }

                                    @Override
                                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                                        appExecutors.getMainThread().execute(() -> {
                                            Log.e("Network Request", "Failed to connect: " + t.getMessage());
                                            progressBar.setVisibility(View.INVISIBLE);
                                            Toast.makeText(TTSRegistrationActivity.this, "Error: "+t.getMessage(),Toast.LENGTH_LONG).show();
                                        });
                                    }


                                });
                            });
//                            if (result.equals("true")) {
//                                Toast.makeText(TTSRegistrationActivity.this, "Registration Successful", Toast.LENGTH_LONG).show();
//                                Intent i = new Intent(getApplicationContext(), TTSLoginActivity.class);
//                                startActivity(i);
//                                finish();
//                            } else {
//                                Toast.makeText(TTSRegistrationActivity.this, "Registration Failed", Toast.LENGTH_LONG).show();
//                                progressBar.setVisibility(View.INVISIBLE);
//                            }
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                        progressBar.setVisibility(View.INVISIBLE);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }


            }
        });


        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(getApplicationContext(), TTSLoginActivity.class);
                startActivity(i);
                finish();

            }
        });

        // Code for show/hide togglePassword Button
        togglePassword.setVisibility(View.INVISIBLE);
        password.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                togglePassword.setVisibility(View.VISIBLE);
                String pass = password.getText().toString().trim().replaceAll("\\s+", "");
                if (pass.length() == 0) {
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
        togglePassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
        toggleRetypePassword.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
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
            }
        });
    }


    //Start Checking FullName valid Or Not
    public String isValidFullName() {
        String fName = fullName.getText().toString();
        if (fName.isEmpty()) {
            fullName.setError("Full Name Cannot Be Empty");
        }
        return fName;
    }

    //Start Checking UserID valid Or Not
    public String isValidUserId() {
        String uName = userName.getText().toString();
        if (uName.isEmpty()) {
            userName.setError("User Name Cannot Be Empty");
        }
        return uName;
    }

    //Start Checking Email valid Or Not
    public String isValidEmail() {
        String mail = email.getText().toString().trim().replaceAll("\\s+", "");
        if (mail.isEmpty()) {
            email.setError("Email Cannot Be Empty");
        } else {
            String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
            CharSequence inputStr = mail;
            Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(inputStr);
            if (!matcher.matches()) {
                email.setError("Email Is Not Valid");
            }
        }
        return mail;
    }

    //Start Checking Password valid Or Not
    public static boolean isValidPassword(final String password) {

        final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$";
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public String checkPassword() {
        String passwordck = password.getText().toString().trim();
        if (passwordck.isEmpty()) {
            password.setError("Password Cannot Be Empty");
        } else if (passwordck.length() < 8 && !isValidPassword(passwordck)) {
            password.setError("Please Enter Valid Password");
        }
        return passwordck;
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

    //Start Checking MobileNo valid Or Not
    public String isValidMobileNo() {
        String mobile = mobileNo.getText().toString().trim().replaceAll("\\s+", "");
        String regexStr = "^[0-9]{10}$";

        Pattern pattern = Pattern.compile(regexStr);
        Matcher matcher = pattern.matcher(mobile);

        if (mobile.isEmpty()) {
            mobileNo.setError("Mobile No Cannot Be Empty");
        } else if (matcher.matches() == false) {
            mobileNo.setError("Please Enter Valid Mobile Number");
        }
        return mobile;
    }

    private Timestamp delegationTime() {
        Calendar calendar = Calendar.getInstance();
        Timestamp delegationTimestamp = new Timestamp(calendar.getTime().getTime());
        return delegationTimestamp;
    }







    //    // Method For Registration Of User
//    public String registerUser(String fullName, String userName, String password, String email, String mobileNo,Timestamp createdOn){
//        String result="false";
//        int x = 0;
//        Connection con;
//
//        try{
//            con = DatabaseHelper.getDBConnection();
//
//            PreparedStatement ps = con.prepareStatement("insert into AUTHENTICATION(FULL_NAME,USER_ID,PASSWORD,EMAIL,MOBILE_NO,CREATED_ON) values(?,?,?,?,?,?)");
//
//            ps.setString(1, fullName);
//            ps.setString(2, userName);
//            ps.setString(3, password);
//            ps.setString(4, email);
//            ps.setString(5, mobileNo);
//            ps.setTimestamp(6, createdOn);
//
//            x = ps.executeUpdate();
//
//            if(x==1){
//                result = "true";
//            }
//
//            ps.close();
//            con.close();
//        }
//        catch(Exception e){ e.printStackTrace(); }
//
//        return result;
//    }
}
