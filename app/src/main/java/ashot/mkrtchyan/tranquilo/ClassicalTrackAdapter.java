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

    interface OnTrackClickListener {
        void onTrackClick(int position);
    }

    private final List<ClassicalTrack>  tracks;
    private final OnTrackClickListener  listener;

    ClassicalTrackAdapter(List<ClassicalTrack> tracks, OnTrackClickListener listener) {
        this.tracks   = tracks;
        this.listener = listener;
    }


    @NonNull
    @Override
    public TrackViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_classical_track, parent, false);
        return new TrackViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull TrackViewHolder h, int position) {
        ClassicalTrack track    = tracks.get(position);

        h.tvTitle.setText(track.title);
        h.tvComposer.setText(track.composer);
        h.tvDuration.setText(track.duration);

        h.btnPlay.setOnClickListener(v -> {
            if (listener != null) listener.onTrackClick(h.getAdapterPosition());
        });
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onTrackClick(h.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() { return tracks.size(); }

    // ── ViewHolder ─────────────────────────────────────────────────────────
    static class TrackViewHolder extends RecyclerView.ViewHolder {
        TextView    tvTitle, tvComposer, tvDuration;
        ImageView   ivMusicNote, btnPlay;

        TrackViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle     = itemView.findViewById(R.id.tvTitle);
            tvComposer  = itemView.findViewById(R.id.tvComposer);
            tvDuration  = itemView.findViewById(R.id.tvDuration);
            btnPlay     = itemView.findViewById(R.id.btnPlay);
            ivMusicNote = itemView.findViewById(R.id.ivMusicNote);
        }
    }
}
