package ca.visionassistinnovators.it.smartassistivesystem.ui.services;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.tts.TextToSpeech;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.maps.model.LatLng;

import android.content.pm.ServiceInfo;

import java.util.Locale;

public class WalkingAssistService extends Service implements LocationListener {

    private LocationManager locationManager;
    private TextToSpeech tts;
    private Vibrator vibrator;

    // Demo locations
    LatLng CROSSWALK = new LatLng(43.7325, -79.6086);
    LatLng BUS_STOP  = new LatLng(43.7330, -79.6075);

    @Override
    public void onCreate() {
        super.onCreate();

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        createNotificationChannel();

        // ✔ NEW — Required for Android 12–16
        Notification notification = buildNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                    1,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION   // <-- FIXED ERROR
            );
        } else {
            startForeground(1, notification);
        }

        requestLocationUpdates();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) { return null; }

    // -------------------------------------------------------------------------
    // NOTIFICATION
    // -------------------------------------------------------------------------
    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, "walk_channel")
                .setContentTitle("Walking Assistance Active")
                .setContentText("Monitoring your surroundings…")
                .setSmallIcon(android.R.drawable.ic_menu_mylocation)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            "walk_channel",
                            "Walking Assistance",
                            NotificationManager.IMPORTANCE_LOW
                    );
            getSystemService(NotificationManager.class).createNotificationChannel(channel);
        }
    }

    // -------------------------------------------------------------------------
    // LOCATION UPDATES
    // -------------------------------------------------------------------------
    private void requestLocationUpdates() {
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1500,   // every 1.5 sec
                1,      // min 1 meter
                this
        );
    }

    // -------------------------------------------------------------------------
    // LOCATION RECEIVED
    // -------------------------------------------------------------------------
    @Override
    public void onLocationChanged(Location location) {

        LatLng current = new LatLng(location.getLatitude(), location.getLongitude());

        double dCrosswalk = distance(current, CROSSWALK);
        double dBusStop   = distance(current, BUS_STOP);

        if (dCrosswalk < 20) {
            speak("Crosswalk ahead");
        }

        if (dBusStop < 20) {
            speak("Your bus stop is here");
        }

        // No known landmark → alert via vibration
        if (dCrosswalk > 25 && dBusStop > 25) {
            vibrate();
        }
    }

    private double distance(LatLng a, LatLng b) {
        float[] result = new float[1];
        Location.distanceBetween(a.latitude, a.longitude, b.latitude, b.longitude, result);
        return result[0];
    }

    // -------------------------------------------------------------------------
    // UTILITIES
    // -------------------------------------------------------------------------
    private void speak(String msg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null, null);
        } else {
            tts.speak(msg, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    private void vibrate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createOneShot(250, VibrationEffect.DEFAULT_AMPLITUDE));
        } else {
            vibrator.vibrate(250);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (tts != null) tts.shutdown();
        if (locationManager != null) locationManager.removeUpdates(this);
    }
}
