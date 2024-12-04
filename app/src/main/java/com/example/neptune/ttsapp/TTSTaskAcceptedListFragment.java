package com.example.neptune.ttsapp;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
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
public class TTSTaskAcceptedListFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    @Inject
    TaskHandlerInterface taskHandlerInterface;

    public TTSTaskAcceptedListFragment() { }

    private SessionManager sessionManager;

    private ArrayList<TaskDataModel> dataModels;

    private ListView listView;

    private TextView user,date,time;
    private String userId;

    private static TaskAllocatedListCustomAdapter adapter;

    boolean result=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.fragment_ttstask_accepted_list, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        listView=view.findViewById(R.id.listAccepted);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=view.findViewById(R.id.textViewAcceptedListUser);
        user.setText(userId);

        date=view.findViewById(R.id.textViewAcceptedListDate);
        time=view.findViewById(R.id.textViewAcceptedListTime);

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
                getAcceptedTask(getUserId(),"accepted").thenAccept(result -> {
                    dataModels = result;
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

//        //Get Data From Database for Accepted Task And set to the ListView
//        if (InternetConnectivity.isConnected()) {
//        dataModels = getAcceptedTaskList(getUserId(),"ACCEPTED");
//        adapter= new TaskAllocatedListCustomAdapter(dataModels,getActivity().getApplicationContext());
//        listView.setAdapter(adapter);
//        }else {
//            Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
//        }


        listView.setOnItemClickListener((parent, view1, position, id) -> {

            TaskDataModel dataModel= dataModels.get(position);

            Intent i = new Intent(getActivity(), TTSTaskDelegateListItemDetailsActivity.class);

            i.putExtra("TaskAcceptedItemDetails",dataModel);

            startActivity(i);

        });

//        listView.setOnItemLongClickListener((parent, view12, position, id) -> {
//            dataModel= dataModels.get(position);
//            new AlertDialog.Builder(getActivity())
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setTitle("Task Complete")
//                    .setMessage(Html.fromHtml("<b>"+"Do You Want Complete The Task..?"+"</b>"))
//                    .setPositiveButton("Yes", (dialog, which) -> {
//                        result = updateCompletedStatus(dataModel.id);
//                        if (result)
//                        {
//                            Toast.makeText(getActivity().getApplicationContext(), "Task Completed", Toast.LENGTH_LONG).show();
//                        }
//
//                    })
//                    .setNegativeButton("No", null)
//                    .show();
//
//            return true;
//
//        });


         return view;
    }

    private String getUserId()
    {
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        return sessionManager.getUserID();
    }


    // Update Task status as COMPLETED
    public boolean updateCompletedStatus(Long taskId){
        Connection con;
        int x = 0;
        boolean result=false;

        try {
            con = DatabaseHelper.getDBConnection();

            Calendar calendar = Calendar.getInstance();
            Timestamp completeTimestamp = new Timestamp(calendar.getTime().getTime());

            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET STATUS =?,COMPLETION_ON=? WHERE ID = ?");

            ps.setString(1, "COMPLETED");
            ps.setString(2, completeTimestamp.toString());
            ps.setLong(3,taskId);
            x=ps.executeUpdate();

            if(x==1){
                result = true;
            }

            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }


    public CompletableFuture<ArrayList<TaskDataModel>> getAcceptedTask(String receivedUsername, String status){
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

    // Getting Accepted Task List
//    public ArrayList <TaskDataModel> getAcceptedTaskList(String receivedUserID, String status){
//
//        ArrayList<TaskDataModel> taskList = new ArrayList();
//        TaskDataModel listDataModel;
//        Connection con;
//
//        try {
//            con = DatabaseHelper.getDBConnection();
//
//            PreparedStatement ps = con.prepareStatement("select * from TASK_MANAGEMENT where FK_AUTHENTICATION_RECEIVED_USER_ID=? and STATUS=?");
//            ps.setString(1, receivedUserID);
//            ps.setString(2,status);
//
//
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//
//                listDataModel = new TaskDataModel();
//
//
//                listDataModel.setId(rs.getLong("ID"));
//                listDataModel.setTaskDeligateOwnerUserID(rs.getString("FK_AUTHENTICATION_OWNER_USER_ID"));
//                listDataModel.setActivityName(rs.getString("ACTIVITY_NAME"));
//                listDataModel.setTaskName(rs.getString("TASK_NAME"));
//                listDataModel.setProjectNo(rs.getString("PROJECT_ID"));
//                listDataModel.setProjectName(rs.getString("PROJECT_NAME"));
//                listDataModel.setExpectedDate(rs.getString("EXPECTED_DATE"));
//                listDataModel.setExpectedTotalTime(rs.getString("EXPECTED_TOTAL_TIME"));
//                listDataModel.setDescription(rs.getString("DESCRIPTION"));
//                listDataModel.setActualTotalTime(rs.getString("ACTUAL_TOTAL_TIME"));
//                listDataModel.setDeligationDateTime(rs.getTimestamp("DELEGATION_ON").toString());
//                listDataModel.setSeenOn(rs.getString("SEEN_ON"));
//                listDataModel.setAcceptedOn(rs.getString("ACCEPTED_ON"));
//                listDataModel.setStatus(rs.getString("STATUS"));
//
//
//                taskList.add(listDataModel);
//            }
//
//            rs.close();
//            ps.close();
//            con.close();
//        } catch (Exception e) { e.printStackTrace(); }
//
//        return taskList;
//
//    }

}
