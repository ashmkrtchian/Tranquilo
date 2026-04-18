package ashot.mkrtchyan.tranquilo;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MoodStreakManager {

    private static final String COLLECTION = "users";
    private static final String FIELD_STREAK = "streak";
    private static final String FIELD_LAST_MOOD = "lastMoodDate";

    private final FirebaseFirestore db;
    private final String userId;

    public interface StreakCallback {
        void onStreakUpdated(int newStreak);
        void onAlreadyDone();
        void onError();
    }

    public interface ResetCallback {
        void onChecked(int currentStreak);
        void onError();
    }

    public MoodStreakManager() {
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
    }
    public void checkAndResetStreakIfBroken(ResetCallback callback) {
        String today = getToday();
        String yesterday = getYesterday();

        db.collection(COLLECTION).document(userId).get()
                .addOnSuccessListener(doc -> {
                    String lastMoodDate = doc.getString(FIELD_LAST_MOOD);
                    Long currentStreak = doc.getLong(FIELD_STREAK);
                    int streak = currentStreak != null ? currentStreak.intValue() : 0;

                    boolean streakAlive = today.equals(lastMoodDate) || yesterday.equals(lastMoodDate);

                    if (streakAlive) {
                        callback.onChecked(streak);
                    } else {
                        Map<String, Object> data = new HashMap<>();
                        data.put(FIELD_STREAK, 0);
                        db.collection(COLLECTION).document(userId)
                                .update(data)
                                .addOnSuccessListener(v -> callback.onChecked(0))
                                .addOnFailureListener(e -> callback.onError());
                    }
                })
                .addOnFailureListener(e -> callback.onError());
    }

    public void recordMoodAndUpdateStreak(StreakCallback callback) {
        String today = getToday();

        db.collection(COLLECTION).document(userId).get()
                .addOnSuccessListener(doc -> {
                    String lastMoodDate = doc.getString(FIELD_LAST_MOOD);
                    Long currentStreak = doc.getLong(FIELD_STREAK);
                    int streak = currentStreak != null ? currentStreak.intValue() : 0;

                    if (today.equals(lastMoodDate)) {
                        callback.onAlreadyDone();
                        return;
                    }

                    int newStreak;
                    String yesterday = getYesterday();
                    if (yesterday.equals(lastMoodDate)) {
                        newStreak = streak + 1;
                    } else {
                        newStreak = 1;
                    }

                    Map<String, Object> data = new HashMap<>();
                    data.put(FIELD_LAST_MOOD, today);
                    data.put(FIELD_STREAK, newStreak);

                    db.collection(COLLECTION).document(userId)
                            .update(data)
                            .addOnSuccessListener(v -> callback.onStreakUpdated(newStreak))
                            .addOnFailureListener(e -> callback.onError());
                })
                .addOnFailureListener(e -> callback.onError());
    }

    private String getToday() {
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());
    }

    private String getYesterday() {
        long oneDayMs = 24 * 60 * 60 * 1000L;
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(new Date(System.currentTimeMillis() - oneDayMs));
    }
}