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
    @TestCase(value = "TC-NFR-001", requirementId = "REQ-NFR-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify storefront responsive layout adapts across Desktop (1920x1080) and Mobile (375x667)")
    public void testResponsiveViewportLayout() {
        getPage().setViewportSize(1920, 1080);
        navigateTo("/");
        Locator desktopMenu = getPage().locator(".header-menu .menu");
        PlaywrightAssertions.assertThat(desktopMenu).isVisible();

        getPage().setViewportSize(375, 667);
        navigateTo("/");
        Locator searchBox = getPage().locator("#small-searchterms");
        PlaywrightAssertions.assertThat(searchBox).isVisible();
        Boolean hasHorizontalOverflow = (Boolean) getPage().evaluate(
                "() => document.documentElement.scrollWidth > window.innerWidth");
        Assert.assertFalse(Boolean.TRUE.equals(hasHorizontalOverflow),
                "Mobile viewport (375x667) must not cause horizontal overflow");
    }

    @Test(groups = {"nfr"})
    @TestCase(value = "TC-NFR-002", requirementId = "REQ-NFR-001")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify mobile viewport renders navigation hamburger menu trigger")
    public void testMobileNavigationMenuTrigger() {
        getPage().setViewportSize(375, 667);
        navigateTo("/");

        Locator mobileMenuTrigger = getPage().locator(".header-menu #menu-1.menu__toggle");
        PlaywrightAssertions.assertThat(mobileMenuTrigger).isVisible();
    }

    @Test(groups = {"nfr"})
    @TestCase(value = "TC-NFR-003", requirementId = "REQ-NFR-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify homepage navigation commit plus header visibility completes within 15000ms")
    public void testPageLoadPerformanceSla() {
        long startNano = System.nanoTime();
        navigateTo("/");
        PlaywrightAssertions.assertThat(getPage().locator(".header-logo")).isVisible();
        long duration = (System.nanoTime() - startNano) / 1_000_000;

        log.info("Homepage navigation took {} ms", duration);
        Assert.assertTrue(duration < 15000, "Homepage load time exceeded maximum 15s SLA: " + duration + "ms");
    }

    @Test(groups = {"nfr"})
    @TestCase(value = "TC-NFR-004", requirementId = "REQ-NFR-003")
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
        PlaywrightAssertions.assertThat(getPage().locator(".header-logo")).isVisible();
        Assert.assertTrue(failedUrls.isEmpty(), "Found 500 Server Errors on page assets: " + failedUrls);
    }
}
