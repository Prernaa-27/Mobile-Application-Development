package com.example.uniglobe;

import java.io.Serializable;

/**
 * University model class representing a university entity.
 * Implements Serializable to allow passing university objects between activities via Intents.
 */
public class University implements Serializable {
    private int universityId;
    private String name;
    private String location;
    private int fees;
    private String universityType;
    private double overallScore;
    private double employmentOutcomes;
    private String websiteUrl;
    private String information;

    /**
     * Constructor for University object.
     */
    public University(int universityId, String name, String location, int fees, 
                     String universityType, double overallScore, double employmentOutcomes, 
                     String websiteUrl, String information) {
        this.universityId = universityId;
        this.name = name;
        this.location = location;
        this.fees = fees;
        this.universityType = universityType;
        this.overallScore = overallScore;
        this.employmentOutcomes = employmentOutcomes;
        this.websiteUrl = websiteUrl;
        this.information = information;
    }

    // Getters for university properties
    public int getUniversityId() { return universityId; }
    public String getName() { return name; }
    public String getLocation() { return location; }
    public int getFees() { return fees; }
    public String getUniversityType() { return universityType; }
    public double getOverallScore() { return overallScore; }
    public double getEmploymentOutcomes() { return employmentOutcomes; }
    public String getWebsiteUrl() { return websiteUrl; }
    public String getInformation() { return information; }

    /**
     * Formats the fees into a readable string (Crores/Lakhs).
     */
    public String getFormattedFees() {
        if (fees >= 10000000) {
            double crores = fees / 10000000.0;
            return "₹" + String.format("%.1f Cr", crores) + " / year";
        } else if (fees >= 100000) {
            double lakhs = fees / 100000.0;
            return "₹" + String.format("%.1f L", lakhs) + " / year";
        } else {
            return "₹" + String.format("%,d", fees) + " / year";
        }
    }

    /**
     * Formats the employment outcome score as a percentage string.
     */
    public String getFormattedEmployment() {
        return String.format("%.1f%%", employmentOutcomes * 10);
    }

    /**
     * Generates initials for the university based on its name.
     */
    public String getInitials() {
        String[] words = name.split(" ");
        if (words.length >= 2) {
            return (words[0].substring(0, 1) + words[1].substring(0, 1)).toUpperCase();
        }
        return name.substring(0, Math.min(2, name.length())).toUpperCase();
    }
}
