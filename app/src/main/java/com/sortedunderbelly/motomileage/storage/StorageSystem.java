package com.sortedunderbelly.motomileage.storage;

import android.content.ContextWrapper;

import com.sortedunderbelly.motomileage.AuthHelperImpl;
import com.sortedunderbelly.motomileage.MainActivity;

/**
 * Created by max.ross on 5/25/14.
 */
public enum StorageSystem {
    LOCAL_PREFERENCES {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelperImpl authHelper) {
            return new SharedPreferencesTripStorage(contextWrapper, activity);
        }
    },
    LOCAL_DATABASE {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelperImpl authHelper) {
            return new LocalDatabaseTripStorage(contextWrapper, activity, authHelper);
        }
    },
    FIREBASE {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelperImpl authHelper) {
            return new FirebaseTripStorage(contextWrapper, activity, authHelper);
        }
    };

    public abstract TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelperImpl authHelper);
}
