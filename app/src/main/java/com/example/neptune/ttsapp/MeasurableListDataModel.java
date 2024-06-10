package com.example.neptune.ttsapp;

import java.io.Serializable;

public class MeasurableListDataModel implements Serializable {

    private String id;
    private String measurableName;
    private String measurableQty;
    private String measurableUnit;

    public MeasurableListDataModel() { }

    public MeasurableListDataModel(String measurableName, String measurableQty, String measurableUnit) {

        this.measurableName = measurableName;
        this.measurableQty = measurableQty;
        this.measurableUnit = measurableUnit;
    }


    @Override
    public String toString() { return this.id + "-" + this.measurableName; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMeasurableName() {
        return measurableName;
    }

    public void setMeasurableName(String measurableName) {
        this.measurableName = measurableName;
    }

    public String getMeasurableQty() {
        return measurableQty;
    }

    public void setMeasurableQty(String measurableQty) {
        this.measurableQty = measurableQty;
    }

    public String getMeasurableUnit() {
        return measurableUnit;
    }

    public void setMeasurableUnit(String measurableUnit) {
        this.measurableUnit = measurableUnit;
    }
}
