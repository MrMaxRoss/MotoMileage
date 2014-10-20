package com.sortedunderbelly.motomileage;

/**
 * Created by max.ross on 10/17/14.
 */
public abstract class NoAuthTripStorage implements TripStorage {

    private final AuthCallbacks authCallbacks;

    NoAuthTripStorage(AuthCallbacks authCallbacks) {
        this.authCallbacks = authCallbacks;
    }

    @Override
    public void login(String authToken) {
        authCallbacks.onAuthStateChanged(new AuthStruct("noauth", "noauth", ""), null);
    }
}
