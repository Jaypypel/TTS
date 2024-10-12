package com.example.neptune.ttsapp;



import android.util.Log;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseHelper {

//    private static final String url="jdbc:mysql://ecologic.org.in/ecologic_ForTest";
//    private static final String url="jdbc:mysql://ecologic.org.in/ecologic_TTS";
//    private static final String usr = "ecologic_YoDo";
//    private static final String pass="password123";
    private static final String url="jdbc:mysql://localhost:3306/tts7";
    private static final String usr = "root";
    private static final String pass="root";

    public static Connection getDBConnection() {

        Connection dbConnection = null;

        try {

            Class.forName("com.mysql.jdbc.Driver");
//            Class.forName("com.mysql.cj.jdbc.Driver");
            Log.d("","Load Driver success");
        } catch (Exception e) { System.out.println(e.getMessage()); }

        try
        {
            dbConnection = DriverManager.getConnection(url, usr, pass);
            Log.d("","Database connection success");
            return dbConnection;

        } catch (SQLException e) { System.out.println(e.getMessage()); }


        return dbConnection;

    }

}
