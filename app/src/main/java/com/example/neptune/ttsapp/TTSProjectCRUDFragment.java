package com.example.neptune.ttsapp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
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


public class TTSProjectCRUDFragment extends Fragment {

    public TTSProjectCRUDFragment() { }

    private TextView user,date,time;
    private AutoCompleteTextView projectName,projectCode;
    private Button addProject;
    private Spinner userSelect,activitySelect;

    private SessionManager sessionManager;

    private ArrayList<ActivityDataModel> activityDataModels;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsproject_crud, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        user=(TextView)view.findViewById(R.id.textViewProjectCRUDUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=(TextView)view.findViewById(R.id.textViewProjectCRUDDate);
        time=(TextView)view.findViewById(R.id.textViewProjectCRUDTime);


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



        projectCode=(AutoCompleteTextView) view.findViewById(R.id.editTextProjectCRUDProjectCode);
        ArrayAdapter<String> projectCodeAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getProjectCodeList());
        projectCode.setAdapter(projectCodeAdapter);

        projectName=(AutoCompleteTextView) view.findViewById(R.id.editTextProjectCRUDProjectName);
        ArrayAdapter<String> projectNameAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getProjectNameList());
        projectName.setAdapter(projectNameAdapter);

        addProject=(Button)view.findViewById(R.id.buttonProjectCRUDAdd);

        if (InternetConnectivity.isConnected()) {
            userSelect=(Spinner) view.findViewById(R.id.spinnerProjectCRUDUserSelect);
            ArrayList users = getUserList();
            users.add(0, "Select User");
            ArrayAdapter<String> adapterMeasurable = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
            adapterMeasurable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            userSelect.setAdapter(adapterMeasurable);
        }else { Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


    activitySelect=(Spinner) view.findViewById(R.id.spinnerProjectCRUDActivitySelect);
        if (InternetConnectivity.isConnected()) {
            userSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    activityDataModels = getActivityList(getUser());
                    ArrayAdapter<ActivityDataModel> activitySelectAdapter = new ArrayAdapter<ActivityDataModel>(getActivity(), android.R.layout.simple_spinner_item,activityDataModels);
                    activitySelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    activitySelect.setAdapter(activitySelectAdapter);

                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {
                }
            });
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}



        addProject.setOnClickListener(v -> {

            try
            {
                if (InternetConnectivity.isConnected())
                {
                   if (isProjectCode().isEmpty()){projectCode.setError("Project Code Be Empty");}
                   else if (isProjectName().isEmpty()){projectName.setError("Project Name Be Empty");}
                   else
                   {
                       String result = insertProject(getUser(), getAct(), isProjectCode(), isProjectName(), createdOn());
                       if (result.equals("true")) {
                           Toast.makeText(getActivity().getApplicationContext(), "Project Inserted ", Toast.LENGTH_LONG).show();
                           projectCode.setText("");
                           projectName.setText("");
                       } else {
                           Toast.makeText(getActivity().getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show();
                       }
                   }
                }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


            }
            catch (Exception e){e.printStackTrace();}
        });

        return view;
    }

    private String getUser()
    {
        return userSelect.getSelectedItem().toString().trim();
    }

    private String getAct()
    {
        return activitySelect.getSelectedItem().toString().trim();
    }

    private String isProjectCode()
    {
        String projCode = projectCode.getText().toString();
        if(projCode.isEmpty()) { projectCode.setError("Project Code Be Empty"); }
        return projCode;
    }

    private String isProjectName()
    {
        String projName = projectName.getText().toString();
        if(projName.isEmpty()) { projectName.setError("Project Name Be Empty"); }
        return projName;
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
        } catch (Exception e) { e.printStackTrace(); }

        return userNameList;

    }

    // Getting Activity List
    public ArrayList<ActivityDataModel> getActivityList(String userId){

        ArrayList<ActivityDataModel> activityList = new ArrayList();
        ActivityDataModel activityDataModel;

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select DISTINCT * from ACTIVITY WHERE FK_AUTHENTICATION_USER_ID = ?");
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
        } catch (Exception e) { e.printStackTrace(); }

        return activityList;

    }

    // Getting Project Name List
    public ArrayList<String> getProjectNameList(){

        ArrayList projectNameList = new ArrayList();

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

    // Getting Project Name List
    public ArrayList<String> getProjectCodeList(){

        ArrayList projectCodeList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select DISTINCT CODE from PROJECT");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                String s = rs.getString("CODE");

                projectCodeList.add(s);

            }
            rs.close();
            ps.close();
            con.close();
        } catch (Exception e) { e.printStackTrace(); }

        return projectCodeList;

    }

    // Insert Project Data into Table
    public String insertProject(String userId, String activityId,String projectCode,String projectName,String createdOn){
        String result="false";
        int x = 0;
        Connection con;

        try{
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("insert into PROJECT(CODE,NAME,FK_ACTIVITY_ID,FK_AUTHENTICATION_USER_ID,CREATED_ON) values(?,?,?,?,?)");

            ps.setString(1, projectCode);
            ps.setString(2, projectName);
            ps.setString(3, activityId);
            ps.setString(4, userId);
            ps.setString(5, createdOn);



            x = ps.executeUpdate();

            if(x==1){
                result = "true";
            }

            ps.close();
            con.close();
        }
        catch(Exception e){ e.printStackTrace(); }

        return result;
    }

}
