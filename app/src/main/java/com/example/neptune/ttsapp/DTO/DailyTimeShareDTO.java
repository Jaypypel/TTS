package com.example.neptune.ttsapp.DTO;

import com.example.neptune.ttsapp.DailyTimeShare;
import com.example.neptune.ttsapp.MeasurableListCustomAdapter;
import com.example.neptune.ttsapp.MeasurableListDataModel;

import java.util.List;

public class DailyTimeShareDTO {

    private DailyTimeShare dailyTimeShare;
    List<MeasurableListDataModel> dailyTimeShareMeasurablesList;

    public DailyTimeShareDTO(DailyTimeShare dailyTimeShare, List<MeasurableListDataModel> dailyTimeShareMeasurablesList) {
        this.dailyTimeShare = dailyTimeShare;
        this.dailyTimeShareMeasurablesList = dailyTimeShareMeasurablesList;
    }
}
