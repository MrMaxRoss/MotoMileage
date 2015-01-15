package com.sortedunderbelly.motomileage.storage;

import android.content.ContextWrapper;

import com.sortedunderbelly.motomileage.AuthHelper;
import com.sortedunderbelly.motomileage.MainActivity;

/**
 * Created by max.ross on 5/25/14.
 */
public enum StorageSystem {
    LOCAL_PREFERENCES {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelper authHelper) {
            return new SharedPreferencesTripStorage(contextWrapper, activity);
        }
    },
    LOCAL_DATABASE {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelper authHelper) {
            return new LocalDatabaseTripStorage(contextWrapper, activity);
        }
    },
    FIREBASE {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelper authHelper) {
            return new FirebaseTripStorage(contextWrapper, activity, authHelper);
        }
    },
    CLOUD_SAVE_V1 {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelper authHelper) {
            return null;
        }
    },
    CLOUD_SAVE_V2 {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelper authHelper) {
            return null;
        }
    },
    PARSE {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelper authHelper) {
            return ParseTripStorage.FACTORY.get(contextWrapper, activity);
        }
    },
    DROPBOX {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelper authHelper) {
            return null;
        }
    },
    GOOGLE_DRIVE {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelper authHelper) {
            return null;
        }
    };

    public abstract TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity, AuthHelper authHelper);
}
