package com.sortedunderbelly.motomileage;

import android.content.Intent;

/**
 * Created by max.ross on 8/5/15.
 */
public interface AuthHelper {
    void onAuthStateChanged(/* @Nullable */ AuthStruct struct, String error);
    void logout();
    void onActivityResult(int requestCode, int resultCode, Intent data);
}
