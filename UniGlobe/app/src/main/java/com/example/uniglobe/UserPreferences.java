package com.example.uniglobe;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * UserPreferences manages the local storage of user-specific settings and questionnaire answers.
 * It uses SharedPreferences and isolates data by Firebase User ID (UID) so that multiple 
 * users can use the same device without seeing each other's preferences.
 */
public class UserPreferences {
    // Constant keys for SharedPreferences storage
    private static final String PREFS_PREFIX = "UniGlobePrefs_";
    private static final String KEY_COURSE = "preferredCourse";
    private static final String KEY_LOCATION = "locationPreference";
    private static final String KEY_DEGREE_LEVEL = "degreeLevel";
    private static final String KEY_BUDGET = "budget";
    private static final String KEY_UNIVERSITY_TYPE = "universityType";

    private Context context;

    /**
     * Constructor for UserPreferences.
     * @param context Application context
     */
    public UserPreferences(Context context) {
        this.context = context.getApplicationContext();
    }

    /**
     * Gets the SharedPreferences instance specific to the currently logged-in user.
     * Uses the Firebase UID as a suffix for the filename.
     */
    private SharedPreferences getPrefs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        // Fallback to "anonymous" if no user is logged in
        String uid = (user != null) ? user.getUid() : "anonymous";
        return context.getSharedPreferences(PREFS_PREFIX + uid, Context.MODE_PRIVATE);
    }

    /**
     * Saves all questionnaire preferences to SharedPreferences.
     */
    public void savePreferences(String course, String location, String degreeLevel, String budget, String universityType) {
        getPrefs().edit()
                .putString(KEY_COURSE, course)
                .putString(KEY_LOCATION, location)
                .putString(KEY_DEGREE_LEVEL, degreeLevel)
                .putString(KEY_BUDGET, budget)
                .putString(KEY_UNIVERSITY_TYPE, universityType)
                .apply(); // Writes to disk asynchronously
    }

    // Getter methods for retrieving individual preferences
    public String getPreferredCourse() { return getPrefs().getString(KEY_COURSE, ""); }
    public String getLocationPreference() { return getPrefs().getString(KEY_LOCATION, ""); }
    public String getDegreeLevel() { return getPrefs().getString(KEY_DEGREE_LEVEL, ""); }
    public String getBudget() { return getPrefs().getString(KEY_BUDGET, ""); }
    public String getUniversityType() { return getPrefs().getString(KEY_UNIVERSITY_TYPE, ""); }

    /**
     * Checks if the user has completed the questionnaire by looking for a saved course preference.
     */
    public boolean hasCompletedQuestionnaire() {
        return !getPreferredCourse().isEmpty();
    }

    /**
     * Clears all stored preferences for the current user.
     */
    public void clear() {
        getPrefs().edit().clear().apply();
    }
}
