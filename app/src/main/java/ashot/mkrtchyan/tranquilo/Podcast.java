package ashot.mkrtchyan.tranquilo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Podcast extends AppCompatActivity {

    private static final String TAG = "Podcast";

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String mood = "";
    TextView tvMoodTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast);

        tvMoodTitle = findViewById(R.id.tvMoodTitle);

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            tvMoodTitle.setText("Please log in to see podcasts.");
            Log.e(TAG, "No authenticated user found.");
            return;
        }

        loadMood(currentUser.getUid());
    }

    private void loadMood(String uid) {
        db.collection("users").document(uid).get()
                .addOnSuccessListener(doc -> {
                    String raw = doc.getString("lastMood");
                    mood = (raw != null) ? raw.toLowerCase().trim() : "calm";
                    tvMoodTitle.setText("Podcasts for your current mood: " + mood);
                    loadSuggestedPodcasts();
                    loadAllPodcasts();
                })
                .addOnFailureListener(e -> {
                    tvMoodTitle.setText("Could not load your mood.");
                    Log.e(TAG, "Failed to fetch user mood", e);
                });
    }

    private void loadSuggestedPodcasts() {
        CardView cardSuggested = findViewById(R.id.cardSuggested);
        TextView tvSuggested = findViewById(R.id.tvSuggested);

        db.collection("podcasts").whereEqualTo("mood", mood).get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) {
                        cardSuggested.setVisibility(View.GONE);
                        TextView tvNoPodcasts = findViewById(R.id.tvNoPodcasts);
                        if (tvNoPodcasts != null) tvNoPodcasts.setVisibility(View.VISIBLE);
                        return;
                    }

                    int randomIndex = new Random().nextInt(q.size());
                    QueryDocumentSnapshot first = (QueryDocumentSnapshot) q.getDocuments().get(randomIndex);
                    String title = first.getString("title");
                    String url = first.getString("url");

                    if (title != null && url != null) {
                        tvSuggested.setText(title);
                        cardSuggested.setOnClickListener(v -> openLink(url));
                        cardSuggested.setVisibility(View.VISIBLE);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch suggested podcast", e);
                    cardSuggested.setVisibility(View.GONE);
                });
    }

    private void loadAllPodcasts() {
        CardView cardFeatured = findViewById(R.id.cardFeatured);
        TextView tvFeatured = findViewById(R.id.tvFeatured);
        LinearLayout container = findViewById(R.id.podcastContainer);

        db.collection("podcasts").get()
                .addOnSuccessListener(q -> {
                    if (q.isEmpty()) return;

                    List<QueryDocumentSnapshot> all = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : q) {
                        if (doc.getString("title") != null && doc.getString("url") != null) {
                            all.add(doc);
                        }
                    }

                    if (all.isEmpty()) return;

                    int randomIndex = new Random().nextInt(all.size());
                    QueryDocumentSnapshot featured = all.get(randomIndex);
                    String featuredTitle = featured.getString("title");
                    String featuredUrl = featured.getString("url");

                    tvFeatured.setText(featuredTitle);
                    cardFeatured.setOnClickListener(v -> openLink(featuredUrl));
                    cardFeatured.setVisibility(View.VISIBLE);

                    container.removeAllViews();
                    for (QueryDocumentSnapshot doc : all) {
                        addCard(container, doc.getString("title"), doc.getString("url"));
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch all podcasts", e);
                    cardFeatured.setVisibility(View.GONE);
                });
    }

    private void addCard(LinearLayout container, String title, String url) {
        CardView card = new CardView(this);
        card.setRadius(22f);
        card.setCardElevation(6f);
        card.setUseCompatPadding(true);
        card.setCardBackgroundColor(getResources().getColor(R.color.milk));

        LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        p.setMargins(0, 0, 0, 16);
        card.setLayoutParams(p);

        TextView tv = new TextView(this);
        tv.setText(title);
        tv.setTextSize(16f);
        tv.setTextColor(getResources().getColor(R.color.darkGreen));
        tv.setPadding(32, 28, 32, 28);
        card.addView(tv);

        card.setOnClickListener(v -> openLink(url));
        container.addView(card);
    }

    void openLink(String url) {
        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}