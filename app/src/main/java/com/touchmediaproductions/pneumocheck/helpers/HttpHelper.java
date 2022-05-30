package com.touchmediaproductions.pneumocheck.helpers;

import android.util.Log;

import java.io.IOException;

import okhttp3.OkHttpClient;

public class HttpHelper {

    // POST HTTP Request using HTTPClient DefaultHTTPCLient
    public static String post(String url, String json) throws IOException {
        OkHttpClient client = new OkHttpClient();
        // Content type
        String contentType = "application/json";
        // Create request body with json object
        okhttp3.RequestBody body = okhttp3.RequestBody.create(okhttp3.MediaType.parse(contentType), json);
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .post(body)
                .build();
        okhttp3.Response response = client.newCall(request).execute();
        if (response.body() == null) throw new AssertionError();
        String responseString = response.body().string();
        Log.d("HttpHelper", "post: " + responseString);
        return responseString;
    }

    // GET HTTP Request using HTTPClient DefaultHTTPCLient
    public static String get(String url) throws IOException {
        OkHttpClient client = new OkHttpClient();
        okhttp3.Request request = new okhttp3.Request.Builder()
                .url(url)
                .build();
        okhttp3.Response response = client.newCall(request).execute();
        if (response.body() == null) throw new AssertionError();
        String responseString = response.body().string();
        Log.d("HttpHelper", "get: " + responseString);
        return responseString;
    }
}
