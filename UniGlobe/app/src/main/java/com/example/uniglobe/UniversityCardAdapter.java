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
 * UniversityCardAdapter is a RecyclerView adapter used to display university information
 * in a card-based layout. It handles data binding and user interactions like bookmarking.
 */
public class UniversityCardAdapter extends RecyclerView.Adapter<UniversityCardAdapter.UniversityCardViewHolder> {

    /**
     * Interface to handle click events on university cards.
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
     * Constructor for UniversityCardAdapter.
     */
    public UniversityCardAdapter(Context context, List<University> universities,
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
    public UniversityCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the card layout for individual items
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_university_card, parent, false);
        return new UniversityCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UniversityCardViewHolder holder, int position) {
        University university = universities.get(position);

        // Bind data from the University object to UI elements
        holder.universityInitials.setText(university.getInitials());
        holder.universityName.setText(university.getName());
        holder.universityLocation.setText(university.getLocation());
        holder.universityType.setText(university.getUniversityType());
        holder.universityRating.setText(String.format("%.1f", university.getOverallScore()));
        holder.universityFees.setText("₹" + String.format("%,d", university.getFees()));
        holder.distanceBadge.setText("🌍 " + university.getLocation());

        // Fetch programs for this university to display as features
        List<Program> programs = databaseHelper.getProgramsByUniversity(university.getUniversityId());
        if (programs != null && !programs.isEmpty()) {
            holder.feature1.setText("🎓 " + programs.get(0).getCourse());
            holder.feature1.setVisibility(View.VISIBLE);
        } else {
            holder.feature1.setVisibility(View.GONE);
        }

        holder.feature2.setText("💼 " + university.getFormattedEmployment() + " Employed");

        // Check if the university is saved in favorites and update the icon
        boolean isSaved = databaseHelper.isUniversitySaved(userEmail, university.getUniversityId());
        updateBookmarkIcon(holder.bookmarkButton, isSaved);

        // Set click listeners for the card and the details button
        // Both trigger the listener to navigate to CollegeDetailsActivity
        holder.viewDetailsButton.setOnClickListener(v -> listener.onUniversityClick(university));
        holder.itemView.setOnClickListener(v -> listener.onUniversityClick(university));

        // Handle bookmark button clicks
        holder.bookmarkButton.setOnClickListener(v -> {
            if (userEmail != null && !userEmail.isEmpty()) {
                boolean currentlySaved = databaseHelper.isUniversitySaved(userEmail, university.getUniversityId());
                if (currentlySaved) {
                    // Remove from favorites in SQLite
                    databaseHelper.removeSavedUniversity(userEmail, university.getUniversityId());
                    updateBookmarkIcon(holder.bookmarkButton, false);
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();

                    // If in BrowseCollegesActivity's Favorites tab, remove the item immediately
                    if (context instanceof BrowseCollegesActivity || context instanceof HomeActivity) {
                        universities.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, universities.size());
                    }
                } else {
                    // Add to favorites in SQLite
                    databaseHelper.saveUniversity(userEmail, university.getUniversityId());
                    updateBookmarkIcon(holder.bookmarkButton, true);
                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Please login to save universities", Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * Updates the bookmark button icon based on saved status.
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
     * ViewHolder class for caching view references.
     */
    static class UniversityCardViewHolder extends RecyclerView.ViewHolder {
        TextView universityInitials, universityName, universityLocation, universityType;
        TextView universityRating, universityFees, distanceBadge, feature1, feature2;
        ImageButton bookmarkButton;
        Button viewDetailsButton;

        UniversityCardViewHolder(@NonNull View itemView) {
            super(itemView);
            universityInitials = itemView.findViewById(R.id.universityInitials);
            universityName = itemView.findViewById(R.id.universityName);
            universityLocation = itemView.findViewById(R.id.universityLocation);
            universityType = itemView.findViewById(R.id.universityType);
            universityRating = itemView.findViewById(R.id.universityRating);
            universityFees = itemView.findViewById(R.id.universityFees);
            distanceBadge = itemView.findViewById(R.id.distanceBadge);
            feature1 = itemView.findViewById(R.id.feature1);
            feature2 = itemView.findViewById(R.id.feature2);
            bookmarkButton = itemView.findViewById(R.id.bookmarkButton);
            viewDetailsButton = itemView.findViewById(R.id.viewDetailsButton);
        }
    }
}
