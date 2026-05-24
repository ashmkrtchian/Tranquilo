package ashot.mkrtchyan.tranquilo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class Home extends AppCompatActivity {

    FirebaseAuth auth;
    FirebaseUser user;
    TextView navHome, navMood, navProfile;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        navHome = findViewById(R.id.nav_home);
        navMood = findViewById(R.id.nav_mood);
        navProfile = findViewById(R.id.nav_profile);

        auth = FirebaseAuth.getInstance();
        user = auth.getCurrentUser();
        if (user == null) {
            Intent i = new Intent(getApplicationContext(), Login.class);
            startActivity(i);
            finish();
        }

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SecondFragment())
                    .commit();
        }

        navHome.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new SecondFragment())
                    .commit();
        });

        navMood.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new LeaderboardFragment())
                    .commit();
        });

        navProfile.setOnClickListener(v -> {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, new ThirdFragment())
                    .commit();
        });
    }
}