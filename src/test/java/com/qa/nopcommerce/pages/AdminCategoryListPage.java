package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Administration Categories List (/Admin/Category/List).
 */
public class AdminCategoryListPage extends BasePage {

    private final Locator categoriesGrid;

    public AdminCategoryListPage(Page page) {
        super(page);
        this.categoriesGrid = page.locator("table#categories-grid");
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
