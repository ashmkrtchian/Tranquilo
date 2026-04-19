package ashot.mkrtchyan.tranquilo;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.List;

public class PoemRepository {

    public interface PoemCallback {
        void onLoaded(List<Poem> poems);
        void onError(String message);
    }

    public static void fetchAll(PoemCallback callback) {
        FirebaseFirestore.getInstance()
                .collection("poems")
                .get()
                .addOnSuccessListener(query -> {
                    List<Poem> poems = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : query) {
                        Poem poem = doc.toObject(Poem.class);
                        poems.add(poem);
                    }
                    callback.onLoaded(poems);
                })
                .addOnFailureListener(e -> callback.onError(e.getMessage()));
    }
}