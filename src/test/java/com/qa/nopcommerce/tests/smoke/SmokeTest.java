package com.qa.nopcommerce.tests.smoke;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.config.ConfigReader;
import com.qa.nopcommerce.pages.AdminLoginPage;
import com.qa.nopcommerce.pages.HomePage;
import com.qa.nopcommerce.pages.SearchPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

@Epic("Storefront Core")
@Feature("Storefront Smoke Gate")
public class SmokeTest extends BaseTest {

    @Test(groups = {"smoke"})
    @TestCase(value = "TC-CAT-001", requirementId = "REQ-CAT-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify storefront homepage title, logo, top navigation bar, and search box render successfully")
    public void testHomePageTitleAndHeaderRendering() {
        navigateTo("/");

        PlaywrightAssertions.assertThat(getPage()).hasTitle(Pattern.compile(".*(nopCommerce|Your store).*"));

        HomePage homePage = new HomePage(getPage());
        Assert.assertTrue(homePage.isLogoVisible(), "Homepage logo should be visible");
        Assert.assertTrue(homePage.isTopMenuVisible(), "Top navigation menu should be visible");
        Assert.assertTrue(homePage.isSearchInputVisible(), "Top search input should be visible");
        Assert.assertTrue(homePage.isCartLinkVisible(), "Shopping cart header link should be visible");
    }

    @Test(groups = {"smoke"})
    @TestCase(value = "TC-CAT-011", requirementId = "REQ-CAT-003")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify top search input executes keyword search and renders product grid results")
    public void testStorefrontSearchBoxInput() {
        navigateTo("/");

        HomePage homePage = new HomePage(getPage());
        homePage.search("computer");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*search.*q=computer.*"));

        SearchPage searchPage = new SearchPage(getPage());
        Assert.assertTrue(searchPage.isSearchResultsVisible(), "Search results should be visible");
    }

    @Test(groups = {"smoke"})
    @TestCase(value = "TC-ADM-002", requirementId = "REQ-ADM-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify nopCommerce administration portal redirects unauthenticated user to login form")
    public void testAdminLoginPageRendering() {
        navigateTo(ConfigReader.getAdminUrl());

        PlaywrightAssertions.assertThat(getPage()).hasTitle(Pattern.compile(".*(Your store|nopCommerce).*Login.*"));

        AdminLoginPage adminLoginPage = new AdminLoginPage(getPage());
        Assert.assertTrue(adminLoginPage.isEmailInputVisible(), "Admin login email input should be visible");
        Assert.assertTrue(adminLoginPage.isPasswordInputVisible(), "Admin login password input should be visible");
        Assert.assertTrue(adminLoginPage.isLoginButtonVisible(), "Admin login button should be visible");
    }
}
