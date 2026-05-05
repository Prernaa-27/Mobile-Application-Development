package com.example.uniglobe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * AccountActivity displays the user's profile information and their saved preferences.
 * It also provides options to edit preferences, contact support, and log out.
 */
public class AccountActivity extends AppCompatActivity {

    // UI Components for profile and preferences
    private TextView profileName, profileEmail, profileInitial, preferencesText;
    private View editPreferencesBtn, helpCard;
    private Button logoutBtn;
    private ImageButton backBtn;
    
    // Helper to manage user-saved preferences
    private UserPreferences userPreferences;
    
    // Flag to prevent multiple logout triggers
    private boolean isLoggingOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for the account screen
        setContentView(R.layout.activity_account);

        try {
            // Bind Views to their XML IDs
            profileName = findViewById(R.id.profileName);
            profileEmail = findViewById(R.id.profileEmail);
            profileInitial = findViewById(R.id.profileInitial);
            preferencesText = findViewById(R.id.preferencesText);
            editPreferencesBtn = findViewById(R.id.editPreferencesBtn);
            helpCard = findViewById(R.id.helpCard);
            logoutBtn = findViewById(R.id.logoutBtn);
            backBtn = findViewById(R.id.backBtn);

            // Initialize UserPreferences helper
            userPreferences = new UserPreferences(this);

            // Load and display current user data
            loadUserInfo();
            loadPreferences();

            // Set up click listener for editing preferences
            if (editPreferencesBtn != null) {
                editPreferencesBtn.setOnClickListener(v -> {
                    // Explicit Intent: Navigates to QuestionnaireActivity to update settings
                    startActivity(new Intent(this, QuestionnaireActivity.class));
                });
            }

            // Set up click listener for the Help/Support card
            if (helpCard != null) {
                helpCard.setOnClickListener(v -> {
                    // Implicit Intent: Opens email app to contact support
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(android.net.Uri.parse("mailto:uniglobehelpdesk@gmail.com"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - UniGlobe App");
                    startActivity(Intent.createChooser(intent, "Contact Support"));
                });
            }

            // Set up click listener for logout
            if (logoutBtn != null) {
                logoutBtn.setOnClickListener(v -> performLogout());
            }

            // Set up back button listener
            if (backBtn != null) {
                backBtn.setOnClickListener(v -> finish()); // Closes current activity and returns to previous
            }
        } catch (Exception e) {
            Log.e("AccountActivity", "Initialization Error", e);
        }
    }

    /**
     * Fetches current Firebase user details and populates the profile UI.
     */
    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();

            if (profileEmail != null && email != null) profileEmail.setText(email);
            if (profileName != null) profileName.setText(name != null && !name.isEmpty() ? name : "Student");
            
            // Set initial (first letter of email) for the profile avatar
            if (profileInitial != null && email != null && !email.isEmpty()) {
                profileInitial.setText(email.substring(0, 1).toUpperCase());
            }
        }
    }

    /**
     * Reads saved user preferences and displays them in a formatted list.
     */
    private void loadPreferences() {
        if (preferencesText == null) return;

        if (userPreferences.hasCompletedQuestionnaire()) {
            StringBuilder sb = new StringBuilder();
            String course = userPreferences.getPreferredCourse();
            String loc = userPreferences.getLocationPreference();
            String deg = userPreferences.getDegreeLevel();

            if (!course.isEmpty()) sb.append("📚 Course: ").append(course).append("\n");
            if (!deg.isEmpty() && !deg.equals("Select option")) sb.append("🎓 Degree: ").append(deg).append("\n");
            if (!loc.isEmpty() && !loc.equals("Select option")) sb.append("📍 Region: ").append(loc);

            preferencesText.setText(sb.length() > 0 ? sb.toString() : "No preferences set yet.");
        } else {
            preferencesText.setText("Personalize your discovery settings here.");
        }
    }

    /**
     * Handles the logout process for both Firebase and Google Sign-In.
     */
    private void performLogout() {
        if (isLoggingOut) return;
        isLoggingOut = true;

        // 1. Log out from Firebase session
        FirebaseAuth.getInstance().signOut();

        // 2. Log out from Google Sign-In client to allow user to choose account next time
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
        
        client.signOut().addOnCompleteListener(task -> {
            // Explicit Intent: Redirect user back to the Login screen
            Intent intent = new Intent(this, LoginActivity.class);
            // Clear activity stack so user cannot go back to Account screen
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Fail-safe redirect: ensure the user is moved to LoginActivity even if sign out takes time
        new Handler().postDelayed(() -> {
            if (!isFinishing()) {
                Intent intent = new Intent(this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        }, 1500);
    }
}
