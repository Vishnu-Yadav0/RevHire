package org.revhire.model;

public class Project {
    private int id;
    private int userId;
    private String title;
    private String description;
    private String role;

    public Project() {
    }

    public Project(String title, String description, String role) {
        this.title = title;
        this.description = description;
        this.role = role;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return title + " (" + role + ")";
    }
}
