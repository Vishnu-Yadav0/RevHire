package org.revhire.service;

import org.revhire.dao.NotificationDAO;
import org.revhire.model.Notification;

import java.sql.SQLException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationService {
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

    private final NotificationDAO notificationDAO;

    public NotificationService() {
        this.notificationDAO = new NotificationDAO();
    }

    public NotificationService(NotificationDAO notificationDAO) {
        this.notificationDAO = notificationDAO;
    }

    public void sendNotification(int userId, String message) throws SQLException {
        Notification notification = new Notification(userId, message);
        notificationDAO.createNotification(notification);
        logger.info("Sent notification to user ID: {}", userId);
    }

    public List<Notification> getUnreadNotifications(int userId) throws SQLException {
        return notificationDAO.getUnreadNotifications(userId);
    }

    public void markAsRead(int notificationId) throws SQLException {
        notificationDAO.markAsRead(notificationId);
    }
}
