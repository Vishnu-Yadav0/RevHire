package org.revhire.controller;

import org.revhire.model.*;
import org.revhire.service.ApplicationService;
import org.revhire.service.JobService;
import org.revhire.service.NotificationService;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import org.revhire.service.AuthService;
import org.revhire.util.TableFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;

public class JobSeekerMenu {
    private static final Logger logger = LoggerFactory.getLogger(JobSeekerMenu.class);
    private final Scanner scanner;
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();
    private final NotificationService notificationService = new NotificationService();
    private final AuthService authService = new AuthService();

    public JobSeekerMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showMenu() {
        while (Session.isLoggedIn()) {
            logger.info("\n--- Job Seeker Dashboard ({}) ---", Session.getCurrentUser().getName());
            logger.info("1. Search Jobs");
            logger.info("2. My Applications");
            logger.info("3. My Profile");
            logger.info("4. Edit Profile");
            logger.info("5. Notifications");
            logger.info("6. Change Password");
            logger.info("7. Logout");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    searchJobs();
                    break;
                case "2":
                    myApplications();
                    break;
                case "3":
                    viewProfile();
                    break;
                case "4":
                    editProfile();
                    break;
                case "5":
                    checkNotifications();
                    break;
                case "6":
                    changePassword();
                    break;
                case "7":
                    Session.clear();
                    logger.info("Logged out.");
                    break;
                default:
                    logger.warn("Invalid choice.");
            }
        }
    }

    private void viewProfile() {
        try {
            Optional<JobSeeker> profileOpt = authService.getJobSeekerProfile(Session.getCurrentUser().getId());
            if (profileOpt.isEmpty()) {
                logger.warn("Profile not found for user ID: {}", Session.getCurrentUser().getId());
                return;
            }
            JobSeeker profile = profileOpt.get();
            logger.info("\n--- My Profile ---");
            logger.info("Phone: {}", profile.getPhone());

            logger.info("\n[Objectives]");
            profile.getObjectives().forEach(o -> logger.info("- {}", o));

            logger.info("\n[Education]");
            profile.getEducationList().forEach(e -> logger.info("- {}", e));

            logger.info("\n[Experience]");
            profile.getExperienceList().forEach(e -> logger.info("- {}", e));

            logger.info("\n[Skills]");
            profile.getSkills().forEach(s -> logger.info("- {}", s));

            logger.info("\n[Projects]");
            profile.getProjects().forEach(p -> logger.info("- {}", p));
            logger.info("------------------");

        } catch (SQLException e) {
            logger.error("Error fetching profile: {}", e.getMessage());
        }
    }

    private void searchJobs() {
        logger.info("\n--- Search Jobs ---");
        System.out.print("Keyword (title/desc, enter to skip): ");
        String keyword = scanner.nextLine();
        System.out.print("Location (enter to skip): ");
        String location = scanner.nextLine();
        System.out.print("Job Type (enter to skip): ");
        String type = scanner.nextLine();
        System.out.print("Company Name (enter to skip): ");
        String company = scanner.nextLine();
        System.out.print("Max Experience Years (enter to skip): ");
        String expStr = scanner.nextLine();
        Integer experience = expStr.isEmpty() ? null : Integer.parseInt(expStr);

        try {
            List<Job> jobs = jobService.searchJobs(keyword.isEmpty() ? null : keyword,
                    location.isEmpty() ? null : location,
                    type.isEmpty() ? null : type,
                    experience,
                    company.isEmpty() ? null : company);
            if (jobs.isEmpty()) {
                logger.info("No jobs found matching the criteria.");
                return;
            }

            List<String[]> rows = new ArrayList<>();
            for (Job job : jobs) {
                rows.add(new String[] {
                        String.valueOf(job.getId()),
                        job.getTitle(),
                        job.getLocation(),
                        job.getJobType(),
                        job.getExperienceYears() + " yrs",
                        job.getSalaryRange()
                });
            }
            TableFormatter.printTable("Available Jobs",
                    new String[] { "ID", "Title", "Location", "Type", "Exp", "Salary" }, rows);

            System.out.print("Enter Job ID to view details (or 0 to go back): ");
            int jobId;
            try {
                jobId = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                logger.warn("Invalid input. Please enter a numeric Job ID.");
                return;
            }
            if (jobId == 0)
                return;

            Job selected = jobs.stream().filter(j -> j.getId() == jobId).findFirst().orElse(null);
            if (selected == null) {
                logger.warn("Invalid ID.");
                return;
            }

            logger.info("Title: {}", selected.getTitle());
            logger.info("Description: {}", selected.getDescription());
            logger.info("Requirements: {}", selected.getRequirements());
            logger.info("Salary: {}", selected.getSalaryRange());

            System.out.print("Do you want to apply? (y/n): ");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                System.out.print("Cover Letter (Optional): ");
                String cover = scanner.nextLine();
                applicationService.applyForJob(selected.getId(), Session.getCurrentUser().getId(), cover);
                logger.info("Applied successfully for job ID: {}", selected.getId());
            }

        } catch (Exception e) {
            logger.error("Error: {}", e.getMessage());
        }
    }

    private void myApplications() {
        try {
            List<Application> apps = applicationService.getApplicationsBySeeker(Session.getCurrentUser().getId());
            if (apps.isEmpty()) {
                logger.info("No applications found for your account.");
                return;
            }

            logger.info("\n--- My Applications ---");
            List<String[]> rows = new ArrayList<>();
            for (Application app : apps) {
                rows.add(new String[] {
                        String.valueOf(app.getId()),
                        String.valueOf(app.getJobId()),
                        app.getStatus().name(),
                        app.getAppliedAt().toString()
                });
            }
            TableFormatter.printTable("My Applications",
                    new String[] { "App ID", "Job ID", "Status", "Applied At" }, rows);

            System.out.print("Enter App ID to withdraw (or 0 to go back): ");
            int appId;
            try {
                appId = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                logger.warn("Invalid input. Please enter a numeric App ID.");
                return;
            }
            if (appId == 0)
                return;

            applicationService.withdrawApplication(appId);
            logger.info("Application withdrawn successfully.");

        } catch (Exception e) {
            logger.error("Error managing applications: {}", e.getMessage());
        }
    }

    private void checkNotifications() {
        try {
            List<Notification> notifs = notificationService.getUnreadNotifications(Session.getCurrentUser().getId());
            if (notifs.isEmpty()) {
                logger.info("No new notifications.");
                return;
            }
            logger.info("\n--- New Notifications ---");
            for (Notification n : notifs) {
                logger.info("- {}", n.getMessage());
                notificationService.markAsRead(n.getId());
            }
        } catch (SQLException e) {
            logger.error("Error checking notifications: {}", e.getMessage());
        }
    }

    private void editProfile() {
        try {
            Optional<JobSeeker> profileOpt = authService.getJobSeekerProfile(Session.getCurrentUser().getId());
            if (profileOpt.isEmpty()) {
                logger.warn("Profile not found.");
                return;
            }
            JobSeeker profile = profileOpt.get();
            logger.info("\n--- Edit Profile ---");
            System.out.print("New Phone [" + profile.getPhone() + "]: ");
            String phone = scanner.nextLine();
            if (!phone.isEmpty())
                profile.setPhone(phone);

            logger.info("Do you want to clear and re-enter resume details? (y/n)");
            if (scanner.nextLine().equalsIgnoreCase("y")) {
                profile.getObjectives().clear();
                profile.getEducationList().clear();
                profile.getExperienceList().clear();
                profile.getSkills().clear();
                profile.getProjects().clear();

                // Reuse collection logic (this is a bit redundant with AuthController, but fine
                // for now)
                logger.info("[Resume] Objectives (Type 'NEXT' to finish):");
                while (true) {
                    System.out.print("- ");
                    String line = scanner.nextLine();
                    if (line.equalsIgnoreCase("NEXT"))
                        break;
                    profile.getObjectives().add(line);
                }

                logger.info("\n[Resume] Education (Type 'DONE' to skip/finish):");
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

                logger.info("\n[Resume] Experience (Type 'DONE' to skip/finish):");
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

                logger.info("\n[Resume] Skills (Type 'NEXT' to finish):");
                while (true) {
                    System.out.print("- ");
                    String line = scanner.nextLine();
                    if (line.equalsIgnoreCase("NEXT"))
                        break;
                    profile.addSkill(new Skill(line));
                }

                logger.info("\n[Resume] Projects (Type 'DONE' to skip/finish):");
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
            }

            // In a real app, we'd allow editing individual lists. Here we'll just save what
            // we have.
            authService.updateJobSeekerProfile(profile);
            logger.info("Profile updated successfully.");

        } catch (Exception e) {
            logger.error("Error updating profile: {}", e.getMessage());
        }
    }

    private void changePassword() {
        logger.info("\n--- Change Password ---");
        System.out.print("Current Password: ");
        String currentPass = scanner.nextLine();
        System.out.print("New Password: ");
        String newPass = scanner.nextLine();
        if (newPass.isBlank() || !org.revhire.util.ValidationUtils.isValidPassword(newPass)) {
            logger.warn("Password does not meet requirements (8+ chars, upper, lower, digit, special).");
            return;
        }

        try {
            boolean success = authService.updatePassword(Session.getCurrentUser().getEmail(), currentPass, newPass);
            if (success) {
                logger.info("Password changed successfully.");
            } else {
                logger.warn("Password change failed. Incorrect current password.");
            }
        } catch (SQLException e) {
            logger.error("Error changing password: {}", e.getMessage());
        }
    }
}
