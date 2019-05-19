package com.appjishu.starzone.util;

import java.text.SimpleDateFormat;
import java.util.Date;

public class DateUtil {
    public static final SimpleDateFormat DEFAULT_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:dd");

    public static String getDateString(Date date) {
        if (date == null) {
            return "";
        }
        try {
            return DEFAULT_FORMAT.format(date).trim();
        }
        catch (Exception e) {
            return "";
        }
    }
}
