package com.example.neptune.ttsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.EnumStatus.Status;
import com.example.neptune.ttsapp.Network.APIEmptyResponse;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;
import com.example.neptune.ttsapp.Network.TimeShareServiceInterface;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TTSTaskDelegateListItemDetailsActivity extends AppCompatActivity {

    @Inject
    TaskHandlerInterface taskHandlerInterface;

    @Inject
    AppExecutors appExecutors;

    @Inject
    TimeShareServiceInterface timeShareService;
    Status completed = Status.Completed;
    Status approved = Status.Approved;
    Status unapproved = Status.Unapproved;
    Status inProcess = Status.In_Process;

    public TTSTaskDelegateListItemDetailsActivity() { }

    private TextView TDLIDDate,TDLIDActivityName,TDLIDTaskName,TDLIDProjCode,TDLIDProjName,
            TDLIDExpectedDate,TDLIDExpectedTime,TDLIDDescription,TDLIDUserName,
            TDLIDReceivedUserName,TDLIDMeasurableLabel;

    private Button TDLIDComplete,TDLIDDisplayTimeShares,TDLIDProcessing;

    private ListView TDLIDlistView;

    private TaskDataModel taskDelegateListItemDetails,taskAcceptedItemDetails,
            taskProcessingItemDetails,
            taskSenderApprovalItemDetails, taskReceiverApprovalItemDetails ;
    ArrayList<MeasurableListDataModel> delegatedMeasurableList,processingMeasurableList,
            senderApprovalMeasurableList,receiverApprovalMeasurableList,acceptedTaskMeasurales;

    private static MeasurableListCustomAdapter measurableListCustomAdapter;
    private SessionManager sessionManager;


        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_ttstask_delegated_list_item_details);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

        TDLIDDate=findViewById(R.id.textViewTDLIDDate);
        TDLIDUserName=findViewById(R.id.textViewTDLIDUser);
        TDLIDReceivedUserName =findViewById(R.id.textViewTDLIDReceivedUser);
        TDLIDActivityName=findViewById(R.id.textViewTDLIDActName);
        TDLIDTaskName=findViewById(R.id.textViewTDLIDTaskName);
        TDLIDProjCode=findViewById(R.id.textViewTDLIDProjNo);
        TDLIDProjName=findViewById(R.id.textViewTDLIDProjName);
        TDLIDExpectedDate=findViewById(R.id.textViewTDLIDExpDate);
        TDLIDExpectedTime=findViewById(R.id.textViewTDLIDExpTime);
        TDLIDDescription=findViewById(R.id.textViewTDLIDDescription);
        TDLIDlistView=findViewById(R.id.listMeasurableTDLID);
        TDLIDComplete =findViewById(R.id.buttonTDLIDComplete);
        TDLIDDisplayTimeShares =findViewById(R.id.buttonTDLIDDisplayTimeShares);
        TDLIDProcessing =findViewById(R.id.buttonTDLIDProcessing);

        TDLIDMeasurableLabel=findViewById(R.id.textViewTDLIDMeasurableLabel);

            // Getting Details From Delegated Task
            taskDelegateListItemDetails  = (TaskDataModel) getIntent()
                    .getSerializableExtra("TaskDelegatedItemDetails");
            delegatedMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent()
                    .getSerializableExtra("TaskDelegatedMeasurableList");
            Log.e("taskDelegateListItemDetails","-"+taskDelegateListItemDetails);

            // Getting Details From Accepted Task
            taskAcceptedItemDetails =  (TaskDataModel) getIntent()
                    .getSerializableExtra("acceptedTasks");
            acceptedTaskMeasurales =  (ArrayList<MeasurableListDataModel>) getIntent()
                    .getSerializableExtra("acceptedTaskMeasurables");

            Log.e("taskAcceptedItemDetails","-"+taskAcceptedItemDetails);
            // Getting Details From Processing Task
            taskProcessingItemDetails = (TaskDataModel) getIntent()
                    .getSerializableExtra("TaskProcessingItemDetails");
            processingMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent()
                    .getSerializableExtra("TaskProcessingMeasurableDetails");
            Log.e("taskProcessingItemDetails","-"+taskProcessingItemDetails);
            Log.e("processingMeasurableList","-"+processingMeasurableList);

            // Getting Details From Sender Approval Task
            taskSenderApprovalItemDetails = (TaskDataModel) getIntent()
                    .getSerializableExtra("senderTaskApprovalItemDetails");
            senderApprovalMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent()
                    .getSerializableExtra("senderTaskApprovalMeasurableList");
            Log.e("taskSenderApprovalItemDetails","-"+taskSenderApprovalItemDetails);

            // Getting Details From Receiver Approval Task
            taskReceiverApprovalItemDetails = (TaskDataModel) getIntent()
                    .getSerializableExtra("receiverTaskApprovalItemDetails");
            receiverApprovalMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent()
                    .getSerializableExtra("receiverTaskApprovalMeasurableList");
            Log.e("taskReceiverApprovalItemDetails","-"+taskReceiverApprovalItemDetails);

        if(taskDelegateListItemDetails!=null)
        {
            TDLIDDate.setText(taskDelegateListItemDetails.getDeligationDateTime());
            TDLIDUserName.setText(taskDelegateListItemDetails.getTaskDeligateOwnerUserID());
            TDLIDReceivedUserName.setText("To,  " + taskDelegateListItemDetails.getTaskReceivedUserId());
            TDLIDActivityName.setText(taskDelegateListItemDetails.getActivityName());
            TDLIDTaskName.setText(taskDelegateListItemDetails.getTaskName());
            TDLIDProjCode.setText(taskDelegateListItemDetails.getProjectNo());
            TDLIDProjName.setText(taskDelegateListItemDetails.getProjectName());
            TDLIDExpectedDate.setText(taskDelegateListItemDetails.getExpectedDate());
            TDLIDExpectedTime.setText(taskDelegateListItemDetails.getExpectedTotalTime());
            TDLIDDescription.setText(taskDelegateListItemDetails.getDescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(delegatedMeasurableList, getApplicationContext());
            TDLIDlistView.setAdapter(measurableListCustomAdapter);

            TDLIDProcessing.setVisibility(View.INVISIBLE);

            if (taskDelegateListItemDetails.getStatus().equals("")) { TDLIDComplete.setVisibility(View.INVISIBLE); }

        }
        else if (taskAcceptedItemDetails!=null)
        {
            TDLIDDate.setText(taskAcceptedItemDetails.getDeligationDateTime());
            TDLIDUserName.setText(taskAcceptedItemDetails.getTaskReceivedUserId());
            TDLIDReceivedUserName.setText("From,  " + taskAcceptedItemDetails.getTaskDeligateOwnerUserID());
            TDLIDActivityName.setText(taskAcceptedItemDetails.getActivityName());
            TDLIDTaskName.setText(taskAcceptedItemDetails.getTaskName());
            TDLIDProjCode.setText(taskAcceptedItemDetails.getProjectNo());
            TDLIDProjName.setText(taskAcceptedItemDetails.getProjectName());
            TDLIDExpectedDate.setText(taskAcceptedItemDetails.getExpectedDate());
            TDLIDExpectedTime.setText(taskAcceptedItemDetails.getExpectedTotalTime());
            TDLIDDescription.setText(taskAcceptedItemDetails.getDescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(acceptedTaskMeasurales,getApplicationContext());
            TDLIDlistView.setAdapter(measurableListCustomAdapter);
            TDLIDMeasurableLabel.setVisibility(View.INVISIBLE);
            TDLIDlistView.setVisibility(View.INVISIBLE);
            TDLIDComplete.setVisibility(View.INVISIBLE);
//            TDLIDDisplayTimeShares.setVisibility(View.INVISIBLE);
        }
        else if (taskProcessingItemDetails!=null)
         {
            TDLIDDate.setText(taskProcessingItemDetails.getDeligationDateTime());
            TDLIDUserName.setText("To, "+taskProcessingItemDetails.getTaskReceivedUserId());
            TDLIDReceivedUserName.setText("From,  " + taskProcessingItemDetails.getTaskDeligateOwnerUserID());
            TDLIDActivityName.setText(taskProcessingItemDetails.getActivityName());
            TDLIDTaskName.setText(taskProcessingItemDetails.getTaskName());
            TDLIDProjCode.setText(taskProcessingItemDetails.getProjectNo());
            TDLIDProjName.setText(taskProcessingItemDetails.getProjectName());
            TDLIDExpectedDate.setText(taskProcessingItemDetails.getExpectedDate());
          //  TDLIDExpectedTime.setText(taskProcessingItemDetails.getExpectedTotalTime());
            TDLIDExpectedTime.setText(taskProcessingItemDetails.getActualTotalTime());
            TDLIDDescription.setText(taskProcessingItemDetails.getDescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(processingMeasurableList, getApplicationContext());
            TDLIDlistView.setAdapter(measurableListCustomAdapter);
            getTimeShares(taskProcessingItemDetails.getId()).thenAccept(timeShares -> {
            Log.e("timeshares",""+timeShares);
                if (timeShares != null && !timeShares.isEmpty() &&
                        taskProcessingItemDetails.getStatus().equals("In_Process") &&
                        !taskProcessingItemDetails.getTaskDeligateOwnerUserID().equals(getUserId())) {
                    Log.d("DEBUG", "timeShares != null: " + (timeShares != null));
                    Log.d("DEBUG", "!timeShares.isEmpty(): " + (timeShares != null && !timeShares.isEmpty()));
                    Log.d("DEBUG", "Status equals In_Process: " + taskProcessingItemDetails.getStatus().equals("In_Process"));
                    Log.d("DEBUG", "DelegateOwnerUserID mismatch: " +
                            !taskProcessingItemDetails.getTaskDeligateOwnerUserID().equals(getUserId()));
                    TDLIDComplete.setVisibility(View.VISIBLE);
                    TDLIDComplete.setText("Approval Request");

                } else {
                    // Either timeShares is null or empty, or the other conditions failed
                    TDLIDComplete.setVisibility(View.INVISIBLE);
                }


//                if(timeShares != null && taskProcessingItemDetails.getStatus().equals("In_Process") &&
//                        !taskProcessingItemDetails.getTaskDeligateOwnerUserID().equals(getUserId())
//                        && !timeShares.isEmpty()) {
//                    TDLIDComplete.setVisibility(View.VISIBLE);
//                    TDLIDComplete.setText("Approval Request");
//                }else
//                {
//                    TDLIDComplete.setVisibility(View.INVISIBLE);}


//                }else{
////                    if(taskProcessingItemDetails.getStatus().equals("In_Process") &&
////                        !taskProcessingItemDetails.getTaskDeligateOwnerUserID().equals(getUserId())) {
//                    TDLIDComplete.setVisibility(View.VISIBLE);
//                    TDLIDComplete.setText("BApproval Request");
//                }
//                if (taskProcessingItemDetails.getStatus().equals("Unapproved")){
//                    TDLIDComplete.setVisibility(View.INVISIBLE);
//                }
            }).exceptionally(e -> {
                Toast.makeText(getApplicationContext(),"Failed to fetch timeshares",Toast.LENGTH_LONG).show();
                return null;
            });
            if(taskProcessingItemDetails.getStatus().equals("Approved")) {TDLIDComplete.setVisibility(View.VISIBLE); TDLIDComplete.setText("Complete");}else {
                TDLIDComplete.setVisibility(View.INVISIBLE);
            }
//                    if(taskProcessingItemDetails.getStatus().equals("In_Process") &&
//                        !taskProcessingItemDetails.getTaskDeligateOwnerUserID().equals(getUserId())) {
//                    TDLIDComplete.setVisibility(View.VISIBLE);
//                    TDLIDComplete.setText("Approval Request");
//                }
//            else if (taskProcessingItemDetails.getStatus().equals("Unapproved")
//                    && !taskProcessingItemDetails.getTaskDeligateOwnerUserID().equals(getUserId())){
//                TDLIDComplete.setText("Approval Request");
//            }

            TDLIDProcessing.setVisibility(View.INVISIBLE);
         }
        else if (taskSenderApprovalItemDetails!=null)
        {
            TDLIDDate.setText(taskSenderApprovalItemDetails.getDeligationDateTime());
            TDLIDUserName.setText(taskSenderApprovalItemDetails.getTaskDeligateOwnerUserID());
            TDLIDReceivedUserName.setText("To,  " + taskSenderApprovalItemDetails.getTaskDeligateOwnerUserID());
            TDLIDActivityName.setText(taskSenderApprovalItemDetails.getActivityName());
            TDLIDTaskName.setText(taskSenderApprovalItemDetails.getTaskName());
            TDLIDProjCode.setText(taskSenderApprovalItemDetails.getProjectNo());
            TDLIDProjName.setText(taskSenderApprovalItemDetails.getProjectName());
            TDLIDExpectedDate.setText(taskSenderApprovalItemDetails.getExpectedDate());
            TDLIDExpectedTime.setText(taskSenderApprovalItemDetails.getExpectedTotalTime());
            TDLIDDescription.setText(taskSenderApprovalItemDetails.getDescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(senderApprovalMeasurableList, getApplicationContext());
            TDLIDlistView.setAdapter(measurableListCustomAdapter);

            TDLIDProcessing.setVisibility(View.INVISIBLE);
            TDLIDComplete.setVisibility(View.INVISIBLE);
        }
        else if (taskReceiverApprovalItemDetails != null)
        {
            if (getUserId().equals(taskReceiverApprovalItemDetails.getTaskDeligateOwnerUserID())
                    && taskReceiverApprovalItemDetails.getStatus().equals("Unapproved"))
            { TDLIDComplete.setText("Check  Approve Task");}
            else {TDLIDComplete.setVisibility(View.INVISIBLE);}

            TDLIDDate.setText(taskReceiverApprovalItemDetails.getDeligationDateTime());
            TDLIDUserName.setText("To, "+taskReceiverApprovalItemDetails.getTaskReceivedUserId());
            TDLIDReceivedUserName.setText("From,  " + taskReceiverApprovalItemDetails.getTaskDeligateOwnerUserID());
            TDLIDActivityName.setText(taskReceiverApprovalItemDetails.getActivityName());
            TDLIDTaskName.setText(taskReceiverApprovalItemDetails.getTaskName());
            TDLIDProjCode.setText(taskReceiverApprovalItemDetails.getProjectNo());
            TDLIDProjName.setText(taskReceiverApprovalItemDetails.getProjectName());
            TDLIDExpectedDate.setText(taskReceiverApprovalItemDetails.getExpectedDate());
            TDLIDExpectedTime.setText(taskReceiverApprovalItemDetails.getExpectedTotalTime());
            TDLIDDescription.setText(taskReceiverApprovalItemDetails.getDescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(receiverApprovalMeasurableList, getApplicationContext());
            TDLIDlistView.setAdapter(measurableListCustomAdapter);

            TDLIDProcessing.setVisibility(View.INVISIBLE);
        }


            TDLIDComplete.setOnClickListener(v -> {
                if (taskDelegateListItemDetails != null)
                {
                    if (taskDelegateListItemDetails.getCompletedOn().equals("not_completed")) {
                        if (InternetConnectivity.isConnected()) {
                            updateTaskManagementStatus(taskDelegateListItemDetails.getId(),completed).thenAccept(isCompleted -> {
                                Log.e("isCompleted after future resolves"," "+isCompleted);
                                if(isCompleted){
                                    Log.e("Complted"," "+isCompleted);

                                    appExecutors.getMainThread().execute(() -> {
                                        Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Task Completed", Toast.LENGTH_LONG).show();
                                        finish();
                                    });
                                }else {
                                    Log.e("Update failed", "Future resolved with false");
                                }
                            }).exceptionally( e -> {
                                Log.e("Exception in CompletableFuture", e.getMessage());
                                Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Failed to update the task", Toast.LENGTH_LONG).show();
                                return null;
                            });

                        } else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show(); }
                    }
                }
                else if (taskProcessingItemDetails != null)
                {
                    if (taskProcessingItemDetails.getStatus().equals("In_Process")) {
                        if (InternetConnectivity.isConnected()) {

                            updateTaskManagementStatus(taskProcessingItemDetails.getId(),unapproved).thenAccept(requestForApproval -> {
                                if(requestForApproval){
                                    appExecutors.getMainThread().execute(() -> {
                                        Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Request forwarded to approve task", Toast.LENGTH_LONG).show();
                                        finish();
                                    });
                                }
                            }).exceptionally( e -> {
                                Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Task Send For Approval Completion", Toast.LENGTH_LONG).show();
                                return null;
                            });

                        } else {
                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                        }
                    }

                    if (taskProcessingItemDetails.getStatus().equals(approved.name())) {
                        if (InternetConnectivity.isConnected()) {

                            updateTaskManagementStatus(taskProcessingItemDetails.getId(),completed).thenAccept(requestForApproval -> {
                                if(requestForApproval){
                                    appExecutors.getMainThread().execute(() -> {
                                        Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Task completed", Toast.LENGTH_LONG).show();
                                        finish();
                                    });
                                }
                            }).exceptionally( e -> {
                                Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Task Send For Approval Completion", Toast.LENGTH_LONG).show();
                                return null;
                            });

                        } else {
                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else if (taskSenderApprovalItemDetails != null)
                {
                    if (taskSenderApprovalItemDetails.getCompletedOn().equals("not_completed")) {
                        if (InternetConnectivity.isConnected()) {
                            updateTaskManagementStatus(taskDelegateListItemDetails.getId(),completed).thenAccept(isCompleted -> {
                                if(isCompleted){
                                    appExecutors.getMainThread().execute(() -> {
                                        Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Task Completed", Toast.LENGTH_LONG).show();
                                        finish();
                                    });
                                }
                            }).exceptionally( e -> {
                                Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Failed to update the task", Toast.LENGTH_LONG).show();
                                return null;
                            });


                        } else {
                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                if(taskReceiverApprovalItemDetails != null){
                    if (InternetConnectivity.isConnected()) {

                        updateTaskManagementStatus(taskReceiverApprovalItemDetails.getId(),approved).thenAccept(isCompleted -> {
                            if(isCompleted){
                                appExecutors.getMainThread().execute(() -> {
                                    Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Task Approved", Toast.LENGTH_LONG).show();
                                    finish();
                                });
                            }
                        }).exceptionally( e -> {
                            Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Failed to update the task", Toast.LENGTH_LONG).show();
                            return null;
                        });
                    } else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show(); }

                }
            });

            TDLIDDisplayTimeShares.setOnClickListener(v -> {
                if(taskAcceptedItemDetails != null) {
                    Intent i = new Intent(getApplicationContext(),TTSTimeShareFormActivity.class);
                    i.putExtra("TaskAcceptedDetails",taskAcceptedItemDetails);
                    startActivity(i);
                    return;
                }
                if (taskDelegateListItemDetails != null)
                {
                    Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);
                    i.putExtra("TaskDelegatedDetails", taskDelegateListItemDetails);
                    startActivity(i);
                    return;
                }
                if (taskProcessingItemDetails != null)
                {

                    Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);
                    i.putExtra("TaskProcessingDetails", taskProcessingItemDetails);
                    startActivity(i);
                    return;
                }
                if (taskSenderApprovalItemDetails != null)
                {
                    Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);
                    i.putExtra("TaskSenderApprovalDetails", taskSenderApprovalItemDetails);
                    startActivity(i);
                    return;
                }
                if (taskReceiverApprovalItemDetails != null)
                {    Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);
                    i.putExtra("TaskReceiverApprovalDetails", taskReceiverApprovalItemDetails);
                    startActivity(i);
                }
            });


            TDLIDProcessing.setOnClickListener(v -> {
                if (InternetConnectivity.isConnected())
                {       Log.e("In-Process-Status"," "+inProcess.name());
                       updateTaskManagementStatus(taskAcceptedItemDetails.getId(),inProcess).thenAccept(isCompleted -> {
                        if(isCompleted){
                            appExecutors.getMainThread().execute(() -> {
                                Toast.makeText(getApplicationContext(), "You Have Start Working on Task", Toast.LENGTH_LONG).show();
                                finish();
                            });
                        }
                    }).exceptionally( e -> {
                        Toast.makeText(getApplicationContext(), "Failed to update the task", Toast.LENGTH_LONG).show();
                        return null;
                    });

                }
            });

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
                            timeShares.removeIf(Objects::isNull);
                            future.complete(timeShares);
                        }
                    }

                    if (apiResponse instanceof APIErrorResponse) {
                        String msg = ((APIErrorResponse) apiResponse).getErrorMessage();
                        Log.e("Error", "" + msg);
                        future.completeExceptionally(new Throwable(msg));
                        return;
                    }
                    if (apiResponse instanceof APIEmptyResponse) {
                        Log.e("API Response", "" + "empty response");
                        future.completeExceptionally(new Throwable("empty response"));
                    }

                } catch (ClassCastException e) {
                    Log.e("ClassCastException error", "Error : unable to cast due to " + e.getMessage());
                    future.completeExceptionally(new Throwable("class cannot be converted"));
                } catch (IOException e) {
                    Log.e("IO Excetpion error", "Error : " + e.getMessage());
                    future.completeExceptionally(new Throwable("class cannot be converted"));
                } catch (RuntimeException e) {
                    Log.e("Unnoticed Exception", "Error : " + "occured " + e.getMessage());
                    future.completeExceptionally(new Throwable("class cannot be converted"));
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Response", "Request failed due to " + t.getMessage());
                future.completeExceptionally(t);
            }
        });

        return future;
    }
