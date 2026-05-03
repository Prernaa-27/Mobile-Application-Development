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

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private UniversityCardAdapter adapter;
    private DatabaseHelper databaseHelper;
    private TextView universityCount;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.universitiesRecyclerView);
        universityCount = view.findViewById(R.id.universityCount);

        databaseHelper = new DatabaseHelper(getContext());

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setHasFixedSize(true);

        loadAllUniversities();

        return view;
    }

    private void loadAllUniversities() {
        try {
            android.util.Log.d("HomeFragment", "Loading universities...");
            List<University> universities = databaseHelper.getAllUniversities();

            android.util.Log.d("HomeFragment", "Universities loaded: " + (universities != null ? universities.size() : 0));

            if (universities != null && !universities.isEmpty()) {
                universityCount.setText("Showing " + universities.size() + " universities");

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                String userEmail = currentUser != null ? currentUser.getEmail() : "";

                adapter = new UniversityCardAdapter(getContext(), universities, databaseHelper, userEmail, university -> {
                    Intent intent = new Intent(getContext(), CollegeDetailsActivity.class);
                    intent.putExtra("university", university);
                    startActivity(intent);
                });

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
