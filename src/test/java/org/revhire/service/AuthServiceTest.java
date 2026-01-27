package org.revhire.service;

import org.revhire.dao.EmployerDAO;
import org.revhire.dao.JobSeekerDAO;
import org.revhire.dao.UserDAO;
import org.revhire.model.JobSeeker;
import org.revhire.model.User;
import org.revhire.model.User.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Optional;
import org.revhire.util.PasswordUtils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserDAO userDAO;
    @Mock
    private JobSeekerDAO jobSeekerDAO;
    @Mock
    private EmployerDAO employerDAO;

    private AuthService authService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthService(userDAO, jobSeekerDAO, employerDAO);
    }

    @Test
    public void shouldRegisterUserSuccessfully() throws SQLException {
        User user = new User("John", "john@example.com", "pass", UserRole.JOB_SEEKER, "Q", "A");
        JobSeeker profile = new JobSeeker(0, "123");

        when(userDAO.getUserByEmail("john@example.com")).thenReturn(Optional.empty());
        when(userDAO.createUser(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(1);
            return u;
        });

        User created = authService.registerJobSeeker(user, profile);

        assertNotNull(created);
        assertEquals(1, created.getId());
        assertTrue(PasswordUtils.checkPassword("pass", created.getPassword()));
        verify(jobSeekerDAO).createProfile(profile);
    }

    @Test
    public void shouldFailOnDuplicateEmail() throws SQLException {
        User user = new User("John", "john@example.com", "pass", UserRole.JOB_SEEKER, "Q", "A");
        JobSeeker profile = new JobSeeker(0, "123");

        when(userDAO.getUserByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(SQLException.class, () -> authService.registerJobSeeker(user, profile));
        verify(userDAO, never()).createUser(any());
    }

    @Test
    public void shouldLoginWithValidCredentials() throws SQLException {
        String password = "pass";
        String hashed = PasswordUtils.hashPassword(password);
        User user = new User("John", "john@example.com", hashed, UserRole.JOB_SEEKER, "Q", "A");
        when(userDAO.getUserByEmail("john@example.com")).thenReturn(Optional.of(user));

        User loggedIn = authService.login("john@example.com", password);
        assertEquals(user, loggedIn);
    }

    @Test
    public void shouldRejectInvalidPassword() throws SQLException {
        String hashed = PasswordUtils.hashPassword("pass");
        User user = new User("John", "john@example.com", hashed, UserRole.JOB_SEEKER, "Q", "A");
        when(userDAO.getUserByEmail("john@example.com")).thenReturn(Optional.of(user));

        assertThrows(SQLException.class, () -> authService.login("john@example.com", "wrongpass"));
    }

    @Test
    public void shouldRecoverPasswordSuccessfully() throws SQLException {
        String oldHashed = PasswordUtils.hashPassword("pass");
        User user = new User("John", "john@example.com", oldHashed, UserRole.JOB_SEEKER, "Q", "A");
        when(userDAO.getUserByEmail("john@example.com")).thenReturn(Optional.of(user));
        when(userDAO.updatePassword(eq("john@example.com"), anyString())).thenReturn(true);

        boolean result = authService.recoverPassword("john@example.com", "A", "newpass");
        assertTrue(result);
        verify(userDAO).updatePassword(eq("john@example.com"),
                argThat(hashed -> PasswordUtils.checkPassword("newpass", hashed)));
    }

    @Test
    public void shouldFailRecoveryWithWrongAnswer() throws SQLException {
        User user = new User("John", "john@example.com", "pass", UserRole.JOB_SEEKER, "Q", "A");
        when(userDAO.getUserByEmail("john@example.com")).thenReturn(Optional.of(user));

        boolean result = authService.recoverPassword("john@example.com", "Wrong", "newpass");
        assertFalse(result);
        verify(userDAO, never()).updatePassword(anyString(), anyString());
    }
}
