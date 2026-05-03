package com.example.uniglobe;

import java.io.Serializable;

/**
 * Represents a dedicated admissions expert for a specific university.
 */
public class Counsellor implements Serializable {
    private int counsellorId;
    private int universityId;
    private String name;
    private String email;

    public Counsellor(int counsellorId, int universityId, String name, String email) {
        this.counsellorId = counsellorId;
        this.universityId = universityId;
        this.name = name;
        this.email = email;
    }

    public int getCounsellorId() { return counsellorId; }
    public int getUniversityId() { return universityId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
}
