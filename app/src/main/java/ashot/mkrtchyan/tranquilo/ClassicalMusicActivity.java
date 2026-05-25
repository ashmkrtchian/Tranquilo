package ashot.mkrtchyan.tranquilo;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class ClassicalMusicActivity extends AppCompatActivity
        implements ClassicalTrackAdapter.OnTrackClickListener,
        ClassicalTrackAdapter.OnFavoriteClickListener {

    private RecyclerView rvTracks;
    private TextView tvListLabel;
    private ImageButton btnBack;
    private LinearLayout chipGroup;
    private EditText etSearch;

    private String activeCategory = "All";
    private ClassicalTrackAdapter adapter;

    private final List<ClassicalTrack> allTracks      = new ArrayList<>();
    private final List<ClassicalTrack> filteredTracks  = new ArrayList<>();
    private final List<ClassicalTrack> favoriteTracks  = new ArrayList<>();

    private MediaPlayer mediaPlayer;
    private int     currentIndex    = -1;
    private boolean isPlaying       = false;
    private boolean showingFavorites = false;
    private String  currentTrackId  = null;

    private FirebaseFirestore db;
    private String userId;

    private static final String[] CATEGORIES = {
            "All", "Favorites", "Bach", "Beethoven", "Chopin",
            "Debussy", "Mozart", "Vivaldi", "Satie", "Tchaikovsky"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classical_music);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        bindViews();
        setupSearch();
        setupChips();
        fetchAllTracks();
    }

    private void bindViews() {
        etSearch   = findViewById(R.id.etSearch);
        rvTracks   = findViewById(R.id.rvTracks);
        tvListLabel = findViewById(R.id.tvListLabel);
        btnBack    = findViewById(R.id.btnBack);
        chipGroup  = findViewById(R.id.chipGroup);

        btnBack.setOnClickListener(v -> finish());
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(android.text.Editable s) {
                filterTracks(s.toString().trim().toLowerCase());
            }
        });
    }

    private void filterTracks(String query) {
        filteredTracks.clear();

        List<ClassicalTrack> source;
        if (activeCategory.equals("Favorites")) {
            source = favoriteTracks;
        } else if (activeCategory.equals("All")) {
            source = allTracks;
        } else {
            source = new ArrayList<>();
            for (ClassicalTrack t : allTracks) {
                if (activeCategory.equalsIgnoreCase(t.composer)) source.add(t);
            }
        }

        if (query.isEmpty()) {
            filteredTracks.addAll(source);
        } else {
            for (ClassicalTrack t : source) {
                if (t.title.toLowerCase().contains(query)
                        || t.composer.toLowerCase().contains(query)) {
                    filteredTracks.add(t);
                }
            }
        }

        if (adapter != null) {
            adapter.notifyDataSetChanged();

            boolean actuallyPlaying = mediaPlayer != null && mediaPlayer.isPlaying();
            int newIndex = -1;
            if (actuallyPlaying) {
                for (int i = 0; i < filteredTracks.size(); i++) {
                    if (filteredTracks.get(i).id.equals(currentTrackId)) { newIndex = i; break; }
                }
            }
            adapter.setPlayingIndex(newIndex);
        }
    }

    private void fetchAllTracks() {
        tvListLabel.setText("Loading…");
        db.collection("classical_tracks").get()
                .addOnSuccessListener(snapshot -> {
                    allTracks.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        allTracks.add(new ClassicalTrack(
                                doc.getId(),
                                doc.getString("title"),
                                doc.getString("composer"),
                                doc.getString("audioUrl")
                        ));
                    }
                    if (userId != null) fetchFavoritesAndMerge(() -> showCategory("All"));
                    else showCategory("All");
                })
                .addOnFailureListener(e -> {
                    tvListLabel.setText("Failed to load");
                    Toast.makeText(this, "Could not load tracks", Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchFavoritesAndMerge(Runnable onDone) {
        db.collection("user_favorites").document(userId).collection("tracks").get()
                .addOnSuccessListener(snapshot -> {
                    List<String> favoriteIds = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) favoriteIds.add(doc.getId());

                    favoriteTracks.clear();
                    for (ClassicalTrack t : allTracks) {
                        t.isFavorite = favoriteIds.contains(t.id);
                        if (t.isFavorite) favoriteTracks.add(t);
                    }
                    onDone.run();
                })
                .addOnFailureListener(e -> onDone.run());
    }

    @Override
    public void onFavoriteClick(int position) {
        if (userId == null) {
            Toast.makeText(this, "Sign in to save favorites", Toast.LENGTH_SHORT).show();
            return;
        }

        ClassicalTrack track = filteredTracks.get(position);
        track.isFavorite = !track.isFavorite;

        var favRef = db.collection("user_favorites")
                .document(userId).collection("tracks").document(track.id);

        if (track.isFavorite) {
            java.util.Map<String, Object> data = new java.util.HashMap<>();
            data.put("title", track.title);
            data.put("composer", track.composer);
            favRef.set(data);
            if (!favoriteTracks.contains(track)) favoriteTracks.add(track);
        } else {
            favRef.delete();
            favoriteTracks.remove(track);
        }

        adapter.notifyItemChanged(position);
        if (showingFavorites && !track.isFavorite) {
            filteredTracks.remove(position);
            adapter.notifyItemRemoved(position);
        }
    }

    private void showCategory(String category) {
        activeCategory   = category;
        showingFavorites = category.equals("Favorites");

        if (category.equals("All"))            tvListLabel.setText("Featured Pieces");
        else if (category.equals("Favorites")) tvListLabel.setText("Your Favorites");
        else                                   tvListLabel.setText(category);

        filterTracks(etSearch.getText().toString().trim().toLowerCase());

        if (adapter == null) {
            adapter = new ClassicalTrackAdapter(filteredTracks, this, this);
            rvTracks.setLayoutManager(new LinearLayoutManager(this));
            rvTracks.setAdapter(adapter);
        }
    }

    private void setupChips() {
        for (String cat : CATEGORIES) {
            TextView chip = new TextView(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMarginEnd(8);
            chip.setLayoutParams(lp);
            chip.setText(cat);
            chip.setTextSize(12f);
            chip.setPaddingRelative(22, 8, 22, 8);
            styleChip(chip, cat.equals("All"));
            chip.setOnClickListener(v -> {
                resetChipSelection();
                styleChip(chip, true);
                showCategory(cat);
            });
            chipGroup.addView(chip);
        }
    }

    private void styleChip(TextView chip, boolean selected) {
        chip.setBackgroundResource(selected ? R.drawable.bg_chip_selected : R.drawable.bg_chip);
        chip.setTextColor(ContextCompat.getColor(this, R.color.milk));
    }

    private void resetChipSelection() {
        for (int i = 0; i < chipGroup.getChildCount(); i++) {
            styleChip((TextView) chipGroup.getChildAt(i), false);
        }
    }

    @Override
    public void onTrackClick(int position) {
        if (filteredTracks.get(position).id.equals(currentTrackId)) {
            togglePlayPause(position);
        } else {
            playTrack(position);
        }
    }

    public void playTrack(int index) {
        if (filteredTracks.isEmpty()) return;
        if (index < 0) index = filteredTracks.size() - 1;
        if (index >= filteredTracks.size()) index = 0;

        currentIndex   = index;
        currentTrackId = filteredTracks.get(currentIndex).id;

        ClassicalTrack track = filteredTracks.get(currentIndex);

        isPlaying = true;

        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        try {
            mediaPlayer.setDataSource(track.audioUrl);
            mediaPlayer.prepareAsync();

            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                if (adapter != null) adapter.setPlayingIndex(currentIndex);
                int secs = mp.getDuration() / 1000;
                track.duration = (secs / 60) + ":" + String.format("%02d", secs % 60);
                if (adapter != null) adapter.notifyItemChanged(currentIndex);
            });

            mediaPlayer.setOnCompletionListener(mp -> playTrack(currentIndex + 1));

            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                runOnUiThread(() ->
                        Toast.makeText(this, "Could not play track", Toast.LENGTH_SHORT).show());
                return true;
            });

        } catch (Exception e) {
            Toast.makeText(this, "Error loading track", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlayPause(int position) {
        if (mediaPlayer == null) return;

        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            isPlaying = false;
            if (adapter != null) adapter.setPlayingIndex(-1);
        } else {
            mediaPlayer.start();
            isPlaying = true;
            if (adapter != null) adapter.setPlayingIndex(position);
        }
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            try { mediaPlayer.stop(); } catch (Exception ignored) {}
            mediaPlayer.release();
            mediaPlayer = null;
        }
        super.onDestroy();
    }
}