package org.revhire.model;

import java.sql.Timestamp;

public class Application {
    private int id;
    private int jobId;
    private int seekerId;
    private String coverLetter;
    private ApplicationStatus status;
    private Timestamp appliedAt;

    public enum ApplicationStatus {
        APPLIED, SHORTLISTED, REJECTED, WITHDRAWN
    }

    public Application() {
    }

    public Application(int jobId, int seekerId, String coverLetter) {
        this.jobId = jobId;
        this.seekerId = seekerId;
        this.coverLetter = coverLetter;
        this.status = ApplicationStatus.APPLIED;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }

    public int getSeekerId() {
        return seekerId;
    }

    public void setSeekerId(int seekerId) {
        this.seekerId = seekerId;
    }

    public String getCoverLetter() {
        return coverLetter;
    }

    public void setCoverLetter(String coverLetter) {
        this.coverLetter = coverLetter;
    }

    public ApplicationStatus getStatus() {
        return status;
    }

    public void setStatus(ApplicationStatus status) {
        this.status = status;
    }

    public Timestamp getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(Timestamp appliedAt) {
        this.appliedAt = appliedAt;
    }
}
