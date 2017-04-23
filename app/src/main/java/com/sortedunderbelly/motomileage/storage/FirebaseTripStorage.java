package com.sortedunderbelly.motomileage.storage;

import android.app.ProgressDialog;
import android.util.Log;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.sortedunderbelly.motomileage.MainActivity;
import com.sortedunderbelly.motomileage.ReminderSchedule;
import com.sortedunderbelly.motomileage.ReminderType;
import com.sortedunderbelly.motomileage.Trip;
import com.sortedunderbelly.motomileage.TripFilter;
import com.sortedunderbelly.motomileage.TripImpl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by max.ross on 7/26/14.
 */
public class FirebaseTripStorage implements TripStorage {

    /* A tag that is used for logging statements */
    private static final String TAG = "FirebaseTripStorage";

    private static final String USER_DATA_PATH = "users";

    private MainActivity activity;
    private String userId;
    private final FirebaseDatabase firebaseRef;

    // All trips for the user, unfiltered.
    // TODO(max.ross): Implement serverside filtering
    private final LinkedList<Trip> trips = new LinkedList<Trip>();
    private final Map<String, Trip> tripsById = new HashMap<String, Trip>();
    private TripFilter tripFilter = TripFilter.MONTH_THUS_FAR;

    private ChildEventListener tripListener;
    private boolean isInitialized = false; // true after we've received our callback with all the user data
    private Set<ReminderType> reminderTypes = new HashSet<ReminderType>();
    private ReminderSchedule reminderSchedule = ReminderSchedule.NONE;

    private ProgressDialog authProgressDialog;

    FirebaseTripStorage() {
        firebaseRef = FirebaseDatabase.getInstance();
        firebaseRef.setPersistenceEnabled(true);
    }

    @Override
    public void init(MainActivity activity, String userId) {
        this.activity = activity;
        if (userId == null) {
            throw new IllegalArgumentException("Cannot init with null userId");
        }
        if (!userId.equals(this.userId)) {
            this.userId = userId;
            authProgressDialog = new ProgressDialog(activity);
            authProgressDialog.setTitle("Loading");
            authProgressDialog.setMessage("Loading data...");
            authProgressDialog.setCancelable(true);
            authProgressDialog.show();
            establishListeners();
        }
        Log.i(TAG, "Initialized storage");
    }

    @Override
    public void reset() {
        isInitialized = false;
        trips.clear();
        tripsById.clear();
        userId = null;
        activity = null;
        authProgressDialog = null;
        Log.i(TAG, "Reset storage");
    }

    private DatabaseReference getTripsRef() {
        return getUserRef().child("trips");
    }

    private DatabaseReference getReminderRef() {
        return getUserRef().child("reminder");
    }

    private DatabaseReference getUserRef() {
        return firebaseRef.getReference(String.format("%s/%s", USER_DATA_PATH, userId));
    }

