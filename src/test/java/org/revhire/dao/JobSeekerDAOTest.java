package org.revhire.dao;

import org.revhire.config.DBConnection;
import org.revhire.model.JobSeeker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class JobSeekerDAOTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private JobSeekerDAO jobSeekerDAO;
    private MockedStatic<DBConnection> mockedDbConnection;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        jobSeekerDAO = new JobSeekerDAO();
        mockedDbConnection = mockStatic(DBConnection.class);
        mockedDbConnection.when(DBConnection::getInstance).thenReturn(mockConnection);
    }

    @AfterEach
    public void tearDown() {
        mockedDbConnection.close();
    }

    @Test
    public void testCreateProfile_TransactionFlow() throws SQLException {
        JobSeeker profile = new JobSeeker(1, "1234567890");
        profile.addObjective("Find a job");

        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);

        jobSeekerDAO.createProfile(profile);

        verify(mockConnection).setAutoCommit(false);
        verify(mockConnection).commit();
        verify(mockConnection).setAutoCommit(true);
        verify(mockStatement, atLeastOnce()).executeUpdate();
        verify(mockStatement, atLeastOnce()).executeBatch();
    }

    @Test
    public void testCreateProfile_RollbackOnFailure() throws SQLException {
        JobSeeker profile = new JobSeeker(1, "123");
        when(mockConnection.prepareStatement(anyString())).thenThrow(new SQLException("DB Down"));

        assertThrows(SQLException.class, () -> jobSeekerDAO.createProfile(profile));

        verify(mockConnection).rollback();
    }
}
