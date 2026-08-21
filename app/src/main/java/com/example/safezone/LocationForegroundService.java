package com.example.safezone;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import com.google.android.gms.location.*;

public class LocationForegroundService extends Service {
    private static boolean running = false;
    private FusedLocationProviderClient fused;
    private LocationCallback locationCallback;
    private static final String CHANNEL_ID = "safezone_channel";
    private SafeZoneManager safeZoneManager;

    public static boolean isRunning() { return running; }

    @Override
    public void onCreate() {
        super.onCreate();
        running = true;
        safeZoneManager = SafeZoneManager.getInstance(getApplicationContext());
        fused = LocationServices.getFusedLocationProviderClient(this);

        createNotificationChannel();
        startForeground(12, buildNotification());
        setupLocationCallback();
        requestLocationUpdates();
    }

    private void setupLocationCallback() {
        locationCallback = new LocationCallback(){
            @Override
            public void onLocationResult(LocationResult result) {
                for (Location loc : result.getLocations()) {
                    safeZoneManager.onLocationUpdate(loc);
                }
            }
        };
    }

    private void requestLocationUpdates() {
        LocationRequest req = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY)
            .setInterval(60_000)
            .setFastestInterval(30_000);

        fused.requestLocationUpdates(req, locationCallback, null);
    }

    private android.app.Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .setContentTitle("SafeZone")
            .setContentText("Monitoring location to manage calls")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel chan = new NotificationChannel(CHANNEL_ID, "SafeZone", NotificationManager.IMPORTANCE_LOW);
            NotificationManager nm = getSystemService(NotificationManager.class);
            if (nm != null) nm.createNotificationChannel(chan);
        }
    }

    @Override
    public void onDestroy() {
        fused.removeLocationUpdates(locationCallback);
        running = false;
        super.onDestroy();
    }

    @Nullable @Override public IBinder onBind(Intent intent) { return null; }
}
