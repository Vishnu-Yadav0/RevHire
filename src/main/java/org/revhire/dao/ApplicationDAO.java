package org.revhire.dao;

import org.revhire.config.DBConnection;
import org.revhire.model.Application;
import org.revhire.model.Application.ApplicationStatus;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ApplicationDAO {
    private static final Logger logger = LoggerFactory.getLogger(ApplicationDAO.class);

    public void apply(Application application) throws SQLException {
        String query = "INSERT INTO applications (job_id, seeker_id, cover_letter, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, application.getJobId());
            stmt.setInt(2, application.getSeekerId());
            stmt.setString(3, application.getCoverLetter());
            stmt.setString(4, application.getStatus().name());

            stmt.executeUpdate();
            logger.info("Seeker {} applied for job {}", application.getSeekerId(), application.getJobId());

            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    application.setId(generatedKeys.getInt(1));
                }
            }
        }
    }

    public List<Application> getApplicationsBySeeker(int seekerId) throws SQLException {
        List<Application> apps = new ArrayList<>();
        String query = "SELECT * FROM applications WHERE seeker_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, seekerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    apps.add(mapResultSetToApplication(rs));
                }
            }
        }
        return apps;
    }

    public List<Application> getApplicationsByJob(int jobId) throws SQLException {
        List<Application> apps = new ArrayList<>();
        String query = "SELECT * FROM applications WHERE job_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, jobId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    apps.add(mapResultSetToApplication(rs));
                }
            }
        }
        return apps;
    }

    public void updateStatus(int applicationId, ApplicationStatus status) throws SQLException {
        String query = "UPDATE applications SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, status.name());
            stmt.setInt(2, applicationId);
            stmt.executeUpdate();
            logger.info("Updated status for application ID {} to {}", applicationId, status);
        }
    }

    public boolean hasApplied(int seekerId, int jobId) throws SQLException {
        String query = "SELECT COUNT(*) FROM applications WHERE seeker_id = ? AND job_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, seekerId);
            stmt.setInt(2, jobId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        }
        return false;
    }

    public Optional<Application> getApplicationById(int id) throws SQLException {
        String query = "SELECT * FROM applications WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToApplication(rs));
                }
            }
        }
        return Optional.empty();
    }

    private Application mapResultSetToApplication(ResultSet rs) throws SQLException {
        Application app = new Application();
        app.setId(rs.getInt("id"));
        app.setJobId(rs.getInt("job_id"));
        app.setSeekerId(rs.getInt("seeker_id"));
        app.setCoverLetter(rs.getString("cover_letter"));
        app.setStatus(ApplicationStatus.valueOf(rs.getString("status")));
        app.setAppliedAt(rs.getTimestamp("applied_at"));
        return app;
    }

    private Connection getConnection() throws SQLException {
        try {
            return DBConnection.getInstance();
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}
