package com.touchmediaproductions.pneumocheck.helpers;

import android.content.Context;
import android.graphics.Bitmap;

import com.touchmediaproductions.pneumocheck.ml.MLHelper;
import com.touchmediaproductions.pneumocheck.ml.MLModels;

/**
 * Class responsible for checking if image is X-Ray or Not
 */
public class XRayNotXRayHelper {

    /**
     * Check if bitmap is valid Chest X-Ray.
     * Rotate image all four angles and run classification to make sure, its not being flipped.
     *
     * @param context
     * @param bitmap
     * @return
     */
    public static boolean isValidChestXRay(Context context, Bitmap bitmap) {
        Bitmap[] rotatedBitmaps = PictureHelper.rotateBitmapInAllFourDirections(bitmap);
        for (int i = 0; i < rotatedBitmaps.length; i++) {
            Bitmap currentRotatedBitmap = rotatedBitmaps[i];
            MLHelper.Prediction prediction = MLHelper.runClassificationOnBitmap(context, currentRotatedBitmap, MLModels.QUANT_XRAY_MODEL);
            //If the picture is an X-Ray OR the first value (if its not an X-Ray) is less than 99% sure that its not an X-Ray (meaning any chances that it might be an X-Ray it is (safe measure to provide a soft validation))
            if (prediction.getFirst().matches("X-Ray") || prediction.getFirstValue() < 99) {
                return true;
            }
        }
        return false;
    }
}
