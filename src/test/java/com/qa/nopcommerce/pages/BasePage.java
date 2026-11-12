package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.PlaywrightException;
import com.qa.nopcommerce.config.ConfigReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BooleanSupplier;

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

    protected static double getDefaultWaitTimeoutMs() {
        return Math.max(15_000, ConfigReader.getDefaultTimeout());
    }

    protected String getText(Locator locator) {
        try {
            double timeoutMs = getDefaultWaitTimeoutMs();
            if (!waitForAnyVisible(timeoutMs, locator)) {
                return "";
            }
            int count = locator.count();
            for (int i = 0; i < count; i++) {
                Locator nth = locator.nth(i);
                if (nth.isVisible()) {
                    return nth.innerText().trim();
                }
            }
            return "";
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
        return waitForVisible(locator, getDefaultWaitTimeoutMs());
    }

    protected boolean waitForVisible(Locator locator, double timeoutMs) {
        return waitForAnyVisible(timeoutMs, locator);
    }

    protected boolean waitForAnyVisible(Locator... locators) {
        return waitForAnyVisible(getDefaultWaitTimeoutMs(), locators);
    }

    protected boolean waitForAnyVisible(double timeoutMs, Locator... locators) {
        return waitForCondition(() -> {
            for (Locator locator : locators) {
                int count = locator.count();
                for (int i = 0; i < count; i++) {
                    if (locator.nth(i).isVisible()) {
                        return true;
                    }
                }
            }
            return false;
        }, timeoutMs);
    }

    protected boolean waitForCondition(BooleanSupplier condition) {
        return waitForCondition(condition, getDefaultWaitTimeoutMs());
    }

    protected boolean waitForCondition(BooleanSupplier condition, double timeoutMs) {
        try {
            page.waitForCondition(() -> {
                try {
                    return condition.getAsBoolean();
                } catch (PlaywrightException e) {
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
        return waitForCondition(() -> {
            int count = locator.count();
            for (int i = 0; i < count; i++) {
                Locator nth = locator.nth(i);
                if (nth.isVisible() && nth.innerText().trim().contains(expectedSubstring)) {
                    return true;
                }
            }
            return false;
        });
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
