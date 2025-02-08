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

    private TextView allocatedTaskState;

    private String userId;
    private long lastTimeClick = 0;
//    SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ttstask_allocated_list, container, false);
        listView=view.findViewById(R.id.list);
        


        sessionManager = new SessionManager(requireContext());
        userId = sessionManager.getToken();
        user=view.findViewById(R.id.textViewAllocatedListUser);
        user.setText(userId);

        date=view.findViewById(R.id.textViewAllocatedListDate);
        time=view.findViewById(R.id.textViewAllocatedListTime);
        allocatedTaskState = view.findViewById(R.id.receivedTasksState);


           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });
        if (InternetConnectivity.isConnected()){
        appExecutors.getNetworkIO().execute(() -> {

           getAssignedTask(getToken(),"Pending").thenAccept(result -> {
               allocatedTaskState.setVisibility(View.INVISIBLE);
               appExecutors.getMainThread().execute(() -> {
                   tasks = result;
                   adapter = new TaskAllocatedListCustomAdapter(tasks,requireContext());
                   listView.setAdapter(adapter);
                   if(tasks == null || tasks.isEmpty()){
                       allocatedTaskState.setVisibility(View.VISIBLE);
                   }
               });
           }).exceptionally( e -> {
               allocatedTaskState.setVisibility(View.VISIBLE);
               allocatedTaskState.setText("failed to get received tasks due to error " +e.getMessage());
               return null;
           });
        });
        } else {
            Toast.makeText(requireContext(),"No Internet Connection", Toast.LENGTH_LONG).show();
        }



        listView.setOnItemClickListener((parent, view1, position, id) -> Debounce
                .debounceEffect(() -> {
            TaskDataModel dataModel= tasks.get(position);
        if (dataModel.getTaskSeenOn().equals(notSeen.name())){
            updateTaskManagementSeenTime(dataModel.getId());
        }
        appExecutors
                .getNetworkIO()
                .execute(()-> getAllocatedMeasurableList(dataModel.getId())
                        .thenAccept(measurables -> appExecutors
                                .getMainThread()
                                .execute(() -> {
                                    Intent i = new Intent(requireContext(), TTSTaskAllocatedListItemDetailsActivity.class);
                                    i.putExtra("TaskAllocatedListItemDetails",dataModel);
                                    i.putExtra("TaskAllocatedListMeasurableList",measurables);
                                    startActivity(i);
                                })).exceptionally(e -> {
                            Log.e("Error", "Failed to get Tasks " );
                            Toast.makeText(requireContext(),"Failed to get Tasks", Toast.LENGTH_LONG).show();
                            return  null;
                        }));
        }));
        return view;
    }




    private String getToken()
    {
        sessionManager = new SessionManager(requireContext());
        return sessionManager.getToken();
    }



    // Update Task Seen TimeStamp
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

   public CompletableFuture<ArrayList<MeasurableListDataModel>> getAllocatedMeasurableList(Long taskId){
        CompletableFuture<ArrayList<MeasurableListDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = measurableService.getAllocatedMeasurableList(taskId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                MeasurableListDataModel measurable;
                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof  APISuccessResponse){
                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type measurableType = new TypeToken<ArrayList<MeasurableListDataModel>>(){}.getType();
                        if (bodyContent.isJsonArray()){
                            JsonArray content = bodyContent.getAsJsonArray();
                            ArrayList<MeasurableListDataModel> measurables = gson
                                    .fromJson(content,measurableType);
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
                future.completeExceptionally(new Throwable(t.getMessage()));
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
//                ArrayList<TaskDataModel> tasks = new ArrayList<>();
//                TaskDataModel task;
                try{
                    APIResponse apiResponse = APIResponse.create(response);
                    if(apiResponse instanceof APISuccessResponse){
                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody().getAsJsonArray();
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
                    future.completeExceptionally(new Throwable("Exception occured while getting no. of completed tasks due to" + e.getMessage()));
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
