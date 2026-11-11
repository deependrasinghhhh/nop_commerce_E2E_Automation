package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Administration Products List (/Admin/Product/List).
 */
public class AdminProductListPage extends BasePage {

    private final Locator productsGrid;
    private final Locator searchProductNameInput;
    private final Locator searchButton;

    public AdminProductListPage(Page page) {
        super(page);
        this.productsGrid = page.locator("table#products-grid");
        this.searchProductNameInput = page.locator("#SearchProductName");
        this.searchButton = page.locator("#search-products");
    }

    public void waitForGrid() {
        productsGrid.waitFor();
    }

    public boolean isGridVisible() {
        return isVisible(productsGrid);
    }

    public void searchProduct(String productName) {
        fill(searchProductNameInput, productName);
        click(searchButton);
        waitForGrid();
    }

    public int getProductRowCount() {
        return productsGrid.locator("tbody tr").count();
    }
}
