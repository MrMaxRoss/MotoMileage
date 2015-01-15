package com.sortedunderbelly.motomileage;

import android.test.AndroidTestCase;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by max.ross on 1/14/15.
 */
public class TripFilterTest extends AndroidTestCase {

    public void testLastFullMonth() {
        TripFilter.setNow(new GregorianCalendar(2015, Calendar.JANUARY, 15));
        assertEquals(new GregorianCalendar(2014, Calendar.DECEMBER, 1).getTime(), TripFilter.LAST_FULL_MONTH.getEarliest());
        assertEquals(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), TripFilter.LAST_FULL_MONTH.getLatest());

        TripFilter.setNow(new GregorianCalendar(2015, Calendar.FEBRUARY, 15));
        assertEquals(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), TripFilter.LAST_FULL_MONTH.getEarliest());
        assertEquals(new GregorianCalendar(2015, Calendar.FEBRUARY, 1).getTime(), TripFilter.LAST_FULL_MONTH.getLatest());
    }

    public void testMonthThusFar() {
        TripFilter.setNow(new GregorianCalendar(2015, Calendar.JANUARY, 15));
        assertEquals(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), TripFilter.MONTH_THUS_FAR.getEarliest());
        assertEquals(null, TripFilter.MONTH_THUS_FAR.getLatest());
    }

    public void testLastFullYear() {
        TripFilter.setNow(new GregorianCalendar(2015, Calendar.JANUARY, 15));
        assertEquals(new GregorianCalendar(2014, Calendar.JANUARY, 1).getTime(), TripFilter.LAST_FULL_YEAR.getEarliest());
        assertEquals(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), TripFilter.LAST_FULL_YEAR.getLatest());
    }

    public void testYearThusFar() {
        TripFilter.setNow(new GregorianCalendar(2015, Calendar.OCTOBER, 15));
        assertEquals(new GregorianCalendar(2015, Calendar.JANUARY, 1).getTime(), TripFilter.YEAR_THUS_FAR.getEarliest());
        assertEquals(null, TripFilter.YEAR_THUS_FAR.getLatest());
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        TripFilter.clearNow();
    }
}
