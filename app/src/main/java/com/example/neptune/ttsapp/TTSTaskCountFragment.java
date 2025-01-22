package com.example.neptune.ttsapp;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


@AndroidEntryPoint
public class TTSTaskCountFragment extends Fragment {

    @Inject
    TaskHandlerInterface taskHandler;

    @Inject
    MeasurableServiceInterface measurableService;

    @Inject
    AppExecutors appExecutors;

    public TTSTaskCountFragment() { }

    private TextView tvPendingTask,tvAcceptedTask,tvCompletedTask,tvApprovalTask,user,date,time;
    private SessionManager sessionManager;
    private ListView listView;

    private static TaskAllocatedListCustomAdapter adapter;
    private ArrayList<TaskDataModel> dataModels;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {   // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttstask_count, container, false);

        tvPendingTask = view.findViewById(R.id.textViewPendingTask);
        tvAcceptedTask = view.findViewById(R.id.textViewAcceptedTask);
        tvCompletedTask = view.findViewById(R.id.textViewCompletedTask);
        tvApprovalTask = view.findViewById(R.id.textViewApprovalTask);

        user = view.findViewById(R.id.textViewTaskCountUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date = view.findViewById(R.id.textViewTaskCountDate);
        time = view.findViewById(R.id.textViewTaskCountTime);

        listView = view.findViewById(R.id.taskList);

           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });

