package ashot.mkrtchyan.tranquilo;

import android.app.AlertDialog;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class PoemsActivity extends AppCompatActivity {

    private PoemAdapter adapter;
    private CountDownTimer sleepTimer;
    private TextView tvTimerLabel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_poems);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        RecyclerView recyclerView = findViewById(R.id.poemRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Switch switchAutoplay = findViewById(R.id.switchAutoplay);
        switchAutoplay.setOnCheckedChangeListener((btn, isChecked) -> {
            if (adapter != null) adapter.setAutoPlayEnabled(isChecked);
        });

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        tvTimerLabel = findViewById(R.id.tvTimerLabel);
        findViewById(R.id.btnTimer).setOnClickListener(v -> showTimerDialog());

        PoemRepository.fetchAll(new PoemRepository.PoemCallback() {
            @Override
            public void onLoaded(List<Poem> poems) {
                adapter = new PoemAdapter(poems);
                adapter.setAutoPlayEnabled(switchAutoplay.isChecked());
                recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(String message) {
                Toast.makeText(PoemsActivity.this,
                        "Failed to load poems", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTimerDialog() {
        if (sleepTimer != null) {
            new AlertDialog.Builder(this)
                    .setTitle("Sleep Timer")
                    .setMessage("Timer is already running. Cancel it?")
                    .setPositiveButton("Cancel Timer", (d, w) -> cancelTimer())
                    .setNegativeButton("Keep", null)
                    .show();
            return;
        }

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(android.view.Gravity.CENTER);
        layout.setPadding(40, 20, 40, 20);

        NumberPicker minutePicker = new NumberPicker(this);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(120);
        minutePicker.setValue(15);

        TextView tvMin = new TextView(this);
        tvMin.setText(" min  ");
        tvMin.setTextSize(16);
        tvMin.setTextColor(0xFF283618);

        NumberPicker secondPicker = new NumberPicker(this);
        secondPicker.setMinValue(0);
        secondPicker.setMaxValue(59);
        secondPicker.setValue(0);
        secondPicker.setFormatter(value -> String.format("%02d", value));

        TextView tvSec = new TextView(this);
        tvSec.setText(" sec");
        tvSec.setTextSize(16);
        tvSec.setTextColor(0xFF283618);

        layout.addView(minutePicker);
        layout.addView(tvMin);
        layout.addView(secondPicker);
        layout.addView(tvSec);

        new AlertDialog.Builder(this)
                .setTitle("Sleep Timer")
                .setMessage("App and audio will stop after:")
                .setView(layout)
                .setPositiveButton("Start", (dialog, which) -> {
                    int minutes = minutePicker.getValue();
                    int seconds = secondPicker.getValue();
                    if (minutes == 0 && seconds == 0) {
                        Toast.makeText(this, "Please set a time", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    startTimer(minutes, seconds);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void startTimer(int minutes, int seconds) {
        long millis = (minutes * 60L + seconds) * 1000L;
        tvTimerLabel.setText(formatCountdown(millis));

        sleepTimer = new CountDownTimer(millis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                tvTimerLabel.setText(formatCountdown(millisUntilFinished));
            }

            @Override
            public void onFinish() {
                if (adapter != null) adapter.releasePlayer();
                sleepTimer = null;
                getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                finishAffinity();
            }
        }.start();

        String msg = (minutes > 0 ? minutes + " min " : "") + (seconds > 0 ? seconds + " sec" : "");
        Toast.makeText(this, "Sleep timer set for " + msg, Toast.LENGTH_SHORT).show();
    }

    private void cancelTimer() {
        if (sleepTimer != null) {
            sleepTimer.cancel();
            sleepTimer = null;
        }
        tvTimerLabel.setText("Timer");
        Toast.makeText(this, "Timer cancelled", Toast.LENGTH_SHORT).show();
    }

    private String formatCountdown(long millis) {
        long mins = millis / 60000;
        long secs = (millis % 60000) / 1000;
        return String.format("%d:%02d", mins, secs);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sleepTimer != null) sleepTimer.cancel();
        if (adapter != null) adapter.releasePlayer();
    }
}