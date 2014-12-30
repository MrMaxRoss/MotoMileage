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
            return new FirebaseTripStorage(contextWrapper, activity);
        }
    },
    CLOUD_SAVE_V1 {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return null;
        }
    },
    CLOUD_SAVE_V2 {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return null;
        }
    },
    PARSE {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return ParseTripStorage.FACTORY.get(contextWrapper, activity);
        }
    },
    DROPBOX {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return null;
        }
    },
    GOOGLE_DRIVE {
        public TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return null;
        }
    };

    public abstract TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity);
}
