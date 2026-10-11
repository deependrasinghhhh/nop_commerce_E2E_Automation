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
}
