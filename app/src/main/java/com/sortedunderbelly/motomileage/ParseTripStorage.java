package com.sortedunderbelly.motomileage;

import android.content.Context;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;

import java.util.ArrayList;

/**
 * Created by maxr on 7/26/14.
 */
public class ParseTripStorage extends NoAuthTripStorage {

    public static class ParseTripStorageFactory {
        private static ParseTripStorage INSTANCE = null;

        public synchronized ParseTripStorage get(Context context, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
            if (INSTANCE == null ) {
                INSTANCE = new ParseTripStorage(context, storageCallbacks, authCallbacks);
            }
            return INSTANCE;
        }
    }

    public static final ParseTripStorageFactory FACTORY = new ParseTripStorageFactory();

    private final StorageCallbacks notifier;

    private ParseTripStorage(Context context, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
        super(authCallbacks);
        Parse.initialize(context, "rBizcZAAvSpLmZ5Xl7wy6JS8PLFmk1VRXOsX6Upi", "JUdQXkDdJY12rbSh4pFDLxgBoz2FBAcW2VZgSmOR");
        Parse.enableLocalDatastore(context);
        this.notifier = storageCallbacks;
    }

    @Override
    public Trip save(Trip trip) {
        ParseObject tripObject = new ParseObject("Trip");
        tripObject.put("desc", trip.getDesc());
        tripObject.put("date", trip.getDate());
        tripObject.put("distance", trip.getDistance());
        tripObject.saveEventually();
        Trip newTrip = new ParseTripImpl(tripObject);
        notifier.onNewTrip(newTrip);
        // TODO(maxr) Distinguish between create and update and fire the proper notification
        return newTrip;
    }

    @Override
    public boolean delete(String tripId) {
        ParseObject obj = lookup(tripId).getObj();
        obj.deleteEventually();
        notifier.onDeletedTrip(tripId);
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
    public void login(String authToken) {

    }

    @Override
    public void logout(AuthStruct authStruct) {

    }

}
