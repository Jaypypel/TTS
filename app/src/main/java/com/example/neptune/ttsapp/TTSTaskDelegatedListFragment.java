package com.example.neptune.ttsapp;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


public class TTSTaskDelegatedListFragment extends Fragment {


    public TTSTaskDelegatedListFragment() { }

    private ArrayList<TaskDataModel> dataModels;

    private ListView listView;

    private TextView user,date,time;
    private String userId;

    private static TaskDelegatedListCustomAdapter adapter;
    private SessionManager sessionManager;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ttstask_delegated_list, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        listView=(ListView)view.findViewById(R.id.listTaskDelegated);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=(TextView)view.findViewById(R.id.textViewDelegatedListUser);
        user.setText(userId);

        date=(TextView)view.findViewById(R.id.textViewDelegatedListDate);
        time=(TextView)view.findViewById(R.id.textViewDelegatedListTime);

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



        // Getting Delegated Task List and set to the ListView
        if (InternetConnectivity.isConnected()) {

            dataModels = getDelegatedTaskList(getUserId());
            adapter= new TaskDelegatedListCustomAdapter(dataModels,getActivity().getApplicationContext());
            listView.setAdapter(adapter);

        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        listView.setOnItemClickListener((parent, view1, position, id) -> {

            TaskDataModel dataModel= dataModels.get(position);

            Intent i = new Intent(getActivity(), TTSTaskDelegateListItemDetailsActivity.class);

            i.putExtra("TaskDelegatedItemDetails",dataModel);
            i.putExtra("TaskDelegatedMeasurableList",getAllocatedMeasurableList(dataModel.getId()));

            startActivity(i);


        });


        return view;
    }

    private String getUserId()
    {
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        return sessionManager.getUserID();
    }

    // Getting Measurables List
    public ArrayList<MeasurableListDataModel> getAllocatedMeasurableList(Long taskId){

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


    // Getting Delegated Task List
    public ArrayList<TaskDataModel> getDelegatedTaskList(String ownerUserID){

        ArrayList<TaskDataModel> taskList = new ArrayList();
        TaskDataModel listDataModel;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from TASK_MANAGEMENT where FK_AUTHENTICATION_OWNER_USER_ID=?");
            ps.setString(1, ownerUserID);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                listDataModel = new TaskDataModel();

                listDataModel.setId(rs.getLong("ID"));
                listDataModel.setTaskDeligateOwnerUserID(rs.getString("FK_AUTHENTICATION_OWNER_USER_ID"));
                listDataModel.setTaskReceivedUserId(rs.getString("FK_AUTHENTICATION_RECEIVED_USER_ID"));
                listDataModel.setActivityName(rs.getString("ACTIVITY_NAME"));
                listDataModel.setTaskName(rs.getString("TASK_NAME"));
                listDataModel.setProjectNo(rs.getString("PROJECT_ID"));
                listDataModel.setProjectName(rs.getString("PROJECT_NAME"));
                listDataModel.setExpectedDate(rs.getString("EXPECTED_DATE"));
                listDataModel.setExpectedTotalTime(rs.getString("EXPECTED_TOTAL_TIME"));
                listDataModel.setDescription(rs.getString("DESCRIPTION"));
                listDataModel.setActualTotalTime(rs.getString("ACTUAL_TOTAL_TIME"));
                listDataModel.setDeligationDateTime(rs.getTimestamp("DELEGATION_ON").toString());
                listDataModel.setCompletedOn(rs.getString("COMPLETION_ON"));
                listDataModel.setStatus(rs.getString("STATUS"));

                taskList.add(listDataModel);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return taskList;

    }

}
