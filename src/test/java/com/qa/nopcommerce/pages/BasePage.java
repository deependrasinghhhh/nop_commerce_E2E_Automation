package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Base Page providing common locator wrappers and navigation hooks.
 *
 * <p>Playwright's {@link Locator} operations ({@code fill}, {@code click}, {@code innerText})
 * already perform auto-waiting (visible, enabled, editable) before acting. The wrapper methods
 * in this class therefore <strong>do not add redundant waits</strong>; they exist only to
 * provide logging and a consistent API surface for subclasses.</p>
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

    /**
     * Waits for the page's default load state ({@code load}).
     * Use only after an action that triggers a full navigation or heavy async load;
     * prefer waiting for a specific UI element instead.
     */
    public void waitForLoad() {
        page.waitForLoadState();
    }

    /**
     * Fills a text input. Playwright auto-waits for the element to be attached,
     * visible, enabled, and editable before filling.
     */
    protected void fill(Locator locator, String value) {
        locator.fill(value);
    }

    /**
     * Clicks an element. Playwright auto-waits for the element to be attached,
     * visible, enabled, and stable before clicking.
     */
    protected void click(Locator locator) {
        locator.click();
    }

    /**
     * Returns the visible inner text of the first matching element, or empty string
     * if the element is not found within a short timeout.
     */
    protected String getText(Locator locator) {
        try {
            locator.first().waitFor(new Locator.WaitForOptions().setTimeout(4000));
            return locator.first().innerText().trim();
        } catch (Exception e) {
            try {
                if (locator.count() > 0) {
                    return locator.first().innerText().trim();
                }
            } catch (Exception ignored) { /* element detached or stale */ }
            return "";
        }
    }

    /**
     * Non-throwing visibility check. Returns {@code false} if the element is absent,
     * detached, or hidden.
     */
    protected boolean isVisible(Locator locator) {
        try {
            return locator.isVisible();
        } catch (Exception e) {
            return false;
        }
    }
}
