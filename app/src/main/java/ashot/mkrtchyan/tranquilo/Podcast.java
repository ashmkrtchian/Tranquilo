package ashot.mkrtchyan.tranquilo;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import java.util.ArrayList;
import java.util.Random;

public class Podcast extends AppCompatActivity {

    ArrayList<String[]> podcasts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_podcast);

        LinearLayout container = findViewById(R.id.podcastContainer);
        TextView tvSuggested = findViewById(R.id.tvSuggested);
        CardView cardSuggested = findViewById(R.id.cardSuggested);
        podcasts.add(new String[]{"Reduce Anxiety Naturally",        "https://www.youtube.com/watch?v=MIr3RsUWrdo"});
        podcasts.add(new String[]{"Stop Overthinking",               "https://www.youtube.com/watch?v=9YRjX3A_8cM"});
        podcasts.add(new String[]{"Guided Meditation for Anxiety",   "https://www.youtube.com/watch?v=O-6f5wQXSu8"});
        podcasts.add(new String[]{"Calm Your Mind",                  "https://www.youtube.com/watch?v=ZToicYcHIOU"});
        podcasts.add(new String[]{"Breathing Exercise for Stress",   "https://www.youtube.com/watch?v=SEfs5TJZ6Nk"});
        podcasts.add(new String[]{"How to Handle Anxiety",           "https://www.youtube.com/watch?v=WWloIAQpMcQ"});
        podcasts.add(new String[]{"Sleep Meditation for Anxiety",    "https://www.youtube.com/watch?v=1vx8iUvfyCY"});
        podcasts.add(new String[]{"Relaxation Talk",                 "https://www.youtube.com/watch?v=inpok4MKVLM"});
        podcasts.add(new String[]{"Morning Calm Podcast",            "https://www.youtube.com/watch?v=UfcAVejslrU"});
        podcasts.add(new String[]{"Let Go of Stress",                "https://www.youtube.com/watch?v=xRxT9cOKiM8"});
        podcasts.add(new String[]{"Anxiety Relief Talk",             "https://www.youtube.com/watch?v=5zhnLG3GW-8"});
        podcasts.add(new String[]{"Mindfulness Meditation",          "https://www.youtube.com/watch?v=6p_yaNFSYao"});
        podcasts.add(new String[]{"Healing Anxiety Podcast",         "https://www.youtube.com/watch?v=2Z9bZ8vO0jM"});
        podcasts.add(new String[]{"Deep Relaxation",                 "https://www.youtube.com/watch?v=odADwWzHR24"});
        podcasts.add(new String[]{"Calm Breathing Guide",            "https://www.youtube.com/watch?v=nmFUDkj1Aq0"});
        podcasts.add(new String[]{"Stress Management Tips",          "https://www.youtube.com/watch?v=hnpQrMqDoqE"});
        podcasts.add(new String[]{"Reduce Panic Attacks",            "https://www.youtube.com/watch?v=MFxlK1ZvOmA"});
        podcasts.add(new String[]{"Mental Health Talk",              "https://www.youtube.com/watch?v=DxIDKZHW3-E"});
        podcasts.add(new String[]{"Relaxing Voice Podcast",          "https://www.youtube.com/watch?v=4EaMJOo1jks"});
        podcasts.add(new String[]{"Overcome Anxiety Fast",           "https://www.youtube.com/watch?v=EU5A2k7dR4c"});
        podcasts.add(new String[]{"Inner Peace Session",             "https://www.youtube.com/watch?v=Z7oYJZg9nOA"});
        podcasts.add(new String[]{"Calm Thoughts Podcast",           "https://www.youtube.com/watch?v=9qR7uwkblbs"});
        podcasts.add(new String[]{"Anxiety & Overthinking Relief",   "https://www.youtube.com/watch?v=QnHgPgB45MQ"});
        podcasts.add(new String[]{"Total Body Stress Release",       "https://www.youtube.com/watch?v=H_uc-uQ3Nkc"});
        podcasts.add(new String[]{"Meditation for Beginners",        "https://www.youtube.com/watch?v=4OV4REpHKqw"});
        podcasts.add(new String[]{"Ease Worry & Overthinking",       "https://www.youtube.com/watch?v=xoYnqvadurg"});
        podcasts.add(new String[]{"Headspace: Stress & Anxiety",     "https://www.youtube.com/watch?v=tuPW7oOudVc"});
        podcasts.add(new String[]{"Meditation for Fear & Anxiety",   "https://www.youtube.com/watch?v=s41tJnY7ZbM"});
        podcasts.add(new String[]{"Deep Relaxation & Stress Relief", "https://www.youtube.com/watch?v=g3tQDV0f3V8"});
        podcasts.add(new String[]{"Calm & Ease with Adriene",        "https://www.youtube.com/watch?v=xqIso01-VMM"});
        podcasts.add(new String[]{"Michael Sealey: Anxiety Relief",  "https://www.youtube.com/watch?v=1ZYbU82GVz4"});
        podcasts.add(new String[]{"RAIN: Working With Anxiety",      "https://www.youtube.com/watch?v=KHQdmSU9QZU"});

        Random random = new Random();
        int index = random.nextInt(podcasts.size());

        String[] suggested = podcasts.get(index);
        tvSuggested.setText(suggested[0]);

        cardSuggested.setOnClickListener(v -> {
            openLink(suggested[1]);
        });

        for (String[] podcast : podcasts) {

            CardView card = new CardView(this);
            card.setRadius(22f);
            card.setCardElevation(6f);
            card.setCardBackgroundColor(getResources().getColor(R.color.milk));
            card.setUseCompatPadding(true);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 16);
            card.setLayoutParams(params);

            TextView tv = new TextView(this);
            tv.setText(podcast[0]);
            tv.setTextSize(16f);
            tv.setTextColor(getResources().getColor(R.color.darkGreen));
            tv.setPadding(32, 28, 32, 28);

            card.addView(tv);

            card.setOnClickListener(v -> openLink(podcast[1]));

            container.addView(card);
        }
    }

    void openLink(String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}