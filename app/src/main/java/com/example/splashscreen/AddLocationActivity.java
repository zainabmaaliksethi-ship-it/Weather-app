package com.example.splashscreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddLocationActivity extends AppCompatActivity {

    private EditText etSearch;
    private RecyclerView rvResults;
    private ProgressBar progressBar;
    private TextView tvNoResults, tvSectionLabel;
    private ImageButton btnBack;

    private WeatherApiService apiService;
    private SearchResultAdapter adapter;

    // Debounce search so we don't spam API on every keystroke
    private Handler searchHandler = new Handler();
    private Runnable searchRunnable;
    private static final int SEARCH_DELAY_MS = 600;

    // Curated list of popular global cities shown before user types
    private static final List<String> POPULAR_CITIES = Arrays.asList(
            "Islamabad",    "Lahore",       "Karachi",
            "Rawalpindi",   "Multan",       "Peshawar",
            "Quetta",       "Faisalabad",   "Sialkot",
            "Dubai",        "Abu Dhabi",    "Riyadh",
            "London",       "Manchester",   "Birmingham",
            "New York",     "Los Angeles",  "Chicago",
            "Toronto",      "Vancouver",    "Montreal",
            "Paris",        "Berlin",       "Madrid",
            "Rome",         "Amsterdam",    "Vienna",
            "Tokyo",        "Beijing",      "Shanghai",
            "Mumbai",       "Delhi",        "Bangalore",
            "Sydney",       "Melbourne",    "Brisbane",
            "Cairo",        "Istanbul",     "Tehran",
            "Nairobi",      "Lagos",        "Johannesburg",
            "Buenos Aires", "São Paulo",    "Mexico City"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_location);

        apiService = RetroFitClient.getInstance().getApiService();
        initViews();
        setupSearch();
        showPopularCities();
    }

    private void initViews() {
        etSearch       = findViewById(R.id.etSearch);
        rvResults      = findViewById(R.id.rvResults);
        progressBar    = findViewById(R.id.progressBar);
        tvNoResults    = findViewById(R.id.tvNoResults);
        tvSectionLabel = findViewById(R.id.tvSectionLabel);
        btnBack        = findViewById(R.id.btnBack);

        rvResults.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchResultAdapter(new ArrayList<>(), city -> {
            Intent result = new Intent();
            result.putExtra("selected_city", city);
            setResult(RESULT_OK, result);
            finish();
        });
        rvResults.setAdapter(adapter);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void afterTextChanged(Editable s) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();

                // Cancel pending search
                if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);

                if (query.isEmpty()) {
                    showPopularCities();
                    return;
                }

                // Debounced API call
                searchRunnable = () -> searchCityViaApi(query);
                searchHandler.postDelayed(searchRunnable, SEARCH_DELAY_MS);
            }
        });
    }

    /**
     * Uses the OWM /weather endpoint to validate any city worldwide.
     * If found, displays city + country. Works for ANY global location.
     */
    private void searchCityViaApi(String query) {
        progressBar.setVisibility(View.VISIBLE);
        tvNoResults.setVisibility(View.GONE);
        tvSectionLabel.setText("🔍 Search Results");

        apiService.getWeatherByCity(query, Constants.API_KEY, "metric")
                .enqueue(new Callback<WeatherResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<WeatherResponse> call,
                                           @NonNull Response<WeatherResponse> response) {
                        progressBar.setVisibility(View.GONE);

                        if (response.isSuccessful() && response.body() != null) {
                            WeatherResponse body = response.body();
                            String cityDisplay = body.getName();
                            if (body.getSys() != null && body.getSys().getCountry() != null) {
                                cityDisplay += ", " + body.getSys().getCountry();
                            }
                            List<String> result = new ArrayList<>();
                            result.add(cityDisplay);
                            adapter.updateData(result);
                            tvNoResults.setVisibility(View.GONE);
                        } else {
                            // Not found via exact match — try partial suggestions from popular list
                            List<String> filtered = new ArrayList<>();
                            for (String city : POPULAR_CITIES) {
                                if (city.toLowerCase().contains(query.toLowerCase())) {
                                    filtered.add(city);
                                }
                            }
                            if (!filtered.isEmpty()) {
                                adapter.updateData(filtered);
                                tvNoResults.setVisibility(View.GONE);
                            } else {
                                adapter.updateData(new ArrayList<>());
                                tvNoResults.setVisibility(View.VISIBLE);
                                tvNoResults.setText("No city found for \"" + query + "\" 🔍\nTry full city name, e.g. \"Islamabad\"");
                            }
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<WeatherResponse> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        Toast.makeText(AddLocationActivity.this,
                                "Network error 🌐 Check connection", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showPopularCities() {
        tvNoResults.setVisibility(View.GONE);
        tvSectionLabel.setText("🌟 Popular Cities");
        adapter.updateData(POPULAR_CITIES);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (searchRunnable != null) searchHandler.removeCallbacks(searchRunnable);
    }
}