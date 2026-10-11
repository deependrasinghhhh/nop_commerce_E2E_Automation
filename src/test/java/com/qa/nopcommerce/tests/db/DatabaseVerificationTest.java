package com.qa.nopcommerce.tests.db;

import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.database.DatabaseManager;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.sql.Connection;
import java.sql.SQLException;

@Epic("Database & Data Integrity")
@Feature("MSSQL Persistence Verification")
public class DatabaseVerificationTest {

    private boolean isDbAvailable = false;

    @BeforeClass(alwaysRun = true)
    public void checkDatabaseAvailability() {
        isDbAvailable = DatabaseManager.isDatabaseAccessible();
        if (!isDbAvailable) {
            System.out.println("[DB-ENV] MSSQL database container is currently offline or unreachable. Database verification tests will record environmental status.");
        }
    }

    @Test(groups = {"db"})
    @TestCase("TC-DB-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer record persists in Customer table after registration")
    public void testCustomerRecordPersistenceInDatabase() {
        if (!isDbAvailable) {
            throw new SkipException("Environmental Blocker: MSSQL Database container unreachable at localhost:1433");
        }
        boolean exists = DatabaseManager.customerExists("admin@yourstore.com");
        Assert.assertTrue(exists, "Expected customer record for admin@yourstore.com in Customer table");
    }

    @Test(groups = {"db"})
    @TestCase("TC-DB-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify order record exists in [Order] table after completed checkout")
    public void testOrderRecordPersistenceInDatabase() {
        if (!isDbAvailable) {
            throw new SkipException("Environmental Blocker: MSSQL Database container unreachable at localhost:1433");
        }
        boolean exists = DatabaseManager.orderExists(1);
        Assert.assertTrue(exists, "Expected order record in [Order] table");
    }

    @Test(groups = {"db"})
    @TestCase("TC-DB-003")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify shopping cart item count query in ShoppingCartItem table")
    public void testCartItemPersistenceInDatabase() {
        if (!isDbAvailable) {
            throw new SkipException("Environmental Blocker: MSSQL Database container unreachable at localhost:1433");
        }
        int itemCount = DatabaseManager.getShoppingCartItemCount(1);
        Assert.assertTrue(itemCount >= 0, "Cart item count must be non-negative");
    }

    @Test(groups = {"db"})
    @TestCase("TC-DB-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify database connection executes read-only validation query successfully")
    public void testDatabaseConnectionHealth() {
        if (!isDbAvailable) {
            throw new SkipException("Environmental Blocker: MSSQL Database container unreachable at localhost:1433");
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            Assert.assertNotNull(conn);
            Assert.assertFalse(conn.isClosed());
        } catch (SQLException e) {
            Assert.fail("Failed to establish database connection: " + e.getMessage());
        }
    }
}
