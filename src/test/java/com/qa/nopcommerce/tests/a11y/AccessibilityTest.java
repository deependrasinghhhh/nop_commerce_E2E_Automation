package com.qa.nopcommerce.tests.a11y;

import com.deque.html.axecore.playwright.AxeBuilder;
import com.deque.html.axecore.results.AxeResults;
import com.deque.html.axecore.results.Rule;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.config.ConfigReader;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Epic("Non-Functional Requirements")
@Feature("Accessibility Compliance (WCAG 2.1 AA)")
public class AccessibilityTest extends BaseTest {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);

    private void saveAxeReport(String pageName, AxeResults results) {
        Path reportDirectory = Paths.get(ConfigReader.getA11yReportDirectory());
        Path reportFile = reportDirectory.resolve(pageName + "-axe-results.json");
        try {
            java.nio.file.Files.createDirectories(reportDirectory);
            OBJECT_MAPPER.writeValue(reportFile.toFile(), results);
            log.info("Saved accessibility scan results to {}", reportFile);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to write accessibility report to " + reportFile, e);
        }
    }

    /**
     * Asserts that no accessibility violations meet or exceed the configured impact threshold.
     * Severity ranking: minor (1) < moderate (2) < serious (3) < critical (4).
     * Violations with null, blank, or unknown impact count as meeting the threshold and are reported as UNKNOWN.
     */
    private void assertAccessibilityViolations(AxeResults results) {
        Assert.assertNotNull(results, "Axe analysis results must not be null");
        String threshold = ConfigReader.getA11yFailOnImpact();
        int thresholdRank = getSeverityRank(threshold);

        List<Rule> violations = results.getViolations();
        if (violations == null || violations.isEmpty()) {
            log.info("No accessibility violations detected");
            return;
        }

        log.info("Total WCAG violations detected: {}", violations.size());

        List<String> offendingViolations = new ArrayList<>();
        for (Rule rule : violations) {
            String rawImpact = rule.getImpact();
            int ruleRank = getSeverityRank(rawImpact);
            if (ruleRank >= thresholdRank) {
                String displayImpact = getDisplayImpact(rawImpact);
                int nodeCount = (rule.getNodes() != null) ? rule.getNodes().size() : 0;
                offendingViolations.add(String.format(
                        "Rule ID: %s, Impact: %s, Nodes: %d%n%s",
                        rule.getId(), displayImpact, nodeCount, formatViolation(rule)));
            }
        }

        if (!offendingViolations.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Accessibility scan failed: %d violation(s) met or exceeded impact threshold '%s':%n",
                    offendingViolations.size(), threshold));
            for (String violation : offendingViolations) {
                sb.append("  - ").append(violation).append("\n");
            }
            Assert.fail(sb.toString().trim());
        }
    }

    private static String formatViolation(Rule rule) {
        try {
            JsonNode violation = OBJECT_MAPPER.valueToTree(rule);
            return OBJECT_MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(violation);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Unable to format axe violation details for rule " + rule.getId(), e);
        }
    }

    private static int getSeverityRank(String impact) {
        if (impact == null || impact.isBlank()) {
            return Integer.MAX_VALUE;
        }
        switch (impact.trim().toLowerCase()) {
            case "minor":
                return 1;
            case "moderate":
                return 2;
            case "serious":
                return 3;
            case "critical":
                return 4;
            default:
                return Integer.MAX_VALUE;
        }
    }

    private static String getDisplayImpact(String rawImpact) {
        if (rawImpact == null || rawImpact.isBlank()) {
            return "UNKNOWN";
        }
        String lower = rawImpact.trim().toLowerCase();
        if (Arrays.asList("minor", "moderate", "serious", "critical").contains(lower)) {
            return lower;
        }
        return "UNKNOWN";
    }

    @Test(groups = {"a11y"})
    @TestCase(value = "TC-A11Y-001", requirementId = "REQ-A11Y-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Execute axe-core WCAG 2.1 AA accessibility scan on Storefront Homepage")
    public void testHomePageAccessibility() {
        navigateTo("/");
        AxeResults axeResults = new AxeBuilder(getPage())
                .withTags(Arrays.asList("wcag2a", "wcag2aa"))
                .analyze();

        saveAxeReport("homepage", axeResults);
        assertAccessibilityViolations(axeResults);
    }

    @Test(groups = {"a11y"})
    @TestCase(value = "TC-A11Y-002", requirementId = "REQ-A11Y-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Execute axe-core WCAG 2.1 AA accessibility scan on Customer Registration page")
    public void testRegisterPageAccessibility() {
        navigateTo("/register");
        AxeResults axeResults = new AxeBuilder(getPage())
                .withTags(Arrays.asList("wcag2a", "wcag2aa"))
                .analyze();

        saveAxeReport("register-page", axeResults);
        assertAccessibilityViolations(axeResults);
    }

    @Test(groups = {"a11y"})
    @TestCase(value = "TC-A11Y-003", requirementId = "REQ-A11Y-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Execute axe-core WCAG 2.1 AA accessibility scan on Customer Login page")
    public void testLoginPageAccessibility() {
        navigateTo("/login");
        AxeResults axeResults = new AxeBuilder(getPage())
                .withTags(Arrays.asList("wcag2a", "wcag2aa"))
                .analyze();

        saveAxeReport("login-page", axeResults);
        assertAccessibilityViolations(axeResults);
    }

    @Test(groups = {"a11y"})
    @TestCase(value = "TC-A11Y-004", requirementId = "REQ-A11Y-001")
    @Severity(SeverityLevel.NORMAL)
    @Description("Execute axe-core WCAG 2.1 AA accessibility scan on Shopping Cart page")
    public void testCartPageAccessibility() {
        navigateTo("/cart");
        AxeResults axeResults = new AxeBuilder(getPage())
                .withTags(Arrays.asList("wcag2a", "wcag2aa"))
                .analyze();

        saveAxeReport("cart-page", axeResults);
        assertAccessibilityViolations(axeResults);
    }

    @Test(groups = {"a11y"})
    @TestCase(value = "TC-A11Y-005", requirementId = "REQ-A11Y-001")
    @Severity(SeverityLevel.NORMAL)
    @Description("Execute axe-core WCAG 2.1 AA accessibility scan on Product Detail / Category page")
    public void testCategoryPageAccessibility() {
        navigateTo("/desktops");
        AxeResults axeResults = new AxeBuilder(getPage())
                .withTags(Arrays.asList("wcag2a", "wcag2aa"))
                .analyze();

        saveAxeReport("category-desktops-page", axeResults);
        assertAccessibilityViolations(axeResults);
    }
}
