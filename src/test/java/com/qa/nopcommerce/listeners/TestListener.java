package com.qa.nopcommerce.listeners;

import com.microsoft.playwright.Page;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.config.ConfigReader;
import com.qa.nopcommerce.factory.PlaywrightFactory;
import io.qameta.allure.Allure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestContext;
import org.testng.ITestListener;
import org.testng.ITestResult;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Properties;

/**
 * TestNG Execution Listener capturing failure evidence (screenshots and Playwright zip traces),
 * writing Allure environment/category/executor metadata, and classifying failure categories.
 */
public class TestListener implements ITestListener {
    private static final Logger log = LoggerFactory.getLogger(TestListener.class);
    private static final Path ALLURE_RESULTS_DIR = Paths.get(
            System.getProperty("allure.results.directory", "target/allure-results")
    );

    @Override
    public void onStart(ITestContext context) {
        log.info("Starting Test Suite: {}", context.getName());
        createEvidenceDirectories();
        writeAllureRunMetadata(context);
        copyAllureCategoriesDefinition();
        writeAllureExecutorMetadata(context);
    }

    @Override
    public void onFinish(ITestContext context) {
        log.info("Finished Test Suite: {}. Passed: {}, Failed: {}, Skipped: {}",
                context.getName(),
                context.getPassedTests().size(),
                context.getFailedTests().size(),
                context.getSkippedTests().size());
    }

