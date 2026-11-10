package com.qa.nopcommerce.tests.smoke;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.config.ConfigReader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
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

        Locator logo = getPage().locator(".header-logo");
        PlaywrightAssertions.assertThat(logo).isVisible();

        Locator topMenu = getPage().locator(".header-menu, .top-menu").first();
        PlaywrightAssertions.assertThat(topMenu).isVisible();

        Locator searchInput = getPage().locator("#small-searchterms");
        PlaywrightAssertions.assertThat(searchInput).isVisible();

        Locator shoppingCartLink = getPage().locator("#topcartlink a");
        PlaywrightAssertions.assertThat(shoppingCartLink).isVisible();
    }

    @Test(groups = {"smoke"})
    @TestCase(value = "TC-CAT-011", requirementId = "REQ-CAT-003")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify top search input executes keyword search and renders product grid results")
    public void testStorefrontSearchBoxInput() {
        navigateTo("/");

        Locator searchInput = getPage().locator("#small-searchterms");
        searchInput.fill("computer");

        Locator searchButton = getPage().locator("button.search-box-button");
        searchButton.click();

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*search.*q=computer.*"));

        Locator searchResults = getPage().locator(".search-results");
        PlaywrightAssertions.assertThat(searchResults).isVisible();
    }

    @Test(groups = {"smoke"})
    @TestCase(value = "TC-ADM-002", requirementId = "REQ-ADM-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify nopCommerce administration portal redirects unauthenticated user to login form")
    public void testAdminLoginPageRendering() {
        navigateTo(ConfigReader.getAdminUrl());

        PlaywrightAssertions.assertThat(getPage()).hasTitle(Pattern.compile(".*(Your store|nopCommerce).*Login.*"));

        Locator emailInput = getPage().locator("#Email");
        PlaywrightAssertions.assertThat(emailInput).isVisible();

        Locator passwordInput = getPage().locator("#Password");
        PlaywrightAssertions.assertThat(passwordInput).isVisible();

        Locator loginButton = getPage().locator("button.login-button, button[type='submit']").first();
        PlaywrightAssertions.assertThat(loginButton).isVisible();
    }
}
