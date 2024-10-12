package com.example.neptune.ttsapp;

public class User {
    private final String fullName;
    private final String userName;
    private final String password;
    private final String email;
    private final String mobileNo;

    public User(String fullName, String userName, String password, String email, String mobileNo) {
        this.fullName = fullName;
        this.userName = userName;
        this.password = password;
        this.email = email;
        this.mobileNo = mobileNo;
    }

    public String getFullName() {
        return fullName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public String getMobileNo() {
        return mobileNo;
    }
}
