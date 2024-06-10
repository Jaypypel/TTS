package com.example.neptune.ttsapp;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class TTSTaskDelegationFragment extends Fragment {

    public TTSTaskDelegationFragment() { }

    EditText taskDeliExpDate,taskDeliExpTime, taskDeliTotalTimeHH,taskDeliTotalTimeMM,taskDeliDescription, taskDeliMeasurableQty;
    AutoCompleteTextView taskDeliActivityName,taskDeliTaskName,taskDeliProjName, taskDeliUserName, taskDeliMeasurableUnit;
    TextView taskDeliProjCode;
    Button taskDeleCancel,taskDelegate,addMeasurable;
    private TextView taskDeliUser,taskDeliDate,time;
    private Spinner spinnerMeasurable;

//    private ProgressBar progressBarInTaskDeli;

    private int mYear, mMonth, mDay, mHour, mMinute;

    private SessionManager sessionManager;

    ArrayList<MeasurableListDataModel> measurableListDataModels = new ArrayList<>();
    private ListView listView;
    private static MeasurableListCustomAdapter measurableListCustomAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_ttstask_delegation, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        sessionManager = new SessionManager(getActivity().getApplicationContext());

        taskDeliUser=(TextView)view.findViewById(R.id.textViewTaskdeliUser);
        taskDeliUser.setText(sessionManager.getUserID());


        taskDeliDate=(TextView)view.findViewById(R.id.textViewDate);
        time = (TextView) view.findViewById(R.id.textViewTime);

        final Handler someHandler = new Handler(Looper.getMainLooper());
        someHandler.postDelayed(new Runnable()
        {
            @Override
            public void run()
            {
                SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
                Date date1 = new Date();
                String currentDate = formatter.format(date1);
                taskDeliDate.setText("Date :  " +currentDate);

                SimpleDateFormat timeFormatter = new SimpleDateFormat("hh:mm a");
                Date time1 = new Date();
                String currentTime = timeFormatter.format(time1);
                time.setText("Time :  " +currentTime);

                someHandler.postDelayed(this, 1000);
            }
        }, 10);


        taskDeliUserName = (AutoCompleteTextView)view.findViewById(R.id.editTextTaskDeliUserName);
        taskDeliActivityName=(AutoCompleteTextView)view.findViewById(R.id.editTextTaskDeliActName);
        taskDeliTaskName=(AutoCompleteTextView)view.findViewById(R.id.editTextTaskDeliTaskName);
        taskDeliProjCode=(TextView)view.findViewById(R.id.editTextTaskDeliProjNo);
        taskDeliProjName=(AutoCompleteTextView)view.findViewById(R.id.editTextTaskDeliProjName);
        taskDeliExpDate=(EditText)view.findViewById(R.id.editTextTaskDeliExpDate);
        taskDeliExpTime=(EditText)view.findViewById(R.id.editTextTaskDeliExpTime);
        taskDeliTotalTimeHH =(EditText)view.findViewById(R.id.editTextTaskDeliExpTotalTimeHH);
        taskDeliTotalTimeMM =(EditText)view.findViewById(R.id.editTextTaskDeliExpTotalTimeMM);

        taskDeliDescription=(EditText)view.findViewById(R.id.editTextTaskDeliDes);

        taskDeleCancel =(Button)view.findViewById(R.id.buttonTaskDeliCancel);
        taskDelegate=(Button)view.findViewById(R.id.buttonTaskDeligate);

//        progressBarInTaskDeli=(ProgressBar)view.findViewById(R.id.progressBarInTaskDelegation);
//        progressBarInTaskDeli.setVisibility(View.INVISIBLE);


        spinnerMeasurable = (Spinner) view.findViewById(R.id.spinnerTaskDeliMeasurableSelect);

        // Code for Measurable list
        listView=(ListView)view.findViewById(R.id.listTaskDeliMeasurable);
        addMeasurable=(Button)view.findViewById(R.id.buttontTaskDeliAdd);
        taskDeliMeasurableQty=(EditText)view.findViewById(R.id.editTextTaskDeliQty);
        taskDeliMeasurableUnit =(AutoCompleteTextView) view.findViewById(R.id.editTextTaskDeliUnit);


        addMeasurable.setOnClickListener(v -> {
            try {
                measurableListDataModels.add(new MeasurableListDataModel(spinnerMeasurable.getSelectedItem().toString(), taskDeliMeasurableQty.getText().toString(), taskDeliMeasurableUnit.getText().toString()));
                measurableListCustomAdapter = new MeasurableListCustomAdapter(measurableListDataModels, getActivity());
                listView.setAdapter(measurableListCustomAdapter);
                clearMeasurableDetails();
            }
            catch (Exception e){e.printStackTrace();}
        });

//        taskDelegate.setBackgroundColor(Color.BLUE);
        taskDelegate.setOnClickListener(v -> {
//                progressBarInTaskDeli.setVisibility(View.VISIBLE);
            try
            {
                String result;
                if (InternetConnectivity.isConnected())
                {
                    if (isReceivedUserValid().isEmpty()){taskDeliUserName.setError("UserName Cannot Be Empty");}
                    else if(isTaskNameValid().isEmpty()) { taskDeliTaskName.setError("Task Name Cannot Be Empty"); }
                    else if (isActivityNameValid().isEmpty()){taskDeliActivityName.setError("Activity Name Cannot Be Empty");}
                    else if (isProjectNameValid().isEmpty()) { taskDeliProjName.setError("Project Name Cannot Be Empty"); }
                    else if (isExpDateValid().isEmpty()) {taskDeliExpDate.setError("Expected Date Cannot Be Empty");}
                    else if (isExpTimeValid().isEmpty()) {taskDeliExpTime.setError("Expected Time Cannot Be Empty");}
                    else if ((taskDeliTotalTimeMM.getText().toString().trim().replaceAll("\\s+","")).length()>0)
                    {
                        if (Integer.parseInt(taskDeliTotalTimeMM.getText().toString().trim().replaceAll("\\s+",""))>60)
                        {
                            Toast.makeText(getActivity(), "Invalid Minute", Toast.LENGTH_LONG).show();
                        }
                    }
                    else
                    {
                        result = insertTaskDeligation(getMaxDelegationTaskId(), deligateOwnerUserId(), isReceivedUserValid(), isProjectCodeValid(), isProjectNameValid(), isActivityNameValid(),
                                isTaskNameValid(), isExpDateValid(), isExpTimeValid(),isTotalTimeValid(), isDescriptionValid(), "PENDING", delegationTime(), measurableListDataModels);

                        if (result.equals("true"))
                        {
                            Toast.makeText(getActivity(), "Thank You..! Task Is Assigned", Toast.LENGTH_LONG).show();
                            clearAll();
                            clearMeasurableDetails();
                        }
                        else
                        {
                            Toast.makeText(getActivity(), "Task Delegation Failed", Toast.LENGTH_LONG).show();
//                                    progressBarInTaskDeli.setVisibility(View.INVISIBLE);
                        }
                     }

                }else
                {
                    Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
//                            progressBarInTaskDeli.setVisibility(View.INVISIBLE);
                }

            } catch (Exception e){e.printStackTrace();}
        });

        taskDeleCancel.setOnClickListener(v -> {
//            Intent i = new Intent(getActivity(), TTSMainActivity.class);
//            startActivity(i);
//            getActivity().finish();
            clearAll();
            clearMeasurableDetails();
        });




        try {
//                        progressBarInTaskDeli.setVisibility(View.VISIBLE);
                        // Set AutoCompleteTextView
                        if (InternetConnectivity.isConnected()) {


                            clearAll();
                            Toast.makeText(getActivity(), "Wait Loading Details", Toast.LENGTH_LONG).show();

                            ArrayAdapter<String> userAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1,getUserList());
                            taskDeliUserName.setAdapter(userAdapter);

                            ArrayAdapter<String> activityNameAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getActivityList());
                            taskDeliActivityName.setAdapter(activityNameAdapter);

                            ArrayAdapter<String> taskNameAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getTaskList());
                            taskDeliTaskName.setAdapter(taskNameAdapter);

                            ArrayAdapter<String> projectNameAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getProjectNameList());
                            taskDeliProjName.setAdapter(projectNameAdapter);

                            ArrayAdapter<MeasurableListDataModel> adapterMeasurable = new ArrayAdapter<MeasurableListDataModel>(getActivity(), android.R.layout.simple_spinner_item, getMeasurableList());
                            adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerMeasurable.setAdapter(adapterMeasurable);

