package com.example.neptune.ttsapp;

public class ActivityDataModel {

    private String id;
    private String name;

    public String getid() {
        return id;
    }

    public void setid(String id) {
        this.id = id;
    }

    public String getname() {
        return name;
    }

    public void setname(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.id + "-" + this.name;
    }
}
