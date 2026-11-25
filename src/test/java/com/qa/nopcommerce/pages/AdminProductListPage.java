package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Administration Products List (/Admin/Product/List).
 */
public class AdminProductListPage extends BasePage {

    private final Locator productsGrid;
    private final Locator gridProcessingOverlay;
    private final Locator searchProductNameInput;
    private final Locator searchButton;

    public AdminProductListPage(Page page) {
        super(page);
        this.productsGrid = page.locator("table#products-grid");
        this.gridProcessingOverlay = page.locator("#products-grid_processing");
        this.searchProductNameInput = page.locator("#SearchProductName");
        this.searchButton = page.locator("#search-products");
    }

    public void waitForGrid() {
        waitForVisible(productsGrid);
        waitForCondition(() -> gridProcessingOverlay.count() == 0 || !isVisible(gridProcessingOverlay), 15_000);
        waitForLoad();
    }

    public boolean isGridVisible() {
        return waitForVisible(productsGrid);
    }

    public void searchProduct(String productName) {
        fill(searchProductNameInput, productName);
        click(searchButton);
        waitForCondition(() -> gridProcessingOverlay.count() == 0 || !isVisible(gridProcessingOverlay), 15_000);
        waitForTextContains(productsGrid.locator("tbody"), productName);
    }

    public int getProductRowCount() {
        return productsGrid.locator("tbody tr").count();
    }
}
