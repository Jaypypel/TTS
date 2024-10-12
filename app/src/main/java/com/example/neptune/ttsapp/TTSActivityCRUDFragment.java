package com.example.neptune.ttsapp;



import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
//import androidx.fragment.app.Fragment;
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

import androidx.fragment.app.Fragment;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


public class TTSActivityCRUDFragment extends Fragment {

    public TTSActivityCRUDFragment() { }

    private TextView user,date,time;
    private AutoCompleteTextView activityName;
    private Button addActivity;
    private Spinner userSelect;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsactivity_crud, container, false);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        user=(TextView)view.findViewById(R.id.textViewActCRUDUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=(TextView)view.findViewById(R.id.textViewActCRUDDate);
        time=(TextView)view.findViewById(R.id.textViewActCRUDTime);


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

        activityName =(AutoCompleteTextView) view.findViewById(R.id.editTextActCRUDActivity);

        userSelect=(Spinner) view.findViewById(R.id.spinnerActCRUDUserSelect);
        if (InternetConnectivity.isConnected()== true) {
            ArrayList users = getUserList();
            users.add(0, "Select User");
            ArrayAdapter<String> userSelectAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item,users);
            userSelectAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            userSelect.setAdapter(userSelectAdapter);
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        try {
            userSelect.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    // Set AutoCompleteTextView
                    if (InternetConnectivity.isConnected() == true) {

                        activityName.setText("");

                        ArrayAdapter<String> activityNameAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_1, getActivityList(getUser()));
                        activityName.setAdapter(activityNameAdapter);

                    } else {
                        Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) { }
            });
        }catch (Exception e){e.printStackTrace();}

        addActivity=(Button)view.findViewById(R.id.buttonActCRUDAddAct);


        addActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    if (InternetConnectivity.isConnected()== true)
                    {
                        if(isActivityName().isEmpty()){activityName.setError("Activity Name Be Empty");}
                        else
                            {
                                String result = insertActivity(getUser(), isActivityName(), createdOn());
                                if (result.equals("true")) {
                                    Toast.makeText(getActivity().getApplicationContext(), "Activity Inserted ", Toast.LENGTH_LONG).show();
                                    activityName.setText("");
                                } else {
                                    Toast.makeText(getActivity().getApplicationContext(), "Insertion Failed", Toast.LENGTH_LONG).show();
                                }
                            }
                    }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}

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

    private String isActivityName()
    {
        String actName = activityName.getText().toString();
        if(actName.isEmpty()) { activityName.setError("Activity Name Be Empty"); }
        return actName;
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
    public ArrayList<String> getActivityList(String userId){

        ArrayList<String> activityList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select DISTINCT NAME from ACTIVITY WHERE FK_AUTHENTICATION_USER_ID = ?");
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

    // Insert Other Activity Data into Table
    public String insertActivity(String userId, String activityName,String createdOn){
        String result="false";
        int x = 0;
        Connection con;

        try{
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("insert into ACTIVITY(NAME,FK_AUTHENTICATION_USER_ID,CREATED_ON) values(?,?,?)");

            ps.setString(1, activityName);
            ps.setString(2, userId);
            ps.setString(3, createdOn);



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
