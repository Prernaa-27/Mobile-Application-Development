package com.example.uniglobe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * ForYouFragment: Displays personalized university recommendations based on user preferences
 * collected during the questionnaire.
 */
public class ForYouFragment extends Fragment {

    // UI components
    private RecyclerView recyclerView;
    private UniversityCardAdapter adapter;
    private TextView recommendationInfo;

    // Data helpers
    private DatabaseHelper databaseHelper;
    private UserPreferences userPreferences;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_for_you, container, false);

        // Bind UI components
        recyclerView = view.findViewById(R.id.recommendedRecyclerView);
        recommendationInfo = view.findViewById(R.id.recommendationInfo);

        // Initialize helpers
        databaseHelper = new DatabaseHelper(getContext());
        userPreferences = new UserPreferences(getContext());

        // Setup RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Load the recommended content
        loadRecommendedUniversities();

        return view;
    }

    /**
     * Fetches universities from the database that match user preferences.
     */
    private void loadRecommendedUniversities() {
        try {
            android.util.Log.d("ForYouFragment", "Loading recommended universities...");
            List<University> universities;

            // Check if the user has provided preferences through the questionnaire
            if (userPreferences.hasCompletedQuestionnaire()) {
                android.util.Log.d("ForYouFragment", "User has completed questionnaire");
                // Fetch filtered results from SQLite
                universities = databaseHelper.getFilteredUniversities(userPreferences);
                
                String course = userPreferences.getPreferredCourse();
                String location = userPreferences.getLocationPreference();
                // Update text to show what criteria are being used
                recommendationInfo.setText("Based on " + course + " in " + location);
            } else {
                android.util.Log.d("ForYouFragment", "User has NOT completed questionnaire");
                // Fallback to all universities if no preferences are set
                universities = databaseHelper.getAllUniversities();
                recommendationInfo.setText("Complete your profile to get personalized recommendations");
            }

            android.util.Log.d("ForYouFragment", "Universities found: " + (universities != null ? universities.size() : 0));

            if (universities != null && !universities.isEmpty()) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userEmail = currentUser != null ? currentUser.getEmail() : "";

                // Initialize adapter with the filtered list
                adapter = new UniversityCardAdapter(getContext(), universities, databaseHelper, userEmail, university -> {
                    // EXPLICIT INTENT: Navigates to the details screen for the selected university
                    Intent intent = new Intent(getContext(), CollegeDetailsActivity.class);
                    intent.putExtra("university", university);
                    startActivity(intent);
                });

                recyclerView.setAdapter(adapter);
            } else {
                recommendationInfo.setText("No universities found matching your criteria.");
                android.util.Log.e("ForYouFragment", "No universities available!");
            }
        } catch (Exception e) {
            android.util.Log.e("ForYouFragment", "Error loading universities", e);
            recommendationInfo.setText("Error: " + e.getMessage());
        }
    }
}
