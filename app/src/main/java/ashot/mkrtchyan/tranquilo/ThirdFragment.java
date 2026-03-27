package ashot.mkrtchyan.tranquilo;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Locale;

import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ThirdFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ThirdFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    View rowLogout;


    public ThirdFragment() {
        // Required empty public constructor
    }

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

        rowLogout = view.findViewById(R.id.rowLogout);

        rowLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent i = new Intent(requireActivity(), Login.class);
                startActivity(i);
                requireActivity().finish();
            }
        });

        TextView tvName = view.findViewById(R.id.tvName);
        TextView tvSchulteBest = view.findViewById(R.id.tvSchulteBest);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(user.getUid())
                    .get()
                    .addOnSuccessListener(snapshot -> {

                        if (snapshot.exists()) {

                            String name = snapshot.getString("name");
                            if (name != null) {
                                tvName.setText(name);
                            }

                            // քո արդեն եղած score-ը
                            if (snapshot.contains("schulte_best_seconds")) {
                                Long best = snapshot.getLong("schulte_best_seconds");
                                if (best != null) {
                                    String formatted = String.format(Locale.getDefault(),
                                            "%02d:%02d", best / 60, best % 60);
                                    tvSchulteBest.setText(formatted);
                                }
                            }
                        }
                    });
        }

        return view;
    }
}