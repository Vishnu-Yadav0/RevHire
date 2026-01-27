package org.revhire.controller;

import org.revhire.model.Application;
import org.revhire.model.Application.ApplicationStatus;
import org.revhire.model.Job;
import org.revhire.model.Job.JobStatus;

import org.revhire.service.ApplicationService;
import org.revhire.service.JobService;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import org.revhire.util.TableFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmployerMenu {
    private static final Logger logger = LoggerFactory.getLogger(EmployerMenu.class);
    private final Scanner scanner;
    private final JobService jobService = new JobService();
    private final ApplicationService applicationService = new ApplicationService();

    public EmployerMenu(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showMenu() {
        while (Session.isLoggedIn()) {
            logger.info("\n--- Employer Dashboard ({}) ---", Session.getCurrentUser().getName());
            logger.info("1. Post a Job");
            logger.info("2. Manage My Jobs");
            logger.info("3. Search Applicants (Not Implemented)");
            logger.info("4. Change Password");
            logger.info("5. Logout");
            System.out.print("Enter choice: ");

            String choice = scanner.nextLine();
            switch (choice) {
                case "1":
                    postJob();
                    break;
                case "2":
                    manageJobs();
                    break;
                case "3":
                    logger.info("Applicant search feature is coming soon.");
                    break;
                case "4":
                    changePassword();
                    break;
                case "5":
                    Session.clear();
                    logger.info("Logged out.");
                    break;
                default:
                    logger.warn("Invalid choice.");
            }
        }
    }

    private void postJob() {
        logger.info("\n--- Post a New Job ---");
        System.out.print("Job Title: ");
        String title = scanner.nextLine();
        System.out.print("Description: ");
        String desc = scanner.nextLine();
        System.out.print("Requirements: ");
        String reqs = scanner.nextLine();
        System.out.print("Location: ");
        String location = scanner.nextLine();
        System.out.print("Salary Range: ");
        String salary = scanner.nextLine();
        System.out.print("Job Type (Full-time/Part-time): ");
        String type = scanner.nextLine();

        Job job = new Job(Session.getCurrentUser().getId(), title, desc, reqs, location, salary, type, 0);
        System.out.print("Experience Years Required: ");
        try {
            job.setExperienceYears(Integer.parseInt(scanner.nextLine()));
        } catch (NumberFormatException e) {
            logger.warn("Invalid experience years. Defaulting to 0.");
        }

        try {
            jobService.postJob(job);
            logger.info("Job posted successfully! Job ID: {}", job.getId());
        } catch (SQLException e) {
            logger.error("Error posting job: {}", e.getMessage());
        }
    }

    private void manageJobs() {
        try {
            List<Job> jobs = jobService.getJobsByEmployer(Session.getCurrentUser().getId());
            if (jobs.isEmpty()) {
                logger.info("No jobs posted yet.");
                return;
            }

            logger.info("\n--- My Jobs ---");
            List<String[]> rows = new ArrayList<>();
            for (Job job : jobs) {
                rows.add(new String[] {
                        String.valueOf(job.getId()),
                        job.getTitle(),
                        job.getStatus().name(),
                        job.getLocation(),
                        job.getExperienceYears() + " yrs"
                });
            }
            TableFormatter.printTable("My Jobs",
                    new String[] { "ID", "Title", "Status", "Location", "Exp" }, rows);

            System.out.print("Enter Job ID to manage (or 0 to go back): ");
            int jobId;
            try {
                jobId = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                logger.warn("Invalid input. Please enter a numeric Job ID.");
                return;
            }
            if (jobId == 0)
                return;

            // Verify ownership
            Job selectedJob = jobs.stream().filter(j -> j.getId() == jobId).findFirst().orElse(null);
            if (selectedJob == null) {
                logger.warn("Invalid Job ID selected: {}", jobId);
                return;
            }

            manageSingleJob(selectedJob);

        } catch (Exception e) {
            logger.error("Error managing jobs: {}", e.getMessage());
        }
    }

    private void manageSingleJob(Job job) throws SQLException {
        while (true) {
            logger.info("\n--- Managing Job: {} ---", job.getTitle());
            logger.info("1. View Applicants");
            logger.info("2. Edit Job Details");
            logger.info("3. Close Job");
            logger.info("4. Reopen Job");
            logger.info("5. Delete Job");
            logger.info("6. Back");
            System.out.print("Enter choice: ");
            String choice = scanner.nextLine();

            switch (choice) {
                case "1":
                    viewApplicants(job.getId());
                    break;
                case "2":
                    editJob(job);
                    break;
                case "3":
                    jobService.closeJob(job.getId());
                    logger.info("Job closed.");
                    job.setStatus(JobStatus.CLOSED); // Update local ref
                    break;
                case "4":
                    jobService.reopenJob(job.getId());
                    logger.info("Job reopened.");
                    job.setStatus(JobStatus.OPEN);
                    break;
                case "5":
                    jobService.deleteJob(job.getId());
                    logger.info("Job deleted successfully.");
                    return;
                case "6":
                    return;
                default:
                    logger.warn("Invalid choice.");
            }
        }
    }

    private void viewApplicants(int jobId) {
        try {
            List<Application> apps = applicationService.getApplicationsByJob(jobId);
            if (apps.isEmpty()) {
                logger.info("No applicants found for job ID: {}", jobId);
                return;
            }
            logger.info("\n--- Applicants ---");
            List<String[]> rows = new ArrayList<>();
            for (Application app : apps) {
                rows.add(new String[] {
                        String.valueOf(app.getId()),
                        String.valueOf(app.getSeekerId()),
                        app.getStatus().name(),
                        app.getCoverLetter()
                });
            }
            TableFormatter.printTable("Applicants for Job " + jobId,
                    new String[] { "App ID", "Seeker ID", "Status", "Cover Letter" }, rows);

            System.out.print("Enter App ID to update status (or 0 to go back): ");
            int appId;
            try {
                appId = Integer.parseInt(scanner.nextLine());
            } catch (NumberFormatException e) {
                logger.warn("Invalid input. Please enter a numeric App ID.");
                return;
            }
            if (appId == 0)
                return;

            logger.info("Update status to: 1. SHORTLISTED  2. REJECTED");
            String statusChoice = scanner.nextLine();
            ApplicationStatus newStatus = null;
            if ("1".equals(statusChoice))
                newStatus = ApplicationStatus.SHORTLISTED;
            if ("2".equals(statusChoice))
                newStatus = ApplicationStatus.REJECTED;

            if (newStatus != null) {
                applicationService.updateApplicationStatus(appId, newStatus);
                logger.info("Status updated.");
            }

        } catch (Exception e) {
            logger.error("Error viewing/updating applicants: {}", e.getMessage());
        }
    }

    private void editJob(Job job) {
        logger.info("\n--- Edit Job Details (Enter to keep current) ---");
        System.out.print("New Title [" + job.getTitle() + "]: ");
        String title = scanner.nextLine();
        if (!title.isEmpty())
            job.setTitle(title);

        System.out.print("New Description [" + job.getDescription() + "]: ");
        String desc = scanner.nextLine();
        if (!desc.isEmpty())
            job.setDescription(desc);

        System.out.print("New Requirements [" + job.getRequirements() + "]: ");
        String reqs = scanner.nextLine();
        if (!reqs.isEmpty())
            job.setRequirements(reqs);

        System.out.print("New Salary [" + job.getSalaryRange() + "]: ");
        String salary = scanner.nextLine();
        if (!salary.isEmpty())
            job.setSalaryRange(salary);

        System.out.print("New Experience Years [" + job.getExperienceYears() + "]: ");
        String exp = scanner.nextLine();
        if (!exp.isEmpty()) {
            try {
                job.setExperienceYears(Integer.parseInt(exp));
            } catch (NumberFormatException e) {
                logger.warn("Invalid input. Experience years not updated.");
            }
        }

        try {
            jobService.updateJob(job);
            logger.info("Job updated successfully.");
        } catch (SQLException e) {
            logger.error("Error updating job: {}", e.getMessage());
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
            boolean success = new org.revhire.service.AuthService().updatePassword(Session.getCurrentUser().getEmail(),
                    currentPass, newPass);
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
