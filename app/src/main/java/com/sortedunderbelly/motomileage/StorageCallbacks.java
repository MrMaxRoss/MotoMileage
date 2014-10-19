package com.sortedunderbelly.motomileage;

/**
 * Created by maxr on 8/25/14.
 */
public interface StorageCallbacks {
    void onNewTrip(Trip trip);
    void onUpdatedTrip(Trip updatedTrip);
    void onDeletedTrip(String deletedTripId);
    // refresh everything
    void onFullRefresh();
}
