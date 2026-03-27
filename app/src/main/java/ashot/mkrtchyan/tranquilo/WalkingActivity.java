package ashot.mkrtchyan.tranquilo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.ProgressBar;
import android.widget.TextView;

public class WalkingActivity extends AppCompatActivity {

    private TextView tvSteps, tvDistance, tvCalories;
    private ProgressBar progressBar;

    private final BroadcastReceiver stepReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateUI(StepService.steps);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking);

        tvSteps    = findViewById(R.id.tv_steps);
        tvDistance = findViewById(R.id.tv_distance);
        tvCalories = findViewById(R.id.tv_calories);
        progressBar = findViewById(R.id.progress_steps);
        progressBar.setMax(10000);

        requestPermissionAndStartService();

        // Show current steps immediately when screen opens
        updateUI(StepService.steps);
    }

    private void requestPermissionAndStartService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ActivityCompat.checkSelfPermission(this,
                    Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, 100);
                return;
            }
        }
        startStepService();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 100 && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startStepService();
        }
    }

    private void startStepService() {
        Intent intent = new Intent(this, StepService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        } else {
            startService(intent);
        }
    }

    private void updateUI(int steps) {
        tvSteps.setText(String.valueOf(steps));
        progressBar.setProgress(steps);

        double distanceKm = steps * 0.00078;
        tvDistance.setText(String.format("%.2f km", distanceKm));

        double calories = steps * 0.04;
        tvCalories.setText(String.format("%.0f kcal", calories));
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(stepReceiver, new IntentFilter("STEP_UPDATE"),
                Context.RECEIVER_NOT_EXPORTED);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(stepReceiver);
    }
}