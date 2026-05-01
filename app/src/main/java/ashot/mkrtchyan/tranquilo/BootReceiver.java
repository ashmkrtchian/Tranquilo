package ashot.mkrtchyan.tranquilo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            SharedPreferences prefs = context.getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE);
            boolean enabled = prefs.getBoolean("reminder_enabled", false);
            int hour = prefs.getInt("reminder_hour", 9);
            int minute = prefs.getInt("reminder_minute", 0);

            if (enabled) {
                ReminderScheduler.schedule(context, hour, minute);
            }
        }
    }
}