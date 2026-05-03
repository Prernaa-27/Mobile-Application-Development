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

public class AccountActivity extends AppCompatActivity {

    private TextView profileName, profileEmail, profileInitial, preferencesText;
    private View editPreferencesBtn, helpCard;
    private Button logoutBtn;
    private ImageButton backBtn;
    private UserPreferences userPreferences;
    private boolean isLoggingOut = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        try {
            // Bind Views
            profileName = findViewById(R.id.profileName);
            profileEmail = findViewById(R.id.profileEmail);
            profileInitial = findViewById(R.id.profileInitial);
            preferencesText = findViewById(R.id.preferencesText);
            editPreferencesBtn = findViewById(R.id.editPreferencesBtn);
            helpCard = findViewById(R.id.helpCard);
            logoutBtn = findViewById(R.id.logoutBtn);
            backBtn = findViewById(R.id.backBtn);

            userPreferences = new UserPreferences(this);

            loadUserInfo();
            loadPreferences();

            if (editPreferencesBtn != null) {
                editPreferencesBtn.setOnClickListener(v -> {
                    startActivity(new Intent(this, QuestionnaireActivity.class));
                });
            }

            // Fixed Support Email Section
            if (helpCard != null) {
                helpCard.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(android.net.Uri.parse("mailto:uniglobehelpdesk@gmail.com"));
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Support Request - UniGlobe App");
                    startActivity(Intent.createChooser(intent, "Contact Support"));
                });
            }

            if (logoutBtn != null) {
                logoutBtn.setOnClickListener(v -> performLogout());
            }

            if (backBtn != null) {
                backBtn.setOnClickListener(v -> finish());
            }
        } catch (Exception e) {
            Log.e("AccountActivity", "Initialization Error", e);
        }
    }

    private void loadUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();

            if (profileEmail != null && email != null) profileEmail.setText(email);
            if (profileName != null) profileName.setText(name != null && !name.isEmpty() ? name : "Student");
            
            if (profileInitial != null && email != null && !email.isEmpty()) {
                profileInitial.setText(email.substring(0, 1).toUpperCase());
            }
        }
    }

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

    private void performLogout() {
        if (isLoggingOut) return;
        isLoggingOut = true;

        // 1. Firebase Sign out
        FirebaseAuth.getInstance().signOut();

        // 2. Google Sign out
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).build();
        GoogleSignInClient client = GoogleSignIn.getClient(this, gso);
        
        client.signOut().addOnCompleteListener(task -> {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Fail-safe redirect
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
