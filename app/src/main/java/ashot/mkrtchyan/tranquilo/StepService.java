package ashot.mkrtchyan.tranquilo;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import androidx.core.app.NotificationCompat;

public class StepService extends Service implements SensorEventListener {

    public static int steps = 0;
    private SensorManager sensorManager;
    private Sensor stepSensor;
    private static final String CHANNEL_ID = "tranquilo_steps";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(1, buildNotification());

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // TYPE_STEP_COUNTER is more reliable than STEP_DETECTOR
        stepSensor = sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (stepSensor != null) {
            sensorManager.registerListener(this, stepSensor, SensorManager.SENSOR_DELAY_UI);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            // STEP_COUNTER gives total steps since last reboot
            // We save the first value and subtract to get today's steps
            int totalStepsSinceReboot = (int) event.values[0];

            if (StepPrefs.getBootSteps(this) == -1) {
                StepPrefs.saveBootSteps(this, totalStepsSinceReboot);
            }

            steps = totalStepsSinceReboot - StepPrefs.getBootSteps(this);

            // Notify the activity to update if it's open
            Intent intent = new Intent("STEP_UPDATE");
            sendBroadcast(intent);

            // Update notification
            NotificationManager nm = getSystemService(NotificationManager.class);
            nm.notify(1, buildNotification());
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Tranquilo")
                .setContentText("Steps today: " + steps)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Step Counter", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public IBinder onBind(Intent intent) { return null; }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sensorManager.unregisterListener(this);
    }
}