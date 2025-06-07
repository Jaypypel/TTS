package com.example.neptune.ttsapp.DTO;

import com.example.neptune.ttsapp.MeasurableListDataModel;

import java.util.List;

public class AddTimeshareDto {
  private final TimeShareDTO timeShareDto;
  private final  List<MeasurableListDataModel> timeshareMeasurables;


    public AddTimeshareDto(TimeShareDTO timeShareDTO, List<MeasurableListDataModel> associatedMeasurables) {
        this.timeShareDto = timeShareDTO;
        this.timeshareMeasurables = associatedMeasurables;
    }
}
