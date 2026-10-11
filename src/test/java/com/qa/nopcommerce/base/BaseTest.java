package com.qa.nopcommerce.base;

import com.microsoft.playwright.Page;
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
        getPage().navigate(targetUrl);

        try {
            if (getPage().title().contains("Just a moment...") || getPage().content().contains("Verify you are human")) {
                log.warn("Cloudflare Turnstile challenge active on {} - attempting challenge interaction pass", targetUrl);
                for (com.microsoft.playwright.Frame frame : getPage().frames()) {
                    if (frame.url().contains("challenges.cloudflare.com")) {
                        try {
                            frame.locator(".fXVnL4 input, .lnOXD9, input[type='checkbox']").first()
                                    .click(new com.microsoft.playwright.Locator.ClickOptions().setForce(true).setTimeout(2000));
                        } catch (Exception ignored) {}
                    }
                }
                getPage().waitForTimeout(2500);
            }
        } catch (Exception ignored) {}
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        PlaywrightFactory.quit();
    }
}
