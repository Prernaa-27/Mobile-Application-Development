package com.example.uniglobe;

import java.io.Serializable;

/**
 * Counsellor model class representing a dedicated admissions expert for a specific university.
 * Implements Serializable to allow passing counsellor objects between components if needed.
 */
public class Counsellor implements Serializable {
    private int counsellorId;
    private int universityId; // Foreign key linking to a University
    private String name;
    private String email;

    /**
     * Constructor for Counsellor object.
     */
    public Counsellor(int counsellorId, int universityId, String name, String email) {
        this.counsellorId = counsellorId;
        this.universityId = universityId;
        this.name = name;
        this.email = email;
    }

    // Getters for counsellor properties
    public int getCounsellorId() { return counsellorId; }
    public int getUniversityId() { return universityId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
