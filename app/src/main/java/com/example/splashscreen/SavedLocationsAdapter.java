package com.example.splashscreen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SavedLocationsAdapter extends RecyclerView.Adapter<SavedLocationsAdapter.LocationViewHolder> {

    public interface OnLocationClickListener {
        void onLocationClick(String city);
    }

    public interface OnLocationDeleteListener {
        void onLocationDelete(String city, int position);
    }

    private List<String> locations;
    private final OnLocationClickListener clickListener;
    private final OnLocationDeleteListener deleteListener;

    public SavedLocationsAdapter(List<String> locations,
                                 OnLocationClickListener clickListener,
                                 OnLocationDeleteListener deleteListener) {
        this.locations = locations;
        this.clickListener = clickListener;
        this.deleteListener = deleteListener;
    }

    public void updateData(List<String> newLocations) {
        this.locations = newLocations;
        notifyDataSetChanged();
    }

    /** Removes item at position and animates it out. */
    public void removeItem(int position) {
        if (position >= 0 && position < locations.size()) {
            locations.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, locations.size());
        }
    }

    @NonNull
    @Override
    public LocationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_saved_location, parent, false);
        return new LocationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull LocationViewHolder holder, int position) {
        String city = locations.get(position);
        holder.tvCity.setText("📌 " + city);

        holder.itemView.setOnClickListener(v -> clickListener.onLocationClick(city));

        holder.btnDelete.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_ID) {
                deleteListener.onLocationDelete(city, pos);
            }
        });
    }

    @Override
    public int getItemCount() { return locations != null ? locations.size() : 0; }

    static class LocationViewHolder extends RecyclerView.ViewHolder {
        TextView tvCity;
        ImageButton btnDelete;

        LocationViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCity    = itemView.findViewById(R.id.tvSavedCity);
            btnDelete = itemView.findViewById(R.id.btnDeleteLocation);
        }
    }
}