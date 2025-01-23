package com.example.neptune.ttsapp;


import android.app.DatePickerDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.neptune.ttsapp.Util.DateConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;


@AndroidEntryPoint
public class TTSManagementReportFragment extends Fragment {

    @Inject
    AppExecutors appExecutors;

    private SessionManager sessionManager;

    private TextView user,date,time,projectCt,activityCt,taskCt,measurableCt,projectFrq,activityFrq,taskFrq,projectTimeConsume;
    private EditText startDate,endDate;
    private Button getPATDetails;

    private String userId;
    private int mYear, mMonth, mDay;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ttsmanagement_report, container, false);


        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getToken();
        user=(TextView)view.findViewById(R.id.textViewMgmtReportUser);
        user.setText(userId);

        date=(TextView)view.findViewById(R.id.textViewMgmtReportDate);
        time=(TextView)view.findViewById(R.id.textViewMgmtReportTime);

        startDate=(EditText)view.findViewById(R.id.editTextMgmtReportStartDate);
        endDate=(EditText)view.findViewById(R.id.editTextMgmtReportEndDate);

        projectCt=(TextView)view.findViewById(R.id.textViewMgmtReportProjectCount);
        activityCt=(TextView)view.findViewById(R.id.textViewMgmtReportActivtyCount);
        taskCt=(TextView)view.findViewById(R.id.textViewMgmtReportTaskCount);
        measurableCt=(TextView)view.findViewById(R.id.textViewMgmtReportMeasurableCount);

        projectFrq=(TextView)view.findViewById(R.id.textViewMgmtReportProjFrq);
        activityFrq=(TextView)view.findViewById(R.id.textViewMgmtReportActFrq);
        taskFrq=(TextView)view.findViewById(R.id.textViewMgmtReportTaskFrq);

        projectTimeConsume=(TextView)view.findViewById(R.id.textViewMgmtReportProjectTimeConsume);

        getPATDetails =(Button) view.findViewById(R.id. buttonGetPATDetails);


           appExecutors.getMainThread().execute(() -> {
            date.setText(DateConverter.currentDate());
            time.setText(DateConverter.currentTime());
        });



        startDate.setFocusable(false);
        endDate.setFocusable(false);

        //Date Picker start
        startDate.setOnClickListener(v -> {
            //To show current date in the datepicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), (view12, year, month, dayOfMonth) ->
                    startDate.setText(convertDateTime(dayOfMonth) + "/" + convertDateTime((month+1))  + "/" + year),mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();

        });

        endDate.setOnClickListener(v -> {

            //To show current date in the datepicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), (view1, year, month, dayOfMonth) ->
                    endDate.setText(convertDateTime(dayOfMonth) + "/" + convertDateTime((month+1))  + "/" + year),mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();

        });


        getPATDetails.setOnClickListener(v -> {
            try
            {
                if (InternetConnectivity.isConnected())
                {
                    if (isStartDateValid().isEmpty()){startDate.setError("Start Date Cannot Be Empty");}
                    else if (isEndDateValid().isEmpty()){endDate.setError("End Date Cannot Be Empty");}
                    else
                    {

                        projectCt.setText("Project Count   :  " + getProjectCount(userId, isStartDateValid(), isEndDateValid()));
                        activityCt.setText("Activity Count      :  " + getActivityCount(userId, isStartDateValid(), isEndDateValid()));
                        taskCt.setText("Task Count       :  " + getTaskCount(userId, isStartDateValid(), isEndDateValid()));
                        measurableCt.setText("Measurable Count   :  " + getMeasurableCount(userId, isStartDateValid(), isEndDateValid()));

                        projectFrq.setText("Project Frequency   :  " + getProjectFrequencyCount(userId, isStartDateValid(), isEndDateValid()));
                        activityFrq.setText("Activity Frequency   :  " + getActivityFrequencyCount(userId, isStartDateValid(), isEndDateValid()));
                        taskFrq.setText("Task Frequency        :  " + getTaskFrequencyCount(userId, isStartDateValid(), isEndDateValid()));

                        projectTimeConsume.setText("Projects Consumed Time:  " + projectsConsumedTime(userId, isStartDateValid(), isEndDateValid()));


                        startDate.setText("");
                        endDate.setText("");
                    }
                }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

            } catch (Exception e){e.printStackTrace();}
        });

        return view;
    }


    // Add leading 0 when input date or time is single No like 5
    public String convertDateTime(int input) {
        if (input >= 10) { return String.valueOf(input); }
        else { return "0" + input; }
    }

    private String isStartDateValid() { return startDate.getText().toString().trim().replaceAll("\\s+",""); }

    private String isEndDateValid() { return endDate.getText().toString().trim().replaceAll("\\s+",""); }

    //Calculate Actual Total Time
    private String projectsConsumedTime(String userId,String startDate,String endDate)
    {
        ArrayList<String> consumedTimeList = getProjectTimeConsumption(userId, startDate,endDate);

            SimpleDateFormat format = new SimpleDateFormat("HH:mm");
            format.setTimeZone(TimeZone.getTimeZone("GMT"));
            String newActualTotalTime  = null;;

            Date d1 = null;

        long addtionTime = 0;
        long diffMinutes;
        long diffHours ;

                try {
                    for (String consumedTime : consumedTimeList)
                    {
                        d1 = format.parse(consumedTime);
                        addtionTime += d1.getTime();

                        diffMinutes = addtionTime / (60 * 1000) % 60;
                        diffHours = addtionTime / (60 * 60 * 1000);

                        newActualTotalTime = diffHours + ":" + diffMinutes;
                    }


                } catch (Exception e) { e.printStackTrace(); }

            return newActualTotalTime;
    }



    // Getting Project Count
    public Integer getProjectCount(String userID, String startDate,String endDate){

        Connection con;
        int count = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            String query = "SELECT COUNT(DISTINCT(PROJECT_NAME)) FROM DAILY_TIME_SHARE WHERE FK_AUTHENTICATION_USER_ID = ? \n" +
                    "AND DAILY_TIME_SHARE.DATE_OF_TIME_SHARE BETWEEN ? AND ? ";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2,startDate);
            ps.setString(3,endDate);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    // Getting Activity Count
    public Integer getActivityCount(String userID, String startDate,String endDate){

        Connection con;
        int count = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            String query = "SELECT COUNT(DISTINCT(ACTIVITY_NAME)) FROM DAILY_TIME_SHARE WHERE FK_AUTHENTICATION_USER_ID = ? \n" +
                    "AND DAILY_TIME_SHARE.DATE_OF_TIME_SHARE BETWEEN ? AND ? ";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2,startDate);
            ps.setString(3,endDate);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }


    // Getting Task Count
    public Integer getTaskCount(String userID, String startDate,String endDate){

        Connection con;
        int count = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            String query = "SELECT COUNT(DISTINCT(TASK_NAME)) FROM DAILY_TIME_SHARE WHERE FK_AUTHENTICATION_USER_ID = ? \n" +
                    "AND DAILY_TIME_SHARE.DATE_OF_TIME_SHARE BETWEEN ? AND ? ";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2,startDate);
            ps.setString(3,endDate);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }


    // Getting Measurable Count
    public Integer getMeasurableCount(String userID, String startDate,String endDate){

        Connection con;
        int count = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            String query = "SELECT COUNT(DISTINCT(DAILY_TIME_SHARE_MEASURABLE.FK_MEASURABLE_ID)) FROM DAILY_TIME_SHARE\n" +
                    "LEFT JOIN DAILY_TIME_SHARE_MEASURABLE ON DAILY_TIME_SHARE.ID = DAILY_TIME_SHARE_MEASURABLE.FK_TIME_SHARE_ID\n" +
                    "WHERE DAILY_TIME_SHARE.FK_AUTHENTICATION_USER_ID = ? AND \n" +
                    "DAILY_TIME_SHARE.DATE_OF_TIME_SHARE BETWEEN ? AND ?";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2,startDate);
            ps.setString(3,endDate);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }


    // Getting Project Frequency Count
    public Integer getProjectFrequencyCount(String userID, String startDate,String endDate){

        Connection con;
        int count = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            String query = "SELECT COUNT(PROJECT_NAME) FROM DAILY_TIME_SHARE WHERE FK_AUTHENTICATION_USER_ID = ? \n" +
                    "AND DAILY_TIME_SHARE.DATE_OF_TIME_SHARE BETWEEN ? AND ? ";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2,startDate);
            ps.setString(3,endDate);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }

    // Getting Activity Frequency Count
    public Integer getActivityFrequencyCount(String userID, String startDate,String endDate){

        Connection con;
        int count = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            String query = "SELECT COUNT(ACTIVITY_NAME) FROM DAILY_TIME_SHARE WHERE FK_AUTHENTICATION_USER_ID = ? \n" +
                    "AND DAILY_TIME_SHARE.DATE_OF_TIME_SHARE BETWEEN ? AND ? ";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2,startDate);
            ps.setString(3,endDate);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        return count;
    }


    // Getting Task Frequency Count
    public Integer getTaskFrequencyCount(String userID, String startDate,String endDate){

        Connection con;
        int count = 0;

        try {
            con = DatabaseHelper.getDBConnection();

            String query = "SELECT COUNT(TASK_NAME) FROM DAILY_TIME_SHARE WHERE FK_AUTHENTICATION_USER_ID = ? \n" +
                    "AND DAILY_TIME_SHARE.DATE_OF_TIME_SHARE BETWEEN ? AND ? ";

            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2,startDate);
            ps.setString(3,endDate);


            ResultSet rs = ps.executeQuery();

            while (rs.next()) { count = rs.getInt(1); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        return count;
}



    // Getting Project Time Consumption
    public ArrayList<String> getProjectTimeConsumption(String userID, String startDate, String endDate){

        Connection con;
        ArrayList<String> timeConsumption = new ArrayList<>();

        try {
            con = DatabaseHelper.getDBConnection();

            String query = "SELECT CONSUMED_TIME FROM DAILY_TIME_SHARE WHERE FK_AUTHENTICATION_USER_ID = ? AND \n" +
                    "DATE_OF_TIME_SHARE BETWEEN ? AND ? ";
 
            PreparedStatement ps = con.prepareStatement(query);
            ps.setString(1, userID);
            ps.setString(2,startDate);
            ps.setString(3,endDate);


            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                String time = rs.getString(1);
                timeConsumption.add(time);
            }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }
        return timeConsumption;
    }

}
