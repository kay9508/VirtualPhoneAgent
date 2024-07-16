package com.sinyoung.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class DateUtil {

    /**
     *
     * @param from
     * @param end
     * @return
     */
    public static long getDiffTime(Date from, Date end) {
        long diff = end.getTime() - from.getTime();
        return diff/1000;
    }

    public static String getCurrentDate() {
       return getCurrentDate(null);
    }

    public static String getCurrentDate(String pattern) {
        String now = "";
        if(pattern != null) {
            now = LocalDateTime.now().format(DateTimeFormatter.ofPattern(pattern));
        } else {
            now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
        }
        return now;
    }
}
