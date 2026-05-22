package ashot.mkrtchyan.tranquilo;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

public class ThirdFragment extends Fragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private String mParam1;
    private String mParam2;

    public ThirdFragment() {}

    public static ThirdFragment newInstance(String param1, String param2) {
        ThirdFragment fragment = new ThirdFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    private void loadUserData(View view) {
        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvSchulteBest = view.findViewById(R.id.tvSchulteBest);
        TextView tvCalmCoins = view.findViewById(R.id.tvCalmCoins);
        ImageView imgAvatar = view.findViewById(R.id.imgAvatar);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            String name = snapshot.getString("name");
                            if (name != null) tvName.setText(name);

                            String profileKey = snapshot.getString("profilePicture");
                            imgAvatar.setImageResource(R.drawable.man);
                            if (profileKey != null) {
                                switch (profileKey) {
                                    case "woman":
                                        imgAvatar.setImageResource(R.drawable.woman);
                                        break;
                                    case "dog":
                                        imgAvatar.setImageResource(R.drawable.dog);
                                        break;
                                    case "cat":
                                        imgAvatar.setImageResource(R.drawable.cat);
                                        break;
                                    default:
                                        imgAvatar.setImageResource(R.drawable.man);
                                        break;
                                }
                            }

                            if (snapshot.contains("schulte_best_seconds")) {
                                Long best = snapshot.getLong("schulte_best_seconds");
                                if (best != null)
                                    tvSchulteBest.setText(String.format(Locale.getDefault(), "%02d:%02d", best / 60, best % 60));
                            }

                            if (snapshot.contains("calmCoins")) {
                                Long coins = snapshot.getLong("calmCoins");
                                if (coins != null) tvCalmCoins.setText(coins.toString());
                            }

                            if (snapshot.contains("lastSchulteSession")) {
                                Long lastSession = snapshot.getLong("lastSchulteSession");
                                TextView tvSchulteSubtitle = view.findViewById(R.id.tvSchulteSubtitle);
                                if (lastSession != null && tvSchulteSubtitle != null) {
                                    tvSchulteSubtitle.setText("Last session: " + formatLastSession(lastSession));
                                }
                            }

                            if (snapshot.contains("lastMoodSession")) {
                                Long lastSession = snapshot.getLong("lastMoodSession");
                                TextView tvMoodSubtitle = view.findViewById(R.id.tvMoodSubtitle);
                                if (lastSession != null && tvMoodSubtitle != null) {
                                    tvMoodSubtitle.setText("Last tracked: " + formatLastSession(lastSession));
                                }
                            }

                            if (snapshot.contains("lastBreathingSession")) {
                                Long lastSession = snapshot.getLong("lastBreathingSession");
                                TextView tvBreathingSubtitle = view.findViewById(R.id.tvBreathingSubtitle);
                                if (lastSession != null && tvBreathingSubtitle != null) {
                                    tvBreathingSubtitle.setText("Last session: " + formatLastSession(lastSession));
                                }
                            }

                            if (snapshot.contains("streak")) {
                                Long streak = snapshot.getLong("streak");
                                TextView tvStreak = view.findViewById(R.id.tvDayStreak);
                                if (streak != null) {
                                    if (streak == 1) {
                                        tvStreak.setText("1 day");
                                    } else if (streak == 0) {
                                        tvStreak.setText("No streak yet");
                                    } else {
                                        tvStreak.setText(streak + " days");
                                    }
                                }
                            }
                        }
                    });
        }
    }

    private String formatLastSession(long timestamp) {
        long now = System.currentTimeMillis();
        long diff = now - timestamp;

        long minutes = diff / (60 * 1000);
        long hours   = diff / (60 * 60 * 1000);
        long days    = diff / (24 * 60 * 60 * 1000);

        if (minutes < 1)   return "Just now";
        if (minutes < 60)  return minutes + " min ago";
        if (hours < 24)    return hours + " hour" + (hours == 1 ? "" : "s") + " ago";
        if (days == 1)     return "Yesterday";
        return days + " days ago";
    }

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_third, container, false);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            new MoodStreakManager().checkAndResetStreakIfBroken(new MoodStreakManager.ResetCallback() {
                @Override
                public void onChecked(int currentStreak) {
                    loadUserData(view);
                }

                @Override
                public void onError() {
                    loadUserData(view);
                }
            });
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(),
                    android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS},
                        101
                );
            }
        }

        view.findViewById(R.id.rowLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(requireActivity(), Login.class);
            startActivity(i);
            requireActivity().finish();
        });

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            EditProfileBottomSheet sheet = new EditProfileBottomSheet();
            sheet.setOnProfileUpdatedListener(() -> loadUserData(view));
            sheet.show(getChildFragmentManager(), "EditProfileBottomSheet");
        });

        SharedPreferences prefs = requireContext().getSharedPreferences("reminder_prefs", Context.MODE_PRIVATE);

        LinearLayout rowNotifications = view.findViewById(R.id.rowNotifications);
        rowNotifications.setOnClickListener(v -> {
            NotificationSettingsBottomSheet sheet = new NotificationSettingsBottomSheet();
            sheet.show(getChildFragmentManager(), "NotificationSettings");
        });

        return view;
    }
}