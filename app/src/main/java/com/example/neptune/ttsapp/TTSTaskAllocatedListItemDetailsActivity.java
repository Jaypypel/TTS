package com.example.neptune.ttsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
//import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.neptune.ttsapp.EnumStatus.Status;
import com.example.neptune.ttsapp.Network.APIErrorResponse;
import com.example.neptune.ttsapp.Network.APIResponse;
import com.example.neptune.ttsapp.Network.APISuccessResponse;
import com.example.neptune.ttsapp.Network.ResponseBody;
import com.example.neptune.ttsapp.Network.TaskHandlerInterface;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;
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
            TALIDDate.setText(allocatedTaskListItemDetails.getDeligationDateTime());
            TALIDUserName.setText("From,  " + allocatedTaskListItemDetails.getTaskDeligateOwnerUserID());
            TALIDActivityName.setText(allocatedTaskListItemDetails.getActivityName());
            TALIDTaskName.setText(allocatedTaskListItemDetails.getTaskName());
            TALIDProjCode.setText(allocatedTaskListItemDetails.getProjectNo());
            TALIDProjName.setText(allocatedTaskListItemDetails.getProjectName());
            TALIDExpectedDate.setText(allocatedTaskListItemDetails.getExpectedDate());
            TALIDExpectedTime.setText(allocatedTaskListItemDetails.getExpectedTotalTime());
            TALIDDescription.setText(allocatedTaskListItemDetails.getDescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(allocatedTaskMeasurableList, getApplicationContext());
            TALIDlistView.setAdapter(measurableListCustomAdapter);

            TALIDDisplayTimeShare.setVisibility(View.INVISIBLE);
        }

        else if (completedTaskListItemDetails != null)
        {

            TALIDDate.setText(completedTaskListItemDetails.getDeligationDateTime());
            TALIDUserName.setText("From,  " + completedTaskListItemDetails.getTaskDeligateOwnerUserID());
            TALIDActivityName.setText(completedTaskListItemDetails.getActivityName());
            TALIDTaskName.setText(completedTaskListItemDetails.getTaskName());
            TALIDProjCode.setText(completedTaskListItemDetails.getProjectNo());
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
                if(allocatedTaskListItemDetails.getAcceptedOn()!=null && allocatedTaskListItemDetails.getAcceptedOn().equals("not_accepted")) {
                    if (InternetConnectivity.isConnected())
                    {

                        updateTaskManagementStatus(allocatedTaskListItemDetails.getId(),accepted).thenAccept(isCompleted -> {
                            Log.e("isCompleted after future resolves"," "+isCompleted);
                            if(isCompleted){
                                Log.e("Complted"," "+isCompleted);

                                appExecutor.getMainThread().execute(() -> {
                                    Toast.makeText(TTSTaskAllocatedListItemDetailsActivity.this, "Task Accepted", Toast.LENGTH_LONG).show();
                                    finish();
                                });
                            }else {
                                Log.e("Update failed", "Future resolved with false");
                            }
                        }).exceptionally( e -> {
                            Log.e("Exception in CompletableFuture", e.getMessage());
                            Toast.makeText(TTSTaskAllocatedListItemDetailsActivity.this, "Failed to update the task", Toast.LENGTH_LONG).show();
                            return null;
                        });
                        Log.d("Accept","YES");
//                    result = updateAcceptTimeStatus(allocatedTaskListItemDetails.getId());
//                    if (result) {
//                        Toast.makeText(TTSTaskAllocatedListItemDetailsActivity.this, "Task Accepted", Toast.LENGTH_LONG).show();
//                        finish();
//                    }
                    }else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}
                }

            });

            TALIDDisplayTimeShare.setOnClickListener(v -> {

                Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);

                i.putExtra("TaskCompletedDetails",completedTaskListItemDetails);
                startActivity(i);
                finish();

            });

            TALIDModify.setOnClickListener(v -> {

                Intent i = new Intent(getApplicationContext(), TTSTaskModificationActivity.class);

                i.putExtra("TaskModificationDetails",allocatedTaskListItemDetails);
                i.putExtra("TaskModificationMeasurableList",allocatedTaskMeasurableList);

                startActivity(i);

            });


    }

    @Override
    public void onBackPressed() { finish(); }



    public CompletableFuture<Boolean> updateTaskManagementStatus(Long taskId, Enum obj){
        CompletableFuture<Boolean> isUpdated = new CompletableFuture<>();

        Call<ResponseBody> call = taskHandlerService.updateTaskManagementStatus(taskId,obj.name());
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
                    isUpdated.completeExceptionally(e);
                } catch ( ClassCastException e){
                    Log.e("ClassCast exception", "Unable to convert apiResponse into apiSuccessResponse");
                    isUpdated.completeExceptionally(e);
                }
                //isUpdated.complete(false); // Ensure fallback
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("Error","Failed to make request due to "+t.getMessage());
                isUpdated.completeExceptionally(t);
            }
        });


        return isUpdated;
    }



    // Update the Time and status when Accept the Task
//    public boolean updateAcceptTimeStatus(Long taskId){
//        Connection con;
//        int x = 0;
//
//        try {
//            con = DatabaseHelper.getDBConnection();
//
//            Calendar calendar = Calendar.getInstance();
//            Timestamp acceptTimestamp = new Timestamp(calendar.getTime().getTime());
//
//            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET STATUS =?, ACCEPTED_ON =? WHERE ID =?");
//
//            ps.setString(1,"ACCEPTED");
//            ps.setString(2, acceptTimestamp.toString());
//            ps.setLong(3,taskId);
//            x=ps.executeUpdate();
//
//            if(x==1){ result = true; }
//
//            ps.close();
//            con.close();
//        } catch (Exception e) { e.printStackTrace(); }
//
//        return result;
//
//    }




}
