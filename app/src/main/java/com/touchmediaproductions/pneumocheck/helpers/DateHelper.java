package com.touchmediaproductions.pneumocheck.helpers;

import android.util.Log;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class responsible for converting and manipulating dates
 */
public class DateHelper {

    final static String DATEHELPERTAG = "DATEHELPER";

    /**
     * Parses from ISO8601 to a Date object
     *
     * @param iso8601StringDate
     * @return
     */
    public static Date fromIso8601StringBackToDate(String iso8601StringDate) {
        Date convertedDate = null;
        //Parse the dates to current local time:
        DateFormat iso8601DateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
        try {
            convertedDate = iso8601DateFormat.parse(iso8601StringDate);
        } catch (ParseException e) {
            Log.d(DATEHELPERTAG, e.getMessage());
        }
        return convertedDate;
    }

    /**
     * Date object converted to ISO8601String for compliance with SQLITE3 string date compatibility
     *
     * @param date
     * @return
     */
    public static String fromDateToISO8601String(Date date) {
        String formattedDate = null;
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssz");
            try {
                formattedDate = formatter.format(date);
            } catch (Exception e) {
                Log.d(DATEHELPERTAG, e.getMessage());
            }
        }
        return formattedDate;
    }

    public static String fromDateToDisplayPrettyShortDateString(Date date) {
        String formattedDate = null;
        if (date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy z");
            try {
                formattedDate = formatter.format(date);
            } catch (Exception e) {
                Log.d(DATEHELPERTAG, e.getMessage());
            }
        }
        return formattedDate;
    }
}
