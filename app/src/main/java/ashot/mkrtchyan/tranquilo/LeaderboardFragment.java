package ashot.mkrtchyan.tranquilo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class LeaderboardFragment extends Fragment {

    private RecyclerView recyclerLeaderboard;
    private ProgressBar progressBar;
    private LeaderboardAdapter adapter;
    private final List<LeaderboardUser> userList = new ArrayList<>();

    private ImageView avatar1, avatar2, avatar3;
    private TextView name1, name2, name3;
    private TextView coins1, coins2, coins3;

    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_leaderboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        bindViews(view);
        setupRecyclerView();
        fetchLeaderboard();

        view.findViewById(R.id.btnBack).setVisibility(View.GONE);
    }

    private void bindViews(View view) {
        recyclerLeaderboard = view.findViewById(R.id.recyclerLeaderboard);
        progressBar         = view.findViewById(R.id.progressBar);

        avatar1 = view.findViewById(R.id.avatar1);
        avatar2 = view.findViewById(R.id.avatar2);
        avatar3 = view.findViewById(R.id.avatar3);

        name1  = view.findViewById(R.id.name1);
        name2  = view.findViewById(R.id.name2);
        name3  = view.findViewById(R.id.name3);

        coins1 = view.findViewById(R.id.coins1);
        coins2 = view.findViewById(R.id.coins2);
        coins3 = view.findViewById(R.id.coins3);
    }

    private void setupRecyclerView() {
        adapter = new LeaderboardAdapter(requireContext(), userList);
        recyclerLeaderboard.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerLeaderboard.setAdapter(adapter);
    }

    private void fetchLeaderboard() {
        progressBar.setVisibility(View.VISIBLE);

        db.collection("users")
                .orderBy("calmCoins", Query.Direction.DESCENDING)
                .limit(20)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!isAdded()) return;

                    progressBar.setVisibility(View.GONE);
                    userList.clear();

                    int rank = 1;
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        String name      = doc.getString("name") != null ? doc.getString("name") : "Anonymous";
                        long   calmCoins = doc.getLong("calmCoins") != null ? doc.getLong("calmCoins") : 0;
                        String profileKey = doc.getString("profilePicture");

                        userList.add(new LeaderboardUser(doc.getId(), name, calmCoins, profileKey, rank));
                        rank++;
                    }

                    bindPodium();

                    List<LeaderboardUser> restList = userList.size() > 3
                            ? userList.subList(3, userList.size())
                            : new ArrayList<>();
                    adapter.setData(restList);
                })
                .addOnFailureListener(e -> {
                    if (!isAdded()) return;
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(requireContext(), "Failed to load leaderboard", Toast.LENGTH_SHORT).show();
                });
    }

    private void bindPodium() {
        if (userList.size() >= 1) bindPodiumSlot(userList.get(0), name1, coins1, avatar1);
        if (userList.size() >= 2) bindPodiumSlot(userList.get(1), name2, coins2, avatar2);
        if (userList.size() >= 3) bindPodiumSlot(userList.get(2), name3, coins3, avatar3);
    }

    private void bindPodiumSlot(LeaderboardUser user, TextView nameView,
                                TextView coinsView, ImageView avatarView) {
        nameView.setText(user.getName());
        coinsView.setText(user.getCalmCoins() + " \uD83E\uDE99");

        String key = user.getAvatarUrl();
        int drawableRes;
        switch (key != null ? key : "") {
            case "woman": drawableRes = R.drawable.woman; break;
            case "cat":   drawableRes = R.drawable.cat;   break;
            case "dog":  drawableRes = R.drawable.dog;  break;
            default:       drawableRes = R.drawable.man;   break;
        }
        avatarView.setImageResource(drawableRes);
    }
}