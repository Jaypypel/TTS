package com.example.neptune.ttsapp;


import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
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


public class TTSDailyTimeShareListFragment extends Fragment {

    public TTSDailyTimeShareListFragment() { }

    private SessionManager sessionManager;

    private ListView listViewDailyTimeShares;

    private TextView user,date,time;

    private String userId;

    ArrayList<DailyTimeShareDataModel> dailyTimeShareDataList;

    private DailyTimeShareListCustomAdapter adapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_ttsdaily_time_share_list, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        listViewDailyTimeShares=(ListView)view.findViewById(R.id.listDailyTimeShare);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=(TextView)view.findViewById(R.id.textViewDailyTimeShareListUser);
        user.setText(userId);

        date =(TextView)view.findViewById(R.id.textViewDailyTimeShareListDate);
        time =(TextView)view.findViewById(R.id.textViewDailyTimeShareListTime);

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
            dailyTimeShareDataList = getDailyTimeShareList(userId,getTodayDate());
            adapter= new DailyTimeShareListCustomAdapter(dailyTimeShareDataList,getActivity().getApplicationContext());
            listViewDailyTimeShares.setAdapter(adapter);
        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

        if (InternetConnectivity.isConnected()) {
            listViewDailyTimeShares.setOnItemClickListener((parent, view1, position, id) -> {
                DailyTimeShareDataModel dataModel = dailyTimeShareDataList.get(position);

                ArrayList<MeasurableListDataModel> measurableList = getDTSMeasurableList(dataModel.getTimeShareId());

                Intent i = new Intent(getActivity(), TTSTaskModificationActivity.class);

                i.putExtra("DTSListItemDetails", dataModel);
                i.putExtra("DTSMeasurableList", measurableList);

                startActivity(i);
            });
        }


        return view;
    }

    private String getTodayDate()
    {
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Date date1 = new Date();
        return formatter.format(date1);
    }


    // Getting Accepted Task List
    public ArrayList<DailyTimeShareDataModel> getDailyTimeShareList(String userID, String date){

        ArrayList<DailyTimeShareDataModel> dailyTimeShareDataList = new ArrayList<>();
        DailyTimeShareDataModel dailyTimeShareData;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from DAILY_TIME_SHARE where FK_AUTHENTICATION_USER_ID=? and DATE_OF_TIME_SHARE=?");
            ps.setString(1, userID);
            ps.setString(2,date);


            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                dailyTimeShareData = new DailyTimeShareDataModel();


                dailyTimeShareData.setTimeShareId(rs.getLong("ID"));
                dailyTimeShareData.setTimeShareDate(rs.getString("DATE_OF_TIME_SHARE"));
                dailyTimeShareData.setProjectNo(rs.getString("PROJECT_CODE"));
                dailyTimeShareData.setProjectName(rs.getString("PROJECT_NAME"));
                dailyTimeShareData.setActivityName(rs.getString("ACTIVITY_NAME"));
                dailyTimeShareData.setTaskName(rs.getString("TASK_NAME"));
                dailyTimeShareData.setStartTime(rs.getString("START_TIME"));
                dailyTimeShareData.setEndTime(rs.getString("END_TIME"));
                dailyTimeShareData.setConsumedTime(rs.getString("CONSUMED_TIME"));
                dailyTimeShareData.setTaskDescription(rs.getString("DESCRIPTION"));

                dailyTimeShareDataList.add(dailyTimeShareData);

            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return dailyTimeShareDataList;

    }

    // Getting Measurables List
    public ArrayList<MeasurableListDataModel> getDTSMeasurableList(Long dtsId){

        ArrayList<MeasurableListDataModel> measurableList = new ArrayList();
        MeasurableListDataModel measurableListDataModel;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select m.ID,m.NAME from MEASURABLES m where ID = ANY(select FK_MEASURABLE_ID from DAILY_TIME_SHARE_MEASURABLE where FK_TIME_SHARE_ID = ?)");
            ps.setLong(1, dtsId);

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
        } catch (Exception e) { e.printStackTrace(); }

        return measurableList;

    }
}
