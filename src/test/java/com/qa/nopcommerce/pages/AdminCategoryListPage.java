package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Administration Categories List (/Admin/Category/List).
 */
public class AdminCategoryListPage extends BasePage {

    private final Locator categoriesGrid;
    private final Locator searchCategoryNameInput;
    private final Locator searchButton;

    public AdminCategoryListPage(Page page) {
        super(page);
        this.categoriesGrid = page.locator("table#categories-grid");
        this.searchCategoryNameInput = page.locator("#SearchCategoryName");
        this.searchButton = page.locator("#search-categories");
    }

    public void waitForGrid() {
        categoriesGrid.waitFor();
    }

    public boolean isGridVisible() {
        return waitForVisible(categoriesGrid);
    }

    public int getCategoryRowCount() {
        return categoriesGrid.locator("tbody tr").count();
    }
}
