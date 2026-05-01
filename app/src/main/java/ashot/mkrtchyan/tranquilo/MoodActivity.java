package ashot.mkrtchyan.tranquilo;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MoodActivity extends AppCompatActivity {

    private LinearLayout cardResult, cardStreak;
    private TextView tvSelectedMood, tvMoodTip, tvStreakInfo, tvStreakLabel;

    private final int[] buttonIds = {
            R.id.btn_happy, R.id.btn_calm, R.id.btn_anxious,
            R.id.btn_sad, R.id.btn_tired, R.id.btn_focused
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mood);

        cardResult    = findViewById(R.id.card_result);
        cardStreak    = findViewById(R.id.card_streak);
        tvSelectedMood = findViewById(R.id.tv_selected_mood);
        tvMoodTip      = findViewById(R.id.tv_mood_tip);
        tvStreakInfo   = findViewById(R.id.tv_streak_info);
        tvStreakLabel  = findViewById(R.id.tv_streak_label);

        findViewById(R.id.btn_happy).setOnClickListener(v ->
                onMoodSelected(v,
                        "You're feeling Happy",
                        "Great mood! Save this moment and boost your streak. Try a short breathing session to stay balanced."));

        findViewById(R.id.btn_calm).setOnClickListener(v ->
                onMoodSelected(v,
                        "You're feeling Calm",
                        "Perfect state. Start a focus session or Schulte table to use this clarity effectively."));

        findViewById(R.id.btn_anxious).setOnClickListener(v ->
                onMoodSelected(v,
                        "You're feeling Anxious",
                        "Open the breathing exercise and follow the rhythm. It will help slow your thoughts and relax."));

        findViewById(R.id.btn_sad).setOnClickListener(v ->
                onMoodSelected(v,
                        "You're feeling Sad",
                        "Try calming sounds or a gentle breathing session. Give yourself a few quiet minutes."));

        findViewById(R.id.btn_tired).setOnClickListener(v ->
                onMoodSelected(v,
                        "You're feeling Tired",
                        "Play ambient sounds and take a short reset. Even 5–10 minutes can recharge you."));

        findViewById(R.id.btn_focused).setOnClickListener(v ->
                onMoodSelected(v,
                        "You're feeling Focused",
                        "Perfect moment to go deep. Start a Schulte session and earn CalmCoins."));
    }

    private void onMoodSelected(View selected, String mood, String tip) {
        for (int id : buttonIds) {
            LinearLayout btn = findViewById(id);
            btn.setBackgroundResource(R.drawable.bg_stat_card);
            ((TextView) btn.getChildAt(0)).setTextColor(getColor(R.color.darkGreen));
            ((TextView) btn.getChildAt(1)).setTextColor(getColor(R.color.caramel));
        }

        LinearLayout selectedBtn = (LinearLayout) selected;
        selectedBtn.setBackgroundResource(R.drawable.bg_card_green);
        ((TextView) selectedBtn.getChildAt(0)).setTextColor(getColor(R.color.milk));
        ((TextView) selectedBtn.getChildAt(1)).setTextColor(getColor(R.color.lightBrown));

        tvSelectedMood.setText(mood);
        tvMoodTip.setText(tip);

        if (cardResult.getVisibility() != View.VISIBLE) {
            cardResult.setAlpha(0f);
            cardResult.setTranslationY(30f);
            cardResult.setVisibility(View.VISIBLE);
            cardResult.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(350)
                    .setInterpolator(new DecelerateInterpolator())
                    .start();
        }

        cardStreak.setVisibility(View.GONE);

        MoodStreakManager manager = new MoodStreakManager();
        manager.recordMoodAndUpdateStreak(new MoodStreakManager.StreakCallback() {
            @Override
            public void onStreakUpdated(int newStreak) {
                saveLastMoodSession();

                new Handler(Looper.getMainLooper()).postDelayed(() ->
                        showStreakAnimation(newStreak), 400);
            }

            @Override
            public void onAlreadyDone() {
                saveLastMoodSession();
                new Handler(Looper.getMainLooper()).postDelayed(() -> {
                    tvStreakInfo.setText("—");
                    tvStreakLabel.setText("Already tracked today");
                    animateCardAppear(cardStreak);
                }, 400);
            }

            @Override
            public void onError() {}
        });
    }

    private void showStreakAnimation(int newStreak) {
        tvStreakInfo.setText("0");
        tvStreakLabel.setText("day streak");
        cardStreak.setVisibility(View.VISIBLE);

        cardStreak.setScaleX(0f);
        cardStreak.setScaleY(0f);
        cardStreak.setAlpha(0f);
        cardStreak.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(500)
                .setInterpolator(new OvershootInterpolator(2f))
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        startCountUp(newStreak);
                    }
                })
                .start();
    }

    private void startCountUp(int target) {
        ValueAnimator animator = ValueAnimator.ofInt(0, target);
        animator.setDuration(Math.min(1200, target * 120L));
        animator.setInterpolator(new DecelerateInterpolator(1.5f));
        animator.addUpdateListener(a -> {
            int val = (int) a.getAnimatedValue();
            tvStreakInfo.setText(String.valueOf(val));
        });
        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                tvStreakInfo.setText(String.valueOf(target));
                triplePulse(tvStreakInfo, 0);
            }
        });
        animator.start();
    }

    private void triplePulse(View view, int count) {
        if (count >= 3) return;
        view.animate()
                .scaleX(1.35f)
                .scaleY(1.35f)
                .setDuration(130)
                .withEndAction(() ->
                        view.animate()
                                .scaleX(1f)
                                .scaleY(1f)
                                .setDuration(130)
                                .withEndAction(() ->
                                        new Handler(Looper.getMainLooper())
                                                .postDelayed(() -> triplePulse(view, count + 1), 60))
                                .start()
                ).start();
    }

    private void saveLastMoodSession() {
        com.google.firebase.auth.FirebaseUser user =
                com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("lastMoodSession", System.currentTimeMillis());

        com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .set(data, com.google.firebase.firestore.SetOptions.merge());
    }

    private void animateCardAppear(View view) {
        view.setScaleX(0f);
        view.setScaleY(0f);
        view.setAlpha(0f);
        view.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(400)
                .setInterpolator(new OvershootInterpolator(1.5f))
                .start();
    }
}