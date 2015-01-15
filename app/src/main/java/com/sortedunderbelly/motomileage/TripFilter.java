package com.sortedunderbelly.motomileage;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by max.ross on 5/17/14.
 */
public enum TripFilter {

    MONTH_THUS_FAR {
        @Override
        public Date getEarliest() {
            return getFirstOfCurrentMonth();
        }

        @Override
        public Date getLatest() {
            return null;
        }
    },
    LAST_FULL_MONTH {
        @Override
        public Date getEarliest() {
            Date latest = getFirstOfCurrentMonth();
            Calendar firstOfLastFullMonth = nowAtMidnight();
            firstOfLastFullMonth.setTime(latest);
            firstOfLastFullMonth.add(Calendar.MONTH, -1);
            return firstOfLastFullMonth.getTime();
        }

        @Override
        public Date getLatest() {
            return getFirstOfCurrentMonth();
        }
    },
    YEAR_THUS_FAR {
        @Override
        public Date getEarliest() {
            return getFirstOfCurrentYear();
        }

        @Override
        public Date getLatest() {
            return null;
        }
    },
    LAST_FULL_YEAR {
        @Override
        public Date getEarliest() {
            Date latest = getFirstOfCurrentYear();
            Calendar firstOfLastFullYear = nowAtMidnight();
            firstOfLastFullYear.setTime(latest);
            firstOfLastFullYear.roll(Calendar.YEAR, -1);
            return firstOfLastFullYear.getTime();
        }

        @Override
        public Date getLatest() {
            return getFirstOfCurrentYear();
        }
    },
    ALL {
        @Override
        public Date getEarliest() {
            return null;
        }

        @Override
        public Date getLatest() {
            return null;
        }
    };

    private static volatile Calendar NOW_OVERRIDE = null;

    static void setNow(Calendar now) {
        NOW_OVERRIDE = now;
    }

    static void clearNow() {
        NOW_OVERRIDE = null;
    }

    // Inclusive
    public abstract Date getEarliest();

    // Exclusive
    public abstract Date getLatest();


    private static void toMidnight(Calendar cal) {
        cal.set(Calendar.HOUR, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
    }

    private static Date getFirstOfCurrentYear() {
        Calendar firstOfTheYear = nowAtMidnight();
        firstOfTheYear.set(Calendar.DAY_OF_YEAR, 1);
        return firstOfTheYear.getTime();
    }

    private static Date getFirstOfCurrentMonth() {
        Calendar firstOfTheMonth = nowAtMidnight();
        firstOfTheMonth.set(Calendar.DAY_OF_MONTH, 1);
        toMidnight(firstOfTheMonth);
        return firstOfTheMonth.getTime();
    }

    public int filterTrips(List<Trip> allTrips, List<Trip> result) {
        Date earliest = getEarliest();
        Date latest = getLatest();
        int totalDistance = 0;
        for (Trip trip : allTrips) {
            if (filterTrip(trip, earliest, latest)) {
                result.add(trip);
                totalDistance += trip.getDistance();
            }
        }
        Collections.sort(result);
        return totalDistance;
    }

    private static boolean filterTrip(Trip trip, Date earliest, Date latest) {
        return (earliest == null || !trip.getDate().before(earliest)) && (latest == null || trip.getDate().before(latest));
    }

    public boolean filterTrip(Trip trip) {
        Date earliest = getEarliest();
        Date latest = getLatest();
        return filterTrip(trip, earliest, latest);
    }

    private static Calendar nowAtMidnight() {
        Calendar now = now();
        toMidnight(now);
        return now;
    }

    private static Calendar now() {
        Calendar now = Calendar.getInstance();
        if (NOW_OVERRIDE != null) {
            now.setTime(NOW_OVERRIDE.getTime());
        }
        return now;
    }
}