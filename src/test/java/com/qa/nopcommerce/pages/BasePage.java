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

    protected String getText(Locator locator) {
        try {
            locator.first().waitFor(new Locator.WaitForOptions().setTimeout(5000));
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
}
