package com.example.uniglobe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        View logoContainer = findViewById(R.id.logoContainer);

        // --- Cinematic Animation Sequence ---
        
        // 1. Initial State (Invisible and small)
        logoContainer.setAlpha(0f);
        logoContainer.setScaleX(0.4f);
        logoContainer.setScaleY(0.4f);
        logoContainer.setTranslationY(100f);

        // 2. Animate In (Fade in, Scale up, Slide up)
        logoContainer.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationY(0f)
                .setDuration(1500)
                .setInterpolator(new AnticipateOvershootInterpolator(1.2f))
                .withEndAction(() -> {
                    // 3. Brief pause at peak state
                    new Handler().postDelayed(this::checkUserStatus, 1000);
                })
                .start();
    }

    private void checkUserStatus() {
        // Simple fade out transition for the whole screen
        findViewById(R.id.logoContainer).animate()
                .alpha(0f)
                .scaleX(1.2f)
                .scaleY(1.2f)
                .setDuration(500)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .withEndAction(() -> {
                    Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    // Use a smooth transition
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
                    finish();
                })
                .start();
    }
}
