package ashot.mkrtchyan.tranquilo;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import java.util.Locale;

public class PoemAdapter extends RecyclerView.Adapter<PoemAdapter.PoemViewHolder> {

    private final List<Poem> poems;
    private MediaPlayer currentPlayer = null;
    private ImageButton currentPlayBtn = null;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean autoPlayEnabled = false;
    private int currentPlayingIndex = -1;
    private RecyclerView recyclerView;

    public PoemAdapter(List<Poem> poems) {
        this.poems = poems;
    }

    public void setAutoPlayEnabled(boolean enabled) {
        this.autoPlayEnabled = enabled;
    }

    public boolean isAutoPlayEnabled() {
        return autoPlayEnabled;
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @NonNull
    @Override
    public PoemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_poem, parent, false);
        return new PoemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PoemViewHolder holder, int position) {
        Poem poem = poems.get(position);
        holder.tvTitle.setText(poem.getTitle());
        holder.tvAuthor.setText("— " + poem.getAuthor());
        holder.seekBar.setProgress(0);
        holder.tvDuration.setText("0:00");
        holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_play);

        holder.btnPlayPause.setOnClickListener(v -> {
            int pos = holder.getAdapterPosition();
            if (pos == RecyclerView.NO_ID) return;
            playAt(pos, holder);
        });
    }

    private void playAt(int index, PoemViewHolder holder) {
        if (currentPlayer != null && currentPlayingIndex == index) {
            if (currentPlayer.isPlaying()) {
                currentPlayer.pause();
                holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
            } else {
                currentPlayer.start();
                holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                updateSeekBar(index, currentPlayer);
            }
            return;
        }

        stopCurrentPlayer();

        Poem poem = poems.get(index);
        currentPlayingIndex = index;

        MediaPlayer player = new MediaPlayer();
        try {
            player.setDataSource(poem.getAudioUrl());
            player.prepareAsync();

            currentPlayer = player;
            currentPlayBtn = holder.btnPlayPause;

            holder.seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    if (fromUser && currentPlayer != null) currentPlayer.seekTo(progress);
                }
                @Override public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override public void onStopTrackingTouch(SeekBar seekBar) {}
            });

            player.setOnPreparedListener(mp -> {
                if (mp != currentPlayer) { mp.release(); return; }
                mp.start();
                holder.seekBar.setMax(mp.getDuration());
                holder.tvDuration.setText(formatDuration(mp.getDuration()));
                holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_pause);
                updateSeekBar(index, mp);
            });

            player.setOnCompletionListener(mp -> {
                holder.btnPlayPause.setImageResource(android.R.drawable.ic_media_play);
                holder.seekBar.setProgress(0);
                handler.removeCallbacksAndMessages(null);
                currentPlayer = null;
                currentPlayBtn = null;
                currentPlayingIndex = -1;

                if (autoPlayEnabled && index + 1 < poems.size()) {
                    playNext(index + 1);
                }
            });

            player.setOnErrorListener((mp, what, extra) -> {
                stopCurrentPlayer();
                return true;
            });

        } catch (Exception e) {
            e.printStackTrace();
            stopCurrentPlayer();
        }
    }

    private void playNext(int nextIndex) {
        if (recyclerView == null) return;
        recyclerView.scrollToPosition(nextIndex);
        handler.postDelayed(() -> {
            PoemViewHolder nextHolder = (PoemViewHolder)
                    recyclerView.findViewHolderForAdapterPosition(nextIndex);
            if (nextHolder != null) {
                playAt(nextIndex, nextHolder);
            }
        }, 300);
    }

    private void stopCurrentPlayer() {
        handler.removeCallbacksAndMessages(null);
        if (currentPlayer != null) {
            try {
                currentPlayer.stop();
                currentPlayer.release();
            } catch (Exception ignored) {}
            currentPlayer = null;
        }
        if (currentPlayBtn != null) {
            currentPlayBtn.setImageResource(android.R.drawable.ic_media_play);
            currentPlayBtn = null;
        }
        currentPlayingIndex = -1;
    }

    private void updateSeekBar(int index, MediaPlayer player) {
        if (player == null || player != currentPlayer) return;

        if (recyclerView != null) {
            PoemViewHolder holder = (PoemViewHolder)
                    recyclerView.findViewHolderForAdapterPosition(index);
            if (holder != null) {
                try {
                    if (player.isPlaying()) {
                        holder.seekBar.setProgress(player.getCurrentPosition());
                    }
                } catch (IllegalStateException ignored) {
                    return;
                }
            }
        }
        handler.postDelayed(() -> updateSeekBar(index, player), 500);
    }

    private String formatDuration(int ms) {
        int secs = ms / 1000;
        return String.format(Locale.getDefault(), "%d:%02d", secs / 60, secs % 60);
    }

    @Override
    public int getItemCount() { return poems.size(); }

    public void releasePlayer() {
        stopCurrentPlayer();
    }

    static class PoemViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvAuthor, tvDuration;
        ImageButton btnPlayPause;
        SeekBar seekBar;

        PoemViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle      = itemView.findViewById(R.id.tvPoemTitle);
            tvAuthor     = itemView.findViewById(R.id.tvPoemAuthor);
            tvDuration   = itemView.findViewById(R.id.tvDuration);
            btnPlayPause = itemView.findViewById(R.id.btnPlayPause);
            seekBar      = itemView.findViewById(R.id.seekBar);
        }
    }
}