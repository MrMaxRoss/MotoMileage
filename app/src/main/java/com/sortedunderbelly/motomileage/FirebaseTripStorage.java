package com.sortedunderbelly.motomileage;

import android.content.Context;
import android.util.Log;

import com.firebase.client.AuthData;
import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;

import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created by maxr on 7/26/14.
 */
public class FirebaseTripStorage implements TripStorage {

    /* A tag that is used for logging statements */
    private static final String TAG = "FirebaseTripStorage";

    private static final String USER_DATA_PATH = "users";

    private final StorageCallbacks storageCallbacks;
    private final AuthCallbacks authCallbacks;
    private String userId;
    private final Firebase firebaseRef;

    // All trips for the user, unfiltered.
    // TODO(maxr): Implement serverside filtering
    private final LinkedList<Trip> trips = new LinkedList<Trip>();
    private final Map<String, Trip> tripsById = new HashMap<String, Trip>();
    private TripFilter tripFilter = TripFilter.MONTH_THUS_FAR;

    private final AuthResultHandler authResultHandler = new AuthResultHandler();
    private ChildEventListener tripListener;
    boolean isInitialized = false; // true after we've received our callback with all the user data

    public FirebaseTripStorage(Context context, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
        this.storageCallbacks = storageCallbacks;
        this.authCallbacks = authCallbacks;
        Firebase.setAndroidContext(context);
        firebaseRef = new Firebase(context.getResources().getString(R.string.firebase_url));

        /* Check if the user is authenticated with Firebase already. If this is the case we can set the authenticated
         * user and hide any login buttons */
        firebaseRef.addAuthStateListener(new Firebase.AuthStateListener() {
            @Override
            public void onAuthStateChanged(AuthData authData) {
                FirebaseTripStorage.this.onAuthStateChanged(authData);
            }
        });
    }

    @Override
    public void login(String authToken) {
        if (authToken == null) {
            throw new NullPointerException("authToken cannot be null");
        }
        /* Successfully got OAuth token, now login with Google */
        firebaseRef.authWithOAuthToken("google", authToken, authResultHandler);
    }

    /**
     * Utility class for authentication results
     */
    private class AuthResultHandler implements Firebase.AuthResultHandler {

        @Override
        public void onAuthenticated(AuthData authData) {
            FirebaseTripStorage.this.onAuthStateChanged(authData);
        }

        @Override
        public void onAuthenticationError(FirebaseError firebaseError) {
            authCallbacks.onAuthStateChanged(null, firebaseError.toString());
        }
    }

    /**
     * Unauthenticate from Firebase.
     */
    @Override
    public void logout(AuthStruct authStruct) {
        if (authStruct != null) {
            /* logout of Firebase */
            firebaseRef.unauth();
        }
        userId = null;
        isInitialized = false;
        clearData();
        storageCallbacks.onFullRefresh();
    }

    private void onAuthStateChanged(AuthData authData) {
        if (authData != null) {
            userId = authData.getUid();
            establishListeners();
        }
        authCallbacks.onAuthStateChanged(authDataToAuthStruct(authData), null);
    }

    private Firebase getTripsRef() {
        return firebaseRef.child(String.format("%s/%s/trips", USER_DATA_PATH, userId));
    }

    private Firebase getUserRef() {
        return firebaseRef.child(String.format("%s/%s", USER_DATA_PATH, userId));
    }

    private void clearData() {
        trips.clear();
        tripsById.clear();
    }

