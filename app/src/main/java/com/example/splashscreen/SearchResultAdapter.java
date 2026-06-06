package com.example.splashscreen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchResultAdapter extends RecyclerView.Adapter<SearchResultAdapter.SearchViewHolder> {

    public interface OnCitySelectedListener {
        void onCitySelected(String city);
    }

    private List<String> cities;
    private final OnCitySelectedListener listener;

    public SearchResultAdapter(List<String> cities, OnCitySelectedListener listener) {
        this.cities = cities;
        this.listener = listener;
    }

    public void updateData(List<String> newCities) {
        this.cities = newCities;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public SearchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_search_result, parent, false);
        return new SearchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchViewHolder holder, int position) {
        String city = cities.get(position);
        holder.tvCity.setText("🌍 " + city);
        holder.itemView.setOnClickListener(v -> listener.onCitySelected(city));
    }

    @Override
    public int getItemCount() {
        return cities != null ? cities.size() : 0;
    }

    static class SearchViewHolder extends RecyclerView.ViewHolder {
        TextView tvCity;

        SearchViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCity = itemView.findViewById(R.id.tvSearchCity);
        }
    }
}