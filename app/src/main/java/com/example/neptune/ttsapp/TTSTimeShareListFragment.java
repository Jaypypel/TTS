package com.example.neptune.ttsapp;


import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class TTSTimeShareListFragment extends Fragment {

    public TTSTimeShareListFragment() { }

    private TextView user,date;
    private EditText startDate,endDate;
    private Button getTimeShares;
    private ListView timeShareList;

    private SessionManager sessionManager;

    private int mYear, mMonth, mDay;

    private String userId;

    private static TimeShareListCustomAdapter adapter;

    private ArrayList<TimeShareDataModel> dataModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttstime_share_list, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getUserID();
        user=view.findViewById(R.id.textViewTimeShareListFragmentUser);
        user.setText(userId);

        date=view.findViewById(R.id.textViewTimeShareListFragmentDate);
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy   hh:mm a");
        Date date1 = new Date();
        String currentDate = formatter.format(date1);
        date.setText("Date & Time:  " +currentDate);

        startDate=view.findViewById(R.id.editTextStartDate);
        endDate=view.findViewById(R.id.editTextEndDate);
        getTimeShares=view.findViewById(R.id.buttonDisplayTimeShares);
        timeShareList=view.findViewById(R.id.timeShareList);


        getTimeShares.setOnClickListener(v -> {
            if (InternetConnectivity.isConnected())
            {
                dataModels = getTimeShareList(userId, getStartDate(), getEndDate());
                Log.d("TimeShareList",dataModels.toString());
                if (dataModels.isEmpty())
                {
                    Toast.makeText(getActivity(), "No Data Found", Toast.LENGTH_LONG).show();
                }
                else
                 {
                    adapter = new TimeShareListCustomAdapter(dataModels, getActivity());
                    timeShareList.setAdapter(adapter);
                    startDate.setText("");
                    endDate.setText("");
                    Toast.makeText(getActivity(), "Getting TimeShares Successfully", Toast.LENGTH_LONG).show();
                 }

            }
            else
            {
                Toast.makeText(getActivity(), "No Internet Connection", Toast.LENGTH_LONG).show();
            }
        });


        startDate.setFocusable(false);
        endDate.setFocusable(false);

        // Date Picker For Select Start Date
        startDate.setOnClickListener(v -> {
            //To show current date in the datepicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view1, int year, int month, int dayOfMonth) {
                    startDate.setText(dayOfMonth + "-" + (month+1)  + "-" + year);
                }
            },mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();
        });


        // Date Picker For Select End Date
        endDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //To show current date in the datepicker
                Calendar mcurrentDate=Calendar.getInstance();
                mYear=mcurrentDate.get(Calendar.YEAR);
                mMonth=mcurrentDate.get(Calendar.MONTH);
                mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        endDate.setText(dayOfMonth + "-" + (month+1)  + "-" + year);
                    }
                },mYear, mMonth, mDay);
                mDatePicker.getDatePicker().setCalendarViewShown(false);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();
            }
        });

        return view;
    }


    private String getStartDate()
    {
        String dateStart = startDate.getText().toString().trim();
        if(dateStart.isEmpty()) { startDate.setError("Start Date Cannot Be Empty"); }
        return dateStart;
    }

    private String getEndDate()
    {
        String dateEnd = endDate.getText().toString().trim();
        if(dateEnd.isEmpty()) { endDate.setError("End Date Cannot Be Empty"); }
        return dateEnd;
    }

    // Getting Accepted Task List
    public ArrayList <TimeShareDataModel> getTimeShareList(String userId,String startDateOfTimeShare,String endDateOfTimeShare){

        ArrayList<TimeShareDataModel> timeShareList = new ArrayList();
        TimeShareDataModel listDataModel;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("SELECT TASK_MANAGEMENT.TASK_NAME,TIME_SHARE.* FROM TASK_MANAGEMENT " +
                    "RIGHT OUTER JOIN TIME_SHARE ON TASK_MANAGEMENT.ID = TIME_SHARE.FK_TASK_MANAGEMENT_ID " +
                    "WHERE TASK_MANAGEMENT.FK_AUTHENTICATION_RECEIVED_USER_ID = ? AND TIME_SHARE.DATE_OF_TIME_SHARE BETWEEN ? AND ? ");
            ps.setString(1, userId);
            ps.setString(2, startDateOfTimeShare);
            ps.setString(3, endDateOfTimeShare);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                listDataModel = new TimeShareDataModel();


                listDataModel.setdateOfTimeShare(rs.getString("DATE_OF_TIME_SHARE"));
                listDataModel.setStartTime(rs.getString("START_TIME"));
                listDataModel.setEndTime(rs.getString("END_TIME"));
                listDataModel.setTimeDifference(rs.getString("TIME_DIFFERENCE"));
                listDataModel.setdescription(rs.getString("DESCRIPTION"));


                timeShareList.add(listDataModel);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Connection Refused Please Try Again", Toast.LENGTH_LONG).show();
        }

        return timeShareList;

    }
}
