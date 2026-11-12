package com.qa.nopcommerce.base;

import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.qa.nopcommerce.config.ConfigReader;
import com.qa.nopcommerce.factory.PlaywrightFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;

/**
 * Base Test class providing lifecycle initialization and teardown hooks for UI tests.
 *
 * <p><strong>Lifecycle mapping to TestNG parallel=classes (thread-count=2):</strong></p>
 * <ul>
 *   <li>{@code @BeforeMethod} — creates a fresh BrowserContext + Page per test method.</li>
 *   <li>{@code @AfterMethod} — closes only the context/page; the Browser stays alive for the next
 *       method in the same class.</li>
 *   <li>{@code @AfterClass} — closes the Browser and Playwright for this thread. Safe even when
 *       parallel execution is active because TestNG runs all methods of a class on the same thread
 *       before invoking {@code @AfterClass}.</li>
 * </ul>
 */
public abstract class BaseTest {
    protected final Logger log = LoggerFactory.getLogger(getClass());

    @BeforeMethod(alwaysRun = true)
    @Parameters({"browser"})
    public void setUp(@Optional("") String browserParam) {
        String browserName = (browserParam != null && !browserParam.trim().isEmpty())
                ? browserParam
                : ConfigReader.getBrowser();

        boolean headless = ConfigReader.isHeadless();
        PlaywrightFactory.initBrowser(browserName, headless);
        log.info("Initialized test session – browser={}, headless={}, thread={}",
                browserName, headless, Thread.currentThread().getName());
    }

    /**
     * Returns the Page owned by the calling thread's current BrowserContext.
     */
    public Page getPage() {
        return PlaywrightFactory.getPage();
    }

    /**
     * Navigates the current page to the given path (absolute URL or relative to baseUrl).
     * Waits for the navigation response to commit. Page readiness is established by
     * Playwright's auto-waiting locator actions and assertions, avoiding document-event
     * stalls caused by long-running or third-party resources.
     */
    public void navigateTo(String path) {
        navigateTo(path, WaitUntilState.COMMIT);
    }

    public void navigateTo(String path, WaitUntilState waitUntil) {
        String baseUrl = ConfigReader.getBaseUrl();
        String targetUrl = path.startsWith("http") ? path : baseUrl + (path.startsWith("/") ? path : "/" + path);
        log.info("Navigating to: {}", targetUrl);
        getPage().navigate(targetUrl, new Page.NavigateOptions().setWaitUntil(waitUntil));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        PlaywrightFactory.stopTracing();
        PlaywrightFactory.closeContext();
    }

    @org.testng.annotations.AfterClass(alwaysRun = true)
    public void tearDownClass() {
        PlaywrightFactory.quit();
    }
}
