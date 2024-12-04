package com.example.neptune.ttsapp;


import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import jxl.Sheet;
import jxl.SheetSettings;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.biff.AutoFilter;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TTSTaskCompletedListFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    TaskHandlerInterface taskHandlerInterface;

    @Inject
    MeasurableServiceInterface measurableService;

    private SessionManager sessionManager;

    private ArrayList<TaskDataModel> dataModels;

    private ListView listView;

    private TextView user,date,time;
    private String userId;

    private static TaskAllocatedListCustomAdapter adapter;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ttstask_completed_list, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        listView=(ListView)view.findViewById(R.id.listCompleted);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=(TextView)view.findViewById(R.id.textViewCompletedListUser);
        user.setText(userId);

        date=(TextView)view.findViewById(R.id.textViewCompletedListDate);
        time=(TextView)view.findViewById(R.id.textViewCompletedListTime);

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
                getCompletedTasks(getUserId(),"completed").thenAccept(tasks -> {
                    dataModels = tasks;
                    adapter = new TaskAllocatedListCustomAdapter(dataModels,getActivity().getApplicationContext());
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
            TaskDataModel dataModel= dataModels.get(position);



            appExecutors.getNetworkIO().execute(()-> {
                getCompletedMeasurableList(dataModel.getId()).thenAccept(measurables -> {
                    appExecutors.getMainThread().execute(() -> {
                        Intent i = new Intent(getActivity(), TTSTaskAllocatedListItemDetailsActivity.class);
                        i.putExtra("TaskCompletedListItemDetails",dataModel);
                        i.putExtra("TaskCompletedListMeasurableList",measurables);
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


    public CompletableFuture<ArrayList<MeasurableListDataModel>> getCompletedMeasurableList(Long taskId){
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





    public CompletableFuture<ArrayList<TaskDataModel>> getCompletedTasks(String receivedUsername, String status){
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
                            //        task.setExpectedTotalTime(taskObj.get("expectedTotalTime").getAsString());
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
