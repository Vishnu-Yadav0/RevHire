package org.revhire.service;

import org.revhire.dao.ApplicationDAO;
import org.revhire.dao.JobDAO;
import org.revhire.model.Application;
import org.revhire.model.Application.ApplicationStatus;
import org.revhire.model.Job;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ApplicationServiceTest {

    @Mock
    private ApplicationDAO applicationDAO;
    @Mock
    private JobDAO jobDAO;
    @Mock
    private NotificationService notificationService;

    private ApplicationService applicationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        // We use constructor injection for tests
        applicationService = new ApplicationService(applicationDAO, jobDAO, notificationService);
    }

    @Test
    public void shouldApplyForJobSuccessfully() throws SQLException {
        int jobId = 1;
        int seekerId = 2;
        Job job = new Job();
        job.setId(jobId);
        job.setEmployerId(3);
        job.setTitle("Java Dev");

        when(jobDAO.getJobById(jobId)).thenReturn(Optional.of(job));
        doNothing().when(applicationDAO).apply(any(Application.class));

        applicationService.applyForJob(jobId, seekerId, "I love Java");

        verify(applicationDAO).apply(any(Application.class));
        verify(notificationService).sendNotification(eq(3), contains("Java Dev"));
    }

    @Test
    public void shouldFailIfJobDoesNotExist() throws SQLException {
        when(jobDAO.getJobById(1)).thenReturn(Optional.empty());

        assertThrows(SQLException.class, () -> applicationService.applyForJob(1, 2, "Hi"));
        verify(applicationDAO, never()).apply(any());
    }

    @Test
    public void shouldUpdateApplicationStatusAndNotifySeeker() throws SQLException {
        int appId = 10;
        Application app = new Application(1, 2, "Letter");
        app.setId(appId);
        app.setJobId(1);
        app.setSeekerId(2);

        Job job = new Job();
        job.setTitle("Java Dev");

        when(applicationDAO.getApplicationById(appId)).thenReturn(Optional.of(app));
        when(jobDAO.getJobById(1)).thenReturn(Optional.of(job));
        doNothing().when(applicationDAO).updateStatus(appId, ApplicationStatus.SHORTLISTED);

        applicationService.updateApplicationStatus(appId, ApplicationStatus.SHORTLISTED);

        verify(applicationDAO).updateStatus(appId, ApplicationStatus.SHORTLISTED);
        verify(notificationService).sendNotification(eq(2), contains("SHORTLISTED"));
    }
}
