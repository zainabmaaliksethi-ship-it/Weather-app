package com.example.splashscreen;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetroFitClient {

    private static final String BASE_URL = "https://api.openweathermap.org/data/2.5/";
    private static RetroFitClient instance;
    private final WeatherApiService apiService;

    private RetroFitClient() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(WeatherApiService.class);
    }

    public static synchronized RetroFitClient getInstance() {
        if (instance == null) {
            instance = new RetroFitClient();
        }
        return instance;
    }

    public WeatherApiService getApiService() {
        return apiService;
    }
}
