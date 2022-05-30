package com.touchmediaproductions.pneumocheck.ui.help;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;

import java.util.HashMap;
import java.util.List;

/**
 * FAQ Information
 */
public class ExpandableListDataInjector {

    public static Task<DocumentSnapshot> getData() {
        return FirestoreRepository.getHelpDocumentFAQ();
    }
}