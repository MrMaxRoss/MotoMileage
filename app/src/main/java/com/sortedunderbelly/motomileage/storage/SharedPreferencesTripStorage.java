package com.sortedunderbelly.motomileage.storage;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.SharedPreferences;

import com.sortedunderbelly.motomileage.MainActivity;
import com.sortedunderbelly.motomileage.ReminderSchedule;
import com.sortedunderbelly.motomileage.ReminderType;
import com.sortedunderbelly.motomileage.StorageCallbacks;
import com.sortedunderbelly.motomileage.Trip;
import com.sortedunderbelly.motomileage.TripFilter;
import com.sortedunderbelly.motomileage.TripImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by max.ross on 5/10/14.
 */
public class SharedPreferencesTripStorage implements TripStorage {
    // name of SharedPreferences XML file that stores the saved tripStorage
    private static final String TRIP_IDS = "trip_ids";
    private static final String APP_DATA = "app_data";
    private static final String DATE_SUFFIX = "_date";
    private static final String DESC_SUFFIX = "_desc";
    private static final String DISTANCE_SUFFIX = "_distance";
    private static final String FILTER_KEY = "filterKey";
    private static final String NEXT_ID_KEY = "nextId";

    private final SharedPreferences tripIds;
    private final SharedPreferences appData;
    private final StorageCallbacks storageCallbacks;

    public SharedPreferencesTripStorage(ContextWrapper contextWrapper, StorageCallbacks storageCallbacks) {
        // get the SharedPreferences containing the user's saved tripsRef
        this.tripIds = contextWrapper.getSharedPreferences(TRIP_IDS, Context.MODE_PRIVATE);
        this.appData = contextWrapper.getSharedPreferences(APP_DATA, Context.MODE_PRIVATE);
        this.storageCallbacks = storageCallbacks;
    }

    public SharedPreferences getTripIds() {
        return tripIds;
    }

    public SharedPreferences getAppData() {
        return appData;
    }

    @Override
    public Trip save(Trip trip) {
        int tripId = trip.hasId() ? Integer.parseInt(trip.getId()) : appData.getInt(NEXT_ID_KEY, 1);
        // Write to the data repository first so that if there is an error writing the primary key
        // we don't end up with dangling pointers in the id repository
        SharedPreferences.Editor dataEditor = appData.edit();
        if (!trip.hasId()) {
            dataEditor.putInt(NEXT_ID_KEY, tripId + 1);
        }
        dataEditor.putLong(tripId + DATE_SUFFIX, trip.getDate().getTime());
        dataEditor.putString(tripId + DESC_SUFFIX, trip.getDesc());
        dataEditor.putInt(tripId + DISTANCE_SUFFIX, trip.getDistance());
        dataEditor.apply();

        SharedPreferences.Editor idsEditor = tripIds.edit();
        idsEditor.putBoolean(Integer.toString(tripId), true);
        idsEditor.apply();
        Trip savedTrip;
        if (trip.hasId()) {
            savedTrip = trip;
            storageCallbacks.onNewTrip(savedTrip);
        } else {
            savedTrip = new TripImpl(Integer.toString(tripId), trip.getDate(), trip.getDesc(), trip.getDistance());
            storageCallbacks.onUpdatedTrip(savedTrip);
        }
        return savedTrip;
    }

    @Override
    public boolean delete(String tripId) {
        if (!tripIds.contains(tripId)) {
            return false;
        }
        // Remove from the id repository first so that if there is a failure afterwards, the data
        // is still effectively deleted
        SharedPreferences.Editor idsEditor = tripIds.edit();
        idsEditor.remove(tripId);
        idsEditor.apply();

        SharedPreferences.Editor appDataEditor = appData.edit();
        appDataEditor.remove(tripId + DATE_SUFFIX);
        appDataEditor.remove(tripId + DESC_SUFFIX);
        appDataEditor.remove(tripId + DISTANCE_SUFFIX);
        appDataEditor.apply();
        storageCallbacks.onDeletedTrip(tripId);
        return true;
    }

    @Override
    public Trip lookup(String tripId) {
        if (!tripIds.contains(tripId)) {
            return null;
        }
        // TODO(max.ross): Find out what the second param to getLong is
        return new TripImpl(
                tripId,
                new Date(appData.getLong(tripId + DATE_SUFFIX, 0)),
                appData.getString(tripId + DESC_SUFFIX, null),
                appData.getInt(tripId + DISTANCE_SUFFIX, 0));
    }

    // sorts by date in descending order
    Comparator<Trip> TRIP_COMPARATOR = new Comparator<Trip>() {
        @Override
        public int compare(Trip trip, Trip trip2) {
            return trip2.getDate().compareTo(trip.getDate());
        }
    };

    @Override
    public List<Trip> allTrips() {
        List<Trip> trips = new ArrayList<Trip>();
        for (String tripId : tripIds.getAll().keySet()) {
            trips.add(lookup(tripId));
        }
        Collections.sort(trips, TRIP_COMPARATOR);
        return trips;
    }

    @Override
    public void saveTripFilter(TripFilter tripFilter) {
        SharedPreferences.Editor appDataEditor = appData.edit();
        appDataEditor.putString(FILTER_KEY, tripFilter.name());
        appDataEditor.apply();
    }

    @Override
    public TripFilter getLastTripFilter() {
        try {
            return TripFilter.valueOf(appData.getString(FILTER_KEY, TripFilter.MONTH_THUS_FAR.name()));
        } catch (IllegalArgumentException iae) {
            return TripFilter.MONTH_THUS_FAR;
        }
    }

    @Override
    public void saveReminderTypes(Set<ReminderType> reminderTypes) {

    }

    @Override
    public Set<ReminderType> getReminderTypes() {
        // TODO(max.ross)
        return Collections.emptySet();
    }

    @Override
    public void saveReminderSchedule(ReminderSchedule schedule) {

    }

    @Override
    public ReminderSchedule getReminderSchedule() {
        return ReminderSchedule.NONE;
    }

    @Override
    public void init(MainActivity activity, String userId) {

    }

    @Override
    public void reset() {

    }
}
