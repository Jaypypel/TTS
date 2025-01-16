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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ActivityServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskServiceInterface;
import com.example.neptune.ttsapp.Network.UserServiceInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Date;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@AndroidEntryPoint
public class TTSTaskCRUDFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    TaskServiceInterface taskService;

    @Inject
    UserServiceInterface userService;

    @Inject
    ActivityServiceInterface activityService;

    public TTSTaskCRUDFragment() { }

    private TextView user,date,time;
    private AutoCompleteTextView taskName;
    private Button addTask;
    private Spinner userSelect,activitySelect;

    private SessionManager sessionManager;

    private ArrayList<ActivityDataModel> activityDataModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttstask_crud, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        user=view.findViewById(R.id.textViewTaskCRUDUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=view.findViewById(R.id.textViewTaskCRUDDate);
        time=view.findViewById(R.id.textViewTaskCRUDTime);


           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });


        taskName= view.findViewById(R.id.editTextTaskCRUDTaskName);
        addTask=view.findViewById(R.id.buttonTaskCRUDAdd);
        userSelect= view.findViewById(R.id.spinnerTaskCRUDUserSelect);


        if (InternetConnectivity.isConnected())
        {
            getUsernames().thenAccept(usernames -> {
                ArrayList<String>  users = usernames;
                users.add(0,"Select user");
                ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
                userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                userSelect.setAdapter(userSelectAdapter);
            }).exceptionally(e -> {Toast.makeText(getActivity().getApplicationContext(), "can't update usernames", Toast.LENGTH_LONG).show();
                return null;
            });

        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}



        activitySelect= view.findViewById(R.id.spinnerTaskCRUDActivitySelect);

        if (InternetConnectivity.isConnected())
        {
            userSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {

                    taskName.setText("");
                    appExecutors.getNetworkIO().execute(() -> getTaskNamesByUsername(getUser()).thenAccept(taskNames -> {
                        ArrayAdapter<String> taskNameAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,taskNames);
                        taskName.setAdapter(taskNameAdapter);
                    }).exceptionally(e -> {
                        Toast.makeText(getActivity().getApplicationContext(), "can't update task names", Toast.LENGTH_LONG).show();
                        return null;
                    }));




                    appExecutors.getNetworkIO().execute(() -> getActivities(getUser()).thenAccept(activities ->{
                        activityDataModels = activities;
                        ArrayAdapter<ActivityDataModel> activitySelectAdapter = new ArrayAdapter<ActivityDataModel>
                                (requireContext(), android.R.layout.simple_spinner_item,activityDataModels);
                        activitySelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                        activitySelect.setAdapter(activitySelectAdapter);
                    }).exceptionally(e -> {
                        Toast.makeText(requireContext(),"Failed to get activities",Toast.LENGTH_LONG).show();
                        return null;
                    }));

                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}



        addTask.setOnClickListener(v -> {
            addTask.setEnabled(false);
            try
            {
                if (InternetConnectivity.isConnected()) {
                    if (isTaskName().isEmpty()) {
                        taskName.setError("Task Name Be Empty");
                        addTask.setEnabled(true);
                        return;
                    }

                        addTask(getUser(), getAct(), isTaskName(), createdOn()).thenAccept(isTaskAdded -> {
                            if(isTaskAdded.equals("successful")){
                                appExecutors.getMainThread().execute(() ->
                                {
                                    Toast.makeText(getActivity().getApplicationContext(), "Task Inserted ", Toast.LENGTH_LONG).show();
                                    taskName.setText("");
                                    addTask.setEnabled(true);
                                });
                            }else {
                                appExecutors.getMainThread().execute(() -> { Toast
                                        .makeText(getActivity()
                                                .getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG)
                                        .show();
                                    addTask.setEnabled(true);
                                });
                            }
                        }).exceptionally(e -> {
                            Toast.makeText(getActivity().getApplicationContext(), "Failed to add activity due to "+e.getMessage(), Toast.LENGTH_LONG).show();
                            addTask.setEnabled(true);
                            return null;
                        });

                }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    addTask.setEnabled(true);}

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

    private Long getAct()
    {
        String activity = activitySelect.getSelectedItem().toString().trim();
        return Long.valueOf(activity.split("-")[0]);
    }

    private String isTaskName()
    {
        String task = taskName.getText().toString();
        if(task.isEmpty()) { taskName.setError("Task Name Be Empty"); }
        return task;
    }


    private String createdOn()
    {
//        Calendar calendar = Calendar.getInstance();
//        Timestamp delegationTimestamp = new Timestamp(calendar.getTime().getTime());
//        return delegationTimestamp.toString();
        return DateConverter.getCurrentDateTime();
    }

   

    public CompletableFuture<ArrayList<ActivityDataModel>> getActivities(String username) {
        CompletableFuture<ArrayList<ActivityDataModel>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> call = activityService.getActivities(username);
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {


                    try {
                        APIResponse apiResponse =   APIResponse.create(response);
                        if(apiResponse instanceof  APISuccessResponse){
                            JsonElement result = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                            Gson gson = new Gson();
                            Type listType = new TypeToken<ArrayList<ActivityDataModel>>() {}.getType();
                            if(result.isJsonArray()){
                                JsonArray jsonArray = result.getAsJsonArray();
                                ArrayList<ActivityDataModel> list = gson.fromJson(jsonArray, listType);
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
                        future.completeExceptionally(new Throwable("Exception occured while getting activities due to" + e.getMessage()));
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
                    future.completeExceptionally(new Throwable(t.getMessage()));
                }
            });

        });

        return future;
    }

    public CompletableFuture<ArrayList<String>> getTaskNamesByUsername(String username) {
        CompletableFuture<ArrayList<String>> future = new CompletableFuture<>();

        appExecutors.getNetworkIO().execute(() -> {
            Call<ResponseBody> usernamesResponse = taskService.getTaskNamesByUsername(username);
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
                        future.completeExceptionally(new Throwable("Exception occured while getting tasks due to" + e.getMessage()));
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
    private CompletableFuture<String> addTask(String username, Long activityId,  String taskName, String createdOn) {
        CompletableFuture<String> future = new CompletableFuture<>();
        //it is used for to message & dts Id from ResponseBody object
        Call<ResponseBody> call = taskService.addTask(username,activityId,taskName,createdOn);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                APIResponse apiResponse = null;
                try {
                    apiResponse = APIResponse.create(response);
                    if (apiResponse != null) {
                        if (apiResponse instanceof APISuccessResponse) {
                            String message = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                            // JsonObject dtsobject = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonObject();
                            if ("successful".equals(message)) {
                                future.complete(message);
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
                }
                catch (ClassCastException e){
                    future.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                catch (IOException e) {

                        future.completeExceptionally(new Throwable("Exception occured while adding a task due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                future.completeExceptionally(new Throwable(t.getMessage()));
            }
        });
        return future;
    }
}
