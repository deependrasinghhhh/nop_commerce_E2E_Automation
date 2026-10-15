package com.qa.nopcommerce.listeners;

import com.microsoft.playwright.BrowserContext;
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
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * TestNG Execution Listener capturing failure evidence (screenshots and Playwright zip traces)
 * and extracting @TestCase metadata for automated traceability reporting.
 */
public class TestListener implements ITestListener {
    private static final Logger log = LoggerFactory.getLogger(TestListener.class);

    @Override
    public void onStart(ITestContext context) {
        log.info("Starting Test Suite: {}", context.getName());
        createEvidenceDirectories();
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
        log.info("=== STARTING TEST: {} [TestCaseID: {}] ===", result.getMethod().getMethodName(), testCaseId);
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        String testCaseId = getTestCaseId(result);
        log.info("=== TEST PASSED: {} [TestCaseID: {}] in {} ms ===",
                result.getMethod().getMethodName(),
                testCaseId,
                (result.getEndMillis() - result.getStartMillis()));

        // Stop tracing without retaining on success to preserve disk space
        Page page = PlaywrightFactory.getPage();
        if (page != null && !page.isClosed() && ConfigReader.isRecordTrace()) {
            try {
                BrowserContext context = PlaywrightFactory.getContext();
                if (context != null) {
                    context.tracing().stop();
                }
            } catch (Exception ignored) {}
        }
    }

    @Override
    public void onTestFailure(ITestResult result) {
        String methodName = result.getMethod().getMethodName();
        String testCaseId = getTestCaseId(result);
        log.error("=== TEST FAILED: {} [TestCaseID: {}] ===", methodName, testCaseId, result.getThrowable());

        Page page = PlaywrightFactory.getPage();
        if (page != null && !page.isClosed()) {
            String sanitizedName = methodName.replaceAll("[^a-zA-Z0-9_-]", "_");

            // 1. Capture full-page screenshot
            if (ConfigReader.isCaptureScreenshotOnFailure()) {
                try {
                    String screenshotPath = "evidence/failures/" + sanitizedName + ".png";
                    byte[] screenshotBytes = page.screenshot(new Page.ScreenshotOptions()
                            .setPath(Paths.get(screenshotPath))
                            .setFullPage(true));
                    log.info("Saved failure screenshot to {}", screenshotPath);

                    Allure.addAttachment(methodName + " Failure Screenshot",
                            "image/png",
                            new ByteArrayInputStream(screenshotBytes),
                            ".png");
                } catch (Exception e) {
                    log.warn("Failed to capture screenshot: {}", e.getMessage());
                }
            }

            // 2. Persist Playwright trace zip
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
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        String testCaseId = getTestCaseId(result);
        log.warn("=== TEST SKIPPED: {} [TestCaseID: {}] ===", result.getMethod().getMethodName(), testCaseId);
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

    private void createEvidenceDirectories() {
        try {
            Files.createDirectories(Paths.get("evidence/failures"));
            Files.createDirectories(Paths.get("evidence/traces"));
            Files.createDirectories(Paths.get("evidence/screenshots"));
            Files.createDirectories(Paths.get("reports"));
        } catch (Exception e) {
            log.warn("Could not pre-create evidence directories: {}", e.getMessage());
        }
    }
}
