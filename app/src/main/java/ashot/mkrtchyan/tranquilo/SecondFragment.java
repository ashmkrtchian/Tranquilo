package ashot.mkrtchyan.tranquilo;

import android.content.Intent;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class SecondFragment extends Fragment {

    CardView breath, podcast, schulteTable, music, moodTracking, poetryLabel;

    public SecondFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_second, container, false);

        music = view.findViewById(R.id.music);
        music.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), Music.class);
                startActivity(i);
            }
        });

        podcast = view.findViewById(R.id.podcast);
        podcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), Podcast.class);
                startActivity(i);
            }
        });

        breath = view.findViewById(R.id.breath);
        breath.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), Breath.class);
                startActivity(i);
            }
        });

        schulteTable = view.findViewById(R.id.schulteTable);
        schulteTable.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), SchulteTableActivity.class);
                startActivity(i);
            }
        });

        moodTracking = view.findViewById(R.id.moodTracking);
        moodTracking.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), MoodActivity.class);
                startActivity(i);
            }
        });

        poetryLabel = view.findViewById(R.id.poetryLabel);
        poetryLabel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getContext(), PoemsActivity.class);
                startActivity(i);
            }
        });

        return view;
    }
}