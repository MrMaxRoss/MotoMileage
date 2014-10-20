package com.sortedunderbelly.motomileage;

import android.content.ContextWrapper;

/**
 * Created by max.ross on 5/25/14.
 */
public enum StorageSystem {
    LOCAL_PREFERENCES {
        TripStorage getTripStorage(ContextWrapper contextWrapper, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
            return new SharedPreferencesTripStorage(contextWrapper, storageCallbacks, authCallbacks);
        }
    },
    LOCAL_DATABASE {
        TripStorage getTripStorage(ContextWrapper contextWrapper, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
            return new LocalDatabaseTripStorage(contextWrapper, storageCallbacks, authCallbacks);
        }
    },
    FIREBASE {
        TripStorage getTripStorage(ContextWrapper contextWrapper, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
            return new FirebaseTripStorage(contextWrapper, storageCallbacks, authCallbacks);
        }
    },
    CLOUD_SAVE_V1 {
        TripStorage getTripStorage(ContextWrapper contextWrapper, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
            return null;
        }
    },
    CLOUD_SAVE_V2 {
        TripStorage getTripStorage(ContextWrapper contextWrapper, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
            return null;
        }
    },
    PARSE {
        TripStorage getTripStorage(ContextWrapper contextWrapper, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
            return ParseTripStorage.FACTORY.get(contextWrapper, storageCallbacks, authCallbacks);
        }
    },
    DROPBOX {
        TripStorage getTripStorage(ContextWrapper contextWrapper, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks) {
            return null;
        }
    },
    GOOGLE_DRIVE {
        TripStorage getTripStorage(ContextWrapper contextWrapper, StorageCallbacks notifier, AuthCallbacks authCallbacks) {
            return null;
        }
    };

    abstract TripStorage getTripStorage(ContextWrapper contextWrapper, StorageCallbacks storageCallbacks, AuthCallbacks authCallbacks);
}
