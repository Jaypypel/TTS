package com.example.neptune.ttsapp;

import java.io.Serializable;
import java.util.Objects;

public class MeasurableListDataModel implements Serializable {

    private String id;
    private String name;
    private String measurableQty;
    private String measurableUnit;

    public MeasurableListDataModel() { }

    public MeasurableListDataModel(String measurableName, String measurableQty, String measurableUnit) {

        this.name = measurableName;
        this.measurableQty = measurableQty;
        this.measurableUnit = measurableUnit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MeasurableListDataModel that = (MeasurableListDataModel) o;
        return Objects.equals(id, that.id) && Objects.equals(name, that.name) && Objects.equals(measurableQty, that.measurableQty) && Objects.equals(measurableUnit, that.measurableUnit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, measurableQty, measurableUnit);
    }
//    public MeasurableListDataModel(String id, String measurableName) {
//
//        this.id = id;
//        this.measurableName = measurableName;
//    }


    @Override
    public String toString() { return this.id + "-" + this.name; }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMeasurableName() {
        return name;
    }

    public void setMeasurableName(String measurableName) {
        this.name = measurableName;
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
