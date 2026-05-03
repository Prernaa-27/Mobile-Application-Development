package com.example.uniglobe;

import android.content.Context;
import android.content.SharedPreferences;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Manages user preferences with UID isolation.
 * Automatically switches the storage file based on the currently logged-in Firebase user.
 */
public class UserPreferences {
    private static final String PREFS_PREFIX = "UniGlobePrefs_";
    private static final String KEY_COURSE = "preferredCourse";
    private static final String KEY_LOCATION = "locationPreference";
    private static final String KEY_DEGREE_LEVEL = "degreeLevel";
    private static final String KEY_BUDGET = "budget";
    private static final String KEY_UNIVERSITY_TYPE = "universityType";

    private Context context;

    public UserPreferences(Context context) {
        this.context = context.getApplicationContext();
    }

    private SharedPreferences getPrefs() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user != null) ? user.getUid() : "anonymous";
        return context.getSharedPreferences(PREFS_PREFIX + uid, Context.MODE_PRIVATE);
    }

    public void savePreferences(String course, String location, String degreeLevel, String budget, String universityType) {
        getPrefs().edit()
                .putString(KEY_COURSE, course)
                .putString(KEY_LOCATION, location)
                .putString(KEY_DEGREE_LEVEL, degreeLevel)
                .putString(KEY_BUDGET, budget)
                .putString(KEY_UNIVERSITY_TYPE, universityType)
                .apply();
    }

    public String getPreferredCourse() { return getPrefs().getString(KEY_COURSE, ""); }
    public String getLocationPreference() { return getPrefs().getString(KEY_LOCATION, ""); }
    public String getDegreeLevel() { return getPrefs().getString(KEY_DEGREE_LEVEL, ""); }
    public String getBudget() { return getPrefs().getString(KEY_BUDGET, ""); }
    public String getUniversityType() { return getPrefs().getString(KEY_UNIVERSITY_TYPE, ""); }

    public boolean hasCompletedQuestionnaire() {
        return !getPreferredCourse().isEmpty();
    }

    public void clear() {
        getPrefs().edit().clear().apply();
    }
}
