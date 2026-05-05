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
 * SectionedUniversityAdapter is a complex RecyclerView adapter that displays university data
 * grouped into different logical sections (e.g., Perfect Matches, Location-based, etc.).
 * It supports multiple view types: Headers, University Items, and Empty State messages.
 */
public class SectionedUniversityAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    // View type constants
    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;
    private static final int TYPE_EMPTY = 2;

    /**
     * Interface for handling clicks on university items within the sections.
     */
    public interface OnUniversityClickListener {
        void onUniversityClick(University university);
    }

    private final Context context;
    // Mixed list containing Strings (for headers/empty states) and University objects
    private final List<Object> items = new ArrayList<>();
    private final DatabaseHelper databaseHelper;
    private final String userEmail;
    private final OnUniversityClickListener listener;

    /**
     * Constructor for SectionedUniversityAdapter.
     */
    public SectionedUniversityAdapter(Context context, DatabaseHelper databaseHelper, 
                                    String userEmail, OnUniversityClickListener listener) {
        this.context = context;
        this.databaseHelper = databaseHelper;
        this.userEmail = userEmail;
        this.listener = listener;
    }

    /**
     * Rebuilds the adapter's internal list based on various filter results.
     * This creates the "Sectioned" structure by interleaving headers and data.
     */
    public void setSections(String location, String budget, 
                           List<University> perfectMatches, 
                           List<University> locationMatches, 
                           List<University> courseMatches,
                           List<University> budgetMatches) {
        items.clear();

        // 1. Perfect Matches Section: Universities matching ALL user preferences
        items.add("Perfect Matches for You"); // Header
        if (perfectMatches == null || perfectMatches.isEmpty()) {
            items.add("No colleges meet all your filters currently."); // Empty state message
        } else {
            items.addAll(perfectMatches);
        }

        // 2. Location Section: Universities in the user's preferred region
        if (location != null && !location.isEmpty() && !location.equals("Select option")) {
            items.add("Colleges in " + location); // Header
            if (locationMatches == null || locationMatches.isEmpty()) {
                items.add("No other colleges found in your preferred region.");
            } else {
                items.addAll(locationMatches);
            }
        }

        // 3. Course Section: Universities offering the user's preferred course
        items.add("Recommended for your Major"); // Header
        if (courseMatches == null || courseMatches.isEmpty()) {
            items.add("No specific colleges found for this course.");
        } else {
            items.addAll(courseMatches);
        }

        // 4. Budget Section: Universities matching the user's fee range
        if (budget != null && !budget.isEmpty() && !budget.equals("Select option") && !budget.equals("Any Budget")) {
            items.add("Matches your Budget (" + budget + ")"); // Header
            if (budgetMatches == null || budgetMatches.isEmpty()) {
                items.add("No other colleges found within this budget category.");
            } else {
                items.addAll(budgetMatches);
            }
        }

        // Notify RecyclerView to refresh the UI
        notifyDataSetChanged();
    }

    /**
     * Determines which layout to use for a specific position in the list.
     */
    @Override
    public int getItemViewType(int position) {
        Object item = items.get(position);
        if (item instanceof String) {
            String s = (String) item;
            // Distinguish between a section header and an empty state message
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
        // Inflate different XML layouts based on the view type
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

        // Bind data based on the type of ViewHolder
        if (holder instanceof HeaderViewHolder) {
            ((HeaderViewHolder) holder).title.setText((String) item);
        } else if (holder instanceof EmptyViewHolder) {
            ((EmptyViewHolder) holder).message.setText((String) item);
        } else if (holder instanceof ItemViewHolder) {
            University university = (University) item;
            ItemViewHolder h = (ItemViewHolder) holder;

            // Bind university details
            h.initials.setText(university.getInitials());
            h.name.setText(university.getName());
            h.country.setText(university.getLocation());
            h.fees.setText(university.getFormattedFees());
            h.type.setText(university.getUniversityType());
            h.rating.setText(String.format(java.util.Locale.US, "%.1f", university.getOverallScore()));

            // Handle bookmark status
            boolean isSaved = databaseHelper.isUniversitySaved(userEmail, university.getUniversityId());
            h.bookmarkBtn.setImageResource(isSaved ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);

            // Click listeners for item navigation
            h.viewDetailsBtn.setOnClickListener(v -> listener.onUniversityClick(university));
            h.itemView.setOnClickListener(v -> listener.onUniversityClick(university));

            // Bookmark toggle logic
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

    /**
     * ViewHolder for section title headers.
     */
    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView title;
        HeaderViewHolder(View v) { super(v); title = v.findViewById(R.id.sectionTitle); }
    }

    /**
     * ViewHolder for "No results found" messages within a section.
     */
    static class EmptyViewHolder extends RecyclerView.ViewHolder {
        TextView message;
        EmptyViewHolder(View v) {
            super(v);
            message = v.findViewById(R.id.emptyMessage);
        }
    }

    /**
     * ViewHolder for standard university list items.
     */
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
