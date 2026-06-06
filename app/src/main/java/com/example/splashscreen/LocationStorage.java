package com.example.splashscreen;

import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class LocationStorage {

    private static final String KEY_LOCATIONS = "saved_locations";
    private static final String SEPARATOR = "||";
    private static final int MAX_LOCATIONS = 10;

    public static void saveLocation(SharedPreferences prefs, String city) {
        List<String> locations = getSavedLocations(prefs);
        // Remove if already exists (move to top)
        locations.remove(city);
        locations.add(0, city);
        // Keep max 10
        if (locations.size() > MAX_LOCATIONS) {
            locations = locations.subList(0, MAX_LOCATIONS);
        }
        String joined = String.join(SEPARATOR, locations);
        prefs.edit().putString(KEY_LOCATIONS, joined).apply();
    }

    public static List<String> getSavedLocations(SharedPreferences prefs) {
        String saved = prefs.getString(KEY_LOCATIONS, "");
        if (saved.isEmpty()) return new ArrayList<>();
        return new ArrayList<>(Arrays.asList(saved.split("\\|\\|")));
    }

    public static void removeLocation(SharedPreferences prefs, String city) {
        List<String> locations = getSavedLocations(prefs);
        locations.remove(city);
        String joined = String.join(SEPARATOR, locations);
        prefs.edit().putString(KEY_LOCATIONS, joined).apply();
    }

    public static void clearAll(SharedPreferences prefs) {
        prefs.edit().remove(KEY_LOCATIONS).apply();
    }
}