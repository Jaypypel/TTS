package com.example.neptune.ttsapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TimeShareListCustomAdapter extends ArrayAdapter<TimeShareDataModel> implements View.OnClickListener {

    private ArrayList<TimeShareDataModel> dataSet;
    Context mContext;

    // View lookup cache
    private static class ViewHolder {
        TextView timeShareDate;
        TextView timeShareStartTime;
        TextView timeShareEndTime;
        TextView timeShareTimeDiff;
        TextView timeShareDescription;
    }


    public TimeShareListCustomAdapter(ArrayList<TimeShareDataModel> data, Context context) {
        super(context, R.layout.time_share_row_item, data);
        this.dataSet = data;
        this.mContext=context;

    }


    @Override
    public void onClick(View v) {

        int position=(Integer) v.getTag();
        Object object= getItem(position);
        TimeShareDataModel dataModel=(TimeShareDataModel)object;


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

        TimeShareDataModel dataModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        final View result;

        if (convertView == null) {


            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.time_share_row_item, parent, false);
            viewHolder.timeShareDate = (TextView) convertView.findViewById(R.id.timeShareDate);
            viewHolder.timeShareStartTime = (TextView) convertView.findViewById(R.id.timeShareStartTime);
            viewHolder.timeShareEndTime = (TextView) convertView.findViewById(R.id.timeShareEndTime);
            viewHolder.timeShareTimeDiff = (TextView) convertView.findViewById(R.id.timeShareTimeDiff);
            viewHolder.timeShareDescription = (TextView) convertView.findViewById(R.id.timeShareDes);

            result=convertView;

            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
            result=convertView;
        }
        String timeShareDate = dataModel != null ? dataModel.getTimeShareDate() : "Not found";
        String timeSharesStartTime = dataModel != null ? dataModel.getStartTime() : "Not found";
        String timeShareEndTime = dataModel != null ? dataModel.getEndTime() : "Not found";
        String timeShareTimeDiff = dataModel != null ? dataModel.getTimeDifference() : "Not found";
        String timeShareDescription = dataModel != null ? dataModel.getTimeShareDescription() : "Not found";


        viewHolder.timeShareDate.setText(timeShareDate);
        viewHolder.timeShareStartTime.setText(timeSharesStartTime);
        viewHolder.timeShareEndTime.setText(timeShareEndTime);
        viewHolder.timeShareTimeDiff.setText(timeShareTimeDiff);
        viewHolder.timeShareDescription.setText(timeShareDescription);

        return result;
    }
}
