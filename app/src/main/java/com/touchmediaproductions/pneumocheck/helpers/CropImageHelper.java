package com.touchmediaproductions.pneumocheck.helpers;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;

import com.touchmediaproductions.pneumocheck.R;
import com.yalantis.ucrop.UCrop;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Class responsible for handling Cropping utilising UCrop library
 */
public class CropImageHelper {

    /**
     * Start the Cropping service utilising UCrop
     *
     * @param selectedUri
     */
    public static void startUCropActivity(Activity activityContext, Uri selectedUri) throws IOException {
        File destinationFile = createCroppedImageFile(activityContext);
        Uri destinationUri = Uri.fromFile(destinationFile);
        UCrop.Options options = new UCrop.Options();
        //Set format to PNG
        options.setCompressionFormat(Bitmap.CompressFormat.PNG);
        //Set branding colors
        options.setActiveControlsWidgetColor(activityContext.getResources().getColor(R.color.colorPrimary, activityContext.getTheme()));
        //Set instructions in Title
        options.setToolbarTitle("Crop, turn upright and center your X-Ray, then tap the checkmark when you are done.");
        UCrop.of(selectedUri, destinationUri).withOptions(options)
                //Square needed so aspect ration of 1:1
                .withAspectRatio(1, 1)
                //224 is the best preferred size of the ML Model input
                .withMaxResultSize(224, 224)
                .start(activityContext);
    }

    /**
     * Create copped image File to be used for saving the image once cropped
     *
     * @param context
     * @return File object of temp file
     * @throws IOException
     */
    private static File createCroppedImageFile(Context context) throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_CROPPED";
        File storageDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );
        return image;
    }
}
