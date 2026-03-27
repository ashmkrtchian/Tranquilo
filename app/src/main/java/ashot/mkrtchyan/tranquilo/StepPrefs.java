package ashot.mkrtchyan.tranquilo;

import android.content.Context;
import android.content.SharedPreferences;

public class StepPrefs {

    private static final String PREFS = "tranquilo_prefs";
    private static final String KEY_BOOT_STEPS = "boot_steps";

    public static void saveBootSteps(Context ctx, int value) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_BOOT_STEPS, value).apply();
    }

    public static int getBootSteps(Context ctx) {
        return ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .getInt(KEY_BOOT_STEPS, -1);
    }

    // Call this every day at midnight to reset
    public static void resetBootSteps(Context ctx) {
        ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
                .edit().putInt(KEY_BOOT_STEPS, -1).apply();
    }
}