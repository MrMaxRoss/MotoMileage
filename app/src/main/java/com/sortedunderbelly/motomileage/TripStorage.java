package com.sortedunderbelly.motomileage;

import android.content.Intent;
import android.view.Menu;

import java.util.List;
import java.util.Set;

/**
 * Created by max.ross on 5/10/14.
 */
public interface TripStorage {
    Trip save(Trip trip);
    boolean delete(String tripId);
    Trip lookup(String tripId);
    List<Trip> allTrips();
    void saveTripFilter(TripFilter tripFilter);
    TripFilter getLastTripFilter();
    void login(String authToken);
    void logout();
    void onActivityResult(int requestCode, int resultCode, Intent data);
    void onPrepareOptionsMenu(Menu menu);
    void saveReminderTypes(Set<ReminderType> reminderTypes);
    Set<ReminderType> getReminderTypes();
    void saveReminderSchedule(ReminderSchedule schedule);
    ReminderSchedule getReminderSchedule();
}
