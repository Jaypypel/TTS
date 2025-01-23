package com.example.neptune.ttsapp;



import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
//import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ActivityServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.UserServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TTSActivityCRUDFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    UserServiceInterface userService;

    @Inject
    ActivityServiceInterface activityService;

    public TTSActivityCRUDFragment() { }

    private TextView user,date,time;
    private AutoCompleteTextView activityName;
    private Button addActivity;
    private Spinner userSelect;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsactivity_crud, container, false);

        user=view.findViewById(R.id.textViewActCRUDUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getToken());

        date=view.findViewById(R.id.textViewActCRUDDate);
        time=view.findViewById(R.id.textViewActCRUDTime);
        
        
        appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });


        activityName =view.findViewById(R.id.editTextActCRUDActivity);

        userSelect=view.findViewById(R.id.spinnerActCRUDUserSelect);
        if (InternetConnectivity.isConnected()) {

            getUsernames().thenAccept(usernames -> {
               ArrayList<String>  users = usernames;
                users.add(0,"Select user");
                ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
                userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSelect.setAdapter(userSelectAdapter);
            }).exceptionally(e -> {Toast.makeText(getActivity().getApplicationContext(), "can't update usernames", Toast.LENGTH_LONG).show();
                return null;
            });

        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        try {
            userSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Set AutoCompleteTextView
                    if (InternetConnectivity.isConnected()) {

                        activityName.setText("");
                        getActivityNameByUsername(getUser()).thenAccept(activityNames -> {
                            ArrayAdapter<String> activityNameAdapter = new ArrayAdapter<String>(requireContext(), android.R.layout.simple_list_item_1, activityNames);
                            activityName.setAdapter(activityNameAdapter);
                        }).exceptionally(e -> {Toast.makeText(requireContext(), "can't update activity names", Toast.LENGTH_LONG).show();
                                    return null;
                                });

                    } else {
                        Toast.makeText(requireContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }catch (Exception e){e.printStackTrace();}

        addActivity= view.findViewById(R.id.buttonActCRUDAddAct);


        addActivity.setOnClickListener(v -> {
                addActivity.setEnabled(false);

                if (InternetConnectivity.isConnected())
                {
                    if(isActivityName().isEmpty()){activityName.setError("Activity Name Be Empty");
                        addActivity.setEnabled(true);
                        return ;
                    }

                            addActivity(getUser(),isActivityName(),createdOn()).thenAccept(isActivityAdded -> {
                                if(isActivityAdded.equals("successful")){
                                    appExecutors.getMainThread().execute(() ->
                                    {
                                        Toast.makeText(getActivity().getApplicationContext(), "Activity Inserted ", Toast.LENGTH_LONG).show();
                                        activityName.setText("");
                                        addActivity.setEnabled(true);

                                    });
                                }else {
                                    appExecutors.getMainThread().execute(() ->
                                    {Toast.makeText(getActivity().getApplicationContext(), "insertion failed ", Toast.LENGTH_LONG).show();
                                        addActivity.setEnabled(true);});
                                }
                            }).exceptionally(e -> {
                                Toast.makeText(getActivity().getApplicationContext(), "Failed to add activity due to "+e.getMessage(), Toast.LENGTH_LONG).show();
                                addActivity.setEnabled(true);
                                return null;
                            });

                }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    addActivity.setEnabled(true);
                }

        });



        return view;
    }


    private String getUser()
    {
        String user = userSelect.getSelectedItem().toString().trim();
        return user;
    }

    private String isActivityName()
    {
        String actName = activityName.getText().toString();
        if(actName.isEmpty()) { activityName.setError("Activity Name Be Empty"); }
        return actName;
    }

    private String createdOn()
    {
       return DateConverter.getCurrentDateTime();
    }

    private CompletableFuture<String> addActivity(String username, String activityName, String createdOn) {
        CompletableFuture<String> result = new CompletableFuture<>();
        //it is used for to message & dts Id from ResponseBody object
        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = activityService.addActivity(username,activityName,createdOn);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    APIResponse apiResponse = null;
                    try {
                        apiResponse = APIResponse.create(response);
                        if (apiResponse != null) {
                            if (apiResponse instanceof APISuccessResponse) {
                                String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();

                                if ("successful".equals(message)) {
                                    result.complete(message);
                                }
                            }
                            if (apiResponse instanceof APIErrorResponse) {
                                String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                                result.completeExceptionally(new Throwable(erMsg));

                            }
                            if (apiResponse instanceof APIErrorResponse) {
                                result.completeExceptionally(new Throwable("empty response"));
                            }
                        }
                    }
                    catch (ClassCastException e){
                        result.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                        }
                    catch (IOException e) {
                            Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        result.completeExceptionally(new Throwable("Exception occured while getting measurables due to" + e.getMessage()));
                        }
                    catch (RuntimeException e) {
                        result.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                        }


                    }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    result.completeExceptionally(new Throwable(t.getMessage()));
                }
            });
        });

        return result;
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
                        if(apiResponse instanceof APISuccessResponse){
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
                        future.completeExceptionally(new Throwable("Exception occured while getting measurables due to" + e.getMessage()));
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

        });

        return future;
    }

    public CompletableFuture<ArrayList<String>> getActivityNameByUsername(String username) {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = activityService.getActivitiesNamebyUsername(username);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                       if(apiResponse instanceof  APISuccessResponse){
                           JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                           Gson gson = new Gson();
                           Type listType = new TypeToken<ArrayList<String>>() {}.getType();
                           if(result.isJsonArray()){
                               JsonArray activityNames = result.getAsJsonArray();
                               ArrayList<String> list = gson.fromJson(activityNames, listType);
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
                    future.completeExceptionally(new Throwable("Exception occured while getting measurables due to" + e.getMessage()));
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

        });

        return future;
    }
}