        if (InternetConnectivity.isConnected()) {
            try {
                user.setText(sessionManager.getUserID());

                appExecutors.getNetworkIO().execute(() -> {
                    // Execute network tasks asynchronously
                    CompletableFuture<String> pendingTasksFuture = countPendingTaskByUser(sessionManager.getUserID()).exceptionally(e -> {Log.e("Error","EROR"+e.getMessage());
                        return "";
                    });
                    Log.d("Logs","pending Count "+pendingTasksFuture.join());
                    CompletableFuture<String> acceptedTasksFuture = countAcceptedTaskByUser(sessionManager.getUserID()).exceptionally(e -> {Log.e("Error","EROR"+e.getMessage());
                        return "";
                    });
                    Log.d("Logs","accepted Count "+acceptedTasksFuture.join());
                    CompletableFuture<String> approvedTasksFuture = countApprovedTaskByUser(sessionManager.getUserID()).exceptionally(e -> {Log.e("Error","EROR"+e.getMessage());
                        return "";
                    });
                    Log.d("Logs","approved Count "+approvedTasksFuture.join());
                    CompletableFuture<String> completedTasksFuture = countCompletedTaskByUser(sessionManager.getUserID()).exceptionally(e -> {Log.e("Error","EROR"+e.getMessage());
                        return "";
                    });
                    Log.d("Logs","completed Count "+completedTasksFuture.join());

                    // Combine all futures
                   CompletableFuture.allOf(
                            pendingTasksFuture, acceptedTasksFuture, approvedTasksFuture, completedTasksFuture
                    ).thenRun(() -> appExecutors.getMainThread().execute(() -> {
                       Toast.makeText(getContext(), "Running main thread to display tasks count", Toast.LENGTH_LONG).show();
                        tvPendingTask.setText("Pending Task      :  " + pendingTasksFuture.getNow("Loading..."));
                        tvAcceptedTask.setText("Accepted Task     :  " + acceptedTasksFuture.getNow("Loading..."));
                        tvApprovalTask.setText("Approved Task     :  " + approvedTasksFuture.getNow("Loading..."));
                        tvCompletedTask.setText("Completed Task    :  " + completedTasksFuture.getNow("Loading..."));
                    })).exceptionally(e -> {
                        appExecutors.getMainThread().execute(() -> {
                            Toast.makeText(getContext(),
                                    "Failed to fetch task counts due to " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                            tvPendingTask.setText("Pending Task      :  " + "Error");
                            tvAcceptedTask.setText("Accepted Task     :  " + "Error");
                            tvApprovalTask.setText("Approved Task     :  " + "Error");
                            tvCompletedTask.setText("Completed Task    :  " + "Error");
                        } );
                        return null;
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_LONG).show();
        }


        //Get Data From Database for Accepted Task And set to the ListView
        if (InternetConnectivity.isConnected()) {
            appExecutors
                    .getNetworkIO()
                    .execute(() -> getAssignedTask(sessionManager.getUserID())
                            .thenAccept(task -> appExecutors
                                    .getMainThread()
                                    .execute(() -> {
                dataModels = task;
                if(isAdded()){
                    adapter = new TaskAllocatedListCustomAdapter(task,getActivity().getApplicationContext());
                    listView.setAdapter(adapter);
                }
                     }))
                            .exceptionally(e -> {
                Toast.makeText(getActivity().getApplicationContext(),"Failed to get Tasks ",Toast.LENGTH_LONG).show();
                return null;
            }));

        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        listView.setOnItemClickListener((parent, v, position, id) -> {
            TaskDataModel dataModel= dataModels.get(position);
            getAllocatedMeasurableList(dataModel.getId()).thenAccept(measurables -> {
                appExecutors.getMainThread().execute(() -> {
                    Intent i = new Intent(getActivity(), TTSTaskDelegateListItemDetailsActivity.class);
                    i.putExtra("TaskProcessingItemDetails",dataModel);
                    i.putExtra("TaskProcessingMeasurableDetails",measurables);
                    startActivity(i);
                });
            }).exceptionally(e -> {
                Toast.makeText(getActivity().getApplicationContext(),"Failed to get the  accepted Tasks", Toast.LENGTH_LONG).show();
                return  null;
            });
        });

        return view;
    }

    public CompletableFuture<String> countAcceptedTaskByUser(String username){
        CompletableFuture<String> count = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandler.getAcceptedTaskCount(username);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,@NonNull Response<ResponseBody> response) {
                try{
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    Log.e("apiResponse",""+apiResponse);
                    if(apiResponse instanceof APISuccessResponse){
                        String bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsString();
                        count.complete(bodyContent);
                    }  if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        count.completeExceptionally(new Throwable(erMsg));

                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        count.completeExceptionally(new Throwable("empty response"));
                    }
                }
                catch (ClassCastException e){
                    count.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    count.completeExceptionally(new Throwable("Exception occured while getting no. of accepted tasks due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    count.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                count.completeExceptionally(t);
            }
        });

        return count;
    }
    public CompletableFuture<String> countPendingTaskByUser(String username){
        CompletableFuture<String> count = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandler.getPendingTaskCount(username);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,@NonNull Response<ResponseBody> response) {
                try{
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);

                    if(apiResponse instanceof APISuccessResponse){
                        String bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsString();
                        count.complete(bodyContent);
                    }  if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        count.completeExceptionally(new Throwable(erMsg));

                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        count.completeExceptionally(new Throwable("empty response"));
                    }
                }
                catch (ClassCastException e){
                    count.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    count.completeExceptionally(new Throwable("Exception occured while getting no. of Pending tasks due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    count.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                count.completeExceptionally(t);
            }
        });

        return count;
    }
    public CompletableFuture<String> countApprovedTaskByUser(String username){
        CompletableFuture<String> count = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandler.getApprovedTaskCount(username);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,@NonNull Response<ResponseBody> response) {

                try{
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof APISuccessResponse){
                        String bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsString();
                        count.complete(bodyContent);
                    }  if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        count.completeExceptionally(new Throwable(erMsg));

                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        count.completeExceptionally(new Throwable("empty response"));
                    }
                }
                catch (ClassCastException e){
                    count.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    count.completeExceptionally(new Throwable("Exception occured while getting no. of approved tasks due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    count.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                count.completeExceptionally(t);
            }
        });

        return count;
    }
    public CompletableFuture<String> countCompletedTaskByUser(String username){
        CompletableFuture<String> count = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandler.getCompletedTaskCount(username);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,@NonNull Response<ResponseBody> response) {
                try{
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof APISuccessResponse){

                        String bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsString();
                        count.complete(bodyContent);
                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        count.completeExceptionally(new Throwable(erMsg));

                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        count.completeExceptionally(new Throwable("empty response"));
                    }
                }
                catch (ClassCastException e){
                    count.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    count.completeExceptionally(new Throwable("Exception occured while getting no. of completed tasks due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    count.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                count.completeExceptionally(t);
            }
        });

