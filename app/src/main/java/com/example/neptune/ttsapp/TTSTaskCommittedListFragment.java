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
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;
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
public class TTSTaskCommittedListFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    TaskHandlerInterface taskHandler;

    public TTSTaskCommittedListFragment() { }

    private SessionManager sessionManager;

    private ArrayList<TaskDataModel> dataModels;

    private ListView listView;

    private TextView user,date,time;
    private String userId;

    private static TaskAllocatedListCustomAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttstask_committed_list, container, false);

        listView=view.findViewById(R.id.processingTaskList);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=view.findViewById(R.id.textViewProcessingListUser);
        user.setText(userId);

        date=view.findViewById(R.id.textViewProcessingListDate);
        time=view.findViewById(R.id.textViewProcessingListTime);

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



        //Get Data From Database for Processing Task And set to the ListView
        if (InternetConnectivity.isConnected()) {

            appExecutors.getNetworkIO().execute(() -> {
                getProcessingTasks(getUserId(),"In-Process").thenAccept(tasks -> {
                    dataModels = tasks;
                    adapter = new TaskAllocatedListCustomAdapter(dataModels,getActivity().getApplicationContext());
                    listView.setAdapter(adapter);
                }).exceptionally(e-> {
                    Toast.makeText(getActivity().getApplicationContext(),"Failed to fetch tasks",Toast.LENGTH_LONG).show();
                    return null;
                });
            });

        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

        listView.setOnItemClickListener((parent, view1, position, id) -> {

            TaskDataModel dataModel= dataModels.get(position);
            Intent i = new Intent(getActivity(), TTSTaskDelegateListItemDetailsActivity.class);

            i.putExtra("TaskProcessingItemDetails",dataModel);
            i.putExtra("TaskProcessingMeasurableDetails",getProcessingTaskMeasurableList(dataModel.getId()));

            startActivity(i);


        });

        return view;
    }

    private String getUserId()
    {
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        return sessionManager.getUserID();
    }

    public CompletableFuture<ArrayList<TaskDataModel>> getProcessingTasks(String receivedUsername, String status){
        CompletableFuture<ArrayList<TaskDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandler.getTasksByTaskOwnerUsernameAndStatus(receivedUsername,status);
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
                            JsonObject usr = taskObj.get("taskOwnerUserID").getAsJsonObject();
                            task.setTaskDeligateOwnerUserID(usr.get("username").getAsString());
                            task.setActivityName(taskObj.get("activityName").getAsString());
                            task.setTaskName(taskObj.get("taskName").getAsString());
                            task.setProjectNo(taskObj.get("projectCode").getAsString());
                            task.setProjectName(taskObj.get("projectName").getAsString());
                            task.setExpectedDate(taskObj.get("expectedDate").getAsString().split("T")[0]);
                            task.setExpectedTotalTime(taskObj.get("expectedTotalTime").getAsString());
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



    // Getting Measurables List
    public ArrayList<MeasurableListDataModel> getProcessingTaskMeasurableList(Long taskId){

        ArrayList<MeasurableListDataModel> measurableList = new ArrayList();
        MeasurableListDataModel measurableListDataModel;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select m.ID,m.NAME from MEASURABLES m where ID = ANY(select FK_MEASURABLE_ID from DELEGATION_MEASURABLES where FK_TASK_MANAGEMENT_ID = ?)");
            ps.setLong(1, taskId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                measurableListDataModel= new MeasurableListDataModel();

                measurableListDataModel.setId(rs.getString("ID"));
                measurableListDataModel.setMeasurableName(rs.getString("NAME"));

                measurableList.add(measurableListDataModel);


            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return measurableList;

    }

}
