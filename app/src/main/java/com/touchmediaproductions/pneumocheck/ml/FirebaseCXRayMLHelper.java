package com.touchmediaproductions.pneumocheck.ml;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.common.modeldownload.FirebaseModelManager;
import com.google.firebase.ml.custom.FirebaseCustomRemoteModel;
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
        //FIREBASE ML
        final FirebaseCustomRemoteModel remoteModel = new FirebaseCustomRemoteModel.Builder(MODELNAME).build();

        final FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder()
                .requireWifi()
                .build();

        FirebaseModelManager.getInstance().isModelDownloaded(remoteModel).addOnCompleteListener(new OnCompleteListener<Boolean>() {
            @Override
            public void onComplete(@NonNull Task<Boolean> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Is Model Downloaded " + task.getResult().booleanValue());
                    if (task.getResult().booleanValue()) {
                        Log.i(TAG, "Using Latest Model File " + remoteModel.getModelName());
                        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
                                .addOnCompleteListener(new OnCompleteListener<File>() {
                                    @Override
                                    public void onComplete(@NonNull Task<File> task) {
                                        File modelFile = task.getResult();
                                        if (modelFile != null) {
                                            firebaseTFLite = new Interpreter(modelFile);
                                        }
                                    }
                                });
                    } else {
                        //Download the model as its not downloaded:
                        Log.i(TAG, "Downloading Model File: " + remoteModel.getModelName());
                        FirebaseModelManager.getInstance().download(remoteModel, conditions)
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void v) {
                                        // Download complete. Depending on your app, you could enable
                                        // the ML feature, or switch from the local model to the remote
                                        // model, etc.
                                        Log.i(TAG, "Model downloaded " + remoteModel.getModelName() + " from FIREBASE");
                                        Log.i(TAG, "Using downloaded Model File " + remoteModel.getModelName());
                                        FirebaseModelManager.getInstance().getLatestModelFile(remoteModel)
                                                .addOnCompleteListener(new OnCompleteListener<File>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<File> task) {
                                                        File modelFile = task.getResult();
                                                        if (modelFile != null) {
                                                            firebaseTFLite = new Interpreter(modelFile);
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                }
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