    @Override
    public void onTestStart(ITestResult result) {
        String testCaseId = getTestCaseId(result);
        String reqId = getRequirementId(result);
        log.info("=== STARTING TEST: {} [TestCaseID: {}] ===", result.getMethod().getMethodName(), testCaseId);

        Allure.label("testCaseId", testCaseId);
        if (!reqId.isEmpty()) {
            Allure.label("requirement", reqId);
        }
        Allure.parameter("browser", ConfigReader.getBrowser());
        Allure.parameter("headless", String.valueOf(ConfigReader.isHeadless()));
        Allure.parameter("environment", ConfigReader.getTestEnvName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testCaseId = getTestCaseId(result);
        log.info("=== TEST PASSED: {} [TestCaseID: {}] ===", result.getMethod().getMethodName(), testCaseId);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String testCaseId = getTestCaseId(result);
        String methodName = result.getMethod().getMethodName();
        String sanitizedName = methodName + "_" + System.currentTimeMillis();
        Throwable throwable = result.getThrowable();

        String category = classifyFailureCategory(throwable);
        Allure.label("failureCategory", category);
        log.error("=== TEST FAILED: {} [TestCaseID: {}, Category: {}] ===",
                methodName, testCaseId, category, throwable);

        Page page = PlaywrightFactory.getPage();
        if (page != null && ConfigReader.isCaptureScreenshotOnFailure()) {
            try {
                if (!page.isClosed()) {
                    String screenshotPath = "evidence/failures/" + sanitizedName + ".png";
                    byte[] screenshotBytes = page.screenshot(new Page.ScreenshotOptions()
                            .setPath(Paths.get(screenshotPath))
                            .setFullPage(true));
                    log.info("Saved failure screenshot to {}", screenshotPath);

                    Allure.addAttachment(methodName + " Failure Screenshot",
                            "image/png",
                            new ByteArrayInputStream(screenshotBytes),
                            ".png");
                }
            } catch (Exception e) {
                log.warn("Failed to capture screenshot: {}", e.getMessage());
            }
        }

        if (ConfigReader.isRecordTrace()) {
            String tracePath = "evidence/traces/" + sanitizedName + "-trace.zip";
            PlaywrightFactory.stopTracing(tracePath);
            try {
                File traceFile = new File(tracePath);
                if (traceFile.exists()) {
                    Allure.addAttachment(methodName + " Execution Trace",
                            "application/zip",
                            new ByteArrayInputStream(Files.readAllBytes(traceFile.toPath())),
                            ".zip");
                }
            } catch (Exception e) {
                log.warn("Failed to attach trace to Allure: {}", e.getMessage());
            }
        }
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testCaseId = getTestCaseId(result);
        String category = classifyFailureCategory(result.getThrowable());
        Allure.label("failureCategory", category);
        log.warn("=== TEST SKIPPED: {} [TestCaseID: {}, Category: {}] ===",
                result.getMethod().getMethodName(), testCaseId, category);
    }

    private String classifyFailureCategory(Throwable t) {
        if (t == null) {
            return "Uncategorized Failure";
        }
        String msg = (t.getClass().getName() + ": " + (t.getMessage() != null ? t.getMessage() : "")).toLowerCase();
        if (msg.contains("missing required secret") || msg.contains("secret") || msg.contains("credentials")) {
            return "Secret & Credential Configuration Error";
        }
        if (msg.contains("environmental blocker") || msg.contains("unreachable") || msg.contains("connection refused") || msg.contains("econnrefused")) {
            return "Environmental / Infrastructure Blocker";
        }
        if (msg.contains("sql") || msg.contains("hikari") || msg.contains("database")) {
            return "Database Connectivity / Persistence Failure";
        }
        if (msg.contains("timeout") || msg.contains("waiting for locator")) {
            return "UI Locator Timeout / Synchronization Failure";
        }
        if (msg.contains("assertionerror") || msg.contains("expected")) {
            return "Functional Product Assertion Defect";
        }
        return "Runtime Execution Error";
    }

    private void writeAllureRunMetadata(ITestContext context) {
        try {
            Files.createDirectories(ALLURE_RESULTS_DIR);
            Properties envProps = new Properties();
            envProps.setProperty("Application", "nopCommerce 4.90.8 (.NET 9)");
            envProps.setProperty("Environment", ConfigReader.getTestEnvName());
            envProps.setProperty("Base.URL", ConfigReader.get("baseUrl", "http://127.0.0.1:5000"));
            envProps.setProperty("Admin.URL", ConfigReader.get("adminUrl", "http://127.0.0.1:5000/Admin"));
            envProps.setProperty("Browser", ConfigReader.getBrowser());
            envProps.setProperty("Headless", String.valueOf(ConfigReader.isHeadless()));
            envProps.setProperty("Ignore.HTTPS.Errors", String.valueOf(ConfigReader.isIgnoreHttpsErrors()));
            envProps.setProperty("Suite.Name", context.getSuite().getName());
            envProps.setProperty("OS", System.getProperty("os.name") + " (" + System.getProperty("os.arch") + ")");
            envProps.setProperty("Java.Version", System.getProperty("java.version"));
            envProps.setProperty("Playwright.Version", "1.49.0");
            envProps.setProperty("Run.Timestamp.UTC", Instant.now().toString());

            Path envFile = ALLURE_RESULTS_DIR.resolve("environment.properties");
            try (FileOutputStream fos = new FileOutputStream(envFile.toFile())) {
                envProps.store(fos, "Allure Execution Run Metadata");
            }
        } catch (Exception e) {
            log.warn("Could not write Allure environment.properties: {}", e.getMessage());
        }
    }

    private void copyAllureCategoriesDefinition() {
        try {
            Files.createDirectories(ALLURE_RESULTS_DIR);
            Path targetCategories = ALLURE_RESULTS_DIR.resolve("categories.json");
            Path srcCategories = Paths.get("src/test/resources/categories.json");
            if (Files.exists(srcCategories)) {
                Files.copy(srcCategories, targetCategories, StandardCopyOption.REPLACE_EXISTING);
            } else {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream("categories.json")) {
                    if (is != null) {
                        Files.copy(is, targetCategories, StandardCopyOption.REPLACE_EXISTING);
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Could not copy Allure categories.json: {}", e.getMessage());
        }
    }

    private void writeAllureExecutorMetadata(ITestContext context) {
        try {
            Files.createDirectories(ALLURE_RESULTS_DIR);
            String ciType = System.getenv("GITHUB_ACTIONS") != null ? "GitHub Actions"
                    : (System.getenv("JENKINS_URL") != null ? "Jenkins CI" : "Local Maven Runner");
            String buildName = System.getenv("GITHUB_RUN_ID") != null
                    ? "GH-Run-" + System.getenv("GITHUB_RUN_ID")
                    : (System.getenv("BUILD_TAG") != null ? System.getenv("BUILD_TAG") : context.getSuite().getName());
            String buildOrder = System.getenv("GITHUB_RUN_NUMBER") != null
                    ? System.getenv("GITHUB_RUN_NUMBER")
                    : (System.getenv("BUILD_NUMBER") != null ? System.getenv("BUILD_NUMBER") : "1");

            String executorJson = String.format(
                    "{\n" +
                    "  \"name\": \"%s\",\n" +
                    "  \"type\": \"%s\",\n" +
                    "  \"buildName\": \"%s\",\n" +
                    "  \"buildOrder\": %s,\n" +
                    "  \"reportName\": \"nopCommerce 4.90.8 E2E Allure Report\"\n" +
                    "}\n",
                    ciType, ciType.toLowerCase().replace(" ", "_"), buildName, buildOrder
            );
            Files.writeString(ALLURE_RESULTS_DIR.resolve("executor.json"), executorJson, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.warn("Could not write Allure executor.json: {}", e.getMessage());
        }
    }

    private String getTestCaseId(ITestResult result) {
        TestCase tc = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(TestCase.class);
        if (tc != null) {
            return tc.value();
        }
        TestCase classTc = result.getTestClass().getRealClass().getAnnotation(TestCase.class);
        if (classTc != null) {
            return classTc.value();
        }
        return "UNANNOTATED";
    }

    private String getRequirementId(ITestResult result) {
        TestCase tc = result.getMethod().getConstructorOrMethod().getMethod().getAnnotation(TestCase.class);
        return (tc != null && tc.requirementId() != null) ? tc.requirementId() : "";
    }

    private void createEvidenceDirectories() {
        try {
            Files.createDirectories(Paths.get("evidence/failures"));
            Files.createDirectories(Paths.get("evidence/traces"));
            Files.createDirectories(Paths.get("evidence/screenshots"));
            Files.createDirectories(Paths.get("evidence/reports"));
        } catch (Exception e) {
            log.warn("Could not pre-create evidence directories: {}", e.getMessage());
        }
    }
}
