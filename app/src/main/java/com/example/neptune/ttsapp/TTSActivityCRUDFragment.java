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
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        user=(TextView)view.findViewById(R.id.textViewActCRUDUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=(TextView)view.findViewById(R.id.textViewActCRUDDate);
        time=(TextView)view.findViewById(R.id.textViewActCRUDTime);


        final Handler someHandler = new Handler(Looper.getMainLooper());
        someHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date1 = new Date();
                String currentDate = formatter.format(date1);
                date.setText("Date :  " +currentDate);

                SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
                Date time1 = new Date();
                String currentTime = timeFormatter.format(time1);
                time.setText("Time :  " +currentTime);

                someHandler.postDelayed(this, 1000);
            }
        }, 10);

        activityName =(AutoCompleteTextView) view.findViewById(R.id.editTextActCRUDActivity);

        userSelect=(Spinner) view.findViewById(R.id.spinnerActCRUDUserSelect);
        if (InternetConnectivity.isConnected()== true) {

            getUsernames().thenAccept(usernames -> {
               ArrayList<String>  users = usernames;
                //users.add(0,"Select user");
                ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
                userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSelect.setAdapter(userSelectAdapter);
            }).exceptionally(e -> {Toast.makeText(getActivity().getApplicationContext(), "can't update usernames", Toast.LENGTH_LONG).show();
                return null;
            });


//            ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
//            userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            userSelect.setAdapter(userSelectAdapter);
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        try {
            userSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Set AutoCompleteTextView
                    if (InternetConnectivity.isConnected()) {

                        activityName.setText("");
                        getActivityNameByUsername(getUser()).thenAccept(activityNames -> {
                            Log.e("activityNames",""+activityNames);
                            ArrayAdapter<String> activityNameAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, activityNames);
                            activityName.setAdapter(activityNameAdapter);
                        }).exceptionally(e -> {Toast.makeText(getActivity().getApplicationContext(), "can't update activity names", Toast.LENGTH_LONG).show();
                                    return null;
                                });
//                        ArrayAdapter<String> activityNameAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getActivityList(getUser()));
//                        activityName.setAdapter(activityNameAdapter);

                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }catch (Exception e){e.printStackTrace();}

        addActivity=(Button)view.findViewById(R.id.buttonActCRUDAddAct);


        addActivity.setOnClickListener(v -> {

            try
            {
                if (InternetConnectivity.isConnected()== true)
                {
                    if(isActivityName().isEmpty()){activityName.setError("Activity Name Be Empty");}
                    else
                        {
                            addActivity(getUser(),isActivityName(),createdOn()).thenAccept(isActivityAdded -> {
                                if(isActivityAdded.equals("successful")){
                                    appExecutors.getMainThread().execute(() ->
                                    {
                                        Toast.makeText(getActivity().getApplicationContext(), "Activity Inserted ", Toast.LENGTH_LONG).show();
                                        activityName.setText("");
                                    });
                                }else {
                                    appExecutors.getMainThread().execute(() ->
                                    {
                                        Toast.makeText(getActivity().getApplicationContext(), "insertion failed ", Toast.LENGTH_LONG).show();

                                    });
                                }
                            }).exceptionally(e -> {
                                Toast.makeText(getActivity().getApplicationContext(), "Failed to add activity due to "+e.getMessage(), Toast.LENGTH_LONG).show();

                                return null;
                            });

//                            String result = insertActivity(getUser(), isActivityName(), createdOn());
//                            if (result.equals("true")) {
//                                Toast.makeText(getActivity().getApplicationContext(), "Activity Inserted ", Toast.LENGTH_LONG).show();
//                                activityName.setText("");
//                            } else {
//                                Toast.makeText(getActivity().getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show();
//                            }
                        }
                }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

            }
            catch (Exception e){e.printStackTrace();}
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
                        }
                    } catch (IOException e) {
                        Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                        result.completeExceptionally(new Exception("API request failed: " + response.code()));
                    }

                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    result.completeExceptionally(t);
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
                        JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

                        if(result.isJsonArray()){
                            JsonArray usernames = result.getAsJsonArray();
                            ArrayList<String> list = gson.fromJson(usernames, listType);
                            future.complete(list);

                        }
                    } catch (RuntimeException e) {
                        Log.e("Error", ""+e.getMessage());
                    } catch (IOException e) {
                        Log.e("Error", ""+e.getMessage());
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

    public CompletableFuture<ArrayList<String>> getActivityNameByUsername(String username) {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = activityService.getActivitiesNamebyUsername(username);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                        JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type listType = new TypeToken<ArrayList<String>>() {}.getType();

                        if(result.isJsonArray()){
                            JsonArray activityNames = result.getAsJsonArray();
                            ArrayList<String> list = gson.fromJson(activityNames, listType);
                            future.complete(list);

                        }
                    }
                    catch (ClassCastException e) {
                        Log.e("Error", ""+e.getMessage());
                    }
                    catch (RuntimeException e) {
                        Log.e("Error", ""+e.getMessage());
                    } catch (IOException e) {
                        Log.e("Error", ""+e.getMessage());
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
