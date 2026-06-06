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

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    // Now uses DailyForecast wrapper (carries real min/max, not per-slot min/max)
    private List<ForecastUtils.DailyForecast> items;
    private boolean isCelsius;

    public ForecastAdapter(List<ForecastUtils.DailyForecast> items, boolean isCelsius) {
        this.items = items;
        this.isCelsius = isCelsius;
    }

    public void updateData(List<ForecastUtils.DailyForecast> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void setIsCelsius(boolean isCelsius) {
        this.isCelsius = isCelsius;
    }

    @NonNull
    @Override
    public ForecastViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_forecast_day, parent, false);
        return new ForecastViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForecastViewHolder holder, int position) {
        ForecastUtils.DailyForecast daily = items.get(position);
        ForecastResponse.ForecastItem rep = daily.getRepresentative();

        // Day name
        SimpleDateFormat dayFmt = new SimpleDateFormat("EEEE", Locale.getDefault());
        holder.tvDay.setText(dayFmt.format(new Date(rep.getDt() * 1000L)));

        // Weather emoji + description
        if (rep.getWeather() != null && !rep.getWeather().isEmpty()) {
            String cond = rep.getWeather().get(0).getMain();
            String desc = rep.getWeather().get(0).getDescription();
            if (desc != null && !desc.isEmpty())
                desc = Character.toUpperCase(desc.charAt(0)) + desc.substring(1);
            holder.tvIcon.setText(WeatherUtils.getWeatherEmoji(cond));
            holder.tvDesc.setText(desc);
        }

        // ── Use REAL min/max scanned across ALL slots of that day ──
        double minC = daily.getRealMin();
        double maxC = daily.getRealMax();

        if (isCelsius) {
            holder.tvMinMax.setText(
                    String.format(Locale.getDefault(), "↓%.0f°C  /  ↑%.0f°C", minC, maxC));
        } else {
            holder.tvMinMax.setText(String.format(Locale.getDefault(),
                    "↓%.0f°F  /  ↑%.0f°F",
                    WeatherUtils.celsiusToFahrenheit(minC),
                    WeatherUtils.celsiusToFahrenheit(maxC)));
        }

        // Rain probability
        int pop = (int) (rep.getPop() * 100);
        if (pop > 0) {
            holder.tvPop.setVisibility(View.VISIBLE);
            holder.tvPop.setText("🌧️ " + pop + "% chance of rain");
        } else {
            holder.tvPop.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() { return items != null ? items.size() : 0; }

    static class ForecastViewHolder extends RecyclerView.ViewHolder {
        TextView tvDay, tvIcon, tvDesc, tvMinMax, tvPop;

        ForecastViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDay    = itemView.findViewById(R.id.tvForecastDay);
            tvIcon   = itemView.findViewById(R.id.tvForecastIcon);
            tvDesc   = itemView.findViewById(R.id.tvForecastDesc);
            tvMinMax = itemView.findViewById(R.id.tvForecastMinMax);
            tvPop    = itemView.findViewById(R.id.tvForecastPop);
        }
    }
}