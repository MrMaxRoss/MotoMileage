package com.sortedunderbelly.motomileage;

import com.parse.ParseObject;

import java.util.Date;

/**
 * Created by max.ross on 7/26/14.
 */
public class ParseTripImpl extends BaseTrip {
    private final ParseObject obj;

    public ParseTripImpl(ParseObject obj) {
        this.obj = obj;
    }

    public ParseObject getObj() {
        return obj;
    }

    @Override
    String getIdInternal() {
        return obj.getObjectId();
    }

    @Override
    public Date getDate() {
        return obj.getDate("date");
    }

    @Override
    public String getDesc() {
        return obj.getString("desc");
    }

    @Override
    public int getDistance() {
        return obj.getNumber("distance").intValue();
    }

    @Override
    public boolean hasId() {
        return getIdInternal() != null;
    }
}
