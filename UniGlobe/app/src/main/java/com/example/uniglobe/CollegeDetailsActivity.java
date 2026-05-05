package com.example.uniglobe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

/**
 * CollegeDetailsActivity displays detailed information about a selected university.
 * It provides details such as location, rating, programs, and contact options.
 */
public class CollegeDetailsActivity extends AppCompatActivity {

    // Helper for database operations
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Sets the layout resource for this activity
        setContentView(R.layout.activity_college_details);

        // Initialize database helper
        databaseHelper = new DatabaseHelper(this);

        // Retrieve the university object passed from the previous activity
        // Get Intent: Receives the data sent by the calling activity
        Intent intent = getIntent();
        University university = (University) intent.getSerializableExtra("university");

        // If no university data is found, close the activity
        if (university == null) {
            finish();
            return;
        }

        // Fetch additional data related to the university from the local database
        List<Program> programs = databaseHelper.getProgramsByUniversity(university.getUniversityId());
        Counsellor counsellor = databaseHelper.getCounsellorByUniversity(university.getUniversityId());

        // Initialize UI components from the layout
        TextView detailInitials = findViewById(R.id.detailInitials);
        TextView detailName = findViewById(R.id.detailName);
        TextView detailCountry = findViewById(R.id.detailCountry);
        TextView detailRating = findViewById(R.id.detailRating);
        TextView detailType = findViewById(R.id.detailType);
        TextView detailDescription = findViewById(R.id.detailDescription);
        TextView detailFees = findViewById(R.id.detailFees);
        TextView detailWebsite = findViewById(R.id.detailWebsite);
        TextView detailEmployment = findViewById(R.id.detailEmployment);
        TextView detailPrograms = findViewById(R.id.detailPrograms);
        Button sendEmailBtn = findViewById(R.id.sendEmailBtn);

        // Set university details to the text views
        detailInitials.setText(university.getInitials());
        detailName.setText(university.getName());
        detailCountry.setText(university.getLocation());
        detailRating.setText(String.format("%.1f", university.getOverallScore()));
        detailType.setText(university.getUniversityType());
        detailDescription.setText(university.getInformation());
        detailFees.setText(university.getFormattedFees());
        detailWebsite.setText("Visit Site");
        detailEmployment.setText(university.getFormattedEmployment());

        // Format and display programs as a bulleted list
        if (programs != null && !programs.isEmpty()) {
            StringBuilder formattedPrograms = new StringBuilder();
            for (Program program : programs) {
                formattedPrograms.append("• ").append(program.getFormattedInfo()).append("\n");
            }
            detailPrograms.setText(formattedPrograms.toString());
        } else {
            detailPrograms.setText("No programs information available");
        }

        // Setup click listener for the website link
        detailWebsite.setOnClickListener(v -> {
            String url = university.getWebsiteUrl();
            if (url != null && !url.isEmpty()) {
                // Ensure URL starts with protocol
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                // Implicit Intent: Used to open a web browser to view the URL
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        // Setup click listener for the contact/email button
        sendEmailBtn.setOnClickListener(v -> {
            // Use specific counsellor email if available, otherwise use default
            String targetEmail = (counsellor != null) ? counsellor.getEmail() : "uniglobehelpdesk@gmail.com";
            String counsellorName = (counsellor != null) ? counsellor.getName() : "UniGlobe Team";

            // Implicit Intent: Used to send an email
            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            // Set the mailto URI data
            emailIntent.setData(Uri.parse("mailto:" + targetEmail));
            // Add subject and body extras
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Inquiry about " + university.getName());
            emailIntent.putExtra(Intent.EXTRA_TEXT, 
                "Hello " + counsellorName + ",\n\n" +
                "I am interested in learning more about " + university.getName() + 
                " (" + university.getLocation() + ").\n\n" +
                "Course: " + (programs != null && !programs.isEmpty() ? programs.get(0).getCourse() : "General Inquiry") + "\n" +
                "Annual Fees: " + university.getFormattedFees() + "\n\n" +
                "Could you please provide me with more information?\n\n" +
                "Thank you.");
            
            try {
                // Create a chooser to let the user select their preferred email app
                startActivity(Intent.createChooser(emailIntent, "Contact Advisor"));
            } catch (Exception e) {
                // Handle case where no email app is installed
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
