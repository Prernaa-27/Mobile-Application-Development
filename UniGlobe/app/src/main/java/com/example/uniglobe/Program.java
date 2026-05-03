package com.example.uniglobe;

public class Program {
    private int programId;
    private int universityId;
    private String course;
    private String degreeLevel;
    private int durationYears;

    public Program(int programId, int universityId, String course, String degreeLevel, int durationYears) {
        this.programId = programId;
        this.universityId = universityId;
        this.course = course;
        this.degreeLevel = degreeLevel;
        this.durationYears = durationYears;
    }

    public int getProgramId() { return programId; }
    public int getUniversityId() { return universityId; }
    public String getCourse() { return course; }
    public String getDegreeLevel() { return degreeLevel; }
    public int getDurationYears() { return durationYears; }

    public String getFullName() {
        return course + " (" + degreeLevel + ")";
    }

    public String getFormattedInfo() {
        return course + " (" + degreeLevel + ", " + durationYears + " years)";
    }
}
