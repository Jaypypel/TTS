package com.example.neptune.ttsapp;


import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.gson.reflect.TypeToken;


import java.io.IOException;
import java.lang.reflect.Type;

import java.util.ArrayList;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

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

    private TextView user,date,time;
    private String userId;
    
    private static TaskDelegatedListCustomAdapter taskDelegatedListCustomAdapter;

    private TextView completedTasksState;

    private RecyclerView recyclerView;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ttstask_completed_list, container, false);
        recyclerView = view.findViewById(R.id.recyclerTaskCompleted);
        sessionManager = new SessionManager(requireContext());
        userId = sessionManager.getUsername();
        user=view.findViewById(R.id.textViewCompletedListUser);
        user.setText(userId);

        date=view.findViewById(R.id.textViewCompletedListDate);
        time=view.findViewById(R.id.textViewCompletedListTime);
        completedTasksState = view.findViewById(R.id.completedTasksState);

        appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });
        if(recyclerView.getLayoutManager() == null) {recyclerView
                .setLayoutManager(new LinearLayoutManager(getContext()));}
        recyclerView.setHasFixedSize(true);
        if (InternetConnectivity.isConnected()){
            completedTasksState.setVisibility(View.INVISIBLE);
            appExecutors.getNetworkIO().
                    execute(() -> loadCompletedTasks(getUsername(),"Completed"));
        } else Toast.makeText(getContext(),"No Internet Connection", Toast.LENGTH_LONG).show();

        return view;
    }


    private void loadCompletedTasks(String username, String status){
        getCompletedTasks(username,status).thenAccept(tasks -> {
            dataModels = tasks;
            if(dataModels == null || dataModels.isEmpty()){
                completedTasksState.setVisibility(View.VISIBLE);
            }
            appExecutors.getMainThread().execute(() ->{
                taskDelegatedListCustomAdapter  = new TaskDelegatedListCustomAdapter(item -> Debounce.debounceEffect(() -> {
                    if(isLearner()){
                        Intent i = new Intent(getContext(), LearnerTaskCompletionActivity.class);
                        i.putExtra("id",item.id);
                        startActivity(i);
                    } else {
                        appExecutors.getNetworkIO().execute(()->loadMeasurables(item.id,item));
                    }
                }));

                recyclerView.setAdapter(taskDelegatedListCustomAdapter);
                taskDelegatedListCustomAdapter.submitList(tasks);}
            );
        }).exceptionally( e -> {
            completedTasksState.setVisibility(View.VISIBLE);
            completedTasksState.setText("failed to get Completed tasks due to error " +e.getMessage());
            showError(e);
            return null;
        });
    }

    private void showError(Throwable e){
        Toast.makeText(requireContext(), "Failure: "+e.getMessage(), Toast.LENGTH_LONG).show();
    }
    private void loadMeasurables(Long taskId,TaskDataModel taskDataModel){
        getCompletedMeasurableList(taskId)
                .thenAccept(measurables -> appExecutors
                        .getMainThread()
                        .execute(() -> moveAllocatedTaskDetails(taskDataModel,measurables)))
                        .exceptionally(e -> {showError(e); return  null;});
    }

    private void moveAllocatedTaskDetails(TaskDataModel taskDataModel, ArrayList<MeasurableListDataModel> measurables){
        Intent i = new Intent(getContext(), TTSTaskAllocatedListItemDetailsActivity.class);
        i.putExtra("TaskCompletedListItemDetails",taskDataModel);
        i.putExtra("TaskCompletedListMeasurableList",measurables);
        startActivity(i);
    }
    private boolean isLearner(){
        return sessionManager.getRoles().contains("ROLE_LEARNER");
    }
    private String getUsername()
    {   /*
            get session manager to get the token
        */
        sessionManager = new SessionManager(getContext());
        return sessionManager.getUsername();
    }


    public CompletableFuture<ArrayList<MeasurableListDataModel>> getCompletedMeasurableList(Long taskId){
        CompletableFuture<ArrayList<MeasurableListDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = measurableService.getAllocatedMeasurableList(taskId);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof APISuccessResponse) {
                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type measurablesType = new TypeToken<ArrayList<MeasurableListDataModel>>() {
                        }.getType();
                        if (bodyContent.isJsonArray()) {
                            JsonArray content = bodyContent.getAsJsonArray();
                            ArrayList<MeasurableListDataModel> measurables = gson.fromJson(content, measurablesType);
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
                } catch (ClassCastException e) {
                    future.completeExceptionally(new Throwable("Unable to cast the response into required format due to " + e.getMessage()));
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Throwable("Exception occurred while getting measurables due to" + e.getMessage()));
                } catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is " + e.getMessage() + " its cause " + e.getCause()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("Error", "Request Failed: " + t.getMessage(), t);
                future.completeExceptionally(new Throwable(t.getMessage()));
            }
        });

        return future;
    }





    public CompletableFuture<ArrayList<TaskDataModel>> getCompletedTasks(String receivedUsername, String status){
        CompletableFuture<ArrayList<TaskDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandlerInterface.getTasksByTaskReceiveUsernameAndStatus(receivedUsername,status);
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                try {
                    APIResponse apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof APISuccessResponse) {

                        JsonElement bodyContent = ((APISuccessResponse<ResponseBody>) apiResponse)
                                .getBody().getBody();
                        Gson gson = new Gson();
                        Type taskType = new TypeToken<ArrayList<TaskDataModel>>() {
                        }.getType();
                        if (bodyContent.isJsonArray()) {
                            JsonArray content = bodyContent.getAsJsonArray();
                            ArrayList<TaskDataModel> tasks = gson.fromJson(content, taskType);
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
                } catch (ClassCastException e) {
                    future.completeExceptionally(new Throwable("Unable to cast the response into required format due to " + e.getMessage()));
                } catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    future.completeExceptionally(new Throwable("Exception occurred while getting no. of completed tasks due to" + e.getMessage()));
                } catch (RuntimeException e) {
                    future.completeExceptionally(new Throwable("Unnoticed Exception occurred which is " + e.getMessage() + " its cause " + e.getCause()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t){
                future.completeExceptionally(new Throwable(t.getMessage()));
            }
        });

        return future;
    }
}
