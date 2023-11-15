package com.touchmediaproductions.pneumocheck.helpers;


import android.content.Context;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Class created to be able to customize every Toast used throughout the app in a central place.
 */
public class ToastHelper {

    /**
     * Customized Long Toast
     *
     * @param context
     * @param message
     */
    public static void showLongToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
//        toast.setGravity(Gravity.BOTTOM, 0, 250);
        toast.show();
    }

    /**
     * Customized Short Toast
     *
     * @param context
     * @param message
     */
    public static void showShortToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
//        toast.setGravity(Gravity.BOTTOM, 0, 250);
        toast.show();
    }

}