//    @Override
//    public void onBackPressed() { finish(); }

    private String getUserId()
    {
        sessionManager = new SessionManager(getApplicationContext());
        return sessionManager.getUserID();
    }
    public CompletableFuture<Boolean> updateTaskManagementStatus(Long taskId, Enum obj){
            CompletableFuture<Boolean> isUpdated = new CompletableFuture<>();

            Call<ResponseBody> call = taskHandlerInterface.updateTaskManagementStatus(taskId,obj.name());
            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    Log.e("response",":-"+response);
                    try {
                        APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                        Log.e("apiResponse",":-"+apiResponse);

                        if (apiResponse instanceof APISuccessResponse){

                            String msg = ((APISuccessResponse<ResponseBody> ) apiResponse).getBody().getMessage().getAsString();
                            Log.e("msg",":-"+msg);
                            if(msg.equals("updated")){
                                Log.e("task updated"," return true");

                                isUpdated.complete(true);
                                return;
                            }
                        }

                        if (apiResponse instanceof APIErrorResponse){
                            APIErrorResponse<ResponseBody> apiErrorResponse = new APIErrorResponse(response.message());
                            String msg = apiErrorResponse.getErrorMessage();
                            Log.e("Error","due to "+msg );
                        }
                    } catch (IOException e) {
                        Log.e("IO exception", "Facing issue to update data as IO exception occurred");
                    } catch ( ClassCastException e){
                        Log.e("ClassCast exception", "Unable to convert apiResponse into apiSuccessResponse");
                    }
                    isUpdated.complete(false); // Ensure fallback
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("Error","Failed to make request due to "+t.getMessage());
                    isUpdated.complete(false);
                }
            });


        return isUpdated;
    }
}
