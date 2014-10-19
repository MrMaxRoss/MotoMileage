package com.sortedunderbelly.motomileage;

import java.util.Date;

/**
 * Created by maxr on 5/10/14.
 */
public interface Trip extends Comparable<Trip> {
    String getId();
    Date getDate();
    String getDesc();
    int getDistance();
    boolean hasId();
}
