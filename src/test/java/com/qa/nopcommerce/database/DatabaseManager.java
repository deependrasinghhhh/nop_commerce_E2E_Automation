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
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.regex.Pattern;

/**
 * Thread-safe Database Manager providing pooled JDBC verification and isolated test-data
 * cleanup against the nopCommerce MSSQL database using HikariCP.
 *
 * <p>Requires {@code DB_USER} and {@code DB_PASSWORD} via {@link ConfigReader}; never falls
 * back to hardcoded or empty credentials.</p>
 */
public class DatabaseManager {
    private static final Logger log = LoggerFactory.getLogger(DatabaseManager.class);
    private static final Pattern ISOLATED_CUSTOMER_EMAIL = Pattern.compile(
            "^(?:nop_(?:qa|profile|addr|deladdr|orders|reviewer)_[0-9a-f]{8}|"
                    + "nop_(?:qa|profile|addr|deladdr|orders|reviewer)_[0-9a-f]{32}|"
                    + "qa_db_cart_[0-9a-f]{32})@example\\.com$",
            Pattern.CASE_INSENSITIVE);
    private static volatile HikariDataSource dataSource;

    private DatabaseManager() {
        // Utility class
    }

    private static String getJdbcUrl() {
        String host = ConfigReader.getDbHost();
        int port = ConfigReader.getDbPort();
        String dbName = ConfigReader.getDbName();
        boolean encrypt = ConfigReader.isDbEncrypt();
        boolean trustCert = ConfigReader.isDbTrustServerCertificate();

        return String.format(
                "jdbc:sqlserver://%s:%d;databaseName=%s;encrypt=%b;trustServerCertificate=%b;loginTimeout=3;",
                host, port, dbName, encrypt, trustCert
        );
    }

