package com.rnbiometrics;

import android.util.Log;

public class ExceptionLogger {

    private static final String BIOMETRY_TAG = "BIOMETRY";

    public static void log(Exception e) {
        e.printStackTrace();
        Log.e(BIOMETRY_TAG, "Error in biometry: " + e.getMessage());
    }

    public static String getExceptionMessage(String reason) {
        return "Cannot generate keys: " + reason + " check native logs for more info. TAG: " + BIOMETRY_TAG;
    }
}
