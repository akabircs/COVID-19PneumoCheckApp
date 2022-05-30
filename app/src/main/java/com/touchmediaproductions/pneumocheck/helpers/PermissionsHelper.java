package com.touchmediaproductions.pneumocheck.helpers;


import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import java.util.ArrayList;

/**
 * Takes care of all permission purposes.
 */
public class PermissionsHelper {

    private static final int REQUEST_CAMERA = 1;
    private static final int REQUEST_STORAGE = 2;

    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };
    private static final String[] STORAGE_PERMISSIONS = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int REQUEST_MULTIPLE = 3;

    private final Activity context;

    public PermissionsHelper(Activity context) {
        this.context = context;
    }

    public boolean checkCameraPermission() {
        int permissionCamera = context.checkSelfPermission(Manifest.permission.CAMERA);

        if (permissionCamera != PackageManager.PERMISSION_GRANTED) {
            if (context.shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
                ToastHelper.showShortToast(context, "Camera permission is needed to show the camera preview.");
            }
            context.requestPermissions(CAMERA_PERMISSIONS, REQUEST_CAMERA);
            return false;
        }

        return true;
    }

    public boolean checkStoragePermission() {
        int permissionWriteExternalMemory = context.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionReadExternalMemory = context.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionWriteExternalMemory == PackageManager.PERMISSION_DENIED || permissionReadExternalMemory == PackageManager.PERMISSION_DENIED) {
            if (context.shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                ToastHelper.showShortToast(context, "Storage permission is needed to save user generated files.");
            }
            context.requestPermissions(STORAGE_PERMISSIONS, REQUEST_STORAGE);
            return false;
        }
        return true;
    }

    public boolean checkMultiplePermissions(String... permissions) {
        ArrayList<String> permissionsToRequest = new ArrayList<>();
        boolean noPermissionRequestRequired = true;
        for (String permission : permissions) {
            int currentPermission = context.checkSelfPermission(permission);

            if (currentPermission != PackageManager.PERMISSION_GRANTED) {
                if (context.shouldShowRequestPermissionRationale(permission)) {
                    ToastHelper.showShortToast(context, "This permission is required to accomplish your request.");
                }
                permissionsToRequest.add(permission);
                noPermissionRequestRequired = false;
            }
        }
        if (noPermissionRequestRequired == false) {
            commitRequestPermissions(permissionsToRequest.toArray(new String[0]));
        }
        return noPermissionRequestRequired;
    }

    private void commitRequestPermissions(String[] permissions) {
        context.requestPermissions(permissions, REQUEST_MULTIPLE);
    }

    /**
     * The context activity should override its own onRequestPermissionsResult and call this method in the body.
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     * @return
     */
    public boolean onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_CAMERA) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                ToastHelper.showShortToast(context, "Camera permission was not granted, please check app permissions in settings.");
            }
            return true;
        } else if (requestCode == REQUEST_STORAGE) {
            if (grantResults[0] + grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                ToastHelper.showShortToast(context, "Storage permission was not granted, please check app permissions in settings.");
            }
            return true;
        } else if (requestCode == REQUEST_MULTIPLE) {
            if (sumGrantResults(grantResults) == PackageManager.PERMISSION_GRANTED) {
                // Permission Granted
            } else {
                ToastHelper.showShortToast(context, "Permissions were not granted, please check app permissions in settings.");
            }
            return true;
        } else {
            return false;
        }
    }

    private int sumGrantResults(int[] grantResults) {
        int sum = 0;
        for (int i : grantResults) {
            sum += i;
        }
        return sum;
    }

}
