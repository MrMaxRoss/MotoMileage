package com.sortedunderbelly.motomileage.test;

import android.test.RenamingDelegatingContext;

import com.sortedunderbelly.motomileage.LocalDatabaseTripStorage;
import com.sortedunderbelly.motomileage.TripStorage;

/**
 * Created by maxr on 6/7/14.
 */
public class LocalDatabaseTripStorageTest extends BaseTripStorageTest {

    private static final String TEST_FILE_PREFIX = "test_";

    @Override
    TripStorage newTripStorage() {
        RenamingDelegatingContext rdc = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
        return new LocalDatabaseTripStorage(rdc, STORAGE_CALLBACKS, AUTH_CALLBACKS);
    }

    LocalDatabaseTripStorage getStorage() {
        return (LocalDatabaseTripStorage) storage;
    }

    @Override
    protected void tearDown() throws Exception {
        getStorage().close();
        super.tearDown();
    }

    public void testCreateDatabase() {
        // the setup and teardown test this
    }
}