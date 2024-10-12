package com.example.neptune.ttsapp;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import androidx.fragment.app.Fragment;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class TTSTaskAcceptedListFragment extends Fragment {

    public TTSTaskAcceptedListFragment() { }

    private SessionManager sessionManager;

    private ArrayList<TaskDataModel> dataModels;

    private ListView listView;

    private TextView user,date,time;
    private String userId;

    private static TaskAllocatedListCustomAdapter adapter;

    boolean result=false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
         View view = inflater.inflate(R.layout.fragment_ttstask_accepted_list, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        listView=(ListView)view.findViewById(R.id.listAccepted);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=(TextView)view.findViewById(R.id.textViewAcceptedListUser);
        user.setText(userId);

        date=(TextView)view.findViewById(R.id.textViewAcceptedListDate);
        time=(TextView)view.findViewById(R.id.textViewAcceptedListTime);

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


        //Get Data From Database for Accepted Task And set to the ListView
        if (InternetConnectivity.isConnected()) {
        dataModels = getAcceptedTaskList(getUserId(),"ACCEPTED");
        adapter= new TaskAllocatedListCustomAdapter(dataModels,getActivity().getApplicationContext());
        listView.setAdapter(adapter);
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        listView.setOnItemClickListener((parent, view1, position, id) -> {

            TaskDataModel dataModel= dataModels.get(position);

            Intent i = new Intent(getActivity(), TTSTaskDelegateListItemDetailsActivity.class);

            i.putExtra("TaskAcceptedItemDetails",dataModel);

            startActivity(i);

        });

//        listView.setOnItemLongClickListener((parent, view12, position, id) -> {
//            dataModel= dataModels.get(position);
//            new AlertDialog.Builder(getActivity())
//                    .setIcon(android.R.drawable.ic_dialog_alert)
//                    .setTitle("Task Complete")
//                    .setMessage(Html.fromHtml("<b>"+"Do You Want Complete The Task..?"+"</b>"))
//                    .setPositiveButton("Yes", (dialog, which) -> {
//                        result = updateCompletedStatus(dataModel.id);
//                        if (result)
//                        {
//                            Toast.makeText(getActivity().getApplicationContext(), "Task Completed", Toast.LENGTH_LONG).show();
//                        }
//
//                    })
//                    .setNegativeButton("No", null)
//                    .show();
//
//            return true;
//
//        });


         return view;
    }

    private String getUserId()
    {
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        return sessionManager.getUserID();
    }


    // Update Task status as COMPLETED
    public boolean updateCompletedStatus(Long taskId){
        Connection con;
        int x = 0;
        boolean result=false;

        try {
            con = DatabaseHelper.getDBConnection();

            Calendar calendar = Calendar.getInstance();
            Timestamp completeTimestamp = new Timestamp(calendar.getTime().getTime());

            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET STATUS =?,COMPLETION_ON=? WHERE ID = ?");

            ps.setString(1, "COMPLETED");
            ps.setString(2, completeTimestamp.toString());
            ps.setLong(3,taskId);
            x=ps.executeUpdate();

            if(x==1){
                result = true;
            }

            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;

    }

    // Getting Accepted Task List
    public ArrayList <TaskDataModel> getAcceptedTaskList(String receivedUserID, String status){

        ArrayList<TaskDataModel> taskList = new ArrayList();
        TaskDataModel listDataModel;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from TASK_MANAGEMENT where FK_AUTHENTICATION_RECEIVED_USER_ID=? and STATUS=?");
            ps.setString(1, receivedUserID);
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
