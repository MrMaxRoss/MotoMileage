package com.sortedunderbelly.motomileage;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * Created by maxr on 5/17/14.
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
            Calendar firstOfLastFullMonth = Calendar.getInstance();
            firstOfLastFullMonth.setTime(latest);
            firstOfLastFullMonth.roll(Calendar.MONTH, -1);
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
            Calendar firstOfLastFullYear = Calendar.getInstance();
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

    public abstract Date getEarliest();
    public abstract Date getLatest();


    private static Date getFirstOfCurrentYear() {
        Calendar firstOfTheYear = Calendar.getInstance();
        firstOfTheYear.set(Calendar.DAY_OF_YEAR, 1);
        return firstOfTheYear.getTime();
    }

    private static Date getFirstOfCurrentMonth() {
        Calendar firstOfTheMonth = Calendar.getInstance();
        firstOfTheMonth.set(Calendar.DAY_OF_MONTH, 1);
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
        return (earliest == null || !trip.getDate().before(earliest)) && (latest == null || !trip.getDate().after(latest));
    }

    public boolean filterTrip(Trip trip) {
        Date earliest = getEarliest();
        Date latest = getLatest();
        return filterTrip(trip, earliest, latest);
    }

}