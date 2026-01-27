package org.revhire;

import org.revhire.controller.*;
import org.revhire.model.User;
import org.revhire.model.User.UserRole;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);
    private static final Scanner scanner = new Scanner(System.in);
    private static final AuthController authController = new AuthController(scanner);
    private static final JobSeekerMenu jobSeekerMenu = new JobSeekerMenu(scanner);
    private static final EmployerMenu employerMenu = new EmployerMenu(scanner);

    public static void main(String[] args) {
        logger.info("Starting RevHire Job Portal...");

        while (true) {
            if (!Session.isLoggedIn()) {
                showPublicMenu();
            } else {
                User user = Session.getCurrentUser();
                if (user.getRole() == UserRole.JOB_SEEKER) {
                    jobSeekerMenu.showMenu();
                } else {
                    employerMenu.showMenu();
                }
            }
        }
    }

    private static void showPublicMenu() {
        logger.info("\n=== RevHire Job Portal ===");
        logger.info("1. Login");
        logger.info("2. Register as Job Seeker");
        logger.info("3. Register as Employer");
        logger.info("4. Forgot Password");
        logger.info("5. Exit");
        System.out.print("Enter choice: ");

        String choice = scanner.nextLine();
        switch (choice) {
            case "1":
                authController.login();
                break;
            case "2":
                authController.registerJobSeeker();
                break;
            case "3":
                authController.registerEmployer();
                break;
            case "4":
                authController.forgotPassword();
                break;
            case "5":
                logger.info("Goodbye!");
                org.revhire.config.DBConnection.closeConnection();
                System.exit(0);
                break;
            default:
                logger.warn("Invalid choice. Please try again.");
        }
    }
}