package ashot.mkrtchyan.tranquilo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Podcast extends AppCompatActivity {

    TextView podcastName;
    Button listenButton;
    LinearLayout famousPodcastsList;

    // Podcast of the Day
    String[][] podcasts = {
            {"The Daily - New York Times", "https://www.youtube.com/watch?v=someid1"},
            {"TED Talks Daily", "https://www.youtube.com/watch?v=someid2"},
            {"The Joe Rogan Experience", "https://www.youtube.com/watch?v=someid3"},
            {"Science Vs", "https://www.youtube.com/watch?v=someid4"}
    };

    // Famous podcasts (add as needed)
    String[][] famousPodcasts = {
            {"Stuff You Should Know", "https://www.youtube.com/watch?v=fyJ1"},
            {"The Michelle Obama Podcast", "https://www.youtube.com/watch?v=fyJ2"},
            {"How I Built This", "https://www.youtube.com/watch?v=fyJ3"}
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast);

        podcastName = findViewById(R.id.podcastName);
        listenButton = findViewById(R.id.listenButton);
        famousPodcastsList = findViewById(R.id.famousPodcastsList);

        // Podcast of the Day - choose random
        int index = (int) (Math.random() * podcasts.length);
        podcastName.setText(podcasts[index][0]);
        String url = podcasts[index][1];

        listenButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            startActivity(intent);
        });

        // Populate famous podcasts
        for(String[] p : famousPodcasts){
            LinearLayout item = new LinearLayout(this);
            item.setOrientation(LinearLayout.VERTICAL);
            item.setPadding(20,20,20,20);

            TextView name = new TextView(this);
            name.setText(p[0]);
            name.setTextSize(18f);
            name.setTextColor(getResources().getColor(R.color.darkGreen));
            name.setPadding(0,0,0,10);

            Button openBtn = new Button(this);
            openBtn.setText("Listen on YouTube");
            openBtn.setTextColor(getResources().getColor(R.color.milk));
            openBtn.setBackgroundColor(getResources().getColor(R.color.lightBrown));
            String podcastUrl = p[1];
            openBtn.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(podcastUrl));
                startActivity(intent);
            });

            item.addView(name);
            item.addView(openBtn);
            famousPodcastsList.addView(item);
        }

    }
}