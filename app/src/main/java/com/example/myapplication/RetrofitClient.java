package com.example.myapplication;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static Retrofit retrofit = null;
    // 10.0.2.2 is the IP address to access localhost from the emulator
    private static final String BASE_URL = "http://10.81.172.117:5000/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static String getAbsoluteUrl(String relativeOrAbsoluteUrl) {
        if (relativeOrAbsoluteUrl == null || relativeOrAbsoluteUrl.isEmpty()) {
            return "";
        }
        // If it's a relative path, prepend base URL
        if (!relativeOrAbsoluteUrl.startsWith("http")) {
            return BASE_URL + (relativeOrAbsoluteUrl.startsWith("/") ? relativeOrAbsoluteUrl.substring(1) : relativeOrAbsoluteUrl);
        }
        // If it contains 10.0.2.2 or localhost or 127.0.0.1, dynamically replace the host/port with BASE_URL host/port
        String baseHost = BASE_URL.replace("http://", "").replace("https://", "").split("/")[0];
        return relativeOrAbsoluteUrl.replaceAll("(?i)(10\\.0\\.2\\.2|localhost|127\\.0\\.0\\.1)(:\\d+)?", baseHost);
    }
}
