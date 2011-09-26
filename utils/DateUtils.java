package com.jsonsync.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import com.jsonsync.JSONSyncRuntimeException;

/**
 *
 * @author Arnaud CAVAILHEZ
 */
public class DateUtils {

    private final static SimpleDateFormat simpleDateFormat;
    private final static String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss Z";

    static {
        simpleDateFormat = new SimpleDateFormat(DATE_FORMAT);
        simpleDateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public static synchronized Date decode(String dateString) {
        try {
            return simpleDateFormat.parse(dateString);
        } catch (ParseException ex) {

            throw new JSONSyncRuntimeException(ex);
        }
    }

    public static synchronized String encode(Date date) {
        return simpleDateFormat.format(date);
    }
}
