package com.example.splashscreen;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ForecastUtils {

    /**
     * ── 3-DAY FORECAST FIX ──────────────────────────────────────────────────
     *
     * ROOT CAUSE of wrong min/max:
     *   The old code picked ONE slot per day (the noon slot) and read its
     *   tempMin/tempMax. OWM's 3-hour forecast slots report min/max only for
     *   that 3-hour window, NOT the full day. So noon alone gives ~20°/20°.
     *
     * FIX:
     *   Group ALL slots that belong to a given calendar day, then iterate
     *   every slot's `temp` (the actual measured temperature at that hour)
     *   and track the running minimum and maximum across the whole day.
     *   This produces real daytime highs and overnight lows.
     *
     *   We still keep one "representative" slot per day (closest to noon)
     *   for the weather icon + description, but override its min/max with
     *   the values computed from the full day.
     */
    public static List<DailyForecast> extractDailyForecasts(
            List<ForecastResponse.ForecastItem> allSlots) {

        // ── Step 1: sort by timestamp (should already be sorted, but be safe) ──
        List<ForecastResponse.ForecastItem> sorted = new ArrayList<>(allSlots);
        Collections.sort(sorted, (a, b) -> Long.compare(a.getDt(), b.getDt()));

        // ── Step 2: group by date key "yyyy-MM-dd" ──
        Map<String, List<ForecastResponse.ForecastItem>> grouped = new LinkedHashMap<>();
        SimpleDateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (ForecastResponse.ForecastItem item : sorted) {
            String key = dateFmt.format(new Date(item.getDt() * 1000L));
            if (!grouped.containsKey(key)) grouped.put(key, new ArrayList<>());
            grouped.get(key).add(item);
        }

        // ── Step 3: skip today, process next 3 days ──
        String todayKey = dateFmt.format(new Date());
        List<DailyForecast> result = new ArrayList<>();

        for (Map.Entry<String, List<ForecastResponse.ForecastItem>> entry : grouped.entrySet()) {
            if (entry.getKey().equals(todayKey)) continue;
            if (result.size() >= 3) break;

            List<ForecastResponse.ForecastItem> daySlots = entry.getValue();

            // ── Compute real min/max by scanning every slot's actual temp ──
            double realMin = Double.MAX_VALUE;
            double realMax = Double.MIN_VALUE;
            for (ForecastResponse.ForecastItem slot : daySlots) {
                double t = slot.getMain().getTemp();
                if (t < realMin) realMin = t;
                if (t > realMax) realMax = t;
            }

            // ── Pick noon slot for icon/description ──
            ForecastResponse.ForecastItem representative = pickNoonSlot(daySlots);

            result.add(new DailyForecast(representative, realMin, realMax));
        }

        return result;
    }

    /** Returns the slot whose hour component is closest to 12 (noon). */
    private static ForecastResponse.ForecastItem pickNoonSlot(
            List<ForecastResponse.ForecastItem> daySlots) {
        ForecastResponse.ForecastItem best = daySlots.get(0);
        long bestDiff = Long.MAX_VALUE;
        for (ForecastResponse.ForecastItem item : daySlots) {
            String txt = item.getDtTxt();
            if (txt != null && txt.length() >= 13) {
                try {
                    int hour = Integer.parseInt(txt.substring(11, 13));
                    long diff = Math.abs(hour - 12);
                    if (diff < bestDiff) { bestDiff = diff; best = item; }
                } catch (NumberFormatException ignored) {}
            }
        }
        return best;
    }

    // ────────────────────────────────────────────────────────────────────────
    /**
     * ── HOURLY FORECAST FIX ──────────────────────────────────────────────────
     *
     * ROOT CAUSE of non-sequential hours:
     *   The old code just took `allSlots.subList(0, 8)` which starts from
     *   midnight of the API's first returned date — not from the CURRENT hour.
     *   If it's 2 PM, slot[0] could be 12 AM (14 hours ago).
     *
     * FIX:
     *   Find the first slot whose timestamp is >= now, then take the next 8
     *   consecutive entries from that position. This guarantees:
     *   - Slots start from the upcoming hour
     *   - Strictly chronological order (already sorted by OWM)
     *   - No past or skipped hours
     */
    public static List<ForecastResponse.ForecastItem> extractHourlyForecast(
            List<ForecastResponse.ForecastItem> allSlots) {

        long nowSeconds = System.currentTimeMillis() / 1000L;
        int startIndex = 0;

        for (int i = 0; i < allSlots.size(); i++) {
            if (allSlots.get(i).getDt() >= nowSeconds) {
                startIndex = i;
                break;
            }
        }

        int endIndex = Math.min(startIndex + 8, allSlots.size());
        return new ArrayList<>(allSlots.subList(startIndex, endIndex));
    }

    // ────────────────────────────────────────────────────────────────────────
    /**
     * Wrapper that carries the representative slot (for icon/desc/day-name)
     * together with the correctly computed full-day min and max.
     */
    public static class DailyForecast {
        private final ForecastResponse.ForecastItem representative;
        private final double realMin;
        private final double realMax;

        public DailyForecast(ForecastResponse.ForecastItem representative,
                             double realMin, double realMax) {
            this.representative = representative;
            this.realMin = realMin;
            this.realMax = realMax;
        }

        public ForecastResponse.ForecastItem getRepresentative() { return representative; }
        public double getRealMin() { return realMin; }
        public double getRealMax() { return realMax; }
    }
}