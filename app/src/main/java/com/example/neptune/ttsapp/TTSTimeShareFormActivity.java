package com.example.neptune.ttsapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;


public class TTSTimeShareFormActivity extends AppCompatActivity {


    private EditText date,startTime,endTime,description,timeShareMeasurableQty,timeShareMeasurableUnit;
    private Button btnCancel,btnPreview,btnSubmit,addMeasurable;
    private TextView user,activityName,taskName,projCode,projName;
    private Spinner spinnerMeasurableName;
//    private ProgressBar progressBar;
    private int mYear, mMonth, mDay, mHour, mMinute;

    private SessionManager sessionManager;

    ArrayList<MeasurableListDataModel> measurableListDataModels;
    private ListView listView;
    private MeasurableListCustomAdapter measurableListCustomAdapter;

    private TaskDataModel allocatedTaskDetails,acceptedTaskDetails,processingTaskDetails;
    Long allocatedDelegationTaskId,acceptedDelegationTaskId,processingDelegationTaskId;
    ArrayList<MeasurableListDataModel> measurables;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_ttstime_share_form);
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);

        user=(TextView)findViewById(R.id.textViewUser);
        sessionManager = new SessionManager(getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=(EditText)findViewById(R.id.editTextDate);

        activityName=(TextView)findViewById(R.id.TSTextViewActName);
        taskName=(TextView)findViewById(R.id.TSTextViewTaskName);
        projCode=(TextView)findViewById(R.id.TSTextViewProjNo);
        projName=(TextView)findViewById(R.id.TSTextViewProjName);
        startTime=(EditText)findViewById(R.id.editTextStartTime);
        endTime=(EditText)findViewById(R.id.editTextEndTime);
        description=(EditText)findViewById(R.id.editTextDescription);

        btnCancel=(Button)findViewById(R.id.buttonCancel);
//        btnPreview=(Button)findViewById(R.id.buttonPreview);
        btnSubmit=(Button)findViewById(R.id.buttonSubmit);

//        progressBar=(ProgressBar)findViewById(R.id.progressBarInTimeShare);
//        progressBar.setVisibility(View.INVISIBLE);

        // Code for Measurable list
        listView=(ListView)findViewById(R.id.listTimeShareMeasurable);
        addMeasurable=(Button)findViewById(R.id.buttonTimeShareMeasurableAdd);
        timeShareMeasurableQty=(EditText)findViewById(R.id.editTextTimeShareMeasurableQty);
        timeShareMeasurableUnit=(EditText)findViewById(R.id.editTextTimeShareMeasurableUnit);

            //Get Data When Task Accept click on ACCEPT button In Task Allocated details
//            allocatedTaskDetails  = (TaskDataModel) getIntent().getSerializableExtra("TaskDetails");

            //Get Data from clicking on Task Accepted Tab ListView
//            acceptedTaskDetails = (TaskDataModel) getIntent().getSerializableExtra("TaskAcceptedDetails");

            processingTaskDetails =(TaskDataModel) getIntent().getSerializableExtra("TaskProcessingDetails");

            if (processingTaskDetails!=null)
            {
                processingDelegationTaskId= processingTaskDetails.getId();
                activityName.setText(processingTaskDetails.getActivityName());
                taskName.setText(processingTaskDetails.getTaskName());
                projCode.setText(processingTaskDetails.getProjectNo());
                projName.setText(processingTaskDetails.getProjectName());

            }

        //Code For set measurable list to spinner
            if (InternetConnectivity.isConnected()) {
//                progressBar.setVisibility(View.VISIBLE);
                measurables = getDeligationMeasurableList(processingDelegationTaskId);
                spinnerMeasurableName = (Spinner)findViewById(R.id.spinnerTimeShareMeasurable);
                ArrayAdapter<MeasurableListDataModel> adapterMeasurable = new ArrayAdapter<MeasurableListDataModel>(this, android.R.layout.simple_spinner_item,measurables);
                adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinnerMeasurableName.setAdapter(adapterMeasurable);
//                progressBar.setVisibility(View.INVISIBLE);
            }else {Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


    //Add Measurable in the ListView



        measurableListDataModels = new ArrayList<>();
        addMeasurable.setOnClickListener(v ->
        {
            try
            {
                // your handler code here
                measurableListDataModels.add(new MeasurableListDataModel(spinnerMeasurableName.getSelectedItem().toString(), timeShareMeasurableQty.getText().toString(), timeShareMeasurableUnit.getText().toString()));
                measurableListCustomAdapter = new MeasurableListCustomAdapter(measurableListDataModels, getApplicationContext());
                listView.setAdapter(measurableListCustomAdapter);
                clear();
            }catch (Exception e){e.printStackTrace();}
        });


        btnSubmit.setOnClickListener(v -> {
            try
            {
                if (InternetConnectivity.isConnected())
                {

                    if (isDateValid().isEmpty()){date.setError("Date Cannot Be Empty");}
                    else if (isStartTimeValid().isEmpty()){startTime.setError("Start Time Cannot Be Empty");}
                    else if (isEndTimeValid().isEmpty()){endTime.setError("End Time Cannot Be Empty");}
                    else if (timeDifference().contains("-")) { Toast.makeText(getApplicationContext(), "Please Enter Valid End Time", Toast.LENGTH_LONG).show(); }
                    else
                    {
                        Toast.makeText(getApplicationContext(), "Wait For Inserting TimeShare", Toast.LENGTH_LONG).show();
//                            progressBar.setVisibility(View.VISIBLE);
                        String result = insertTimeShare(getMaxTimeShareTaskId(), processingDelegationTaskId, isDateValid(), isStartTimeValid(), isEndTimeValid(), timeDifference(), isDescriptionValid(), delegationTime(), measurableListDataModels);
                            if (result.equals("true"))
                            {
                                insertActualTotalTime(processingDelegationTaskId, totalTime());
                                Toast.makeText(getApplicationContext(), "Time Share Inserted ", Toast.LENGTH_LONG).show();
                                clearAll();
//                                Intent i = new Intent(getApplicationContext(), TTSMainActivity.class);
//                                startActivity(i);
                                finish();

                            } else { Toast.makeText(getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show(); }
                    }
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
//                        progressBar.setVisibility(View.INVISIBLE);
                }

            } catch (Exception e){e.printStackTrace();}
        });

        btnCancel.setOnClickListener(v -> {
            Intent i = new Intent(getApplicationContext(), TTSMainActivity.class);
            startActivity(i);
            finish();

        });

//        btnPreview.setOnClickListener(v -> {
//
//            DailyTimeShareDataModel timeShareDataModel = new DailyTimeShareDataModel();
//            timeShareDataModel.setTimeShareDate(isDateValid());
//            timeShareDataModel.setProjectNo(isProjectCodeValid());
//            timeShareDataModel.setProjectName(isProjectNameValid());
//            timeShareDataModel.setActivityName(isActivityNameValid());
//            timeShareDataModel.setTaskName(isTaskNameValid());
//            timeShareDataModel.setStartTime(isStartTimeValid());
//            timeShareDataModel.setEndTime(isEndTimeValid());
//            timeShareDataModel.setTaskDescription(isDescriptionValid());
//
//            Intent i = new Intent(getApplicationContext(), TTSTaskModificationActivity.class);
//            i.putExtra("timeShareData", timeShareDataModel);
//            i.putExtra("MEASURABLElIST",measurableListDataModels);
//
//            startActivity(i);
//        });


//  single click view Date and time pickers
        date.setFocusable(false);
        startTime.setFocusable(false);
        endTime.setFocusable(false);

        //Date Picker start
        date.setOnClickListener(v -> {

            //To show current date in the datePicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(TTSTimeShareFormActivity.this, new DatePickerDialog.OnDateSetListener() {

                @Override
                public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                    date.setText(convertDateTime(dayOfMonth) + "-" + convertDateTime((month+1))  + "-" + year);
                }
            },mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();

            });


        // Time Picker for Start Time
        startTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(TTSTimeShareFormActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {

                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
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
            TimePickerDialog timePickerDialog = new TimePickerDialog(TTSTimeShareFormActivity.this,
                    new TimePickerDialog.OnTimeSetListener() {
                        @Override
                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                            endTime.setText(convertDateTime(hourOfDay) + ":" + convertDateTime(minute));
                        }
                    }, mHour, mMinute, true);
            timePickerDialog.show();
        });


            endTime.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    try
                    {
                        if (timeDifference() != null)
                        {
                            if (timeDifference().contains("-")) {
                                Toast.makeText(getApplicationContext(), "Please Enter Valid End Time", Toast.LENGTH_LONG).show();
                            }
                        }
                    }catch (Exception e){e.printStackTrace();}
                }
                public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

                public void onTextChanged(CharSequence s, int start, int before, int count) { }
                });

    }

    @Override
    public void onBackPressed()
    {
//        Intent i = new Intent(getApplicationContext(), TTSMainActivity.class);
//        startActivity(i);
        finish();
    }

    private void clearAll()
    {
        date.setText("");
        startTime.setText("");
        endTime.setText("");
        description.setText("");
    }

    // Add leading 0 when input date or time is single No like 5
    public String convertDateTime(int input) {
        if (input >= 10) {
            return String.valueOf(input);
        } else {
            return "0" + String.valueOf(input);
        }
    }

    // Clear the EditText of Measurable
    public void clear() {
        timeShareMeasurableQty.setText("");
        timeShareMeasurableUnit.setText("");
    }

    //Validation Start
    private String isDateValid()
    {
        String datets = date.getText().toString().trim().replaceAll("\\s+","").replace("/","-");
        if(datets.isEmpty()) { date.setError("Date Cannot Be Empty"); }
        return datets;
    }


    private String isActivityNameValid()
    {
        String actName = activityName.getText().toString().trim();
        return actName;
    }


    private String isTaskNameValid()
    {
        String tskName = taskName.getText().toString().trim();
        return tskName;
    }

    private String isProjectCodeValid()
    {
        String projectCode = projCode.getText().toString().trim();
        return projectCode;
    }

    private String isProjectNameValid()
    {
        String projectName = projName.getText().toString().trim();
        return projectName;
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

    private String isDescriptionValid()
    {
        String descrip= description.getText().toString().trim();

        return descrip;
    }
    //Validation End

    //Getting Current TimeStamp
    private Timestamp delegationTime()
    {
        Calendar calendar = Calendar.getInstance();
        Timestamp delegationTimestamp = new Timestamp(calendar.getTime().getTime());
        return delegationTimestamp;
    }

    //Calculate Time Difference between startTime and endTime
    private String timeDifference()
    {
        String start= startTime.getText().toString().trim().replaceAll("\\s+","");
        String end= endTime.getText().toString().trim().replaceAll("\\s+","");
        SimpleDateFormat format = new SimpleDateFormat("HH:mm");
        format.setTimeZone(TimeZone.getTimeZone("GMT"));

        String difference=null;

        if(!start.isEmpty() && !end.isEmpty()) {
            try {
                Date date1 = format.parse(start);
                Date date2 = format.parse(end);
                long mills = date2.getTime() - date1.getTime();
                int hours = (int) (mills / (1000 * 60 * 60));
                int mins = (int) (mills / (1000 * 60)) % 60;
                difference = hours + ":" + mins;

            } catch (ParseException e) { e.printStackTrace(); }

        }
        return difference;
    }

    //Calculate Actual Total Time
    private String totalTime(){
        String oldActualTotalTime = getActualTotalTime(processingDelegationTaskId);
        String timeDifference =timeDifference();
        String newActualTotalTime = null;
            if(oldActualTotalTime.equals("NO_TIME")) { return timeDifference; }
            else {
                SimpleDateFormat format = new SimpleDateFormat("HH:mm");
                format.setTimeZone(TimeZone.getTimeZone("GMT"));

                Date d1 = null;
                Date d2 = null;

                try {
                    d1 = format.parse(oldActualTotalTime);
                    d2 = format.parse(timeDifference);

                    //in milliseconds
                    long addtionTime = d2.getTime() + d1.getTime();

//            long diffSeconds = diff / 1000 % 60;
                    long diffMinutes = addtionTime / (60 * 1000) % 60;
                    long diffHours = addtionTime / (60 * 60 * 1000) % 24;
//            long diffDays = diff / (24 * 60 * 60 * 1000);
                    newActualTotalTime = diffHours + ":" + diffMinutes;

                } catch (Exception e) {
                    e.printStackTrace();
                }

                return newActualTotalTime;
            }
    }

    //Getting Maximum Id from TIME_SHARE Table
    public Long getMaxTimeShareTaskId() {
        long id=0;
        Connection con;
        ResultSet rs ;
        try {
            con=DatabaseHelper.getDBConnection();
            PreparedStatement ps = con.prepareStatement("select max(ID) maxnum from TIME_SHARE");

            rs=ps.executeQuery();
            if (rs.next())
            {
                id=rs.getLong("maxnum");
                System.out.println(id);
                id++;
            }else {
                id++;
            }
            rs.close();
            ps.close();
            con.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return id;
    }


    // Getting Measurables List
    public ArrayList<MeasurableListDataModel> getDeligationMeasurableList(Long timeShareId){

        ArrayList<MeasurableListDataModel> measurableList = new ArrayList();
        MeasurableListDataModel measurableListDataModel;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select m.ID,m.NAME from MEASURABLES m where ID = ANY(select FK_MEASURABLE_ID from DELEGATION_MEASURABLES where FK_TASK_MANAGEMENT_ID = ?)");
            ps.setLong(1, timeShareId);

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


    //Insert Time Share
    public String insertTimeShare(Long timeShareId,Long taskId,String date,String startTime,String endTime,String timeDifference,String description,Timestamp createdOn,ArrayList<MeasurableListDataModel> measurableList){
        String result="false";
        int x = 0;
        Connection con;

        try{
            con = DatabaseHelper.getDBConnection();
            PreparedStatement ps;

            ps = con.prepareStatement("insert into TIME_SHARE(ID,FK_TASK_MANAGEMENT_ID,DATE_OF_TIME_SHARE,START_TIME,END_TIME,TIME_DIFFERENCE,DESCRIPTION,CREATED_ON) values(?,?,?,?,?,?,?,?)");

            ps.setLong(1,timeShareId);
            ps.setLong(2,taskId);
            ps.setString(3, date);
            ps.setString(4, startTime);
            ps.setString(5, endTime);
            ps.setString(6, timeDifference);
            ps.setString(7, description);
            ps.setTimestamp(8, createdOn);

            x = ps.executeUpdate();
            ps.close();


            String sql = "insert into TIME_SHARE_MEASURABLES(FK_TIME_SHARE_ID,FK_MEASURABLE_ID,MEASURABLE_QUANTITY,MEASURABLE_UNIT) values(?,?,?,?)";
            PreparedStatement ps1 = con.prepareStatement(sql);
            con.setAutoCommit(false);
            for (MeasurableListDataModel mList:measurableList)
            {
                ps1.setLong(1,timeShareId);
                ps1.setLong(2, Long.parseLong(mList.getMeasurableName().replaceAll("[^0-9]", "")));
                ps1.setString(3, mList.getMeasurableQty());
                ps1.setString(4, mList.getMeasurableUnit());

                ps1.addBatch();

            }

            int[] x1=ps1.executeBatch();

            if (x==1 ||  x1.length>0){
                result = "true";
            }

            con.commit();
            ps1.close();
            con.close();
        }
        catch(Exception e){
            e.printStackTrace();
        }

        return result;
    }


    //Getting Actual Total Time
    public String getActualTotalTime(Long deligationTaskId) {
        String result = null;
        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select ACTUAL_TOTAL_TIME from TASK_MANAGEMENT where ID=?");
            ps.setLong(1, deligationTaskId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) { result=rs.getString("ACTUAL_TOTAL_TIME"); }

            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }


    //Inserting Actual Total Time
    public void insertActualTotalTime(Long delegationTaskId,String actualTotalTime) {

        Connection con;

        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("UPDATE TASK_MANAGEMENT SET ACTUAL_TOTAL_TIME = ? WHERE ID = ?");
            ps.setString(1, actualTotalTime);
            ps.setLong(2, delegationTaskId);
            ps.executeUpdate();

            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
