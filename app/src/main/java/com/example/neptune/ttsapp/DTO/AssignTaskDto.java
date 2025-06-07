package com.example.neptune.ttsapp.DTO;

import com.example.neptune.ttsapp.MeasurableListDataModel;

import java.util.List;

public class AssignTaskDto {
    private final TaskManagement dto;
    private final List<MeasurableListDataModel> associatedMeasurableDtos;

    public AssignTaskDto(TaskManagement dto, List<MeasurableListDataModel> associatedMeasurableDtos) {
        this.dto = dto;
        this.associatedMeasurableDtos = associatedMeasurableDtos;
    }
}
