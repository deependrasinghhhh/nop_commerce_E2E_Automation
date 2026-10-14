package com.qa.nopcommerce.database;

import com.qa.nopcommerce.config.ConfigReader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Thread-safe Database Manager providing high-performance pooled read-only JDBC verification
 * against nopCommerce MSSQL database using HikariCP.
 * Supports automated verification of customer records, order records, and shopping cart persistence.
 */
public class DatabaseManager {
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static volatile HikariDataSource dataSource;

    private static String getJdbcUrl() {
        String host = ConfigReader.get("dbHost", "localhost");
        String port = ConfigReader.get("dbPort", "1433");
        String dbName = ConfigReader.get("dbName", "nopcommerce_db");
        String encrypt = ConfigReader.get("dbEncrypt", "false");
        String trustCert = ConfigReader.get("dbTrustServerCertificate", "true");

        return String.format(
                "jdbc:sqlserver://%s:%s;databaseName=%s;encrypt=%s;trustServerCertificate=%s;loginTimeout=3;",
                host, port, dbName, encrypt, trustCert
        );
    }

    private static synchronized HikariDataSource getDataSource() {
        if (dataSource == null || dataSource.isClosed()) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(getJdbcUrl());
            config.setUsername(ConfigReader.get("dbUser", "sa"));
            config.setPassword(ConfigReader.get("dbPassword", "NopCommerce@2026!"));
            config.setMaximumPoolSize(5);
            config.setMinimumIdle(1);
            config.setIdleTimeout(30000);
            config.setConnectionTimeout(3000);
            config.setPoolName("nopCommerce-HikariPool");
            config.setDriverClassName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            dataSource = new HikariDataSource(config);
            log.info("Initialized HikariCP connection pool for {}", config.getJdbcUrl());
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        try {
            return getDataSource().getConnection();
        } catch (Exception e) {
            log.warn("HikariCP connection checkout failed ({}), falling back to direct connection", e.getMessage());
            return DriverManager.getConnection(
                    getJdbcUrl(),
                    ConfigReader.get("dbUser", "sa"),
                    ConfigReader.get("dbPassword", "NopCommerce@2026!")
            );
        }
    }

    public static synchronized void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("Closed HikariCP connection pool");
        }
    }

    public static boolean isDatabaseAccessible() {
        try (Connection conn = getConnection()) {
            return conn != null && !conn.isClosed();
        } catch (Exception e) {
            log.warn("MSSQL database instance is currently unreachable at {}: {}", getJdbcUrl(), e.getMessage());
            return false;
        }
    }

    public static boolean customerExists(String email) {
        String sql = "SELECT COUNT(1) AS TotalCount FROM Customer WHERE Email = ? AND Deleted = 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TotalCount") > 0;
                }
            }
        } catch (SQLException e) {
            log.error("Database query failed for customerExists: {}", e.getMessage());
        }
        return false;
    }

    public static boolean orderExists(int orderId) {
        String sql = "SELECT COUNT(1) AS TotalCount FROM [Order] WHERE Id = ? AND Deleted = 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TotalCount") > 0;
                }
            }
        } catch (SQLException e) {
            log.error("Database query failed for orderExists: {}", e.getMessage());
        }
        return false;
    }

    public static int getShoppingCartItemCount(int customerId) {
        String sql = "SELECT COUNT(1) AS TotalItems FROM ShoppingCartItem WHERE CustomerId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TotalItems");
                }
            }
        } catch (SQLException e) {
            log.error("Database query failed for getShoppingCartItemCount: {}", e.getMessage());
        }
        return 0;
    }
}
