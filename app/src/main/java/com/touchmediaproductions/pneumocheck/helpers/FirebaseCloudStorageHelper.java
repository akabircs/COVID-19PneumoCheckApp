package com.touchmediaproductions.pneumocheck.helpers;

import com.google.firebase.storage.FirebaseStorage;

public class FirebaseCloudStorageHelper {
    private static final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();

    public static FirebaseStorage getFirebaseStorage() {
        return firebaseStorage;
    }

}
