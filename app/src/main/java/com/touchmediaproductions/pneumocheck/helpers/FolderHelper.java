package com.touchmediaproductions.pneumocheck.helpers;

import android.app.Activity;
import android.os.Environment;
import android.util.Log;

import java.io.File;

/**
 * Class responsible for manipulating folders
 */
public class FolderHelper {

    public static final String PROVIDER = "com.touchmediaproductions.android.fileprovider";

    /**
     * Method for clearing the pictures folder
     *
     * @param activity
     * @return
     */
    public static int clearPicturesFolder(Activity activity) {
        String path = String.valueOf(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        Log.d("Files", "Path: " + path);
        File directory = new File(path);
        //Get list of how many files are in folder
        File[] files = directory.listFiles();
        Log.d("Files", "Size: " + files.length);

        /*
         Loop through folder contents and count how many are deleted as it deletes them successfully.
         */
        int deletedFilesCount = 0;
        for (int i = 0; i < files.length; i++) {
            Log.d("Files", "FileName:" + files[i].getName());
            try {
                if (files[i].delete()) {
                    deletedFilesCount++;
                }
            } catch (Exception ex) {
                // Couldn't delete file
            }
        }
        return deletedFilesCount;
    }

}