    private static synchronized HikariDataSource getDataSource() {
        // Fail fast if required DB credentials are not configured
        String dbUser = ConfigReader.getDbUser();
        String dbPassword = ConfigReader.getDbPassword();

        if (dataSource == null || dataSource.isClosed()) {
            HikariConfig config = new HikariConfig();
            config.setJdbcUrl(getJdbcUrl());
            config.setUsername(dbUser);
            config.setPassword(dbPassword);
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
        String dbUser = ConfigReader.getDbUser();
        String dbPassword = ConfigReader.getDbPassword();
        try {
            return getDataSource().getConnection();
        } catch (Exception e) {
            log.warn("HikariCP connection checkout failed ({}), attempting direct JDBC connection", e.getMessage());
            return DriverManager.getConnection(getJdbcUrl(), dbUser, dbPassword);
        }
    }

    public static synchronized void closePool() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            log.info("Closed HikariCP connection pool");
        }
    }

    /**
     * Validates that DB credentials are configured (throws {@link IllegalStateException} if missing)
     * and checks whether the MSSQL instance is reachable.
     */
    public static boolean isDatabaseAccessible() {
        // Ensure missing secrets fail clearly rather than being swallowed
        ConfigReader.getDbUser();
        ConfigReader.getDbPassword();

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
            throw new IllegalStateException("Database query failed for customerExists", e);
        }
        throw new IllegalStateException("Customer existence query returned no result row");
    }

    public static OptionalInt findCustomerIdByEmail(String email) {
        String sql = "SELECT TOP (1) Id FROM Customer WHERE Email = ? AND Deleted = 0 ORDER BY Id DESC";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next() ? OptionalInt.of(rs.getInt("Id")) : OptionalInt.empty();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed for findCustomerIdByEmail", e);
        }
    }

    public static int getCustomerIdByEmail(String email) {
        return findCustomerIdByEmail(email).orElseThrow(() ->
                new IllegalStateException("No active customer found for email " + email));
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
            throw new IllegalStateException("Database query failed for orderExists", e);
        }
        throw new IllegalStateException("Order existence query returned no result row");
    }

    public static int getActiveOrderCount() {
        String sql = "SELECT COUNT(1) AS TotalCount FROM [Order] WHERE Deleted = 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("TotalCount");
            }
            throw new IllegalStateException("Active order count query returned no result row");
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed for getActiveOrderCount", e);
        }
    }

    public static int getActiveOrderCountWithItems() {
        String sql = "SELECT COUNT(DISTINCT o.Id) AS TotalCount " +
                "FROM [Order] o INNER JOIN OrderItem oi ON oi.OrderId = o.Id WHERE o.Deleted = 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) {
                return rs.getInt("TotalCount");
            }
            throw new IllegalStateException("Active order-with-items query returned no result row");
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed for getActiveOrderCountWithItems", e);
        }
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
            throw new IllegalStateException("Database query failed for getShoppingCartItemCount", e);
        }
        throw new IllegalStateException("Shopping cart item count query returned no result row");
    }

    public static int deleteShoppingCartItemsForCustomer(int customerId) {
        String sql = "DELETE FROM ShoppingCartItem WHERE CustomerId = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            throw new IllegalStateException("Database cleanup failed for ShoppingCartItem", e);
        }
    }

    public static int softDeleteCustomerByEmail(String email) {
        if (email == null || email.isBlank()) {
            return 0;
        }
        String sql = "UPDATE Customer SET Deleted = 1 WHERE Email = ? AND Deleted = 0";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, email);
            int deleted = stmt.executeUpdate();
            log.info("Soft-deleted {} isolated customer record(s) for email '{}'", deleted, email);
            return deleted;
        } catch (SQLException e) {
            throw new IllegalStateException("Database cleanup failed for isolated customer", e);
        }
    }

    /**
     * Physically removes one account created by an isolated customer test.
     * The email allowlist prevents this cleanup path from targeting seeded accounts.
     */
    public static int deleteIsolatedCustomerByEmail(String email) {
        if (email == null || email.isBlank()) {
            return 0;
        }
        if (!ISOLATED_CUSTOMER_EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Refusing isolated-customer cleanup for an unrecognized test email.");
        }

        String customerIdSql = "SELECT TOP (1) Id FROM Customer WHERE Email = ? ORDER BY Id DESC";
        String addressIdsSql = "SELECT Address_Id FROM CustomerAddresses WHERE Customer_Id = ?";
        String deleteGenericAttributesSql =
                "DELETE FROM GenericAttribute WHERE KeyGroup = 'Customer' AND EntityId = ?";
        String deleteCustomWishlistSql = "DELETE FROM CustomWishlist WHERE CustomerId = ?";
        String deleteCustomerSql = "DELETE FROM Customer WHERE Id = ?";
        String deleteOrphanAddressSql = "DELETE FROM Address WHERE Id = ? " +
                "AND NOT EXISTS (SELECT 1 FROM Customer WHERE BillingAddress_Id = ? OR ShippingAddress_Id = ?) " +
                "AND NOT EXISTS (SELECT 1 FROM [Order] WHERE BillingAddressId = ? " +
                "OR PickupAddressId = ? OR ShippingAddressId = ?) " +
                "AND NOT EXISTS (SELECT 1 FROM Affiliate WHERE AddressId = ?)";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                int customerId;
                try (PreparedStatement statement = connection.prepareStatement(customerIdSql)) {
                    statement.setString(1, email);
                    try (ResultSet result = statement.executeQuery()) {
                        if (!result.next()) {
                            connection.commit();
                            return 0;
                        }
                        customerId = result.getInt("Id");
                    }
                }

                List<Integer> addressIds = new ArrayList<>();
                try (PreparedStatement statement = connection.prepareStatement(addressIdsSql)) {
                    statement.setInt(1, customerId);
                    try (ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            addressIds.add(result.getInt("Address_Id"));
                        }
                    }
                }

                try (PreparedStatement statement = connection.prepareStatement(deleteGenericAttributesSql)) {
                    statement.setInt(1, customerId);
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(deleteCustomWishlistSql)) {
                    statement.setInt(1, customerId);
                    statement.executeUpdate();
                }
                int deletedCustomers;
                try (PreparedStatement statement = connection.prepareStatement(deleteCustomerSql)) {
                    statement.setInt(1, customerId);
                    deletedCustomers = statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement(deleteOrphanAddressSql)) {
                    for (int addressId : addressIds) {
                        for (int parameter = 1; parameter <= 7; parameter++) {
                            statement.setInt(parameter, addressId);
                        }
                        statement.addBatch();
                    }
                    statement.executeBatch();
                }

                connection.commit();
                log.info("Deleted {} isolated customer record(s) and removed eligible orphan addresses for '{}'",
                        deletedCustomers, email);
                return deletedCustomers;
            } catch (SQLException | RuntimeException e) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackFailure) {
                    e.addSuppressed(rollbackFailure);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database cleanup failed for isolated customer", e);
        }
    }

    public static boolean addressExistsForCustomer(String customerEmail, String address1) {
        String sql = "SELECT COUNT(1) AS TotalCount FROM Address a " +
                     "INNER JOIN CustomerAddresses ca ON a.Id = ca.Address_Id " +
                     "INNER JOIN Customer c ON c.Id = ca.Customer_Id " +
                     "WHERE c.Email = ? AND a.Address1 = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, customerEmail);
            stmt.setString(2, address1);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TotalCount") > 0;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed for addressExistsForCustomer", e);
        }
        throw new IllegalStateException("Address existence query returned no result row");
    }

    /**
     * Deletes isolated test product reviews created during automated E2E runs by review title.
     */
    public static void deleteProductReviewByTitle(String reviewTitle) {
        if (reviewTitle == null || reviewTitle.isBlank()) {
            return;
        }
        String sql = "DELETE FROM ProductReview WHERE Title = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, reviewTitle);
            int removed = stmt.executeUpdate();
            log.info("Cleaned up {} isolated ProductReview row(s) with title '{}'", removed, reviewTitle);
        } catch (SQLException e) {
            throw new IllegalStateException("Database cleanup failed for isolated ProductReview", e);
        }
    }

    /**
     * Deletes isolated test discount coupon created during automated E2E runs.
     */
    public static void deleteDiscountCouponByCode(String couponCode) {
        if (couponCode == null || couponCode.isBlank()) {
            return;
        }
        String sql = "DELETE FROM Discount WHERE CouponCode = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, couponCode);
            int removed = stmt.executeUpdate();
            log.info("Cleaned up {} isolated Discount row(s) with couponCode '{}'", removed, couponCode);
        } catch (SQLException e) {
            throw new IllegalStateException("Database cleanup failed for isolated Discount", e);
        }
    }
}
