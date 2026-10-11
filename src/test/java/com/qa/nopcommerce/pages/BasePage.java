package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Page providing common locator wrappers, wait helpers, and navigation hooks.
 */
public abstract class BasePage {
    protected final Page page;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    public BasePage(Page page) {
        this.page = page;
    }

    public Page getPage() {
        return this.page;
    }

    public String getTitle() {
        return page.title();
    }

    public String getUrl() {
        return page.url();
    }

    public void waitForLoad() {
        page.waitForLoadState();
    }

    protected void fill(Locator locator, String value) {
        locator.waitFor();
        locator.fill(value);
    }

    protected void click(Locator locator) {
        locator.waitFor();
        locator.click();
    }

    protected String getText(Locator locator) {
        locator.waitFor();
        return locator.innerText().trim();
    }

    protected boolean isVisible(Locator locator) {
        try {
            return locator.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public void handleCloudflareIfPresent() {
        if (page == null) return;
        try {
            boolean isCloudflare = page.title().contains("Just a moment...")
                    || page.title().contains("Attention Required")
                    || page.locator("#challenge-stage, .cf-turnstile, iframe[src*='challenges.cloudflare.com'], #turnstile-wrapper").count() > 0;

            if (isCloudflare) {
                log.info("Cloudflare challenge screen active on {}. Attempting automated bypass...", page.url());
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
                                Locator checkbox = frame.locator("input[type='checkbox'], span.mark, .ctp-checkbox-label, div[id*='checkbox']").first();
                                if (checkbox.isVisible()) {
                                    log.info("Clicking Cloudflare Turnstile checkbox inside challenge frame");
                                    checkbox.click(new Locator.ClickOptions().setTimeout(2000));
                                    page.waitForTimeout(3000);
                                    break;
                                }
                            } catch (Exception ignored) {}
                        }
                    }

                    // Attempt 2: If frame checkbox not directly clickable, click iframe directly
                    Locator cfIframe = page.locator("iframe[src*='challenges.cloudflare.com'], iframe[src*='turnstile'], #turnstile-wrapper iframe, #challenge-stage iframe").first();
                    if (cfIframe.isVisible()) {
                        try {
                            log.info("Clicking Turnstile iframe container directly");
                            cfIframe.click(new Locator.ClickOptions().setTimeout(2000));
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
}
