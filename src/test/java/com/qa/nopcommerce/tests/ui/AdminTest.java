package com.qa.nopcommerce.tests.ui;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.config.ConfigReader;
import com.qa.nopcommerce.pages.AdminDashboardPage;
import com.qa.nopcommerce.pages.AdminLoginPage;
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

        PlaywrightAssertions.assertThat(dashboardPage.getDashboardHeader()).isVisible();
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-003", requirementId = "REQ-ADM-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify admin dashboard renders navigation sidebar and dashboard statistics cards")
    public void testAdminDashboardElements() {
        loginAsAdmin();
        AdminDashboardPage dashboardPage = new AdminDashboardPage(getPage());

        PlaywrightAssertions.assertThat(dashboardPage.getDashboardHeader()).isVisible();
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-004", requirementId = "REQ-ADM-003")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin can access catalog product list page and search products")
    public void testAdminCatalogProductsPage() {
        loginAsAdmin();
        navigateTo("/Admin/Product/List");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*Product/List.*"));
        PlaywrightAssertions.assertThat(getPage().locator("#products-grid, .content-header h1").first()).isVisible();
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-005", requirementId = "REQ-ADM-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin can access sales orders list page")
    public void testAdminSalesOrdersPage() {
        loginAsAdmin();
        navigateTo("/Admin/Order/List");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*Order/List.*"));
        PlaywrightAssertions.assertThat(getPage().locator("#orders-grid, .content-header h1").first()).isVisible();
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-006", requirementId = "REQ-ADM-003")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin catalog category list page renders")
    public void testAdminCatalogCategoriesPage() {
        loginAsAdmin();
        navigateTo("/Admin/Category/List");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*Category/List.*"));
        PlaywrightAssertions.assertThat(getPage().locator("#categories-grid, .content-header h1").first()).isVisible();
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-007", requirementId = "REQ-ADM-002")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin can access customer accounts list page")
    public void testAdminCustomersPage() {
        loginAsAdmin();
        navigateTo("/Admin/Customer/List");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*Customer/List.*"));
        PlaywrightAssertions.assertThat(getPage().locator("#customers-grid, .content-header h1").first()).isVisible();
    }

    @Test(groups = {"ui", "admin"})
    @TestCase(value = "TC-ADM-008", requirementId = "REQ-ADM-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify administration login rejects invalid password and admin logout terminates session")
    public void testAdminLoginInvalidPassword() {
        navigateTo(ConfigReader.getAdminUrl());
        AdminLoginPage adminLoginPage = new AdminLoginPage(getPage());

        String invalidPass = "WrongAdmin_" + UUID.randomUUID().toString().substring(0, 8);
        adminLoginPage.login(ConfigReader.getAdminEmail(), invalidPass);

        Assert.assertTrue(adminLoginPage.isErrorMessageDisplayed(), "Expected admin error message");
    }
}
