package com.qa.nopcommerce.database;

import com.qa.nopcommerce.config.ConfigReader;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
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
                    + "qa_(?:db_cart|checkout)_[0-9a-f]{32})@example\\.com$",
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
        return getDataSource().getConnection();
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
        String sql = "SELECT COUNT(1) AS TotalItems FROM ShoppingCartItem WHERE CustomerId = ? AND ShoppingCartTypeId = 1";
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

    /**
     * Returns persisted shopping cart quantity ({@code ShoppingCartTypeId = 1}) for the specified
     * customer and product URL slug, or {@code 0} when no matching row exists.
     */
    public static int getShoppingCartItemQuantity(int customerId, String productSlug) {
        String sql = "SELECT COALESCE(SUM(sci.Quantity), 0) AS TotalQty "
                + "FROM ShoppingCartItem sci "
                + "INNER JOIN UrlRecord u ON u.EntityId = sci.ProductId AND u.EntityName = 'Product' "
                + "WHERE sci.CustomerId = ? AND sci.ShoppingCartTypeId = 1 AND u.Slug = ?";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            stmt.setString(2, productSlug);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TotalQty");
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed for getShoppingCartItemQuantity", e);
        }
        throw new IllegalStateException("Shopping cart quantity query returned no result row");
    }

    /**
     * Checks whether a persisted active order exists for the specified registered customer email
     * or guest billing address email and numeric order ID.
     */
    public static boolean orderExistsForCustomerEmail(int orderId, String customerEmail) {
        String sql = "SELECT COUNT(1) AS TotalCount FROM [Order] o "
                + "INNER JOIN Customer c ON c.Id = o.CustomerId "
                + "LEFT JOIN Address ba ON ba.Id = o.BillingAddressId "
                + "WHERE o.Id = ? AND o.Deleted = 0 AND (c.Email = ? OR ba.Email = ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, orderId);
            stmt.setString(2, customerEmail);
            stmt.setString(3, customerEmail);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("TotalCount") > 0;
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Database query failed for orderExistsForCustomerEmail", e);
        }
        throw new IllegalStateException("Customer order check query returned no result row");
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

    /**
     * Physically removes one account (or anonymous guest customer matching billing address email)
     * created by an isolated test, including any test-owned orders, cart items, reviews, generic
     * attributes, and orphan addresses. The email allowlist protects seeded accounts.
     */
    public static int deleteIsolatedCustomerByEmail(String email) {
        if (email == null || email.isBlank()) {
            return 0;
        }
        if (!ISOLATED_CUSTOMER_EMAIL.matcher(email).matches()) {
            throw new IllegalArgumentException("Refusing isolated-customer cleanup for an unrecognized test email.");
        }

        String customerIdsSql = "SELECT DISTINCT Id FROM ("
                + "SELECT Id FROM Customer WHERE Email = ? "
                + "UNION "
                + "SELECT c.Id FROM Customer c INNER JOIN Address a ON c.BillingAddress_Id = a.Id OR c.ShippingAddress_Id = a.Id WHERE a.Email = ? "
                + "UNION "
                + "SELECT o.CustomerId AS Id FROM [Order] o INNER JOIN Address a ON o.BillingAddressId = a.Id WHERE a.Email = ? "
                + "UNION "
                + "SELECT ca.Customer_Id AS Id FROM CustomerAddresses ca INNER JOIN Address a ON ca.Address_Id = a.Id WHERE a.Email = ?"
                + ") AS MatchedCustomers";
        String addressIdsSql = "SELECT DISTINCT AddressId FROM ("
                + "SELECT Id AS AddressId FROM Address WHERE Email = ? "
                + "UNION "
                + "SELECT Address_Id AS AddressId FROM CustomerAddresses WHERE Customer_Id = ? "
                + "UNION "
                + "SELECT BillingAddress_Id AS AddressId FROM Customer WHERE Id = ? AND BillingAddress_Id IS NOT NULL "
                + "UNION "
                + "SELECT ShippingAddress_Id AS AddressId FROM Customer WHERE Id = ? AND ShippingAddress_Id IS NOT NULL "
                + "UNION "
                + "SELECT BillingAddressId AS AddressId FROM [Order] WHERE CustomerId = ? "
                + "UNION "
                + "SELECT ShippingAddressId AS AddressId FROM [Order] WHERE CustomerId = ? AND ShippingAddressId IS NOT NULL"
                + ") AS CandidateAddresses";
        String orderIdsSql = "SELECT Id FROM [Order] WHERE CustomerId = ?";
        String deleteOrderNotesSql = "DELETE FROM OrderNote WHERE OrderId = ?";
        String deleteDiscountHistorySql = "DELETE FROM DiscountUsageHistory WHERE OrderId = ?";
        String deleteGiftCardHistorySql = "DELETE FROM GiftCardUsageHistory WHERE UsedWithOrderId = ?";
        String deleteShipmentsSql = "DELETE FROM Shipment WHERE OrderId = ?";
        String deleteOrderItemsSql = "DELETE FROM OrderItem WHERE OrderId = ?";
        String deleteOrdersSql = "DELETE FROM [Order] WHERE CustomerId = ?";
        String deleteShoppingCartSql = "DELETE FROM ShoppingCartItem WHERE CustomerId = ?";
        String deleteReviewsSql = "DELETE FROM ProductReview WHERE CustomerId = ?";
        String deleteActivityLogSql = "DELETE FROM ActivityLog WHERE CustomerId = ?";
        String deleteLogSql = "DELETE FROM Log WHERE CustomerId = ?";
        String clearCustomerAddressRefsSql = "UPDATE Customer SET BillingAddress_Id = NULL, ShippingAddress_Id = NULL WHERE Id = ?";
        String deleteCustomerAddressesSql = "DELETE FROM CustomerAddresses WHERE Customer_Id = ?";
        String deleteGenericAttributesSql =
                "DELETE FROM GenericAttribute WHERE KeyGroup = 'Customer' AND EntityId = ?";
        String deleteCustomWishlistSql = "DELETE FROM CustomWishlist WHERE CustomerId = ?";
        String deleteCustomerSql = "DELETE FROM Customer WHERE Id = ?";
        String deleteOrphanAddressSql = "DELETE FROM Address WHERE Id = ? " +
                "AND NOT EXISTS (SELECT 1 FROM Customer WHERE BillingAddress_Id = ? OR ShippingAddress_Id = ?) " +
                "AND NOT EXISTS (SELECT 1 FROM CustomerAddresses WHERE Address_Id = ?) " +
                "AND NOT EXISTS (SELECT 1 FROM [Order] WHERE BillingAddressId = ? " +
                "OR PickupAddressId = ? OR ShippingAddressId = ?) " +
                "AND NOT EXISTS (SELECT 1 FROM Affiliate WHERE AddressId = ?)";

        try (Connection connection = getConnection()) {
            connection.setAutoCommit(false);
            try {
                List<Integer> customerIds = new ArrayList<>();
                try (PreparedStatement statement = connection.prepareStatement(customerIdsSql)) {
                    for (int p = 1; p <= 4; p++) {
                        statement.setString(p, email);
                    }
                    try (ResultSet result = statement.executeQuery()) {
                        while (result.next()) {
                            customerIds.add(result.getInt("Id"));
                        }
                    }
                }
                if (customerIds.isEmpty()) {
                    connection.commit();
                    return 0;
                }

                List<Integer> addressIds = new ArrayList<>();
                for (int customerId : customerIds) {
                    try (PreparedStatement statement = connection.prepareStatement(addressIdsSql)) {
                        statement.setString(1, email);
                        for (int p = 2; p <= 6; p++) {
                            statement.setInt(p, customerId);
                        }
                        try (ResultSet result = statement.executeQuery()) {
                            while (result.next()) {
                                int addrId = result.getInt("AddressId");
                                if (!result.wasNull() && !addressIds.contains(addrId)) {
                                    addressIds.add(addrId);
                                }
                            }
                        }
                    }

                    List<Integer> orderIds = new ArrayList<>();
                    try (PreparedStatement statement = connection.prepareStatement(orderIdsSql)) {
                        statement.setInt(1, customerId);
                        try (ResultSet result = statement.executeQuery()) {
                            while (result.next()) {
                                orderIds.add(result.getInt("Id"));
                            }
                        }
                    }

                    for (String orderChildSql : List.of(
                            deleteOrderNotesSql, deleteDiscountHistorySql, deleteGiftCardHistorySql,
                            deleteShipmentsSql, deleteOrderItemsSql)) {
                        if (!orderIds.isEmpty()) {
                            try (PreparedStatement statement = connection.prepareStatement(orderChildSql)) {
                                for (int orderId : orderIds) {
                                    statement.setInt(1, orderId);
                                    statement.addBatch();
                                }
                                statement.executeBatch();
                            }
                        }
                    }

                    try (PreparedStatement statement = connection.prepareStatement(deleteOrdersSql)) {
                        statement.setInt(1, customerId);
                        statement.executeUpdate();
                    }
                    for (String customerChildSql : List.of(
                            deleteShoppingCartSql, deleteReviewsSql, deleteActivityLogSql, deleteLogSql,
                            clearCustomerAddressRefsSql, deleteCustomerAddressesSql,
                            deleteGenericAttributesSql, deleteCustomWishlistSql)) {
                        try (PreparedStatement statement = connection.prepareStatement(customerChildSql)) {
                            statement.setInt(1, customerId);
                            statement.executeUpdate();
                        }
                    }
                }

                int deletedCustomers = 0;
                for (int customerId : customerIds) {
                    try (PreparedStatement statement = connection.prepareStatement(deleteCustomerSql)) {
                        statement.setInt(1, customerId);
                        deletedCustomers += statement.executeUpdate();
                    }
                }
                if (!addressIds.isEmpty()) {
                    try (PreparedStatement statement = connection.prepareStatement(deleteOrphanAddressSql)) {
                        for (int addressId : addressIds) {
                            for (int parameter = 1; parameter <= 8; parameter++) {
                                statement.setInt(parameter, addressId);
                            }
                            statement.addBatch();
                        }
                        statement.executeBatch();
                    }
                }

                connection.commit();
                log.info("Deleted {} isolated customer record(s) (orphanAddressesChecked={}) for '{}'",
                        deletedCustomers, addressIds.size(), email);
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
