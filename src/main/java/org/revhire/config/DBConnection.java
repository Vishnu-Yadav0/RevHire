package org.revhire.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final Logger logger = LoggerFactory.getLogger(DBConnection.class);

    // Singleton connection instance
    private static Connection connection = null;

    // Database configuration
    private static final String URL = "jdbc:mysql://localhost:3306/revhire";
    private static final String USER = "root";
    private static final String PASS = "root";

    private DBConnection() {
        // Private constructor for Singleton
    }

    public static Connection getInstance() throws SQLException {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(URL, USER, PASS);
                logger.info("New database connection established.");
            }
        } catch (SQLException e) {
            logger.error("DB Connection Error: {}", e.getMessage());
            throw e;
        }
        return connection;
    }

    public static void closeConnection() {
        try {
            if (connection != null) {
                if (!connection.isClosed()) {
                    connection.close();
                }
                logger.info("Database connection closed gracefully.");
            }
        } catch (SQLException e) {
            logger.error("Error while closing database connection: {}", e.getMessage());
        }
    }

    static {
        Runtime.getRuntime().addShutdownHook(new Thread(DBConnection::closeConnection));
    }
}
