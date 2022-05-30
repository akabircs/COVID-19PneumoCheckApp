package com.touchmediaproductions.pneumocheck.helpers;

import org.junit.Test;

import java.time.Instant;
import java.util.Date;

import static org.junit.Assert.*;

/**
 * Test the DateHelper Class
 */
public class DateHelperTest {

    @Test
    public void fromIso8601StringBackToDate() {
        Date date = DateHelper.fromIso8601StringBackToDate("2020-10-08T02:01:26+0000");
        Date expectedDate = Date.from(Instant.parse("2020-10-08T02:01:26Z"));
        assertEquals(date, expectedDate);
    }

    @Test
    public void fromDateToISO8601String() {
        Date date = Date.from(Instant.parse("2020-10-08T02:01:26z"));
        String isoStringReturned = DateHelper.fromDateToISO8601String(date);
        String expectedString = "2020-10-08T13:01:26AEDT";
        assertEquals(isoStringReturned, expectedString);
    }

    @Test
    public void fromDateToDisplayPrettyShortDateString() {
        Date date = Date.from(Instant.parse("2020-10-08T02:01:26z"));
        String prettyShortDateStringReturned = DateHelper.fromDateToDisplayPrettyShortDateString(date);
        String expectedString = "08-10-2020 AEDT";
        assertEquals(prettyShortDateStringReturned, expectedString);
    }
}