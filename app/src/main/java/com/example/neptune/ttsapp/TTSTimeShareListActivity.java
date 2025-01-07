package com.example.neptune.ttsapp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.example.neptune.ttsapp.Network.APIEmptyResponse;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TimeShareServiceInterface;
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
public class TTSTimeShareListActivity extends AppCompatActivity {

    @Inject
    AppExecutors appExecutor;

    @Inject
    TimeShareServiceInterface timeShareService;

    private ArrayList<TimeShareDataModel> dataModels;

    private ListView listView;
    private Button gotoTimeshare;

    private static TimeShareListCustomAdapter adapter;

    private TaskDataModel taskAcceptedDetails, taskCompletedDetails, taskDelegatedDetails,
            taskProcessingDetails, taskSenderApprovalItemDetails, taskReceiverApprovalItemDetails;

    private Long taskId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttstime_share_list);

        listView =  findViewById(R.id.timeShareList);
        gotoTimeshare =  findViewById(R.id.buttonGotoTimeshare);

        //Get Data from clicking on Task Accepted Tab ListView
        taskAcceptedDetails = (TaskDataModel) getIntent().getSerializableExtra("TaskAcceptedItemDetails");

        //Get Data from clicking on Task Completed Tab ListView
        taskCompletedDetails = (TaskDataModel) getIntent().getSerializableExtra("TaskCompletedDetails");

        //Get Data from clicking on Task Delegated Tab ListView
        taskDelegatedDetails = (TaskDataModel) getIntent().getSerializableExtra("TaskDelegatedDetails");

        //Get Data from clicking on Task Processing Tab ListView
        taskProcessingDetails = (TaskDataModel) getIntent().getSerializableExtra("TaskProcessingDetails");

        //Get Data from clicking on ShowTimeshare button in Task Sender Approval
        taskSenderApprovalItemDetails = (TaskDataModel) getIntent().getSerializableExtra("TaskSenderApprovalDetails");

        //Get Data from clicking on ShowTimeshare button in Task Receiver Approval
        taskReceiverApprovalItemDetails = (TaskDataModel) getIntent().getSerializableExtra("TaskReceiverApprovalDetails");

        if (taskAcceptedDetails != null) {
            if (InternetConnectivity.isConnected()) {
                taskId = taskAcceptedDetails.getId();
                getTimeShares(taskId).thenAccept(timeShares -> {
                    dataModels = timeShares;
                    Log.e("TimeShares",""+dataModels);
                    adapter = new TimeShareListCustomAdapter(dataModels, getApplicationContext());
                    listView.setAdapter(adapter);

                }).exceptionally(e -> {
                    Log.e("Server Error", "Failed to get Timeshares due to " + e.getMessage());
                    return null;
                });

            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }


        } else if (taskCompletedDetails != null) {
            if (InternetConnectivity.isConnected()) {
                taskId = taskCompletedDetails.getId();
                getTimeShares(taskId).thenAccept(timeShares -> {

                    dataModels = timeShares;
                    Log.e("TimeShares",""+dataModels);
                    adapter = new TimeShareListCustomAdapter(dataModels, getApplicationContext());
                    listView.setAdapter(adapter);
                    gotoTimeshare.setVisibility(View.INVISIBLE);

                }).exceptionally(e -> {
                    Log.e("Server Error", "Failed to get Timeshares due to " + e.getMessage());
                    return null;
                });

            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        } else if (taskDelegatedDetails != null) {
            if (InternetConnectivity.isConnected()) {
                taskId = taskDelegatedDetails.getId();
                getTimeShares(taskId).thenAccept(timeShares -> {
                    dataModels = timeShares;
                    Log.e("TimeShares",""+dataModels);
                    adapter = new TimeShareListCustomAdapter(dataModels, getApplicationContext());
                    listView.setAdapter(adapter);
                    gotoTimeshare.setVisibility(View.INVISIBLE);

                }).exceptionally(e -> {
                    Log.e("Server Error", "Failed to get Timeshares due to " + e.getMessage());
                    return null;
                });


            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        } else if (taskProcessingDetails != null) {
            if (InternetConnectivity.isConnected()) {
                taskId = taskProcessingDetails.getId();
                getTimeShares(taskId).thenAccept(timeShares -> {


                    dataModels = timeShares;
                    adapter = new TimeShareListCustomAdapter(dataModels, getApplicationContext());
                    listView.setAdapter(adapter);
                }).exceptionally(e -> {
                    Log.e("Server Error", "Failed to get Timeshares due to " + e.getMessage());
                    return null;
                });

            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        } else if (taskSenderApprovalItemDetails != null) {
            if (InternetConnectivity.isConnected()) {
                taskId = taskSenderApprovalItemDetails.getId();
                getTimeShares(taskId).thenAccept(timeShares -> {
                    dataModels = timeShares;
                    Log.e("TimeShares",""+dataModels);
                    adapter = new TimeShareListCustomAdapter(dataModels, getApplicationContext());
                    listView.setAdapter(adapter);
                    gotoTimeshare.setVisibility(View.INVISIBLE);

                }).exceptionally(e -> {
                    Log.e("Server Error", "Failed to get Timeshares due to " + e.getMessage());
                    return null;
                });


            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        } else {
            if (InternetConnectivity.isConnected()) {
                taskId = taskReceiverApprovalItemDetails.getId();
                getTimeShares(taskId).thenAccept(timeShares -> {
                    dataModels = timeShares;
                    Log.e("TimeShares",""+dataModels);
                    adapter = new TimeShareListCustomAdapter(dataModels, getApplicationContext());
                    listView.setAdapter(adapter);
                    gotoTimeshare.setVisibility(View.INVISIBLE);

                }).exceptionally(e -> {
                    Log.e("Server Error", "Failed to get Timeshares due to " + e.getMessage());
                    return null;
                });

            } else {
                Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        }

        gotoTimeshare.setOnClickListener(v -> {
            if (taskAcceptedDetails != null) {
                Intent i = new Intent(getApplicationContext(), TTSTimeShareFormActivity.class);
                i.putExtra("TaskAcceptedDetails", taskAcceptedDetails);
                startActivity(i);
                finish();
            } else {
                Intent i = new Intent(getApplicationContext(), TTSTimeShareFormActivity.class);
                i.putExtra("TaskProcessingDetails", taskProcessingDetails);
                startActivity(i);
                finish();
            }
            // Code for finishing Accepted List
            TTSMainActivity.mainActivity.finish();

        });

    }

    @Override
    public void onBackPressed() {
        finish();
    }


    public CompletableFuture<ArrayList<TimeShareDataModel>> getTimeShares(Long taskId) {
        CompletableFuture<ArrayList<TimeShareDataModel>> future = new CompletableFuture<>();
        Call<ResponseBody> call = timeShareService.getTimeShares(taskId);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof APISuccessResponse) {
                        JsonElement body = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getBody();
                        Gson gson = new Gson();
                        Type timeShareType = new TypeToken<ArrayList<TimeShareDataModel>>() {
                        }.getType();
                        if (body.isJsonArray()) {
                            JsonArray content = body.getAsJsonArray();
                            ArrayList<TimeShareDataModel> timeShares = gson.fromJson(content, timeShareType);
                            future.complete(timeShares);
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
                    future.completeExceptionally(new Throwable("Exception occured while performing input output of measurables due to" + e.getMessage()));
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
