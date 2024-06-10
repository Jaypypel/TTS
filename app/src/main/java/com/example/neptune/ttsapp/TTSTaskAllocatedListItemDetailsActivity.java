package com.example.neptune.ttsapp;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Calendar;

public class TTSTaskAllocatedListItemDetailsActivity extends AppCompatActivity {

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

        TALIDDate=(TextView)findViewById(R.id.textViewTALIDDate);
        TALIDUserName=(TextView)findViewById(R.id.textViewTALIDUser);
        TALIDActivityName=(TextView)findViewById(R.id.textViewTALIDActName);
        TALIDTaskName=(TextView)findViewById(R.id.textViewTALIDTaskName);
        TALIDProjCode=(TextView)findViewById(R.id.textViewTALIDProjNo);
        TALIDProjName=(TextView)findViewById(R.id.textViewTALIDProjName);
        TALIDExpectedDate=(TextView)findViewById(R.id.textViewTALIDExpDate);
        TALIDExpectedTime=(TextView)findViewById(R.id.textViewTALIDExpTime);
        TALIDDescription=(TextView)findViewById(R.id.textViewTALIDDescription);
        TALIDlistView=(ListView)findViewById(R.id.listMeasurableTALID);
        TALIDAccept =(Button)findViewById(R.id.buttonTALIDAccept);
        TALIDDisplayTimeShare =(Button)findViewById(R.id.buttonTALIDDisplayTimeShare);
        TALIDModify =(Button)findViewById(R.id.buttonTALIDModify);


        // Getting Details From Allocated Task
            allocatedTaskListItemDetails  = (TaskDataModel) getIntent().getSerializableExtra("TaskAllocatedListItemDetails");
            allocatedTaskMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("TaskAllocatedListMeasurableList");

        // Getting Details From Completed Task
            completedTaskListItemDetails  = (TaskDataModel) getIntent().getSerializableExtra("TaskCompletedListItemDetails");
            completedTaskMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("TaskCompletedListMeasurableList");


        if(allocatedTaskListItemDetails!=null)
        {

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

        else
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
                if(allocatedTaskListItemDetails.getAcceptedOn().equals("NO_ACCEPT")) {
                    if (InternetConnectivity.isConnected())
                    {
                        Log.d("Accept","YES");
                    result = updateAcceptTimeStatus(allocatedTaskListItemDetails.getId());
                    if (result) {
//                        Intent i = new Intent(getApplicationContext(), TTSTimeShareFormActivity.class);
//                        i.putExtra("TaskDetails",taskListItemDetails);
//                        startActivity(i);
                        Toast.makeText(TTSTaskAllocatedListItemDetailsActivity.this, "Task Accepted", Toast.LENGTH_LONG).show();
                        finish();
                    }
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


    // Update the Time and status when Accept the Task
    public boolean updateAcceptTimeStatus(Long taskId){
        Connection con;
        int x = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            Calendar calendar = Calendar.getInstance();
            Timestamp acceptTimestamp = new Timestamp(calendar.getTime().getTime());

            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET STATUS =?, ACCEPTED_ON =? WHERE ID =?");

            ps.setString(1,"ACCEPTED");
            ps.setString(2, acceptTimestamp.toString());
            ps.setLong(3,taskId);
            x=ps.executeUpdate();

            if(x==1){ result = true; }

            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return result;

    }




}
