package ashot.mkrtchyan.tranquilo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Rain extends AppCompatActivity {


    ImageButton btnPlay;
    SeekBar seekBarPosition, volumeBar;
    TextView elapsedTimeView, remainingTimeView;
    MediaPlayer mediaPlayer;
    int totalTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rain);

        btnPlay = findViewById(R.id.btnPlay);
        elapsedTimeView = findViewById(R.id.elapsedTimeView);
        remainingTimeView = findViewById(R.id.remainingTimeView);

        mediaPlayer = MediaPlayer.create(this, R.raw.rainsound);
        mediaPlayer.setLooping(true);
        mediaPlayer.seekTo(0);
        mediaPlayer.setVolume(0.5f, 0.5f);
        totalTime = mediaPlayer.getDuration();


        seekBarPosition = findViewById(R.id.seekBarPosition);
        seekBarPosition.setMax(totalTime);
        seekBarPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean user) {
                if (user) {
                    mediaPlayer.seekTo(progress);
                    seekBarPosition.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        volumeBar = findViewById(R.id.volumeBar);
        volumeBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean user) {
                float numVolume = progress / 100f;
                mediaPlayer.setVolume(numVolume, numVolume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mediaPlayer.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        }).start();
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
    private Handler handler = new Handler() {
        @Override
        public void handleMessage(@NonNull Message msg) {
            int currentPosition = msg.what;
            //update seekbar position
            seekBarPosition.setProgress(currentPosition);

            //update timeView
            String elapsedTime = createTimeView(currentPosition);
            elapsedTimeView.setText(elapsedTime);

            String remainingTime = createTimeView(totalTime-currentPosition);
            remainingTimeView.setText("- "+remainingTime);
        }
    };

    public String createTimeView(int time) {
        String timeView = "";
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;

        timeView = min+":";
        if (sec<10) timeView+="0";
        timeView+=sec;

        return timeView;
    }

    public void setBtnPlay(View view) {
        if (!mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            btnPlay.setImageResource(R.drawable.stop2);
        } else {
            mediaPlayer.pause();
            btnPlay.setImageResource(R.drawable.play);
        }
    }
}