package com.qa.nopcommerce.tests.db;

import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.config.ConfigReader;
import com.qa.nopcommerce.database.DatabaseManager;
import com.qa.nopcommerce.pages.ProductDetailsPage;
import com.qa.nopcommerce.pages.RegisterPage;
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
import java.util.UUID;

@Epic("Database & Data Integrity")
@Feature("MSSQL Persistence Verification")
public class DatabaseVerificationTest extends BaseTest {

    private boolean isDbAvailable = false;

    @BeforeClass(alwaysRun = true)
    public void checkDatabaseAvailability() {
        // Fails fast with IllegalStateException if DB_USER / DB_PASSWORD secrets are missing
        isDbAvailable = DatabaseManager.isDatabaseAccessible();
        if (!isDbAvailable) {
            System.out.println("[DB-ENV] MSSQL database container is currently offline or unreachable. Database verification tests will record environmental blocker status.");
        }
    }

    @Test(groups = {"db"})
    @TestCase(value = "TC-DB-001", requirementId = "REQ-DB-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer record persists in Customer table")
    public void testCustomerRecordPersistenceInDatabase() {
        if (!isDbAvailable) {
            throw new SkipException("Environmental Blocker: MSSQL Database container unreachable at " + ConfigReader.getDbHost() + ":" + ConfigReader.getDbPort());
        }
        boolean exists = DatabaseManager.customerExists(ConfigReader.getAdminEmail());
        Assert.assertTrue(exists, "Expected customer record for configured admin account in Customer table");
    }

    @Test(groups = {"db"})
    @TestCase(value = "TC-DB-002", requirementId = "REQ-DB-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify seeded database contains an active order with at least one persisted order item")
    public void testOrderRecordPersistenceInDatabase() {
        if (!isDbAvailable) {
            throw new SkipException("Environmental Blocker: MSSQL Database container unreachable at " + ConfigReader.getDbHost() + ":" + ConfigReader.getDbPort());
        }
        int ordersWithItems = DatabaseManager.getActiveOrderCountWithItems();
        Assert.assertTrue(ordersWithItems > 0,
                "Expected at least one active order joined to a persisted OrderItem row");
    }

    @Test(groups = {"db"})
    @TestCase(value = "TC-DB-003", requirementId = "REQ-DB-003")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify adding a product for a unique registered customer persists a cart row in SQL Server")
    public void testCartItemPersistenceInDatabase() {
        if (!isDbAvailable) {
            throw new SkipException("Environmental Blocker: MSSQL Database container unreachable at " + ConfigReader.getDbHost() + ":" + ConfigReader.getDbPort());
        }
        String email = "qa_db_cart_" + UUID.randomUUID().toString().replace("-", "") + "@example.com";
        String password = "QaDb2026@" + UUID.randomUUID().toString().substring(0, 8);
        runWithCleanup("database verification customer " + email, () -> {
            navigateTo("/register");
            RegisterPage registerPage = new RegisterPage(getPage());
            registerPage.selectGender("male");
            registerPage.fillRegistrationForm("Database", "Verification", email, "QA Automation", password, password);
            registerPage.clickRegisterButton();
            Assert.assertTrue(registerPage.isRegistrationSuccess(), "Expected isolated customer registration to succeed");

            int customerId = DatabaseManager.getCustomerIdByEmail(email);
            int baselineCount = DatabaseManager.getShoppingCartItemCount(customerId);
            Assert.assertEquals(baselineCount, 0, "Newly registered customer should have an empty cart");

            navigateTo("/apple-icam");
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.clickAddToCart();
            Assert.assertTrue(productPage.getNotificationText().contains("The product has been added to your shopping cart"),
                    "Expected successful add-to-cart notification");

            int persistedCount = DatabaseManager.getShoppingCartItemCount(customerId);
            Assert.assertEquals(persistedCount, baselineCount + 1,
                    "Expected the application cart action to persist one ShoppingCartItem row");
        }, () -> DatabaseManager.findCustomerIdByEmail(email).ifPresent(
                customerId -> DatabaseManager.deleteShoppingCartItemsForCustomer(customerId)),
                () -> DatabaseManager.deleteIsolatedCustomerByEmail(email));
    }

    @Test(groups = {"db"})
    @TestCase(value = "TC-DB-004", requirementId = "REQ-DB-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify database connection executes read-only validation query successfully")
    public void testDatabaseConnectionHealth() {
        if (!isDbAvailable) {
            throw new SkipException("Environmental Blocker: MSSQL Database container unreachable at " + ConfigReader.getDbHost() + ":" + ConfigReader.getDbPort());
        }
        try (Connection conn = DatabaseManager.getConnection()) {
            Assert.assertNotNull(conn);
            Assert.assertFalse(conn.isClosed());
        } catch (SQLException e) {
            Assert.fail("Failed to establish database connection: " + e.getMessage());
        }
    }
}
