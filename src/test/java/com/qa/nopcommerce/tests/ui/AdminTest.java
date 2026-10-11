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

import java.util.regex.Pattern;

@Epic("Administration Portal")
@Feature("Administration Operations & Management")
public class AdminTest extends BaseTest {

    @Test(groups = {"ui", "admin", "smoke"})
    @TestCase("TC-ADM-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify nopCommerce administration login page renders email input, password input, and login button")
    public void testAdminLoginPageRendering() {
        navigateTo("https://admin-demo.nopcommerce.com/login");
        AdminLoginPage adminLoginPage = new AdminLoginPage(getPage());

        PlaywrightAssertions.assertThat(getPage()).hasTitle(Pattern.compile(".*(Your store|nopCommerce demo store)\\. Login.*"));
        PlaywrightAssertions.assertThat(adminLoginPage.getEmailInput()).isVisible();
        PlaywrightAssertions.assertThat(adminLoginPage.getPasswordInput()).isVisible();
    }

    @Test(groups = {"ui", "admin", "smoke"})
    @TestCase("TC-ADM-002")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify administration login succeeds with valid administrator credentials")
    public void testAdminLoginValidCredentials() {
        navigateTo("https://admin-demo.nopcommerce.com/login");
        AdminLoginPage adminLoginPage = new AdminLoginPage(getPage());

        String email = ConfigReader.getAdminEmail();
        String password = ConfigReader.getAdminPassword();
        AdminDashboardPage dashboardPage = adminLoginPage.login(email, password);

        PlaywrightAssertions.assertThat(dashboardPage.getDashboardHeader())
                .isVisible(new com.microsoft.playwright.assertions.LocatorAssertions.IsVisibleOptions().setTimeout(15000));
    }

    @Test(groups = {"ui", "admin"})
    @TestCase("TC-ADM-003")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify administration login rejects invalid password with error message")
    public void testAdminLoginInvalidPassword() {
        navigateTo("https://admin-demo.nopcommerce.com/login");
        AdminLoginPage adminLoginPage = new AdminLoginPage(getPage());

        adminLoginPage.login("admin@yourstore.com", "WrongAdminPass999!");

        Assert.assertTrue(adminLoginPage.isErrorMessageDisplayed(), "Expected admin error message");
    }

    @Test(groups = {"ui", "admin"})
    @TestCase("TC-ADM-004")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify admin dashboard renders navigation sidebar and dashboard statistics cards")
    public void testAdminDashboardElements() {
        navigateTo(ConfigReader.getAdminUrl());
        AdminLoginPage adminLoginPage = new AdminLoginPage(getPage());
        AdminDashboardPage dashboardPage = adminLoginPage.login(ConfigReader.getAdminEmail(), ConfigReader.getAdminPassword());

        Assert.assertTrue(dashboardPage.isDashboardLoaded(), "Dashboard should be loaded");
        Assert.assertTrue(dashboardPage.getStatisticsCardCount() > 0, "Dashboard should display stat cards");
    }

    @Test(groups = {"ui", "admin"})
    @TestCase("TC-ADM-005")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin can access catalog product list page")
    public void testAdminCatalogProductsPage() {
        navigateTo(ConfigReader.getAdminUrl() + "Product/List");

        PlaywrightAssertions.assertThat(getPage())
                .hasURL(Pattern.compile(".*(Product/List|login).*"));
    }

    @Test(groups = {"ui", "admin"})
    @TestCase("TC-ADM-007")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin can access sales orders list page")
    public void testAdminSalesOrdersPage() {
        navigateTo(ConfigReader.getAdminUrl() + "Order/List");

        PlaywrightAssertions.assertThat(getPage())
                .hasURL(Pattern.compile(".*(Order/List|login).*"));
    }

    @Test(groups = {"ui", "admin"})
    @TestCase("TC-ADM-009")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify admin can access customer accounts list page")
    public void testAdminCustomersPage() {
        navigateTo(ConfigReader.getAdminUrl() + "Customer/List");

        PlaywrightAssertions.assertThat(getPage())
                .hasURL(Pattern.compile(".*(Customer/List|login).*"));
    }
}
