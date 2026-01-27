package org.revhire.dao;

import org.revhire.config.DBConnection;
import org.revhire.model.Application;
import org.revhire.model.Application.ApplicationStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import java.sql.*;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ApplicationDAOTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private ApplicationDAO applicationDAO;
    private MockedStatic<DBConnection> mockedDbConnection;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        applicationDAO = new ApplicationDAO();
        mockedDbConnection = mockStatic(DBConnection.class);
        mockedDbConnection.when(DBConnection::getInstance).thenReturn(mockConnection);
    }

    @AfterEach
    public void tearDown() {
        mockedDbConnection.close();
    }

    @Test
    public void testApply_Success() throws SQLException {
        Application app = new Application(1, 2, "Cover Letter");

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(500);

        applicationDAO.apply(app);

        assertEquals(500, app.getId());
        verify(mockStatement).executeUpdate();
    }

    @Test
    public void testGetApplicationById_Success() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(10);
        when(mockResultSet.getString("status")).thenReturn("APPLIED");

        Optional<Application> result = applicationDAO.getApplicationById(10);

        assertTrue(result.isPresent());
        assertEquals(ApplicationStatus.APPLIED, result.get().getStatus());
    }

    @Test
    public void testHasApplied_True() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(1);

        boolean result = applicationDAO.hasApplied(1, 1);

        assertTrue(result);
    }
}
