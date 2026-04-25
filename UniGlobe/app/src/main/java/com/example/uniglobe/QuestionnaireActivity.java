package com.example.uniglobe;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class QuestionnaireActivity extends AppCompatActivity {

    private Spinner careerSpinner, examsSpinner, accommodationSpinner,
            locationSpinner, climateSpinner, extracurricularSpinner, languageSpinner;
    private EditText courseInput;
    private Switch scholarshipSwitch;
    private Button submitBtn;

    private FirebaseUser user;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_questionnaire);

        user = FirebaseAuth.getInstance().getCurrentUser();
        db = FirebaseFirestore.getInstance();

        // Bind views
        careerSpinner = findViewById(R.id.careerSpinner);
        examsSpinner = findViewById(R.id.examsSpinner);
        accommodationSpinner = findViewById(R.id.accommodationSpinner);
        locationSpinner = findViewById(R.id.locationSpinner);
        climateSpinner = findViewById(R.id.climateSpinner);
        extracurricularSpinner = findViewById(R.id.extracurricularSpinner);
        languageSpinner = findViewById(R.id.languageSpinner);
        courseInput = findViewById(R.id.courseInput);
        scholarshipSwitch = findViewById(R.id.scholarshipSwitch);
        submitBtn = findViewById(R.id.submitBtn);

        // Disable submit initially
        submitBtn.setEnabled(false);

        // Setup all spinners
        setupSpinner(careerSpinner, R.array.career_options);
        setupSpinner(examsSpinner, R.array.exams_options);
        setupSpinner(accommodationSpinner, R.array.accommodation_options);
        setupSpinner(locationSpinner, R.array.location_options);
        setupSpinner(climateSpinner, R.array.climate_options);
        setupSpinner(extracurricularSpinner, R.array.extracurricular_options);
        setupSpinner(languageSpinner, R.array.language_options);

        // Watch text input
        courseInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override public void afterTextChanged(Editable s) { validateForm(); }
        });

        // Submit button click
        submitBtn.setOnClickListener(v -> {
            saveAnswersToFirestore();
        });
    }

    private void setupSpinner(Spinner spinner, int arrayResId) {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
                this,
                arrayResId,
                android.R.layout.simple_spinner_item // keeps arrow visible
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setSelection(0);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, android.view.View view, int position, long id) {
                TextView textView = (TextView) parent.getChildAt(0);
                if (position == 0) {
                    textView.setTextColor(Color.parseColor("#666666")); // grey for "Select option"
                } else {
                    textView.setTextColor(Color.parseColor("#000000")); // black for real options
                }
                validateForm();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void validateForm() {
        boolean allValid =
                careerSpinner.getSelectedItemPosition() != 0 &&
                        examsSpinner.getSelectedItemPosition() != 0 &&
                        accommodationSpinner.getSelectedItemPosition() != 0 &&
                        locationSpinner.getSelectedItemPosition() != 0 &&
                        climateSpinner.getSelectedItemPosition() != 0 &&
                        extracurricularSpinner.getSelectedItemPosition() != 0 &&
                        languageSpinner.getSelectedItemPosition() != 0 &&
                        !courseInput.getText().toString().trim().isEmpty();

        submitBtn.setEnabled(allValid);
    }

    private void saveAnswersToFirestore() {
        if (user == null) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> answers = new HashMap<>();
        answers.put("careerGoal", careerSpinner.getSelectedItem().toString());
        answers.put("examTaken", examsSpinner.getSelectedItem().toString());
        answers.put("accommodation", accommodationSpinner.getSelectedItem().toString());
        answers.put("locationPreference", locationSpinner.getSelectedItem().toString());
        answers.put("climatePreference", climateSpinner.getSelectedItem().toString());
        answers.put("extracurricular", extracurricularSpinner.getSelectedItem().toString());
        answers.put("languagePreference", languageSpinner.getSelectedItem().toString());
        answers.put("preferredCourse", courseInput.getText().toString().trim());
        answers.put("scholarshipNeeded", scholarshipSwitch.isChecked());
        answers.put("questionnaireCompleted", true);

        db.collection("users").document(user.getUid())
                .set(answers)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, "Questionnaire saved", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(QuestionnaireActivity.this, HomeActivity.class));
                    finish();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error saving questionnaire", Toast.LENGTH_SHORT).show();
                });
    }
}
