package com.touchmediaproductions.pneumocheck.ui.settings;

import android.util.Log;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;
import com.touchmediaproductions.pneumocheck.models.UserProfile;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class SettingsViewModel extends ViewModel {

    private static final String TAG = "SettingsViewModel";

    private MutableLiveData<UserProfile> userProfileMutableLiveData;

    private LinkedList<ListenerRegistration> snapShotListeners = new LinkedList<>();

    public LiveData<UserProfile> getUserProfile(String uid){
        if(userProfileMutableLiveData == null){
            userProfileMutableLiveData = new MutableLiveData<>();
            fetchLiveUserProfile(uid);
        }
        return userProfileMutableLiveData;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        if(snapShotListeners != null) {
            for (ListenerRegistration listenerRegistration : snapShotListeners) {
                try {
                    listenerRegistration.remove();
                } catch(Exception ex){
                    Log.i(TAG, "Unable to remove snap shot listener");
                }
            }
        }
    }

    public void fetchLiveUserProfile(String uid){
        snapShotListeners.add(FirestoreRepository.getUserProfile(uid).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(value != null && value.exists()) {
                    userProfileMutableLiveData.setValue(value.toObject(UserProfile.class));
                }
            }
        }));
    }
}
