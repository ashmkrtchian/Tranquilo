package ashot.mkrtchyan.tranquilo;

import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClassicalMusicActivity extends AppCompatActivity
        implements ClassicalTrackAdapter.OnTrackClickListener {

    private static final String CLIENT_ID = "096994f4";

    private EditText etSearch;
    private RecyclerView rvTracks;
    private TextView tvListLabel, tvNowPlayingTitle, tvNowPlayingComposer;
    private ImageButton btnBack;
    private ImageView btnPlayPause, btnPrev, btnNext;
    private LinearLayout chipGroup;
    private View cardNowPlaying;

    private ClassicalTrackAdapter adapter;
    private final List<ClassicalTrack> allTracks      = new ArrayList<>();
    private final List<ClassicalTrack> filteredTracks = new ArrayList<>();

    private MediaPlayer mediaPlayer;
    private int currentIndex = -1;
    private boolean isPlaying = false;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private static final String[] CATEGORIES = {
            "All", "Bach", "Beethoven", "Chopin", "Debussy",
            "Mozart", "Vivaldi", "Satie", "Tchaikovsky"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_classical_music);
        bindViews();
        setupChips();
        setupSearch();
        fetchTracks("");
    }

    private void bindViews() {
        etSearch             = findViewById(R.id.etSearch);
        rvTracks             = findViewById(R.id.rvTracks);
        cardNowPlaying       = findViewById(R.id.cardNowPlaying);
        tvNowPlayingTitle    = findViewById(R.id.tvNowPlayingTitle);
        tvNowPlayingComposer = findViewById(R.id.tvNowPlayingComposer);
        tvListLabel          = findViewById(R.id.tvListLabel);
        btnBack              = findViewById(R.id.btnBack);
        btnPlayPause         = findViewById(R.id.btnPlayPause);
        btnPrev              = findViewById(R.id.btnPrev);
        btnNext              = findViewById(R.id.btnNext);
        chipGroup            = findViewById(R.id.chipGroup);

        btnBack.setOnClickListener(v -> finish());
        btnPlayPause.setOnClickListener(v -> togglePlayPause());
        btnPrev.setOnClickListener(v -> playTrack(currentIndex - 1));
        btnNext.setOnClickListener(v -> playTrack(currentIndex + 1));
    }

    private void fetchTracks(String query) {
        tvListLabel.setText("Loading…");
        executor.execute(() -> {
            try {
                String urlStr = "https://api.jamendo.com/v3.0/tracks/"
                        + "?client_id=" + CLIENT_ID
                        + "&format=json"
                        + "&limit=30"
                        + "&tags=classical+instrumental"
                        + "&search=" + query.replace(" ", "+")
                        + "&audioformat=mp32"
                        + "&include=musicinfo"
                        + "&order=popularity_total";

                HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
                conn.setRequestMethod("GET");

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) sb.append(line);
                reader.close();

                JSONObject root    = new JSONObject(sb.toString());
                JSONArray  results = root.getJSONArray("results");

                allTracks.clear();
                for (int i = 0; i < results.length(); i++) {
                    JSONObject obj = results.getJSONObject(i);
                    String title    = obj.getString("name");
                    //String artist   = obj.getString("artist_name");
                    String artistName = obj.getString("artist_name");
                    String composer = "Classical";
                    for (String cat : CATEGORIES) {
                        if (!cat.equals("All") && artistName.toLowerCase().contains(cat.toLowerCase())) {
                            composer = cat;
                            break;
                        }
                        if (!cat.equals("All") && query.toLowerCase().contains(cat.toLowerCase())) {
                            composer = cat;
                            break;
                        }
                    }
                    String duration = formatDuration(obj.getInt("duration"));
                    String audioUrl = obj.getString("audio");

                    //allTracks.add(new ClassicalTrack(title, artist, duration, audioUrl, "classical"));
                    allTracks.add(new ClassicalTrack(title, composer, duration, audioUrl, "classical"));
                }

                runOnUiThread(() -> {
                    filteredTracks.clear();
                    filteredTracks.addAll(allTracks);
                    if (adapter == null) {
                        adapter = new ClassicalTrackAdapter(filteredTracks, this);
                        rvTracks.setLayoutManager(new LinearLayoutManager(this));
                        rvTracks.setAdapter(adapter);
                    } else {
                        adapter.notifyDataSetChanged();
                    }
                    tvListLabel.setText(allTracks.isEmpty() ? "No results" : "Featured Pieces");
                });

            } catch (Exception e) {
                runOnUiThread(() -> {
                    tvListLabel.setText("Failed to load");
                    Toast.makeText(this, "Check internet connection", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private String formatDuration(int seconds) {
        return (seconds / 60) + ":" + String.format("%02d", seconds % 60);
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int i, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int i, int b, int c) {
                String q = s.toString().trim();
                if (q.isEmpty()) {
                    filteredTracks.clear();
                    filteredTracks.addAll(allTracks);
                    tvListLabel.setText("Featured Pieces");
                    if (adapter != null) adapter.notifyDataSetChanged();
                } else {
                    fetchTracks(q);
                }
            }
        });
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
                etSearch.setText("");
                fetchTracks(cat.equals("All") ? "classical" : cat);
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
        playTrack(position);
    }

    public void playTrack(int index) {
        if (filteredTracks.isEmpty()) return;
        if (index < 0) index = filteredTracks.size() - 1;
        if (index >= filteredTracks.size()) index = 0;

        currentIndex = index;
        ClassicalTrack track = filteredTracks.get(currentIndex);

        cardNowPlaying.setVisibility(View.VISIBLE);
        tvNowPlayingTitle.setText(track.title);
        tvNowPlayingComposer.setText(track.composer);
        isPlaying = true;
        btnPlayPause.setImageResource(R.drawable.stop2);

        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }

        mediaPlayer = new MediaPlayer();
        mediaPlayer.setAudioAttributes(new AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build());

        try {
            mediaPlayer.setDataSource(track.youtubeId);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(mp -> mp.start());
            mediaPlayer.setOnCompletionListener(mp -> playTrack(currentIndex + 1));
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                runOnUiThread(() -> Toast.makeText(this, "Could not play track", Toast.LENGTH_SHORT).show());
                return true;
            });
        } catch (Exception e) {
            Toast.makeText(this, "Error loading track", Toast.LENGTH_SHORT).show();
        }
    }

    private void togglePlayPause() {
        if (mediaPlayer == null) return;
        if (isPlaying) {
            mediaPlayer.pause();
            btnPlayPause.setImageResource(R.drawable.play);
        } else {
            mediaPlayer.start();
            btnPlayPause.setImageResource(R.drawable.stop2);
        }
        isPlaying = !isPlaying;
    }

    @Override
    protected void onDestroy() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
        }
        executor.shutdown();
        super.onDestroy();
    }
}