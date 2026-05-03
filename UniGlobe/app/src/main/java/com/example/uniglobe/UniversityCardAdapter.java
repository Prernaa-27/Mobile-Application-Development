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

public class UniversityCardAdapter extends RecyclerView.Adapter<UniversityCardAdapter.UniversityCardViewHolder> {

    public interface OnUniversityClickListener {
        void onUniversityClick(University university);
    }

    private Context context;
    private List<University> universities;
    private DatabaseHelper databaseHelper;
    private String userEmail;
    private OnUniversityClickListener listener;

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
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_university_card, parent, false);
        return new UniversityCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UniversityCardViewHolder holder, int position) {
        University university = universities.get(position);

        holder.universityInitials.setText(university.getInitials());
        holder.universityName.setText(university.getName());
        holder.universityLocation.setText(university.getLocation());
        holder.universityType.setText(university.getUniversityType());
        holder.universityRating.setText(String.format("%.1f", university.getOverallScore()));
        holder.universityFees.setText("₹" + String.format("%,d", university.getFees()));
        holder.distanceBadge.setText("🌍 " + university.getLocation());

        List<Program> programs = databaseHelper.getProgramsByUniversity(university.getUniversityId());
        if (programs != null && !programs.isEmpty()) {
            holder.feature1.setText("🎓 " + programs.get(0).getCourse());
            holder.feature1.setVisibility(View.VISIBLE);
        } else {
            holder.feature1.setVisibility(View.GONE);
        }

        holder.feature2.setText("💼 " + university.getFormattedEmployment() + " Employed");

        boolean isSaved = databaseHelper.isUniversitySaved(userEmail, university.getUniversityId());
        updateBookmarkIcon(holder.bookmarkButton, isSaved);

        holder.viewDetailsButton.setOnClickListener(v -> listener.onUniversityClick(university));
        holder.itemView.setOnClickListener(v -> listener.onUniversityClick(university));

        holder.bookmarkButton.setOnClickListener(v -> {
            if (userEmail != null && !userEmail.isEmpty()) {
                boolean currentlySaved = databaseHelper.isUniversitySaved(userEmail, university.getUniversityId());
                if (currentlySaved) {
                    databaseHelper.removeSavedUniversity(userEmail, university.getUniversityId());
                    updateBookmarkIcon(holder.bookmarkButton, false);
                    Toast.makeText(context, "Removed from favorites", Toast.LENGTH_SHORT).show();

                    // Refresh list if we are in a context that requires it
                    if (context instanceof BrowseCollegesActivity || context instanceof HomeActivity) {
                        universities.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, universities.size());
                    }
                } else {
                    databaseHelper.saveUniversity(userEmail, university.getUniversityId());
                    updateBookmarkIcon(holder.bookmarkButton, true);
                    Toast.makeText(context, "Added to favorites", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(context, "Please login to save universities", Toast.LENGTH_SHORT).show();
            }
        });
    }

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
