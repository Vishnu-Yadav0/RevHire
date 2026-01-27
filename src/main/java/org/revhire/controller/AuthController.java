package org.revhire.controller;

import org.revhire.model.*;
import org.revhire.model.User.UserRole;
import org.revhire.service.AuthService;

import java.sql.SQLException;
import java.util.Scanner;
import org.revhire.util.ValidationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthController {
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    private final Scanner scanner;
    private final AuthService authService = new AuthService();

    public AuthController(Scanner scanner) {
        this.scanner = scanner;
    }

    public void login() {
        logger.info("\n--- Login ---");
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();

        try {
            User user = authService.login(email, password);
            Session.setCurrentUser(user);
            logger.info("Login successful! Welcome, {}", user.getName());
        } catch (SQLException e) {
            logger.error("Login failed: {}", e.getMessage());
        }
    }

    public void registerJobSeeker() {
        logger.info("\n--- Job Seeker Registration ---");
        User user = collectBasicUserInfo(UserRole.JOB_SEEKER);
        if (user == null)
            return;

        System.out.print("Phone: ");
        String phone = scanner.nextLine();

        JobSeeker profile = new JobSeeker(0, phone);

        // Collect Objectives
        logger.info("\n[Resume] Objectives (Type 'NEXT' to finish):");
        while (true) {
            System.out.print("- ");
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("NEXT"))
                break;
            profile.addObjective(line);
        }

        // Collect Education
        logger.info("\n[Resume] Education (Type 'NEXT' for details or 'DONE' to skip):");
        while (true) {
            System.out.print("Degree (or DONE): ");
            String degree = scanner.nextLine();
            if (degree.equalsIgnoreCase("DONE"))
                break;
            System.out.print("Institution: ");
            String inst = scanner.nextLine();
            System.out.print("Year: ");
            try {
                int year = Integer.parseInt(scanner.nextLine());
                profile.addEducation(new Education(degree, inst, year));
            } catch (NumberFormatException e) {
                logger.warn("Invalid year.");
            }
        }

        // Collect Experience
        logger.info("\n[Resume] Experience (Type 'NEXT' for details or 'DONE' to skip):");
        while (true) {
            System.out.print("Company (or DONE): ");
            String comp = scanner.nextLine();
            if (comp.equalsIgnoreCase("DONE"))
                break;
            System.out.print("Role: ");
            String role = scanner.nextLine();
            System.out.print("Duration: ");
            String dur = scanner.nextLine();
            System.out.print("Description: ");
            String desc = scanner.nextLine();
            profile.addExperience(new Experience(comp, role, dur, desc));
        }

        // Collect Skills
        logger.info("\n[Resume] Skills (Type 'NEXT' to finish):");
        while (true) {
            System.out.print("- ");
            String line = scanner.nextLine();
            if (line.equalsIgnoreCase("NEXT"))
                break;
            profile.addSkill(new Skill(line));
        }

        // Collect Projects
        logger.info("\n[Resume] Projects (Type 'NEXT' for details or 'DONE' to skip):");
        while (true) {
            System.out.print("Title (or DONE): ");
            String title = scanner.nextLine();
            if (title.equalsIgnoreCase("DONE"))
                break;
            System.out.print("Role: ");
            String role = scanner.nextLine();
            System.out.print("Description: ");
            String desc = scanner.nextLine();
            profile.addProject(new Project(title, desc, role));
        }

        try {
            authService.registerJobSeeker(user, profile);
            logger.info("Registration successful! Please login.");
        } catch (SQLException e) {
            logger.error("Registration failed: {}", e.getMessage());
        }
    }

    public void registerEmployer() {
        logger.info("\n--- Employer Registration ---");
        User user = collectBasicUserInfo(UserRole.EMPLOYER);
        if (user == null)
            return;

        System.out.print("Company Name: ");
        String company = scanner.nextLine();
        System.out.print("Industry: ");
        String industry = scanner.nextLine();
        System.out.print("Location: ");
        String location = scanner.nextLine();
        System.out.print("Description: ");
        String desc = scanner.nextLine();

        Employer profile = new Employer(0, company, industry, desc, location);

        try {
            authService.registerEmployer(user, profile);
            logger.info("Registration successful! Welcome to RevHire.");
        } catch (SQLException e) {
            logger.error("Registration failed: {}", e.getMessage());
        }
    }

    public void forgotPassword() {
        logger.info("\n--- Forgot Password ---");
        System.out.print("Email: ");
        String email = scanner.nextLine();

        try {
            String question = authService.getSecurityQuestion(email);
            if (question == null) {
                logger.warn("No user found with that email address.");
                return;
            }

            System.out.println("Security Question: " + question);
            System.out.print("Security Answer: ");
            String answer = scanner.nextLine();
            System.out.print("New Password: ");
            String newPass = scanner.nextLine();

            if (newPass.isBlank()) {
                logger.warn("New password cannot be blank.");
                return;
            }

            if (!ValidationUtils.isValidPassword(newPass)) {
                logger.warn("Password must be at least 8 characters, with upper/lowercase, digit, and special char.");
                return;
            }

            boolean success = authService.recoverPassword(email, answer, newPass);
            if (success) {
                logger.info("Password reset successful. Please login.");
            } else {
                logger.warn("Recovery failed. Incorrect security answer.");
            }
        } catch (SQLException e) {
            logger.error("Recovery error: {}", e.getMessage());
        }
    }

    private User collectBasicUserInfo(UserRole role) {
        System.out.print("Name: ");
        String name = scanner.nextLine();
        System.out.print("Email: ");
        String email = scanner.nextLine();
        System.out.print("Password: ");
        String password = scanner.nextLine();
        System.out.print("Security Question: ");
        String question = scanner.nextLine();
        System.out.print("Security Answer: ");
        String answer = scanner.nextLine();

        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            logger.warn("Name, Email, and Password cannot be blank.");
            return null;
        }

        if (!ValidationUtils.isValidEmail(email)) {
            logger.warn("Invalid email format.");
            return null;
        }

        if (!ValidationUtils.isValidPassword(password)) {
            logger.warn("Password must be at least 8 characters, with upper/lowercase, digit, and special char.");
            return null;
        }

        return new User(name, email, password, role, question, answer);
    }
}
