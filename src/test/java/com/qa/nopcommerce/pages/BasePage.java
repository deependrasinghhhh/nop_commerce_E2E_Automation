package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Page providing common locator wrappers and navigation hooks.
 *
 * <p>Playwright's {@link Locator} operations ({@code fill}, {@code click}, {@code innerText})
 * already perform auto-waiting (visible, enabled, editable) before acting.</p>
 */
public abstract class BasePage {
    protected final Page page;
    protected final Logger log = LoggerFactory.getLogger(getClass());

    protected BasePage(Page page) {
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
        locator.fill(value);
    }

    protected void click(Locator locator) {
        locator.click();
    }

    protected static final double DEFAULT_WAIT_TIMEOUT_MS = 15_000;

    protected String getText(Locator locator) {
        try {
            locator.first().waitFor(new Locator.WaitForOptions().setTimeout(DEFAULT_WAIT_TIMEOUT_MS));
            return locator.first().innerText().trim();
        } catch (Exception e) {
            return "";
        }
    }

    protected boolean isVisible(Locator locator) {
        try {
            return locator.first().isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean waitForVisible(Locator locator) {
        return waitForVisible(locator, DEFAULT_WAIT_TIMEOUT_MS);
    }

    protected boolean waitForVisible(Locator locator, double timeoutMs) {
        try {
            locator.first().waitFor(new Locator.WaitForOptions().setTimeout(timeoutMs));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean waitForCondition(java.util.function.BooleanSupplier condition) {
        return waitForCondition(condition, DEFAULT_WAIT_TIMEOUT_MS);
    }

    protected boolean waitForCondition(java.util.function.BooleanSupplier condition, double timeoutMs) {
        try {
            page.waitForCondition(() -> {
                try {
                    return condition.getAsBoolean();
                } catch (com.microsoft.playwright.PlaywrightException e) {
                    String msg = e.getMessage();
                    if (msg != null && (msg.contains("Execution context was destroyed")
                            || msg.contains("Cannot find context")
                            || msg.contains("not attached"))) {
                        return false;
                    }
                    throw e;
                }
            }, new Page.WaitForConditionOptions().setTimeout(timeoutMs));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    protected boolean waitForTextContains(Locator locator, String expectedSubstring) {
        return waitForCondition(() -> locator.count() > 0
                && locator.first().isVisible()
                && locator.first().innerText().trim().contains(expectedSubstring));
    }

    protected boolean waitForCount(Locator locator, int expectedCount) {
        return waitForCondition(() -> locator.count() == expectedCount);
    }

    protected boolean waitForMinCount(Locator locator, int minCount) {
        return waitForCondition(() -> locator.count() >= minCount);
    }

    protected boolean waitForCountLessThan(Locator locator, int maxExclusive) {
        return waitForCondition(() -> locator.count() < maxExclusive);
    }

    protected int safeCount(Locator locator) {
        try {
            return locator.count();
        } catch (Exception e) {
            return 0;
        }
    }
}
