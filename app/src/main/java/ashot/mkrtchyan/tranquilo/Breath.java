package ashot.mkrtchyan.tranquilo;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.view.View;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class Breath extends AppCompatActivity {

    enum Technique {
        BOX("Box Breathing",             4000, 4000, 4000, 4000, 2),
        FOUR_SEVEN_EIGHT("4-7-8",        4000, 7000, 8000,    0, 2),
        CALM("Calm Breathing",           4000,    0, 6000,    0, 1),
        PHYSIOLOGICAL_SIGH("Phys. Sigh", 2000, 1500, 6000,   0, 3);

        final String displayName;
        final long inhaleMs, hold1Ms, exhaleMs, hold2Ms;
        final int coins;

        Technique(String n, long i, long h1, long e, long h2, int c) {
            displayName = n; inhaleMs = i; hold1Ms = h1;
            exhaleMs = e; hold2Ms = h2; coins = c;
        }
    }

    ImageView    breathBall;
    TextView     breathState, breathTimer, coinCount, quoteText;
    MaterialButton startButton;

    View cardBox, card478, cardCalm, cardSigh;

    View rainToggle, oceanToggle, fireToggle, forestToggle;
    SeekBar  rainSeek, oceanSeek, fireSeek, forestSeek;
    TextView rainLabel, oceanLabel, fireLabel, forestLabel;

    MediaPlayer inhaleSound, exhaleSound, holdSound;
    MediaPlayer rainPlayer, oceanPlayer, firePlayer, forestPlayer;

    boolean    running     = false;
    Technique  currentTech = Technique.BOX;
    Handler    handler     = new Handler();
    CountDownTimer phaseTimer;

    int totalCoins = 0;

    FirebaseFirestore db;
    FirebaseAuth      auth;

    String[] quotes = {
            "Calm mind brings inner strength.",
            "Breathing is the bridge to inner peace.",
            "Relax, breathe, and let go.",
            "Every breath is a new beginning.",
            "Focus on the now, release the stress.",
            "Peace comes from within.",
            "Breathe deeply, live fully.",
            "Each exhale is a release of what no longer serves you."
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_breath);

        bindViews();
        initFirebase();
        initBreathingSounds();
        initSoundMixer();
        initTechniqueCards();

        findViewById(R.id.btnBack).setOnClickListener(v -> {
            getOnBackPressedDispatcher().onBackPressed();
        });

        quoteText.setText(quotes[(int)(Math.random() * quotes.length)]);
        selectTechnique(Technique.BOX);

        startButton.setOnClickListener(v -> {
            if (!running) beginSession();
            else          endSession();
        });

        loadCoinsFromFirestore();
    }

    @Override protected void onPause()  { super.onPause();  if (running) endSession(); }

    @Override protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
        releaseAll();
    }


    private void bindViews() {
        breathBall   = findViewById(R.id.breathBall);
        breathState  = findViewById(R.id.breathState);
        breathTimer  = findViewById(R.id.breathTimer);
        coinCount    = findViewById(R.id.coinCount);
        quoteText    = findViewById(R.id.quoteText);
        startButton  = findViewById(R.id.startButton);

        cardBox  = findViewById(R.id.cardBox);
        card478  = findViewById(R.id.card478);
        cardCalm = findViewById(R.id.cardCalm);
        cardSigh = findViewById(R.id.cardSigh);

        rainToggle   = findViewById(R.id.rainToggle);
        oceanToggle  = findViewById(R.id.oceanToggle);
        fireToggle   = findViewById(R.id.fireToggle);
        forestToggle = findViewById(R.id.forestToggle);

        rainSeek   = findViewById(R.id.rainVolume);
        oceanSeek  = findViewById(R.id.oceanVolume);
        fireSeek   = findViewById(R.id.fireVolume);
        forestSeek = findViewById(R.id.forestVolume);

        rainLabel   = findViewById(R.id.rainVolumeLabel);
        oceanLabel  = findViewById(R.id.oceanVolumeLabel);
        fireLabel   = findViewById(R.id.fireVolumeLabel);
        forestLabel = findViewById(R.id.forestVolumeLabel);
    }

    private void initTechniqueCards() {
        cardBox.setOnClickListener(v  -> selectTechnique(Technique.BOX));
        card478.setOnClickListener(v  -> selectTechnique(Technique.FOUR_SEVEN_EIGHT));
        cardCalm.setOnClickListener(v -> selectTechnique(Technique.CALM));
        cardSigh.setOnClickListener(v -> selectTechnique(Technique.PHYSIOLOGICAL_SIGH));
    }

    private void selectTechnique(Technique t) {
        if (running) return;
        currentTech = t;
        breathState.setText("");
        breathTimer.setText("");

        cardBox.setAlpha(0.4f);  card478.setAlpha(0.4f);
        cardCalm.setAlpha(0.4f); cardSigh.setAlpha(0.4f);

        View chosen = null;
        switch (t) {
            case BOX:                chosen = cardBox;  break;
            case FOUR_SEVEN_EIGHT:   chosen = card478;  break;
            case CALM:               chosen = cardCalm; break;
            case PHYSIOLOGICAL_SIGH: chosen = cardSigh; break;
        }
        if (chosen != null) {
            chosen.setAlpha(1f);
            ScaleAnimation pop = new ScaleAnimation(0.94f, 1f, 0.94f, 1f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                    ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
            pop.setDuration(180);
            chosen.startAnimation(pop);
        }
    }


    private void beginSession() {
        running = true;
        startButton.setText("STOP");
        startBreathing();
    }

    private void endSession() {
        running = false;
        handler.removeCallbacksAndMessages(null);
        if (phaseTimer != null) phaseTimer.cancel();
        startButton.setText("START");
        breathState.setText("");
        breathTimer.setText("");
        resetOrbScale();
        stopBreathingSounds();
        saveLastBreathingSession();
    }

    private void startBreathing() {
        if (!running) return;
        switch (currentTech) {
            case BOX:                runBox();               break;
            case FOUR_SEVEN_EIGHT:   runFourSevenEight();    break;
            case CALM:               runCalm();              break;
            case PHYSIOLOGICAL_SIGH: runPhysiologicalSigh(); break;
        }
    }

    private void runBox() {
        phase("INHALE", 4000, true);
        after(4000,  () -> phase("HOLD",   4000, false));
        after(8000,  () -> phase("EXHALE", 4000, false));
        after(12000, () -> phase("HOLD",   4000, false));
        after(16000, () -> { awardCoins(currentTech.coins); runBox(); });
        animateBall(1f, 1.2f, 4000);
        after(8000,  () -> animateBall(1.2f, 1f, 4000));
    }

    private void runFourSevenEight() {
        phase("INHALE", 4000, true);
        after(4000,  () -> phase("HOLD",   7000, false));
        after(11000, () -> phase("EXHALE", 8000, false));
        after(19000, () -> { awardCoins(currentTech.coins); runFourSevenEight(); });
        animateBall(1f, 1.2f, 4000);
        after(11000, () -> animateBall(1.2f, 1f, 8000));
    }

    private void runCalm() {
        phase("INHALE", 4000, true);
        after(4000,  () -> phase("EXHALE", 6000, false));
        after(10000, () -> { awardCoins(currentTech.coins); runCalm(); });
        animateBall(1f, 1.2f, 4000);
        after(4000, () -> animateBall(1.2f, 1f, 6000));
    }

    private void runPhysiologicalSigh() {
        phase("INHALE",       2000, true);
        after(2000, () -> phase("SNIFF ↑",      1500, false));
        after(3500, () -> phase("EXHALE SLOW",   6000, false));
        after(9500, () -> { awardCoins(currentTech.coins); runPhysiologicalSigh(); });
        animateBall(1f, 1.1f, 2000);
        after(2000, () -> animateBall(1.1f, 1.2f, 1500));
        after(3500, () -> animateBall(1.2f, 1f, 6000));
    }

    private void phase(String label, long durationMs, boolean playInhale) {
        if (!running) return;
        breathState.setText(label);

        if (playInhale || label.equals("SNIFF ↑")) playSound(inhaleSound);
        else if (label.equals("HOLD")) playSound(holdSound);
        else playSound(exhaleSound);

        if (phaseTimer != null) phaseTimer.cancel();
        phaseTimer = new CountDownTimer(durationMs, 100) {
            @Override public void onTick(long ms) {
                breathTimer.setText(String.format("%.1fs", ms / 1000f));
            }
            @Override public void onFinish() { breathTimer.setText(""); }
        }.start();
    }


    private void initSoundMixer() {
        rainPlayer   = MediaPlayer.create(this, R.raw.rainsound);
        oceanPlayer  = MediaPlayer.create(this, R.raw.ocean);
        firePlayer   = MediaPlayer.create(this, R.raw.fire);
        forestPlayer = MediaPlayer.create(this, R.raw.forest);

        for (MediaPlayer p : new MediaPlayer[]{rainPlayer, oceanPlayer, firePlayer, forestPlayer}) {
            p.setLooping(true);
            p.setVolume(0f, 0f);
            p.start();
        }

        setupSoundRow(rainToggle,   rainSeek,   rainLabel,   rainPlayer);
        setupSoundRow(oceanToggle,  oceanSeek,  oceanLabel,  oceanPlayer);
        setupSoundRow(fireToggle,   fireSeek,   fireLabel,   firePlayer);
        setupSoundRow(forestToggle, forestSeek, forestLabel, forestPlayer);
    }

    private void setupSoundRow(View toggle, SeekBar seek, TextView label, MediaPlayer player) {
        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                float v = progress / 100f;
                player.setVolume(v, v);
                label.setText(progress + "%");
                toggle.setAlpha(progress > 0 ? 1f : 0.4f);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb)  {}
        });

        toggle.setAlpha(0.4f);
        toggle.setOnClickListener(v -> seek.setProgress(seek.getProgress() > 0 ? 0 : 60));
    }


    private void initBreathingSounds() {
        inhaleSound = MediaPlayer.create(this, R.raw.inhale);
        exhaleSound = MediaPlayer.create(this, R.raw.exhale);
        holdSound   = MediaPlayer.create(this, R.raw.hold);
    }

    private void playSound(MediaPlayer p) {
        if (p == null) return;
        if (p.isPlaying()) { p.pause(); p.seekTo(0); }
        p.start();
    }

    private void stopBreathingSounds() {
        pauseReset(inhaleSound);
        pauseReset(exhaleSound);
        pauseReset(holdSound);
    }

    private void animateBall(float from, float to, long duration) {
        ScaleAnimation anim = new ScaleAnimation(from, to, from, to,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        anim.setDuration(duration);
        anim.setFillAfter(true);
        breathBall.startAnimation(anim);
    }

    private void resetOrbScale() {
        ScaleAnimation reset = new ScaleAnimation(1f, 1f, 1f, 1f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f,
                ScaleAnimation.RELATIVE_TO_SELF, 0.5f);
        reset.setDuration(600);
        reset.setFillAfter(true);
        breathBall.startAnimation(reset);
    }

    private void after(long delay, Runnable r) {
        handler.postDelayed(() -> { if (running) r.run(); }, delay);
    }

    private void pauseReset(MediaPlayer p) {
        if (p != null) { if (p.isPlaying()) p.pause(); p.seekTo(0); }
    }

    private void releaseAll() {
        MediaPlayer[] all = {inhaleSound, exhaleSound, holdSound,
                rainPlayer, oceanPlayer, firePlayer, forestPlayer};
        for (MediaPlayer p : all) { if (p != null) p.release(); }
    }

    private void initFirebase() {
        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    private void loadCoinsFromFirestore() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid()).get()
                .addOnSuccessListener(snap -> {
                    if (snap.exists() && snap.contains("calmCoins")) {
                        Long c = snap.getLong("calmCoins");
                        if (c != null) {
                            totalCoins = c.intValue();
                            coinCount.setText(String.valueOf(totalCoins));
                        }
                    }
                });
    }

    private void awardCoins(int amount) {
        totalCoins += amount;
        coinCount.setText(String.valueOf(totalCoins));

        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        DocumentReference ref = db.collection("users").document(user.getUid());
        db.runTransaction(tx -> {
            DocumentSnapshot snap = tx.get(ref);
            long current = 0;
            if (snap.exists() && snap.contains("calmCoins")) {
                Long c = snap.getLong("calmCoins");
                if (c != null) current = c;
            }
            tx.update(ref, "calmCoins", current + amount);
            return null;
        }).addOnSuccessListener(v ->
                Toast.makeText(this, "🪙 +" + amount + " CalmCoins!", Toast.LENGTH_SHORT).show()
        );
    }

    private void saveLastBreathingSession() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;
        db.collection("users").document(user.getUid())
                .update("lastBreathingSession", System.currentTimeMillis())
                .addOnFailureListener(e -> {
                    java.util.Map<String, Object> data = new java.util.HashMap<>();
                    data.put("lastBreathingSession", System.currentTimeMillis());
                    db.collection("users").document(user.getUid()).set(data, SetOptions.merge());
                });
    }
}