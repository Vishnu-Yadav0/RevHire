package org.revhire.service;

import org.revhire.dao.NotificationDAO;
import org.revhire.model.Notification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class NotificationServiceTest {

    @Mock
    private NotificationDAO notificationDAO;

    private NotificationService notificationService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        notificationService = new NotificationService(notificationDAO);
    }

    @Test
    public void testSendNotification() throws SQLException {
        doNothing().when(notificationDAO).createNotification(any(Notification.class));

        notificationService.sendNotification(1, "Welcome");

        verify(notificationDAO)
                .createNotification(argThat(n -> n.getUserId() == 1 && n.getMessage().equals("Welcome")));
    }

    @Test
    public void testGetUnreadNotifications() throws SQLException {
        Notification n = new Notification(1, "Msg");
        when(notificationDAO.getUnreadNotifications(1)).thenReturn(Collections.singletonList(n));

        List<Notification> result = notificationService.getUnreadNotifications(1);
        assertEquals(1, result.size());
        assertEquals("Msg", result.get(0).getMessage());
    }
}
