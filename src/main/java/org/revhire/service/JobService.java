package org.revhire.service;

import org.revhire.dao.JobDAO;
import org.revhire.model.Job;
import org.revhire.model.Job.JobStatus;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobService {
    private static final Logger logger = LoggerFactory.getLogger(JobService.class);

    private final JobDAO jobDAO;

    public JobService() {
        this.jobDAO = new JobDAO();
    }

    public JobService(JobDAO jobDAO) {
        this.jobDAO = jobDAO;
    }

    public Job postJob(Job job) throws SQLException {
        logger.info("Posting new job: {}", job.getTitle());
        return jobDAO.createJob(job);
    }

    public List<Job> getJobsByEmployer(int employerId) throws SQLException {
        return jobDAO.getJobsByEmployer(employerId);
    }

    public List<Job> searchJobs(String keyword, String location, String jobType, Integer experience, String company)
            throws SQLException {
        return jobDAO.searchJobs(keyword, location, jobType, experience, company);
    }

    public Optional<Job> getJobById(int jobId) throws SQLException {
        return jobDAO.getJobById(jobId);
    }

    public void closeJob(int jobId) throws SQLException {
        logger.info("Closing job ID: {}", jobId);
        jobDAO.updateStatus(jobId, JobStatus.CLOSED);
    }

    public void reopenJob(int jobId) throws SQLException {
        jobDAO.updateStatus(jobId, JobStatus.OPEN);
    }

    public void updateJob(Job job) throws SQLException {
        jobDAO.updateJob(job);
    }

    public void deleteJob(int jobId) throws SQLException {
        jobDAO.deleteJob(jobId);
    }
}
