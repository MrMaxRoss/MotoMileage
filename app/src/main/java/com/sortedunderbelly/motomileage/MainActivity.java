// MainActivity.java
// Manages mileage for moto
package com.sortedunderbelly.motomileage;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.DialogFragment;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.SignInButton;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.NoSuchElementException;

public class MainActivity extends ListActivity implements DatePickerDialog.OnDateSetListener,
        SharedPreferences.OnSharedPreferenceChangeListener, StorageCallbacks {

    private AuthHelper authHelper;
    private EditText tripDateText;
    private EditText tripDescText;
    private EditText tripDistanceText;
    private TextView tripTotalValueText;
    private Spinner tripFilterSpinner;
    private Button saveOrUpdateButton;
    private TripStorage storage;
    private List<Trip> filteredTrips = new ArrayList<Trip>();
    private ArrayAdapter<Trip> adapter; // binds trip strings to ListView
    private Integer positionOfTripToUpdate = null;

    // TODO(max.ross): DateFormat is not threadsafe. Do we need to care about that in an android app?
    private DateFormat dateFormat;

    // called when MainActivity is first created
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        authHelper = new AuthHelper(this, findViewById(R.id.gridLayout), (SignInButton) findViewById(R.id.login_with_google));

        dateFormat = android.text.format.DateFormat.getDateFormat(this);

        // get references to the EditTexts
        tripDateText = (EditText) findViewById(R.id.tripDate);
        tripDescText = (EditText) findViewById(R.id.tripDesc);
        tripDistanceText = (EditText) findViewById(R.id.tripDistance);
        tripTotalValueText = (TextView) findViewById(R.id.totalTextValueView);
        tripFilterSpinner = (Spinner) findViewById(R.id.tripDisplaySpinner);
        // create ArrayAdapter and use it to bind tags to the ListView
        adapter = new MyArrayAdapter(getApplicationContext(), R.layout.list_item, filteredTrips, dateFormat);
        setListAdapter(adapter);

        // register listener to save a new or edited trip
        saveOrUpdateButton = (Button) findViewById(R.id.saveOrUpdateButton);
        saveOrUpdateButton.setOnClickListener(saveOrUpdateButtonListener);

        Button clearTripButton = (Button) findViewById(R.id.clearButton);
        clearTripButton.setOnClickListener(clearTripButtonListener);

        // register listener that lets user copy, update, or delete
        getListView().setOnItemClickListener(itemClickListener);
        setTripDateText(new Date());

        tripDateText.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog(view);
            }
        });
        SharedPreferences defaultSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        defaultSharedPrefs.registerOnSharedPreferenceChangeListener(this);
        initStorage(defaultSharedPrefs);
    }

    private void filterAllTrips(TripFilter filter) {
        filteredTrips.clear();
        int totalDistance = filter.filterTrips(storage.allTrips(), filteredTrips);
        notifyDataSetChanged(totalDistance);
    }

    private void notifyDataSetChanged(int totalDistance) {
        adapter.notifyDataSetChanged(); // rebind tags to ListView
        tripTotalValueText.setText(Integer.toString(totalDistance));
    }

    private String dateToString(Date d) {
        return dateFormat.format(d);
    }

    private Date stringToDate(String dateStr) throws ParseException {
        return dateFormat.parse(dateStr);
    }

    private static boolean hasText(EditText text) {
        return text.getText().length() > 0;
    }

    public OnClickListener saveOrUpdateButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            // create tag if all fields are populated
            if (hasText(tripDateText) && hasText(tripDescText) && hasText(tripDistanceText)) {
                Date date;
                int distance;
                try {
                    date = stringToDate(tripDateText.getText().toString());
                    distance = Integer.valueOf(tripDistanceText.getText().toString());
                } catch (ParseException e) {
                    simpleErrorDialog(R.string.invalidInputMessage);
                    return;
                }

                saveTrip(date, tripDescText.getText().toString(), distance);
                clearInputs();

                // TODO(max.ross): Find out what this does
                ((InputMethodManager) getSystemService(
                        Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(
                        tripDistanceText.getWindowToken(), 0);
                Toast.makeText(
                        getApplicationContext(), R.string.tripSavedText, Toast.LENGTH_SHORT).show();
            } else {
                // display message asking user to provide date, desc, and distance
                simpleErrorDialog(R.string.missingMessage);
            }
        }

    };

    public OnClickListener clearTripButtonListener = new OnClickListener() {
        @Override
        public void onClick(View v) {
            clearInputs();
        }

    };

    private void clearInputs() {
        setTripDateText(new Date());
        tripDescText.setText("");
        tripDistanceText.setText("");
        positionOfTripToUpdate = null;
        saveOrUpdateButton.setText(R.string.saveTripButtonText);
    }

    void simpleErrorDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Error")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, null)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    private void simpleErrorDialog(int messageId) {
        simpleErrorDialog(getResources().getString(messageId));
    }

    // add new Trip to the database, then refresh the ui list if the trip matches the current filter
    private void saveTrip(Date date, String description, int distance) {
        Trip trip;
        if (positionOfTripToUpdate == null) {
            trip = new TripImpl(date, description, distance);
        } else {
            trip = new TripImpl(filteredTrips.get(positionOfTripToUpdate).getId(), date, description, distance);
        }
        storage.save(trip);
    }

    private TripFilter getCurrentFilter() {
        int selectedItemPos = tripFilterSpinner.getSelectedItemPosition();
        return TripFilter.values()[selectedItemPos];
    }

    // itemClickListener lets the user edit/delete an existing trip
    OnItemClickListener itemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view,
                                final int position, long id) {
            // get the tag that the user long touched
            final Trip trip = filteredTrips.get(position);// ((TextView) view).getText().toString();
            if (trip == null) {
                simpleErrorDialog(R.string.invalidTripReference);
                return;
            }

            // create a new AlertDialog
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

            // set the AlertDialog's title
            builder.setTitle(getString(R.string.updateDeleteCopyTitle));

            // set list of items to display in dialog
            builder.setItems(R.array.dialog_items,
                    new DialogInterface.OnClickListener() {
                        // responds to user touch by sharing, editing or
                        // deleting a saved search
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case 0:
                                    // edit
                                    tripDateText.setText(dateToString(trip.getDate()));
                                    tripDescText.setText(trip.getDesc());
                                    tripDistanceText.setText(Integer.toString(trip.getDistance()));
                                    positionOfTripToUpdate = position;
                                    saveOrUpdateButton.setText(R.string.updateTripButtonText);
                                    break;
                                case 1:
                                    // delete
                                    deleteTrip(position, trip);
                                    break;
                                case 2:
                                    // copy, just the description and the distance since
                                    // the date is likely to change
                                    tripDescText.setText(trip.getDesc());
                                    tripDistanceText.setText(Integer.toString(trip.getDistance()));
                                    break;
                            }
                        }
                    }
            );

            // set the AlertDialog's negative Button
            builder.setNegativeButton(getString(R.string.cancel),
                    new DialogInterface.OnClickListener() {
                        // called when the "Cancel" Button is clicked
                        public void onClick(DialogInterface dialog, int id) {
                            dialog.cancel(); // dismiss the AlertDialog
                        }
                    }
            );
            builder.create().show(); // display the AlertDialog
        }
    };

    // deletes a search after the user confirms the delete operation
    private void deleteTrip(final int tripPosition, final Trip trip) {
        // create a new AlertDialog
        AlertDialog.Builder confirmBuilder = new AlertDialog.Builder(this);

        // set the AlertDialog's message
        confirmBuilder.setMessage(getString(R.string.confirmMessage));

        // set the AlertDialog's negative Button
        confirmBuilder.setNegativeButton(getString(R.string.cancel),
                new DialogInterface.OnClickListener() {
                    // called when "Cancel" Button is clicked
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel(); // dismiss dialog
                    }
                }
        );

        // set the AlertDialog's positive Button
        confirmBuilder.setPositiveButton(getString(R.string.delete),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // delete from storage first since that's more likely to fail
                        storage.delete(trip.getId());
                        filteredTrips.remove(tripPosition);

                        // rebind tags ArrayList to ListView to show updated list
                        int currentTotalDistance = Integer.valueOf(tripTotalValueText.getText().toString());
                        notifyDataSetChanged(currentTotalDistance - trip.getDistance());
                        Toast.makeText(getApplicationContext(), R.string.tripDeletedText, Toast.LENGTH_SHORT).show();
                    }
                }
        );

        confirmBuilder.create().show(); // display AlertDialog
    }


    public void showDatePickerDialog(View view) {
        Date initValue;
        try {
            initValue = dateFormat.parse(tripDateText.getText().toString());
        } catch (ParseException e) {
            initValue = new Date();
        }
        DialogFragment newFragment = new DatePickerFragment();
        Bundle args = new Bundle();

        args.putLong(DatePickerFragment.INIT_DATE_AS_LONG, initValue.getTime());
        newFragment.setArguments(args);

        newFragment.show(this.getFragmentManager(), "datePicker");
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(year, month, day);
        setTripDateText(cal.getTime());
    }

    private void setTripDateText(Date date) {
        tripDateText.setText(dateFormat.format(date));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_logout) {
            authHelper.logout();
            return true;
        } else if (id == R.id.action_settings) {
            Intent preferencesIntent = new Intent(this, SettingsActivity.class);
            startActivity(preferencesIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String prefStorageChoiceKey = getResources().getString(R.string.prefStorageChoiceKey);
        if (key.equals(prefStorageChoiceKey)) {
            initStorage(sharedPreferences);
            Toast.makeText(getApplicationContext(), R.string.storageChangedText, Toast.LENGTH_SHORT).show();
        }
    }

    private void initStorage(SharedPreferences preferences) {
        String keyName = getResources().getString(R.string.prefStorageChoiceKey);
        StorageSystem storageSystem;
        try {
            // Firebase is our default storage system
            storageSystem = StorageSystem.valueOf(
                    preferences.getString(keyName, StorageSystem.FIREBASE.name()));
        } catch (IllegalArgumentException iae) {
            storageSystem = StorageSystem.FIREBASE;
        }
        storage = storageSystem.getTripStorage(this, this, authHelper);
    }

    @Override
    public void onFullRefresh() {
        TripFilter tripFilter = storage.getLastTripFilter();
        // Set the value here without the listener so that we don't trigger an update
        tripFilterSpinner.setSelection(tripFilter.ordinal());
        filteredTrips.clear();
        int totalDistance = tripFilter.filterTrips(storage.allTrips(), filteredTrips);
        Collections.sort(filteredTrips);
        notifyDataSetChanged(totalDistance);

        if (tripFilterSpinner.getOnItemSelectedListener() == null) {
            // Now set the listener
            tripFilterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int itemPosition, long id) {
                    TripFilter filter = TripFilter.values()[itemPosition];
                    filterAllTrips(filter);
                    storage.saveTripFilter(filter);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        }
    }

    @Override
    public void onNewTrip(Trip trip) {
        int currentTotalDistance = Integer.valueOf(tripTotalValueText.getText().toString());
        TripFilter filter = getCurrentFilter();
        boolean newTripMatchesFilter = filter.filterTrip(trip);
        if (newTripMatchesFilter) {
            filteredTrips.add(trip);
            Collections.sort(filteredTrips);
            notifyDataSetChanged(currentTotalDistance + trip.getDistance());
        }
    }

    private int getIndexOfTrip(String tripId) {
        for (int index = 0; index < filteredTrips.size(); index++) {
            if (filteredTrips.get(index).getId().equals(tripId)) {
                return index;
            }
        }
        throw new NoSuchElementException();
    }

    @Override
    public void onUpdatedTrip(Trip updatedTrip) {
        // Get the index of the updated trip
        int index;
        try {
            index = getIndexOfTrip(updatedTrip.getId());
        } catch (NoSuchElementException e) {
            // couldn't find the Trip so assume it was previously not part of the filtered view
            // treat it as a new trip instead
            onNewTrip(updatedTrip);
            return;
        }
        int currentTotalDistance = Integer.valueOf(tripTotalValueText.getText().toString());
        TripFilter filter = getCurrentFilter();
        boolean updatedTripMatchesFilter = filter.filterTrip(updatedTrip);
        Trip oldTrip = filteredTrips.get(index);
        int newTotalDistance = currentTotalDistance - oldTrip.getDistance();
        if (updatedTripMatchesFilter) {
            newTotalDistance += updatedTrip.getDistance();
            filteredTrips.set(index, updatedTrip);
            Collections.sort(filteredTrips);
        } else {
            filteredTrips.remove(index);
        }
        notifyDataSetChanged(newTotalDistance);
    }

    @Override
    public void onDeletedTrip(String deletedTripId) {
        // Get the index of the deleted trip
        int index;
        try {
            index = getIndexOfTrip(deletedTripId);
        } catch (NoSuchElementException e) {
            // couldn't find the Trip, likely means it didn't match the filter so just swallow
            return;
        }
        int currentTotalDistance = Integer.valueOf(tripTotalValueText.getText().toString());
        Trip deletedTrip = filteredTrips.get(index);
        filteredTrips.remove(index);
        notifyDataSetChanged(currentTotalDistance - deletedTrip.getDistance());
    }

    /**
     * This method fires when any startActivityForResult finishes. The requestCode maps to
     * the value passed into startActivityForResult.
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        authHelper.onActivityResult(requestCode, resultCode, data);
    }

    public void login(String token) {
        storage.login(token);
    }

    public void logout(AuthStruct authStruct) {
        storage.logout(authStruct);
    }
}