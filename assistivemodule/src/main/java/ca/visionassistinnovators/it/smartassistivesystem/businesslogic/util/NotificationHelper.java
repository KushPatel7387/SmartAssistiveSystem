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

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import ca.visionassistinnovators.it.smartassistivesystem.R;
import ca.visionassistinnovators.it.smartassistivesystem.ui.home.HomeActivity;

public class NotificationHelper {

    // ==== CHANNEL INFO ====
    public static final String CHANNEL_ID_ALERTS = "sas_alerts_channel";
    private static final String CHANNEL_NAME = "Assistive Alerts";
    private static final String CHANNEL_DESCRIPTION =
            "Notifications for SOS triggers and patient alerts.";

    /**
     * Ensures notification channel exists.
     * Safe to call multiple times.
     */
    private static void createChannel(Context context) {

        // Channels are only for Android O (API 26+) and above
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
     * Sends a simple TEST notification
     * Used when user clicks "Send Test Notification" in AlertsFragment
     */
    public static void showTestAlertNotification(Context context) {

        // Always create channel before sending notification
        createChannel(context.getApplicationContext());

        // Tap on notification → open HomeActivity
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                1001,
                intent,
                flags
        );

        // Build notification
        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                        .setSmallIcon(R.drawable.ic_notification)  // 🔔 ensure vector exists
                        .setContentTitle("Smart Assistive System")
                        .setContentText("You have a new alert (test notification).")
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        // Send notification
        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(1001, builder.build());
    }

    /**
     * 🔔 New patient added notification
     * Called from GuardianPatientManager after a patient is saved to DB.
     */
    public static void showNewPatientNotification(Context context) {

        // Ensure channel exists
        createChannel(context.getApplicationContext());

        // Tap → open HomeActivity (same as test)
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        int flags = PendingIntent.FLAG_UPDATE_CURRENT;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            flags |= PendingIntent.FLAG_IMMUTABLE;
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                2002,
                intent,
                flags
        );

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(context, CHANNEL_ID_ALERTS)
                        .setSmallIcon(R.drawable.ic_notification)  // reuse same icon
                        .setContentTitle(context.getString(R.string.new_patient_added_title))
                        .setContentText(context.getString(R.string.new_patient_added_message))
                        .setPriority(NotificationCompat.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentIntent(pendingIntent);

        NotificationManagerCompat manager = NotificationManagerCompat.from(context);
        manager.notify(2002, builder.build());
    }
}
