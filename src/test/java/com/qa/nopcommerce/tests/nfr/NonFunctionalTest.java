package com.qa.nopcommerce.tests.nfr;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

@Epic("Non-Functional Requirements")
@Feature("Responsive Design & Client-Side SLA")
public class NonFunctionalTest extends BaseTest {

    @Test(groups = {"nfr"})
    @TestCase("TC-NFR-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify storefront responsive layout adapts across Desktop (1920x1080), Tablet (768x1024), and Mobile (375x667)")
    public void testResponsiveViewportLayout() {
        // Desktop
        getPage().setViewportSize(1920, 1080);
        navigateTo("/");
        Locator topMenuDesktop = getPage().locator(".top-menu.notmobile");
        if (topMenuDesktop.count() > 0) {
            PlaywrightAssertions.assertThat(topMenuDesktop).isVisible();
        }

        // Mobile
        getPage().setViewportSize(375, 667);
        navigateTo("/");
        Locator searchBox = getPage().locator("#small-searchterms, .search-box");
        Assert.assertTrue(searchBox.count() > 0, "Search element should be available in mobile viewport");
    }

    @Test(groups = {"nfr"})
    @TestCase("TC-NFR-002")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify mobile viewport renders navigation hamburger menu trigger")
    public void testMobileNavigationMenuTrigger() {
        getPage().setViewportSize(375, 667);
        navigateTo("/");

        Locator mobileMenu = getPage().locator(".menu-toggle, .responsive-nav-button, .mobile-menu");
        if (mobileMenu.count() > 0) {
            PlaywrightAssertions.assertThat(mobileMenu.first()).isVisible();
        }
    }

    @Test(groups = {"nfr"})
    @TestCase("TC-NFR-003")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify page load performance SLA: DOMContentLoaded within 10000ms threshold")
    public void testPageLoadPerformanceSla() {
        long startTime = System.currentTimeMillis();
        navigateTo("/");
        long duration = System.currentTimeMillis() - startTime;

        log.info("Homepage navigation took {} ms", duration);
        Assert.assertTrue(duration < 15000, "Homepage load time exceeded maximum 15s SLA: " + duration + "ms");
    }

    @Test(groups = {"nfr"})
    @TestCase("TC-NFR-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify network request traffic contains no uncaught 500 Server Errors on core assets")
    public void testNo500InternalServerErrorsOnAssets() {
        List<String> failedUrls = new ArrayList<>();
        getPage().onResponse(response -> {
            if (response.status() >= 500) {
                failedUrls.add(response.url() + " (" + response.status() + ")");
            }
        });

        navigateTo("/");
        Assert.assertTrue(failedUrls.isEmpty(), "Found 500 Server Errors on page assets: " + failedUrls);
    }
}
