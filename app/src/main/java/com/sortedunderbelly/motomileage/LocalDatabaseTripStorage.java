package com.sortedunderbelly.motomileage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by maxr on 5/25/14.
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
    private final AuthCallbacks authCallbacks;

    public LocalDatabaseTripStorage(Context context, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.db = getWritableDatabase();
        this.storageCallbacks = storageCallbacks;
        this.authCallbacks = authCallbacks;
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
        // TODO(maxr): implement
    }

    @Override
    public TripFilter getLastTripFilter() {
        // TODO(maxr): implement
        return TripFilter.MONTH_THUS_FAR;
    }

    @Override
    public void login(String authToken) {
        authCallbacks.onAuthStateChanged(new AuthStruct("noauth", "noauth", ""), null);
    }

    @Override
    public void logout(AuthStruct authStruct) {

    }
}
