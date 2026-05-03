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

public class ForYouFragment extends Fragment {

    private RecyclerView recyclerView;
    private UniversityCardAdapter adapter;
    private DatabaseHelper databaseHelper;
    private UserPreferences userPreferences;
    private TextView recommendationInfo;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_for_you, container, false);

        recyclerView = view.findViewById(R.id.recommendedRecyclerView);
        recommendationInfo = view.findViewById(R.id.recommendationInfo);

        databaseHelper = new DatabaseHelper(getContext());
        userPreferences = new UserPreferences(getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        loadRecommendedUniversities();

        return view;
    }

    private void loadRecommendedUniversities() {
        try {
            android.util.Log.d("ForYouFragment", "Loading recommended universities...");
            List<University> universities;

            if (userPreferences.hasCompletedQuestionnaire()) {
                android.util.Log.d("ForYouFragment", "User has completed questionnaire");
                universities = databaseHelper.getFilteredUniversities(userPreferences);
                String course = userPreferences.getPreferredCourse();
                String location = userPreferences.getLocationPreference();
                recommendationInfo.setText("Based on " + course + " in " + location);
            } else {
                android.util.Log.d("ForYouFragment", "User has NOT completed questionnaire");
                universities = databaseHelper.getAllUniversities();
                recommendationInfo.setText("Complete your profile to get personalized recommendations");
            }

            android.util.Log.d("ForYouFragment", "Universities found: " + (universities != null ? universities.size() : 0));

            if (universities != null && !universities.isEmpty()) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userEmail = currentUser != null ? currentUser.getEmail() : "";

                adapter = new UniversityCardAdapter(getContext(), universities, databaseHelper, userEmail, university -> {
                    Intent intent = new Intent(getContext(), CollegeDetailsActivity.class);
                    intent.putExtra("university", university);
                    startActivity(intent);
                });

                recyclerView.setAdapter(adapter);
            } else {
                recommendationInfo.setText("No universities found in database");
                android.util.Log.e("ForYouFragment", "No universities available!");
            }
        } catch (Exception e) {
            android.util.Log.e("ForYouFragment", "Error loading universities", e);
            recommendationInfo.setText("Error: " + e.getMessage());
        }
    }
}
