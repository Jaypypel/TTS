package com.example.neptune.ttsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;

import android.view.View;
import android.view.ViewGroup;


import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.EnumStatus.Status;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

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
public class TTSTaskAllocatedListFragment extends Fragment {
    Status notSeen = Status.Not_Seen;

    @Inject
    AppExecutors appExecutors;

    @Inject
    TaskHandlerInterface taskHandlerInterface;

    @Inject
    MeasurableServiceInterface measurableService;

    public TTSTaskAllocatedListFragment() { }

    private SessionManager sessionManager;

    private ArrayList<TaskDataModel> tasks;

    private ListView listView;
    private TextView user,date,time;

    private static TaskAllocatedListCustomAdapter adapter;

    private String userId;

//    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ttstask_allocated_list, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        listView=view.findViewById(R.id.list);
        


        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=view.findViewById(R.id.textViewAllocatedListUser);
        user.setText(userId);

        date=view.findViewById(R.id.textViewAllocatedListDate);
        time=view.findViewById(R.id.textViewAllocatedListTime);

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
        if (InternetConnectivity.isConnected()){
        appExecutors.getNetworkIO().execute(() -> {
           getAssignedTask(getUserId(),"Pending").thenAccept(result -> {
               tasks = result;
               adapter = new TaskAllocatedListCustomAdapter(tasks,getActivity().getApplicationContext());
               listView.setAdapter(adapter);
           }).exceptionally( e -> {
               Log.e("Error", "Failed to get Tasks ");
               return null;
           });
        });
        } else {
            Toast.makeText(getActivity().getApplicationContext(),"No Internet Connection", Toast.LENGTH_LONG).show();
        }



        listView.setOnItemClickListener((parent, view1, position, id) -> {

            TaskDataModel dataModel= tasks.get(position);

            if (dataModel.getSeenOn().equals(notSeen.name())){
                updateTaskManagementSeenTime(dataModel.getId());
            }
            appExecutors.getNetworkIO().execute(()-> {
                getAllocatedMeasurableList(dataModel.getId()).thenAccept(measurables -> {
                    appExecutors.getMainThread().execute(() -> {
                        Intent i = new Intent(getActivity(), TTSTaskAllocatedListItemDetailsActivity.class);
                        i.putExtra("TaskAllocatedListItemDetails",dataModel);
                        i.putExtra("TaskAllocatedListMeasurableList",measurables);
                        startActivity(i);
                    });
                }).exceptionally(e -> {
                    Log.e("Error", "Failed to get Tasks " );
                    Toast.makeText(getActivity().getApplicationContext(),"Failed to get Tasks", Toast.LENGTH_LONG).show();
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




    public CompletableFuture<String> updateTaskManagementSeenTime(Long taskId){
        CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandlerInterface.updateSeenTimeTaskManagement(taskId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof  APISuccessResponse){
                        String msg =((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                        future.complete(msg);
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

            }
        });
        return future;
    }
    // Update Task Seen TimeStamp
//    public void seenUpdateTimestamp(Long id){
//        Connection con;
//        try {
//            con = DatabaseHelper.getDBConnection();
//
//            Calendar calendar = Calendar.getInstance();
//            Timestamp seenTimestamp = new Timestamp(calendar.getTime().getTime());
//
//            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET SEEN_ON = ? WHERE ID = ?");
//            ps.setString(1, seenTimestamp.toString());
//            ps.setLong(2,id);
//            ps.executeUpdate();
//
//            ps.close();
//            con.close();
//        }
//        catch(Exception e){
//            e.printStackTrace();
//        }
//    }


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
                    if(apiResponse instanceof  APISuccessResponse){
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


    public CompletableFuture<ArrayList<TaskDataModel>> getAssignedTask(String receivedUsername,String status){
        CompletableFuture<ArrayList<TaskDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandlerInterface.getTasksByTaskReceiveUsernameAndStatus(receivedUsername,status);
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

                            task.setExpectedTotalTime(taskObj.get("expectedTime").getAsString());
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
}
