package org.revhire.model;

import java.util.ArrayList;
import java.util.List;

public class JobSeeker {
    private int userId;
    private String phone;
    private List<String> objectives = new ArrayList<>();
    private List<Education> educationList = new ArrayList<>();
    private List<Experience> experienceList = new ArrayList<>();
    private List<Skill> skills = new ArrayList<>();
    private List<Project> projects = new ArrayList<>();

    public JobSeeker() {
    }

    public JobSeeker(int userId, String phone) {
        this.userId = userId;
        this.phone = phone;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public List<String> getObjectives() {
        return objectives;
    }

    public void setObjectives(List<String> objectives) {
        this.objectives = objectives;
    }

    public void addObjective(String objective) {
        this.objectives.add(objective);
    }

    public List<Education> getEducationList() {
        return educationList;
    }

    public void setEducationList(List<Education> educationList) {
        this.educationList = educationList;
    }

    public void addEducation(Education education) {
        this.educationList.add(education);
    }

    public List<Experience> getExperienceList() {
        return experienceList;
    }

    public void setExperienceList(List<Experience> experienceList) {
        this.experienceList = experienceList;
    }

    public void addExperience(Experience experience) {
        this.experienceList.add(experience);
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public void setSkills(List<Skill> skills) {
        this.skills = skills;
    }

    public void addSkill(Skill skill) {
        this.skills.add(skill);
    }

    public List<Project> getProjects() {
        return projects;
    }

    public void setProjects(List<Project> projects) {
        this.projects = projects;
    }

    public void addProject(Project project) {
        this.projects.add(project);
    }
}
