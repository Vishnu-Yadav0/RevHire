package org.revhire.model;

public class Education {
    private int id;
    private int userId;
    private String degree;
    private String institution;
    private int year;

    public Education() {
    }

    public Education(String degree, String institution, int year) {
        this.degree = degree;
        this.institution = institution;
        this.year = year;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getDegree() {
        return degree;
    }

    public void setDegree(String degree) {
        this.degree = degree;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    @Override
    public String toString() {
        return degree + " at " + institution + " (" + year + ")";
    }
}
