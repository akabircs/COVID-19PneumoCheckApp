package com.touchmediaproductions.pneumocheck.models;

import com.google.firebase.Timestamp;

import java.util.HashMap;

public class Submission {

    private String dateOfScan;
    private String imageType;
    private String imageUrl;
    private String prediction;
    private String modelName;
    private HashMap<String, Float> probabilities;
    private String submissionDate;
    private String userId;
    private Timestamp learntAt;

    public String getDateOfScan() {
        return dateOfScan;
    }

    public String getImageType() {
        return imageType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPrediction() {
        return prediction;
    }

    public HashMap<String, Float> getProbabilities() {
        return probabilities;
    }

    public String getSubmissionDate() {
        return submissionDate;
    }

    public String getUserId() {
        return userId;
    }

    public String getModelName() {
        return modelName;
    }

    public Timestamp getLearntAt() { return learntAt; }

}
