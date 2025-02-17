package com.example.taskremainderapp;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;

public class NotificationHelper {

    private static final String CHANNEL_ID_HIGH = "high_priority_channel";
    private static final String CHANNEL_ID_LOW = "low_priority_channel";
    private static final int NOTIFICATION_ID = 1;

    private final Context context;
    private final NotificationManager notificationManager;
    private final SharedPreferences sharedPreferences;

    public NotificationHelper(Context context) {
        this.context = context;
        this.notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        this.sharedPreferences = context.getSharedPreferences("NotificationSettings", Context.MODE_PRIVATE);
        createNotificationChannels();
    }

    private void createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel highChannel = new NotificationChannel(
                    CHANNEL_ID_HIGH,
                    "High Priority Tasks",
                    NotificationManager.IMPORTANCE_HIGH
            );
            highChannel.setDescription("Channel for critical reminders");
            highChannel.enableVibration(sharedPreferences.getBoolean("vibration_enabled", true));
            highChannel.enableLights(true);

            NotificationChannel lowChannel = new NotificationChannel(
                    CHANNEL_ID_LOW,
                    "General Tasks",
                    NotificationManager.IMPORTANCE_LOW
            );
            lowChannel.setDescription("Channel for regular notifications");
            lowChannel.enableVibration(false);
            lowChannel.enableLights(false);

            notificationManager.createNotificationChannel(highChannel);
            notificationManager.createNotificationChannel(lowChannel);
        }
    }

    public void sendNotification(String title, String message, boolean isHighPriority, String taskId) {
        String channelId = isHighPriority ? CHANNEL_ID_HIGH : CHANNEL_ID_LOW;

        // Create an intent for TaskDetailsActivity
        Intent intent = new Intent(context, TaskDetailsActivity.class);
        intent.putExtra("title", title);
        intent.putExtra("description", message);
        intent.putExtra("isHighPriority", isHighPriority);
        intent.putExtra("taskId", taskId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.drawable.ic_notification)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(isHighPriority ? NotificationCompat.PRIORITY_HIGH : NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .setAutoCancel(true);

        if (isHighPriority) {
            boolean vibrationEnabled = sharedPreferences.getBoolean("vibration_enabled", true);
            if (vibrationEnabled) {
                builder.setDefaults(Notification.DEFAULT_ALL);
            } else {
                builder.setDefaults(Notification.DEFAULT_LIGHTS | Notification.DEFAULT_SOUND);
            }
        }

        notificationManager.notify(taskId.hashCode(), builder.build());
    }

    public void updateNotificationSettings(boolean enableVibration) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("vibration_enabled", enableVibration);
        editor.apply();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel highChannel = notificationManager.getNotificationChannel(CHANNEL_ID_HIGH);
            if (highChannel != null) {
                highChannel.enableVibration(enableVibration);
                notificationManager.createNotificationChannel(highChannel);
            }
        }
    }
}