package org.revhire.dao;

import org.revhire.config.DBConnection;
import org.revhire.model.Notification;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NotificationDAO {
    private static final Logger logger = LoggerFactory.getLogger(NotificationDAO.class);

    public void createNotification(Notification notification) throws SQLException {
        String query = "INSERT INTO notifications (user_id, message, is_read) VALUES (?, ?, ?)";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notification.getUserId());
            stmt.setString(2, notification.getMessage());
            stmt.setBoolean(3, notification.isRead());

            stmt.executeUpdate();
            logger.info("Notification created for user: {}", notification.getUserId());
        }
    }

    public List<Notification> getUnreadNotifications(int userId) throws SQLException {
        List<Notification> notifications = new ArrayList<>();
        String query = "SELECT * FROM notifications WHERE user_id = ? AND is_read = FALSE ORDER BY created_at DESC";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Notification notif = new Notification();
                    notif.setId(rs.getInt("id"));
                    notif.setUserId(rs.getInt("user_id"));
                    notif.setMessage(rs.getString("message"));
                    notif.setRead(rs.getBoolean("is_read"));
                    notif.setCreatedAt(rs.getTimestamp("created_at"));
                    notifications.add(notif);
                }
            }
        }
        return notifications;
    }

    public void markAsRead(int notificationId) throws SQLException {
        String query = "UPDATE notifications SET is_read = TRUE WHERE id = ?";
        try (Connection conn = getConnection();
                PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, notificationId);
            stmt.executeUpdate();
        }
    }

    private Connection getConnection() throws SQLException {
        try {
            return DBConnection.getInstance();
        } catch (Exception e) {
            throw new SQLException(e);
        }
    }
}
