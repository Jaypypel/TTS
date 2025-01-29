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
import com.example.neptune.ttsapp.Network.MeasurableServiceInterface;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;
import com.example.neptune.ttsapp.Util.DateConverter;
import com.example.neptune.ttsapp.Util.Debounce;
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

    @Inject
    MeasurableServiceInterface measurableService;

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
        userId = sessionManager.getToken();
        user=view.findViewById(R.id.textViewAcceptedListUser);
        user.setText(userId);

        date=view.findViewById(R.id.textViewAcceptedListDate);
        time=view.findViewById(R.id.textViewAcceptedListTime);

           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });

        if (InternetConnectivity.isConnected()){
            appExecutors.getNetworkIO().execute(() -> {
                getAcceptedTask(getToken(),"accepted").thenAccept(result -> {
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


        listView.setOnItemClickListener((parent, v, position, id) -> Debounce.debounceEffect(() -> {
                TaskDataModel dataModel= dataModels.get(position);
                getAllocatedMeasurableList(dataModel.getId()).thenAccept(measurables -> {
                appExecutors.getMainThread().execute(() -> {
                Intent i = new Intent(getActivity(), TTSTaskDelegateListItemDetailsActivity.class);
                i.putExtra("acceptedTasks",dataModel);
                i.putExtra("acceptedTaskMeasurables",measurables);
                startActivity(i);
            });
        }).exceptionally(e -> {
            Toast.makeText(getActivity().getApplicationContext(),"Failed to get the  accepted Tasks", Toast.LENGTH_LONG).show();
            return  null;
        });
        }));

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

    private String getToken()
    {
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        return sessionManager.getToken();
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

                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse)
                                .getBody().getBody();
                        Gson gson = new Gson();
                        Type measurableType = new TypeToken<ArrayList<MeasurableListDataModel>>(){}.getType();
                        if (bodyContent.isJsonArray()){
                            JsonArray content = bodyContent.getAsJsonArray();
                            ArrayList<MeasurableListDataModel> measurables = gson.fromJson(content,measurableType);
                            future.complete(measurables);
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
                    future.completeExceptionally(new Throwable("Exception occured while getting measurables due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
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


    public CompletableFuture<ArrayList<TaskDataModel>> getAcceptedTask(String receivedUsername, String status){
        CompletableFuture<ArrayList<TaskDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandlerInterface.getTasksByTaskReceiveUsernameAndStatus(receivedUsername,status);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                try{
                    APIResponse apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof APISuccessResponse){

                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse)
                                .getBody().getBody();
                        Gson gson = new Gson();
                        Type taskType = new TypeToken<ArrayList<TaskDataModel>>(){}.getType();
                        if (bodyContent.isJsonArray()){
                            JsonArray content = bodyContent.getAsJsonArray();
                            ArrayList<TaskDataModel> tasks = gson.fromJson(content,taskType);
                            future.complete(tasks);
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
                    future.completeExceptionally(new Throwable("Exception occured while getting  tasks due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                future.completeExceptionally(new Throwable(t.getMessage()));
                Log.e("Error", "Request Failed: " + t.getMessage(), t);
            }
        });

        return future;
    }


}
