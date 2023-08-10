package com.touchmediaproductions.pneumocheck.helpers;

import android.app.Application;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.UploadTask;
import com.touchmediaproductions.pneumocheck.MainActivity;
import com.touchmediaproductions.pneumocheck.ml.MLHelper;
import com.touchmediaproductions.pneumocheck.models.SubmissionModel;
import com.touchmediaproductions.pneumocheck.survey.SurveyResults;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

public class FirestoreRepository {

    private static final String TAG = "FirestoreDB";
    private static FirebaseFirestore fsDatabase = FirebaseFirestore.getInstance();

    /**
     * AccountTypes
     */
    public enum AccountType {
        doctor, participant, researcher
    }

    /**
     * SruveyState
     */
    public enum SurveyState {
        complete, incomplete, neverattempted
    }

    public static FirebaseFirestore getFirestoreInstance() {
        return fsDatabase;
    }

    /**
     * Used for initial user profile creation in Firestore
     *
     * @param uid
     * @param displayName
     * @param accountType
     */
    public static void createUserProfile(String uid, String displayName, AccountType accountType, SurveyState surveyState) {
        Map<String, Object> user = new HashMap<>();
        //uid might be unecessary if already adding the document under the uid name
        user.put("uid", uid);
        /////////////////////////////
        user.put("displayName", displayName);
        user.put("accountType", accountType.toString());
        user.put("surveyState", surveyState.toString());
        user.put("associatedUsers", new ArrayList<String>());


        // Add a new document with a generated ID
        fsDatabase.collection("users").document(uid).set(user)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "User profile added to db.");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error adding document", e);
                    }
                });
    }


    /**
     * Used for deleting user profile data (final account shutdown) use with care.
     *
     * @param uid
     */
    public static void deleteUserProfile(String uid) {
        fsDatabase.collection("users").document(uid).delete().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "User cloud profile was deleted successfully.");
                }
            }
        });
    }

    public static DocumentReference getUserProfile(String uid) {
        return fsDatabase.collection("users").document(uid);
    }

    public static Task<QuerySnapshot> getSubmissionForUser(String userId) {
        Task<QuerySnapshot> submissionByUser = fsDatabase.collection("submissions").whereEqualTo("userId", userId).get();
        return submissionByUser;
    }

    public static void deleteSubmission(SubmissionModel submissionModel) {
        //Delete Submission
        fsDatabase.collection("submissions").document(submissionModel.getId()).delete();
        //Delete Images
        FirebaseCloudStorageHelper.getFirebaseStorage().getReference(submissionModel.getXrayType().toString().toLowerCase()).child(submissionModel.getId()).delete();
        //Remove from User Array
        removeXrayIdFromUser(submissionModel.getUserId(), submissionModel.getId(), submissionModel.getXrayType());
    }

    public static void updateSubmission(String submissionId, SubmissionModel submission) {
        final String predictionString = submission.getConfirmation().name();

        //Push to Xrays Collection
        //Prepare db Content value pairs:
        HashMap<String, Object> xrayMetadata = new HashMap<>();
        xrayMetadata.put("prediction", predictionString);

        FirestoreRepository.getFirestoreInstance().collection("submissions").document(submissionId).update(xrayMetadata).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.i(TAG, "Submission metadata was updated successfully.");
                }
            }
        });
    }

    public static Task<Void> saveSurveyResults(String userUID, SurveyResults surveyResults) {
        Map<String, Object> surveyResultsMap = new HashMap<>();
        surveyResultsMap.put("sex", surveyResults.getSex());
        surveyResultsMap.put("age", surveyResults.getAge());
        surveyResultsMap.put("medicalConditions", surveyResults.getMedicalConditions());
        surveyResultsMap.put("smoker", surveyResults.getSmoker());
        surveyResultsMap.put("todaySymptom", surveyResults.getTodaySymptom());
        surveyResultsMap.put("covidResults", surveyResults.getCovidResults());
        surveyResultsMap.put("hospitalStatus", surveyResults.getHospitalised());
        surveyResultsMap.put("surveyState", surveyResults.getSurveyState());

        return fsDatabase.collection("users").document(userUID).update(surveyResultsMap);
    }


    public static boolean add(SubmissionModel submission, BiFunction<String, String, String> runAfterImageIsUploaded) {
        Date scanCreationDate = submission.getScanCreationDate();
        Date submissionCreationDate = submission.getSubmissionCreationDate();

        final String scanCreationString = DateHelper.fromDateToISO8601String(scanCreationDate);
        final String submissionCreationString = DateHelper.fromDateToISO8601String(submissionCreationDate);
        final String predictionString = submission.getConfirmation().name();
        HashMap<String, Float> probabilitiesMap = null;
        String modelNameString = "";
        MLHelper.Prediction prediction = submission.getPrediction();
        if (prediction != null) {
            probabilitiesMap = submission.getPrediction().getSortedProbabilities();
            modelNameString = submission.getPrediction().getModelName() != null ? submission.getPrediction().getModelName() : null;
        }
        final String userId = submission.getUserId();
        final byte[] xrayImage = submission.getCxrPhoto();
        final SubmissionModel.XrayType xrayTypeName = submission.getXrayType();

        //Push to Xrays Collection
        //Prepare db Content value pairs:
        HashMap<String, Object> xrayMetadata = new HashMap<>();
        xrayMetadata.put("userId", userId);
        xrayMetadata.put("dateOfScan", scanCreationString);
        xrayMetadata.put("submissionDate", submissionCreationString);
        xrayMetadata.put("prediction", predictionString);
        xrayMetadata.put("modelName", modelNameString);
        xrayMetadata.put("probabilities", probabilitiesMap);
        xrayMetadata.put("imageType", xrayTypeName);

        // Adding Learnt At
        xrayMetadata.put("learntAt", submission.getLearntAt());

        FirestoreRepository.getFirestoreInstance().collection("submissions").add(xrayMetadata).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
            @Override
            public void onComplete(@NonNull Task<DocumentReference> task) {
                if (task.isSuccessful()) {
                    String xrayId = task.getResult().getId();
                    pushXrayIdToUser(userId, xrayId, xrayTypeName);
                    pushImageWithXrayId(xrayId, xrayImage, xrayTypeName, runAfterImageIsUploaded);
                } else {
                    Log.i(TAG, "Couldn't save xray submission");
                }
            }
        });

        return true;
    }

    private static void pushXrayIdToUser(String userId, String xrayId, SubmissionModel.XrayType xrayTypeName) {
        String xrayTypeNameString = xrayTypeName.toString().toLowerCase();
        FirestoreRepository.getFirestoreInstance().collection("users").document(userId).update(xrayTypeNameString, FieldValue.arrayUnion(xrayId));
    }

    private static void removeXrayIdFromUser(String userId, String xrayId, SubmissionModel.XrayType xrayTypeName) {
        String xrayTypeNameString = xrayTypeName.toString().toLowerCase();
        FirestoreRepository.getFirestoreInstance().collection("users").document(userId).update(xrayTypeNameString, FieldValue.arrayRemove(xrayId));
    }

    private static void pushImageWithXrayId(final String xrayId, byte[] xrayPhoto, SubmissionModel.XrayType xrayTypeName, BiFunction<String, String, String> runAfterImageIsUploaded) {
        String xrayTypeNameString = xrayTypeName.toString().toLowerCase();
        final UploadTask imageUpload = FirebaseCloudStorageHelper.getFirebaseStorage().getReference(xrayTypeNameString).child(xrayId).putBytes(xrayPhoto);
        imageUpload.addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Task<Uri> getDownloadUrlTask = task.getResult().getMetadata().getReference().getDownloadUrl();
                getDownloadUrlTask.addOnSuccessListener(uri -> {
                    String imageUrl = uri.toString();
                    addImageUrlToSubmissionOnFirebase(xrayId, imageUrl, runAfterImageIsUploaded);
                });
            } else {
                String error = task.getException().getMessage();
                Log.i(TAG, error);
            }
        });
    }

    private static void addImageUrlToSubmissionOnFirebase(String submissionId, String imageUrl, BiFunction<String, String, String> runAfterImageIsUploaded) {
        HashMap<String, Object> imageUrlMap = new HashMap<>();
        imageUrlMap.put("imageUrl", imageUrl);
        FirestoreRepository.getFirestoreInstance().collection("submissions").document(submissionId).update(imageUrlMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    if (runAfterImageIsUploaded != null) {
                        runAfterImageIsUploaded.apply(submissionId, imageUrl);
                    }
                    Log.i(TAG, "Image URL saved.");
                } else {
                    Log.i(TAG, "Couldn't save imageUrl to submission");
                }
            }
        });
    }


    /**
     * Method for retrieving ML Labels based on Model Name
     *
     * @param modelName
     * @return
     */
    public static Task<QuerySnapshot> getMLModelLabels(String modelName) {
        Task<QuerySnapshot> resultQuerySnapshot = fsDatabase.collection("mlModelLabels").whereEqualTo("modelName", modelName).get();
        return resultQuerySnapshot;
    }


    /**
     * Get Help Strings for Help App Section
     *
     * @return
     */
    public static Task<DocumentSnapshot> getHelpDocumentFAQ() {
        return fsDatabase.collection("help").document("faq").get();
    }

    public static DocumentReference getTestBatchResults(String testBatchSubmissionId) {
        return fsDatabase.collection("test_batch").document(testBatchSubmissionId);
    }


    public static void pushLocalTestResults(Context context, ResearchTests.LoadTimeTestResults loadingTimeResults, ResearchTests.InferenceTimeTestResults inferenceTimeResults) {
        HashMap<String, Object> testResultMap = new HashMap<>();
        testResultMap.put("dateTimestamp", FieldValue.serverTimestamp());
        testResultMap.put("deviceModel", Build.MODEL);
        testResultMap.put("deviceInfo", MainActivity.getSystemInfoString(context));
        testResultMap.put("loadingTimeResultsCSV", loadingTimeResults.getLoadTimeResultsAsCSV());
        testResultMap.put("inferenceTimeResultsCSV", inferenceTimeResults.getInferenceTimeResultsAsCSV());
        testResultMap.put("loadingTimeTestsCSV", loadingTimeResults.getLoadTimeTestsAsCSV());
        testResultMap.put("inferenceTimeTestsCSV", inferenceTimeResults.getInferenceTimeTestsAsCSV());
        fsDatabase.collection("local_test_results").add(testResultMap).addOnSuccessListener(documentReference -> Log.i(TAG, "Local Test Results pushed"));
    }

}
