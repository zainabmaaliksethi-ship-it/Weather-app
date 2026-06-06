package com.example.splashscreen;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class WeatherResponse {

    @SerializedName("name")
    private String name;

    @SerializedName("main")
    private Main main;

    @SerializedName("weather")
    private List<Weather> weather;

    @SerializedName("wind")
    private Wind wind;

    @SerializedName("sys")
    private Sys sys;

    /**
     * CRITICAL FIX: 'timezone' is the shift in seconds from UTC for the city.
     * e.g. Islamabad = 18000 (UTC+5), London = 3600 (UTC+1), New York = -14400 (UTC-4)
     * This field was MISSING in the previous version — it's essential for local time.
     */
    @SerializedName("timezone")
    private int timezone;

    @SerializedName("visibility")
    private int visibility;

    public String getName()      { return name; }
    public Main getMain()        { return main; }
    public List<Weather> getWeather() { return weather; }
    public Wind getWind()        { return wind; }
    public Sys getSys()          { return sys; }
    public int getTimezone()     { return timezone; }
    public int getVisibility()   { return visibility; }

    // ── Main (temperature, humidity, pressure) ────────────────────────────────
    public static class Main {
        /**
         * All temperatures are in °C when units=metric is sent to API.
         * Previous bug: the code was doing extra conversions on values
         * that were ALREADY in Celsius — that caused wrong readings.
         */
        @SerializedName("temp")
        private double temp;

        @SerializedName("feels_like")
        private double feelsLike;

        @SerializedName("temp_min")
        private double tempMin;

        @SerializedName("temp_max")
        private double tempMax;

        /** Humidity is a plain integer percentage (0-100). No conversion needed. */
        @SerializedName("humidity")
        private int humidity;

        /** Pressure in hPa (hectopascals). No conversion needed. */
        @SerializedName("pressure")
        private int pressure;

        public double getTemp()      { return temp; }
        public double getFeelsLike() { return feelsLike; }
        public double getTempMin()   { return tempMin; }
        public double getTempMax()   { return tempMax; }
        public int getHumidity()     { return humidity; }
        public int getPressure()     { return pressure; }
    }

    // ── Weather condition ─────────────────────────────────────────────────────
    public static class Weather {
        @SerializedName("main")
        private String main;        // e.g. "Clear", "Clouds", "Rain"

        @SerializedName("description")
        private String description; // e.g. "clear sky", "scattered clouds"

        @SerializedName("icon")
        private String icon;

        public String getMain()        { return main; }
        public String getDescription() { return description; }
        public String getIcon()        { return icon; }
    }

    // ── Wind ──────────────────────────────────────────────────────────────────
    public static class Wind {
        /**
         * Wind speed from API with units=metric is in METRES PER SECOND (m/s).
         * Convert to km/h by multiplying by 3.6.
         * Previous bug: value was displayed as-is (m/s) which looked very low.
         */
        @SerializedName("speed")
        private double speed; // m/s

        @SerializedName("deg")
        private int deg;

        public double getSpeed() { return speed; }
        public int getDeg()      { return deg; }
    }

    // ── Sys (country, sunrise, sunset) ────────────────────────────────────────
    public static class Sys {
        @SerializedName("country")
        private String country;

        @SerializedName("sunrise")
        private long sunrise;

        @SerializedName("sunset")
        private long sunset;

        public String getCountry() { return country; }
        public long getSunrise()   { return sunrise; }
        public long getSunset()    { return sunset; }
    }
}