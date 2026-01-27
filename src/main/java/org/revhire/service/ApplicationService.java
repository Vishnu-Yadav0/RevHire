package org.revhire.service;

import org.revhire.dao.ApplicationDAO;
import org.revhire.dao.JobDAO;
import org.revhire.model.Application;
import org.revhire.model.Application.ApplicationStatus;
import org.revhire.model.Job;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationService {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationService.class);

    private final ApplicationDAO applicationDAO;
    private final JobDAO jobDAO;
    private final NotificationService notificationService;

    public ApplicationService() {
        this.applicationDAO = new ApplicationDAO();
        this.jobDAO = new JobDAO();
        this.notificationService = new NotificationService();
    }

    public ApplicationService(ApplicationDAO applicationDAO, JobDAO jobDAO, NotificationService notificationService) {
        this.applicationDAO = applicationDAO;
        this.jobDAO = jobDAO;
        this.notificationService = notificationService;
    }

    public void applyForJob(int jobId, int seekerId, String coverLetter) throws SQLException {
        if (applicationDAO.hasApplied(seekerId, jobId)) {
            throw new SQLException("You have already applied for this job.");
        }

        Optional<Job> jobOpt = jobDAO.getJobById(jobId);
        if (jobOpt.isEmpty()) {
            throw new SQLException("Job not found.");
        }
        Job job = jobOpt.get();
        if (job.getStatus() == Job.JobStatus.CLOSED) {
            throw new SQLException("Job is closed.");
        }

        Application app = new Application(jobId, seekerId, coverLetter);
        applicationDAO.apply(app);
        logger.info("Seeker {} successfully applied for job {}", seekerId, jobId);

        // Notify Employer
        notificationService.sendNotification(job.getEmployerId(),
                "New application received for job: " + job.getTitle());
    }

    public List<Application> getApplicationsBySeeker(int seekerId) throws SQLException {
        return applicationDAO.getApplicationsBySeeker(seekerId);
    }

    public List<Application> getApplicationsByJob(int jobId) throws SQLException {
        return applicationDAO.getApplicationsByJob(jobId);
    }

    public void updateApplicationStatus(int applicationId, ApplicationStatus status) throws SQLException {
        Optional<Application> appOpt = applicationDAO.getApplicationById(applicationId);
        if (appOpt.isEmpty()) {
            throw new SQLException("Application not found");
        }
        Application app = appOpt.get();
        applicationDAO.updateStatus(applicationId, status);
        logger.info("Updated status for application {} to {}", applicationId, status);

        Optional<Job> job = jobDAO.getJobById(app.getJobId());
        String jobTitle = job.map(Job::getTitle).orElse("Unknown Job");

        // Notify Seeker
        notificationService.sendNotification(app.getSeekerId(),
                "Your application for " + jobTitle + " has been updated to: " + status);
    }

    public void withdrawApplication(int applicationId) throws SQLException {
        Optional<Application> appOpt = applicationDAO.getApplicationById(applicationId);
        if (appOpt.isEmpty()) {
            throw new SQLException("Application not found");
        }
        Application app = appOpt.get();
        if (app.getStatus() == ApplicationStatus.WITHDRAWN) {
            throw new SQLException("Application already withdrawn");
        }

        applicationDAO.updateStatus(applicationId, ApplicationStatus.WITHDRAWN);

        // Notify Employer
        Optional<Job> job = jobDAO.getJobById(app.getJobId());
        if (job.isPresent()) {
            notificationService.sendNotification(job.get().getEmployerId(),
                    "An applicant has withdrawn from job: " + job.get().getTitle());
        }
    }
}
