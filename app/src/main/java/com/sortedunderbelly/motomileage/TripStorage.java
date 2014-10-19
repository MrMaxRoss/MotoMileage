package com.sortedunderbelly.motomileage;

import java.util.List;

/**
 * Created by maxr on 5/10/14.
 */
public interface TripStorage {
    Trip save(Trip trip);
    boolean delete(String tripId);
    Trip lookup(String tripId);
    List<Trip> allTrips();
    void saveTripFilter(TripFilter tripFilter);
    TripFilter getLastTripFilter();
    void logout(AuthStruct authStruct);
    void login(String authToken);
}
