package com.example.uniglobe;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * QuestionnaireActivity collects user preferences such as location, degree level,
 * budget, and preferred course to provide personalized university recommendations.
 */
public class QuestionnaireActivity extends AppCompatActivity {

    // UI Components for user input
    private Spinner locationSpinner, degreeLevelSpinner, budgetSpinner, universityTypeSpinner;
    private AutoCompleteTextView courseInput;
    private Button submitBtn;

    // Data and Firebase helpers
    private FirebaseUser user;
    private FirebaseFirestore db;
    private DatabaseHelper databaseHelper;
    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sets the layout resource for the questionnaire
        setContentView(R.layout.activity_questionnaire);

        try {
            // Initialize Firebase and local storage helpers
            user = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();
            databaseHelper = new DatabaseHelper(this);
            userPreferences = new UserPreferences(this);

            // Bind UI components from layout
            locationSpinner = findViewById(R.id.locationSpinner);
            degreeLevelSpinner = findViewById(R.id.degreeLevelSpinner);
            budgetSpinner = findViewById(R.id.budgetSpinner);
            universityTypeSpinner = findViewById(R.id.universityTypeSpinner);
            courseInput = findViewById(R.id.courseInput);
            submitBtn = findViewById(R.id.submitBtn);

            // Disable submit button by default until form validation passes
            if (submitBtn != null) {
                submitBtn.setEnabled(false);
            }

            // Setup spinners with static data from strings.xml resources
            if (degreeLevelSpinner != null) setupSpinner(degreeLevelSpinner, R.array.degree_level_options);
            if (budgetSpinner != null) setupSpinner(budgetSpinner, R.array.budget_options);
            if (universityTypeSpinner != null) setupSpinner(universityTypeSpinner, R.array.university_type_options);

            // Fetch and setup the location spinner with data from the SQLite database
            setupDynamicLocationSpinner();

            // Setup autocomplete for course input using data from the database
            setupCourseAutocomplete();

            // Set click listener for the submit button
            if (submitBtn != null) {
                submitBtn.setOnClickListener(v -> {
                    saveAnswers();
                });
            }
        } catch (Exception e) {
            android.util.Log.e("QuestionnaireActivity", "Error in onCreate", e);
            Toast.makeText(this, "Error loading questionnaire. Please try again.", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    /**
     * Configures a Spinner with an adapter and selection listener.
     */
    private void setupSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                arrayResId,
                android.R.layout.simple_spinner_item
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                if (view != null) {
                    TextView textView = (TextView) view;
                    // Change text color based on whether an item is selected or it's the hint
                    if (position == 0) {
                        textView.setTextColor(Color.parseColor("#666666")); // Hint color
                    } else {
                        textView.setTextColor(Color.parseColor("#000000")); // Selection color
                    }
                }
                // Check if all fields are filled whenever an item is selected
                validateForm();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    /**
     * Populates the location spinner with unique locations found in the database.
     */
    private void setupDynamicLocationSpinner() {
        try {
            if (locationSpinner == null || databaseHelper == null) return;

            // Get unique locations from SQLite
            List<String> locations = databaseHelper.getAvailableLocations();
            List<String> locationOptions = new ArrayList<>();
            locationOptions.add("Select option"); // Placeholder/Hint
            if (locations != null && !locations.isEmpty()) {
                locationOptions.addAll(locations);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this,
                    android.R.layout.simple_spinner_item,
                    locationOptions
            );
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            locationSpinner.setAdapter(adapter);
            locationSpinner.setSelection(0);

            locationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                    if (view != null) {
                        TextView textView = (TextView) view;
                        if (position == 0) {
                            textView.setTextColor(Color.parseColor("#666666"));
                        } else {
                            textView.setTextColor(Color.parseColor("#000000"));
                        }
                    }
                    validateForm();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        } catch (Exception e) {
            android.util.Log.e("QuestionnaireActivity", "Error setting up location spinner", e);
        }
    }

    /**
     * Sets up the autocomplete suggestions for the course input field.
     */
    private void setupCourseAutocomplete() {
        try {
            if (courseInput == null || databaseHelper == null) return;

            // Get unique courses from SQLite
            List<String> courses = databaseHelper.getAvailableCourses();
            if (courses != null && !courses.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        courses
                );
                courseInput.setAdapter(adapter);
                courseInput.setThreshold(1); // Show suggestions after 1 character
            }

            courseInput.setOnItemClickListener((parent, view, position, id) -> {
                validateForm();
            });

            courseInput.addTextChangedListener(new android.text.TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {}
                @Override
                public void afterTextChanged(android.text.Editable s) {
                    // Validate form as the user types
                    validateForm();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("QuestionnaireActivity", "Error setting up course autocomplete", e);
        }
    }

    /**
     * Checks if all required fields in the questionnaire have been filled out.
     */
    private void validateForm() {
        try {
            boolean allValid =
                    locationSpinner != null && locationSpinner.getSelectedItemPosition() != 0 &&
                    degreeLevelSpinner != null && degreeLevelSpinner.getSelectedItemPosition() != 0 &&
                    budgetSpinner != null && budgetSpinner.getSelectedItemPosition() != 0 &&
                    universityTypeSpinner != null && universityTypeSpinner.getSelectedItemPosition() != 0 &&
                    courseInput != null && !courseInput.getText().toString().trim().isEmpty();

            if (submitBtn != null) {
                // Enable or disable the submit button based on validation result
                submitBtn.setEnabled(allValid);
            }
        } catch (Exception e) {
            android.util.Log.e("QuestionnaireActivity", "Error validating form", e);
        }
    }

    /**
     * Saves the user's answers to SharedPreferences and Firestore, then navigates away.
     */
    private void saveAnswers() {
        String location = locationSpinner.getSelectedItem().toString();
        String degreeLevel = degreeLevelSpinner.getSelectedItem().toString();
        String budget = budgetSpinner.getSelectedItem().toString();
        String universityType = universityTypeSpinner.getSelectedItem().toString();
        String course = courseInput.getText().toString().trim();

        // Save preferences locally using UserPreferences helper (SharedPreferences)
        userPreferences.savePreferences(course, location, degreeLevel, budget, universityType);

        // Also save to Firestore for cross-device persistence if user is logged in
        if (user != null) {
            Map<String, Object> answers = new HashMap<>();
            answers.put("preferredCourse", course);
            answers.put("locationPreference", location);
            answers.put("degreeLevel", degreeLevel);
            answers.put("budget", budget);
            answers.put("universityType", universityType);
            answers.put("questionnaireCompleted", true);

            db.collection("users").document(user.getUid())
                    .set(answers)
                    .addOnSuccessListener(aVoid -> {
                        navigateToBrowse();
                    })
                    .addOnFailureListener(e -> {
                        // Navigate even if cloud save fails to not block user experience
                        navigateToBrowse();
                    });
        } else {
            navigateToBrowse();
        }
    }

    /**
     * Navigates the user to the BrowseCollegesActivity.
     */
    private void navigateToBrowse() {
        Toast.makeText(this, "Preferences saved! Finding universities for you...", Toast.LENGTH_SHORT).show();
        // Explicit Intent: Used to launch BrowseCollegesActivity
        Intent intent = new Intent(QuestionnaireActivity.this, BrowseCollegesActivity.class);
        // Flags to clear the task stack so the user cannot navigate back to the questionnaire
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        // Finish this activity
        finish();
    }
}
