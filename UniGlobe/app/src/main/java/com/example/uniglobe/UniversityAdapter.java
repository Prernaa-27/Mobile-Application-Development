package com.example.uniglobe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

/**
 * UniversityAdapter is a RecyclerView adapter used to display university items in a list.
 * It is used for the search results and general explore list in BrowseCollegesActivity.
 */
public class UniversityAdapter extends RecyclerView.Adapter<UniversityAdapter.UniversityViewHolder> {

    /**
     * Interface for handling click events on university items.
     */
    public interface OnUniversityClickListener {
        void onUniversityClick(University university);
    }

    private Context context;
    private List<University> universities;
    private DatabaseHelper databaseHelper;
    private String userEmail;
    private OnUniversityClickListener listener;

    /**
     * Constructor for UniversityAdapter.
     */
    public UniversityAdapter(Context context, List<University> universities, 
                           DatabaseHelper databaseHelper, String userEmail,
                           OnUniversityClickListener listener) {
        this.context = context;
        this.universities = universities;
        this.databaseHelper = databaseHelper;
        this.userEmail = userEmail;
        this.listener = listener;
    }

    @NonNull
    @Override
    public UniversityViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout for individual list items
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.college_item, parent, false);
        return new UniversityViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UniversityViewHolder holder, int position) {
        University university = universities.get(position);

        // Bind data from the University object to the ViewHolder's views
        holder.initials.setText(university.getInitials());
        holder.name.setText(university.getName());
        holder.country.setText(university.getLocation());
        holder.fees.setText(university.getFormattedFees());
        holder.type.setText(university.getUniversityType());
        holder.rating.setText(String.format("%.1f", university.getOverallScore()));

        // Check the database to see if this university is favorited by the user
        boolean isSaved = databaseHelper.isUniversitySaved(userEmail, university.getUniversityId());
        updateBookmarkIcon(holder.bookmarkBtn, isSaved);

        // Click listeners to open university details
        holder.viewDetailsBtn.setOnClickListener(v -> listener.onUniversityClick(university));
        holder.itemView.setOnClickListener(v -> listener.onUniversityClick(university));

        // Handle bookmark/favorite button clicks
        holder.bookmarkBtn.setOnClickListener(v -> {
            if (userEmail != null && !userEmail.isEmpty()) {
                boolean currentlySaved = databaseHelper.isUniversitySaved(userEmail, university.getUniversityId());
                if (currentlySaved) {
                    // Remove from SQLite favorites
                    databaseHelper.removeSavedUniversity(userEmail, university.getUniversityId());
                    updateBookmarkIcon(holder.bookmarkBtn, false);
                    Toast.makeText(context, "Removed from saved", Toast.LENGTH_SHORT).show();
                } else {
                    // Add to SQLite favorites
                    databaseHelper.saveUniversity(userEmail, university.getUniversityId());
                    updateBookmarkIcon(holder.bookmarkBtn, true);
                    Toast.makeText(context, "Saved successfully", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Please login to save universities", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the bookmark icon depending on the saved state.
     */
    private void updateBookmarkIcon(ImageButton button, boolean isSaved) {
        if (isSaved) {
            button.setImageResource(android.R.drawable.btn_star_big_on);
        } else {
            button.setImageResource(android.R.drawable.btn_star_big_off);
        }
    }

    @Override
    public int getItemCount() {
        return universities != null ? universities.size() : 0;
    }

    /**
     * ViewHolder class to hold and reuse view references for list items.
     */
    static class UniversityViewHolder extends RecyclerView.ViewHolder {
        TextView initials, name, country, fees, type, rating;
        Button viewDetailsBtn;
        ImageButton bookmarkBtn;

        UniversityViewHolder(@NonNull View itemView) {
            super(itemView);
            initials = itemView.findViewById(R.id.collegeInitials);
            name = itemView.findViewById(R.id.collegeName);
            country = itemView.findViewById(R.id.collegeCountry);
            fees = itemView.findViewById(R.id.collegeFees);
            type = itemView.findViewById(R.id.collegeType);
            rating = itemView.findViewById(R.id.collegeRating);
            viewDetailsBtn = itemView.findViewById(R.id.viewDetailsBtn);
            bookmarkBtn = itemView.findViewById(R.id.bookmarkBtn);
        }
    }
}
