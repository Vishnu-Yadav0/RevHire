package org.revhire.dao;

import org.revhire.config.DBConnection;
import org.revhire.model.Job;
import org.revhire.model.Job.JobStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobDAO {
    private static final Logger logger = LoggerFactory.getLogger(JobDAO.class);

    public Job createJob(Job job) throws SQLException {
        String query = "INSERT INTO jobs (employer_id, title, description, requirements, location, salary_range, job_type, experience_years, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, job.getEmployerId());
            stmt.setString(2, job.getTitle());
            stmt.setString(3, job.getDescription());
            stmt.setString(4, job.getRequirements());
            stmt.setString(5, job.getLocation());
            stmt.setString(6, job.getSalaryRange());
            stmt.setString(7, job.getJobType());
            stmt.setInt(8, job.getExperienceYears());
            stmt.setString(9, job.getStatus().name());

            stmt.executeUpdate();
            logger.info("Successfully created job with title: '{}' for employer: {}", job.getTitle(),
                    job.getEmployerId());

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    job.setId(generatedKeys.getInt(1));
                }
            }
        }
        return job;
    }

    public List<Job> getJobsByEmployer(int employerId) throws SQLException {
        List<Job> jobs = new ArrayList<>();
        String query = "SELECT * FROM jobs WHERE employer_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, employerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    jobs.add(mapResultSetToJob(rs));
                }
            }
        }
        return jobs;
    }

    public List<Job> searchJobs(String keyword, String location, String jobType, Integer experience, String company)
            throws SQLException {
        List<Job> jobs = new ArrayList<>();
        StringBuilder queryBuilder = new StringBuilder(
                "SELECT j.* FROM jobs j JOIN employers e ON j.employer_id = e.user_id WHERE j.status = 'OPEN'");

        if (keyword != null && !keyword.isEmpty()) {
            queryBuilder.append(" AND (j.title LIKE ? OR j.description LIKE ?)");
        }
        if (location != null && !location.isEmpty()) {
            queryBuilder.append(" AND j.location LIKE ?");
        }
        if (jobType != null && !jobType.isEmpty()) {
            queryBuilder.append(" AND j.job_type LIKE ?");
        }
        if (experience != null) {
            queryBuilder.append(" AND j.experience_years <= ?");
        }
        if (company != null && !company.isEmpty()) {
            queryBuilder.append(" AND e.company_name LIKE ?");
        }

        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(queryBuilder.toString())) {

            int paramIndex = 1;
            if (keyword != null && !keyword.isEmpty()) {
                stmt.setString(paramIndex++, "%" + keyword + "%");
                stmt.setString(paramIndex++, "%" + keyword + "%");
            }
            if (location != null && !location.isEmpty()) {
                stmt.setString(paramIndex++, "%" + location + "%");
            }
            if (jobType != null && !jobType.isEmpty()) {
                stmt.setString(paramIndex++, "%" + jobType + "%");
            }
            if (experience != null) {
                stmt.setInt(paramIndex++, experience);
            }
            if (company != null && !company.isEmpty()) {
                stmt.setString(paramIndex++, "%" + company + "%");
            }

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    jobs.add(mapResultSetToJob(rs));
                }
            }
        }
        return jobs;
    }

    public Optional<Job> getJobById(int id) throws SQLException {
        String query = "SELECT * FROM jobs WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToJob(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Job mapResultSetToJob(ResultSet rs) throws SQLException {
        Job job = new Job();
        job.setId(rs.getInt("id"));
        job.setEmployerId(rs.getInt("employer_id"));
        job.setTitle(rs.getString("title"));
        job.setDescription(rs.getString("description"));
        job.setRequirements(rs.getString("requirements"));
        job.setLocation(rs.getString("location"));
        job.setSalaryRange(rs.getString("salary_range"));
        job.setJobType(rs.getString("job_type"));
        job.setExperienceYears(rs.getInt("experience_years"));
        job.setStatus(JobStatus.valueOf(rs.getString("status")));
        job.setPostedAt(rs.getTimestamp("posted_at"));
        return job;
    }

    public void updateJob(Job job) throws SQLException {
        String query = "UPDATE jobs SET title = ?, description = ?, requirements = ?, location = ?, salary_range = ?, job_type = ?, experience_years = ? WHERE id = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, job.getTitle());
            stmt.setString(2, job.getDescription());
            stmt.setString(3, job.getRequirements());
            stmt.setString(4, job.getLocation());
            stmt.setString(5, job.getSalaryRange());
            stmt.setString(6, job.getJobType());
            stmt.setInt(7, job.getExperienceYears());
            stmt.setInt(8, job.getId());
            stmt.executeUpdate();
            logger.info("Updated job details for ID: {}", job.getId());
        }
    }

    public void updateStatus(int jobId, JobStatus status) throws SQLException {
        String query = "UPDATE jobs SET status = ? WHERE id = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, jobId);
            stmt.executeUpdate();
            logger.info("Updated status for job ID {} to {}", jobId, status);
        }
    }

    public void deleteJob(int jobId) throws SQLException {
        String query = "DELETE FROM jobs WHERE id = ?";
        Connection conn = getConnection();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, jobId);
            stmt.executeUpdate();
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            return DBConnection.getInstance();
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}
