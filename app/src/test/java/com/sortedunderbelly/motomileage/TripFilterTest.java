package com.sortedunderbelly.motomileage;

import org.junit.After;
import org.junit.Test;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static junit.framework.Assert.assertEquals;

/**
 * Created by max.ross on 1/14/15.
 */
public class TripFilterTest {

    public TripFilterTest() {

    }

    private void lastFullMonth(Calendar now1, Calendar now2) {
        TripFilter.setNow(now1);
        assertEquals(new GregorianCalendar(2014, Calendar.DECEMBER, 1).getTime(), TripFilter.LAST_FULL_MONTH.getEarliest());
        assertEquals(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), TripFilter.LAST_FULL_MONTH.getLatest());

        TripFilter.setNow(now2);
        assertEquals(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), TripFilter.LAST_FULL_MONTH.getEarliest());
        assertEquals(new GregorianCalendar(2015, Calendar.FEBRUARY, 1).getTime(), TripFilter.LAST_FULL_MONTH.getLatest());

    }

    @Test
    public void testLastFullMonth() {
        for (int dayOfMonth = 1; dayOfMonth < 28; dayOfMonth++) {
            lastFullMonth(
                    new GregorianCalendar(2015, Calendar.JANUARY, dayOfMonth, 13, 22, 11),
                    new GregorianCalendar(2015, Calendar.FEBRUARY, dayOfMonth, 13, 22, 11));
            lastFullMonth(
                    new GregorianCalendar(2015, Calendar.JANUARY, dayOfMonth, 2, 22, 11),
                    new GregorianCalendar(2015, Calendar.FEBRUARY, dayOfMonth, 2, 22, 11));
        }
    }

    @Test
    public void testMonthThusFar() {
        for (int dayOfMonth = 1; dayOfMonth < 28; dayOfMonth++) {
            TripFilter.setNow(new GregorianCalendar(2015, Calendar.JANUARY, dayOfMonth, 13, 22, 11));
            assertEquals(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), TripFilter.MONTH_THUS_FAR.getEarliest());
            assertEquals(null, TripFilter.MONTH_THUS_FAR.getLatest());
        }
    }

    @Test
    public void testLastFullYear() {
        for (int dayOfMonth = 1; dayOfMonth < 28; dayOfMonth++) {
            TripFilter.setNow(new GregorianCalendar(2015, Calendar.JANUARY, dayOfMonth, 13, 22, 11));
            assertEquals(new GregorianCalendar(2014, Calendar.JANUARY, 1).getTime(), TripFilter.LAST_FULL_YEAR.getEarliest());
            assertEquals(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), TripFilter.LAST_FULL_YEAR.getLatest());
        }
    }

    @Test
    public void testYearThusFar() {
        for (int dayOfMonth = 1; dayOfMonth < 28; dayOfMonth++) {
            TripFilter.setNow(new GregorianCalendar(2015, Calendar.OCTOBER, dayOfMonth, 13, 22, 11));
            assertEquals(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), TripFilter.YEAR_THUS_FAR.getEarliest());
            assertEquals(null, TripFilter.YEAR_THUS_FAR.getLatest());
        }
    }

    @After
    public void tearDown() throws Exception {
        TripFilter.clearNow();
    }
}
