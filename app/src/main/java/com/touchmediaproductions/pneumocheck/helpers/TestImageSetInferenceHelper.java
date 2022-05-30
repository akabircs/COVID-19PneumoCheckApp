package com.touchmediaproductions.pneumocheck.helpers;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

public class TestImageSetInferenceHelper {

    private static final String TAG = "PytorchMLHelper";

    public static class ImageSet {
        public String imageUrl;
        public String diagnosis;
        public String path;

        @Override
        public String toString() {
            return  "imageUrl='" + imageUrl + '\'' +
                    ", diagnosis='" + diagnosis + '\'' +
                    ", path='" + path + '\'';
        }
    }

    /**
     * Read the file covid-xray-test-images.txt in the assets folder and return the list of image urls
     */
    public static ImageSet[] getImageUrls(Context context, int limit) {
        ImageSet[] imageUrls = null;
        try {
            InputStream inputStream = context.getAssets().open("covid-xray-test-images.txt");
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            // For each line split into tuple of image url and diagnosis
            imageUrls = new ImageSet[limit];
            int i = 0;
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                String[] lineSplit = line.split("\\s+");
                if (lineSplit.length != 3) {
                    Log.e(TAG, "Invalid line: " + line);
                    continue;
                }
                ImageSet imageSet = new ImageSet();
                imageSet.path = lineSplit[0];
                imageSet.diagnosis = lineSplit[1];
                imageSet.imageUrl = lineSplit[2];
                imageUrls[i] = imageSet;
                i++;
                if (i == limit) {
                    break;
                }
            }
            Log.i(TAG, "imageUrls: " + imageUrls.length);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return imageUrls;
    }

}
