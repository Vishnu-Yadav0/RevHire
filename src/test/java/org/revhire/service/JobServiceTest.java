package org.revhire.service;

import org.revhire.dao.JobDAO;
import org.revhire.model.Job;
import org.revhire.model.Job.JobStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class JobServiceTest {

    @Mock
    private JobDAO jobDAO;

    private JobService jobService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        jobService = new JobService(jobDAO);
    }

    @Test
    public void testPostJob() throws SQLException {
        Job job = new Job();
        job.setTitle("Software Engineer");

        when(jobDAO.createJob(job)).thenReturn(job);

        Job result = jobService.postJob(job);
        assertEquals("Software Engineer", result.getTitle());
        verify(jobDAO).createJob(job);
    }

    @Test
    public void testSearchJobs_Success() throws SQLException {
        Job job = new Job();
        job.setTitle("Java Developer");

        when(jobDAO.searchJobs("Java", "Remote", "Full-time", null, null))
                .thenReturn(Collections.singletonList(job));

        List<Job> results = jobService.searchJobs("Java", "Remote", "Full-time", null, null);
        assertEquals(1, results.size());
        assertEquals("Java Developer", results.get(0).getTitle());
    }

    @Test
    public void testSearchJobs_NoResults() throws SQLException {
        when(jobDAO.searchJobs(any(), any(), any(), any(), any()))
                .thenReturn(Collections.emptyList());

        List<Job> results = jobService.searchJobs("NonExistent", "Mars", "Remote", null, null);
        assertTrue(results.isEmpty());
    }

    @Test
    public void testCloseJob() throws SQLException {
        int jobId = 101;
        doNothing().when(jobDAO).updateStatus(jobId, JobStatus.CLOSED);

        jobService.closeJob(jobId);
        verify(jobDAO).updateStatus(jobId, JobStatus.CLOSED);
    }

    @Test
    public void testDeleteJob() throws SQLException {
        int jobId = 101;
        doNothing().when(jobDAO).deleteJob(jobId);

        jobService.deleteJob(jobId);
        verify(jobDAO).deleteJob(jobId);
    }

    @Test
    public void testUpdateJob() throws SQLException {
        Job job = new Job();
        job.setId(1);
        doNothing().when(jobDAO).updateJob(job);

        jobService.updateJob(job);
        verify(jobDAO).updateJob(job);
    }
}
