package com.sortedunderbelly.motomileage;

import java.util.Date;

/**
 * Created by max.ross on 7/26/14.
 */
public class TripImpl extends BaseTrip {
    private final String id;
    private final Date date;
    private final String desc;
    private final int distance;

    private static final String NO_ID = "NO_ID";

    public TripImpl(String id, Date date, String desc, int distance) {
        this.id = id;
        this.date = date;
        this.desc = desc;
        this.distance = distance;
    }

    public TripImpl(Date date, String desc, int distance) {
        this(NO_ID, date, desc, distance);
    }

    @Override
    String getIdInternal() {
        return id;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public String getDesc() {
        return desc;
    }

    @Override
    public int getDistance() {
        return distance;
    }

    @Override
    public boolean hasId() {
        return !id.equals(NO_ID);
    }
}
