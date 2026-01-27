package org.revhire.service;

import org.revhire.dao.EmployerDAO;
import org.revhire.dao.JobSeekerDAO;
import org.revhire.dao.UserDAO;
import org.revhire.model.Employer;
import org.revhire.model.JobSeeker;
import org.revhire.model.User;

import java.sql.SQLException;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.revhire.util.PasswordUtils;

public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    private final UserDAO userDAO;
    private final JobSeekerDAO jobSeekerDAO;
    private final EmployerDAO employerDAO;

    public AuthService() {
        this.userDAO = new UserDAO();
        this.jobSeekerDAO = new JobSeekerDAO();
        this.employerDAO = new EmployerDAO();
    }

    public AuthService(UserDAO userDAO, JobSeekerDAO jobSeekerDAO, EmployerDAO employerDAO) {
        this.userDAO = userDAO;
        this.jobSeekerDAO = jobSeekerDAO;
        this.employerDAO = employerDAO;
    }

    public User registerJobSeeker(User user, JobSeeker profile) throws SQLException {
        if (userDAO.getUserByEmail(user.getEmail()).isPresent()) {
            logger.warn("Registration attempt with existing email: {}", user.getEmail());
            throw new SQLException("Email already exists");
        }
        // Hash password before saving
        user.setPassword(PasswordUtils.hashPassword(user.getPassword()));

        User createdUser = userDAO.createUser(user);
        profile.setUserId(createdUser.getId());
        jobSeekerDAO.createProfile(profile);
        return createdUser;
    }

    public User registerEmployer(User user, Employer profile) throws SQLException {
        if (userDAO.getUserByEmail(user.getEmail()).isPresent()) {
            throw new SQLException("Email already exists");
        }
        // Hash password before saving
        user.setPassword(PasswordUtils.hashPassword(user.getPassword()));

        User createdUser = userDAO.createUser(user);
        profile.setUserId(createdUser.getId());
        employerDAO.createProfile(profile);
        return createdUser;
    }

    public User login(String email, String password) throws SQLException {
        Optional<User> userOpt = userDAO.getUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (PasswordUtils.checkPassword(password, user.getPassword())) {
                logger.info("User logged in: {}", email);
                return user;
            }
            logger.warn("Invalid password for user: {}", email);
        } else {
            logger.warn("Login attempt for non-existent email: {}", email);
        }
        throw new SQLException("Invalid email or password");
    }

    public boolean updatePassword(String email, String currentPassword, String newPassword) throws SQLException {
        Optional<User> userOpt = userDAO.getUserByEmail(email);
        if (userOpt.isPresent() && PasswordUtils.checkPassword(currentPassword, userOpt.get().getPassword())) {
            String hashedNewPassword = PasswordUtils.hashPassword(newPassword);
            boolean success = userDAO.updatePassword(email, hashedNewPassword);
            if (success) {
                logger.info("Password updated successfully for: {}", email);
            }
            return success;
        }
        logger.warn("Password update failed for: {}", email);
        return false;
    }

    public boolean recoverPassword(String email, String securityAnswer, String newPassword) throws SQLException {
        Optional<User> userOpt = userDAO.getUserByEmail(email);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getSecurityAnswer().equalsIgnoreCase(securityAnswer)) {
                String hashedNewPassword = PasswordUtils.hashPassword(newPassword);
                boolean success = userDAO.updatePassword(email, hashedNewPassword);
                if (success) {
                    logger.info("Password recovered successfully for: {}", email);
                }
                return success;
            }
            logger.warn("Recovery failed: Wrong security answer for: {}", email);
        } else {
            logger.warn("Recovery failed: Email not found: {}", email);
        }
        return false;
    }

    public Optional<JobSeeker> getJobSeekerProfile(int userId) throws SQLException {
        return jobSeekerDAO.getProfileByUserId(userId);
    }

    public void updateJobSeekerProfile(JobSeeker profile) throws SQLException {
        jobSeekerDAO.updateProfile(profile);
    }

    public String getSecurityQuestion(String email) throws SQLException {
        Optional<User> userOpt = userDAO.getUserByEmail(email);
        return userOpt.map(User::getSecurityQuestion).orElse(null);
    }
}
