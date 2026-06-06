package com.example.splashscreen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HourlyAdapter extends RecyclerView.Adapter<HourlyAdapter.HourlyViewHolder> {

    private List<ForecastResponse.ForecastItem> items;
    private boolean isCelsius;

    public HourlyAdapter(List<ForecastResponse.ForecastItem> items, boolean isCelsius) {
        this.items = items;
        this.isCelsius = isCelsius;
    }

    public void updateData(List<ForecastResponse.ForecastItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setIsCelsius(boolean isCelsius) {
        this.isCelsius = isCelsius;
    }

    @NonNull
    @Override
    public HourlyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_hourly, parent, false);
        return new HourlyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HourlyViewHolder holder, int position) {
        ForecastResponse.ForecastItem item = items.get(position);

        // Time — "h a" gives "12 AM", "3 PM", etc.
        long ts = item.getDt() * 1000L;
        SimpleDateFormat sdf = new SimpleDateFormat("h a", Locale.getDefault());
        holder.tvTime.setText(sdf.format(new Date(ts)));

        // Temperature — already °C from API (units=metric)
        double tempC = item.getMain().getTemp();
        if (isCelsius) {
            holder.tvTemp.setText(String.format(Locale.getDefault(), "%.0f°C", tempC));
        } else {
            holder.tvTemp.setText(String.format(Locale.getDefault(),
                    "%.0f°F", WeatherUtils.celsiusToFahrenheit(tempC)));
        }

        // Weather emoji
        if (item.getWeather() != null && !item.getWeather().isEmpty()) {
            holder.tvIcon.setText(WeatherUtils.getWeatherEmoji(item.getWeather().get(0).getMain()));
        }

        // Rain probability
        int pop = (int) (item.getPop() * 100);
        if (pop > 0) {
            holder.tvPop.setVisibility(View.VISIBLE);
            holder.tvPop.setText("💧 " + pop + "%");
        } else {
            holder.tvPop.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    public int getItemCount() { return items != null ? items.size() : 0; }

    static class HourlyViewHolder extends RecyclerView.ViewHolder {
        TextView tvTime, tvTemp, tvIcon, tvPop;

        HourlyViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTime = itemView.findViewById(R.id.tvHourTime);
            tvTemp = itemView.findViewById(R.id.tvHourTemp);
            tvIcon = itemView.findViewById(R.id.tvHourIcon);
            tvPop  = itemView.findViewById(R.id.tvHourPop);
        }
    }
}