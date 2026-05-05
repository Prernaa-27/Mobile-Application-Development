package com.example.uniglobe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

/**
 * SplashActivity: The entry point of the application.
 * Displays a cinematic logo animation before checking the user's login status.
 */
public class SplashActivity extends AppCompatActivity {

    // Firebase Auth instance to check if a user is already signed in
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for the splash screen
        setContentView(R.layout.activity_splash);

        // Initialize Firebase Authentication
        mAuth = FirebaseAuth.getInstance();
        
        // Find the logo container view for animation
        View logoContainer = findViewById(R.id.logoContainer);

        // --- Cinematic Animation Sequence ---
        
        // 1. Initial State: Set the logo to be invisible, smaller, and slightly shifted down
        logoContainer.setAlpha(0f);
        logoContainer.setScaleX(0.4f);
        logoContainer.setScaleY(0.4f);
        logoContainer.setTranslationY(100f);

        // 2. Animate In: Gradually fade in, scale up to normal size, and slide to original position
        logoContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(1500) // 1.5 seconds duration
                .setInterpolator(new AnticipateOvershootInterpolator(1.2f)) // Adds a slight bounce effect
                .withEndAction(() -> {
                    // 3. Brief pause of 1 second after animation completes before proceeding
                    new Handler().postDelayed(this::checkUserStatus, 1000);
                })
                .start();
    }

    /**
     * Logic to determine where to navigate after the splash screen.
     */
    private void checkUserStatus() {
        // Simple fade out transition for the whole screen before navigating
        findViewById(R.id.logoContainer).animate()
                .alpha(0f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    // Navigate to LoginActivity
                    // Explicit Intent: Used to start a specific component (LoginActivity)
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    
                    // Flags to clear the activity stack so the user can't go back to Splash
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    
                    // Start the activity
                    startActivity(intent);
                    
                    // Apply a smooth cross-fade transition between activities
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    
                    // Close the SplashActivity
                    finish();
                })
                .start();
    }
}
