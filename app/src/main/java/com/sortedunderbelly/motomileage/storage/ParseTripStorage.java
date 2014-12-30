package com.sortedunderbelly.motomileage.storage;

import android.content.Context;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sortedunderbelly.motomileage.MainActivity;
import com.sortedunderbelly.motomileage.ReminderSchedule;
import com.sortedunderbelly.motomileage.ReminderType;
import com.sortedunderbelly.motomileage.Trip;
import com.sortedunderbelly.motomileage.TripFilter;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by max.ross on 7/26/14.
 */
public class ParseTripStorage extends NoAuthTripStorage {

    public static class ParseTripStorageFactory {
        private static ParseTripStorage INSTANCE = null;

        public synchronized ParseTripStorage get(Context context, MainActivity activity) {
            if (INSTANCE == null ) {
                INSTANCE = new ParseTripStorage(context, activity);
            }
            return INSTANCE;
        }
    }

    public static final ParseTripStorageFactory FACTORY = new ParseTripStorageFactory();

    private final MainActivity activity;

    private ParseTripStorage(Context context, MainActivity activity) {
        this.activity = activity;
        Parse.initialize(context, "rBizcZAAvSpLmZ5Xl7wy6JS8PLFmk1VRXOsX6Upi", "JUdQXkDdJY12rbSh4pFDLxgBoz2FBAcW2VZgSmOR");
        Parse.enableLocalDatastore(context);
        login(null);
    }

    @Override
    public Trip save(Trip trip) {
        ParseObject tripObject = new ParseObject("Trip");
        tripObject.put("desc", trip.getDesc());
        tripObject.put("date", trip.getDate());
        tripObject.put("distance", trip.getDistance());
        tripObject.saveEventually();
        Trip newTrip = new ParseTripImpl(tripObject);
        activity.onNewTrip(newTrip);
        // TODO(max.ross) Distinguish between create and update and fire the proper notification
        return newTrip;
    }

    @Override
    public boolean delete(String tripId) {
        ParseObject obj = lookup(tripId).getObj();
        obj.deleteEventually();
        activity.onDeletedTrip(tripId);
        return true;
    }

    @Override
    public ParseTripImpl lookup(String tripId) {
        ParseQuery<ParseObject> query = ParseQuery.getQuery("Trip");
        query.fromLocalDatastore();
        try {
            ParseObject obj = query.get(tripId);
            return new ParseTripImpl(obj);
        } catch (ParseException e) {
            throw new RuntimeException("Could not retrieve from local storage");
        }
    }

    @Override
    public ArrayList<Trip> allTrips() {
        return null;
    }

    @Override
    public void saveTripFilter(TripFilter tripFilter) {

    }

    @Override
    public TripFilter getLastTripFilter() {
        // TODO
        return TripFilter.YEAR_THUS_FAR;
    }

    @Override
    public void saveReminderTypes(Set<ReminderType> reminderTypes) {

    }

    @Override
    public Set<ReminderType> getReminderTypes() {
        return null;
    }

    @Override
    public void saveReminderSchedule(ReminderSchedule schedule) {

    }

    @Override
    public ReminderSchedule getReminderSchedule() {
        return null;
    }
}
