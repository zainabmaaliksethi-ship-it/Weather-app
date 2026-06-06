package com.example.splashscreen;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface WeatherApiService {

    // Current weather by coordinates
    @GET("weather")
    Call<WeatherResponse> getWeatherByCoordinates(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    // Current weather by city name
    @GET("weather")
    Call<WeatherResponse> getWeatherByCity(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    // Hourly forecast by coordinates (3-hour intervals, 5-day)
    @GET("forecast")
    Call<ForecastResponse> getForecastByCoordinates(
            @Query("lat") double lat,
            @Query("lon") double lon,
            @Query("appid") String apiKey,
            @Query("units") String units
    );

    // Hourly forecast by city
    @GET("forecast")
    Call<ForecastResponse> getForecastByCity(
            @Query("q") String city,
            @Query("appid") String apiKey,
            @Query("units") String units
    );
}
