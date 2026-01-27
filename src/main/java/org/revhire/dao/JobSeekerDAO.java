package org.revhire.dao;

import org.revhire.config.DBConnection;
import org.revhire.model.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobSeekerDAO {
    private static final Logger logger = LoggerFactory.getLogger(JobSeekerDAO.class);

    public void createProfile(JobSeeker profile) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                // 1. Insert Base Profile
                try (PreparedStatement stmt = conn
                        .prepareStatement("INSERT INTO job_seekers (user_id, phone) VALUES (?, ?)")) {
                    stmt.setInt(1, profile.getUserId());
                    stmt.setString(2, profile.getPhone());
                    stmt.executeUpdate();
                }

                // 2. Insert Resume Parts
                insertObjectives(conn, profile);
                insertEducation(conn, profile);
                insertExperience(conn, profile);
                insertSkills(conn, profile);
                insertProjects(conn, profile);

                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    // Simplification: For update, we delete existing and re-insert for lists.
    public void updateProfile(JobSeeker profile) throws SQLException {
        try (Connection conn = getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement stmt = conn
                        .prepareStatement("UPDATE job_seekers SET phone = ? WHERE user_id = ?")) {
                    stmt.setString(1, profile.getPhone());
                    stmt.setInt(2, profile.getUserId());
                    stmt.executeUpdate();
                }

                // Clear existing
                deleteTables(conn, profile.getUserId());

                // Re-insert
                insertObjectives(conn, profile);
                insertEducation(conn, profile);
                insertExperience(conn, profile);
                insertSkills(conn, profile);
                insertProjects(conn, profile);

                conn.commit();
                logger.info("Successfully updated profile for user ID: {}", profile.getUserId());
            } catch (SQLException e) {
                logger.error("Update failed, rolling back for user ID: {}. Error: {}", profile.getUserId(),
                        e.getMessage());
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public Optional<JobSeeker> getProfileByUserId(int userId) throws SQLException {
        JobSeeker profile = null;
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM job_seekers WHERE user_id = ?")) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    profile = new JobSeeker(rs.getInt("user_id"), rs.getString("phone"));
                }
            }
        }

        if (profile != null) {
            profile.setObjectives(fetchObjectives(userId));
            profile.setEducationList(fetchEducation(userId));
            profile.setExperienceList(fetchExperience(userId));
            profile.setSkills(fetchSkills(userId));
            profile.setProjects(fetchProjects(userId));
            return Optional.of(profile);
        }
        return Optional.empty();
    }

    // Helpers
    private void insertObjectives(Connection conn, JobSeeker p) throws SQLException {
        String query = "INSERT INTO resume_objectives (user_id, objective) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (String obj : p.getObjectives()) {
                stmt.setInt(1, p.getUserId());
                stmt.setString(2, obj);
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void insertEducation(Connection conn, JobSeeker p) throws SQLException {
        String query = "INSERT INTO resume_education (user_id, degree, institution, year) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Education edu : p.getEducationList()) {
                stmt.setInt(1, p.getUserId());
                stmt.setString(2, edu.getDegree());
                stmt.setString(3, edu.getInstitution());
                stmt.setInt(4, edu.getYear());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void insertExperience(Connection conn, JobSeeker p) throws SQLException {
        String query = "INSERT INTO resume_experience (user_id, company, role, duration, description) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Experience exp : p.getExperienceList()) {
                stmt.setInt(1, p.getUserId());
                stmt.setString(2, exp.getCompany());
                stmt.setString(3, exp.getRole());
                stmt.setString(4, exp.getDuration());
                stmt.setString(5, exp.getDescription());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void insertSkills(Connection conn, JobSeeker p) throws SQLException {
        String query = "INSERT INTO resume_skills (user_id, skill_name) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Skill s : p.getSkills()) {
                stmt.setInt(1, p.getUserId());
                stmt.setString(2, s.getName());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void insertProjects(Connection conn, JobSeeker p) throws SQLException {
        String query = "INSERT INTO resume_projects (user_id, title, description, role) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            for (Project pr : p.getProjects()) {
                stmt.setInt(1, p.getUserId());
                stmt.setString(2, pr.getTitle());
                stmt.setString(3, pr.getDescription());
                stmt.setString(4, pr.getRole());
                stmt.addBatch();
            }
            stmt.executeBatch();
        }
    }

    private void deleteTables(Connection conn, int userId) throws SQLException {
        String[] tables = { "resume_objectives", "resume_education", "resume_experience", "resume_skills",
                "resume_projects" };
        for (String table : tables) {
            try (PreparedStatement stmt = conn.prepareStatement("DELETE FROM " + table + " WHERE user_id = ?")) {
                stmt.setInt(1, userId);
                stmt.executeUpdate();
            }
        }
    }

    private List<String> fetchObjectives(int userId) throws SQLException {
        List<String> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn
                        .prepareStatement("SELECT objective FROM resume_objectives WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next())
                    list.add(rs.getString(1));
            }
        }
        return list;
    }

    private List<Education> fetchEducation(int userId) throws SQLException {
        List<Education> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM resume_education WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Education(rs.getString("degree"), rs.getString("institution"), rs.getInt("year")));
                }
            }
        }
        return list;
    }

    private List<Experience> fetchExperience(int userId) throws SQLException {
        List<Experience> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM resume_experience WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Experience(rs.getString("company"), rs.getString("role"), rs.getString("duration"),
                            rs.getString("description")));
                }
            }
        }
        return list;
    }

    private List<Skill> fetchSkills(int userId) throws SQLException {
        List<Skill> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM resume_skills WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Skill(rs.getString("skill_name")));
                }
            }
        }
        return list;
    }

    private List<Project> fetchProjects(int userId) throws SQLException {
        List<Project> list = new ArrayList<>();
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement("SELECT * FROM resume_projects WHERE user_id = ?")) {
            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new Project(rs.getString("title"), rs.getString("description"), rs.getString("role")));
                }
            }
        }
        return list;
    }

    private Connection getConnection() throws SQLException {
        try {
            return DBConnection.getInstance();
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}
