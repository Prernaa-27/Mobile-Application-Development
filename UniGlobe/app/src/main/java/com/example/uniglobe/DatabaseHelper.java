package com.example.uniglobe;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "uniglobe_v800.db";
    private static final int DATABASE_VERSION = 800;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Universities (" +
                "university_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "location TEXT NOT NULL," +
                "fees INTEGER NOT NULL," +
                "university_type TEXT," +
                "overall_score REAL," +
                "employment_outcomes REAL," +
                "website_url TEXT," +
                "information TEXT)");

        db.execSQL("CREATE TABLE Programs (" +
                "program_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "university_id INTEGER," +
                "course TEXT NOT NULL," +
                "degree_level TEXT," +
                "duration_years INTEGER," +
                "FOREIGN KEY (university_id) REFERENCES Universities(university_id))");

        db.execSQL("CREATE TABLE Counsellors (" +
                "counsellor_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "university_id INTEGER," +
                "name TEXT NOT NULL," +
                "email TEXT NOT NULL," +
                "FOREIGN KEY (university_id) REFERENCES Universities(university_id))");

        db.execSQL("CREATE TABLE Saved_Universities (" +
                "save_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "student_email TEXT NOT NULL," +
                "university_id INTEGER," +
                "saved_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");

        insertSampleData(db);
    }

    private void insertSampleData(SQLiteDatabase db) {
        // Universities
        db.execSQL("INSERT INTO Universities VALUES (1, 'MIT', 'USA', 41500000, 'Private', 9.5, 9.2, 'https://web.mit.edu', 'Top engineering university.')");
        db.execSQL("INSERT INTO Universities VALUES (2, 'Oxford', 'UK', 33200000, 'Public', 9.3, 9.0, 'https://www.ox.ac.uk', 'Historic academic excellence.')");
        db.execSQL("INSERT INTO Universities VALUES (3, 'IIT Bombay', 'India', 300000, 'Public', 9.2, 9.4, 'https://www.iitb.ac.in', 'Premier Indian Institute.')");
        db.execSQL("INSERT INTO Universities VALUES (4, 'NIFT Delhi', 'India', 250000, 'Public', 8.7, 8.8, 'https://www.nift.ac.in', 'Leading design institute.')");
        db.execSQL("INSERT INTO Universities VALUES (5, 'VIT Chennai', 'India', 400000, 'Private', 8.8, 8.9, 'https://www.vit.ac.in', 'Renowned for engineering.')");
        db.execSQL("INSERT INTO Universities VALUES (6, 'DTU', 'India', 200000, 'Public', 8.5, 8.6, 'https://www.dtu.ac.in', 'Premier Delhi Technical University.')");
        db.execSQL("INSERT INTO Universities VALUES (7, 'Stanford', 'USA', 39800000, 'Private', 9.4, 9.1, 'https://www.stanford.edu', 'Leading innovation hub.')");
        db.execSQL("INSERT INTO Universities VALUES (8, 'BML Munjal University', 'India', 550000, 'Private', 8.4, 8.5, 'https://www.bmu.edu.in', 'Modern university focused on innovation.')");

        // Programs
        db.execSQL("INSERT INTO Programs VALUES (1, 1, 'Computer Science', 'UG', 4)");
        db.execSQL("INSERT INTO Programs VALUES (2, 3, 'Computer Science', 'UG', 4)");
        db.execSQL("INSERT INTO Programs VALUES (3, 8, 'Computer Science', 'UG', 4)");
        db.execSQL("INSERT INTO Programs VALUES (4, 8, 'Business', 'UG', 3)");
        db.execSQL("INSERT INTO Programs VALUES (5, 5, 'Computer Science', 'UG', 4)");
        db.execSQL("INSERT INTO Programs VALUES (6, 4, 'Fashion Design', 'UG', 4)");
        db.execSQL("INSERT INTO Programs VALUES (7, 6, 'Engineering', 'UG', 4)");
        db.execSQL("INSERT INTO Programs VALUES (8, 2, 'History', 'UG', 3)");

        // Counsellors
        db.execSQL("INSERT INTO Counsellors VALUES (1, 1, 'Sarah Miller', 'admissions.sarah@mit.edu')");
        db.execSQL("INSERT INTO Counsellors VALUES (2, 2, 'John Davies', 'j.davies@ox.ac.uk')");
        db.execSQL("INSERT INTO Counsellors VALUES (3, 3, 'Amit Sharma', 'amit.admissions@iitb.ac.in')");
        db.execSQL("INSERT INTO Counsellors VALUES (4, 8, 'BMU Admissions Expert', 'admissions@bmu.edu.in')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS Saved_Universities");
        db.execSQL("DROP TABLE IF EXISTS Counsellors");
        db.execSQL("DROP TABLE IF EXISTS Programs");
        db.execSQL("DROP TABLE IF EXISTS Universities");
        onCreate(db);
    }

    public List<String> getAvailableLocations() {
        List<String> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT DISTINCT location FROM Universities ORDER BY location", null);
        if (c.moveToFirst()) { do { list.add(c.getString(0)); } while (c.moveToNext()); }
        c.close();
        return list;
    }

    public List<String> getAvailableCourses() {
        Set<String> set = new HashSet<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT DISTINCT course FROM Programs ORDER BY course", null);
        if (c.moveToFirst()) { do { set.add(c.getString(0)); } while (c.moveToNext()); }
        c.close();
        return new ArrayList<>(set);
    }

    public List<University> getAllUniversities() {
        List<University> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM Universities ORDER BY overall_score DESC", null);
        if (c.moveToFirst()) { do { list.add(cursorToUniversity(c)); } while (c.moveToNext()); }
        c.close();
        return list;
    }

    public List<University> searchUniversities(String query) {
        List<University> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM Universities WHERE name LIKE ? OR location LIKE ? ORDER BY overall_score DESC", 
            new String[]{"%" + query + "%", "%" + query + "%"});
        if (c.moveToFirst()) { do { list.add(cursorToUniversity(c)); } while (c.moveToNext()); }
        c.close();
        return list;
    }

    public List<University> getFilteredUniversities(UserPreferences prefs) {
        List<University> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        
        String course = prefs.getPreferredCourse();
        String location = prefs.getLocationPreference();
        String degree = prefs.getDegreeLevel();
        String budgetStr = prefs.getBudget();

        StringBuilder q = new StringBuilder("SELECT DISTINCT u.* FROM Universities u ");
        List<String> args = new ArrayList<>();

        if (!course.isEmpty() || (degree != null && !degree.equals("Select option"))) {
            q.append("INNER JOIN Programs p ON u.university_id = p.university_id ");
        }
        q.append("WHERE 1=1 ");

        if (!course.isEmpty()) { q.append("AND p.course LIKE ? "); args.add("%" + course + "%"); }
        if (location != null && !location.isEmpty() && !location.equals("Select option")) { q.append("AND u.location = ? "); args.add(location); }
        if (degree != null && !degree.equals("Select option")) {
            if (degree.contains("UG")) q.append("AND p.degree_level = 'UG' ");
            else if (degree.contains("PG")) q.append("AND p.degree_level = 'PG' ");
        }
        if (budgetStr != null && !budgetStr.equals("Select option") && !budgetStr.equals("Any Budget")) {
            if (budgetStr.contains("Under ₹5 Lakh")) q.append("AND u.fees < 500000 ");
            else if (budgetStr.contains("₹5 Lakh - ₹10 Lakh")) q.append("AND u.fees >= 500000 AND u.fees <= 1000000 ");
        }

        q.append("ORDER BY u.overall_score DESC");

        Cursor c = db.rawQuery(q.toString(), args.toArray(new String[0]));
        if (c.moveToFirst()) { do { list.add(cursorToUniversity(c)); } while (c.moveToNext()); }
        c.close();
        return list;
    }

    public List<University> getUniversitiesByLocation(String location) {
        List<University> list = new ArrayList<>();
        if (location == null || location.equals("Select option")) return list;
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM Universities WHERE location = ?", new String[]{location});
        if (c.moveToFirst()) { do { list.add(cursorToUniversity(c)); } while (c.moveToNext()); }
        c.close();
        return list;
    }

    public List<University> getUniversitiesByCourse(String course) {
        List<University> list = new ArrayList<>();
        if (course == null || course.isEmpty()) return list;
        Cursor c = getReadableDatabase().rawQuery(
            "SELECT DISTINCT u.* FROM Universities u INNER JOIN Programs p ON u.university_id = p.university_id WHERE p.course LIKE ?", 
            new String[]{"%" + course + "%"});
        if (c.moveToFirst()) { do { list.add(cursorToUniversity(c)); } while (c.moveToNext()); }
        c.close();
        return list;
    }

    public List<University> getUniversitiesByBudget(String budgetStr) {
        List<University> list = new ArrayList<>();
        if (budgetStr == null || budgetStr.equals("Select option") || budgetStr.equals("Any Budget")) return list;
        String sql = "SELECT * FROM Universities WHERE 1=1 ";
        if (budgetStr.contains("Under ₹5 Lakh")) sql += "AND fees < 500000";
        else if (budgetStr.contains("₹5 Lakh - ₹10 Lakh")) sql += "AND fees >= 500000 AND fees <= 1000000";
        Cursor c = getReadableDatabase().rawQuery(sql, null);
        if (c.moveToFirst()) { do { list.add(cursorToUniversity(c)); } while (c.moveToNext()); }
        c.close();
        return list;
    }

    private University cursorToUniversity(Cursor c) {
        return new University(
            c.getInt(c.getColumnIndexOrThrow("university_id")),
            c.getString(c.getColumnIndexOrThrow("name")),
            c.getString(c.getColumnIndexOrThrow("location")),
            c.getInt(c.getColumnIndexOrThrow("fees")),
            c.getString(c.getColumnIndexOrThrow("university_type")),
            c.getDouble(c.getColumnIndexOrThrow("overall_score")),
            c.getDouble(c.getColumnIndexOrThrow("employment_outcomes")),
            c.getString(c.getColumnIndexOrThrow("website_url")),
            c.getString(c.getColumnIndexOrThrow("information"))
        );
    }

    public List<Program> getProgramsByUniversity(int id) {
        List<Program> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM Programs WHERE university_id = ?", new String[]{String.valueOf(id)});
        if (c.moveToFirst()) { do { list.add(new Program(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3), c.getInt(4))); } while (c.moveToNext()); }
        c.close();
        return list;
    }

    public Counsellor getCounsellorByUniversity(int id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT * FROM Counsellors WHERE university_id = ? LIMIT 1", new String[]{String.valueOf(id)});
        Counsellor counsellor = null;
        if (c.moveToFirst()) {
            counsellor = new Counsellor(c.getInt(0), c.getInt(1), c.getString(2), c.getString(3));
        }
        c.close();
        return counsellor;
    }

    public List<University> getSavedUniversities(String email) {
        List<University> list = new ArrayList<>();
        Cursor c = getReadableDatabase().rawQuery("SELECT u.* FROM Universities u INNER JOIN Saved_Universities s ON u.university_id = s.university_id WHERE s.student_email = ?", new String[]{email});
        if (c.moveToFirst()) { do { list.add(cursorToUniversity(c)); } while (c.moveToNext()); }
        c.close();
        return list;
    }

    public boolean isUniversitySaved(String email, int id) {
        Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM Saved_Universities WHERE student_email = ? AND university_id = ?", new String[]{email, String.valueOf(id)});
        boolean exists = c.moveToFirst();
        c.close();
        return exists;
    }

    public boolean saveUniversity(String email, int id) {
        ContentValues v = new ContentValues();
        v.put("student_email", email); v.put("university_id", id);
        return getWritableDatabase().insertWithOnConflict("Saved_Universities", null, v, SQLiteDatabase.CONFLICT_IGNORE) != -1;
    }

    public boolean removeSavedUniversity(String email, int id) {
        return getWritableDatabase().delete("Saved_Universities", "student_email = ? AND university_id = ?", new String[]{email, String.valueOf(id)}) > 0;
    }
}
