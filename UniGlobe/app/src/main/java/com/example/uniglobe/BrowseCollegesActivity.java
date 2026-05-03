package com.example.uniglobe;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

/**
 * Main exploration hub for colleges.
 * Implements sectioned discovery, favorites management, and real-time search.
 */
public class BrowseCollegesActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private SectionedUniversityAdapter sectionedAdapter;
    private DatabaseHelper databaseHelper;
    private UserPreferences userPreferences;
    private TextView headerText, pageTitle;
    private EditText searchEditText;
    private BottomNavigationView bottomNav;
    private String currentPage = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_browse_colleges);

        databaseHelper = new DatabaseHelper(this);
        userPreferences = new UserPreferences(this);

        initViews();
        setupNavigation();
        setupSearch();

        // Default landing page
        bottomNav.setSelectedItemId(R.id.navigation_home);
        loadAllUniversities();
    }

    private void initViews() {
        recyclerView = findViewById(R.id.collegeRecycler);
        headerText = findViewById(R.id.headerText);
        pageTitle = findViewById(R.id.pageTitle);
        searchEditText = findViewById(R.id.searchEditText);
        bottomNav = findViewById(R.id.bottom_navigation);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        String userEmail = getUserEmail();
        sectionedAdapter = new SectionedUniversityAdapter(this, databaseHelper, userEmail, this::onUniversityClick);
    }

    private void setupSearch() {
        if (searchEditText == null) return;

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void performSearch(String query) {
        if (query.isEmpty()) {
            // Re-load the current page state if search is cleared
            refreshCurrentPage();
            return;
        }

        pageTitle.setText("Search Results");
        headerText.setText("Finding colleges matching '" + query + "'");

        List<University> results = databaseHelper.searchUniversities(query);
        UniversityAdapter searchAdapter = new UniversityAdapter(this, results, databaseHelper, getUserEmail(), this::onUniversityClick);
        recyclerView.setAdapter(searchAdapter);
    }

    private void onUniversityClick(University university) {
        Intent intent = new Intent(this, CollegeDetailsActivity.class);
        intent.putExtra("university", university);
        startActivity(intent);
    }

    private void setupNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_home) {
                currentPage = "home";
                loadAllUniversities();
            } else if (itemId == R.id.navigation_for_you) {
                currentPage = "foryou";
                loadRecommendedSections();
            } else if (itemId == R.id.navigation_favorites) {
                currentPage = "favorites";
                loadFavorites();
            } else if (itemId == R.id.navigation_account) {
                startActivity(new Intent(this, AccountActivity.class));
            }
            return true;
        });
    }

    private void loadAllUniversities() {
        if (searchEditText != null && !searchEditText.getText().toString().isEmpty()) return;
        
        pageTitle.setText("Explore Universities");
        headerText.setText("Discover top colleges worldwide");
        
        List<University> list = databaseHelper.getAllUniversities();
        UniversityAdapter adapter = new UniversityAdapter(this, list, databaseHelper, getUserEmail(), this::onUniversityClick);
        recyclerView.setAdapter(adapter);
    }

    private void loadRecommendedSections() {
        if (searchEditText != null && !searchEditText.getText().toString().isEmpty()) return;

        pageTitle.setText("For You");
        
        if (!userPreferences.hasCompletedQuestionnaire()) {
            headerText.setText("Set your preferences in Account for custom matches");
            loadAllUniversities();
            return;
        }

        headerText.setText("Personalized sections for you");
        
        String location = userPreferences.getLocationPreference();
        String course = userPreferences.getPreferredCourse();
        String budget = userPreferences.getBudget();

        List<University> perfectMatches = databaseHelper.getFilteredUniversities(userPreferences);
        List<University> locationMatches = databaseHelper.getUniversitiesByLocation(location);
        List<University> courseMatches = databaseHelper.getUniversitiesByCourse(course);
        List<University> budgetMatches = databaseHelper.getUniversitiesByBudget(budget);

        sectionedAdapter.setSections(location, budget, perfectMatches, locationMatches, courseMatches, budgetMatches);
        recyclerView.setAdapter(sectionedAdapter);
    }

    private void loadFavorites() {
        if (searchEditText != null && !searchEditText.getText().toString().isEmpty()) return;

        pageTitle.setText("Favorites");
        String email = getUserEmail();
        if (email.isEmpty()) {
            headerText.setText("Login to see your favorites");
            recyclerView.setAdapter(null);
            return;
        }

        List<University> favorites = databaseHelper.getSavedUniversities(email);
        headerText.setText(favorites.size() + " items saved");
        
        UniversityAdapter adapter = new UniversityAdapter(this, favorites, databaseHelper, email, this::onUniversityClick);
        recyclerView.setAdapter(adapter);
    }

    private void refreshCurrentPage() {
        if ("home".equals(currentPage)) loadAllUniversities();
        else if ("foryou".equals(currentPage)) loadRecommendedSections();
        else if ("favorites".equals(currentPage)) loadFavorites();
    }

    private String getUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null) ? user.getEmail() : "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshCurrentPage();
    }
}
