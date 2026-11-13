package com.qa.nopcommerce.tests.ui;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.config.ConfigReader;
import com.qa.nopcommerce.pages.AdminCategoryListPage;
import com.qa.nopcommerce.pages.AdminCustomerListPage;
import com.qa.nopcommerce.pages.AdminDashboardPage;
import com.qa.nopcommerce.pages.AdminLoginPage;
import com.qa.nopcommerce.pages.AdminOrderListPage;
import com.qa.nopcommerce.pages.AdminProductListPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.regex.Pattern;

@Epic("Administration Portal")
@Feature("Administration Operations & Management")
public class AdminTest extends BaseTest {

    private void loginAsAdmin() {
        navigateTo(ConfigReader.getAdminUrl());
        AdminLoginPage adminLoginPage = new AdminLoginPage(getPage());
        adminLoginPage.login(ConfigReader.getAdminEmail(), ConfigReader.getAdminPassword());
    }

    @Test(groups = {"ui", "admin", "smoke"})
    @TestCase(value = "TC-ADM-001", requirementId = "REQ-ADM-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify administration login succeeds with valid administrator credentials")
    public void testAdminLoginValidCredentials() {
        navigateTo(ConfigReader.getAdminUrl());
        AdminLoginPage adminLoginPage = new AdminLoginPage(getPage());

        AdminDashboardPage dashboardPage = adminLoginPage.login(
                ConfigReader.getAdminEmail(),
                ConfigReader.getAdminPassword()
        );

        Assert.assertTrue(dashboardPage.isHeaderVisible(), "Expected admin dashboard header to be visible");
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-003", requirementId = "REQ-ADM-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify admin dashboard renders navigation sidebar and dashboard statistics cards")
    public void testAdminDashboardElements() {
        loginAsAdmin();
        AdminDashboardPage dashboardPage = new AdminDashboardPage(getPage());

        Assert.assertTrue(dashboardPage.isDashboardLoaded(), "Expected admin dashboard to load");
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-004", requirementId = "REQ-ADM-003")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin can access catalog product list page and search products")
    public void testAdminCatalogProductsPage() {
        loginAsAdmin();
        navigateTo("/Admin/Product/List");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*Product/List.*"));
        AdminProductListPage productListPage = new AdminProductListPage(getPage());
        productListPage.waitForGrid();
        Assert.assertTrue(productListPage.isGridVisible(), "Expected products grid to be visible");
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-005", requirementId = "REQ-ADM-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin can access sales orders list page")
    public void testAdminSalesOrdersPage() {
        loginAsAdmin();
        navigateTo("/Admin/Order/List");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*Order/List.*"));
        AdminOrderListPage orderListPage = new AdminOrderListPage(getPage());
        orderListPage.waitForGrid();
        Assert.assertTrue(orderListPage.isGridVisible(), "Expected orders grid to be visible");
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-006", requirementId = "REQ-ADM-003")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin catalog category list page renders")
    public void testAdminCatalogCategoriesPage() {
        loginAsAdmin();
        navigateTo("/Admin/Category/List");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*Category/List.*"));
        AdminCategoryListPage categoryListPage = new AdminCategoryListPage(getPage());
        categoryListPage.waitForGrid();
        Assert.assertTrue(categoryListPage.isGridVisible(), "Expected categories grid to be visible");
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-007", requirementId = "REQ-ADM-002")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin can access customer accounts list page")
    public void testAdminCustomersPage() {
        loginAsAdmin();
        navigateTo("/Admin/Customer/List");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*Customer/List.*"));
        AdminCustomerListPage customerListPage = new AdminCustomerListPage(getPage());
        customerListPage.waitForGrid();
        Assert.assertTrue(customerListPage.isGridVisible(), "Expected customers grid to be visible");
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-008", requirementId = "REQ-ADM-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify administration login rejects an invalid password and displays an authentication error")
    public void testAdminLoginInvalidPassword() {
        navigateTo(ConfigReader.getAdminUrl());
        AdminLoginPage adminLoginPage = new AdminLoginPage(getPage());

        String invalidPass = "WrongAdmin_" + UUID.randomUUID().toString().substring(0, 8);
        adminLoginPage.login(ConfigReader.getAdminEmail(), invalidPass);

        Assert.assertTrue(adminLoginPage.isErrorMessageDisplayed(), "Expected admin error message");
    }
}
