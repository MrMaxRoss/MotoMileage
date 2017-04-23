package com.sortedunderbelly.motomileage.storage;

import android.content.ContextWrapper;

import com.sortedunderbelly.motomileage.MainActivity;

/**
 * Created by max.ross on 5/25/14.
 */
public enum StorageSystem {
    LOCAL_PREFERENCES {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return new SharedPreferencesTripStorage(contextWrapper, activity);
        }
    },
    LOCAL_DATABASE {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return new LocalDatabaseTripStorage(contextWrapper, activity);
        }
    },
    FIREBASE {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return new FirebaseTripStorage();
        }
    };

    public abstract TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity);
}
