package com.touchmediaproductions.pneumocheck.helpers;

import android.util.Log;

import java.io.IOException;

public class CloudMLXrayContinualServerClient {

    final static String TAG = "CloudMLXrayContinualServerClient";
    static String DOMAIN = "http://192.168.1.134:8888";

    public static void setDOMAIN(String domain){
        DOMAIN = domain;
    }

    public static void ping(){
        new Thread(() -> {
        try {
            String response = HttpHelper.get(DOMAIN + "/ping");
            Log.d(TAG, "ping: " + response);
        } catch (IOException e) {
            e.printStackTrace();
        }
        }).start();
    }

    public static void classify(String submissionId){
        String url = DOMAIN + "/classify";

//        Log.d(TAG, "classify: " + url);
        new Thread(() -> {
            try {
                String response = HttpHelper.post(url, "{\"submission_id\":\"" + submissionId + "\"}");
                Log.d(TAG, "classify: " + response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public static void train(String submissionId, String diagnosis){
        String url = DOMAIN + "/train";

//        Log.d(TAG, "train: " + url);
        new Thread(() -> {
            try {
                String response = HttpHelper.post(url, "{\"submission_id\":\"" + submissionId + "\", \"diagnosis\":\"" + diagnosis + "\"}");
                Log.d(TAG, "learn: " + response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * For testing purposes can provide one image to train on to the server
     * @param submissionId
     * @param diagnosis
     * @param imageUrl
     */
    public static void trainOnSpecificImageUrl(String submissionId, String diagnosis, String imageUrl){
        String url = DOMAIN + "/trainOnUrl";

//        Log.d(TAG, "train: " + url);
        new Thread(() -> {
            try {
                String response = HttpHelper.post(url, "{\"submission_id\":\"" + submissionId + "\", \"diagnosis\":\"" + diagnosis + "\", \"image_url\":\"" + imageUrl + "\"}");
                Log.d(TAG, "learn: " + response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    /**
     * For testing purposes can provide one image to classify on to the server
     */
    public static void classifySpecificImageUrl(String submissionId, String diagnosis, String imageUrl, boolean isLast){
        String url = DOMAIN + "/classifyOnUrl";

        Log.d(TAG, "classify_batch_test: /classifyOnUrl");
        new Thread(() -> {
            try {
                String response = HttpHelper.post(url, "{\"submission_id\":\"" + submissionId + "\", \"diagnosis\":\"" + diagnosis + "\", \"image_url\":\"" + imageUrl + "\", \"is_last\":" + isLast + "}");
                Log.d(TAG, "classify_batch_test: " + response);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }


}
