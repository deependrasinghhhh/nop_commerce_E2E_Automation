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
        log.info("Initialized test session using browser: {}", browserName);
    }

    public Page getPage() {
        return PlaywrightFactory.getPage();
    }

    public void navigateTo(String path) {
        String baseUrl = ConfigReader.getBaseUrl();
        String targetUrl = path.startsWith("http") ? path : baseUrl + (path.startsWith("/") ? path : "/" + path);
        log.info("Navigating to: {}", targetUrl);
        getPage().navigate(targetUrl, new Page.NavigateOptions().setWaitUntil(WaitUntilState.DOMCONTENTLOADED));
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        PlaywrightFactory.closeContext();
    }

    @org.testng.annotations.AfterClass(alwaysRun = true)
    public void tearDownClass() {
        PlaywrightFactory.quit();
    }
}
