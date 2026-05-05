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
 * HomeFragment: A UI component representing the 'Home' screen within a larger activity.
 * It displays a list of all available universities using a RecyclerView.
 */
public class HomeFragment extends Fragment {

    // UI Components for the fragment
    private RecyclerView recyclerView;
    private UniversityCardAdapter adapter;
    private TextView universityCount;
    
    // Helper to interact with the local SQLite database
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment's layout from the XML resource
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        // Bind UI components to their IDs from the layout
        recyclerView = view.findViewById(R.id.universitiesRecyclerView);
        universityCount = view.findViewById(R.id.universityCount);

        // Initialize the database helper using the context of the activity containing the fragment
        databaseHelper = new DatabaseHelper(getContext());

        // Configure the RecyclerView with a linear layout manager
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Fetch and display data
        loadAllUniversities();

        return view;
    }

    /**
     * Retrieves university data from the database and binds it to the RecyclerView.
     */
    private void loadAllUniversities() {
        try {
            android.util.Log.d("HomeFragment", "Loading universities...");
            // Query all universities from the local SQLite database
            List<University> universities = databaseHelper.getAllUniversities();

            android.util.Log.d("HomeFragment", "Universities loaded: " + (universities != null ? universities.size() : 0));

            if (universities != null && !universities.isEmpty()) {
                // Update the counter text
                universityCount.setText("Showing " + universities.size() + " universities");

                // Get current user's email for personalization (e.g., checking favorites)
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userEmail = currentUser != null ? currentUser.getEmail() : "";

                // Initialize the adapter to handle the university cards
                adapter = new UniversityCardAdapter(getContext(), universities, databaseHelper, userEmail, university -> {
                    // EXPLICIT INTENT: Used to navigate to CollegeDetailsActivity when a university is clicked.
                    // This intent explicitly names the target component.
                    Intent intent = new Intent(getContext(), CollegeDetailsActivity.class);
                    // Pass the university object data to the details activity
                    intent.putExtra("university", university);
                    startActivity(intent);
                });

                // Set the adapter to the RecyclerView to display the items
                recyclerView.setAdapter(adapter);
            } else {
                universityCount.setText("No universities found. Please check database.");
                android.util.Log.e("HomeFragment", "No universities in database!");
            }
        } catch (Exception e) {
            android.util.Log.e("HomeFragment", "Error loading universities", e);
            universityCount.setText("Error loading universities: " + e.getMessage());
        }
    }
}
