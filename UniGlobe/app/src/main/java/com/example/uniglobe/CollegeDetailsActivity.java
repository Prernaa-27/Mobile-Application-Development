package com.example.uniglobe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class CollegeDetailsActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_college_details);

        databaseHelper = new DatabaseHelper(this);

        Intent intent = getIntent();
        University university = (University) intent.getSerializableExtra("university");

        if (university == null) {
            finish();
            return;
        }

        // Get programs and counsellor
        List<Program> programs = databaseHelper.getProgramsByUniversity(university.getUniversityId());
        Counsellor counsellor = databaseHelper.getCounsellorByUniversity(university.getUniversityId());

        // Set data to views
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

        detailInitials.setText(university.getInitials());
        detailName.setText(university.getName());
        detailCountry.setText(university.getLocation());
        detailRating.setText(String.format("%.1f", university.getOverallScore()));
        detailType.setText(university.getUniversityType());
        detailDescription.setText(university.getInformation());
        detailFees.setText(university.getFormattedFees());
        detailWebsite.setText("Visit Site");
        detailEmployment.setText(university.getFormattedEmployment());

        // Format programs as bullet points
        if (programs != null && !programs.isEmpty()) {
            StringBuilder formattedPrograms = new StringBuilder();
            for (Program program : programs) {
                formattedPrograms.append("• ").append(program.getFormattedInfo()).append("\n");
            }
            detailPrograms.setText(formattedPrograms.toString());
        } else {
            detailPrograms.setText("No programs information available");
        }

        // Website click
        detailWebsite.setOnClickListener(v -> {
            String url = university.getWebsiteUrl();
            if (url != null && !url.isEmpty()) {
                if (!url.startsWith("http://") && !url.startsWith("https://")) {
                    url = "https://" + url;
                }
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                startActivity(browserIntent);
            }
        });

        // Send email button - uses university-specific counsellor if available
        sendEmailBtn.setOnClickListener(v -> {
            String targetEmail = (counsellor != null) ? counsellor.getEmail() : "uniglobehelpdesk@gmail.com";
            String counsellorName = (counsellor != null) ? counsellor.getName() : "UniGlobe Team";

            Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
            emailIntent.setData(Uri.parse("mailto:" + targetEmail));
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
                startActivity(Intent.createChooser(emailIntent, "Contact Advisor"));
            } catch (Exception e) {
                Toast.makeText(this, "No email app found", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
