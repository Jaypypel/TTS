package com.example.neptune.ttsapp;

public class User {
    private final String fullName;
    private final String username;
    private final String password;
    private final String email;
    private final String mobileNo;
    private final String userRole;

    public User(String fullName, String userName, String password, String email, String mobileNo, String userRole) {
        this.fullName = fullName;
        this.username = userName;
        this.password = password;
        this.email = email;
        this.mobileNo = mobileNo;
        this.userRole = userRole;
    }

    public User(String username){
        this.fullName =null;
        this.username = username;
        this.password = null;
        this.email = null;
        this.mobileNo = null;
        this.userRole = "";
    }
    public String getFullName() {
        return fullName;
    }

    @Override
    public String toString() {
        return "User{" +
                "fullName='" + fullName + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", email='" + email + '\'' +
                ", mobileNo='" + mobileNo + '\'' +
                ", userRole='" + userRole + '\'' +
                '}';
    }

    public String getUsername() {

        return username;
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
