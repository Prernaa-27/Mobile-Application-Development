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

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private UniversityCardAdapter adapter;
    private DatabaseHelper databaseHelper;
    private TextView favoritesCount;
    private LinearLayout emptyState;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        recyclerView = view.findViewById(R.id.favoritesRecyclerView);
        favoritesCount = view.findViewById(R.id.favoritesCount);
        emptyState = view.findViewById(R.id.emptyState);

        databaseHelper = new DatabaseHelper(getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        loadFavorites();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadFavorites(); // Refresh when coming back to this fragment
    }

    private void loadFavorites() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String userEmail = currentUser.getEmail();
            List<University> favorites = databaseHelper.getSavedUniversities(userEmail);

            if (favorites != null && !favorites.isEmpty()) {
                favoritesCount.setText(favorites.size() + " saved universities");
                recyclerView.setVisibility(View.VISIBLE);
                emptyState.setVisibility(View.GONE);

                adapter = new UniversityCardAdapter(getContext(), favorites, databaseHelper, userEmail, university -> {
                    Intent intent = new Intent(getContext(), CollegeDetailsActivity.class);
                    intent.putExtra("university", university);
                    startActivity(intent);
                });

                recyclerView.setAdapter(adapter);
            } else {
                showEmptyState();
            }
        } else {
            showEmptyState();
        }
    }

    private void showEmptyState() {
        recyclerView.setVisibility(View.GONE);
        emptyState.setVisibility(View.VISIBLE);
        favoritesCount.setText("No saved universities");
    }
}
