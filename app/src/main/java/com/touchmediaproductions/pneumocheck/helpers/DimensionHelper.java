package com.touchmediaproductions.pneumocheck.helpers;

import android.content.Context;

/**
 * Class responsible for dimension conversions, as most methods take pixels and not dps, this class converts from pixels to DPs and back.
 */
public class DimensionHelper {

    /**
     * Convert from pixels to DP values
     *
     * @param c     Context such as activity
     * @param pixel float value for pixels to convert to dp
     * @return dp value
     */
    public static float pixelTodp(Context c, float pixel) {
        float density = c.getResources().getDisplayMetrics().density;
        float dp = pixel / density;
        return dp;
    }

    /**
     * Convert from DP number to pixel value with consideration of the screen
     *
     * @param c  context such as activity
     * @param dp float value of dp value to convert to pixels
     * @return pixels float value
     */
    public static float dpTopixel(Context c, float dp) {
        float density = c.getResources().getDisplayMetrics().density;
        float pixel = dp * density;
        return pixel;
    }

}
