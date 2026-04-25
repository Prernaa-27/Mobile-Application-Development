package com.example.uniglobe;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;

public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // Add a short splash delay (optional)
        new Handler().postDelayed(this::checkUserStatus, 2000);
    }

    private void checkUserStatus() {
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // No user logged in → go to login
            startActivity(new Intent(SplashActivity.this, LoginActivity.class));
            finish();
        } else {
            // User logged in → check questionnaire status in Firestore
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(document -> {
                        if (document.exists()) {
                            Boolean questionnaireCompleted = document.getBoolean("questionnaireCompleted");
                            if (questionnaireCompleted != null && questionnaireCompleted) {
                                // Questionnaire already filled → go to home
                                startActivity(new Intent(SplashActivity.this, HomeActivity.class));
                            } else {
                                // Questionnaire not filled → go to questionnaire
                                startActivity(new Intent(SplashActivity.this, QuestionnaireActivity.class));
                            }
                        } else {
                            // No user doc yet → treat as new user → questionnaire
                            startActivity(new Intent(SplashActivity.this, QuestionnaireActivity.class));
                        }
                        finish();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(SplashActivity.this, "Error checking user data", Toast.LENGTH_SHORT).show();
                        // Fallback → go to login
                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        finish();
                    });
        }
    }
}
