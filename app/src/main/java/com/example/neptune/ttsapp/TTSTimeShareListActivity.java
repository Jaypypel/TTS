package com.example.neptune.ttsapp;

import android.content.Intent;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

public class TTSTimeShareListActivity extends AppCompatActivity {

    private ArrayList<TimeShareDataModel> dataModels;

    private ListView listView;
    private Button gotoTimeshare;

    private static TimeShareListCustomAdapter adapter;

    private TaskDataModel taskAcceptedDetails,taskCompletedDetails,taskDelegatedDetails,
            taskProcessingDetails, taskSenderApprovalItemDetails,taskReceiverApprovalItemDetails;

    private Long taskId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttstime_share_list);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        listView=(ListView)findViewById(R.id.timeShareList);
        gotoTimeshare=(Button)findViewById(R.id.buttonGotoTimeshare);

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

        if (taskAcceptedDetails!=null)
        {
            if (InternetConnectivity.isConnected()) {
                taskId = taskAcceptedDetails.getId();
                dataModels = getTimeShareList(taskId);
                adapter= new TimeShareListCustomAdapter(dataModels,getApplicationContext());
                listView.setAdapter(adapter);
            }else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        }
        else if (taskCompletedDetails!=null)
        {
            if (InternetConnectivity.isConnected()) {
                taskId = taskCompletedDetails.getId();
                dataModels = getTimeShareList(taskId);
                adapter= new TimeShareListCustomAdapter(dataModels,getApplicationContext());
                listView.setAdapter(adapter);
                gotoTimeshare.setVisibility(View.INVISIBLE);

            }else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}
        }
        else if (taskDelegatedDetails!=null)
        {
            if (InternetConnectivity.isConnected()) {
                taskId = taskDelegatedDetails.getId();
                dataModels = getTimeShareList(taskId);
                adapter= new TimeShareListCustomAdapter(dataModels,getApplicationContext());
                listView.setAdapter(adapter);
                gotoTimeshare.setVisibility(View.INVISIBLE);

            }else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}
        }
        else if (taskProcessingDetails!=null)
        {
            if (InternetConnectivity.isConnected()) {
                taskId = taskProcessingDetails.getId();
                dataModels = getTimeShareList(taskId);
                adapter= new TimeShareListCustomAdapter(dataModels,getApplicationContext());
                listView.setAdapter(adapter);

            }else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}
        }
        else if (taskSenderApprovalItemDetails!=null)
        {
            if (InternetConnectivity.isConnected()) {
                taskId = taskSenderApprovalItemDetails.getId();
                dataModels = getTimeShareList(taskId);
                adapter= new TimeShareListCustomAdapter(dataModels,getApplicationContext());
                listView.setAdapter(adapter);
                gotoTimeshare.setVisibility(View.INVISIBLE);

            }else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}
        }
        else
        {
            if (InternetConnectivity.isConnected()) {
                taskId = taskReceiverApprovalItemDetails.getId();
                dataModels = getTimeShareList(taskId);
                adapter= new TimeShareListCustomAdapter(dataModels,getApplicationContext());
                listView.setAdapter(adapter);
                gotoTimeshare.setVisibility(View.INVISIBLE);

            }else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}
        }

        gotoTimeshare.setOnClickListener(v -> {
            if (taskAcceptedDetails!=null) {
                Intent i = new Intent(getApplicationContext(), TTSTimeShareFormActivity.class);
                i.putExtra("TaskAcceptedDetails", taskAcceptedDetails);
                startActivity(i);
                finish();
            }
            else
            {
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
    public void onBackPressed() { finish(); }

    // Getting Accepted Task List
    public ArrayList <TimeShareDataModel> getTimeShareList(Long taskId){

        ArrayList<TimeShareDataModel> timeShareList = new ArrayList();
        TimeShareDataModel listDataModel;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("SELECT TIME_SHARE.* FROM TASK_MANAGEMENT" +
                    " RIGHT OUTER JOIN TIME_SHARE ON TASK_MANAGEMENT.ID = TIME_SHARE.FK_TASK_MANAGEMENT_ID WHERE TASK_MANAGEMENT.ID = ?");
            ps.setLong(1, taskId);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                listDataModel = new TimeShareDataModel();


                listDataModel.setTimeShareDate(rs.getString("DATE_OF_TIME_SHARE"));
                listDataModel.setStartTime(rs.getString("START_TIME"));
                listDataModel.setEndTime(rs.getString("END_TIME"));
                listDataModel.setTimeDifference(rs.getString("TIME_DIFFERENCE"));
                listDataModel.setTimeShareDescription(rs.getString("DESCRIPTION"));


                timeShareList.add(listDataModel);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return timeShareList;

    }
}
