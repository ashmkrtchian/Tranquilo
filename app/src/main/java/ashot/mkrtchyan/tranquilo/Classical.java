package ashot.mkrtchyan.tranquilo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Classical extends AppCompatActivity {

    TextView mozartBtn;
    LinearLayout mozartControls;
    SeekBar mozartVolume;

    MediaPlayer mediaPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classical);

        mozartBtn = findViewById(R.id.mozartBtn);
        mozartControls = findViewById(R.id.mozartControls);
        mozartVolume = findViewById(R.id.mozartVolume);

        mozartBtn.setOnClickListener(v -> {

            if (mozartControls.getVisibility() == View.GONE) {

                mozartControls.setVisibility(View.VISIBLE);

                mediaPlayer = MediaPlayer.create(this, R.raw.rainsound);
                mediaPlayer.start();

            } else {

                mozartControls.setVisibility(View.GONE);

            }

        });

        mozartVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                float volume = progress / 100f;
                mediaPlayer.setVolume(volume, volume);

            }

            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }
}