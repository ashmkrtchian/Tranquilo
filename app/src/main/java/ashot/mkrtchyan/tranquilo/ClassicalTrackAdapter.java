package ashot.mkrtchyan.tranquilo;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

class ClassicalTrackAdapter
        extends RecyclerView.Adapter<ClassicalTrackAdapter.TrackViewHolder> {

    interface OnTrackClickListener    { void onTrackClick(int position); }
    interface OnFavoriteClickListener { void onFavoriteClick(int position); }

    private final List<ClassicalTrack>    tracks;
    private final OnTrackClickListener    trackListener;
    private final OnFavoriteClickListener favListener;
    private int playingIndex = -1;

    ClassicalTrackAdapter(List<ClassicalTrack> tracks,
                          OnTrackClickListener trackListener,
                          OnFavoriteClickListener favListener) {
        this.tracks        = tracks;
        this.trackListener = trackListener;
        this.favListener   = favListener;
    }

    public void setPlayingIndex(int index) {
        int previous = playingIndex;
        playingIndex = index;
        if (previous >= 0 && previous < tracks.size())
            notifyItemChanged(previous);
        if (playingIndex >= 0 && playingIndex < tracks.size())
            notifyItemChanged(playingIndex);
    }

    @NonNull @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_classical_track, parent, false);
        return new TrackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder h, int position) {
        ClassicalTrack track = tracks.get(position);
        boolean isPlaying = position == playingIndex;

        h.tvTitle.setText(track.title);
        h.tvComposer.setText(track.composer);
        h.tvDuration.setText(track.duration);

        h.btnPlay.setImageResource(isPlaying ? R.drawable.stop2 : R.drawable.play);
        h.ivMusicNote.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
        h.ivPlaying.setVisibility(isPlaying ? View.VISIBLE : View.GONE);
        h.btnFavorite.setImageResource(
                track.isFavorite ? R.drawable.ic_heart_filled : R.drawable.ic_heart_outline
        );

        h.btnPlay.setOnClickListener(v -> {
            if (trackListener != null) trackListener.onTrackClick(h.getAdapterPosition());
        });
        h.itemView.setOnClickListener(v -> {
            if (trackListener != null) trackListener.onTrackClick(h.getAdapterPosition());
        });
        h.btnFavorite.setOnClickListener(v -> {
            if (favListener != null) favListener.onFavoriteClick(h.getAdapterPosition());
        });
    }

    @Override public int getItemCount() { return tracks.size(); }

    static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView  tvTitle, tvComposer, tvDuration;
        ImageView ivMusicNote, ivPlaying, btnPlay, btnFavorite;

        TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle     = itemView.findViewById(R.id.tvTitle);
            tvComposer  = itemView.findViewById(R.id.tvComposer);
            tvDuration  = itemView.findViewById(R.id.tvDuration);
            btnPlay     = itemView.findViewById(R.id.btnPlay);
            ivMusicNote = itemView.findViewById(R.id.ivMusicNote);
            ivPlaying   = itemView.findViewById(R.id.ivPlaying);
            btnFavorite = itemView.findViewById(R.id.btnFavorite);
        }
    }
}