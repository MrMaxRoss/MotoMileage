package com.sortedunderbelly.motomileage;

import android.app.Activity;
import android.os.Bundle;

/**
 * Created by max.ross on 5/17/14.
 */
public class SettingsActivity extends Activity
{
    // use FragmentManager to display SettingsFragment
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
    }
}
