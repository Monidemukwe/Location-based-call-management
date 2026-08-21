package com.example.safezone;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fused;
    private TextView txtStatus;
    private Button btnStart, btnStop, btnSetCenter, btnRefresh;
    private SafeZoneManager safeZoneManager;

    private final ActivityResultLauncher<String[]> requestPermissionsLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
            updateUi();
        });

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fused = LocationServices.getFusedLocationProviderClient(this);
        safeZoneManager = SafeZoneManager.getInstance(this);

        txtStatus = findViewById(R.id.txtStatus);
        btnStart = findViewById(R.id.btnStart);
        btnStop = findViewById(R.id.btnStop);
        btnSetCenter = findViewById(R.id.btnSetCenter);
        btnRefresh = findViewById(R.id.btnRefresh);

        btnStart.setOnClickListener(v -> {
            if (hasLocationPermissions()) {
                startService(new Intent(this, LocationForegroundService.class));
                updateUi();
            } else {
                requestPermissions();
            }
        });

        btnStop.setOnClickListener(v -> {
            stopService(new Intent(this, LocationForegroundService.class));
            updateUi();
        });

        btnSetCenter.setOnClickListener(v -> {
            fused.getLastLocation().addOnSuccessListener(loc -> {
                if (loc != null) {
                    safeZoneManager.setSafeZone(loc.getLatitude(), loc.getLongitude(), safeZoneManager.getRadiusMeters());
                    updateUi();
                } else {
                    txtStatus.setText("Unable to get last location. Try Refresh.");
                }
            });
        });

        btnRefresh.setOnClickListener(v -> {
            fused.getLastLocation().addOnSuccessListener(this::onLocationReceived);
        });

        updateUi();
    }

    private void onLocationReceived(Location loc) {
        if (loc == null) {
            txtStatus.setText("No location available");
            return;
        }
        boolean inside = safeZoneManager.isInsideSafeZone(loc.getLatitude(), loc.getLongitude());
        String status = String.format("Lat: %.5f Lon: %.5f\nInside safe zone: %s\nRadius: %dm",
            loc.getLatitude(), loc.getLongitude(), inside ? "YES" : "NO", safeZoneManager.getRadiusMeters());
        txtStatus.setText(status);
    }

    private boolean hasLocationPermissions() {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermissions() {
        requestPermissionsLauncher.launch(new String[]{
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        });
    }

    private void updateUi() {
        boolean running = LocationForegroundService.isRunning();
        btnStart.setEnabled(!running);
        btnStop.setEnabled(running);
        txtStatus.setText(running ? "Service running" : "Service stopped");
    }
}
