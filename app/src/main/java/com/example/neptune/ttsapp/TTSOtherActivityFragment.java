package com.example.neptune.ttsapp;


import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.neptune.ttsapp.Util.DateConverter;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class TTSOtherActivityFragment extends Fragment {

    public TTSOtherActivityFragment() { }

    private EditText date,startTime,endTime,description;
    private Button btnCancel,btnSubmit;
    private TextView user,displayDate,displayTime;
    private Spinner actSpinner;
    private SessionManager sessionManager;

    private int mYear, mMonth, mDay, mHour, mMinute;

    private String userId;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ttsother_activity, container, false);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sessionManager = new SessionManager(getActivity().getApplicationContext());
        userId = sessionManager.getToken();
        user=(TextView)view.findViewById(R.id.textViewTOAUser);
        user.setText(userId);

        displayDate=(TextView)view.findViewById(R.id.textViewOtherActivityDate);
        displayTime=(TextView)view.findViewById(R.id.textViewOtherActivityTime);

        final Handler someHandler = new Handler(Looper.getMainLooper());
        someHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date1 = new Date();
                String currentDate = formatter.format(date1);
                displayDate.setText("Date :  " +currentDate);

                SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
                Date time1 = new Date();
                String currentTime = timeFormatter.format(time1);
                displayTime.setText("Time :  " +currentTime);

                someHandler.postDelayed(this, 1000);
            }
        }, 10);





        date=(EditText)view.findViewById(R.id.editTextTOADate);
        startTime=(EditText)view.findViewById(R.id.editTextTOAStartTime);
        endTime=(EditText)view.findViewById(R.id.editTextTOAEndTime);
        description=(EditText)view.findViewById(R.id.editTextTOADescription);

        btnCancel=(Button)view.findViewById(R.id.buttonTOACancel);
        btnSubmit=(Button)view.findViewById(R.id.buttonTOASubmit);

        btnSubmit.setOnClickListener(v -> {

            try
            {
                if (InternetConnectivity.isConnected()== true)
                {
                    if (isDateValid().isEmpty()) { date.setError("Date Cannot Be Empty"); }
                    else if (isStartTimeValid().isEmpty()){ startTime.setError("Start Time Cannot Be Empty"); }
                    else if (isEndTimeValid().isEmpty()) { endTime.setError("End Time Cannot Be Empty"); }
                    else {
                        String result = insertOtherActivity(userId, getSpinnerActivity(), isDateValid(), isStartTimeValid(), isEndTimeValid(), timeDifference(), isDescriptionValid(), delegationTime());
                            if (result.equals("true"))
                            {
                                Toast.makeText(getActivity().getApplicationContext(), "Other Activity Inserted ", Toast.LENGTH_LONG).show();
                                date.setText("");
                                startTime.setText("");
                                endTime.setText("");
                                description.setText("");

                            }
                            else { Toast.makeText(getActivity().getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show(); }
                        }
                    }else{ Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show(); }

            }
            catch (Exception e){e.printStackTrace();}
        });

        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity().getApplicationContext(), TTSMainActivity.class);
                startActivity(i);

            }
        });


        date.setFocusable(false);
        startTime.setFocusable(false);
        endTime.setFocusable(false);

        // Date Picker For Select Current Date
        date.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //To show current date in the datepicker
                Calendar mcurrentDate=Calendar.getInstance();
                mYear=mcurrentDate.get(Calendar.YEAR);
                mMonth=mcurrentDate.get(Calendar.MONTH);
                mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), (view3, year, month, dayOfMonth) -> date.setText(convertDateTime(dayOfMonth) + "-" + convertDateTime((month+1))  + "-" + year),mYear, mMonth, mDay);
                mDatePicker.getDatePicker().setCalendarViewShown(false);
                mDatePicker.setTitle("Select date");
                mDatePicker.show();

            }

        });

        // Time Picker for Start Time
        startTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(),
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view2, int hourOfDay,
                                              int minute) {

                            startTime.setText(convertDateTime(hourOfDay) + ":" + convertDateTime(minute));
                        }
                    }, mHour, mMinute, true);
            timePickerDialog.show();
        });

        // Time Picker for End Time
        endTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), (view1, hourOfDay, minute) ->
                            endTime.setText(convertDateTime(hourOfDay) + ":" + convertDateTime(minute)), mHour, mMinute, true);
            timePickerDialog.show();
        });



        // Set Other Activity To Spinner
        actSpinner = (Spinner)view.findViewById(R.id.spinnerTaskOtherActSelect);
        if (InternetConnectivity.isConnected()) {
         ArrayList<String> otherActivity =  getOtherActivityList();
         otherActivity.add(0,"Select Other Activity");
        ArrayAdapter<String> adapterMeasurable = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,otherActivity);
        adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        actSpinner.setAdapter(adapterMeasurable);
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

        return view;
    }

    // Add leading 0 when input date or time is single No like 5
    public String convertDateTime(int input) {
        if (input >= 10) {
            return String.valueOf(input);
        } else {
            return "0" + input;
        }
    }


    private String getSpinnerActivity()
    {
        String activity = actSpinner.getSelectedItem().toString().trim();
        return activity;
    }

    private String isDateValid()
    {
        String datets = date.getText().toString().trim().replaceAll("\\s+","");
        if(datets.isEmpty()) { date.setError("Date Cannot Be Empty"); }
        return datets;
    }

    private String isStartTimeValid()
    {
        String strtTime= startTime.getText().toString().trim().replaceAll("\\s+","");
        if(strtTime.isEmpty()) { startTime.setError("Start Time Cannot Be Empty"); }
        return strtTime;
    }

    private String isEndTimeValid()
    {
        String edTime= endTime.getText().toString().trim().replaceAll("\\s+","");
        if(edTime.isEmpty()) { endTime.setError("End Time Cannot Be Empty"); }
        return edTime;
    }

    private String isDescriptionValid() { return description.getText().toString().trim(); }

    private String delegationTime()
    {
//        Calendar calendar = Calendar.getInstance();
//        Timestamp delegationTimestamp = new Timestamp(calendar.getTime().getTime());
//        return delegationTimestamp.toString();
        return DateConverter.getCurrentDateTime();
    }

    // Calculate Time Difference between startTime and endTime
    private String timeDifference()
    {
        String start= startTime.getText().toString().trim().replaceAll("\\s+","");
        String end= endTime.getText().toString().trim().replaceAll("\\s+","");
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        String difference=null;

        if(!start.isEmpty() && !end.isEmpty()) {
            try {
                Date date1 = format.parse(start);
                Date date2 = format.parse(end);
                long mills = date2.getTime() - date1.getTime();
                int hours = (int) (mills / (1000 * 60 * 60));
                int mins = (int) (mills / (1000 * 60)) % 60;
                difference = convertDateTime(hours) + ":" + convertDateTime(mins);

            } catch (ParseException e) { e.printStackTrace(); }

        }
        return difference;
    }

    // Getting Other Activity List Name List
    public ArrayList<String> getOtherActivityList(){

        ArrayList<String> otherActivityList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from OTHER_ACTIVITY");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String s = rs.getString("NAME");

                otherActivityList.add(s);

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return otherActivityList;

    }

    // Insert Other Activity Data into Table
    public String insertOtherActivity(String userId, String activityName,String date, String startTime, String endTime, String timeDifference,String description,String createdOn){
        String result="false";
        int x = 0;
        Connection con;

        try{
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("insert into TIME_SHARE_OTHER_ACTIVITY(FK_AUTHENTICATION_USER_ID,ACTIVITY,DATE,START_TIME,END_TIME,TIME_DIFFERENCE,DESCRIPTION,CREATED_ON) values(?,?,?,?,?,?,?,?)");

            ps.setString(1, userId);
            ps.setString(2, activityName);
            ps.setString(3, date);
            ps.setString(4, startTime);
            ps.setString(5, endTime);
            ps.setString(6, timeDifference);
            ps.setString(7, description);
            ps.setString(8, createdOn);


            x = ps.executeUpdate();

            if(x==1){
                result = "true";
            }

            ps.close();
            con.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }

}
