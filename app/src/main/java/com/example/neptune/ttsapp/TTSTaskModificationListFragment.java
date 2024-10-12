package com.example.neptune.ttsapp;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class TTSTaskModificationListFragment extends Fragment {

    public TTSTaskModificationListFragment() { }

    private TextView user,date,time;

    private ListView senderModificationTaskList,receiverModificationTaskList;

    private String userId;

    private SessionManager sessionManager;

    private static TaskAllocatedListCustomAdapter adapter;

    private ArrayList<TaskDataModel> senderDataModels,receiverDataModels;



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsmodification_task_list, container, false);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=(TextView)view.findViewById(R.id.textViewModificationListUser);
        user.setText(userId);

        date=(TextView)view.findViewById(R.id.textViewModificationListDate);
        time=(TextView)view.findViewById(R.id.textViewModificationListTime);

        senderModificationTaskList=(ListView)view.findViewById(R.id.senderModificationTaskList);
        receiverModificationTaskList=(ListView)view.findViewById(R.id.receiverModificationTaskList);


        final Handler someHandler = new Handler(Looper.getMainLooper());
        someHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date1 = new Date();
                String currentDate = formatter.format(date1);
                date.setText("Date :  " +currentDate);

                SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
                Date time1 = new Date();
                String currentTime = timeFormatter.format(time1);
                time.setText("Time :  " +currentTime);

                someHandler.postDelayed(this, 1000);
            }
        }, 10);


        //Get Data From Database for Modification Task And set to the ListView
        if (InternetConnectivity.isConnected()) {
            senderDataModels = getSendModificationTaskList(getUserId(),"REVISION");
            adapter= new TaskAllocatedListCustomAdapter(senderDataModels,getActivity().getApplicationContext());
            senderModificationTaskList.setAdapter(adapter);

            receiverDataModels = getReceiveModificationTaskList(getUserId(),"REVISION");
            adapter= new TaskAllocatedListCustomAdapter(receiverDataModels,getActivity().getApplicationContext());
            receiverModificationTaskList.setAdapter(adapter);

        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        senderModificationTaskList.setOnItemClickListener((parent, view1, position, id) -> {

            TaskDataModel dataModel= senderDataModels.get(position);

            Intent i = new Intent(getActivity(), TTSTaskModificationListItemDetailsActivity.class);

            i.putExtra("senderTaskModificationItemDetails",dataModel);
            i.putExtra("senderTaskModificationMeasurableList",getModificationTaskMeasurableList(dataModel.getId()));
            startActivity(i);
        });

        receiverModificationTaskList.setOnItemClickListener((parent, view1, position, id) -> {

            TaskDataModel dataModel= receiverDataModels.get(position);

            Intent i = new Intent(getActivity(), TTSTaskModificationListItemDetailsActivity.class);

            i.putExtra("receiverTaskModificationItemDetails",dataModel);
            i.putExtra("receiverTaskModificationMeasurableList",getModificationTaskMeasurableList(dataModel.getId()));
            startActivity(i);
        });

        return view;
    }

    private String getUserId()
    {
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        return sessionManager.getUserID();
    }

    // Getting Send Modification Task List
    public ArrayList<TaskDataModel> getSendModificationTaskList(String senderUserID, String status){

        ArrayList<TaskDataModel> taskList = new ArrayList();
        TaskDataModel listDataModel;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from TASK_MANAGEMENT where FK_AUTHENTICATION_OWNER_USER_ID=? and STATUS=?");
            ps.setString(1, senderUserID);
            ps.setString(2,status);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                listDataModel = new TaskDataModel();


                listDataModel.setId(rs.getLong("ID"));
                listDataModel.setTaskDeligateOwnerUserID(rs.getString("FK_AUTHENTICATION_OWNER_USER_ID"));
                listDataModel.setActivityName(rs.getString("ACTIVITY_NAME"));
                listDataModel.setTaskName(rs.getString("TASK_NAME"));
                listDataModel.setProjectNo(rs.getString("PROJECT_ID"));
                listDataModel.setProjectName(rs.getString("PROJECT_NAME"));
                listDataModel.setExpectedDate(rs.getString("EXPECTED_DATE"));
                listDataModel.setExpectedTotalTime(rs.getString("EXPECTED_TOTAL_TIME"));
                listDataModel.setDescription(rs.getString("DESCRIPTION"));
                listDataModel.setModificationdescription(rs.getString("MODIFICATION_DESCRIPTION"));
                listDataModel.setActualTotalTime(rs.getString("ACTUAL_TOTAL_TIME"));
                listDataModel.setDeligationDateTime(rs.getTimestamp("DELEGATION_ON").toString());
                listDataModel.setSeenOn(rs.getString("SEEN_ON"));
                listDataModel.setAcceptedOn(rs.getString("ACCEPTED_ON"));
                listDataModel.setStatus(rs.getString("STATUS"));


                taskList.add(listDataModel);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return taskList;

    }



    // Getting Receive Modification Task List
    public ArrayList<TaskDataModel> getReceiveModificationTaskList(String receiverUserID, String status){

        ArrayList<TaskDataModel> taskList = new ArrayList();
        TaskDataModel listDataModel;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from TASK_MANAGEMENT where FK_AUTHENTICATION_RECEIVED_USER_ID=? and STATUS=?");
            ps.setString(1, receiverUserID);
            ps.setString(2,status);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                listDataModel = new TaskDataModel();


                listDataModel.setId(rs.getLong("ID"));
                listDataModel.setTaskDeligateOwnerUserID(rs.getString("FK_AUTHENTICATION_OWNER_USER_ID"));
                listDataModel.setActivityName(rs.getString("ACTIVITY_NAME"));
                listDataModel.setTaskName(rs.getString("TASK_NAME"));
                listDataModel.setProjectNo(rs.getString("PROJECT_ID"));
                listDataModel.setProjectName(rs.getString("PROJECT_NAME"));
                listDataModel.setExpectedDate(rs.getString("EXPECTED_DATE"));
                listDataModel.setExpectedTotalTime(rs.getString("EXPECTED_TOTAL_TIME"));
                listDataModel.setDescription(rs.getString("DESCRIPTION"));
                listDataModel.setModificationdescription(rs.getString("MODIFICATION_DESCRIPTION"));
                listDataModel.setActualTotalTime(rs.getString("ACTUAL_TOTAL_TIME"));
                listDataModel.setDeligationDateTime(rs.getTimestamp("DELEGATION_ON").toString());
                listDataModel.setSeenOn(rs.getString("SEEN_ON"));
                listDataModel.setAcceptedOn(rs.getString("ACCEPTED_ON"));
                listDataModel.setStatus(rs.getString("STATUS"));


                taskList.add(listDataModel);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return taskList;

    }

    // Getting Measurables List
    public ArrayList<MeasurableListDataModel> getModificationTaskMeasurableList(Long taskId){

        ArrayList<MeasurableListDataModel> measurableList = new ArrayList();
        MeasurableListDataModel measurableListDataModel;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select m.ID,m.NAME from MEASURABLES m where ID = ANY(select FK_MEASURABLE_ID from DELEGATION_MEASURABLES where FK_TASK_MANAGEMENT_ID = ?)");
            ps.setLong(1, taskId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                measurableListDataModel= new MeasurableListDataModel();

                measurableListDataModel.setId(rs.getString("ID"));
                measurableListDataModel.setMeasurableName(rs.getString("NAME"));

                measurableList.add(measurableListDataModel);


            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return measurableList;

    }


}
