package com.example.uniglobe;

/**
 * Program model class representing a specific course or degree offered by a university.
 */
public class Program {
    private int programId;
    private int universityId; // Foreign key linking to a University
    private String course;
    private String degreeLevel; // e.g., "UG", "PG"
    private int durationYears;

    /**
     * Constructor for Program object.
     */
    public Program(int programId, int universityId, String course, String degreeLevel, int durationYears) {
        this.programId = programId;
        this.universityId = universityId;
        this.course = course;
        this.degreeLevel = degreeLevel;
        this.durationYears = durationYears;
    }

    // Getters for program properties
    public int getProgramId() { return programId; }
    public int getUniversityId() { return universityId; }
    public String getCourse() { return course; }
    public String getDegreeLevel() { return degreeLevel; }
    public int getDurationYears() { return durationYears; }

    /**
     * Returns a simple string representation: "Course (Level)".
     */
    public String getFullName() {
        return course + " (" + degreeLevel + ")";
    }

    /**
     * Returns a detailed formatted string: "Course (Level, Duration years)".
     */
    public String getFormattedInfo() {
        return course + " (" + degreeLevel + ", " + durationYears + " years)";
    }
}