        return count;
    }

    public CompletableFuture<ArrayList<TaskDataModel>> getAssignedTask(String receivedUsername){
        CompletableFuture<ArrayList<TaskDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandler.getTaskList(receivedUsername);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                TaskDataModel task;
                try{
                    APIResponse apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof APISuccessResponse){
                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type taskType = new TypeToken<ArrayList<TaskDataModel>>(){}.getType();
                        if (bodyContent.isJsonArray()){
                            JsonArray content = bodyContent.getAsJsonArray();
                            ArrayList<TaskDataModel> tasks = gson.fromJson(content,taskType);
                            future.complete(tasks);
                        }
                      //  return;
//                        Log.e("bodyContent",""+bodyContent);
//                        for (JsonElement item: bodyContent
//                        ) {
//                            JsonObject taskObj = item.getAsJsonObject();
//                            task = new TaskDataModel();
//                            task.setId(taskObj.get("id").getAsLong());
//                            JsonObject usr = taskObj.get("taskOwnerUserID").getAsJsonObject();
//                            task.setTaskDeligateOwnerUserID(usr.get("username").getAsString());
//                            task.setActivityName(taskObj.get("activityName").getAsString());
//                            task.setTaskName(taskObj.get("taskName").getAsString());
//                            task.setProjectNo(taskObj.get("projectCode").getAsString());
//                            task.setProjectName(taskObj.get("projectName").getAsString());
//                            task.setExpectedDate(taskObj.get("expectedDate").getAsString().split("T")[0]);
////                            task.setExpectedTotalTime(taskObj.get("expectedTotalTime").getAsString());
//                            task.setDescription(taskObj.get("description").getAsString());
//                            task.setActualTotalTime(taskObj.get("actualTotalTime").getAsString());
//                            task.setDeligationDateTime(taskObj.get("taskAssignedOn").getAsString());
//                            task.setSeenOn(taskObj.get("taskSeenOn").getAsString());
//                            task.setAcceptedOn(taskObj.get("taskAcceptedOn").getAsString());
//                            task.setStatus(taskObj.get("status").getAsString());
//                            tasks.add(task);
//
//                        }future.complete(tasks);
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

        return future;
    }


    public CompletableFuture<ArrayList<MeasurableListDataModel>> getAllocatedMeasurableList(Long taskId){
        CompletableFuture<ArrayList<MeasurableListDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = measurableService.getAllocatedMeasurableList(taskId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                ArrayList<MeasurableListDataModel> measurables = new ArrayList<>();
//                MeasurableListDataModel measurable;
                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof  APISuccessResponse){
                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type measurablesType = new TypeToken<ArrayList<MeasurableListDataModel>>(){}.getType();
                        if(bodyContent.isJsonArray()){
                            JsonArray content = bodyContent.getAsJsonArray();
                            ArrayList<MeasurableListDataModel> measurables = gson.fromJson(content,measurablesType);
                            future.complete(measurables);
                        }
//                        for (JsonElement e : bodyContent){
//                            JsonObject msrObj = e.getAsJsonObject();
//                            measurable = new MeasurableListDataModel();
//                            measurable.setId(msrObj.get("id").getAsString());
//                            measurable.setMeasurableName(msrObj.get("name").getAsString());
//                            measurables.add(measurable);
//                        }
//                        future.complete(measurables);
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

        return future;
    }
}
