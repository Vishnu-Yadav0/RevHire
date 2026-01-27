package org.revhire.dao;

import org.revhire.config.DBConnection;
import org.revhire.model.Job;
import org.revhire.model.Job.JobStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class JobDAOTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private JobDAO jobDAO;
    private MockedStatic<DBConnection> mockedDbConnection;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        jobDAO = new JobDAO();
        mockedDbConnection = mockStatic(DBConnection.class);
        mockedDbConnection.when(DBConnection::getInstance).thenReturn(mockConnection);
    }

    @AfterEach
    public void tearDown() {
        mockedDbConnection.close();
    }

    @Test
    public void shouldSearchJobsWithFilters() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);

        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("title")).thenReturn("Software Engineer");
        when(mockResultSet.getString("status")).thenReturn("OPEN");
        when(mockResultSet.getInt("experience_years")).thenReturn(3);

        List<Job> results = jobDAO.searchJobs("Software", "New York", "Full-time", 5, "Google");

        assertFalse(results.isEmpty());
        assertEquals(1, results.size());
        assertEquals("Software Engineer", results.get(0).getTitle());
    }

    @Test
    public void shouldUpdateJobStatus() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        jobDAO.updateStatus(1, JobStatus.CLOSED);

        verify(mockStatement).setString(1, "CLOSED");
        verify(mockStatement).setInt(2, 1);
        verify(mockStatement).executeUpdate();
    }
}
