package ashot.mkrtchyan.tranquilo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import androidx.appcompat.app.AppCompatActivity;

public class Breath extends AppCompatActivity {

    View ball;
    TextView state;
    Button button;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
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
    MediaPlayer inhaleSound,exhaleSound,holdSound;

    MediaPlayer rainSound, oceanSound, fireSound, forestSound;
    MediaPlayer currentBGSound = null;
    int currentBGSoundId = -1;
    Handler handler = new Handler();

    @Override
    protected void onPause() {
        super.onPause();
        stopAllSounds();
        running = false;
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (inhaleSound != null) {
            inhaleSound.release();
            inhaleSound = null;
        }

        if (exhaleSound != null) {
            exhaleSound.release();
            exhaleSound = null;
        }

        if (currentBGSound != null) {
            currentBGSound.release();
            currentBGSound = null;
        }
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breath);

        ball = findViewById(R.id.breathBall);
        state = findViewById(R.id.breathState);
        button = findViewById(R.id.startButton);

        inhaleSound = MediaPlayer.create(this, R.raw.inhale);
        exhaleSound = MediaPlayer.create(this, R.raw.exhale);
        holdSound = MediaPlayer.create(this, R.raw.hold);
        quoteText = findViewById(R.id.quoteText);
        quoteText.setText(quotes[randomIndex]);
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        button.setOnClickListener(v -> {

            if(!running){
                running = true;
                button.setText("STOP");
                startBreathing();
            } else {
                running = false;
                button.setText("START");
                state.setText("Ready");
                saveLastBreathingSession();

                if (inhaleSound.isPlaying()) inhaleSound.pause();
                if (exhaleSound.isPlaying()) exhaleSound.pause();
                if (holdSound.isPlaying()) holdSound.pause();

                inhaleSound.seekTo(0);
                exhaleSound.seekTo(0);
                holdSound.seekTo(0);
            }

        });
        rainSound = MediaPlayer.create(this, R.raw.rainsound);
        oceanSound = MediaPlayer.create(this, R.raw.ocean);
        fireSound = MediaPlayer.create(this, R.raw.fire);
        forestSound = MediaPlayer.create(this, R.raw.forest);

        rainSound.setLooping(true);
        oceanSound.setLooping(true);
        fireSound.setLooping(true);
        forestSound.setLooping(true);

        findViewById(R.id.rainBtn).setOnClickListener(v -> toggleBGSound(R.raw.rainsound));
        findViewById(R.id.oceanBtn).setOnClickListener(v -> toggleBGSound(R.raw.ocean));
        findViewById(R.id.fireBtn).setOnClickListener(v -> toggleBGSound(R.raw.fire));
        findViewById(R.id.forestBtn).setOnClickListener(v -> toggleBGSound(R.raw.forest));

    }

    private void addCalmCoins(int coinsToAdd) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        DocumentReference userRef = db.collection("users").document(uid);

        db.runTransaction(transaction -> {
            DocumentSnapshot snapshot = transaction.get(userRef);

            long currentCoins = 0;
            if (snapshot.exists() && snapshot.contains("calmCoins")) {
                Long coins = snapshot.getLong("calmCoins");
                if (coins != null) currentCoins = coins;
            }

            long newCoins = currentCoins + coinsToAdd;

            transaction.update(userRef, "calmCoins", newCoins);

            return null;
        }).addOnSuccessListener(aVoid ->
                Toast.makeText(this, "🪙 +"+coinsToAdd+" CalmCoins earned!", Toast.LENGTH_SHORT).show()
        ).addOnFailureListener(e ->
                Toast.makeText(this, "Error adding coins", Toast.LENGTH_SHORT).show()
        );
    }
    private void toggleBGSound(int soundResId) {
        if (currentBGSoundId == soundResId) {
            if (currentBGSound != null && currentBGSound.isPlaying()) {
                currentBGSound.pause();
                currentBGSound.seekTo(0);
            }
            currentBGSound = null;
            currentBGSoundId = -1;
            return;
        }

        if (currentBGSound != null && currentBGSound.isPlaying()) {
            currentBGSound.pause();
            currentBGSound.seekTo(0);
        }

        currentBGSound = MediaPlayer.create(this, soundResId);
        currentBGSound.setVolume(0.1f, 0.1f);
        currentBGSound.setLooping(true);
        currentBGSound.start();
        currentBGSoundId = soundResId;
    }
    private void startBreathing(){

        if(!running) return;
        inhaleSound.start();
        state.setText("Inhale");

        animateBall(1f,1.5f,4000);

        handler.postDelayed(() -> {

            if(!running) return;
            holdSound.start();
            state.setText("Hold");

        },4000);

        handler.postDelayed(() -> {

            if(!running) return;
            exhaleSound.start();
            state.setText("Exhale");

            animateBall(1.5f,1f,8000);

            addCalmCoins(2);
        },11000);

        handler.postDelayed(this::startBreathing,19000);

    }

    private void saveLastBreathingSession() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        db.collection("users").document(user.getUid())
                .update("lastBreathingSession", System.currentTimeMillis())
                .addOnFailureListener(e -> {
                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    data.put("lastBreathingSession", System.currentTimeMillis());
                    db.collection("users").document(user.getUid())
                            .set(data, SetOptions.merge());
                });
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
    private void stopAllSounds() {


        if (inhaleSound != null) {
            if (inhaleSound.isPlaying()) inhaleSound.pause();
            inhaleSound.seekTo(0);
        }

        if (exhaleSound != null) {
            if (exhaleSound.isPlaying()) exhaleSound.pause();
            exhaleSound.seekTo(0);
        }

        if (holdSound != null) {
            if (holdSound.isPlaying()) holdSound.pause();
            holdSound.seekTo(0);
        }


        if (currentBGSound != null) {
            if (currentBGSound.isPlaying()) currentBGSound.pause();
            currentBGSound.seekTo(0);
        }
    }
}