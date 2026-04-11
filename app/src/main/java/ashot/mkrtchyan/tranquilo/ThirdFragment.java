package ashot.mkrtchyan.tranquilo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
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

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_third, container, false);

        view.findViewById(R.id.rowLogout).setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent i = new Intent(requireActivity(), Login.class);
            startActivity(i);
            requireActivity().finish();
        });

        Button btnEditProfile = view.findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            EditProfileBottomSheet sheet = new EditProfileBottomSheet();
            sheet.show(getChildFragmentManager(), "EditProfileBottomSheet");
        });

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
                            if (profileKey != null) {
                                switch (profileKey) {
                                    case "man":   imgAvatar.setImageResource(R.drawable.man);   break;
                                    case "woman": imgAvatar.setImageResource(R.drawable.woman); break;
                                    case "dog":   imgAvatar.setImageResource(R.drawable.dog);   break;
                                    case "cat":  imgAvatar.setImageResource(R.drawable.cat);  break;
                                }
                            }

                            if (snapshot.contains("schulte_best_seconds")) {
                                Long best = snapshot.getLong("schulte_best_seconds");
                                if (best != null) {
                                    tvSchulteBest.setText(String.format(Locale.getDefault(),
                                            "%02d:%02d", best / 60, best % 60));
                                }
                            }

                            if (snapshot.contains("calmCoins")) {
                                Long coins = snapshot.getLong("calmCoins");
                                if (coins != null) tvCalmCoins.setText(coins.toString());
                            }

                            if (snapshot.contains("streak")) {
                                Long streak = snapshot.getLong("streak");
                                TextView tvStreak = view.findViewById(R.id.tvDayStreak);
                                if (streak != null) tvStreak.setText(streak + " days");
                            }
                        }
                    });
        }

        return view;
    }
}