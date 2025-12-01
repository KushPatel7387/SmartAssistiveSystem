/**
 * Notification Helper for Smart Assistive System
 * Handles API 26+ channels + API 33+ POST_NOTIFICATIONS permission
 *
 * Course Section: OCA
 * Team Members:
 *  - Sarang Prajapati – N01662036
 *  - Krish Patel – N01666556
 *  - Kush Patel – N01657387
 *  - Daksh Rana – N01664095
 */

package ca.visionassistinnovators.it.smartassistivesystem.businesslogic.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.ui.home.HomeActivity;

public class NotificationHelper {

    // ==== CHANNEL INFO ====
    public static final String CHANNEL_ID_ALERTS = "sas_alerts_channel";
    private static final String CHANNEL_NAME = "Assistive Alerts";
    private static final String CHANNEL_DESCRIPTION =
            "Notifications for SOS triggers and patient alerts.";

    // ==== NOTIFICATION IDS ====
    private static final int NOTIF_ID_TEST        = 1001;
    private static final int NOTIF_ID_NEW_PATIENT = 2002;

    /**
     * Ensures notification channel exists.
     * Safe to call multiple times.
     */
    private static void createChannel(Context context) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID_ALERTS,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESCRIPTION);

            NotificationManager manager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    /**
     * Check POST_NOTIFICATIONS permission on Android 13+.
     */
    private static boolean hasNotificationPermission(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return true; // no runtime permission before API 33
        }

        return ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * Sends a simple TEST notification
     * Used when user clicks "Send Test Notification" in AlertsFragment
     */
    @SuppressLint("MissingPermission")
    public static void showTestAlertNotification(Context context) {

        if (!hasNotificationPermission(context)) {
            // Permission not granted – do nothing.
            return;
        }

        Context appCtx = context.getApplicationContext();
        createChannel(appCtx);

        Intent intent = new Intent(appCtx, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                appCtx,
                NOTIF_ID_TEST,
                intent,
                flags
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(appCtx, CHANNEL_ID_ALERTS)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(appCtx.getString(R.string.app_name))
                        .setContentText(appCtx.getString(R.string.test_notification_message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(appCtx);
        try {
            manager.notify(NOTIF_ID_TEST, builder.build());
        } catch (SecurityException ignored) {
            // Permission might have been revoked between check and notify.
        }
    }

    /**
     * Old API kept for compatibility – generic message.
     */
    public static void showNewPatientNotification(Context context) {
        showNewPatientNotification(context, null);
    }

    /**
     * 🔔 New patient added notification, with optional patient name
     * Called from GuardianPatientManager after patient is saved to DB.
     */
    @SuppressLint("MissingPermission")
    public static void showNewPatientNotification(Context context, String patientName) {

        if (!hasNotificationPermission(context)) {
            return;
        }

        Context appCtx = context.getApplicationContext();
        createChannel(appCtx);

        Intent intent = new Intent(appCtx, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                appCtx,
                NOTIF_ID_NEW_PATIENT,
                intent,
                flags
        );

        String title = appCtx.getString(R.string.new_patient_added_title);
        String message = (patientName != null && !patientName.isEmpty())
                ? appCtx.getString(R.string.new_patient_added_message_with_name, patientName)
                : appCtx.getString(R.string.new_patient_added_message);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(appCtx, CHANNEL_ID_ALERTS)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setContentTitle(title)
                        .setContentText(message)
                        .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(appCtx);
        try {
            manager.notify(NOTIF_ID_NEW_PATIENT, builder.build());
        } catch (SecurityException ignored) {
            // Permission might have been revoked between check and notify.
        }
    }
}
