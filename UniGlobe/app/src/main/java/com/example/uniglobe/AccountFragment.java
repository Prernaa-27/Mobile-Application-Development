package com.example.uniglobe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class AccountFragment extends Fragment {

    private TextView profileInitials, profileName, profileEmail, preferencesText;
    private Button editPreferencesBtn, logoutBtn;
    private UserPreferences userPreferences;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        profileInitials = view.findViewById(R.id.profileInitials);
        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        preferencesText = view.findViewById(R.id.preferencesText);
        editPreferencesBtn = view.findViewById(R.id.editPreferencesBtn);
        logoutBtn = view.findViewById(R.id.logoutBtn);

        mAuth = FirebaseAuth.getInstance();
        userPreferences = new UserPreferences(getContext());

        loadUserInfo();
        loadPreferences();

        editPreferencesBtn.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), QuestionnaireActivity.class);
            startActivity(intent);
        });

        logoutBtn.setOnClickListener(v -> {
            mAuth.signOut();
            Intent intent = new Intent(getContext(), LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadPreferences(); // Refresh preferences when coming back
    }

    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            String name = currentUser.getDisplayName();

            if (email != null) {
                profileEmail.setText(email);
                String initials = email.substring(0, 1).toUpperCase();
                profileInitials.setText(initials);
            }

            if (name != null && !name.isEmpty()) {
                profileName.setText(name);
            } else {
                profileName.setText("Student");
            }
        }
    }

    private void loadPreferences() {
        if (userPreferences.hasCompletedQuestionnaire()) {
            StringBuilder prefs = new StringBuilder();
            prefs.append("📚 Course: ").append(userPreferences.getPreferredCourse()).append("\n");
            prefs.append("🎓 Degree: ").append(userPreferences.getDegreeLevel()).append("\n");
            prefs.append("📍 Location: ").append(userPreferences.getLocationPreference()).append("\n");
            prefs.append("💰 Budget: ").append(userPreferences.getBudget()).append("\n");
            prefs.append("🏛️ Type: ").append(userPreferences.getUniversityType());

            preferencesText.setText(prefs.toString());
        } else {
            preferencesText.setText("Complete the questionnaire to get personalized recommendations");
        }
    }
}
