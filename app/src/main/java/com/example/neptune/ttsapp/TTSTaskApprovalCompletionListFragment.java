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
public class TTSTaskApprovalCompletionListFragment extends Fragment {


    @Inject
    AppExecutors appExecutors;

    @Inject
    TaskHandlerInterface taskHandler;

    @Inject
    MeasurableServiceInterface measurableService;

    private TextView user,date,time;

    private ListView senderApprovalCompletionTaskList, receiverApprovalCompletionTaskList;

    private String userId;

    private SessionManager sessionManager;

    private static TaskAllocatedListCustomAdapter adapter;

    private ArrayList<TaskDataModel> senderDataModels,receiverDataModels;
    private TaskDataModel dataModel;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsapproval_completion_list, container, false);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=view.findViewById(R.id.textViewApprovalCompletionUser);
        user.setText(userId);

        date=view.findViewById(R.id.textViewApprovalCompletionListDate);
        time=view.findViewById(R.id.textViewApprovalCompletionListTime);

        senderApprovalCompletionTaskList =view.findViewById(R.id.senderApprovalCompletionTaskList);
        receiverApprovalCompletionTaskList =view.findViewById(R.id.receiverApprovalCompletionTaskList);



        appExecutors.getMainThread().execute(() -> {
            date.setText("Date : "+DateConverter.currentDate());
            time.setText("Time : "+DateConverter.currentTime());
        });
//        final Handler someHandler = new Handler(Looper.getMainLooper());
//        someHandler.postDelayed(new Runnable()
//        {
//            @Override
//            public void run()
//            {
//                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
//                Date date1 = new Date();
//                String currentDate = formatter.format(date1);
//                date.setText("Date :  " +currentDate);
//
//                SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
//                Date time1 = new Date();
//                String currentTime = timeFormatter.format(time1);
//                time.setText("Time :  " +currentTime);
//
//                someHandler.postDelayed(this, 1000);
//            }
//        }, 10);


        //Get Data From Database for Modification Task And set to the ListView
        if (InternetConnectivity.isConnected()) {
            appExecutors.getNetworkIO().execute(() -> {
                getSendModificationTasks(getUserId(),"unapproved").thenAccept(tasks -> {
                    senderDataModels = tasks;
                    adapter = new TaskAllocatedListCustomAdapter(senderDataModels,getActivity().getApplicationContext());
                    senderApprovalCompletionTaskList.setAdapter(adapter);
                }).exceptionally(e -> {
                   Toast.makeText(getContext().getApplicationContext(),"Failed to fetch outgoing tasks due to "+ e,Toast.LENGTH_LONG).show();
                    return null;
                });
            });
            appExecutors.getNetworkIO().execute(() -> {
                getReceiveModificationTasks(getUserId(),"unapproved").thenAccept(tasks -> {
                    receiverDataModels = tasks;
                    adapter = new TaskAllocatedListCustomAdapter(receiverDataModels,getActivity().getApplicationContext());
                    receiverApprovalCompletionTaskList.setAdapter(adapter);
                }).exceptionally(e -> {
                    Toast.makeText(getContext().getApplicationContext(),"Failed to fetch outgoing tasks due to "+ e,Toast.LENGTH_LONG).show();
                    return null;
                });
            });


        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        senderApprovalCompletionTaskList.setOnItemClickListener((parent, view1, position, id) -> {

            if(senderDataModels == null ){
                Log.e("TTSTaskModificationListFragment", "Data not loaded yet");
                return;
            }
            TaskDataModel dataModel= senderDataModels.get(position);
            appExecutors.getNetworkIO().execute(() -> {


                getAllocatedMeasurableList(dataModel.getId()).thenAccept(measurables -> {
                    Intent i = new Intent(getActivity(), TTSTaskDelegateListItemDetailsActivity.class);


                    i.putExtra("senderTaskApprovalItemDetails",dataModel);
                    i.putExtra("senderTaskApprovalMeasurableList", measurables);
                    startActivity(i);
                }).exceptionally(e -> {
                    Toast.makeText(getContext().getApplicationContext(),"Failed to fetch measurables  due to "+ e,Toast.LENGTH_LONG).show();
                    return  null;
                });

            });

        });

        receiverApprovalCompletionTaskList.setOnItemClickListener((parent, view1, position, id) -> {
            if(receiverDataModels == null){
                Log.e("TTSTaskModificationListFragment", "Data not loaded yet");
                return;
            }
            TaskDataModel dataModel= receiverDataModels.get(position);


            appExecutors.getNetworkIO().execute(() -> {


                getAllocatedMeasurableList(dataModel.getId()).thenAccept(measurables -> {
                    Intent i = new Intent(getActivity(), TTSTaskDelegateListItemDetailsActivity.class);


                    i.putExtra("receiverTaskApprovalItemDetails",dataModel);
                    i.putExtra("receiverTaskApprovalMeasurableList", measurables);
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



    public CompletableFuture<ArrayList<TaskDataModel>> getSendModificationTasks
            (String receivedUsername, String status){
        CompletableFuture<ArrayList<TaskDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandler.getTasksByTaskReceiveUsernameAndStatus(
                receivedUsername,status);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                ArrayList<TaskDataModel> tasks = new ArrayList<>();
                TaskDataModel task;
                try{
                    APIResponse apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof APISuccessResponse){
                        JsonArray bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse)
                                .getBody().getBody().getAsJsonArray();
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
                            task.setExpectedDate(taskObj.get("expectedDate").getAsString()
                                    .split("T")[0]);
//                            task.setExpectedTotalTime(taskObj.get("expectedTotalTime")
//                                    .getAsString());
                            task.setDescription(taskObj.get("description").getAsString());
                            task.setActualTotalTime(taskObj.get("actualTotalTime").getAsString());
                            task.setDeligationDateTime(taskObj.get("taskAssignedOn").getAsString());
                            task.setSeenOn(taskObj.get("taskSeenOn").getAsString());
                            task.setAcceptedOn(taskObj.get("taskAcceptedOn").getAsString());
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

    public CompletableFuture<ArrayList<TaskDataModel>> getReceiveModificationTasks(String taskOwner, String status){
        CompletableFuture<ArrayList<TaskDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandler.getTasksByTaskOwnerUsernameAndStatus(taskOwner,status);
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
                            task.setTaskReceivedUserId(taskReceivedUserID.get("username").getAsString());
                            JsonObject taskOwnerUserId = taskObj.get("taskOwnerUserID").getAsJsonObject();
                            task.setTaskDeligateOwnerUserID(taskOwnerUserId.get("username").getAsString());
                            task.setActivityName(taskObj.get("activityName").getAsString());
                            task.setTaskName(taskObj.get("taskName").getAsString());
                            task.setProjectNo(taskObj.get("projectCode").getAsString());
                            task.setProjectName(taskObj.get("projectName").getAsString());
                            task.setExpectedDate(taskObj.get("expectedDate").getAsString().split("T")[0]);
                          //  task.setExpectedTotalTime(taskObj.get("expectedTotalTime").getAsString());
                            task.setDescription(taskObj.get("description").getAsString());
                            task.setActualTotalTime(taskObj.get("actualTotalTime").getAsString());
                            task.setDeligationDateTime(taskObj.get("taskAssignedOn").getAsString());
                            task.setSeenOn(taskObj.get("taskSeenOn").getAsString());
                            task.setAcceptedOn(taskObj.get("taskAcceptedOn").getAsString());
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

      public CompletableFuture<ArrayList<MeasurableListDataModel>> getAllocatedMeasurableList(Long taskId){
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
