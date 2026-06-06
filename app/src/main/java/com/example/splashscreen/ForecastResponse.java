package com.example.splashscreen;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ForecastResponse {

    @SerializedName("list")
    private List<ForecastItem> list;

    @SerializedName("city")
    private City city;

    public List<ForecastItem> getList() { return list; }
    public City getCity()               { return city; }

    // ── City info (includes timezone offset) ──────────────────────────────────
    public static class City {
        @SerializedName("name")
        private String name;

        @SerializedName("country")
        private String country;

        /** Timezone offset in seconds from UTC — same field as WeatherResponse */
        @SerializedName("timezone")
        private int timezone;

        public String getName()    { return name; }
        public String getCountry() { return country; }
        public int getTimezone()   { return timezone; }
    }

    // ── Single forecast slot (3-hour interval) ────────────────────────────────
    public static class ForecastItem {

        /** Unix timestamp of this forecast slot */
        @SerializedName("dt")
        private long dt;

        @SerializedName("main")
        private Main main;

        @SerializedName("weather")
        private List<Weather> weather;

        @SerializedName("wind")
        private Wind wind;

        /** Probability of precipitation (0.0 – 1.0) */
        @SerializedName("pop")
        private double pop;

        /** Human-readable date-time string, e.g. "2024-07-15 12:00:00" */
        @SerializedName("dt_txt")
        private String dtTxt;

        public long getDt()              { return dt; }
        public Main getMain()            { return main; }
        public List<Weather> getWeather(){ return weather; }
        public Wind getWind()            { return wind; }
        public double getPop()           { return pop; }
        public String getDtTxt()         { return dtTxt; }

        // ── Main ─────────────────────────────────────────────────────────────
        public static class Main {
            @SerializedName("temp")
            private double temp;

            @SerializedName("temp_min")
            private double tempMin;

            @SerializedName("temp_max")
            private double tempMax;

            @SerializedName("humidity")
            private int humidity;

            @SerializedName("pressure")
            private int pressure;

            public double getTemp()    { return temp; }
            public double getTempMin() { return tempMin; }
            public double getTempMax() { return tempMax; }
            public int getHumidity()   { return humidity; }
            public int getPressure()   { return pressure; }
        }

        // ── Weather condition ─────────────────────────────────────────────────
        public static class Weather {
            @SerializedName("main")
            private String main;

            @SerializedName("description")
            private String description;

            @SerializedName("icon")
            private String icon;

            public String getMain()        { return main; }
            public String getDescription() { return description; }
            public String getIcon()        { return icon; }
        }

        // ── Wind ─────────────────────────────────────────────────────────────
        public static class Wind {
            @SerializedName("speed")
            private double speed; // m/s

            public double getSpeed() { return speed; }
        }
    }
}