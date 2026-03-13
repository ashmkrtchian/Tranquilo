package ashot.mkrtchyan.tranquilo;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Breath extends AppCompatActivity {

    View ball;
    TextView state;
    Button button;
    String[] quotes = {
            "Calm mind brings inner strength.",
            "Breathing is the bridge to inner peace.",
            "Relax, breathe, and let go.",
            "Every breath is a new beginning.",
            "Focus on the now, release the stress.",
            "Peace comes from within.",
            "Breathe deeply, live fully."
    };
    TextView quoteText;

    int randomIndex = (int) (Math.random() * quotes.length);

    boolean running = false;

    Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breath);

        ball = findViewById(R.id.breathBall);
        state = findViewById(R.id.breathState);
        button = findViewById(R.id.startButton);

        quoteText = findViewById(R.id.quoteText);
        quoteText.setText(quotes[randomIndex]);

        button.setOnClickListener(v -> {

            if(!running){
                running = true;
                button.setText("STOP");
                startBreathing();
            } else {
                running = false;
                button.setText("START");
                state.setText("Ready");
            }

        });

    }

    private void startBreathing(){

        if(!running) return;

        state.setText("Inhale");

        animateBall(1f,1.5f,4000);

        handler.postDelayed(() -> {

            if(!running) return;

            state.setText("Hold");

        },4000);

        handler.postDelayed(() -> {

            if(!running) return;

            state.setText("Exhale");

            animateBall(1.5f,1f,8000);

        },11000);

        handler.postDelayed(this::startBreathing,19000);

    }

    private void animateBall(float from, float to, int duration){

        ScaleAnimation anim = new ScaleAnimation(
                from,to,
                from,to,
                ScaleAnimation.RELATIVE_TO_SELF,0.5f,
                ScaleAnimation.RELATIVE_TO_SELF,0.5f
        );

        anim.setDuration(duration);
        anim.setFillAfter(true);

        ball.startAnimation(anim);
    }
}