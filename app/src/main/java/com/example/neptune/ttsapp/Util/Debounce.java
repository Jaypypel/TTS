package com.example.neptune.ttsapp.Util;


/* Debounce class is used to multiple clicks */
public class Debounce {
    private static long lastClick = 0;
    private static long latestClick = 0;

    /* function  called once after debouncing*/
    public static void debounceEffect(Runnable onClickFunction){
        latestClick = System.currentTimeMillis();
        if (latestClick - lastClick < 1500){
            return;
        }
        lastClick = latestClick;
        onClickFunction.run();
    }

}
