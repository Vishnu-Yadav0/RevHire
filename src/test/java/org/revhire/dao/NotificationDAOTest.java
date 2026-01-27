package org.revhire.dao;

import org.revhire.config.DBConnection;
import org.revhire.model.Notification;
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

public class NotificationDAOTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private NotificationDAO notificationDAO;
    private MockedStatic<DBConnection> mockedDbConnection;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        notificationDAO = new NotificationDAO();
        mockedDbConnection = mockStatic(DBConnection.class);
        mockedDbConnection.when(DBConnection::getInstance).thenReturn(mockConnection);
    }

    @AfterEach
    public void tearDown() {
        mockedDbConnection.close();
    }

    @Test
    public void testCreateNotification() throws SQLException {
        Notification n = new Notification(1, "Test Message");
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);

        notificationDAO.createNotification(n);

        verify(mockStatement).setInt(1, 1);
        verify(mockStatement).setString(2, "Test Message");
        verify(mockStatement).executeUpdate();
    }

    @Test
    public void testGetUnreadNotifications() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true, false);
        when(mockResultSet.getString("message")).thenReturn("Hello User");

        List<Notification> results = notificationDAO.getUnreadNotifications(1);

        assertEquals(1, results.size());
        assertEquals("Hello User", results.get(0).getMessage());
    }
}
