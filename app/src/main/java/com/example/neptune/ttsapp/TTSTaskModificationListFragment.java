package com.example.neptune.ttsapp;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TTSTaskModificationListFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    TaskHandlerInterface taskHandlerInterface;

    @Inject
    MeasurableServiceInterface measurableService;

    public TTSTaskModificationListFragment() { }

    private TextView user,date,time;

    private ListView senderModificationTaskList,receiverModificationTaskList;

    private String userId;

    private SessionManager sessionManager;

    private static TaskAllocatedListCustomAdapter adapter;

    private ArrayList<TaskDataModel> senderDataModels,receiverDataModels;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsmodification_task_list, container, false);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=view.findViewById(R.id.textViewModificationListUser);
        user.setText(userId);

        date=view.findViewById(R.id.textViewModificationListDate);
        time=view.findViewById(R.id.textViewModificationListTime);

        senderModificationTaskList=view.findViewById(R.id.senderModificationTaskList);
        receiverModificationTaskList=view.findViewById(R.id.receiverModificationTaskList);


           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });


        if (InternetConnectivity.isConnected()) {
            appExecutors.getNetworkIO().execute(() -> {
                getSendModificationTaskList(getUserId(),"revised").thenAccept(tasks -> {
                    senderDataModels = tasks;
                    adapter = new TaskAllocatedListCustomAdapter(senderDataModels,getActivity().getApplicationContext());
                    senderModificationTaskList.setAdapter(adapter);
                }).exceptionally(e -> {
                    Toast.makeText(getContext().getApplicationContext(),"Failed to fetch outgoing tasks due to "+ e,Toast.LENGTH_LONG).show();
                    return null;
                });
            });
            appExecutors.getNetworkIO().execute(() -> {
                getReceiveModificationTaskList(getUserId(),"revised").thenAccept(tasks -> {
                    receiverDataModels = tasks;
                    adapter = new TaskAllocatedListCustomAdapter(receiverDataModels,getActivity().getApplicationContext());
                    receiverModificationTaskList.setAdapter(adapter);
                }).exceptionally(e -> {
                    Toast.makeText(getContext().getApplicationContext(),"Failed to fetch outgoing tasks due to "+ e,Toast.LENGTH_LONG).show();
                    return null;
                });
            });


        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

        senderModificationTaskList.setOnItemClickListener((parent, view1, position, id) -> {

            TaskDataModel dataModel= senderDataModels.get(position);
            appExecutors.getNetworkIO().execute(() -> {


                getModificationTaskMeasurableList(dataModel.getId()).thenAccept(measurables -> {
                    Intent i = new Intent(getActivity(), TTSTaskModificationListItemDetailsActivity.class);


                    i.putExtra("senderTaskModificationItemDetails",dataModel);
                    i.putExtra("senderTaskModificationMeasurableList", measurables);
                    startActivity(i);
                }).exceptionally(e -> {
                    Toast.makeText(getContext().getApplicationContext(),"Failed to fetch measurables  due to "+ e,Toast.LENGTH_LONG).show();
                    return  null;
                });

            });

        });

        receiverModificationTaskList.setOnItemClickListener((parent, view1, position, id) -> {

            TaskDataModel dataModel= receiverDataModels.get(position);
            appExecutors.getNetworkIO().execute(() -> {


                getModificationTaskMeasurableList(dataModel.getId()).thenAccept(measurables -> {
                    Intent i = new Intent(getActivity(), TTSTaskModificationListItemDetailsActivity.class);


                    i.putExtra("receiverTaskModificationItemDetails",dataModel);
                    i.putExtra("receiverTaskModificationMeasurableList", measurables);
                    startActivity(i);
                }).exceptionally(e -> {
                    Toast.makeText(getContext().getApplicationContext(),"Failed to fetch measurables  due to "+ e,Toast.LENGTH_LONG).show();
                    return  null;
                });

            });
        });

        return view;
    }

    private String getUserId()
    {
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        return sessionManager.getUserID();
    }

    public CompletableFuture<ArrayList<TaskDataModel>> getSendModificationTaskList(String taskOwnerUsername, String status){
        CompletableFuture<ArrayList<TaskDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandlerInterface.getTasksByTaskReceiveUsernameAndStatus(taskOwnerUsername,status);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ArrayList<TaskDataModel> tasks = new ArrayList<>();
                TaskDataModel task;
                try{
                    APIResponse apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof APISuccessResponse){
                        JsonArray bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonArray();


                        for (JsonElement item: bodyContent
                        ) {
                            JsonObject taskObj = item.getAsJsonObject();
                            task = new TaskDataModel();
                            task.setId(taskObj.get("id").getAsLong());
                            JsonObject usr = taskObj.get("taskReceivedUserID").getAsJsonObject();
                            task.setTaskDeligateOwnerUserID(usr.get("username").getAsString());
                            task.setActivityName(taskObj.get("activityName").getAsString());
                            task.setTaskName(taskObj.get("taskName").getAsString());
                            task.setProjectNo(taskObj.get("projectCode").getAsString());
                            task.setProjectName(taskObj.get("projectName").getAsString());
                            task.setExpectedDate(taskObj.get("expectedDate").getAsString().split("T")[0]);
                            //        task.setExpectedTotalTime(taskObj.get("expectedTotalTime").getAsString());
                            task.setDescription(taskObj.get("description").getAsString());
                            task.setActualTotalTime(taskObj.get("actualTotalTime").getAsString());
//                            task.setDeligationDateTime(taskObj.get("taskAssignedOn").getAsString());
//                            task.setSeenOn(taskObj.get("taskSeenOn").getAsString());
//                            task.setAcceptedOn(taskObj.get("taskAcceptedOn").getAsString());
                            task.setDeligationDateTime(taskObj.get("taskAssignedOn").getAsString());
                            task.setStatus(taskObj.get("status").getAsString());
                            tasks.add(task);

                        }future.complete(tasks);
                    }

                    if (apiResponse instanceof APIErrorResponse){
                        String msg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        future.completeExceptionally(new RuntimeException("Error while fetching taskList :"+msg+"ResponseCode :"+response.code()));
                    }

                    if(apiResponse instanceof APIErrorResponse){
                        future.completeExceptionally(new Throwable("Response is empty"));
                    }
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                future.completeExceptionally(t);
                Log.e("Error", "Request Failed: " + t.getMessage(), t);
            }
        });

        return future;
    }


    public CompletableFuture<ArrayList<TaskDataModel>> getReceiveModificationTaskList(String receivedUsername, String status){
        CompletableFuture<ArrayList<TaskDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandlerInterface.getTasksByTaskOwnerUsernameAndStatus(receivedUsername,status);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ArrayList<TaskDataModel> tasks = new ArrayList<>();
                TaskDataModel task;
                try{
                    APIResponse apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof APISuccessResponse){
                        JsonArray bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonArray();


                        for (JsonElement item: bodyContent
                        ) {
                            JsonObject taskObj = item.getAsJsonObject();
                            task = new TaskDataModel();
                            task.setId(taskObj.get("id").getAsLong());
                            JsonObject taskReceivedUserID = taskObj.get("taskReceivedUserID").getAsJsonObject();
                            JsonObject taskOwnerUserID = taskObj.get("taskOwnerUserID").getAsJsonObject();
                            task.setTaskReceivedUserId(taskReceivedUserID.get("username").getAsString());
                            task.setTaskDeligateOwnerUserID(taskOwnerUserID.get("username").getAsString());
                            task.setActivityName(taskObj.get("activityName").getAsString());
                            task.setTaskName(taskObj.get("taskName").getAsString());
                            task.setProjectNo(taskObj.get("projectCode").getAsString());
                            task.setProjectName(taskObj.get("projectName").getAsString());
                            task.setExpectedDate(taskObj.get("expectedDate").getAsString().split("T")[0]);
                            //        task.setExpectedTotalTime(taskObj.get("expectedTotalTime").getAsString());
                            task.setDescription(taskObj.get("description").getAsString());
                            task.setActualTotalTime(taskObj.get("actualTotalTime").getAsString());
                            task.setDeligationDateTime(taskObj.get("taskAssignedOn").getAsString());
//                            task.setSeenOn(taskObj.get("taskSeenOn").getAsString());
//                            task.setAcceptedOn(taskObj.get("taskAcceptedOn").getAsString());
                            task.setStatus(taskObj.get("status").getAsString());
                            tasks.add(task);

                        }future.complete(tasks);
                    }

                    if (apiResponse instanceof APIErrorResponse){
                        String msg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        future.completeExceptionally(new RuntimeException("Error while fetching taskList :"+msg+"ResponseCode :"+response.code()));
                    }

                    if(apiResponse instanceof APIErrorResponse){
                        future.completeExceptionally(new Throwable("Response is empty"));
                    }
                } catch (IOException e) {
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                future.completeExceptionally(t);
                Log.e("Error", "Request Failed: " + t.getMessage(), t);
            }
        });

        return future;
    }


    public CompletableFuture<ArrayList<MeasurableListDataModel>> getModificationTaskMeasurableList(Long taskId){
        CompletableFuture<ArrayList<MeasurableListDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = measurableService.getAllocatedMeasurableList(taskId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ArrayList<MeasurableListDataModel> measurables = new ArrayList<>();
                MeasurableListDataModel measurable;
                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof APISuccessResponse){
                        JsonArray bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonArray();
                        for (JsonElement e : bodyContent){
                            JsonObject msrObj = e.getAsJsonObject();
                            measurable = new MeasurableListDataModel();
                            measurable.setId(msrObj.get("id").getAsString());
                            measurable.setMeasurableName(msrObj.get("name").getAsString());
                            measurables.add(measurable);
                        }
                        future.complete(measurables);
                    }
                    if (apiResponse instanceof APIErrorResponse){
                        String msg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        future.completeExceptionally(new RuntimeException("Error while fetching measurableList :"+msg+"ResponseCode :"+response.code()));
                    }

                    if(apiResponse instanceof APIErrorResponse){
                        future.completeExceptionally(new Throwable("Response is empty"));
                    }
                } catch (IOException e) {
                    Log.e("Error", "IOException occurred" + e.getMessage(), e);
                    future.completeExceptionally(e);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Error", "Request Failed: " + t.getMessage(), t);
                future.completeExceptionally(t);
            }
        });

        return future;
    }
  }
