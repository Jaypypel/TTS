package com.example.neptune.ttsapp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
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


public class TTSTaskCountFragment extends Fragment {

    public TTSTaskCountFragment() { }

    private TextView tvPendingTask,tvAcceptedTask,tvCompletedTask,tvApprovalTask,user,date,time;
    private SessionManager sessionManager;
    private ListView listView;

    private static TaskAllocatedListCustomAdapter adapter;
    private ArrayList<TaskDataModel> dataModels;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {   // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttstask_count, container, false);

        tvPendingTask = (TextView) view.findViewById(R.id.textViewPendingTask);
        tvAcceptedTask = (TextView) view.findViewById(R.id.textViewAcceptedTask);
        tvCompletedTask = (TextView) view.findViewById(R.id.textViewCompletedTask);
        tvApprovalTask = (TextView) view.findViewById(R.id.textViewApprovalTask);

        user = (TextView) view.findViewById(R.id.textViewTaskCountUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date = (TextView) view.findViewById(R.id.textViewTaskCountDate);
        time = (TextView) view.findViewById(R.id.textViewTaskCountTime);

        listView = (ListView)view.findViewById(R.id.taskList);

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



        if (InternetConnectivity.isConnected()) {
            try {
                user.setText(sessionManager.getUserID());

                String pendingTaskCount = Integer.toString(getPendingTaskCount(sessionManager.getUserID()));
                tvPendingTask.setText("Pending Task      :  " + pendingTaskCount);

                String acceptedTaskCount = Integer.toString(getAcceptedTaskCount(sessionManager.getUserID()));
                tvAcceptedTask.setText("In Process Task   :  " + acceptedTaskCount);

                String approvalTaskCount = Integer.toString(getApprovalTaskCount(sessionManager.getUserID()));
                tvApprovalTask.setText("Approval Task    :  " + approvalTaskCount);

                String completedTaskCount = Integer.toString(getCompletedTaskCount(sessionManager.getUserID()));
                tvCompletedTask.setText("Completed Task   :  " + completedTaskCount);

            } catch (Exception e) { e.printStackTrace(); }

        } else { Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_LONG).show(); }


        //Get Data From Database for Accepted Task And set to the ListView
        if (InternetConnectivity.isConnected()) {
            dataModels = getTaskList(sessionManager.getUserID());
            adapter= new TaskAllocatedListCustomAdapter(dataModels,getActivity().getApplicationContext());
            listView.setAdapter(adapter);
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}




        return view;
    }



    // Getting Pending Task Count
    public int getPendingTaskCount(String userId){

        int count = 0;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM TASK_MANAGEMENT WHERE FK_AUTHENTICATION_RECEIVED_USER_ID= ? AND STATUS = ? ");
            ps.setString(1, userId);
            ps.setString(2, "PENDING");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;

    }

    // Getting Accepted Task Count
    public int getAcceptedTaskCount(String userId){

        int count = 0;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM TASK_MANAGEMENT WHERE FK_AUTHENTICATION_RECEIVED_USER_ID= ? AND STATUS = ? ");
            ps.setString(1, userId);
            ps.setString(2, "IN_PROCESS");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return count;

    }

    // Getting Approval Task Count
    public int getApprovalTaskCount(String userId){

        int count = 0;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM TASK_MANAGEMENT WHERE FK_AUTHENTICATION_RECEIVED_USER_ID= ? AND STATUS = ? ");
            ps.setString(1, userId);
            ps.setString(2, "APPROVAL");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;

    }

    // Getting Completed Task Count
    public int getCompletedTaskCount(String userId){

        int count = 0;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("SELECT COUNT(*) FROM TASK_MANAGEMENT WHERE FK_AUTHENTICATION_RECEIVED_USER_ID= ? AND STATUS = ? ");
            ps.setString(1, userId);
            ps.setString(2, "COMPLETED");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return count;

    }

    // Getting  Task List
    public ArrayList<TaskDataModel> getTaskList(String receivedUserID){

        ArrayList<TaskDataModel> taskList = new ArrayList();
        TaskDataModel listDataModel;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from TASK_MANAGEMENT where FK_AUTHENTICATION_RECEIVED_USER_ID=? ");
            ps.setString(1, receivedUserID);


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
}
