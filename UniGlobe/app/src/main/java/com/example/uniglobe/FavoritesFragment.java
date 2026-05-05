package com.example.uniglobe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
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
 * FavoritesFragment: Displays a list of universities that the user has marked as favorites.
 */
public class FavoritesFragment extends Fragment {

    // UI Components
    private RecyclerView recyclerView;
    private UniversityCardAdapter adapter;
    private TextView favoritesCount;
    private LinearLayout emptyState;

    // Helper for database operations
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the fragment's layout
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // Bind UI components
        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        favoritesCount = view.findViewById(R.id.favoritesCount);
        emptyState = view.findViewById(R.id.emptyState);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(getContext());

        // Setup RecyclerView with linear layout
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        // Load favorited items
        loadFavorites();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh the favorites list whenever the user returns to this fragment
        // (e.g., if they unfavorited something in the details screen)
        loadFavorites();
    }

    /**
     * Retrieves the current user's favorite universities from the local database.
     */
    private void loadFavorites() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            // Fetch list from SQLite
            List<University> favorites = databaseHelper.getSavedUniversities(userEmail);

            if (favorites != null && !favorites.isEmpty()) {
                // Update UI to show the list
                favoritesCount.setText(favorites.size() + " saved universities");
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);

                // Initialize adapter
                adapter = new UniversityCardAdapter(getContext(), favorites, databaseHelper, userEmail, university -> {
                    // EXPLICIT INTENT: Navigates to the details activity for the selected university
                    Intent intent = new Intent(getContext(), CollegeDetailsActivity.class);
                    intent.putExtra("university", university);
                    startActivity(intent);
                });

                recyclerView.setAdapter(adapter);
            } else {
                // Show empty state if no favorites exist
                showEmptyState();
            }
        } else {
            // Show empty state if no user is logged in
            showEmptyState();
        }
    }

    /**
     * Toggles visibility to show an 'Empty' message when no favorites are found.
     */
    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        favoritesCount.setText("No saved universities");
    }
}