    private void establishListeners() {
        DatabaseReference userRef = getUserRef();
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (isInitialized) {
                    return;
                }
                isInitialized = true;
                boolean writeFilter = true;
                Map<String, Object> userData = mapFromSnapshot(dataSnapshot);
                if (userData != null) {
                    String tripFilterStr = (String) userData.get("tripFilter");
                    if (tripFilterStr != null) {
                        tripFilter = TripFilter.valueOf(tripFilterStr);
                        writeFilter = false;
                    }

                    @SuppressWarnings("unchecked")
                    Map<String, Object> reminderData = (Map<String, Object>) userData.get("reminder");
                    if (reminderData != null) {
                        if (reminderData.containsKey("reminderSchedule")) {
                            try {
                                reminderSchedule = ReminderSchedule.valueOf((String) reminderData.get("reminderSchedule"));
                            } catch (IllegalArgumentException iae) {
                                // that's ok
                            }
                        }

                        @SuppressWarnings("unchecked")
                        List<String> reminderTypeStrs = (List<String>) reminderData.get("reminderTypes");
                        if (reminderTypeStrs != null) {
                            for (String reminderTypeStr : reminderTypeStrs) {
                                try {
                                    reminderTypes.add(ReminderType.valueOf(reminderTypeStr));
                                } catch (IllegalArgumentException iae) {
                                    // that's ok
                                }
                            }
                        }
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
                authProgressDialog.hide();
                activity.onFullRefresh();

                if (writeFilter) {
                    // no data stored so write the default value
                    saveTripFilter(tripFilter);
                }
                attachTripListeners();
            }

            @Override
            public void onCancelled(DatabaseError firebaseError) {
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
        DatabaseReference tripsRef = getTripsRef();
        if (tripListener == null) {
            tripListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String previousChild) {
                    Trip newTrip = toTrip(dataSnapshot.getKey(), mapFromSnapshot(dataSnapshot));
                    // There's an initialization issue where we load all the user data up front and then
                    // add listeners on the list of trips. This lets us avoid adding the same trips
                    // multiple times.
                    if (!tripsById.containsKey(newTrip.getId())) {
                        trips.add(newTrip);
                        tripsById.put(newTrip.getId(), newTrip);
                        activity.onNewTrip(newTrip);
                    }
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String previousChild) {
                    Trip changedTrip = toTrip(dataSnapshot.getKey(), mapFromSnapshot(dataSnapshot));
                    int index = getTripIndex(changedTrip);
                    trips.set(index, changedTrip);
                    tripsById.put(changedTrip.getId(), changedTrip);
                    activity.onUpdatedTrip(changedTrip);
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Trip deletedTrip = toTrip(dataSnapshot.getKey(), mapFromSnapshot(dataSnapshot));
                    int index = getTripIndex(deletedTrip);
                    trips.remove(index);
                    tripsById.remove(deletedTrip.getId());
                    activity.onDeletedTrip(deletedTrip.getId());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String previousChild) {
                    throw new UnsupportedOperationException("child movement not supported");
                }

                @Override
                public void onCancelled(DatabaseError firebaseError) {
                    Log.e(TAG, "onCancelled: " + firebaseError.toString());
                }
            };
            tripsRef.addChildEventListener(tripListener);
        } else {
            // TODO(max.ross): Why is this needed? Seems like deauth somehow poisons the listeners.
            tripsRef.removeEventListener(tripListener);
            tripsRef.addChildEventListener(tripListener);
        }
    }
    @Override
    public Trip save(Trip trip) {
        DatabaseReference tripsRef = getTripsRef();
        DatabaseReference tripRef;
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
        return new TripImpl(tripRef.getKey(), trip.getDate(), trip.getDesc(), trip.getDistance());
    }

    @Override
    public boolean delete(String tripId) {
        DatabaseReference child = getTripsRef().child(tripId);
        child.removeValue();
        return true;
    }

    @Override
    public void saveReminderTypes(Set<ReminderType> reminderTypes) {
        this.reminderTypes.clear();
        this.reminderTypes.addAll(reminderTypes);
        DatabaseReference remindersRef = getReminderRef().child("reminderTypes");
        remindersRef.setValue(new ArrayList<ReminderType>(reminderTypes));
    }

    @Override
    public Set<ReminderType> getReminderTypes() {
        return reminderTypes;
    }

    @Override
    public void saveReminderSchedule(final ReminderSchedule schedule) {
        this.reminderSchedule = schedule;
        DatabaseReference reminderRef = getReminderRef();
        Map<String, Object> userData = new HashMap<String, Object>();
        userData.put("reminderSchedule", schedule.name());
        reminderRef.updateChildren(userData, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError == null) {
                    addReminderModification();
                } else {
                    Log.e(TAG, "Could not update reminder schedule: " + databaseError.toString());
                }
            }
        });
    }

    private void addReminderModification() {
        // Add an entry to signal that the cron process needs to evaluate.
        // There doesn't seem to be a way to add a child node that is just a key
        // so we write the empty string as the associated value.
        Map<String, Object> data = new HashMap<String, Object>();
        data.put(userId, "");
        firebaseRef.getReference("reminderModificationQueue").updateChildren(data, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.e(TAG, "Could not update reminder modification queue: " + databaseError.toString());
                }
            }
        });
    }

    @Override
    public ReminderSchedule getReminderSchedule() {
        return reminderSchedule;
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
        DatabaseReference userRef = getUserRef();
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
