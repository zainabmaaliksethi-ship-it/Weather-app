package com.example.splashscreen;

public class WeatherUtils {

    public static String getWeatherEmoji(String condition) {
        if (condition == null) return "🌤️";
        switch (condition.toLowerCase()) {
            case "clear":       return "☀️";
            case "clouds":      return "☁️";
            case "rain":        return "🌧️";
            case "drizzle":     return "🌦️";
            case "thunderstorm":return "⛈️";
            case "snow":        return "❄️";
            case "mist":
            case "fog":
            case "haze":        return "🌫️";
            case "dust":
            case "sand":        return "🌪️";
            case "tornado":     return "🌪️";
            default:            return "🌤️";
        }
    }

    public static double celsiusToFahrenheit(double celsius) {
        return (celsius * 9.0 / 5.0) + 32.0;
    }

    public static double fahrenheitToCelsius(double fahrenheit) {
        return (fahrenheit - 32.0) * 5.0 / 9.0;
    }

    public static String getWindDirection(int degrees) {
        String[] directions = {"N", "NE", "E", "SE", "S", "SW", "W", "NW"};
        int index = (int) Math.round(degrees / 45.0) % 8;
        return directions[index];
    }
}