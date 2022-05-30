package com.touchmediaproductions.pneumocheck.helpers;

import android.app.Activity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

/**
 * Class responsible for dismissing the keyboard
 */
public class KeyboardHelper {

    public static void hideSoftKeyboard(View view, Activity activity) {
        try {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (Exception ex) {
            Log.i("Keyboard", "Attempted to close but couldn't, can be safely ignored");
        }
    }
}
