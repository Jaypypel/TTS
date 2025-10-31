package com.example.neptune.ttsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.os.StrictMode;
//import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.compose.ui.node.MyersDiffKt;
import androidx.fragment.app.FragmentTransaction;

import com.example.neptune.ttsapp.EnumStatus.Status;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;
import com.example.neptune.ttsapp.repository.QueryRepository;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@AndroidEntryPoint
public class TTSTaskAllocatedListItemDetailsActivity extends AppCompatActivity {

    Status accepted = Status.Accepted;


    @Inject
    QueryRepository queryRepository;

    @Inject
    AppExecutors appExecutor;

    @Inject
    TaskHandlerInterface taskHandlerService;

    public TTSTaskAllocatedListItemDetailsActivity() { }

    private TextView TALIDDate,TALIDActivityName,TALIDTaskName,TALIDProjCode,TALIDProjName,TALIDExpectedDate,TALIDExpectedTime,TALIDUserName;

    private Button TALIDAccept,TALIDDisplayTimeShare,TALIDModify;

    private ListView TALIDlistView;

    private TextView TALIDDescription;

    boolean result=false;

    TaskDataModel allocatedTaskListItemDetails,completedTaskListItemDetails;
    ArrayList<MeasurableListDataModel> allocatedTaskMeasurableList,completedTaskMeasurableList;

