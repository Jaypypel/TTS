package com.example.neptune.ttsapp;


import android.app.DatePickerDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;

import android.os.StrictMode;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ReportServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.UserServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TTSReportGenerationFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    UserServiceInterface userService;

    @Inject
    ReportServiceInterface reportService;

    private EditText startDate,endDate;
    private TextView user,date,time;
    private Button btnReportGenerate;
    private Spinner spinnerSelectUser;
    private int mYear, mMonth, mDay;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsreport_generation, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        user=view.findViewById(R.id.textViewRGUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getToken());

        date=view.findViewById(R.id.textViewRGDate);
        time=view.findViewById(R.id.textViewRGTime);


           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });

        startDate=view.findViewById(R.id.editTextRGStartDate);
        endDate=view.findViewById(R.id.editTextRGEndDate);

        btnReportGenerate=view.findViewById(R.id.buttonRGReportGenerate);
        spinnerSelectUser = view.findViewById(R.id.spinnerRGSelectUser);

        if (InternetConnectivity.isConnected()) {

            getUsernames().thenAccept(usernames -> {
                ArrayList<String>  users = usernames;
                users.add(0,"Select user");
                ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item,users);
                userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerSelectUser.setAdapter(userSelectAdapter);
            }).exceptionally(e -> {Toast.makeText(getActivity().getApplicationContext(), "can't update usernames", Toast.LENGTH_LONG).show();
                return null;
            });

        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}



        startDate.setFocusable(false);
        endDate.setFocusable(false);

        //Date Picker start
        startDate.setOnClickListener(v -> {
            //To show current date in the datepicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), (view12, year, month, dayOfMonth) ->
            startDate.setText(convertDateTime(dayOfMonth) + "/" + convertDateTime((month+1))  + "/" + year),mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();

        });

        endDate.setOnClickListener(v -> {

            //To show current date in the datepicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), (view1, year, month, dayOfMonth) ->
            endDate.setText(convertDateTime(dayOfMonth) + "/" + convertDateTime((month+1))  + "/" + year),mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();

        });

        btnReportGenerate.setOnClickListener(v -> {
            btnReportGenerate.setEnabled(false);
                if (InternetConnectivity.isConnected())
                {
                    if (isStartDateValid().isEmpty()){startDate.setError("Start Date Cannot Be Empty");
                        btnReportGenerate.setEnabled(true);
                        return;
                    }
                    if (isEndDateValid().isEmpty()){endDate.setError("Start Date Cannot Be Empty");
                        btnReportGenerate.setEnabled(true);
                        return;
                    }

                    getReportByUsernameAndDateRange(isUserValid(),isStartDateValid(),isEndDateValid()).thenAccept(file -> {

                        saveToPublicDownloads(getContext(),isUserValid()+ " Report "+ " For " +isStartDateValid() + " And " + isEndDateValid(),file);
                        btnReportGenerate.setEnabled(true);

                    }).exceptionally(e -> {
                        Toast.makeText(getActivity().getApplicationContext(), "Failed to get the file from the server ", Toast.LENGTH_LONG).show();
                        Log.e("Error", "getting error : "+e.getMessage() +" cause : "+e.getCause()+" error : "+e);
                        btnReportGenerate.setEnabled(true);

                        return null;
                    });
//                        generateUserDTSExcelReport(getUserDTSReportDetails(isUserValid(), isStartDateValid(), isEndDateValid()));
                        startDate.setText("");
                        endDate.setText("");

                }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    btnReportGenerate.setEnabled(true);
                }
        });

        return view;
    }

    // Add leading 0 when input date or time is single No like 5
    public String convertDateTime(int input) {
        if (input >= 10) { return String.valueOf(input); }
        else { return "0" + input; }
    }

    private String isUserValid()
    {
        return spinnerSelectUser.getSelectedItem().toString().trim();
    }

    private String isStartDateValid()
    {
        String date= startDate.getText().toString().trim().replaceAll("\\s+","");
        if(date.isEmpty()) { startDate.setError("Start Date Cannot Be Empty"); }
        return date;
    }

    private String isEndDateValid()
    {
        String date= endDate.getText().toString().trim().replaceAll("\\s+","");
        if(date.isEmpty()) { endDate.setError("Start Date Cannot Be Empty"); }
        return date;
    }

    private String getMonth()
    {
        String actualDate= startDate.getText().toString().trim().replaceAll("\\s+","");
        SimpleDateFormat month_date = new SimpleDateFormat("MMM yyyy", Locale.ENGLISH);
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

        Date date = null;
        try {
            date = sdf.parse(actualDate);
        } catch (ParseException e) { e.printStackTrace(); }

        return month_date.format(date);
    }


    public CompletableFuture<ArrayList<String>> getUsernames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> usernamesResponse = userService.getUsernames();
            usernamesResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                        if (apiResponse instanceof APISuccessResponse){
                            JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            Gson gson = new Gson();
                            Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                            if(result.isJsonArray()){
                                JsonArray usernames = result.getAsJsonArray();
                                ArrayList<String> list = gson.fromJson(usernames, listType);
                                future.complete(list);
                            }
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
                        future.completeExceptionally(new Throwable("Exception occured while getting usernames due to" + e.getMessage()));
                    }
                    catch (RuntimeException e) {
                        future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Network Request", "Failed: " + t.getMessage());

                }
            });

        });

        return future;
    }



    public CompletableFuture<byte[]> getReportByUsernameAndDateRange(String username, String startDate, String endDate){
        CompletableFuture<byte[]> future = new CompletableFuture<>();
        Call<okhttp3.ResponseBody> call = reportService.getDTSReportByUsernameAndDateRange(username,startDate,endDate);
        call.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(Call<okhttp3.ResponseBody> call, Response<okhttp3.ResponseBody> response) {
                try {
                    if (response.isSuccessful()) {
                        byte[] fileDate = response.body().bytes();
                        future.complete(fileDate);}
                    else future.completeExceptionally(new Throwable("Failed to retrieve report"));
                }


                catch (ClassCastException e){
                future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
            }
                catch (IOException e) {
                Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                future.completeExceptionally(new Throwable("Exception occured while getting a report due to" + e.getMessage()));
            }
                catch (RuntimeException e) {
                future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
            }
            }

            @Override
            public void onFailure(Call<okhttp3.ResponseBody> call, Throwable t) {
                future.completeExceptionally(new Throwable(t.getMessage()));
            }
        });

        return future;
    }

    private void saveToPublicDownloads(Context context, String fileName, byte[] fileData) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
        values.put(MediaStore.MediaColumns.MIME_TYPE, "application/vnd.ms-excel");
        values.put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

        ContentResolver resolver = context.getContentResolver();
        Uri uri = resolver.insert(MediaStore.Files.getContentUri("external"), values);
        if (uri == null) {
            Toast.makeText(context, "Failed to get URI for file", Toast.LENGTH_SHORT).show();
            return; // Exit if URI is null
        }
        try (OutputStream outputStream = resolver.openOutputStream(uri)) {
            if (outputStream != null) {
                outputStream.write(fileData);
                Toast.makeText(context, "File saved to Downloads", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(context, "Failed to open OutputStream", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Error saving file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
}
