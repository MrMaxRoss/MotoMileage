package com.sortedunderbelly.motomileage.storage;

import android.test.RenamingDelegatingContext;

/**
 * Created by max.ross on 6/7/14.
 */
public class LocalDatabaseTripStorageTest extends BaseTripStorageTest {

    private static final String TEST_FILE_PREFIX = "test_";

    @Override
    TripStorage newTripStorage() {
        RenamingDelegatingContext rdc = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
        return new LocalDatabaseTripStorage(rdc, STORAGE_CALLBACKS);
    }

    private LocalDatabaseTripStorage getStorage() {
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
