package com.example.neptune.ttsapp;


import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.Network.APIEmptyResponse;
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
public class TTSTaskModificationActivity extends AppCompatActivity {

    @Inject
    AppExecutors appExecutor;

    @Inject
    TaskHandlerInterface taskHandlerService;

    private TextView prevDate,prevActivityName,prevTaskName,prevProjCode,prevProjName,prevStartTime,
            prevEndTime,prevDescription,prevUserName,StartTimeLabel,EndTimeLabel;

    private EditText modificationDescription;

    private Button submit;

    private ListView listView;
    private static MeasurableListCustomAdapter measurableListCustomAdapter;

    private SessionManager sessionManager;

    private ArrayList<MeasurableListDataModel> dtsMeasurableList,modifyMeasurableList;
    private DailyTimeShareDataModel dtsListItemDetails;
    private TaskDataModel modifyTaskDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttstask_modification);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        prevUserName=findViewById(R.id.textViewPreviewUser);
        prevDate=findViewById(R.id.textViewDate);
        prevActivityName=findViewById(R.id.textViewActName);
        prevTaskName=findViewById(R.id.textViewTaskName);
        prevProjCode=findViewById(R.id.textViewProjNo);
        prevProjName=findViewById(R.id.textViewProjName);
        prevStartTime=findViewById(R.id.textViewStartTime);
        prevEndTime=findViewById(R.id.textViewEndTime);
        prevDescription=findViewById(R.id.textViewDescription);
        listView=findViewById(R.id.listMeasurablePreview);

        StartTimeLabel =findViewById(R.id.textViewStartTimeLabel);
        EndTimeLabel =findViewById(R.id.textViewEndTimeLabel);

        modificationDescription =findViewById(R.id.editTextModificationDescription);

        submit =  findViewById(R.id.buttonModifySubmit);


        // Set UserName
        sessionManager = new SessionManager(getApplicationContext());
        prevUserName.setText(sessionManager.getUserID());


//        //Get Data from TTSTimeShareFragment Intent
//        timeShareDetails = (DailyTimeShareDataModel)getIntent().getSerializableExtra("timeShareData");
//        measurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("MEASURABLElIST");

        // Getting Details From DTS
        dtsListItemDetails = (DailyTimeShareDataModel) getIntent().getSerializableExtra("DTSListItemDetails");
        dtsMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("DTSMeasurableList");

        //Getting Details For Modification
        modifyTaskDetails = (TaskDataModel) getIntent().getSerializableExtra("TaskModificationDetails");
        modifyMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("TaskModificationMeasurableList");


        try {
            //Set Data To Preview
           if(modifyTaskDetails!= null) {

               prevActivityName.setText(modifyTaskDetails.getActivityName());
               prevTaskName.setText(modifyTaskDetails.getTaskName());
               prevProjCode.setText(modifyTaskDetails.getProjectCode());
               prevProjName.setText(modifyTaskDetails.getProjectName());
               prevStartTime.setText(modifyTaskDetails.getExpectedDate());
               prevEndTime.setText(modifyTaskDetails.getExpectedTotalTime());
               prevDescription.setText(modifyTaskDetails.getDescription());
               measurableListCustomAdapter = new MeasurableListCustomAdapter(modifyMeasurableList, getApplicationContext());
               listView.setAdapter(measurableListCustomAdapter);

               StartTimeLabel.setText("Expected Date");
               EndTimeLabel.setText("Expected Time");

               prevDate.setVisibility(View.INVISIBLE);


           }
            else {
                prevDate.setText(dtsListItemDetails.getDateOfTimeShare());
                prevActivityName.setText(dtsListItemDetails.getActivityName());
                prevTaskName.setText(dtsListItemDetails.getTaskName());
                prevProjCode.setText(dtsListItemDetails.getProjectCode());
                prevProjName.setText(dtsListItemDetails.getProjectName());
                prevStartTime.setText(dtsListItemDetails.getStartTime());
                prevEndTime.setText(dtsListItemDetails.getEndTime());
                prevDescription.setText(dtsListItemDetails.getDescription());
                measurableListCustomAdapter = new MeasurableListCustomAdapter(dtsMeasurableList, getApplicationContext());
                listView.setAdapter(measurableListCustomAdapter);

               modificationDescription.setVisibility(View.INVISIBLE);
               submit.setVisibility(View.INVISIBLE);
            }

        }catch (Exception e){e.printStackTrace();}


        submit.setOnClickListener(v -> {

            if (InternetConnectivity.isConnected())
            {
            String description = modificationDescription.getText().toString().trim();

                updateModificationTaskStatusAndDescription(description,modifyTaskDetails.getId()).thenAccept(isTaskAdded -> {
                    if(isTaskAdded.equals("updated")){
                        appExecutor.getMainThread().execute(() ->
                        {
                            Toast.makeText(getApplicationContext().getApplicationContext(), "Sending Modification Request Successfully ", Toast.LENGTH_LONG).show();
                            modificationDescription.setText("");
                        });
                    }else {
                        appExecutor.getMainThread().execute(() -> Toast
                                .makeText(getApplicationContext()
                                        .getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG)
                                .show());
                    }
                }).exceptionally(e -> {
                    Toast.makeText(getApplicationContext().getApplicationContext(), "Failed to add activity due to "+e.getMessage(), Toast.LENGTH_LONG).show();

                    return null;
                });


            }else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}
        });

    }

    @Override
    public void onBackPressed() { finish(); }



    public CompletableFuture<String> updateModificationTaskStatusAndDescription(String descriptionj, Long taskId){
        CompletableFuture<String> future = new CompletableFuture<>();
        Call<ResponseBody> call = taskHandlerService.updateModifiedTaskStatusAndDescription(descriptionj,taskId );
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    APIResponse<ResponseBody> apiResponse = APIResponse.create(response);
                    if (apiResponse instanceof APISuccessResponse){
                        String msg = ((APISuccessResponse<ResponseBody>) apiResponse).getBody().getMessage().getAsString();
                        future.complete(msg);
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
                    future.completeExceptionally(new Throwable("Exception occured while updating the task due to" + e.getMessage()));
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
    // Update the status and Description
//    public boolean updateModificationTaskStatusAndDescription(String description,Long taskId){
//        Connection con;
//        int x = 0;
//        boolean result  = false;
//
//        try {
//            con = DatabaseHelper.getDBConnection();
//
//            Calendar calendar = Calendar.getInstance();
//            Timestamp acceptTimestamp = new Timestamp(calendar.getTime().getTime());
//
//            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET STATUS =?, MODIFICATION_DESCRIPTION=? WHERE ID =?");
//
//            ps.setString(1, "REVISION");
//            ps.setString(2,description);
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