    private void establishListeners() {
        Firebase userRef = getUserRef();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isInitialized) {
                    return;
                }
                isInitialized = true;
                clearData();
                boolean writeFilter = true;
                Map<String, Object> userData = mapFromSnapshot(dataSnapshot);
                if (userData != null) {
                    String tripFilterStr = (String) userData.get("tripFilter");
                    if (tripFilterStr != null) {
                        tripFilter = TripFilter.valueOf(tripFilterStr);
                        writeFilter = false;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Map<String, Object>> tripMaps = (Map<String, Map<String, Object>>) userData.get("trips");
                    if (tripMaps != null) {
                        for (Map.Entry<String, Map<String, Object>> entries : tripMaps.entrySet()) {
                            Trip newTrip = toTrip(entries.getKey(), entries.getValue());
                            trips.add(newTrip);
                            tripsById.put(newTrip.getId(), newTrip);
                        }
                    }
                }
                storageCallbacks.onFullRefresh();

                if (writeFilter) {
                    // no data stored so write the default value
                    saveTripFilter(tripFilter);
                }
                attachTripListeners();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                Log.e(TAG, "onCancelled: " + firebaseError.toString());
            }
        });
    }

    static Trip toTrip(String name, Map<String, Object> tripData) {
        return new TripImpl(
                name,
                new Date((Long) tripData.get("date")),
                (String) tripData.get("desc"),
                ((Long) tripData.get("distance")).intValue());
    }

    private void attachTripListeners() {
        Firebase tripsRef = getTripsRef();
        if (tripListener == null) {
            tripListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                    Trip newTrip = toTrip(dataSnapshot.getName(), mapFromSnapshot(dataSnapshot));
                    // There's an initialization issue where we load all the user data up front and then
                    // add listeners on the list of trips. This lets us avoid adding the same trips
                    // multiple times.
                    if (!tripsById.containsKey(newTrip.getId())) {
                        trips.add(newTrip);
                        tripsById.put(newTrip.getId(), newTrip);
                        storageCallbacks.onNewTrip(newTrip);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChild) {
                    Trip changedTrip = toTrip(dataSnapshot.getName(), mapFromSnapshot(dataSnapshot));
                    int index = getTripIndex(changedTrip);
                    trips.set(index, changedTrip);
                    tripsById.put(changedTrip.getId(), changedTrip);
                    storageCallbacks.onUpdatedTrip(changedTrip);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Trip deletedTrip = toTrip(dataSnapshot.getName(), mapFromSnapshot(dataSnapshot));
                    int index = getTripIndex(deletedTrip);
                    trips.remove(index);
                    tripsById.remove(deletedTrip.getId());
                    storageCallbacks.onDeletedTrip(deletedTrip.getId());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChild) {
                    throw new UnsupportedOperationException("child movement not supported");
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    Log.e(TAG, "onCancelled: " + firebaseError.toString());
                }
            };
            tripsRef.addChildEventListener(tripListener);
        } else {
            // TODO(maxr): Why is this needed? Seems like deauth somehow poisons the listeners.
            tripsRef.removeEventListener(tripListener);
            tripsRef.addChildEventListener(tripListener);
        }
    }
    private static AuthStruct authDataToAuthStruct(AuthData authData) {
        if (authData == null) {
            return null;
        }
        return new AuthStruct(
                authData.getProvider(),
                (String) authData.getProviderData().get("displayName"),
                authData.getToken());
    }

    @Override
    public Trip save(Trip trip) {
        Firebase tripsRef = getTripsRef();
        Firebase tripRef;
        if (trip.hasId()) {
            tripRef = tripsRef.child(trip.getId());
        } else {
            tripRef = tripsRef.push();
        }
        Map<String, Object> tripData = new HashMap<String, Object>();
        tripData.put("date", trip.getDate().getTime());
        tripData.put("desc", trip.getDesc());
        tripData.put("distance", trip.getDistance());
        tripRef.setValue(tripData);
        return new TripImpl(tripRef.getName(), trip.getDate(), trip.getDesc(), trip.getDistance());
    }

    @Override
    public boolean delete(String tripId) {
        Firebase child = getTripsRef().child(tripId);
        child.removeValue();
        return true;
    }

    @Override
    public Trip lookup(String tripId) {
        return tripsById.get(tripId);
    }

    @Override
    public List<Trip> allTrips() {
        return trips;
    }

    @Override
    public void saveTripFilter(TripFilter tripFilter) {
        this.tripFilter = tripFilter;
        // Updating the database is best effort. If it fails, that's fine, it just means we may
        // lose the current filter the next time we run the app.
        Firebase userRef = getUserRef();
        Map<String, Object> userData = new HashMap<String, Object>();
        userData.put("tripFilter", tripFilter.name());
        userRef.updateChildren(userData);
    }

    @Override
    public TripFilter getLastTripFilter() {
        return tripFilter;
    }

    private int getTripIndex(Trip trip) {
        String tripId = trip.getId();
        for (int loc = 0; loc < trips.size(); loc++) {
            if (trips.get(loc).getId().equals(tripId)) {
                return loc;
            }
        }
        throw new IllegalStateException("Could not find Trip with id " + tripId);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapFromSnapshot(DataSnapshot snapshot) {
        return (Map<String, Object>) snapshot.getValue();
    }
}
