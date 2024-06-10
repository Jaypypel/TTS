package com.example.neptune.ttsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;


public class DailyTimeShareListCustomAdapter extends ArrayAdapter<DailyTimeShareDataModel> implements View.OnClickListener{

    private ArrayList<DailyTimeShareDataModel> dataSet;
    private Context mContext;

    // View lookup cache
    private static class ViewHolder {
    //    TextView txttaskDeligateOwnerName;
        TextView txtdailyTimeShareProjName;
//        TextView txtdailyTimeShareActName;
        TextView txtdailyTimeShareTaskName;
        TextView txtdailyTimeShareStartTime;
        TextView txtdailyTimeShareEndTime;
//        TextView txtdailyTimeShareConsumedTime;
      //  Button gotoTimeshare;

    }



    public DailyTimeShareListCustomAdapter(ArrayList<DailyTimeShareDataModel> data, Context context) {
        super(context, R.layout.daily_time_share_row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }


    @Override
    public void onClick(View v) {


        int position=(Integer) v.getTag();
        Object object= getItem(position);
        DailyTimeShareDataModel dataModel=(DailyTimeShareDataModel)object;




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

        DailyTimeShareDataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {


            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.daily_time_share_row_item, parent, false);

            viewHolder.txtdailyTimeShareProjName = (TextView) convertView.findViewById(R.id.dailyTimeShareProjName);
//            viewHolder.txtdailyTimeShareActName= (TextView) convertView.findViewById(R.id.dailyTimeShareActName);
            viewHolder.txtdailyTimeShareTaskName = (TextView) convertView.findViewById(R.id.dailyTimeShareTaskName);
            viewHolder.txtdailyTimeShareStartTime = (TextView) convertView.findViewById(R.id.dailyTimeShareStartTime);
            viewHolder.txtdailyTimeShareEndTime = (TextView) convertView.findViewById(R.id.dailyTimeShareEndTime);
//            viewHolder.txtdailyTimeShareConsumedTime = (TextView) convertView.findViewById(R.id.dailyTimeShareConsumedTime);



            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }

//        Animation animation = AnimationUtils.loadAnimation(mContext, (position > lastPosition) ? R.animator.up_from_bottom : R.animator.down_from_top);
//        result.startAnimation(animation);
//        lastPosition = position;


        viewHolder.txtdailyTimeShareProjName.setText(dataModel.getProjectName());
//        viewHolder.txtdailyTimeShareActName.setText(dataModel.getActivityName());
        viewHolder.txtdailyTimeShareTaskName.setText(dataModel.getTaskName());
        viewHolder.txtdailyTimeShareStartTime.setText(dataModel.getStartTime());
        viewHolder.txtdailyTimeShareEndTime.setText(dataModel.getEndTime());
//        viewHolder.txtdailyTimeShareConsumedTime.setText(dataModel.getConsumedTime());


        return convertView;
    }


}
