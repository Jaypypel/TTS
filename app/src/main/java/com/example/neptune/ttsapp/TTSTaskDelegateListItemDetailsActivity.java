package com.example.neptune.ttsapp;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.EnumStatus.Status;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Objects;

import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TTSTaskDelegateListItemDetailsActivity extends AppCompatActivity {

    public static final String NO_INTERNET_CONNECTION = "No Internet Connection";
    public static final String ERROR = "Error";
    public static final String RESPONSE = "response";
    public static final String STRING = ":-";
    public static final String FAILED_TO_MAKE_REQUEST_DUE_TO = "Failed to make request due to ";
    public static final String UNNOTICED_EXCEPTION_OCCURRED_WHICH_IS = "Unnoticed Exception occurred which is ";
    public static final String ITS_CAUSE = " its cause ";
    public static final String EXCEPTION_OCCURRED_WHILE_PERFORMING_INPUT_OUTPUT_OF_MEASURABLES_DUE_TO = "Exception occurred while performing input output of measurables due to";
    public static final String EXCEPTION_OCCURRED = "Exception occurred: ";
    public static final String IO_EXCEPTION = "IOException";
    public static final String UNABLE_TO_CAST_THE_RESPONSE_INTO_REQUIRED_FORMAT_DUE_TO = "Unable to cast the response into required format due to ";
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

        private Button TDLIDComplete;
        private Button TDLIDDisplayTimeShares;
        private Button TDLIDProcessing;

        private TaskDataModel taskDelegateListItemDetails,taskAcceptedItemDetails,
                taskProcessingItemDetails,
                taskSenderApprovalItemDetails, taskReceiverApprovalItemDetails ;
        ArrayList<MeasurableListDataModel> delegatedMeasurableList,processingMeasurableList,
                senderApprovalMeasurableList,receiverApprovalMeasurableList, acceptedTaskMeasurables;

        private  MeasurableListCustomAdapter measurableListCustomAdapter;
    TextView TDLIDDate ;
    TextView TDLIDUserName ;
    TextView TDLIDReceivedUserName ;
    TextView TDLIDActivityName;
    TextView TDLIDTaskName ;
    TextView TDLIDProjCode ;
    TextView TDLIDProjName ;
    TextView TDLIDExpectedDate ;
    TextView TDLIDExpectedTime ;
    TextView TDLIDDescription;
    ListView TDLIDlistView ;
    private SessionManager sessionManager;


            @Override
            protected void onCreate(Bundle savedInstanceState) {
                super.onCreate(savedInstanceState);
                setContentView(R.layout.activity_ttstask_delegated_list_item_details);

                 TDLIDDate = findViewById(R.id.textViewTDLIDDate);
                 TDLIDUserName = findViewById(R.id.textViewTDLIDUser);
                 TDLIDReceivedUserName = findViewById(R.id.textViewTDLIDReceivedUser);
                 TDLIDActivityName = findViewById(R.id.textViewTDLIDActName);
                 TDLIDTaskName = findViewById(R.id.textViewTDLIDTaskName);
                 TDLIDProjCode = findViewById(R.id.textViewTDLIDProjNo);
                 TDLIDProjName = findViewById(R.id.textViewTDLIDProjName);
                 TDLIDExpectedDate = findViewById(R.id.textViewTDLIDExpDate);
                 TDLIDExpectedTime = findViewById(R.id.textViewTDLIDExpTime);
                TDLIDDescription = findViewById(R.id.textViewTDLIDDescription);
                TDLIDlistView = findViewById(R.id.listMeasurableTDLID);
                TDLIDComplete =findViewById(R.id.buttonTDLIDComplete);
                TDLIDDisplayTimeShares =findViewById(R.id.buttonTDLIDDisplayTimeShares);
                TDLIDProcessing =findViewById(R.id.buttonTDLIDProcessing);
                Button goToQueries = findViewById(R.id.queriesButton);

//                TextView TDLIDMeasurableLabel = findViewById(R.id.textViewTDLIDMeasurableLabel);

                // Getting Details From Delegated Task
                taskDelegateListItemDetails  = getCurrentTaskDetails("TaskDelegatedItemDetails");
                delegatedMeasurableList = getMeasurables("TaskDelegatedMeasurableList");

                // Getting Details From Accepted Task
                taskAcceptedItemDetails = getCurrentTaskDetails("acceptedTasks");
                acceptedTaskMeasurables =  getMeasurables("acceptedTaskMeasurables");

                // Getting Details From Processing
                taskProcessingItemDetails =  getCurrentTaskDetails("TaskProcessingItemDetails");
                processingMeasurableList = getMeasurables("TaskProcessingMeasurableDetails");

                // Getting Details From Sender Approval Task
                taskSenderApprovalItemDetails =getCurrentTaskDetails("senderTaskApprovalItemDetails");
                senderApprovalMeasurableList = getMeasurables("senderTaskApprovalMeasurableList");

                // Getting Details From Receiver Approval Task
                taskReceiverApprovalItemDetails = getCurrentTaskDetails("receiverTaskApprovalItemDetails");
                receiverApprovalMeasurableList = getMeasurables("receiverTaskApprovalMeasurableList");

            if(taskDelegateListItemDetails!=null) {
                TDLIDUserName.setText(taskDelegateListItemDetails.getTaskOwnerUserID());
                TDLIDReceivedUserName.setText(getString(R.string.text_to_user,taskDelegateListItemDetails.getTaskReceivedUserID()));
                setCurrentTaskDetails(taskDelegateListItemDetails,delegatedMeasurableList);
                TDLIDProcessing.setVisibility(View.INVISIBLE);

            //    if (taskDelegateListItemDetails.getStatus().equals("")) { TDLIDComplete.setVisibility(View.INVISIBLE); }
                // a user who assigned the task , can not complete the task by own, thus this code check the same
                if (loginUserNotEqualToTaskReceviedUser()) hideCurrentButton(TDLIDComplete);
            }
            else if (taskAcceptedItemDetails!=null)
            {
                TDLIDUserName.setText(taskAcceptedItemDetails.getTaskReceivedUserID());
                TDLIDReceivedUserName.setText(getString(R.string.text_from_user,taskAcceptedItemDetails.getTaskOwnerUserID()));
                setCurrentTaskDetails(taskAcceptedItemDetails, acceptedTaskMeasurables);
                if (taskAcceptedItemDetails.getStatus().equals(Status.Accepted.name()) && !taskAcceptedItemDetails.getTaskOwnerUserID().equals(getUsername())){
                    showCurrentButton(TDLIDProcessing);
                    hideCurrentButton(TDLIDDisplayTimeShares);
                    hideCurrentButton(TDLIDComplete);
                }

            }
            else if (taskProcessingItemDetails!=null)
             {

                TDLIDUserName.setText(getString(R.string.text_to_user,taskProcessingItemDetails.getTaskReceivedUserID()));
                TDLIDReceivedUserName.setText(getString(R.string.text_from_user,taskProcessingItemDetails.getTaskOwnerUserID()));
                setCurrentTaskDetails(taskProcessingItemDetails,processingMeasurableList);
                 runOnNetworkThread(() -> getTimeShares(taskProcessingItemDetails.getId())
                         .thenAccept(timeShares -> {
                     Log.e("timeshares",""+timeShares);
                     if (timeShares != null && !timeShares.isEmpty() &&
                             taskProcessingItemDetails.getStatus().equals("In_Process") &&
                             !taskProcessingItemDetails.getTaskOwnerUserID().equals(getUsername())) {
                         runOnUiThread(() -> {
                             showCurrentButton(TDLIDComplete);
                             TDLIDComplete.setText(R.string.button_approve_request);
                             showCurrentButton(TDLIDDisplayTimeShares);
                             hideCurrentButton(TDLIDProcessing);
                         });
                     } else {
                         showCurrentButton(TDLIDDisplayTimeShares);
                         hideCurrentButton(TDLIDComplete);
                         hideCurrentButton(TDLIDProcessing);
                     }


                     if(taskProcessingItemDetails.getStatus().equals(Status.Approved.name()) && !taskProcessingItemDetails.getTaskOwnerUserID().equals(getUsername())) {TDLIDComplete.setVisibility(View.VISIBLE); TDLIDComplete.setText(R.string.button_complete);
                         Log.d("DEBUG", "check status processing task status " + (taskProcessingItemDetails.getStatus()));
                         hideCurrentButton(TDLIDDisplayTimeShares);
                         hideCurrentButton(TDLIDProcessing);
                     }

                     else {
                         // Either timeShares is null or empty, or the other conditions failed
                         hideCurrentButton(TDLIDComplete);

                     }
                     if (taskProcessingItemDetails.getStatus().equals(Status.Accepted.name()) && !taskProcessingItemDetails.getTaskOwnerUserID().equals(getUsername())){
                         showCurrentButton(TDLIDProcessing);
                         hideCurrentButton(TDLIDDisplayTimeShares);
                     }

                     if (taskProcessingItemDetails.getStatus().equals(Status.Pending.name()) && !taskProcessingItemDetails.getTaskOwnerUserID().equals(getUsername())){
                         hideCurrentButton(TDLIDProcessing);
                         hideCurrentButton(TDLIDDisplayTimeShares);
                         hideCurrentButton(TDLIDComplete);
                     }

                     if (taskProcessingItemDetails.getStatus().equals(Status.Revised.name()) && !taskProcessingItemDetails.getTaskOwnerUserID().equals(getUsername())){
                         showCurrentButton(TDLIDComplete);
                         runOnUiThread(() -> TDLIDComplete.setText(R.string.button_approve_request));
                         hideCurrentButton(TDLIDDisplayTimeShares);
                         hideCurrentButton(TDLIDProcessing);
                     }


                 }).exceptionally(e -> {
                     showAlertMessageToUser("Failed to fetch timeshares");
                     return null;
                 }));

             }
            else if (taskSenderApprovalItemDetails!=null) {
                TDLIDUserName.setText(taskSenderApprovalItemDetails.getTaskOwnerUserID());
                TDLIDReceivedUserName.setText(getString(R.string.text_to_user,taskSenderApprovalItemDetails.getTaskOwnerUserID()));
                setCurrentTaskDetails(taskSenderApprovalItemDetails,senderApprovalMeasurableList);
                hideCurrentButton(TDLIDProcessing);
                hideCurrentButton(TDLIDComplete);
            }
            else if (taskReceiverApprovalItemDetails != null)
            {
                if (getUsername().equals(taskReceiverApprovalItemDetails.getTaskOwnerUserID())
                        && taskReceiverApprovalItemDetails.getStatus().equals(Status.Unapproved.name()))
                TDLIDComplete.setText(R.string.button_check_approve_task);
                else hideCurrentButton(TDLIDComplete);
               setCurrentTaskDetails(taskReceiverApprovalItemDetails,receiverApprovalMeasurableList);
                hideCurrentButton(TDLIDProcessing);

            }
                TDLIDComplete.
                        setOnClickListener(
                                v -> completeButtonListener()
                        );

                TDLIDDisplayTimeShares
                        .setOnClickListener(
                                v -> timeshareButtonListener()
                        );

                if(isMentor()) goToQueries.setOnClickListener(
                        v -> moveQueriesScreen()
                );
                else showCurrentButton(goToQueries);

                TDLIDProcessing.setOnClickListener(v -> updateTaskAndAlertUserIfNeed(
                        taskDelegateListItemDetails,
                        inProcess,
                        "You Have Start Working on Task"));



        }

        private boolean loginUserNotEqualToTaskReceviedUser(){
            return !taskDelegateListItemDetails.getTaskReceivedUserID().equals(getUsername());
        }

        private void showCurrentButton(Button currentButton){
            currentButton.setVisibility(View.VISIBLE);
        }

        private void hideCurrentButton(Button currentButton){
            currentButton.setVisibility(View.INVISIBLE);
        }

        private TaskDataModel getCurrentTaskDetails(String taskType){
            return (TaskDataModel) getIntent()
                    .getSerializableExtra(taskType);
        }

        private ArrayList<MeasurableListDataModel> getMeasurables(String measurablesAssociatedToCurrentTask)
        {
                return (ArrayList<MeasurableListDataModel>) getIntent()
                        .getSerializableExtra(measurablesAssociatedToCurrentTask);
        }



        private void setCurrentTaskDetails(TaskDataModel currentTaskDetails,
                                           ArrayList<MeasurableListDataModel> measurables){
                TDLIDDate.setText(currentTaskDetails.getTaskAssignedOn());
            TDLIDActivityName.setText(currentTaskDetails.getActivityName());
            TDLIDTaskName.setText(currentTaskDetails.getTaskName());
            TDLIDProjCode.setText(currentTaskDetails.getProjectCode());
            TDLIDProjName.setText(currentTaskDetails.getProjectName());
            TDLIDExpectedDate.setText(currentTaskDetails.getExpectedDate());
            TDLIDExpectedTime.setText(currentTaskDetails.getExpectedTotalTime());
            TDLIDDescription.setText(currentTaskDetails.getDescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(measurables, getApplicationContext());
            TDLIDlistView.setAdapter(measurableListCustomAdapter);

        }
        private void completeButtonListener(){
            if (taskDelegateListItemDetails != null) {
                if(taskDelegateListItemDetails.getTaskCompletedOn().equals(LocalDateTime.of(1970, 1, 1, 0, 0).toString()));
                    updateTaskAndAlertUserIfNeed(
                            taskDelegateListItemDetails,
                            completed,
                            "Task Completed"
                    );
            }
            else if (taskProcessingItemDetails != null)
            {
                if (taskStatusIsInProcessOrRevised(taskProcessingItemDetails))
                    updateTaskAndAlertUserIfNeed(
                            taskProcessingItemDetails,
                            unapproved,
                            "Request forwarded to approve task"
                    );

                if (taskProcessingItemDetails.getStatus().equals(approved.name()))
                    updateTaskAndAlertUserIfNeed(
                            taskProcessingItemDetails,
                            completed,
                            "Task completed");

            }
            else if (taskSenderApprovalItemDetails != null) {
                if (taskSenderApprovalItemDetails.getTaskCompletedOn().equals(LocalDateTime.of(1970, 1, 1, 0, 0).toString()))
                    updateTaskAndAlertUserIfNeed(taskDelegateListItemDetails,completed,"Task Completed");

            }
            if(taskReceiverApprovalItemDetails != null){
                updateTaskAndAlertUserIfNeed(taskReceiverApprovalItemDetails,approved,"Task Approved");
            }
        }
        private boolean taskStatusIsInProcessOrRevised(TaskDataModel currentTask){
            return currentTask.getStatus().equals(Status.In_Process.name()) ||
                    currentTask.getStatus().equals(Status.Revised.name());
        }
        public void timeshareButtonListener(){
            if(taskAcceptedItemDetails != null) {
                goToAddTimeshareScreen("TaskAcceptedDetails",taskAcceptedItemDetails);
                return;
            }
            if (taskDelegateListItemDetails != null)
            {
                if(isTaskStatusEqualsAccepted()){
                    showAlertMessageToUser("The user didn't accept the task yet");
                    return;
                }
                if(isisTaskStatusEqualsPending()){
                    showAlertMessageToUser("The user didn't commit the task yet");
                    return;
                }
                if(isTaskStatusRevised()){
                    showAlertMessageToUser("The user didn't commit the task yet");
                    return;
                }
                goToAddTimeshareScreen("TaskDelegatedDetails",taskDelegateListItemDetails);
                return;
            }

            if(taskProcessingItemDetails != null)  {
                runOnNetworkThread(this::goToAddTimeshareOrTimesharesScreen);
                return ;
            }
            if (taskSenderApprovalItemDetails != null) {
                goToTimeshareScreen("TaskSenderApprovalDetails",
                        taskSenderApprovalItemDetails);
                return;
            }
            if (taskReceiverApprovalItemDetails != null) {
                goToTimeshareScreen("TaskReceiverApprovalDetails",
                        taskReceiverApprovalItemDetails);
            }
        }
        private boolean isTaskStatusRevised(){
            return Status
                    .Revised
                    .name()
                    .equals(taskDelegateListItemDetails.getStatus());
        }

        private boolean isTaskStatusEqualsAccepted(){
           return Status.Accepted.name().equals(taskDelegateListItemDetails.getStatus());
        }

        private boolean isisTaskStatusEqualsPending(){
            return Status.Pending.name().equals(taskDelegateListItemDetails.getStatus());
        }
        private void goToAddTimeshareOrTimesharesScreen(){
            if(hasTimeShare()){
                Log.e(ERROR, "timeShares"+getTimeShares(taskProcessingItemDetails.getId()));
                runOnUiThread(() ->
                        goToAddTimeshareScreen("TaskProcessingDetails",
                                taskProcessingItemDetails)
                );
            }else goToTimeshareScreen("TaskProcessingDetails"
                        ,taskProcessingItemDetails);

        }
        private boolean hasTimeShare(){
            return getTimeShares(taskProcessingItemDetails.getId()).join().isEmpty();
        }
        private void goToAddTimeshareScreen(String details,
                        TaskDataModel task){
            if(taskStatusNotAccepted(task)) showAlertMessageToUser(
                   getString(R.string.text_add_the_timeshare_as_you_dont_have_timeshares)
            );
            Intent i = new Intent(getApplicationContext(),TTSTimeShareFormActivity.class);
            i.putExtra(details, task);
            startActivity(i);
        }

        private boolean taskStatusNotAccepted(TaskDataModel task){
            return !task.getStatus().equals(Status.Accepted.name());
        }
        private void goToTimeshareScreen(String details, TaskDataModel taskDataModel){
            Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);
            i.putExtra(details, taskDataModel);
            startActivity(i);
        }

    private  void moveQueriesScreen(){
        Intent i = new Intent(getApplicationContext(), DisplayQueriesAgainstTaskActivity.class);
        Long id = taskDelegateListItemDetails.getId();
        i.putExtra(getString(R.string.task_id),Math.toIntExact(id));
        startActivity(i);
    }
    private void updateTaskAndAlertUserIfNeed(TaskDataModel task,Status currentStatusOfTask,String userMessage){
        if (InternetConnectivity.isConnected()) runOnNetworkThread(() ->
                callUpdateTaskManagementStatus(task, currentStatusOfTask, userMessage));
        else showAlertMessageToUser(NO_INTERNET_CONNECTION);
    }
    private void runOnNetworkThread(Runnable action) {
        appExecutors.getNetworkIO().execute(action);
    }


        private void callUpdateTaskManagementStatus(TaskDataModel task,
                                                    Status currentStatusOfTask,String userMessage){
                runOnNetworkThread(() -> updateTaskManagementStatus(task.getId(),currentStatusOfTask).
                        thenAccept(isUpdatedSuccessfully -> {
                            if(isUpdatedSuccessfully) runOnUiThread(() -> {
                                    showAlertMessageToUser(userMessage);
                                    finish();
                            });
                            else showAlertMessageToUser(getString(R.string.text_something_went_wrong));
                        }).exceptionally( e -> {
                            showAlertMessageToUser(getString(R.string.text_failure)+ e.getMessage());
                            return null;
                        }));
        }

        private void showAlertMessageToUser(String message) {
            Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
        }
        public boolean isMentor(){
            sessionManager = new SessionManager(getApplicationContext());
            return sessionManager.getRoles().contains(getString(R.string.role_mentor));
        }

        private String getUsername() {
            sessionManager = new SessionManager(getApplicationContext());
            return sessionManager.getUsername();
        }

        public CompletableFuture<ArrayList<TimeShareDataModel>> getTimeShares(Long taskId) {
            CompletableFuture<ArrayList<TimeShareDataModel>> future = new CompletableFuture<>();
            Call<ResponseBody> call = timeShareService.getTimeShares(taskId);
            call.enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<ResponseBody> call,
                                       @NonNull Response<ResponseBody> response) {
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
                            String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                            future.completeExceptionally(new Throwable(erMsg));

                        }
                        if (apiResponse instanceof APIErrorResponse) {
                            future.completeExceptionally(new Throwable(getString(R.string.text_empty_response)));
                        }
                    } catch (ClassCastException e) {
                        future.completeExceptionally(new Throwable(UNABLE_TO_CAST_THE_RESPONSE_INTO_REQUIRED_FORMAT_DUE_TO + e.getMessage()));
                    } catch (IOException e) {
                        Log.e(IO_EXCEPTION, EXCEPTION_OCCURRED + e.getMessage(), e);
                        future.completeExceptionally(new Throwable(getString(R.string.text_exception_occurred_while_getting_timeshares_due_to) + e.getMessage()));
                    } catch (RuntimeException e) {
                        future.completeExceptionally(new Throwable(UNNOTICED_EXCEPTION_OCCURRED_WHICH_IS + e.getMessage() + ITS_CAUSE + e.getCause()));
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                    Log.e(getString(R.string.text_response), getString(R.string.text_request_failed_due_to) + t.getMessage());
                    future.completeExceptionally(new Throwable(t.getMessage()));
                }
            });

            return future;
        }

        public CompletableFuture<Boolean> updateTaskManagementStatus(Long taskId, Status obj){
                CompletableFuture<Boolean> isUpdated = new CompletableFuture<>();

                Call<ResponseBody> call = taskHandlerInterface.updateTaskManagementStatus(taskId,obj.name());
                call.enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        Log.e(RESPONSE, STRING + response);
                        try {
                            APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                            Log.e(getString(R.string.text_apiResponse), STRING + apiResponse);

                            if (apiResponse instanceof APISuccessResponse) {

                                String msg = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                                Log.e(getString(R.string.text_log), STRING + msg);
                                if (msg.equals(getString(R.string.text_success))) {
                                    Log.e(RESPONSE, STRING + true);
                                    isUpdated.complete(true);

                                }
                            }

                            if (apiResponse instanceof APIErrorResponse) {
                                String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                                isUpdated.completeExceptionally(new Throwable(erMsg));

                            }
                            if (apiResponse instanceof APIErrorResponse) {
                                isUpdated.completeExceptionally(new Throwable(getString(R.string.text_empty_response)));
                            }
                        } catch (ClassCastException e) {
                            isUpdated.completeExceptionally(new Throwable(UNABLE_TO_CAST_THE_RESPONSE_INTO_REQUIRED_FORMAT_DUE_TO + e.getMessage()));
                        } catch (IOException e) {
                            Log.e(IO_EXCEPTION, EXCEPTION_OCCURRED + e.getMessage(), e);
                            isUpdated.completeExceptionally(new Throwable(EXCEPTION_OCCURRED_WHILE_PERFORMING_INPUT_OUTPUT_OF_MEASURABLES_DUE_TO + e.getMessage()));
                        } catch (RuntimeException e) {
                            isUpdated.completeExceptionally(new Throwable(UNNOTICED_EXCEPTION_OCCURRED_WHICH_IS + e.getMessage() + ITS_CAUSE + e.getCause()));
                        }
                        //isUpdated.complete(false); // Ensure fallback
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Log.e(ERROR, FAILED_TO_MAKE_REQUEST_DUE_TO + t.getMessage());
                        isUpdated.completeExceptionally(new Throwable(t.getMessage()));
                    }
                });


            return isUpdated;
        }
}
