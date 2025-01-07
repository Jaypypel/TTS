package com.example.neptune.ttsapp;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TTSTaskModificationListItemDetailsActivity extends AppCompatActivity {

    private TextView TMLIDDate,TMLIDActivityName,TMLIDTaskName,TMLIDProjCode,TMLIDProjName,TMLIDExpectedDate,TMLIDExpectedTime,TMLIDDescription,TMLIDModificationDescription,TMLIDUserName,TMLIDReceivedUserName,TMLIDMeasurableLabel;

    private Button TMLIDReAssign;

    private ListView TMLIDlistView;

    private TaskDataModel senderTaskModificationListItemDetails,receiverTaskModificationListItemDetails;
    private ArrayList<MeasurableListDataModel> senderModificationMeasurableList,receiverModificationMeasurableList;

    private static MeasurableListCustomAdapter measurableListCustomAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ttstask_modification_list_item_details);

        TMLIDDate=findViewById(R.id.textViewTMLIDDate);
        TMLIDUserName=findViewById(R.id.textViewTMLIDUser);
        TMLIDReceivedUserName =findViewById(R.id.textViewTMLIDReceivedUser);
        TMLIDActivityName=findViewById(R.id.textViewTMLIDActName);
        TMLIDTaskName=findViewById(R.id.textViewTMLIDTaskName);
        TMLIDProjCode=findViewById(R.id.textViewTMLIDProjNo);
        TMLIDProjName=findViewById(R.id.textViewTMLIDProjName);
        TMLIDExpectedDate=findViewById(R.id.textViewTMLIDExpDate);
        TMLIDExpectedTime=findViewById(R.id.textViewTMLIDExpTime);
        TMLIDDescription=findViewById(R.id.textViewTMLIDDescription);
        TMLIDModificationDescription=findViewById(R.id.textViewTMLIDModificationDescription);
        TMLIDlistView=findViewById(R.id.listMeasurableTMLID);
//        TMLIDReAssign =(Button)findViewById(R.id.buttonTMLIDGotoTaskManagement);



        // Getting Task Details From Sender Modification List
        senderTaskModificationListItemDetails = (TaskDataModel) getIntent().getSerializableExtra("senderTaskModificationItemDetails");
        senderModificationMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("senderTaskModificationMeasurableList");

        // Getting Task Details From Receiver Modification List
        receiverTaskModificationListItemDetails = (TaskDataModel) getIntent().getSerializableExtra("receiverTaskModificationItemDetails");
        receiverModificationMeasurableList = (ArrayList<MeasurableListDataModel>) getIntent().getSerializableExtra("receiverTaskModificationMeasurableList");


        if(senderTaskModificationListItemDetails!=null)
        {
            TMLIDDate.setText(senderTaskModificationListItemDetails.getTaskAssignedOn());
            TMLIDUserName.setText(senderTaskModificationListItemDetails.getTaskOwnerUserID());
            TMLIDReceivedUserName.setText("To,  " + senderTaskModificationListItemDetails.getTaskOwnerUserID());
            TMLIDActivityName.setText(senderTaskModificationListItemDetails.getActivityName());
            TMLIDTaskName.setText(senderTaskModificationListItemDetails.getTaskName());
            TMLIDProjCode.setText(senderTaskModificationListItemDetails.getProjectCode());
            TMLIDProjName.setText(senderTaskModificationListItemDetails.getProjectName());
            TMLIDExpectedDate.setText(senderTaskModificationListItemDetails.getExpectedDate());
            TMLIDExpectedTime.setText(senderTaskModificationListItemDetails.getExpectedTotalTime());
            TMLIDDescription.setText(senderTaskModificationListItemDetails.getDescription());
            TMLIDModificationDescription.setText(senderTaskModificationListItemDetails.getModificationdescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(senderModificationMeasurableList, getApplicationContext());
            TMLIDlistView.setAdapter(measurableListCustomAdapter);

//            TMLIDReAssign.setVisibility(View.INVISIBLE);
        }
        else
        {
            TMLIDDate.setText(receiverTaskModificationListItemDetails.getTaskAssignedOn());
            TMLIDUserName.setText(receiverTaskModificationListItemDetails.getTaskOwnerUserID());
            TMLIDReceivedUserName.setText("From,  " + receiverTaskModificationListItemDetails.getTaskOwnerUserID());
            TMLIDActivityName.setText(receiverTaskModificationListItemDetails.getActivityName());
            TMLIDTaskName.setText(receiverTaskModificationListItemDetails.getTaskName());
            TMLIDProjCode.setText(receiverTaskModificationListItemDetails.getProjectCode());
            TMLIDProjName.setText(receiverTaskModificationListItemDetails.getProjectName());
            TMLIDExpectedDate.setText(receiverTaskModificationListItemDetails.getExpectedDate());
            TMLIDExpectedTime.setText(receiverTaskModificationListItemDetails.getExpectedTotalTime());
            TMLIDDescription.setText(receiverTaskModificationListItemDetails.getDescription());
            TMLIDModificationDescription.setText(receiverTaskModificationListItemDetails.getModificationdescription());
            measurableListCustomAdapter = new MeasurableListCustomAdapter(receiverModificationMeasurableList, getApplicationContext());
            TMLIDlistView.setAdapter(measurableListCustomAdapter);
        }


//        TMLIDReAssign.setOnClickListener(v ->
//        {
//
//        });

    }
}
