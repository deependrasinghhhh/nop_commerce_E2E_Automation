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
        handleCloudflareIfPresent();
    }

    public void handleCloudflareIfPresent() {
        Page page = getPage();
        if (page == null) return;

        try {
            boolean isCloudflare = page.title().contains("Just a moment...")
                    || page.title().contains("Attention Required")
                    || page.locator("#challenge-stage, .cf-turnstile, iframe[src*='challenges.cloudflare.com'], #turnstile-wrapper").count() > 0;

            if (isCloudflare) {
                log.info("Cloudflare Turnstile challenge detected on {}. Attempting automated resolution...", page.url());
                long start = System.currentTimeMillis();

                while (System.currentTimeMillis() - start < 15000) {
                    if (!page.title().contains("Just a moment...") && !page.title().contains("Attention Required")) {
                        log.info("Cloudflare challenge resolved successfully! Page title: {}", page.title());
                        return;
                    }

                    // Attempt 1: Locate Turnstile frame and click the checkbox
                    for (com.microsoft.playwright.Frame frame : page.frames()) {
                        if (frame.url().contains("challenges.cloudflare.com") || frame.url().contains("turnstile")) {
                            try {
                                com.microsoft.playwright.Locator checkbox = frame.locator("input[type='checkbox'], span.mark, .ctp-checkbox-label, div[id*='checkbox']").first();
                                if (checkbox.isVisible()) {
                                    log.info("Clicking Cloudflare Turnstile checkbox inside challenge frame");
                                    checkbox.click(new com.microsoft.playwright.Locator.ClickOptions().setTimeout(2000));
                                    page.waitForTimeout(3000);
                                    break;
                                }
                            } catch (Exception ignored) {}
                        }
                    }

                    // Attempt 2: If frame checkbox not directly clickable, click iframe directly
                    com.microsoft.playwright.Locator cfIframe = page.locator("iframe[src*='challenges.cloudflare.com'], iframe[src*='turnstile'], #turnstile-wrapper iframe").first();
                    if (cfIframe.isVisible()) {
                        try {
                            log.info("Clicking Turnstile iframe container directly");
                            cfIframe.click(new com.microsoft.playwright.Locator.ClickOptions().setTimeout(2000));
                            page.waitForTimeout(3000);
                        } catch (Exception ignored) {}
                    }

                    page.waitForTimeout(1000);
                }

                try {
                    page.waitForFunction(
                            "() => !document.title.includes('Just a moment...') && !document.title.includes('Attention Required')",
                            null,
                            new Page.WaitForFunctionOptions().setTimeout(10000)
                    );
                    log.info("Cloudflare challenge cleared! Page title: {}", page.title());
                } catch (Exception e) {
                    log.warn("Cloudflare verification screen did not clear within timeout: {}", page.title());
                }
            }
        } catch (Exception e) {
            log.debug("Cloudflare check encountered: {}", e.getMessage());
        }
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        PlaywrightFactory.quit();
    }
}
