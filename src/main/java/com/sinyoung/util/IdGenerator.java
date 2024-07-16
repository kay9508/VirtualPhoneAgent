package com.sinyoung.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class IdGenerator {

    /**
     * 일시(yyyymmddhhmmss)_uuid
     *
     * @return
     */
    public static String getId() {
        return getDateTime() + "_" + getUUID();
    }

    /**
     * prefix_일시(yyyymmddhhmmss)_uuid
     *
     * @param prefix
     * @return
     */
    public static String getId(String prefix) {
        return prefix + "_" + getDateTime() + "_" + getUUID();
    }

    public static String getUUID() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    public static String getDateTime() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        String datetime = format.format(new Date());
        return datetime;
    }
}
