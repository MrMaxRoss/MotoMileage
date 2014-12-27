package com.sortedunderbelly.motomileage;

/**
 * Created by max.ross on 7/26/14.
 */
public abstract class BaseTrip implements Trip {
    @Override
    public final String getId() {
        if (!hasId()) {
//            throw new IllegalStateException("Cannot call getId() on an object that does not yet have an id assigned.");
        }
        return getIdInternal();
    }

    public abstract String getIdInternal();

    @Override
    public final int compareTo(Trip trip) {
        // compare by date, then by distance, then by description, then by id (id isn't visible
        // to the user but it gives us a stable ordering)
        int dateCompare = getDate().compareTo(trip.getDate());
        if (dateCompare != 0) {
            return dateCompare;
        }

        int distanceCompare = Integer.valueOf(getDistance()).compareTo(trip.getDistance());
        if (distanceCompare != 0) {
            return distanceCompare;
        }

        int descCompare = getDesc().compareTo(trip.getDesc());
        if (descCompare != 0) {
            return descCompare;
        }
        if (hasId()) {
            if (trip.hasId()) {
                // both tripsRef have ids
                return getId().compareTo(trip.getId());
            }
            // only this trip has an id
            return 1;
        } else { // this trip does not have an id
            if (trip.hasId()) {
                // the other trip has an id
                return -1;
            }
            // neither trip has an id
            return 0;
        }
    }

    @Override
    public final String toString() {
        return String.format("%s, %s, %s miles", getDate(), getDesc(), getDistance());
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        BaseTrip trip = (BaseTrip) o;
        return getId().equals(trip.getId());
    }

    @Override
    public final int hashCode() {
        return getId().hashCode();
    }

}
