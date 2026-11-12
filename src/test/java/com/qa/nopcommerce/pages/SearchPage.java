package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Search results page (/search).
 */
public class SearchPage extends BasePage {

    private final Locator searchInput;
    private final Locator searchButton;
    private final Locator noResult;
    private final Locator warning;
    private final Locator searchResults;

    public SearchPage(Page page) {
        super(page);
        this.searchInput = page.locator("#q, #small-searchterms");
        this.searchButton = page.locator("button.search-button, button.search-box-button");
        this.noResult = page.locator(".no-result");
        this.warning = page.locator(".warning, .no-result");
        this.searchResults = page.locator(".search-results, .products-wrapper");
    }

    public boolean isNoResultVisible() {
        return waitForVisible(noResult);
    }

    public boolean isWarningVisible() {
        return waitForVisible(warning);
    }

    public String getWarningText() {
        return getText(warning);
    }

    public int getSearchResultCount() {
        return safeCount(page.locator(".search-results .product-item"));
    }

    public boolean hasSearchResults() {
        return waitForMinCount(page.locator(".search-results .product-item"), 1);
    }

    public boolean isSearchResultsVisible() {
        return waitForVisible(searchResults);
    }
}
