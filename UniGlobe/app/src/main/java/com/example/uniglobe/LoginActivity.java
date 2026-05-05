package com.example.uniglobe;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.*;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.*;

/**
 * LoginActivity handles the user authentication process.
 * It currently supports Google Sign-In via Firebase Authentication.
 */
public class LoginActivity extends AppCompatActivity {

    // Request code for Google Sign-In intent
    private static final int RC_SIGN_IN = 100;
    
    // Firebase Auth instance for managing user sessions
    private FirebaseAuth mAuth;
    
    // Client for Google Sign-In operations
    private GoogleSignInClient mGoogleSignInClient;
    
    // Google Sign-In button UI component
    private SignInButton googleSignInBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sets the layout resource for this activity
        setContentView(R.layout.activity_login);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configure Google Sign-In to request the user's ID, email address, and basic profile.
        // ID and basic profile are included in DEFAULT_SIGN_IN.
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();
        
        // Build a GoogleSignInClient with the options specified by gso.
        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        // Initialize UI component and set up click listener
        googleSignInBtn = findViewById(R.id.googleSignInBtn);
        googleSignInBtn.setOnClickListener(v -> signIn());
    }

    /**
     * Triggers the Google Sign-In flow by starting the Google Sign-In intent.
     */
    private void signIn() {
        // Create an implicit intent to start the Google Sign-In activity
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        // Start the activity for result to capture the user's sign-in status
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...)
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                // Google Sign In failed, update UI appropriately
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Authenticates with Firebase using the Google ID token.
     * @param idToken The token obtained from Google Sign-In
     */
    private void firebaseAuthWithGoogle(String idToken) {
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);
        mAuth.signInWithCredential(credential).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                android.util.Log.d("LoginActivity", "Google sign in successful");
                FirebaseUser user = mAuth.getCurrentUser();
                if (user != null) {
                    UserPreferences userPreferences = new UserPreferences(this);
                    Intent intent;
                    
                    // Determine where to navigate based on whether the user has completed the questionnaire
                    if (userPreferences.hasCompletedQuestionnaire()) {
                        // Explicit intent to navigate to BrowseCollegesActivity
                        intent = new Intent(LoginActivity.this, BrowseCollegesActivity.class);
                    } else {
                        // Explicit intent to navigate to QuestionnaireActivity
                        intent = new Intent(LoginActivity.this, QuestionnaireActivity.class);
                    }
                    
                    // Clear the activity stack so the user cannot navigate back to the login screen
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish(); // Finish LoginActivity
                }
            } else {
                // If sign in fails, display a message to the user.
                Toast.makeText(this, "Authentication Failed.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
