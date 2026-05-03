package com.example.uniglobe;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

/**
 * A specialized adapter that groups colleges into sections based on filters.
 */
public class SectionedUniversityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_EMPTY = 2;

    public interface OnUniversityClickListener {
        void onUniversityClick(University university);
    }

    private final Context context;
    private final List<Object> items = new ArrayList<>();
    private final DatabaseHelper databaseHelper;
    private final String userEmail;
    private final OnUniversityClickListener listener;

    public SectionedUniversityAdapter(Context context, DatabaseHelper databaseHelper, 
                                    String userEmail, OnUniversityClickListener listener) {
        this.context = context;
        this.databaseHelper = databaseHelper;
        this.userEmail = userEmail;
        this.listener = listener;
    }

    /**
     * Updates the adapter sections. Accepts 6 parameters to match the call in BrowseCollegesActivity.
     */
    public void setSections(String location, String budget, 
                           List<University> perfectMatches, 
                           List<University> locationMatches, 
                           List<University> courseMatches,
                           List<University> budgetMatches) {
        items.clear();

        // 1. Perfect Matches Section (All Filters Combined)
        items.add("Perfect Matches for You");
        if (perfectMatches == null || perfectMatches.isEmpty()) {
            items.add("No colleges meet all your filters currently.");
        } else {
            items.addAll(perfectMatches);
        }

        // 2. Location Section
        if (location != null && !location.isEmpty() && !location.equals("Select option")) {
            items.add("Colleges in " + location);
            if (locationMatches == null || locationMatches.isEmpty()) {
                items.add("No other colleges found in your preferred region.");
            } else {
                items.addAll(locationMatches);
            }
        }

        // 3. Course Section
        items.add("Recommended for your Major");
        if (courseMatches == null || courseMatches.isEmpty()) {
            items.add("No specific colleges found for this course.");
        } else {
            items.addAll(courseMatches);
        }

        // 4. Budget Section
        if (budget != null && !budget.isEmpty() && !budget.equals("Select option") && !budget.equals("Any Budget")) {
            items.add("Matches your Budget (" + budget + ")");
            if (budgetMatches == null || budgetMatches.isEmpty()) {
                items.add("No other colleges found within this budget category.");
            } else {
                items.addAll(budgetMatches);
            }
        }

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) {
            String s = (String) item;
            if (s.contains("No colleges") || s.contains("No other") || s.contains("No specific") || s.contains("meet all") || s.contains("currently meet")) {
                return TYPE_EMPTY;
            }
            return TYPE_HEADER;
        }
        return TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        if (viewType == TYPE_HEADER) {
            return new HeaderViewHolder(inflater.inflate(R.layout.row_section_header, parent, false));
        } else if (viewType == TYPE_EMPTY) {
            return new EmptyViewHolder(inflater.inflate(R.layout.row_empty_state, parent, false));
        } else {
            return new ItemViewHolder(inflater.inflate(R.layout.college_item, parent, false));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object item = items.get(position);

        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).title.setText((String) item);
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).message.setText((String) item);
        } else if (holder instanceof ItemViewHolder) {
            University university = (University) item;
            ItemViewHolder h = (ItemViewHolder) holder;

            h.initials.setText(university.getInitials());
            h.name.setText(university.getName());
            h.country.setText(university.getLocation());
            h.fees.setText(university.getFormattedFees());
            h.type.setText(university.getUniversityType());
            h.rating.setText(String.format(java.util.Locale.US, "%.1f", university.getOverallScore()));

            boolean isSaved = databaseHelper.isUniversitySaved(userEmail, university.getUniversityId());
            h.bookmarkBtn.setImageResource(isSaved ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

            h.viewDetailsBtn.setOnClickListener(v -> listener.onUniversityClick(university));
            h.itemView.setOnClickListener(v -> listener.onUniversityClick(university));

            h.bookmarkBtn.setOnClickListener(v -> {
                if (userEmail != null && !userEmail.isEmpty()) {
                    if (databaseHelper.isUniversitySaved(userEmail, university.getUniversityId())) {
                        databaseHelper.removeSavedUniversity(userEmail, university.getUniversityId());
                        h.bookmarkBtn.setImageResource(android.R.drawable.btn_star_big_off);
                    } else {
                        databaseHelper.saveUniversity(userEmail, university.getUniversityId());
                        h.bookmarkBtn.setImageResource(android.R.drawable.btn_star_big_on);
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        HeaderViewHolder(View v) { super(v); title = v.findViewById(R.id.sectionTitle); }
    }

    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        EmptyViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.emptyMessage);
        }
    }

    static class ItemViewHolder extends RecyclerView.ViewHolder {
        TextView initials, name, country, fees, type, rating;
        Button viewDetailsBtn;
        ImageButton bookmarkBtn;

        ItemViewHolder(View v) {
            super(v);
            initials = v.findViewById(R.id.collegeInitials);
            name = v.findViewById(R.id.collegeName);
            country = v.findViewById(R.id.collegeCountry);
            fees = v.findViewById(R.id.collegeFees);
            type = v.findViewById(R.id.collegeType);
            rating = v.findViewById(R.id.collegeRating);
            viewDetailsBtn = v.findViewById(R.id.viewDetailsBtn);
            bookmarkBtn = v.findViewById(R.id.bookmarkBtn);
        }
    }
}
