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

import android.content.pm.ServiceInfo;

import java.util.Locale;

import ca.visionassistinnovators.it.smartassistivesystem.businesslogic.WalkingAssistLogic;

public class WalkingAssistService extends Service implements LocationListener {

    private LocationManager locationManager;
    private TextToSpeech tts;
    private Vibrator vibrator;

    // ✅ Business logic moved to businesslogic package
    private WalkingAssistLogic logic;

    @Override
    public void onCreate() {
        super.onCreate();

        logic = new WalkingAssistLogic(); // ✅ Inject logic

        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

        tts = new TextToSpeech(getApplicationContext(), status -> {
            if (status == TextToSpeech.SUCCESS) {
                tts.setLanguage(Locale.US);
            }
        });

        createNotificationChannel();

        Notification notification = buildNotification();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                    1,
                    notification,
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION
            );
        } else {
            startForeground(1, notification);
        }

        requestLocationUpdates();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

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
            getSystemService(NotificationManager.class)
                    .createNotificationChannel(channel);
        }
    }

    // -------------------------------------------------------------------------
    // LOCATION UPDATES
    // -------------------------------------------------------------------------
    private void requestLocationUpdates() {

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            stopSelf();
            return;
        }

        locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                1500,
                1,
                this
        );
    }

    // -------------------------------------------------------------------------
    // LOCATION RECEIVED → DELEGATE TO BUSINESS LOGIC ✅
    // -------------------------------------------------------------------------
    @Override
    public void onLocationChanged(Location location) {

        WalkingAssistLogic.AssistEvent event =
                logic.processLocation(location);

        switch (event) {

            case CROSSWALK_AHEAD:
                speak("Crosswalk ahead");
                break;

            case BUS_STOP_REACHED:
                speak("Your bus stop is here");
                break;

            case NO_LANDMARK:
                vibrate();
                break;
        }
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
            vibrator.vibrate(
                    VibrationEffect.createOneShot(
                            250,
                            VibrationEffect.DEFAULT_AMPLITUDE
                    )
            );
        } else {
            vibrator.vibrate(250);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (tts != null) {
            tts.shutdown();
        }

        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
    }
}
