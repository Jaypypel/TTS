package com.example.neptune.ttsapp;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

public class TTSOtherActivityCRUDFragment extends Fragment {

    public TTSOtherActivityCRUDFragment() { }

    private TextView user,date,time;
    private AutoCompleteTextView otherActivityName;
    private Button addOtherActivity;

    private SessionManager sessionManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_ttsother_activity_crud, container, false);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        user=(TextView)view.findViewById(R.id.textViewOtherActivityCRUDUser);
        sessionManager = new SessionManager(getActivity().getApplicationContext());
        user.setText(sessionManager.getUserID());

        date=(TextView)view.findViewById(R.id.textViewOtherActivityCRUDDate);
        time=(TextView)view.findViewById(R.id.textViewOtherActivityCRUDTime);


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

        otherActivityName=(AutoCompleteTextView) view.findViewById(R.id.editTextOtherActivityCRUDOtherActivity);
        if (InternetConnectivity.isConnected()== true) {
            ArrayAdapter<String> measurableNameAdapter = new ArrayAdapter<String>(getActivity(),android.R.layout.simple_list_item_1,getOtherActivityList());
            otherActivityName.setAdapter(measurableNameAdapter);
        }else {Toast.makeText(getActivity().getApplicationContext(), "No Internet Connection", Toast.LENGTH_LONG).show();}


        addOtherActivity=(Button)view.findViewById(R.id.buttonOtherActivityCRUDAddOtherActivity);


        addOtherActivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    if (InternetConnectivity.isConnected()== true)
                    {
                        if (isOtherActivityName().isEmpty()){otherActivityName.setError("Other Activity Name Be Empty");}
                        else
                        {
                            String result = insertOtherActivity(isOtherActivityName(), createdOn());
                            if (result.equals("true")) {
                                Toast.makeText(getActivity().getApplicationContext(), "Other Activity Inserted ", Toast.LENGTH_LONG).show();
                                otherActivityName.setText("");
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

    private String isOtherActivityName()
    {
        String actName = otherActivityName.getText().toString();
        if(actName.isEmpty()) { otherActivityName.setError("Other Activity Name Be Empty"); }
        return actName;
    }

    private String createdOn()
    {
        Calendar calendar = Calendar.getInstance();
        Timestamp delegationTimestamp = new Timestamp(calendar.getTime().getTime());
        return delegationTimestamp.toString();
    }


    // Getting Measurable List
    public ArrayList<String> getOtherActivityList(){

        ArrayList activityList = new ArrayList();

        Connection con;
        try {
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("select * from OTHER_ACTIVITY");

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
    public String insertOtherActivity(String otherActivityName,String createdOn){
        String result="false";
        int x = 0;
        Connection con;

        try{
            con = DatabaseHelper.getDBConnection();

            PreparedStatement ps = con.prepareStatement("insert into OTHER_ACTIVITY(NAME,CREATED_ON) values(?,?)");

            ps.setString(1, otherActivityName);
            ps.setString(2, createdOn);



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
