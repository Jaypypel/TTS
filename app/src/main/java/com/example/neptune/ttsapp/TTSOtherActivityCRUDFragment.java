package com.example.neptune.ttsapp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ActivityServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
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
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TTSOtherActivityCRUDFragment extends Fragment {
    
    @Inject
    AppExecutors appExecutors;
    
    @Inject
    ActivityServiceInterface activityService;
    
    public TTSOtherActivityCRUDFragment() { }

    private TextView user,date,time;
    private AutoCompleteTextView otherActivityName;
    private Button addOtherActivity;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsother_activity_crud, container, false);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        user=(TextView)view.findViewById(R.id.textViewOtherActivityCRUDUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=(TextView)view.findViewById(R.id.textViewOtherActivityCRUDDate);
        time=(TextView)view.findViewById(R.id.textViewOtherActivityCRUDTime);


           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });

        otherActivityName=(AutoCompleteTextView) view.findViewById(R.id.editTextOtherActivityCRUDOtherActivity);
        if (InternetConnectivity.isConnected()== true) {
            getOtherActivityNames().thenAccept(names -> {
                ArrayList<String>  otherActivityNames = names;
                ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,otherActivityNames);
                userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                otherActivityName.setAdapter(userSelectAdapter);
            }).exceptionally(e -> {Toast.makeText(getActivity().getApplicationContext(), "can't update activityNames", Toast.LENGTH_LONG).show();
                return null;
            });
            
       //     ArrayAdapter<String> measurableNameAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getOtherActivityList());
       //     otherActivityName.setAdapter(measurableNameAdapter);
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        addOtherActivity=(Button)view.findViewById(R.id.buttonOtherActivityCRUDAddOtherActivity);


        addOtherActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    if (InternetConnectivity.isConnected()== true)
                    {
                        if (isOtherActivityName().isEmpty()){otherActivityName.setError("Other Activity Name Be Empty");}
                        else
                        {   addOtherActivity(isOtherActivityName(), createdOn()).thenAccept(isMeasurbaleAdded -> {
                            if(isMeasurbaleAdded.equals("successful")){
                                appExecutors.getMainThread().execute(() ->
                                {
                                    Toast.makeText(getActivity().getApplicationContext(), "Other Activity Inserted ", Toast.LENGTH_LONG).show();
                                    otherActivityName.setText("");
                                });
                            }else {
                                appExecutors.getMainThread().execute(() -> Toast
                                        .makeText(getActivity()
                                                .getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG)
                                        .show());
                            }
                        }).exceptionally(e -> {
                            Toast.makeText(getActivity().getApplicationContext(), "Failed to add other activity due to "+e.getMessage(), Toast.LENGTH_LONG).show();

                            return null;
                        });

//                            String result = insertOtherActivity(isOtherActivityName(), createdOn());
//                            if (result.equals("true")) {
//                                Toast.makeText(getActivity().getApplicationContext(), "Other Activity Inserted ", Toast.LENGTH_LONG).show();
//                                otherActivityName.setText("");
//                            } else {
//                                Toast.makeText(getActivity().getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show();
//                            }
                        }
                    }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


                }
                catch (Exception e){e.printStackTrace();}
            }
        });

        return view;
    }

    private String isOtherActivityName()
    {
        String actName = otherActivityName.getText().toString();
        if(actName.isEmpty()) { otherActivityName.setError("Other Activity Name Be Empty"); }
        return actName;
    }

    private String createdOn()
    {
        return DateConverter.getCurrentDateTime();
    }


    private CompletableFuture<String> addOtherActivity( String activityName, String createdOn) {
        CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = activityService.addOtherActivity(activityName,createdOn);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                
                try {
                    APIResponse  apiResponse = APIResponse.create(response);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                            // JsonObject dtsobject = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonObject();

                            if ("successful".equals(message)) {
                                future.complete(message);
                            }
                        }
                    }
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Exception("API request failed: " + response.code()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                future.completeExceptionally(t);
            }
        });
        return future;
    }

    public CompletableFuture<ArrayList<String>> getOtherActivityNames() {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> usernamesResponse = activityService.getOtherActivityNames();
            usernamesResponse.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                        JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

                        if(result.isJsonArray()){
                            JsonArray usernames = result.getAsJsonArray();
                            ArrayList<String> list = gson.fromJson(usernames, listType);
                            future.complete(list);

                        }
                    } catch (IOException e) {
                        throw new RuntimeException(e);
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
}
