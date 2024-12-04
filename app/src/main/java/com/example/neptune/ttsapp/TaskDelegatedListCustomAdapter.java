package com.example.neptune.ttsapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;


public class TaskDelegatedListCustomAdapter extends ArrayAdapter<TaskDataModel> implements View.OnClickListener{

    private ArrayList<TaskDataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
    //    TextView txttaskDeligateOwnerName;
        TextView txttaskDeligateReceivedName;
        TextView txttaskDeligateTaskDate;
        TextView txttaskDeligateTaskName;
        TextView txttaskDeligateTaskStatus;
      //  Button gotoTimeshare;

    }



    public TaskDelegatedListCustomAdapter(ArrayList<TaskDataModel> data, Context context) {
        super(context, R.layout.task_acceptance_row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }


    @Override
    public void onClick(View v) {


        int position=(Integer) v.getTag();
        Object object= getItem(position);
        TaskDataModel dataModel=(TaskDataModel)object;




        switch (v.getId())
        {

//            case R.id.item_info:
//
////                Snackbar.make(v, "Release date " +dataModel.getFeature(), Snackbar.LENGTH_LONG)
////                        .setAction("No action", null).show();
//
//                break;


        }


    }

    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position

        TaskDataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {


            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.task_acceptance_row_item, parent, false);

            viewHolder.txttaskDeligateReceivedName = (TextView) convertView.findViewById(R.id.taskDeligateOwnerName);
            viewHolder.txttaskDeligateTaskDate = (TextView) convertView.findViewById(R.id.taskDeligateTaskDate);
            viewHolder.txttaskDeligateTaskName = (TextView) convertView.findViewById(R.id.taskDeligateTaskName);
            viewHolder.txttaskDeligateTaskStatus = (TextView) convertView.findViewById(R.id.taskDeligateTaskStatus);



            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

//        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.animator.up_from_bottom : R.animator.down_from_top);
//        result.startAnimation(animation);
//        lastPosition = position;


        viewHolder.txttaskDeligateReceivedName.setText(dataModel.getTaskReceivedUserId());
//        if(dataModel.getDeligationDateTime() != null){
//            viewHolder.txttaskDeligateTaskDate.setText(extractDate(dataModel.getDeligationDateTime()));
//        }
        viewHolder.txttaskDeligateTaskDate.setText(extractDate(dataModel.getDeligationDateTime()));
//        viewHolder.txttaskDeligateTaskDate.setText("25-08-2001");
               if(dataModel.getDeligationDateTime() != null){
           Log.e("delegationDate from dataModel",dataModel.getDeligationDateTime());
           String d = dataModel.getDeligationDateTime();
           Log.e("delegationDate funciton"," "+extractDate(d));
       }else{
           Log.e("delegationDate", "is null ");
           Log.e("taskAllocationModle", ""+dataModel);

       }
        viewHolder.txttaskDeligateTaskName.setText(dataModel.getTaskName());
        viewHolder.txttaskDeligateTaskStatus.setText(dataModel.getStatus());


        return convertView;
    }


    private String extractDate(String timestamp)
    {
        String currentDate= "";
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy hh:mm a");
        ZonedDateTime ist = ZonedDateTime.of(LocalDateTime.parse(timestamp,dateTimeFormatter), ZoneId.of("Asia/Kolkata"));
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        currentDate = ist.format(dateFormatter);



        return currentDate;
    }
}
