package com.yurixahri.ahrify.utils;

import java.util.Locale;

public class TimeFormat {
    public static String msToString(long millis){
        short hours = 0;
        if (millis >= 3600000) hours = (short) (millis / 1000 / 60 / 60);
        short minutes = (short) (millis / 1000 / 60);
        short seconds = (short) (millis / 1000 % 60);
        if (hours > 0) {
            return String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
        }else {
            return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds);
        }
    }
}
