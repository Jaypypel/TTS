package com.example.neptune.ttsapp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
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


public class TTSTaskCRUDFragment extends Fragment {

    public TTSTaskCRUDFragment() { }

    private TextView user,date,time;
    private AutoCompleteTextView taskName;
    private Button addTask;
    private Spinner userSelect,activitySelect;

    private SessionManager sessionManager;

    private ArrayList<ActivityDataModel> activityDataModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttstask_crud, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);


        user=(TextView)view.findViewById(R.id.textViewTaskCRUDUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=(TextView)view.findViewById(R.id.textViewTaskCRUDDate);
        time=(TextView)view.findViewById(R.id.textViewTaskCRUDTime);


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


        taskName=(AutoCompleteTextView) view.findViewById(R.id.editTextTaskCRUDTaskName);
        addTask=(Button)view.findViewById(R.id.buttonTaskCRUDAdd);
        userSelect=(Spinner) view.findViewById(R.id.spinnerTaskCRUDUserSelect);


        if (InternetConnectivity.isConnected()== true)
        {
            ArrayList users = getUserList();
            users.add(0, "Select User");
            ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
            userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            userSelect.setAdapter(userSelectAdapter);
        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}



        activitySelect=(Spinner) view.findViewById(R.id.spinnerTaskCRUDActivitySelect);

        if (InternetConnectivity.isConnected()== true)
        {
            userSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
            {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
                {

                    taskName.setText("");
                    ArrayAdapter<String> taskNameAdapter = new ArrayAdapter<>(getActivity(),android.R.layout.simple_list_item_1,getTaskNameList(getUser()));
                    taskName.setAdapter(taskNameAdapter);

                    activityDataModels = getActivityList(getUser());
                    ArrayAdapter<ActivityDataModel> activitySelectAdapter = new ArrayAdapter<ActivityDataModel>(getActivity(), android.R.layout.simple_spinner_item,activityDataModels);
                    activitySelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    activitySelect.setAdapter(activitySelectAdapter);

                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}



        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    if (InternetConnectivity.isConnected()== true) {
                        if (isTaskName().isEmpty()) {
                            taskName.setError("Task Name Be Empty");
                        } else
                        {
                            String result = insertTask(getUser(), getAct(), isTaskName(), createdOn());
                            if (result.equals("true")) {
                                Toast.makeText(getActivity().getApplicationContext(), "Task Inserted ", Toast.LENGTH_LONG).show();
                                taskName.setText("");
                            } else {
                                Toast.makeText(getActivity().getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show();
                            }
                        }
                    }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

                }
                catch (Exception e){e.printStackTrace();}
            }
        });



        return view;
    }


    private String getUser()
    {
        String user = userSelect.getSelectedItem().toString().trim();
        return user;
    }

    private String getAct()
    {
        String activity = activitySelect.getSelectedItem().toString().trim();
        return activity;
    }

    private String isTaskName()
    {
        String task = taskName.getText().toString();
        if(task.isEmpty()) { taskName.setError("Task Name Be Empty"); }
        return task;
    }


    private String createdOn()
    {
        Calendar calendar = Calendar.getInstance();
        Timestamp delegationTimestamp = new Timestamp(calendar.getTime().getTime());
        return delegationTimestamp.toString();
    }

    // Getting Users List
    public ArrayList<String> getUserList(){

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

    // Getting Activity List
    public ArrayList<ActivityDataModel> getActivityList(String userId){

        ArrayList<ActivityDataModel> activityList = new ArrayList();
        ActivityDataModel activityDataModel;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from ACTIVITY WHERE FK_AUTHENTICATION_USER_ID = ?");
            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                activityDataModel = new ActivityDataModel();

                activityDataModel.setActivityId(rs.getString("ID"));
                activityDataModel.setActivityName(rs.getString("NAME"));

                activityList.add(activityDataModel);

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return activityList;

    }

    // Getting Task List
    public ArrayList<String> getTaskNameList(String userId){

        ArrayList<String> activityList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select DISTINCT NAME from TASK WHERE FK_AUTHENTICATION_USER_ID = ?");
            ps.setString(1, userId);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                String s = rs.getString("NAME");

                activityList.add(s);

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return activityList;

    }

    // Insert Task Data into Table
    public String insertTask(String userId, String activityId,String taskName,String createdOn){
        String result="false";
        int x = 0;
        Connection con;

        try{
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("insert into TASK(NAME,FK_ACTIVITY_ID,FK_AUTHENTICATION_USER_ID,CREATED_ON) values(?,?,?,?)");

            ps.setString(1, taskName);
            ps.setString(2, activityId);
            ps.setString(3, userId);
            ps.setString(4, createdOn);

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
