package com.touchmediaproductions.pneumocheck.ml;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
//import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
//import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
//import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
import com.google.firebase.ml.modeldownloader.CustomModel;
import com.google.firebase.ml.modeldownloader.CustomModelDownloadConditions;
import com.google.firebase.ml.modeldownloader.DownloadType;
import com.google.firebase.ml.modeldownloader.FirebaseModelDownloader;
import com.touchmediaproductions.pneumocheck.helpers.FirestoreRepository;

import org.tensorflow.lite.Interpreter;

import java.io.File;
import java.util.List;

public class FirebaseCXRayMLHelper {

    private static final String TAG = "FirebaseCXRayMLHelper";


    //Instance fields:
    private List<String> LABELS_LIST = null;
    private final String MODELNAME = "firebasecovidnetmodel";


    private Interpreter firebaseTFLite = null;
    private static FirebaseCXRayMLHelper instance;

    private FirebaseCXRayMLHelper() {
        prepareFirebaseCXRayML();
    }

    public static FirebaseCXRayMLHelper getInstance() {
        if (instance == null) {
            instance = new FirebaseCXRayMLHelper();
        }
        return instance;
    }


    private void prepareFirebaseCXRayML() {

        final CustomModelDownloadConditions conditions = new CustomModelDownloadConditions.Builder()
                .requireWifi()  // Also possible: .requireCharging() and .requireDeviceIdle()
                .build();


        FirebaseModelDownloader.getInstance().getModel(
                        MODELNAME,
                        DownloadType.LOCAL_MODEL_UPDATE_IN_BACKGROUND, conditions)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        CustomModel model = task.getResult();
                        Log.i(TAG, "Using Latest Model File " + model.getName());
                        Log.i(TAG, "Model Downloaded: " + model.getDownloadId());
                        firebaseTFLite = new Interpreter(model.getFile());

                    } else {
                        Log.e(TAG, "Error downloading model: " + task.getException());
                    }
                });

        //Prepare Fetch Labels from Firestore
        FirestoreRepository.getMLModelLabels(this.getModelName()).addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //Iterate through the submissions to build a SubmissionModel object for each
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        try {
                            List<String> labelsList = (List<String>) document.get("labelsList");
                            LABELS_LIST = labelsList;
                            Log.i(TAG, "Labels: " + labelsList.toString());
                        } catch (Exception ex) {
                            Log.d(TAG, "Error getting labels: ", ex);
                        }
                    }
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }


    public Interpreter getFirebaseTFLite() {
        if (isModelReady()) {
            return firebaseTFLite;
        }
        return null;
    }

    /**
     * If both the labels and the model downloaded and not null the model is ready.
     *
     * @return
     */
    public boolean isModelReady() {
        if (LABELS_LIST != null && firebaseTFLite != null) {
            return true;
        }
        return false;
    }

    public String getModelName() {
        return MODELNAME;
    }

    public List<String> getModelLabels() {
        return this.LABELS_LIST;
    }
}
