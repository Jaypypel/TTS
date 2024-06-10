package com.example.neptune.ttsapp;


import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class TTSTaskModificationActivity extends AppCompatActivity {

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

        prevUserName=(TextView)findViewById(R.id.textViewPreviewUser);
        prevDate=(TextView)findViewById(R.id.textViewDate);
        prevActivityName=(TextView)findViewById(R.id.textViewActName);
        prevTaskName=(TextView)findViewById(R.id.textViewTaskName);
        prevProjCode=(TextView)findViewById(R.id.textViewProjNo);
        prevProjName=(TextView)findViewById(R.id.textViewProjName);
        prevStartTime=(TextView)findViewById(R.id.textViewStartTime);
        prevEndTime=(TextView)findViewById(R.id.textViewEndTime);
        prevDescription=(TextView)findViewById(R.id.textViewDescription);
        listView=(ListView)findViewById(R.id.listMeasurablePreview);

        StartTimeLabel =(TextView)findViewById(R.id.textViewStartTimeLabel);
        EndTimeLabel =(TextView)findViewById(R.id.textViewEndTimeLabel);

        modificationDescription =(EditText)findViewById(R.id.editTextModificationDescription);

        submit = (Button) findViewById(R.id.buttonModifySubmit);


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
               prevProjCode.setText(modifyTaskDetails.getProjectNo());
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
                prevDate.setText(dtsListItemDetails.getTimeShareDate());
                prevActivityName.setText(dtsListItemDetails.getActivityName());
                prevTaskName.setText(dtsListItemDetails.getTaskName());
                prevProjCode.setText(dtsListItemDetails.getProjectNo());
                prevProjName.setText(dtsListItemDetails.getProjectName());
                prevStartTime.setText(dtsListItemDetails.getStartTime());
                prevEndTime.setText(dtsListItemDetails.getEndTime());
                prevDescription.setText(dtsListItemDetails.getTaskDescription());
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

            boolean result = updateModificationTaskStatusAndDescription(description,modifyTaskDetails.getId());

            if(result)
            {
                modificationDescription.setText("");
                Toast.makeText(getApplicationContext(), "Sending Modification Request Successfully", Toast.LENGTH_LONG).show();
            }

            }else { Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}
        });

    }

    @Override
    public void onBackPressed() { finish(); }

    // Update the status and Description
    public boolean updateModificationTaskStatusAndDescription(String description,Long taskId){
        Connection con;
        int x = 0;
        boolean result  = false;

        try {
            con = DatabaseHelper.getDBConnection();

            Calendar calendar = Calendar.getInstance();
            Timestamp acceptTimestamp = new Timestamp(calendar.getTime().getTime());

            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET STATUS =?, MODIFICATION_DESCRIPTION=? WHERE ID =?");

            ps.setString(1, "REVISION");
            ps.setString(2,description);
            ps.setLong(3,taskId);
            x=ps.executeUpdate();

            if(x==1){ result = true; }

            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return result;

    }


}
