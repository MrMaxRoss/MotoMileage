package com.sortedunderbelly.motomileage;

public class AuthStruct {
    private final String provider;
    private final String displayName;
    private final String authToken;

    AuthStruct(String provider, String displayName, String authToken) {
        this.provider = provider;
        this.displayName = displayName;
        this.authToken = authToken;
    }

    public String getProvider() {
        return provider;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAuthToken() { return authToken; }
}

