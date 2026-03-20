package ashot.mkrtchyan.tranquilo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class Music extends AppCompatActivity {


    FrameLayout rainCard;
    FrameLayout oceanCard;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music);

        rainCard = findViewById(R.id.rainCard);
        rainCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Rain.class);
                startActivity(i);
            }
        });

        oceanCard = findViewById(R.id.oceanCard);
        oceanCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Ocean.class);
                startActivity(i);
            }
        });

        FrameLayout fireCard = findViewById(R.id.fireCard);
        fireCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Fire.class);
                startActivity(i);
            }
        });

        FrameLayout classicalCard = findViewById(R.id.classicalCard);
        classicalCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Classical.class);
                startActivity(i);
            }
        });

        FrameLayout forestCard = findViewById(R.id.forestCard);
        forestCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), Forest.class);
                startActivity(i);
            }
        });


    }
}