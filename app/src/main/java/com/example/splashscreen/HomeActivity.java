package com.example.splashscreen;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final String PREFS_NAME = "WeatherAppPrefs";
    private static final String KEY_UNIT   = "temp_unit";

    private FusedLocationProviderClient fusedLocationClient;
    private WeatherApiService apiService;
    private SharedPreferences prefs;

    // Live clock
    private Handler clockHandler;
    private Runnable clockRunnable;
    private int currentTimezoneOffset = 0;

    // UI
    private TextView tvGreeting, tvCityName, tvTemperature, tvCondition;
    private TextView tvHumidity, tvWindSpeed, tvFeelsLike, tvPressure;
    private TextView tvDate, tvUnitToggle, tvMinMax, tvLocalTime;
    private TextView tvLocationPrompt;
    private ProgressBar progressBar;
    private LinearLayout weatherCard, locationPromptLayout;
    private RecyclerView rvHourly, rvForecast, rvSavedLocations;
    private ImageButton btnAddLocation, btnRefresh, btnLogout;

    private HourlyAdapter hourlyAdapter;
    private ForecastAdapter forecastAdapter;
    private SavedLocationsAdapter savedLocationsAdapter;

    private boolean isCelsius = true;
    private double currentTempCelsius = 0;
    private double currentFeelsLike   = 0;
    private double currentTempMin     = 0;
    private double currentTempMax     = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_home);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        isCelsius = prefs.getBoolean(KEY_UNIT, true);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        apiService = RetroFitClient.getInstance().getApiService();

        initViews();
        setupClickListeners();
        setGreeting();
        loadSavedLocations();
        requestLocationPermission();
    }

    // ── INIT VIEWS ────────────────────────────────────────────────────────────

    private void initViews() {
        tvGreeting           = findViewById(R.id.tvGreeting);
        tvCityName           = findViewById(R.id.tvCityName);
        tvTemperature        = findViewById(R.id.tvTemperature);
        tvCondition          = findViewById(R.id.tvCondition);
        tvHumidity           = findViewById(R.id.tvHumidity);
        tvWindSpeed          = findViewById(R.id.tvWindSpeed);
        tvFeelsLike          = findViewById(R.id.tvFeelsLike);
        tvPressure           = findViewById(R.id.tvPressure);
        tvDate               = findViewById(R.id.tvDate);
        tvUnitToggle         = findViewById(R.id.tvUnitToggle);
        tvMinMax             = findViewById(R.id.tvMinMax);
        tvLocationPrompt     = findViewById(R.id.tvLocationPrompt);
        tvLocalTime          = findViewById(R.id.tvLocalTime);
        progressBar          = findViewById(R.id.progressBar);
        weatherCard          = findViewById(R.id.weatherCard);
        locationPromptLayout = findViewById(R.id.locationPromptLayout);
        rvHourly             = findViewById(R.id.rvHourly);
        rvForecast           = findViewById(R.id.rvForecast);
        rvSavedLocations     = findViewById(R.id.rvSavedLocations);
        btnAddLocation       = findViewById(R.id.btnAddLocation);
        btnRefresh           = findViewById(R.id.btnRefresh);
        btnLogout            = findViewById(R.id.btnLogout);

        // Hourly — horizontal scroll
        rvHourly.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        hourlyAdapter = new HourlyAdapter(new ArrayList<>(), isCelsius);
        rvHourly.setAdapter(hourlyAdapter);

        // 3-Day forecast — vertical, no nested scroll
        rvForecast.setLayoutManager(new LinearLayoutManager(this));
        rvForecast.setNestedScrollingEnabled(false);
        forecastAdapter = new ForecastAdapter(new ArrayList<>(), isCelsius);
        rvForecast.setAdapter(forecastAdapter);

        // Saved Locations — with delete callback
        rvSavedLocations.setLayoutManager(new LinearLayoutManager(this));
        rvSavedLocations.setNestedScrollingEnabled(false);
        savedLocationsAdapter = new SavedLocationsAdapter(
                new ArrayList<>(),
                this::onLocationSelected,   // click
                this::onLocationDeleted     // delete
        );
        rvSavedLocations.setAdapter(savedLocationsAdapter);

        updateUnitToggleText();

        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMM dd yyyy", Locale.getDefault());
        tvDate.setText(sdf.format(new Date()));
    }

    // ── CLICK LISTENERS ───────────────────────────────────────────────────────

    private void setupClickListeners() {
        tvUnitToggle.setOnClickListener(v -> {
            isCelsius = !isCelsius;
            prefs.edit().putBoolean(KEY_UNIT, isCelsius).apply();
            updateUnitToggleText();
            updateTemperatureDisplay();
            hourlyAdapter.setIsCelsius(isCelsius);
            hourlyAdapter.notifyDataSetChanged();
            forecastAdapter.setIsCelsius(isCelsius);
            forecastAdapter.notifyDataSetChanged();
            animateButtonPress(v);
        });

        btnAddLocation.setOnClickListener(v -> {
            animateButtonPress(v);
            startActivityForResult(new Intent(this, AddLocationActivity.class), 100);
        });

        btnRefresh.setOnClickListener(v -> {
            animateButtonPress(v);
            requestLocationPermission();
        });

        btnLogout.setOnClickListener(v -> {
            stopClock();
            FirebaseAuth.getInstance().signOut();
            startActivity(new Intent(this, MainActivity2.class));
            finish();
        });
    }

    // ── GREETING ──────────────────────────────────────────────────────────────

    private void setGreeting() {
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String name = (user != null && user.getDisplayName() != null
                && !user.getDisplayName().isEmpty()) ? user.getDisplayName() : "";

        String g;
        if      (hour >= 5  && hour < 12) g = "Good Morning ☀️";
        else if (hour >= 12 && hour < 17) g = "Good Afternoon 🌤️";
        else if (hour >= 17 && hour < 21) g = "Good Evening 🌇";
        else                              g = "Good Night 🌙";

        tvGreeting.setText(g + (name.isEmpty() ? " 👋" : ", " + name + " 👋"));
    }

    // ── LOCATION ──────────────────────────────────────────────────────────────

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            getCurrentLocation();
        }
    }

    private void getCurrentLocation() {
        showLoading(true);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) return;

        fusedLocationClient.getLastLocation().addOnSuccessListener(this, loc -> {
            if (loc != null) {
                fetchWeatherByCoordinates(loc.getLatitude(), loc.getLongitude());
            } else {
                showLoading(false);
                showLocationPrompt();
                Toast.makeText(this, "Cannot get location. Tap refresh 📍", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> { showLoading(false); showLocationPrompt(); });
    }

    // ── FETCH BY COORDS ───────────────────────────────────────────────────────

    private void fetchWeatherByCoordinates(double lat, double lon) {
        apiService.getWeatherByCoordinates(lat, lon, Constants.API_KEY, "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call,
                                           @NonNull Response<WeatherResponse> r) {
                        showLoading(false);
                        if (r.isSuccessful() && r.body() != null) updateWeatherUI(r.body());
                        else Toast.makeText(HomeActivity.this, "Weather fetch failed 😕", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                        showLoading(false);
                        Toast.makeText(HomeActivity.this, "Network error 🌐", Toast.LENGTH_SHORT).show();
                    }
                });

        apiService.getForecastByCoordinates(lat, lon, Constants.API_KEY, "metric")
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ForecastResponse> call,
                                           @NonNull Response<ForecastResponse> r) {
                        if (r.isSuccessful() && r.body() != null) processForecast(r.body());
                    }

                    @Override public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {}
                });
    }

    // ── FETCH BY CITY ─────────────────────────────────────────────────────────

    public void fetchWeatherByCity(String cityName) {
        showLoading(true);
        apiService.getWeatherByCity(cityName, Constants.API_KEY, "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call,
                                           @NonNull Response<WeatherResponse> r) {
                        showLoading(false);
                        if (r.isSuccessful() && r.body() != null) updateWeatherUI(r.body());
                        else Toast.makeText(HomeActivity.this, "City not found 🔍", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                        showLoading(false);
                        Toast.makeText(HomeActivity.this, "Network error 🌐", Toast.LENGTH_SHORT).show();
                    }
                });

        apiService.getForecastByCity(cityName, Constants.API_KEY, "metric")
                .enqueue(new Callback<ForecastResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<ForecastResponse> call,
                                           @NonNull Response<ForecastResponse> r) {
                        if (r.isSuccessful() && r.body() != null) processForecast(r.body());
                    }

                    @Override public void onFailure(@NonNull Call<ForecastResponse> call, @NonNull Throwable t) {}
                });
    }

    // ── FORECAST PROCESSING ───────────────────────────────────────────────────

    private void processForecast(ForecastResponse forecast) {
        List<ForecastResponse.ForecastItem> all = forecast.getList();
        if (all == null || all.isEmpty()) return;

        // ── Hourly: next 8 slots from NOW (fixed sequential order) ──
        List<ForecastResponse.ForecastItem> hourly = ForecastUtils.extractHourlyForecast(all);
        hourlyAdapter.updateData(hourly);

        // ── 3-Day: real min/max scanned across entire day ──
        List<ForecastUtils.DailyForecast> daily = ForecastUtils.extractDailyForecasts(all);
        forecastAdapter.updateData(daily);
    }

    // ── WEATHER UI UPDATE ─────────────────────────────────────────────────────

    private void updateWeatherUI(WeatherResponse weather) {
        weatherCard.setVisibility(View.VISIBLE);
        locationPromptLayout.setVisibility(View.GONE);

        currentTempCelsius = weather.getMain().getTemp();
        currentFeelsLike   = weather.getMain().getFeelsLike();
        currentTempMin     = weather.getMain().getTempMin();
        currentTempMax     = weather.getMain().getTempMax();

        String country = (weather.getSys() != null && weather.getSys().getCountry() != null)
                ? ", " + weather.getSys().getCountry() : "";
        tvCityName.setText(weather.getName() + country + " 📍");

        if (weather.getWeather() != null && !weather.getWeather().isEmpty()) {
            String cond = weather.getWeather().get(0).getMain();
            String desc = weather.getWeather().get(0).getDescription();
            if (desc != null && !desc.isEmpty())
                desc = Character.toUpperCase(desc.charAt(0)) + desc.substring(1);
            tvCondition.setText(WeatherUtils.getWeatherEmoji(cond) + " " + desc);
        }

        tvHumidity.setText("💧 " + weather.getMain().getHumidity() + "%");
        tvWindSpeed.setText("🌬️ " +
                String.format(Locale.getDefault(), "%.1f km/h", weather.getWind().getSpeed() * 3.6));
        tvPressure.setText("🔵 " + weather.getMain().getPressure() + " hPa");

        currentTimezoneOffset = weather.getTimezone();
        startClock(currentTimezoneOffset);
        updateTemperatureDisplay();

        AlphaAnimation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(600);
        weatherCard.startAnimation(fadeIn);
    }

    // ── LIVE CLOCK ────────────────────────────────────────────────────────────

    private void startClock(int timezoneOffsetSeconds) {
        stopClock();
        clockHandler = new Handler();
        clockRunnable = new Runnable() {
            @Override
            public void run() {
                long localMs = System.currentTimeMillis() + (timezoneOffsetSeconds * 1000L);
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss a", Locale.getDefault());
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                if (tvLocalTime != null) tvLocalTime.setText("🕐 " + sdf.format(new Date(localMs)));
                clockHandler.postDelayed(this, 1000);
            }
        };
        clockHandler.post(clockRunnable);
    }

    private void stopClock() {
        if (clockHandler != null && clockRunnable != null)
            clockHandler.removeCallbacks(clockRunnable);
    }

    // ── TEMPERATURE DISPLAY ───────────────────────────────────────────────────

    private void updateTemperatureDisplay() {
        if (isCelsius) {
            tvTemperature.setText(String.format(Locale.getDefault(), "%.0f°C", currentTempCelsius));
            tvFeelsLike.setText("🌡️ Feels like " +
                    String.format(Locale.getDefault(), "%.0f°C", currentFeelsLike));
            tvMinMax.setText(String.format(Locale.getDefault(),
                    "↓%.0f°  /  ↑%.0f°", currentTempMin, currentTempMax));
        } else {
            tvTemperature.setText(String.format(Locale.getDefault(), "%.0f°F",
                    WeatherUtils.celsiusToFahrenheit(currentTempCelsius)));
            tvFeelsLike.setText("🌡️ Feels like " +
                    String.format(Locale.getDefault(), "%.0f°F",
                            WeatherUtils.celsiusToFahrenheit(currentFeelsLike)));
            tvMinMax.setText(String.format(Locale.getDefault(), "↓%.0f°  /  ↑%.0f°",
                    WeatherUtils.celsiusToFahrenheit(currentTempMin),
                    WeatherUtils.celsiusToFahrenheit(currentTempMax)));
        }
    }

    private void updateUnitToggleText() {
        tvUnitToggle.setText("°C | °F");
        tvUnitToggle.setBackgroundResource(
                isCelsius ? R.drawable.unit_toggle_celsius : R.drawable.unit_toggle_fahrenheit);
    }

    // ── DELETE LOCATION ───────────────────────────────────────────────────────

    /**
     * Called when user taps 🗑️ on a saved location card.
     * Removes from SharedPreferences and immediately updates the RecyclerView
     * with a smooth item-removed animation.
     */
    private void onLocationDeleted(String city, int position) {
        LocationStorage.removeLocation(prefs, city);        // remove from SharedPreferences
        savedLocationsAdapter.removeItem(position);         // animate removal in RecyclerView
        Toast.makeText(this, "Removed: " + city + " 🗑️", Toast.LENGTH_SHORT).show();
    }

    // ── SAVED LOCATIONS ───────────────────────────────────────────────────────

    private void loadSavedLocations() {
        savedLocationsAdapter.updateData(LocationStorage.getSavedLocations(prefs));
    }

    private void onLocationSelected(String city) {
        fetchWeatherByCity(city);
    }

    // ── UTILS ─────────────────────────────────────────────────────────────────

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        if (show) weatherCard.setVisibility(View.GONE);
    }

    private void showLocationPrompt() {
        locationPromptLayout.setVisibility(View.VISIBLE);
        weatherCard.setVisibility(View.GONE);
    }

    private void animateButtonPress(View v) {
        AlphaAnimation anim = new AlphaAnimation(1f, 0.5f);
        anim.setDuration(100);
        anim.setRepeatMode(Animation.REVERSE);
        anim.setRepeatCount(1);
        v.startAnimation(anim);
    }

    // ── LIFECYCLE ─────────────────────────────────────────────────────────────

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                getCurrentLocation();
            else {
                showLocationPrompt();
                Toast.makeText(this, "Location denied — add a city manually 🌍", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100 && resultCode == RESULT_OK && data != null) {
            String city = data.getStringExtra("selected_city");
            if (city != null && !city.isEmpty()) {
                LocationStorage.saveLocation(prefs, city);
                loadSavedLocations();
                fetchWeatherByCity(city);
            }
        }
    }

    @Override protected void onPause()   { super.onPause();   stopClock(); }
    @Override protected void onResume()  { super.onResume();  if (currentTimezoneOffset != 0) startClock(currentTimezoneOffset); }
    @Override protected void onDestroy() { super.onDestroy(); stopClock(); }
}