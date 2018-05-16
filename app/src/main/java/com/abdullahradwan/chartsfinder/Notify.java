package com.abdullahradwan.chartsfinder;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;

class Notify {

    public static void notify(final Context context, final String text) {

        final String title = context.getResources().getString(R.string.app_name);

        // Set builder
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"charts-finder")

                // Set appropriate defaults for the notification light, sound,
                // and vibration.
                .setDefaults(Notification.DEFAULT_ALL)

                // Set required fields, including the small icon, the
                // notification title, and text.

                .setSmallIcon(R.drawable.ic_notify)
                .setContentTitle(title)
                .setContentText(text)

                // All fields below this line are optional.

                // Use a default priority (recognized on devices running Android
                // 4.1 or later)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)

                // Provide a large icon, shown with the notification in the
                // notification drawer on devices running Android 3.0 or later.
                //.setLargeIcon(picture)

                // Set ticker text (preview) information for this notification.
                .setTicker(text)

                // Show a number. This is useful when stacking notifications of
                // a single type.
                .setNumber(1)

                // Automatically dismiss the notification when it is touched.
                .setAutoCancel(true);

        notify(context, builder.build(),title);
    }

    private static void notify(final Context context, final Notification notification, final String title) {

        // Get access to notifications service
        final NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        try {

            // If Android O (Oreo) or above
            if (Build.VERSION.SDK_INT >= 26) {
                nm.createNotificationChannel(new NotificationChannel("charts-finder", title,
                        NotificationManager.IMPORTANCE_DEFAULT));
            }

            // Start notify
            nm.notify("charts-finder", 0, notification);

        } catch (Exception ignored){}
    }
}
