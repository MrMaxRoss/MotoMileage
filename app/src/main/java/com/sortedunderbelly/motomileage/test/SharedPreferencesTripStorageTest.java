package com.sortedunderbelly.motomileage.test;

import android.test.RenamingDelegatingContext;

import com.sortedunderbelly.motomileage.SharedPreferencesTripStorage;
import com.sortedunderbelly.motomileage.TripStorage;

/**
 * Created by max.ross on 6/7/14.
 */
public class SharedPreferencesTripStorageTest extends BaseTripStorageTest {
    private static final String TEST_FILE_PREFIX = "test_";

    @Override
    TripStorage newTripStorage() {
        RenamingDelegatingContext rdc = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
        return new SharedPreferencesTripStorage(rdc, STORAGE_CALLBACKS);
    }

    SharedPreferencesTripStorage getStorage() {
        return (SharedPreferencesTripStorage) storage;
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        // make sure we start fresh each time
        getStorage().getTripIds().edit().clear().commit();
        getStorage().getAppData().edit().clear().commit();
    }
}
