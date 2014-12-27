package com.sortedunderbelly.motomileage.storage;

import android.content.Intent;
import android.view.Menu;

/**
 * Created by max.ross on 10/17/14.
 */
public abstract class NoAuthTripStorage implements TripStorage {

    NoAuthTripStorage() {
    }

    @Override
    public final void login(String authToken) {
    }

    @Override
    public final void logout() {
    }

    @Override
    public final void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
    }
}
