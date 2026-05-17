package ashot.mkrtchyan.tranquilo;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.animation.OvershootInterpolator;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.res.ResourcesCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SchulteTableActivity extends AppCompatActivity {

    private static final int COLOR_DARK_GREEN  = 0xFF283618;
    private static final int COLOR_MILK        = 0xFFFEFAE0;
    private static final int COLOR_LIGHT_BROWN = 0xFFDDA15E;
    private static final int COLOR_LIGHT_GREEN = 0xFF606C38;
    private static final int COLOR_CARAMEL     = 0xFFBC6C25;

    private static final int COLOR_HINT_YELLOW = 0xFFFFD600;
    private static final int HINT_DELAY_MS = 3000;

    private int currentHintIndex = -1;
    private final Handler hintHandler = new Handler(Looper.getMainLooper());
    private final Runnable hintRunnable = new Runnable() {
        @Override
        public void run() {
            if (gameRunning) showHint();
        }
    };
    private GridLayout schulteGrid;
    private TextView   tvTimer, tvCurrentNumber, tvScore;
    private Button     btnStartPause, btnReset;

    private static final int GRID_SIZE      = 5;
    private static final int TOTAL_NUMBERS  = GRID_SIZE * GRID_SIZE;

    private final int[]  numbers   = new int[TOTAL_NUMBERS];
    private final CardView[] cells = new CardView[TOTAL_NUMBERS];

    private int  nextExpected = 1;
    private int  foundCount   = 0;
    private boolean gameRunning = false;


    private final Handler  timerHandler  = new Handler(Looper.getMainLooper());
    private long   elapsedSeconds = 0;

    private final Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (gameRunning) {
                elapsedSeconds++;
                updateTimerDisplay();
                timerHandler.postDelayed(this, 1000);
            }
        }
    };

    private FirebaseFirestore db;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schulte_table);

        db   = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        bindViews();
        setupButtons();
        buildGrid();
        updateScoreDisplay();

    }

    private void bindViews() {
        schulteGrid     = findViewById(R.id.schulteGrid);
        tvTimer         = findViewById(R.id.tvTimer);
        tvCurrentNumber = findViewById(R.id.tvCurrentNumber);
        tvScore         = findViewById(R.id.tvScore);
        btnStartPause   = findViewById(R.id.btnStartPause);
        btnReset        = findViewById(R.id.btnReset);
    }

    private void setupButtons() {
        btnStartPause.setOnClickListener(v -> onStartPauseClicked());
        btnReset.setOnClickListener(v -> resetGame());

        findViewById(R.id.btnBack).setOnClickListener(v -> onBackPressed());
    }

    private void onStartPauseClicked() {
        if (!gameRunning) startGame();
    }
    private void scheduleHint() {
        hintHandler.removeCallbacks(hintRunnable);
        hintHandler.postDelayed(hintRunnable, HINT_DELAY_MS);
    }

    private ValueAnimator hintAnimator;

    private void showHint() {
        clearHint();
        for (int i = 0; i < TOTAL_NUMBERS; i++) {
            if (numbers[i] == nextExpected && cells[i].isEnabled()) {
                currentHintIndex = i;
                CardView hintCard = cells[i];

                hintAnimator = ValueAnimator.ofArgb(COLOR_MILK, 0xFFFFC300);
                hintAnimator.setDuration(700);
                hintAnimator.setRepeatMode(ValueAnimator.REVERSE);
                hintAnimator.setRepeatCount(ValueAnimator.INFINITE);
                hintAnimator.addUpdateListener(animator -> {
                    int color = (int) animator.getAnimatedValue();
                    hintCard.setCardBackgroundColor(color);
                });
                hintAnimator.start();
                break;
            }
        }
    }

    private void clearHint() {
        hintHandler.removeCallbacks(hintRunnable);
        if (hintAnimator != null) {
            hintAnimator.cancel();
            hintAnimator = null;
        }
        if (currentHintIndex != -1) {
            if (cells[currentHintIndex].isEnabled()) {
                cells[currentHintIndex].setCardBackgroundColor(COLOR_MILK);
            }
            currentHintIndex = -1;
        }
    }
    private void onCellTapped(int index, int number, CardView card, TextView tv) {
        if (number == nextExpected) {
            foundCount++;
            nextExpected++;

            clearHint();
            markFound(card, tv);
            updateScoreDisplay();

            if (foundCount == TOTAL_NUMBERS) {
                tvCurrentNumber.setText("✓");
                onGameComplete();
            } else {
                tvCurrentNumber.setText(String.valueOf(nextExpected));
                scheduleHint();
            }
        } else {
            shakeCell(card);
        }
    }
    private void buildGrid() {
        schulteGrid.removeAllViews();

        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= TOTAL_NUMBERS; i++) list.add(i);
        Collections.shuffle(list);
        for (int i = 0; i < TOTAL_NUMBERS; i++) numbers[i] = list.get(i);

        int columns  = GRID_SIZE;
        int margin   = dpToPx(3);

        for (int i = 0; i < TOTAL_NUMBERS; i++) {
            final int index  = i;
            final int number = numbers[i];

            CardView card = new CardView(this);
            card.setRadius(dpToPx(12));
            card.setCardElevation(dpToPx(4));
            card.setCardBackgroundColor(COLOR_MILK);
            card.setUseCompatPadding(false);
            card.setPreventCornerOverlap(true);

            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.rowSpec    = GridLayout.spec(i / columns, 1, 1f);
            params.columnSpec = GridLayout.spec(i % columns, 1, 1f);
            params.width  = 0;
            params.height = 0;
            params.setMargins(margin, margin, margin, margin);
            card.setLayoutParams(params);

            TextView tv = new TextView(this);
            tv.setText(String.valueOf(number));
            tv.setTextSize(22f);
            tv.setTextColor(COLOR_DARK_GREEN);
            tv.setTypeface(Typeface.DEFAULT_BOLD);
            tv.setGravity(Gravity.CENTER);

            card.addView(tv);
            cells[i] = card;

            card.setOnClickListener(v -> {
                if (gameRunning) onCellTapped(index, number, card, tv);
            });

            schulteGrid.addView(card);
        }
    }

    private void markFound(CardView card, TextView tv) {
        card.setCardBackgroundColor(COLOR_LIGHT_GREEN);
        tv.setTextColor(COLOR_MILK);
        card.setEnabled(false);

        ObjectAnimator scaleX = ObjectAnimator.ofFloat(card, "scaleX", 1f, 1.15f, 1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(card, "scaleY", 1f, 1.15f, 1f);
        scaleX.setDuration(300);
        scaleY.setDuration(300);
        scaleX.setInterpolator(new OvershootInterpolator());
        scaleY.setInterpolator(new OvershootInterpolator());
        AnimatorSet set = new AnimatorSet();
        set.playTogether(scaleX, scaleY);
        set.start();
    }

    private void shakeCell(View view) {
        ObjectAnimator shake = ObjectAnimator.ofFloat(
                view, "translationX", 0f, -12f, 12f, -10f, 10f, -6f, 6f, 0f);
        shake.setDuration(400);
        shake.start();

        if (view instanceof CardView) {
            CardView card = (CardView) view;
            card.setCardBackgroundColor(COLOR_CARAMEL);
            new Handler(Looper.getMainLooper()).postDelayed(
                    () -> card.setCardBackgroundColor(COLOR_MILK), 350);
        }
    }

    private void startGame() {
        gameRunning = true;
        btnStartPause.setEnabled(false);
        btnStartPause.setText("In Progress");
        btnStartPause.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(0xFF9E9E9E));
        timerHandler.postDelayed(timerRunnable, 1000);
        scheduleHint();
    }

    private void resetGame() {
        clearHint();
        timerHandler.removeCallbacks(timerRunnable);
        gameRunning    = false;
        elapsedSeconds = 0;
        nextExpected   = 1;
        foundCount     = 0;
        currentHintIndex = -1;

        tvTimer.setText("00:00");
        tvCurrentNumber.setText("1");
        updateScoreDisplay();

        btnStartPause.setText("Start");
        btnStartPause.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(COLOR_DARK_GREEN));

        btnStartPause.setEnabled(true);
        buildGrid();
    }

    private void onGameComplete() {
        gameRunning = false;
        timerHandler.removeCallbacks(timerRunnable);
        clearHint();
        btnStartPause.setEnabled(false);

        btnStartPause.setText("Done ✓");
        btnStartPause.setBackgroundTintList(
                android.content.res.ColorStateList.valueOf(COLOR_CARAMEL));

        long mins = elapsedSeconds / 60;
        long secs = elapsedSeconds % 60;
        String message = String.format(Locale.getDefault(),
                "🎉 Completed in %02d:%02d!", mins, secs);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        saveBestScore(elapsedSeconds);
        saveLastSchulteSession();
        addCalmCoins(10);
    }

    private void saveBestScore(long completedSeconds) {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        String uid = user.getUid();
        DocumentReference userDoc = db.collection("users").document(uid);

        userDoc.get().addOnSuccessListener(snapshot -> {
            boolean shouldSave = true;

            if (snapshot.exists() && snapshot.contains("schulte_best_seconds")) {
                Long currentBest = snapshot.getLong("schulte_best_seconds");
                if (currentBest != null && currentBest <= completedSeconds) {
                    shouldSave = false;
                }
            }

            if (shouldSave) {
                Map<String, Object> data = new HashMap<>();
                data.put("schulte_best_seconds", completedSeconds);
                userDoc.set(data, SetOptions.merge())
                        .addOnSuccessListener(a ->
                                Toast.makeText(this, "🏆 New best: " + formatTime(completedSeconds), Toast.LENGTH_SHORT).show())
                        .addOnFailureListener(e ->
                                Toast.makeText(this, "Could not save score.", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private String formatTime(long seconds) {
        return String.format(Locale.getDefault(), "%02d:%02d", seconds / 60, seconds % 60);
    }

    private void updateTimerDisplay() {
        long mins = elapsedSeconds / 60;
        long secs = elapsedSeconds % 60;
        tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", mins, secs));
    }

    private void updateScoreDisplay() {
        tvScore.setText(foundCount + "/" + TOTAL_NUMBERS);
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }

    private void saveLastSchulteSession() {
        FirebaseUser user = auth.getCurrentUser();
        if (user == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("lastSchulteSession", System.currentTimeMillis());

        db.collection("users").document(user.getUid())
                .set(data, SetOptions.merge());
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
    @Override
    protected void onDestroy() {
        super.onDestroy();
        timerHandler.removeCallbacks(timerRunnable);
        hintHandler.removeCallbacks(hintRunnable);
    }
}