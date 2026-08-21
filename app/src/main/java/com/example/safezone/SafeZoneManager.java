package com.example.safezone;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import androidx.annotation.NonNull;

public class SafeZoneManager {
    private static final String PREF = "safezone_prefs";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_RADIUS = "radius";
    private static final double DEFAULT_LAT = 0.0;
    private static final double DEFAULT_LON = 0.0;
    private static final int DEFAULT_RADIUS = 500; // meters

    private static SafeZoneManager instance;
    private final SharedPreferences prefs;
    private final Context ctx;
    private final WhitelistManager whitelist;

    private SafeZoneManager(Context context) {
        this.ctx = context.getApplicationContext();
        this.prefs = ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        this.whitelist = WhitelistManager.getInstance(ctx);
    }

    public static SafeZoneManager getInstance(@NonNull Context ctx) {
        if (instance == null) instance = new SafeZoneManager(ctx);
        return instance;
    }

    public void setSafeZone(double lat, double lon, int radiusMeters) {
        prefs.edit().putLong(KEY_LAT, Double.doubleToRawLongBits(lat))
            .putLong(KEY_LON, Double.doubleToRawLongBits(lon))
            .putInt(KEY_RADIUS, radiusMeters)
            .apply();
    }

    public double getLat() {
        return Double.longBitsToDouble(prefs.getLong(KEY_LAT, Double.doubleToRawLongBits(DEFAULT_LAT)));
    }

    public double getLon() {
        return Double.longBitsToDouble(prefs.getLong(KEY_LON, Double.doubleToRawLongBits(DEFAULT_LON)));
    }

    public int getRadiusMeters() {
        return prefs.getInt(KEY_RADIUS, DEFAULT_RADIUS);
    }

    public boolean isInsideSafeZone(double lat, double lon) {
        float d = DistanceUtils.distanceMeters(getLat(), getLon(), lat, lon);
        return d <= getRadiusMeters();
    }

    public void onLocationUpdate(Location loc) {
        boolean inside = isInsideSafeZone(loc.getLatitude(), loc.getLongitude());
        prefs.edit().putBoolean("last_inside", inside).apply();
    }

    public boolean shouldAllowNumber(String normalizedNumber) {
        if (whitelist.isWhitelisted(normalizedNumber)) return true;
        boolean lastInside = prefs.getBoolean("last_inside", true);
        return lastInside;
    }
}
