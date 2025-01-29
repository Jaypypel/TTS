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
    MeasurableServiceInterface measurableService;

    @Inject
    TaskHandlerInterface taskHandler;

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
        userId = sessionManager.getToken();
        user=view.findViewById(R.id.textViewProcessingListUser);
        user.setText(userId);

        date=view.findViewById(R.id.textViewProcessingListDate);
        time=view.findViewById(R.id.textViewProcessingListTime);

           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });



        //Get Data From Database for Processing Task And set to the ListView
        if (InternetConnectivity.isConnected()) {

            appExecutors.getNetworkIO().execute(() -> {
                getProcessingTasks(getToken(),"In_Process").thenAccept(tasks -> {
                    dataModels = tasks;
                    adapter = new TaskAllocatedListCustomAdapter(dataModels,getActivity().getApplicationContext());
                    listView.setAdapter(adapter);
                }).exceptionally(e-> {
                    Toast.makeText(getActivity().getApplicationContext(),"Failed to fetch tasks",Toast.LENGTH_LONG).show();
                    return null;
                });
            });

        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

        listView.setOnItemClickListener((parent, view1, position, id) -> Debounce.debounceEffect(() -> {
            TaskDataModel dataModel= dataModels.get(position);
            getAllocatedMeasurableList(dataModel.getId()).thenAccept(measurables -> {
                appExecutors.getMainThread().execute(() -> {
                    Intent i = new Intent(getActivity(), TTSTaskDelegateListItemDetailsActivity.class);
                    i.putExtra("TaskProcessingItemDetails",dataModel);
                    i.putExtra("TaskProcessingMeasurableDetails",measurables);
                    startActivity(i);
                });
            }).exceptionally(e -> {
                Log.e("Error", "Failed to get Tasks " );
                Toast.makeText(getActivity().getApplicationContext(),"Failed to get the  accepted Tasks", Toast.LENGTH_LONG).show();
                return  null;
            });
        }));
        return view;
    }

    private String getToken()
    {
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        return sessionManager.getToken();
    }

    public CompletableFuture<ArrayList<TaskDataModel>> getProcessingTasks(String receivedUsername, String status){
        CompletableFuture<ArrayList<TaskDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandler.getTasksByTaskReceiveUsernameAndStatus(receivedUsername,status);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {

                TaskDataModel task;
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
      //                  return;
//                        for (JsonElement item: bodyContent
//                        ) {
//                            JsonObject taskObj = item.getAsJsonObject();
//                            task = new TaskDataModel();
//                            task.setId(taskObj.get("id").getAsLong());
//                            JsonObject taskOwnerObj = taskObj.get("taskOwnerUserID").getAsJsonObject();
//                            JsonObject taskRecevierObj = taskObj.get("taskReceivedUserID").getAsJsonObject();
//                            task.setTaskReceivedUserId(taskRecevierObj.get("username").getAsString());
//                            task.setTaskDeligateOwnerUserID(taskOwnerObj.get("username").getAsString());
//                            task.setActivityName(taskObj.get("activityName").getAsString());
//                            task.setTaskName(taskObj.get("taskName").getAsString());
//                            task.setProjectNo(taskObj.get("projectCode").getAsString());
//                            task.setProjectName(taskObj.get("projectName").getAsString());
//                            task.setExpectedDate(taskObj.get("expectedDate").getAsString());
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
                    future.completeExceptionally(new Throwable("Exception occurred while getting In-Process tasks due to" + e.getMessage()));
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
             //   ArrayList<MeasurableListDataModel> measurables = new ArrayList<>();
               // MeasurableListDataModel measurable;
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
                    future.completeExceptionally(new Throwable("Exception occurred while getting measurables due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                future.completeExceptionally(new Throwable(t.getMessage()) );
            }
        });

        return future;
    }

    // Getting Measurables List
//    public ArrayList<MeasurableListDataModel> getProcessingTaskMeasurableList(Long taskId){
//
//        ArrayList<MeasurableListDataModel> measurableList = new ArrayList();
//        MeasurableListDataModel measurableListDataModel;
//
//        Connection con;
//        try {
//            con = DatabaseHelper.getDBConnection();
//
//            PreparedStatement ps = con.prepareStatement("select m.ID,m.NAME from MEASURABLES m where ID = ANY(select FK_MEASURABLE_ID from DELEGATION_MEASURABLES where FK_TASK_MANAGEMENT_ID = ?)");
//            ps.setLong(1, taskId);
//
//            ResultSet rs = ps.executeQuery();
//
//            while (rs.next()) {
//
//                measurableListDataModel= new MeasurableListDataModel();
//
//                measurableListDataModel.setId(rs.getString("ID"));
//                measurableListDataModel.setMeasurableName(rs.getString("NAME"));
//
//                measurableList.add(measurableListDataModel);
//
//
//            }
//            rs.close();
//            ps.close();
//            con.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return measurableList;
//
//    }

}
