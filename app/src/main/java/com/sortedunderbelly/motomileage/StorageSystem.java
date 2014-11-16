package com.sortedunderbelly.motomileage;

import android.content.ContextWrapper;

/**
 * Created by max.ross on 5/25/14.
 */
public enum StorageSystem {
    LOCAL_PREFERENCES {
        TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return new SharedPreferencesTripStorage(contextWrapper, activity);
        }
    },
    LOCAL_DATABASE {
        TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return new LocalDatabaseTripStorage(contextWrapper, activity);
        }
    },
    FIREBASE {
        TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return new FirebaseTripStorage(contextWrapper, activity);
        }
    },
    CLOUD_SAVE_V1 {
        TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return null;
        }
    },
    CLOUD_SAVE_V2 {
        TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return null;
        }
    },
    PARSE {
        TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return ParseTripStorage.FACTORY.get(contextWrapper, activity);
        }
    },
    DROPBOX {
        TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return null;
        }
    },
    GOOGLE_DRIVE {
        TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity) {
            return null;
        }
    };

    abstract TripStorage getTripStorage(ContextWrapper contextWrapper, MainActivity activity);
}
