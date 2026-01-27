package org.revhire.model;

public class Employer {
    private int userId;
    private String companyName;
    private String industry;
    private String description;
    private String location;

    public Employer() {
    }

    public Employer(int userId, String companyName, String industry, String description, String location) {
        this.userId = userId;
        this.companyName = companyName;
        this.industry = industry;
        this.description = description;
        this.location = location;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getIndustry() {
        return industry;
    }

    public void setIndustry(String industry) {
        this.industry = industry;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
