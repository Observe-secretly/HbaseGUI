package com.lm.hbase.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class DateUtil {

    public static long convertMinStamp(String minStampStr, String pattern) {
        try {
            if (!StringUtil.isEmpty(minStampStr)) {
                SimpleDateFormat dataFormat = new SimpleDateFormat(pattern);
                Date date = dataFormat.parse(minStampStr);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                // calendar.set(Calendar.HOUR_OF_DAY, 0);
                // calendar.set(Calendar.MINUTE, 0);
                // calendar.set(Calendar.SECOND, 0);
                return calendar.getTimeInMillis();
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    public static long convertMaxStamp(String maxStampStr, String pattern) {
        try {
            if (!StringUtil.isEmpty(maxStampStr)) {
                SimpleDateFormat dataFormat = new SimpleDateFormat(pattern);
                Date date = dataFormat.parse(maxStampStr);
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);
                // calendar.set(Calendar.HOUR_OF_DAY, 23);
                // calendar.set(Calendar.MINUTE, 59);
                // calendar.set(Calendar.SECOND, 59);
                return calendar.getTimeInMillis();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

}
