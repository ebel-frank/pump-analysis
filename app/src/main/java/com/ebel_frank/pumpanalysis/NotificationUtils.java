package com.ebel_frank.pumpanalysis;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import static android.content.Context.MODE_PRIVATE;

class NotificationUtils {

    private static final String CHANNEL_ID = "com.ebel_frank.pumpanalysis";

    static void notify(@NonNull Context context, String text) {

        SharedPreferences preferences = context.getSharedPreferences(SettingsActivity.PREFS, MODE_PRIVATE);

        boolean notify =  preferences.getBoolean("notification", false);
        if (notify) {

            // Build notification based on Intent
            Notification notification = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_water_drop)
                    /*.setLargeIcon()*/
                    .setContentText(text)
                    .setAutoCancel(true)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                    .build();

            // Show notification
            NotificationManagerCompat manager = NotificationManagerCompat.from(context);
            int ALARM_REQUEST_CODE = 231220;
            manager.notify(ALARM_REQUEST_CODE, notification);
        }
    }

    static Intent openNotificationSettingsForApp(String packageName, int appUid) {
        // Links to this app's notification settings.
        Intent intent = new Intent();
        intent.setAction("android.settings.APP_NOTIFICATION_SETTINGS");
        intent.putExtra("app_package", packageName);
        intent.putExtra("app_uid", appUid);

        // for Android 8 and above
        intent.putExtra("android.provider.extra.APP_PACKAGE", packageName);
        return intent;
    }

    static void createNotificationChannel(Context context) {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = context.getString(R.string.channel_name);
            NotificationChannel channel = new NotificationChannel(NotificationUtils.CHANNEL_ID, name, NotificationManager.IMPORTANCE_DEFAULT);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            assert notificationManager != null;
            notificationManager.createNotificationChannel(channel);
        }
    }

}
