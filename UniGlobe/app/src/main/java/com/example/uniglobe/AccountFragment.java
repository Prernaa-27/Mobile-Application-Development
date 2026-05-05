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

/**
 * AccountFragment: A UI component representing the user profile and settings screen.
 * It displays user details and allows editing preferences or logging out.
 */
public class AccountFragment extends Fragment {

    // UI Components
    private TextView profileInitials, profileName, profileEmail, preferencesText;
    private Button editPreferencesBtn, logoutBtn;
    
    // Helpers
    private UserPreferences userPreferences;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        // Bind UI components to their XML IDs
        profileInitials = view.findViewById(R.id.profileInitials);
        profileName = view.findViewById(R.id.profileName);
        profileEmail = view.findViewById(R.id.profileEmail);
        preferencesText = view.findViewById(R.id.preferencesText);
        editPreferencesBtn = view.findViewById(R.id.editPreferencesBtn);
        logoutBtn = view.findViewById(R.id.logoutBtn);

        // Initialize Firebase and local preference helpers
        mAuth = FirebaseAuth.getInstance();
        userPreferences = new UserPreferences(getContext());

        // Load data into views
        loadUserInfo();
        loadPreferences();

        // Setup click listener for editing preferences
        editPreferencesBtn.setOnClickListener(v -> {
            // EXPLICIT INTENT: Navigates to QuestionnaireActivity to update settings.
            Intent intent = new Intent(getContext(), QuestionnaireActivity.class);
            startActivity(intent);
        });

        // Setup click listener for logout
        logoutBtn.setOnClickListener(v -> {
            // Sign out from Firebase
            mAuth.signOut();
            // EXPLICIT INTENT: Redirect user back to LoginActivity.
            Intent intent = new Intent(getContext(), LoginActivity.class);
            // Clear the activity stack so the user cannot go back to the account screen.
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh displayed preferences when the fragment becomes visible again
        loadPreferences();
    }

    /**
     * Fetches and displays information about the currently logged-in user.
     */
    private void loadUserInfo() {
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null) {
            String email = currentUser.getEmail();
            String name = currentUser.getDisplayName();

            if (email != null) {
                profileEmail.setText(email);
                // Set first letter of email as initials if no name is available
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

    /**
     * Formats and displays the user's saved preferences.
     */
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
