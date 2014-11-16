package com.sortedunderbelly.motomileage;

/**
 * Created by max.ross on 11/15/14.
 */
public interface StorageCallbacks {
    void onNewTrip(Trip trip);
    void onUpdatedTrip(Trip updatedTrip);
    void onDeletedTrip(String deletedTripId);
    void onFullRefresh();
}
