package org.revhire.dao;

import org.revhire.config.DBConnection;
import org.revhire.model.User;
import org.revhire.model.User.UserRole;
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

public class UserDAOTest {

    @Mock
    private Connection mockConnection;
    @Mock
    private PreparedStatement mockStatement;
    @Mock
    private ResultSet mockResultSet;

    private UserDAO userDAO;
    private MockedStatic<DBConnection> mockedDbConnection;

    @BeforeEach
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);
        userDAO = new UserDAO();
        mockedDbConnection = mockStatic(DBConnection.class);
        mockedDbConnection.when(DBConnection::getInstance).thenReturn(mockConnection);
    }

    @AfterEach
    public void tearDown() {
        mockedDbConnection.close();
    }

    @Test
    public void shouldFetchUserByEmail() throws SQLException {
        String email = "test@example.com";
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt("id")).thenReturn(1);
        when(mockResultSet.getString("name")).thenReturn("Test User");
        when(mockResultSet.getString("email")).thenReturn(email);
        when(mockResultSet.getString("role")).thenReturn("JOB_SEEKER");
        when(mockResultSet.getString("password")).thenReturn("hashedPass");

        Optional<User> result = userDAO.getUserByEmail(email);

        assertTrue(result.isPresent());
        assertEquals("Test User", result.get().getName());
        assertEquals(email, result.get().getEmail());
        verify(mockStatement).setString(1, email);
    }

    @Test
    public void shouldReturnEmptyIfUserNotFound() throws SQLException {
        when(mockConnection.prepareStatement(anyString())).thenReturn(mockStatement);
        when(mockStatement.executeQuery()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(false);

        Optional<User> result = userDAO.getUserByEmail("notfound@example.com");

        assertFalse(result.isPresent());
    }

    @Test
    public void shouldCreateNewUserSuccessfully() throws SQLException {
        User user = new User("New User", "new@example.com", "pass", UserRole.JOB_SEEKER, "Q", "A");

        when(mockConnection.prepareStatement(anyString(), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(mockStatement);
        when(mockStatement.executeUpdate()).thenReturn(1);
        when(mockStatement.getGeneratedKeys()).thenReturn(mockResultSet);
        when(mockResultSet.next()).thenReturn(true);
        when(mockResultSet.getInt(1)).thenReturn(100);

        User created = userDAO.createUser(user);

        assertNotNull(created);
        assertEquals(100, created.getId());
        verify(mockStatement).executeUpdate();
    }
}
