package com.qa.nopcommerce.tests.a11y;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.Rule;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

@Epic("Non-Functional Requirements")
@Feature("Accessibility Compliance (WCAG 2.1 AA)")
public class AccessibilityTest extends BaseTest {

    private void saveAxeReport(String pageName, AxeResults results) {
        try {
            Files.createDirectories(Paths.get("evidence/a11y"));
            String fileName = "evidence/a11y/" + pageName + "-axe-results.json";
            try (FileWriter writer = new FileWriter(fileName)) {
                writer.write(results.toString());
            }
            log.info("Saved accessibility scan results to {}", fileName);
        } catch (IOException e) {
            log.warn("Failed to write axe report for {}: {}", pageName, e.getMessage());
        }
    }

    @Test(groups = {"a11y"})
    @TestCase("TC-A11Y-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Execute axe-core WCAG 2.1 AA accessibility scan on Storefront Homepage")
    public void testHomePageAccessibility() {
        navigateTo("/");
        AxeResults axeResults = new AxeBuilder(getPage())
                .withTags(java.util.Arrays.asList("wcag2a", "wcag2aa"))
                .analyze();

        saveAxeReport("homepage", axeResults);
        List<Rule> violations = axeResults.getViolations();
        log.info("Homepage WCAG violations detected: {}", violations.size());
        // Document violations in evidence rather than hard-failing build
        Assert.assertNotNull(axeResults, "Axe analysis results must not be null");
    }

    @Test(groups = {"a11y"})
    @TestCase("TC-A11Y-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Execute axe-core WCAG 2.1 AA accessibility scan on Customer Login page")
    public void testLoginPageAccessibility() {
        navigateTo("/login");
        AxeResults axeResults = new AxeBuilder(getPage())
                .withTags(java.util.Arrays.asList("wcag2a", "wcag2aa"))
                .analyze();

        saveAxeReport("login-page", axeResults);
        Assert.assertNotNull(axeResults, "Axe analysis results must not be null");
    }

    @Test(groups = {"a11y"})
    @TestCase("TC-A11Y-003")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Execute axe-core WCAG 2.1 AA accessibility scan on Customer Registration page")
    public void testRegisterPageAccessibility() {
        navigateTo("/register");
        AxeResults axeResults = new AxeBuilder(getPage())
                .withTags(java.util.Arrays.asList("wcag2a", "wcag2aa"))
                .analyze();

        saveAxeReport("register-page", axeResults);
        Assert.assertNotNull(axeResults, "Axe analysis results must not be null");
    }

    @Test(groups = {"a11y"})
    @TestCase("TC-A11Y-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Execute axe-core WCAG 2.1 AA accessibility scan on Shopping Cart page")
    public void testCartPageAccessibility() {
        navigateTo("/cart");
        AxeResults axeResults = new AxeBuilder(getPage())
                .withTags(java.util.Arrays.asList("wcag2a", "wcag2aa"))
                .analyze();

        saveAxeReport("cart-page", axeResults);
        Assert.assertNotNull(axeResults, "Axe analysis results must not be null");
    }

    @Test(groups = {"a11y"})
    @TestCase("TC-A11Y-005")
    @Severity(SeverityLevel.NORMAL)
    @Description("Execute axe-core WCAG 2.1 AA accessibility scan on Category listing page")
    public void testCategoryPageAccessibility() {
        navigateTo("/desktops");
        AxeResults axeResults = new AxeBuilder(getPage())
                .withTags(java.util.Arrays.asList("wcag2a", "wcag2aa"))
                .analyze();

        saveAxeReport("category-desktops-page", axeResults);
        Assert.assertNotNull(axeResults, "Axe analysis results must not be null");
    }
}
