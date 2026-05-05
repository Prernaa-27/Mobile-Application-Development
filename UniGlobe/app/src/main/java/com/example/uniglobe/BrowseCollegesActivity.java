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
 * BrowseCollegesActivity is the main discovery hub of the app.
 * It displays universities in different views: General Explore, Personalized "For You", and Favorites.
 */
public class BrowseCollegesActivity extends AppCompatActivity {

    // UI Components
    private RecyclerView recyclerView;
    private SectionedUniversityAdapter sectionedAdapter;
    private TextView headerText, pageTitle;
    private EditText searchEditText;
    private BottomNavigationView bottomNav;

    // Helpers for data and preferences
    private DatabaseHelper databaseHelper;
    private UserPreferences userPreferences;
    
    // State variable to track the current active tab
    private String currentPage = "home";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Set the layout for this activity
        setContentView(R.layout.activity_browse_colleges);

        // Initialize database and shared preferences helpers
        databaseHelper = new DatabaseHelper(this);
        userPreferences = new UserPreferences(this);

        // Initialize UI components and listeners
        initViews();
        setupNavigation();
        setupSearch();

        // Set the default selected item in the bottom navigation to 'Home'
        bottomNav.setSelectedItemId(R.id.navigation_home);
        
        // Load initial data
        loadAllUniversities();
    }

    /**
     * Finds and initializes all view references from the layout.
     */
    private void initViews() {
        recyclerView = findViewById(R.id.collegeRecycler);
        headerText = findViewById(R.id.headerText);
        pageTitle = findViewById(R.id.pageTitle);
        searchEditText = findViewById(R.id.searchEditText);
        bottomNav = findViewById(R.id.bottom_navigation);

        // Set layout manager for the recycler view to vertical list
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        String userEmail = getUserEmail();
        // Initialize the adapter used for the personalized sectioned view
        sectionedAdapter = new SectionedUniversityAdapter(this, databaseHelper, userEmail, this::onUniversityClick);
    }

    /**
     * Sets up the real-time search functionality.
     */
    private void setupSearch() {
        if (searchEditText == null) return;

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                // Perform search every time the user types a character
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    /**
     * Filters universities based on the user's search query.
     */
    private void performSearch(String query) {
        if (query.isEmpty()) {
            // If search is cleared, go back to the original content of the current tab
            refreshCurrentPage();
            return;
        }

        pageTitle.setText("Search Results");
        headerText.setText("Finding colleges matching '" + query + "'");

        // Fetch search results from the SQLite database
        List<University> results = databaseHelper.searchUniversities(query);
        // Use standard UniversityAdapter for search results
        UniversityAdapter searchAdapter = new UniversityAdapter(this, results, databaseHelper, getUserEmail(), this::onUniversityClick);
        recyclerView.setAdapter(searchAdapter);
    }

    /**
     * Handles clicks on university items in the list.
     */
    private void onUniversityClick(University university) {
        // Explicit Intent: Used to navigate to the CollegeDetailsActivity
        Intent intent = new Intent(this, CollegeDetailsActivity.class);
        // Passing the University object (which should be Serializable or Parcelable) to the next activity
        intent.putExtra("university", university);
        startActivity(intent);
    }

    /**
     * Configures the bottom navigation bar behavior.
     */
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
                // Explicit Intent: Opens the Account screen
                startActivity(new Intent(this, AccountActivity.class));
            }
            return true;
        });
    }

    /**
     * Loads all universities from the database for the general explore tab.
     */
    private void loadAllUniversities() {
        if (searchEditText != null && !searchEditText.getText().toString().isEmpty()) return;
        
        pageTitle.setText("Explore Universities");
        headerText.setText("Discover top colleges worldwide");
        
        List<University> list = databaseHelper.getAllUniversities();
        UniversityAdapter adapter = new UniversityAdapter(this, list, databaseHelper, getUserEmail(), this::onUniversityClick);
        recyclerView.setAdapter(adapter);
    }

    /**
     * Loads personalized recommendations based on user preferences.
     */
    private void loadRecommendedSections() {
        if (searchEditText != null && !searchEditText.getText().toString().isEmpty()) return;

        pageTitle.setText("For You");
        
        // Check if user has completed their profile questionnaire
        if (!userPreferences.hasCompletedQuestionnaire()) {
            headerText.setText("Set your preferences in Account for custom matches");
            loadAllUniversities();
            return;
        }

        headerText.setText("Personalized sections for you");
        
        // Retrieve stored preferences
        String location = userPreferences.getLocationPreference();
        String course = userPreferences.getPreferredCourse();
        String budget = userPreferences.getBudget();

        // Get filtered lists from the database helper
        List<University> perfectMatches = databaseHelper.getFilteredUniversities(userPreferences);
        List<University> locationMatches = databaseHelper.getUniversitiesByLocation(location);
        List<University> courseMatches = databaseHelper.getUniversitiesByCourse(course);
        List<University> budgetMatches = databaseHelper.getUniversitiesByBudget(budget);

        // Update the sectioned adapter with these lists
        sectionedAdapter.setSections(location, budget, perfectMatches, locationMatches, courseMatches, budgetMatches);
        recyclerView.setAdapter(sectionedAdapter);
    }

    /**
     * Loads the list of universities favorited by the current user.
     */
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

    /**
     * Refreshes the content of the currently active tab.
     */
    private void refreshCurrentPage() {
        if ("home".equals(currentPage)) loadAllUniversities();
        else if ("foryou".equals(currentPage)) loadRecommendedSections();
        else if ("favorites".equals(currentPage)) loadFavorites();
    }

    /**
     * Utility method to get the current authenticated user's email.
     */
    private String getUserEmail() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return (user != null) ? user.getEmail() : "";
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh data when returning to this activity (e.g., if favorites were changed in details)
        refreshCurrentPage();
    }
}
