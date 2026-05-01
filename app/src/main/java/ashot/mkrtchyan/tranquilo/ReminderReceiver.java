package ashot.mkrtchyan.tranquilo;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import androidx.core.app.NotificationCompat;

public class ReminderReceiver extends BroadcastReceiver {

    private static final String CHANNEL_ID = "daily_reminder_channel";

    @Override
    public void onReceive(Context context, Intent intent) {

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Daily Reminder",
                    NotificationManager.IMPORTANCE_HIGH
            );
            manager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Tranquilo 🌿")
                .setContentText("Time for your daily mindfulness check-in!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        manager.notify(1001, builder.build());

        SharedPreferences prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("reminder_enabled", false);
        int hour = prefs.getInt("reminder_hour", 9);
        int minute = prefs.getInt("reminder_minute", 0);

        if (enabled) {
            ReminderScheduler.schedule(context, hour, minute);
        }
    }
}