    private static MeasurableListCustomAdapter measurableListCustomAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_ttstask_allocated_list_item_details);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

        TALIDDate=findViewById(R.id.textViewTALIDDate);
        TALIDUserName=findViewById(R.id.textViewTALIDUser);
        TALIDActivityName=findViewById(R.id.textViewTALIDActName);
        TALIDTaskName=findViewById(R.id.textViewTALIDTaskName);
        TALIDProjCode=findViewById(R.id.textViewTALIDProjNo);
        TALIDProjName=findViewById(R.id.textViewTALIDProjName);
        TALIDExpectedDate=findViewById(R.id.textViewTALIDExpDate);
        TALIDExpectedTime=findViewById(R.id.textViewTALIDExpTime);
        TALIDDescription=findViewById(R.id.textViewTALIDDescription);
        TALIDlistView=findViewById(R.id.listMeasurableTALID);
        TALIDAccept =findViewById(R.id.buttonTALIDAccept);
        TALIDDisplayTimeShare =findViewById(R.id.buttonTALIDDisplayTimeShare);
        TALIDModify =findViewById(R.id.buttonTALIDModify);


        // Getting Details From Allocated Task
            allocatedTaskListItemDetails  = (TaskDataModel) getIntent().getSerializableExtra("TaskAllocatedListItemDetails");
            allocatedTaskMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("TaskAllocatedListMeasurableList");
            Log.e("allocatedTasks",""+allocatedTaskListItemDetails);

        // Getting Details From Completed Task
            completedTaskListItemDetails  = (TaskDataModel) getIntent().getSerializableExtra("TaskCompletedListItemDetails");
            completedTaskMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("TaskCompletedListMeasurableList");
            Log.e("completedTaskListItemDetails",""+completedTaskListItemDetails);

        if(allocatedTaskListItemDetails!=null)
        {
            Log.e("allocatedTaskListItemDetails",""+allocatedTaskListItemDetails);
            TALIDDate.setText(allocatedTaskListItemDetails.getTaskAssignedOn());
            TALIDUserName.setText("From,  " + allocatedTaskListItemDetails.getTaskOwnerUserID());
            TALIDActivityName.setText(allocatedTaskListItemDetails.getActivityName());
            TALIDTaskName.setText(allocatedTaskListItemDetails.getTaskName());
            TALIDProjCode.setText(allocatedTaskListItemDetails.getProjectCode());
            TALIDProjName.setText(allocatedTaskListItemDetails.getProjectName());
            TALIDExpectedDate.setText(allocatedTaskListItemDetails.getExpectedDate());
            TALIDExpectedTime.setText(allocatedTaskListItemDetails.getExpectedTotalTime());
            TALIDDescription.setText(allocatedTaskListItemDetails.getDescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(allocatedTaskMeasurableList, getApplicationContext());
            TALIDlistView.setAdapter(measurableListCustomAdapter);

            TALIDDisplayTimeShare.setText("Raise a query");
        }

        else if (completedTaskListItemDetails != null)
        {   completedTaskListItemDetails.getTaskOwnerUserID();

            TALIDDate.setText(completedTaskListItemDetails.getTaskAssignedOn());
            TALIDUserName.setText("From,  " + completedTaskListItemDetails.getTaskOwnerUserID());
            TALIDActivityName.setText(completedTaskListItemDetails.getActivityName());
            TALIDTaskName.setText(completedTaskListItemDetails.getTaskName());
            TALIDProjCode.setText(completedTaskListItemDetails.getProjectCode());
            TALIDProjName.setText(completedTaskListItemDetails.getProjectName());
            TALIDExpectedDate.setText(completedTaskListItemDetails.getExpectedDate());
            TALIDExpectedTime.setText(completedTaskListItemDetails.getExpectedTotalTime());
            TALIDDescription.setText(completedTaskListItemDetails.getDescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(completedTaskMeasurableList, getApplicationContext());
            TALIDlistView.setAdapter(measurableListCustomAdapter);

            TALIDModify.setVisibility(View.INVISIBLE);
            TALIDAccept.setVisibility(View.INVISIBLE);

        }





            TALIDAccept.setOnClickListener(v -> {
                if(allocatedTaskListItemDetails.getTaskAcceptedOn()!=null && allocatedTaskListItemDetails.getTaskAcceptedOn().equals(LocalDateTime.of(1970, 1, 1, 0, 0).toString())) {
                    if (InternetConnectivity.isConnected())
                    {
                        TALIDAccept.setEnabled(false);
                        updateTaskManagementStatus(allocatedTaskListItemDetails.getId(),accepted).thenAccept(isCompleted -> {
                            if(isCompleted){
                                appExecutor.getMainThread().execute(() -> {
                                    Toast.makeText(TTSTaskAllocatedListItemDetailsActivity.this, "Task Accepted", Toast.LENGTH_LONG).show();

                                    finish();
                                    TALIDAccept.setEnabled(true);
                                });
                            }else {
                                Toast.makeText(TTSTaskAllocatedListItemDetailsActivity.this, "Failure: ", Toast.LENGTH_LONG).show();
                                TALIDAccept.setEnabled(true);
                            }
                        }).exceptionally( e -> {
                            Toast.makeText(TTSTaskAllocatedListItemDetailsActivity.this, "Failure: "+e.getMessage(), Toast.LENGTH_LONG).show();
                            TALIDAccept.setEnabled(true);
                            return null;
                        });
                    }else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                        TALIDAccept.setEnabled(true);}
                }

            });


            TALIDDisplayTimeShare.setOnClickListener(v -> {
                if(allocatedTaskListItemDetails!=null && allocatedTaskListItemDetails.getStatus().equals("Pending")){

                    appExecutor.getNetworkIO().execute(() ->  {
                        ArrayList<Query>  queries = queryRepository.getQueries(allocatedTaskListItemDetails.getId()).join();
                        if(!queries.isEmpty() ){
                            Intent intent = new Intent(this, DisplayQueriesAgainstTaskActivity.class);
                            intent.putParcelableArrayListExtra("queries",  queries);
                            intent.putExtra("task_id", Math.toIntExact(allocatedTaskListItemDetails.getId())); // Pass your ID here
                            startActivity(intent);

                        }else {
                            Intent intent = new Intent(this, QueryAgainstTask.class);
                            intent.putExtra("task_id", Math.toIntExact(allocatedTaskListItemDetails.getId())); // Pass your ID here
                            startActivity(intent);
                        }
                    });



                }else {

                Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);

                i.putExtra("TaskCompletedDetails",completedTaskListItemDetails);
                startActivity(i);
                finish();}

            });

            TALIDModify.setOnClickListener(v -> {

                Intent i = new Intent(getApplicationContext(), TTSTaskModificationActivity.class);

                i.putExtra("TaskModificationDetails",allocatedTaskListItemDetails);
                i.putExtra("TaskModificationMeasurableList",allocatedTaskMeasurableList);

                startActivity(i);

            });


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish(); }



    public CompletableFuture<Boolean> updateTaskManagementStatus(Long taskId, Enum obj){
        CompletableFuture<Boolean> isUpdated = new CompletableFuture<>();

        Call<ResponseBody> call = taskHandlerService.updateTaskManagementStatus(taskId,obj.name());
        call.enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                Log.e("response",":-"+response);
                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof APISuccessResponse){
                        String msg = ((APISuccessResponse<ResponseBody> ) apiResponse)
                                .getBody()
                                .getMessage()
                                .getAsString();
                        if(msg.equals("updated")){
                            isUpdated.complete(true);
                           // return;
                        }
                    }

                    if (apiResponse instanceof APIErrorResponse) {
                        String erMsg = ((APIErrorResponse<ResponseBody>) apiResponse).getErrorMessage();
                        isUpdated.completeExceptionally(new Throwable(erMsg));

                    }
                    if (apiResponse instanceof APIErrorResponse) {
                        isUpdated.completeExceptionally(new Throwable("empty response"));
                    }
                }
                catch (ClassCastException e){
                    isUpdated.completeExceptionally(new Throwable("Unable to cast the response into required format due to "+ e.getMessage()));
                }
                catch (IOException e) {
                    Log.e("IOException", "Exception occurred: " + e.getMessage(), e);
                    isUpdated.completeExceptionally(new Throwable("Exception occured while getting no. of completed tasks due to" + e.getMessage()));
                }
                catch (RuntimeException e) {
                    isUpdated.completeExceptionally(new Throwable("Unnoticed Exception occurred which is "+ e.getMessage() +   " its cause "+e.getCause()));
                }
                //isUpdated.complete(false); // Ensure fallback
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("Error","Failed to make request due to "+t.getMessage());
                isUpdated.completeExceptionally(new Throwable(t.getMessage()));
            }
        });


        return isUpdated;
    }


}
