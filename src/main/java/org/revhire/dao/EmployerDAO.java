package org.revhire.dao;

import org.revhire.config.DBConnection;
import org.revhire.model.Employer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployerDAO {
    private static final Logger logger = LoggerFactory.getLogger(EmployerDAO.class);

    public void createProfile(Employer profile) throws SQLException {
        String query = "INSERT INTO employers (user_id, company_name, industry, description, location) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, profile.getUserId());
            stmt.setString(2, profile.getCompanyName());
            stmt.setString(3, profile.getIndustry());
            stmt.setString(4, profile.getDescription());
            stmt.setString(5, profile.getLocation());

            stmt.executeUpdate();
            logger.info("Successfully created employer profile for user ID: {}", profile.getUserId());
        }
    }

    public void updateProfile(Employer profile) throws SQLException {
        String query = "UPDATE employers SET company_name = ?, industry = ?, description = ?, location = ? WHERE user_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, profile.getCompanyName());
            stmt.setString(2, profile.getIndustry());
            stmt.setString(3, profile.getDescription());
            stmt.setString(4, profile.getLocation());
            stmt.setInt(5, profile.getUserId());

            stmt.executeUpdate();
            logger.info("Successfully updated employer profile for user ID: {}", profile.getUserId());
        }
    }

    public Optional<Employer> getProfileByUserId(int userId) throws SQLException {
        String query = "SELECT * FROM employers WHERE user_id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Employer(
                            rs.getInt("user_id"),
                            rs.getString("company_name"),
                            rs.getString("industry"),
                            rs.getString("description"),
                            rs.getString("location")));
                }
            }
        }
        return Optional.empty();
    }

    private Connection getConnection() throws SQLException {
        try {
            return DBConnection.getInstance();
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}
