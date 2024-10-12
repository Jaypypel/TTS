package com.example.neptune.ttsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.appcompat.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class TTSTaskDelegateListItemDetailsActivity extends AppCompatActivity {

    public TTSTaskDelegateListItemDetailsActivity() { }

    private TextView TDLIDDate,TDLIDActivityName,TDLIDTaskName,TDLIDProjCode,TDLIDProjName,TDLIDExpectedDate,TDLIDExpectedTime,TDLIDDescription,TDLIDUserName,TDLIDReceivedUserName,TDLIDMeasurableLabel;

    private Button TDLIDComplete,TDLIDDisplayTimeShares,TDLIDProcessing;

    private ListView TDLIDlistView;

    private boolean result=false;

    private TaskDataModel taskDelegateListItemDetails,taskAcceptedItemDetails,taskProcessingItemDetails,
            taskSenderApprovalItemDetails, taskReceiverApprovalItemDetails ;
    ArrayList<MeasurableListDataModel> delegatedMeasurableList,processingMeasurableList,senderApprovalMeasurableList,receiverApprovalMeasurableList;

    private static MeasurableListCustomAdapter measurableListCustomAdapter;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_ttstask_delegated_list_item_details);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

        TDLIDDate=(TextView)findViewById(R.id.textViewTDLIDDate);
        TDLIDUserName=(TextView)findViewById(R.id.textViewTDLIDUser);
        TDLIDReceivedUserName =(TextView)findViewById(R.id.textViewTDLIDReceivedUser);
        TDLIDActivityName=(TextView)findViewById(R.id.textViewTDLIDActName);
        TDLIDTaskName=(TextView)findViewById(R.id.textViewTDLIDTaskName);
        TDLIDProjCode=(TextView)findViewById(R.id.textViewTDLIDProjNo);
        TDLIDProjName=(TextView)findViewById(R.id.textViewTDLIDProjName);
        TDLIDExpectedDate=(TextView)findViewById(R.id.textViewTDLIDExpDate);
        TDLIDExpectedTime=(TextView)findViewById(R.id.textViewTDLIDExpTime);
        TDLIDDescription=(TextView)findViewById(R.id.textViewTDLIDDescription);
        TDLIDlistView=(ListView)findViewById(R.id.listMeasurableTDLID);
        TDLIDComplete =(Button)findViewById(R.id.buttonTDLIDComplete);
        TDLIDDisplayTimeShares =(Button)findViewById(R.id.buttonTDLIDDisplayTimeShares);
        TDLIDProcessing =(Button)findViewById(R.id.buttonTDLIDProcessing);

        TDLIDMeasurableLabel=(TextView)findViewById(R.id.textViewTDLIDMeasurableLabel);

            // Getting Details From Delegated Task
            taskDelegateListItemDetails  = (TaskDataModel) getIntent().getSerializableExtra("TaskDelegatedItemDetails");
            delegatedMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("TaskDelegatedMeasurableList");

            // Getting Details From Accepted Task
            taskAcceptedItemDetails =  (TaskDataModel) getIntent().getSerializableExtra("TaskAcceptedItemDetails");

            // Getting Details From Processing Task
            taskProcessingItemDetails = (TaskDataModel) getIntent().getSerializableExtra("TaskProcessingItemDetails");
            processingMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("TaskProcessingMeasurableDetails");

            // Getting Details From Sender Approval Task
            taskSenderApprovalItemDetails = (TaskDataModel) getIntent().getSerializableExtra("senderTaskApprovalItemDetails");
            senderApprovalMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("senderTaskApprovalMeasurableList");

            // Getting Details From Receiver Approval Task
            taskReceiverApprovalItemDetails = (TaskDataModel) getIntent().getSerializableExtra("receiverTaskApprovalItemDetails");
            receiverApprovalMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("receiverTaskApprovalMeasurableList");

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

            if (taskDelegateListItemDetails.getStatus().equals("COMPLETED")) { TDLIDComplete.setVisibility(View.INVISIBLE); }

        }
        else if (taskAcceptedItemDetails!=null)
        {
            TDLIDDate.setText(taskAcceptedItemDetails.getDeligationDateTime());
            TDLIDUserName.setText(taskAcceptedItemDetails.getTaskDeligateOwnerUserID());
            TDLIDReceivedUserName.setText("From,  " + taskAcceptedItemDetails.getTaskDeligateOwnerUserID());
            TDLIDActivityName.setText(taskAcceptedItemDetails.getActivityName());
            TDLIDTaskName.setText(taskAcceptedItemDetails.getTaskName());
            TDLIDProjCode.setText(taskAcceptedItemDetails.getProjectNo());
            TDLIDProjName.setText(taskAcceptedItemDetails.getProjectName());
            TDLIDExpectedDate.setText(taskAcceptedItemDetails.getExpectedDate());
            TDLIDExpectedTime.setText(taskAcceptedItemDetails.getExpectedTotalTime());
            TDLIDDescription.setText(taskAcceptedItemDetails.getDescription());

            TDLIDMeasurableLabel.setVisibility(View.INVISIBLE);
            TDLIDlistView.setVisibility(View.INVISIBLE);
            TDLIDComplete.setVisibility(View.INVISIBLE);
            TDLIDDisplayTimeShares.setVisibility(View.INVISIBLE);
        }
        else if (taskProcessingItemDetails!=null)
         {
            TDLIDDate.setText(taskProcessingItemDetails.getDeligationDateTime());
            TDLIDUserName.setText(taskProcessingItemDetails.getTaskDeligateOwnerUserID());
            TDLIDReceivedUserName.setText("From,  " + taskProcessingItemDetails.getTaskDeligateOwnerUserID());
            TDLIDActivityName.setText(taskProcessingItemDetails.getActivityName());
            TDLIDTaskName.setText(taskProcessingItemDetails.getTaskName());
            TDLIDProjCode.setText(taskProcessingItemDetails.getProjectNo());
            TDLIDProjName.setText(taskProcessingItemDetails.getProjectName());
            TDLIDExpectedDate.setText(taskProcessingItemDetails.getExpectedDate());
            TDLIDExpectedTime.setText(taskProcessingItemDetails.getExpectedTotalTime());
            TDLIDDescription.setText(taskProcessingItemDetails.getDescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(processingMeasurableList, getApplicationContext());
            TDLIDlistView.setAdapter(measurableListCustomAdapter);

            TDLIDComplete.setText("Approve");
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
        else
        {
            TDLIDDate.setText(taskReceiverApprovalItemDetails.getDeligationDateTime());
            TDLIDUserName.setText(taskReceiverApprovalItemDetails.getTaskDeligateOwnerUserID());
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
                    if (taskDelegateListItemDetails.getCompletedOn().equals("NO_COMPLETE")) {
                        if (InternetConnectivity.isConnected()) {
                            result = updateCompletedStatus(taskDelegateListItemDetails.getId());
                            if (result) {
                                Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Task Completed", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show(); }
                    }
                }
                else if (taskProcessingItemDetails != null)
                {
                    if (taskProcessingItemDetails.getCompletedOn().equals("NO_COMPLETE")) {
                        if (InternetConnectivity.isConnected()) {
                            result = updateApprovalCompletionStatus(taskProcessingItemDetails.getId());
                            if (result) {
                                Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Task Send For Approval Completion", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else if (taskSenderApprovalItemDetails != null)
                {
                    if (taskSenderApprovalItemDetails.getCompletedOn().equals("NO_COMPLETE")) {
                        if (InternetConnectivity.isConnected()) {
                            result = updateCompletedStatus(taskSenderApprovalItemDetails.getId());
                            if (result) {
                                Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Task Completed", Toast.LENGTH_LONG).show();
                                finish();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                        }
                    }
                }
                else
                {
                    if (InternetConnectivity.isConnected()) {
                        result = updateCompletedStatus(taskReceiverApprovalItemDetails.getId());
                        if (result) {
                            Toast.makeText(TTSTaskDelegateListItemDetailsActivity.this, "Task Completed", Toast.LENGTH_LONG).show();
                            finish();
                        }
                    } else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show(); }
                }
            });

            TDLIDDisplayTimeShares.setOnClickListener(v -> {
                if (taskDelegateListItemDetails != null)
                {
                    Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);
                    i.putExtra("TaskDelegatedDetails", taskDelegateListItemDetails);
                    startActivity(i);
                }
                else if (taskProcessingItemDetails != null)
                {
                    Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);
                    i.putExtra("TaskProcessingDetails", taskProcessingItemDetails);
                    startActivity(i);
                }
                else if (taskSenderApprovalItemDetails != null)
                {
                    Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);
                    i.putExtra("TaskSenderApprovalDetails", taskSenderApprovalItemDetails);
                    startActivity(i);
                }
                else
                {
                    Intent i = new Intent(getApplicationContext(), TTSTimeShareListActivity.class);
                    i.putExtra("TaskReceiverApprovalDetails", taskReceiverApprovalItemDetails);
                    startActivity(i);
                }
            });


            TDLIDProcessing.setOnClickListener(v -> {
                if (InternetConnectivity.isConnected())
                {
                    result =updateProcessingTimeStatus(taskAcceptedItemDetails.getId());
                    if (result)
                    {
                        Toast.makeText(getApplicationContext(), "You Have Start Working on Task", Toast.LENGTH_LONG).show();
                        finish();
                    }
                }
            });

    }

//    @Override
//    public void onBackPressed() { finish(); }


    public boolean updateCompletedStatus(Long taskId){

        Connection con;
        int x = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            Calendar calendar = Calendar.getInstance();
            Timestamp completeTimestamp = new Timestamp(calendar.getTime().getTime());

            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET STATUS =?,COMPLETION_ON=? WHERE ID = ?");

            ps.setString(1, "COMPLETED");
            ps.setString(2, completeTimestamp.toString());
            ps.setLong(3,taskId);
            x=ps.executeUpdate();

            if(x==1){ result = true; }

            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return result;

    }

    public boolean updateApprovalCompletionStatus(Long taskId){

        Connection con;
        int x = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            Calendar calendar = Calendar.getInstance();
            Timestamp approveTimestamp = new Timestamp(calendar.getTime().getTime());

            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET STATUS =?,APPROVAL_ON =? WHERE ID = ?");

            ps.setString(1, "APPROVAL");
            ps.setString(2, approveTimestamp.toString());
            ps.setLong(3,taskId);
            x=ps.executeUpdate();

            if(x==1){ result = true; }

            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return result;

    }



    // Update the Time and status when Processing the Task
    public boolean updateProcessingTimeStatus(Long taskId){
        Connection con;
        int x = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            Calendar calendar = Calendar.getInstance();
            Timestamp processTimestamp = new Timestamp(calendar.getTime().getTime());

            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET STATUS =?, PROCESSING_ON =? WHERE ID =?");

            ps.setString(1, "IN_PROCESS");
            ps.setString(2, processTimestamp.toString());
            ps.setLong(3,taskId);
            x=ps.executeUpdate();

            if(x==1){ result = true; }

            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }


}
