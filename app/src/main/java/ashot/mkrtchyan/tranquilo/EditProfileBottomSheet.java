package ashot.mkrtchyan.tranquilo;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class EditProfileBottomSheet extends BottomSheetDialogFragment {


    public interface OnProfileUpdatedListener {
        void onProfileUpdated();
    }

    private OnProfileUpdatedListener listener;

    public void setOnProfileUpdatedListener(OnProfileUpdatedListener listener) {
        this.listener = listener;
    }


    private ImageView ivPreview;
    private EditText etName;
    private String selectedAvatarKey = null;

    private final int[] avatarViewIds = {
            R.id.avatar_rain,
            R.id.avatar_forest,
            R.id.avatar_fire,
            R.id.avatar_ocean
    };

    private final String[] avatarKeys = {
            "man", "woman", "dog", "cat"
    };

    private final int[] avatarDrawables = {
            R.drawable.man,
            R.drawable.woman,
            R.drawable.dog,
            R.drawable.cat
    };

    private String userId;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bottom_sheet_edit_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ivPreview = view.findViewById(R.id.iv_profile_preview);
        etName = view.findViewById(R.id.et_name);
        Button btnSave = view.findViewById(R.id.btn_save_profile);

        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("users").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String name = doc.getString("name");
                        if (name != null) etName.setText(name);

                        String savedKey = doc.getString("profilePicture");
                        if (savedKey != null) {
                            setPreviewByKey(savedKey);
                            selectedAvatarKey = savedKey;
                        }
                    }
                });

        for (int i = 0; i < avatarViewIds.length; i++) {
            final int index = i;
            view.findViewById(avatarViewIds[i]).setOnClickListener(v -> {
                selectedAvatarKey = avatarKeys[index];
                ivPreview.setImageResource(avatarDrawables[index]);
            });
        }

        btnSave.setOnClickListener(v -> saveProfile());
    }

    private void setPreviewByKey(String key) {
        for (int i = 0; i < avatarKeys.length; i++) {
            if (avatarKeys[i].equals(key)) {
                ivPreview.setImageResource(avatarDrawables[i]);
                return;
            }
        }
    }

    private void saveProfile() {
        String name = etName.getText().toString().trim();
        if (name.isEmpty()) {
            etName.setError("Name cannot be empty");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("name", name);
        if (selectedAvatarKey != null) {
            updates.put("profilePicture", selectedAvatarKey);
        }

        db.collection("users").document(userId)
                .update(updates)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Profile updated!", Toast.LENGTH_SHORT).show();
                    if (listener != null) listener.onProfileUpdated();//added
                    dismiss();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update profile", Toast.LENGTH_SHORT).show()
                );
    }
}