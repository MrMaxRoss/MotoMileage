package com.sortedunderbelly.motomileage;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.IntentSender;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.Scopes;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import java.io.IOException;

/**
 * Created by max.ross on 10/12/14.
 */
public class AuthHelper implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, AuthCallbacks {

    /* A tag that is used for logging statements */
    private static final String TAG = "AuthHelper";

    private MainActivity activity;

    private AuthStruct authStruct;

    /* A dialog that is presented until the authentication is finished. */
    private ProgressDialog authProgressDialog;

    /* Request code used to invoke sign in user interactions for Google+ */
    public static final int RC_GOOGLE_LOGIN = 1;

    /* Client used to interact with Google APIs. */
    private GoogleApiClient googleApiClient;

    /* A flag indicating that a PendingIntent is in progress and prevents us from starting further intents. */
    private boolean googleIntentInProgress;

    /* Track whether the sign-in button has been clicked so that we know to resolve all issues preventing sign-in
     * without waiting. */
    private boolean googleLoginClicked;

    /* Store the connection result from onConnectionFailed callbacks so that we can resolve them when the user clicks
     * sign-in. */
    private ConnectionResult googleConnectionResult;

    /* The login button for Google */
    private SignInButton googleLoginButton;

    private View gridLayout;

    public AuthHelper(MainActivity activity, View gridLayout, SignInButton googleLoginButton) {
        this.activity = activity;
        this.gridLayout = gridLayout;
        this.googleLoginButton = googleLoginButton;
        googleLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                googleLoginClicked = true;
                if (!googleApiClient.isConnecting()) {
                    if (googleConnectionResult != null) {
                        resolveSignInError();
                    } else if (googleApiClient.isConnected()) {
                        getGoogleOAuthTokenAndLogin();
                    } else {
                    /* connect API now */
                        Log.d(TAG, "Trying to connect to Google API");
                        googleApiClient.connect();
                    }
                }
            }
        });

        /* Setup the Google API object to allow Google+ logins */
        googleApiClient = new GoogleApiClient.Builder(this.activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        authProgressDialog = new ProgressDialog(this.activity);
        authProgressDialog.setTitle("Loading");
        authProgressDialog.setMessage("Authenticating with Moto Mileage...");
        authProgressDialog.setCancelable(true);
        authProgressDialog.show();

    }

    /* A helper method to resolve the current ConnectionResult error. */
    private void resolveSignInError() {
        if (googleConnectionResult.hasResolution()) {
            try {
                googleIntentInProgress = true;
                googleConnectionResult.startResolutionForResult(activity, RC_GOOGLE_LOGIN);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                googleIntentInProgress = false;
                googleApiClient.connect();
            }
        }
    }

    private void getGoogleOAuthTokenAndLogin() {
        authProgressDialog.show();
        /* Get OAuth token in Background */
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            String errorMessage = null;

            @Override
            protected String doInBackground(Void... params) {
                String token = null;

                try {
                    String scope = String.format("oauth2:%s", Scopes.PLUS_LOGIN);
                    token = GoogleAuthUtil.getToken(activity, Plus.AccountApi.getAccountName(googleApiClient), scope);
                } catch (IOException transientEx) {
                    /* Network or server error */
                    Log.e(TAG, "Error authenticating with Google: " + transientEx);
                    errorMessage = "Network error: " + transientEx.getMessage();
                } catch (UserRecoverableAuthException e) {
                    Log.w(TAG, "Recoverable Google OAuth error: " + e.toString());
                    /* We probably need to ask for permissions, so start the intent if there is none pending */
                    if (!googleIntentInProgress) {
                        googleIntentInProgress = true;
                        Intent recover = e.getIntent();
                        activity.startActivityForResult(recover, RC_GOOGLE_LOGIN);
                    }
                } catch (GoogleAuthException authEx) {
                    /* The call is not ever expected to succeed assuming you have already verified that
                     * Google Play services is installed. */
                    Log.e(TAG, "Error authenticating with Google: " + authEx.getMessage(), authEx);
                    errorMessage = "Error authenticating with Google: " + authEx.getMessage();
                }
                return token;
            }

            @Override
            protected void onPostExecute(String token) {
                googleLoginClicked = false;
                if (token != null) {
                    activity.login(token);
                } else if (errorMessage != null) {
                    authProgressDialog.hide();
                    activity.simpleErrorDialog(errorMessage);
                }
            }
        };
        task.execute();
    }

    @Override
    public void onConnected(final Bundle bundle) {
        /* Connected with Google API, use this to authenticate */
        getGoogleOAuthTokenAndLogin();
    }


    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!googleIntentInProgress) {
            /* Store the ConnectionResult so that we can use it later when the user clicks on the Google+ login button */
            googleConnectionResult = result;

            if (googleLoginClicked) {
                /* The user has already clicked login so we attempt to resolve all errors until the user is signed in,
                 * or they cancel. */
                resolveSignInError();
            } else {
                Log.e(TAG, result.toString());
            }
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        // ignore
        Log.i(TAG, "Connection suspended");
    }

    private void setAuthenticatedUser(/* @Nullable */AuthStruct authStruct) {
        this.authStruct = authStruct;
        if (authStruct != null) {
            /* Hide all the login buttons */
            googleLoginButton.setVisibility(View.GONE);
            gridLayout.setVisibility(View.VISIBLE);
        } else {
            /* No authenticated user show all the login buttons */
            googleLoginButton.setVisibility(View.VISIBLE);
            gridLayout.setVisibility(View.GONE);
        }
    }

    /**
     * This method fires when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_GOOGLE_LOGIN) {
            /* This was a request by the Google API */
            if (resultCode != Activity.RESULT_OK) {
                googleLoginClicked = false;
            }
            googleIntentInProgress = false;
            if (!googleApiClient.isConnecting()) {
                googleApiClient.connect();
            }
        } else {
            /* Unknown */
            Log.e(TAG, "Unknown auth provider");
        }
    }

    @Override
    public void onAuthStateChanged(/* @Nullable */AuthStruct authStruct, String error) {
        authProgressDialog.hide();
        if (error != null) {
            activity.simpleErrorDialog(error);
        }
        setAuthenticatedUser(authStruct);
    }

    /**
     * Unauthenticate from providers.
     */
    public void logout() {
        if (authStruct != null) {
            activity.logout(authStruct);
            /* Logout of any of the Frameworks. This step is optional, but ensures the user is not logged into
             * Google+
             */
            if (authStruct.getProvider().equals("google")) {
                /* Logout from Google+ */
                if (googleApiClient.isConnected()) {
                    Plus.AccountApi.clearDefaultAccount(googleApiClient);
                    googleApiClient.disconnect();
                }
            }
            authStruct = null;
        }
    }
}
