package com.sortedunderbelly.motomileage.storage;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;
import android.view.Menu;

import com.sortedunderbelly.motomileage.AuthHelper;
import com.sortedunderbelly.motomileage.AuthHelperImpl;
import com.sortedunderbelly.motomileage.AuthStruct;
import com.sortedunderbelly.motomileage.ReminderSchedule;
import com.sortedunderbelly.motomileage.ReminderType;
import com.sortedunderbelly.motomileage.StorageCallbacks;
import com.sortedunderbelly.motomileage.Trip;
import com.sortedunderbelly.motomileage.TripFilter;
import com.sortedunderbelly.motomileage.TripImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * Created by max.ross on 5/25/14.
 */
public class LocalDatabaseTripStorage extends SQLiteOpenHelper implements TripStorage {

    public static final String TABLE_TRIPS = "tripsRef";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATE = "trip_date";
    public static final String COLUMN_DISTANCE = "distance";
    public static final String COLUMN_DESC = "desc";
    public static final String[] ALL_COLUMNS = {COLUMN_ID, COLUMN_DATE, COLUMN_DISTANCE, COLUMN_DESC};

    private static final String DATABASE_NAME = "tripsRef.db";
    private static final int DATABASE_VERSION = 2;

    // Database creation sql statement
    private static final String DATABASE_CREATE = "create table "
            + TABLE_TRIPS     + "("
            + COLUMN_ID       + " INTEGER primary key autoincrement, "
            + COLUMN_DATE     + " INTEGER not null, "
            + COLUMN_DISTANCE + " INTEGER not null, "
            + COLUMN_DESC     + " text);";

    private final SQLiteDatabase db;
    private final StorageCallbacks storageCallbacks;
    private final AuthHelper authHelper;

    public LocalDatabaseTripStorage(Context context, StorageCallbacks storageCallbacks,
                                    AuthHelper authHelper) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.db = getWritableDatabase();
        this.storageCallbacks = storageCallbacks;
        this.authHelper = authHelper;
        login("local database");
    }

    public void close() {
        db.close();
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(getClass().getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data"
        );
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIPS);
        onCreate(db);
    }

    @Override
    public Trip save(Trip trip) {
        ContentValues values = new ContentValues();
        if (trip.hasId()) {
            values.put(COLUMN_ID, trip.getId());
        }
        values.put(COLUMN_DATE, trip.getDate().getTime());
        values.put(COLUMN_DISTANCE, trip.getDistance());
        values.put(COLUMN_DESC, trip.getDesc());
        // We use the CONFLICT_REPLACE policy as a form of UPSERT.
        int tripId = (int) db.insertWithOnConflict(TABLE_TRIPS, null, values, SQLiteDatabase.CONFLICT_REPLACE);
        Trip savedTrip;
        if (trip.hasId()) {
            savedTrip = trip;
            storageCallbacks.onUpdatedTrip(savedTrip);
        } else {
            savedTrip = new TripImpl(Integer.toString(tripId), trip.getDate(), trip.getDesc(), trip.getDistance());
            storageCallbacks.onNewTrip(savedTrip);
        }

        return savedTrip;
    }

    @Override
    public boolean delete(String tripId) {
        if (db.delete(TABLE_TRIPS, COLUMN_ID + " = " + tripId, null) == 1) {
            storageCallbacks.onDeletedTrip(tripId);
            return true;
        }
        return false;
    }

    @Override
    public Trip lookup(String tripId) {
        SQLiteQueryBuilder queryBuilder = new SQLiteQueryBuilder();
        queryBuilder.setTables(TABLE_TRIPS);
        queryBuilder.appendWhere(COLUMN_ID + "=" + tripId);
        Cursor cursor = queryBuilder.query(db, ALL_COLUMNS, null, null, null, null, null);
        try {
            if (!cursor.moveToFirst()) {
                return null;
            }
            return cursorToTrip(cursor);
        } finally {
            // make sure to close the cursor
            cursor.close();
        }
    }

    @Override
    public List<Trip> allTrips() {
        List<Trip> trips = new ArrayList<Trip>();
        Cursor cursor = db.query(TABLE_TRIPS, ALL_COLUMNS, null, null, null, null, COLUMN_DATE + " desc");
        try {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                Trip trip = cursorToTrip(cursor);
                trips.add(trip);
                cursor.moveToNext();
            }
        } finally {
            // make sure to close the cursor
            cursor.close();
        }
        return trips;
    }

    private Trip cursorToTrip(Cursor cursor) {
        return new TripImpl(
                Integer.toString(cursor.getInt(0)),
                new Date(cursor.getLong(1)),
                cursor.getString(3),
                cursor.getInt(2));
    }

    @Override
    public void saveTripFilter(TripFilter tripFilter) {
        // TODO(max.ross): implement
    }

    @Override
    public TripFilter getLastTripFilter() {
        // TODO(max.ross): implement
        return TripFilter.MONTH_THUS_FAR;
    }

    @Override
    public void login(String authToken) {
        if (authToken == null) {
            throw new NullPointerException("authToken cannot be null");
        }
        AuthStruct struct = new AuthStruct("local database", "local database", authToken);
        authHelper.onAuthStateChanged(struct, null);
    }

    @Override
    public void logout() {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
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
}