//                            ArrayAdapter<String> unitNameAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getMeasurableUnit());
//                            taskDeliMeasurableUnit.setAdapter(unitNameAdapter);


                        } else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show(); }
//                        progressBarInTaskDeli.setVisibility(View.INVISIBLE);

            }catch (Exception e){e.printStackTrace();}

        // Get Project Code Against Project Name And Set To Project Code TextView
//        taskDeliProjName.setOnFocusChangeListener((v, hasFocus) -> {
//            if (!hasFocus)
//            {
//                String projectName = taskDeliProjName.getText().toString().trim();
//                if (projectName.length()>0)
//                {
//                    taskDeliProjCode.setText(getProjectCode(isProjectNameValid()));
//                }
//            }
//        });


        //  single click view Date and time pickers
        taskDeliExpDate.setFocusable(false);
        taskDeliExpTime.setFocusable(false);

        //Date Picker for Expected Date start
        taskDeliExpDate.setOnClickListener((View v) -> {

            //To show current date in the DatePicker
            Calendar mcurrentDate=Calendar.getInstance();
            mYear=mcurrentDate.get(Calendar.YEAR);
            mMonth=mcurrentDate.get(Calendar.MONTH);
            mDay=mcurrentDate.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog mDatePicker=new DatePickerDialog(getActivity(), (view1, year, month, dayOfMonth) ->
                    taskDeliExpDate.setText(convertDateTime(dayOfMonth) + "-" + convertDateTime((month+1))  + "-" + year),mYear, mMonth, mDay);
            mDatePicker.getDatePicker().setCalendarViewShown(false);
            mDatePicker.setTitle("Select date");
            mDatePicker.show();

            // Get Project Code Against Project Name And Set To Project Code TextView
            String projectName = taskDeliProjName.getText().toString().trim();
            if (projectName.length()>0)
            {
                taskDeliProjCode.setText(getProjectCode(isProjectNameValid()));
            }
        });

        // Time Picker for Expected Time
        taskDeliExpTime.setOnClickListener(v -> {
            final Calendar c = Calendar.getInstance();
            mHour = c.get(Calendar.HOUR_OF_DAY);
            mMinute = c.get(Calendar.MINUTE);

            // Launch Time Picker Dialog
            TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), (view12, hourOfDay, minute) ->
                    taskDeliExpTime.setText(convertDateTime(hourOfDay) + ":" + convertDateTime(minute)), mHour, mMinute, true);
            timePickerDialog.show();
        });


        taskDeliTotalTimeMM.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                String totalTimeMM= taskDeliTotalTimeMM.getText().toString().trim().replaceAll("\\s+","");
                if (totalTimeMM.length()>0)
                {
                    try
                    {
                        int min = Integer.parseInt(totalTimeMM);
                        if (min > 60)
                        {
                            Toast.makeText(getActivity().getApplicationContext(), "Invalid Minute", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            public void onTextChanged(CharSequence s, int start, int before, int count) { }
        });



        // code for go to Next EditText when press button of DONE on keyboard

        taskDeliUserName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliTaskName.requestFocus(); }
            return false;
        });

        taskDeliTaskName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliDescription.requestFocus(); }
            return false;
        });

        taskDeliDescription.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliActivityName.requestFocus(); }
            return false;
        });

        taskDeliActivityName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliProjName.requestFocus(); }
            return false;
        });

        taskDeliProjName.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliTotalTimeHH.requestFocus(); }
            return false;
        });

        taskDeliTotalTimeHH.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliTotalTimeMM.requestFocus(); }
            return false;
        });

        taskDeliTotalTimeMM.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliMeasurableQty.requestFocus(); }
            return false;
        });

        taskDeliMeasurableQty.setOnEditorActionListener((v, actionId, event) -> {
            if ((event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) || (actionId == EditorInfo.IME_ACTION_DONE))
            { taskDeliMeasurableUnit.requestFocus(); }
            return false;
        });

        return view;
    }

    public void clearMeasurableDetails() {
        taskDeliMeasurableQty.setText("");
        taskDeliMeasurableUnit.setText("");
    }

    public void clearAll() {
        taskDeliUserName.setText("");
        taskDeliActivityName.setText("");
        taskDeliTaskName.setText("");
        taskDeliProjName.setText("");
        taskDeliProjCode.setText("");
        taskDeliDescription.setText("");
        taskDeliExpDate.setText("");
        taskDeliExpTime.setText("");
        taskDeliTotalTimeHH.setText("");
        taskDeliTotalTimeMM.setText("");
        listView.setAdapter(null);
    }

    // Add leading 0 when input date or time is single No like 5
    public String convertDateTime(int input) {
        if (input >= 10) { return String.valueOf(input); }
        else { return "0" + input; }
    }


    //Validation Start
    private String deligateOwnerUserId()
    {
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        return sessionManager.getUserID();
    }

    private String isReceivedUserValid()
    {
        return taskDeliUserName.getText().toString().trim();
    }


    private String isActivityNameValid()
    {
        String actName = taskDeliActivityName.getText().toString().trim();
        if(actName.isEmpty()) { taskDeliActivityName.setError("Activity Name Cannot Be Empty"); }
        return actName;
    }


    private String isTaskNameValid()
    {
        String taskName = taskDeliTaskName.getText().toString().trim();
        if(taskName.isEmpty()) { taskDeliTaskName.setError("Task Name Cannot Be Empty"); }
        return taskName;
    }

    private String isProjectCodeValid()
    {
        return taskDeliProjCode.getText().toString().trim();
    }

    private String isProjectNameValid()
    {
        String projectName = taskDeliProjName.getText().toString().trim();
        if(projectName.isEmpty()) { taskDeliProjName.setError("Project Name Cannot Be Empty"); }
        return projectName;
    }

    private String isExpDateValid()
    {
        String expDate= taskDeliExpDate.getText().toString().trim().replaceAll("\\s+","");
        if(expDate.isEmpty()) { taskDeliExpDate.setError("Expected Date Cannot Be Empty"); }
        return expDate;
    }

    private String isExpTimeValid()
    {
        String expTime= taskDeliExpTime.getText().toString().trim().replaceAll("\\s+","");
        if(expTime.isEmpty()) { taskDeliExpTime.setError("Expected Time Cannot Be Empty"); }
        return expTime;
    }

    private String isTotalTimeValid()
    {
        String totalTimeHH= taskDeliTotalTimeHH.getText().toString().trim().replaceAll("\\s+","");
        String totalTimeMM= taskDeliTotalTimeMM.getText().toString().trim().replaceAll("\\s+","");

        if (totalTimeHH.isEmpty()) {totalTimeHH = "00";}
        if (totalTimeMM.isEmpty()) {totalTimeMM = "00";}

        return totalTimeHH + ":" + totalTimeMM;
    }

    private String isDescriptionValid()
    {
        return taskDeliDescription.getText().toString().trim();
    }

    private Timestamp delegationTime()
    {
        Calendar calendar = Calendar.getInstance();
        return new Timestamp(calendar.getTime().getTime());
    }
    //Validation End


    // Getting Users List
    public ArrayList <String> getUserList(){

        ArrayList userNameList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from AUTHENTICATION");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String s = rs.getString("USER_ID");

                userNameList.add(s);

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return userNameList;

    }

    // Getting Measurable List
    public ArrayList<MeasurableListDataModel> getMeasurableList(){

        ArrayList measurableList = new ArrayList();
        MeasurableListDataModel measurableListDataModel;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from MEASURABLES ORDER BY NAME ASC");


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

        Log.d("Measurable List",measurableList.toString());
        return measurableList;

    }

    // Getting Measurable Unit List
    public ArrayList <String> getMeasurableUnit(){

        ArrayList unitNameList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from MEASURABLE_UNIT");

            ResultSet rs = ps.executeQuery();

            while (rs.next())
            {
                String s = rs.getString("UNIT_NAME");

                unitNameList.add(s);
            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return unitNameList;

    }

    // Getting Activity List
    public ArrayList<String> getActivityList(){

        ArrayList<String> activityList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select DISTINCT NAME from ACTIVITY");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String s = rs.getString("NAME");

                activityList.add(s);

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return activityList;

    }


    // Getting Task List
    public ArrayList<String> getTaskList(){

        ArrayList<String> activityList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select DISTINCT NAME from TASK");


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String s = rs.getString("NAME");

                activityList.add(s);

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return activityList;

    }

    // Getting Project Name List
    public ArrayList<String> getProjectNameList(){

        ArrayList<String> projectNameList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select DISTINCT NAME from PROJECT");


            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String s = rs.getString("NAME");

                projectNameList.add(s);

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return projectNameList;

    }

    // Getting Project Code List
    public String getProjectCode(String projectName){

        String projectCode=null;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select DISTINCT CODE from PROJECT WHERE NAME = ?");
            ps.setString(1, projectName);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                projectCode = rs.getString("CODE");

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return projectCode;

    }

    //Getting Maximum ID From TASK_MANAGEMENT Table
    public Long getMaxDelegationTaskId() {
        long id=0;
        Connection con;
        ResultSet rs ;
        try {
            con=DatabaseHelper.getDBConnection();
            PreparedStatement ps = con.prepareStatement("select max(ID) maxnum from TASK_MANAGEMENT");

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

        } catch (SQLException e) { e.printStackTrace(); }
        return id;
    }


    //Insert Task Delegation Start
    public String insertTaskDeligation(Long delegationId,String delegateOwnerUserId,String receivedUserId,String taskDeliProjCode,String taskDeliProjName,String taskDeliActivityName,
                                       String taskDeliTaskName,String taskDeliExpDate,String taskDeliExpTime,String taskDeliTotalTime,String taskDeliDescription,String status,Timestamp delegationTime,ArrayList<MeasurableListDataModel> measurableList)
    {
        String result="false";
        int x = 0;
        Connection con;

        try{
            con = DatabaseHelper.getDBConnection();
            PreparedStatement ps,ps1 ;

            String sqlTask = "insert into TASK_MANAGEMENT(ID,FK_AUTHENTICATION_OWNER_USER_ID,FK_AUTHENTICATION_RECEIVED_USER_ID,ACTIVITY_NAME,TASK_NAME," +
                    "PROJECT_ID,PROJECT_NAME,EXPECTED_DATE,EXPECTED_TIME,EXPECTED_TOTAL_TIME,DESCRIPTION,DELEGATION_ON,STATUS) values(?,?,?,?,?,?,?,?,?,?,?,?,?)";

            ps = con.prepareStatement(sqlTask);

            ps.setLong(1, delegationId);
            ps.setString(2, delegateOwnerUserId);
            ps.setString(3, receivedUserId);
            ps.setString(4, taskDeliActivityName);
            ps.setString(5, taskDeliTaskName);
            ps.setString(6, taskDeliProjCode);
            ps.setString(7, taskDeliProjName);
            ps.setString(8, taskDeliExpDate );
            ps.setString(9,taskDeliExpTime );
            ps.setString(10,taskDeliTotalTime);
            ps.setString(11,taskDeliDescription);
            ps.setTimestamp(12,delegationTime );
            ps.setString(13,status);

            x=ps.executeUpdate();
            ps.close();


            String sqlMeasurable = "insert into DELEGATION_MEASURABLES(FK_TASK_MANAGEMENT_ID,FK_MEASURABLE_ID,EXPECTED_MEASURABLE_QUANTITY,MEASURBLE_UNIT) values(?,?,?,?)";
            ps1 = con.prepareStatement(sqlMeasurable);
            con.setAutoCommit(false);

            for (MeasurableListDataModel mList:measurableList)
            {
                ps1.setLong(1,delegationId);
                ps1.setLong(2, Long.parseLong(mList.getMeasurableName().replaceAll("[^0-9]", "")));
                ps1.setString(3, mList.getMeasurableQty());
                ps1.setString(4, mList.getMeasurableUnit());

                ps1.addBatch();

            }

             int[] x1=ps1.executeBatch();

            if (x==1 ||  x1.length>0){ result = "true"; }

            con.commit();
            ps.close();
            con.close();
        }
        catch(Exception e){ e.printStackTrace(); }

        return result;
    }

}
