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

public class QuestionnaireActivity extends AppCompatActivity {

    private Spinner locationSpinner, degreeLevelSpinner, budgetSpinner, universityTypeSpinner;
    private AutoCompleteTextView courseInput;
    private Switch scholarshipSwitch;
    private Button submitBtn;

    private FirebaseUser user;
    private FirebaseFirestore db;
    private DatabaseHelper databaseHelper;
    private UserPreferences userPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        try {
            user = FirebaseAuth.getInstance().getCurrentUser();
            db = FirebaseFirestore.getInstance();
            databaseHelper = new DatabaseHelper(this);
            userPreferences = new UserPreferences(this);

            // Bind views
            locationSpinner = findViewById(R.id.locationSpinner);
            degreeLevelSpinner = findViewById(R.id.degreeLevelSpinner);
            budgetSpinner = findViewById(R.id.budgetSpinner);
            universityTypeSpinner = findViewById(R.id.universityTypeSpinner);
            courseInput = findViewById(R.id.courseInput);
            submitBtn = findViewById(R.id.submitBtn);

            // Disable submit initially
            if (submitBtn != null) {
                submitBtn.setEnabled(false);
            }

            // Setup static spinners
            if (degreeLevelSpinner != null) setupSpinner(degreeLevelSpinner, R.array.degree_level_options);
            if (budgetSpinner != null) setupSpinner(budgetSpinner, R.array.budget_options);
            if (universityTypeSpinner != null) setupSpinner(universityTypeSpinner, R.array.university_type_options);

            // Setup dynamic location spinner from database
            setupDynamicLocationSpinner();

            // Setup autocomplete for courses from database
            setupCourseAutocomplete();

            // Submit button click
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
    }

    private void setupDynamicLocationSpinner() {
        try {
            if (locationSpinner == null || databaseHelper == null) {
                android.util.Log.e("QuestionnaireActivity", "LocationSpinner or DatabaseHelper is null");
                return;
            }

            List<String> locations = databaseHelper.getAvailableLocations();
            List<String> locationOptions = new ArrayList<>();
            locationOptions.add("Select option");
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

    private void setupCourseAutocomplete() {
        try {
            if (courseInput == null || databaseHelper == null) {
                android.util.Log.e("QuestionnaireActivity", "CourseInput or DatabaseHelper is null");
                return;
            }

            List<String> courses = databaseHelper.getAvailableCourses();
            if (courses != null && !courses.isEmpty()) {
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this,
                        android.R.layout.simple_dropdown_item_1line,
                        courses
                );
                courseInput.setAdapter(adapter);
                courseInput.setThreshold(1);
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
                    validateForm();
                }
            });
        } catch (Exception e) {
            android.util.Log.e("QuestionnaireActivity", "Error setting up course autocomplete", e);
        }
    }

    private void validateForm() {
        try {
            boolean allValid =
                    locationSpinner != null && locationSpinner.getSelectedItemPosition() != 0 &&
                    degreeLevelSpinner != null && degreeLevelSpinner.getSelectedItemPosition() != 0 &&
                    budgetSpinner != null && budgetSpinner.getSelectedItemPosition() != 0 &&
                    universityTypeSpinner != null && universityTypeSpinner.getSelectedItemPosition() != 0 &&
                    courseInput != null && !courseInput.getText().toString().trim().isEmpty();

            if (submitBtn != null) {
                submitBtn.setEnabled(allValid);
            }
        } catch (Exception e) {
            android.util.Log.e("QuestionnaireActivity", "Error validating form", e);
        }
    }

    private void saveAnswers() {
        String location = locationSpinner.getSelectedItem().toString();
        String degreeLevel = degreeLevelSpinner.getSelectedItem().toString();
        String budget = budgetSpinner.getSelectedItem().toString();
        String universityType = universityTypeSpinner.getSelectedItem().toString();
        String course = courseInput.getText().toString().trim();

        // Save locally
        userPreferences.savePreferences(course, location, degreeLevel, budget, universityType);

        // Also save to Firestore if user is logged in
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
                        navigateToBrowse();
                    });
        } else {
            navigateToBrowse();
        }
    }

    private void navigateToBrowse() {
        android.util.Log.d("QuestionnaireActivity", "Navigating to BrowseCollegesActivity");
        Toast.makeText(this, "Preferences saved! Finding universities for you...", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(QuestionnaireActivity.this, BrowseCollegesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
