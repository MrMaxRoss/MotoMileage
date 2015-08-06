package com.sortedunderbelly.motomileage.storage;

import android.content.Intent;
import android.test.RenamingDelegatingContext;

import com.sortedunderbelly.motomileage.AuthHelper;
import com.sortedunderbelly.motomileage.AuthStruct;

/**
 * Created by max.ross on 6/7/14.
 */
public class LocalDatabaseTripStorageTest extends BaseTripStorageTest {

    private static final String TEST_FILE_PREFIX = "test_";

    private static class MyAuthHelper implements AuthHelper {
        @Override
        public void onAuthStateChanged(AuthStruct struct, String error) {
        }

        @Override
        public void logout() {

        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {

        }
    }

    @Override
    TripStorage newTripStorage() {
        RenamingDelegatingContext rdc = new RenamingDelegatingContext(getContext(), TEST_FILE_PREFIX);
        return new LocalDatabaseTripStorage(rdc, STORAGE_CALLBACKS, new MyAuthHelper());
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
