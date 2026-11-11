package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Administration Orders List (/Admin/Order/List).
 */
public class AdminOrderListPage extends BasePage {

    private final Locator ordersGrid;
    private final Locator searchButton;

    public AdminOrderListPage(Page page) {
        super(page);
        this.ordersGrid = page.locator("table#orders-grid");
        this.searchButton = page.locator("#search-orders");
    }

    public void waitForGrid() {
        ordersGrid.waitFor();
    }

    public boolean isGridVisible() {
        return isVisible(ordersGrid);
    }

    public int getOrderRowCount() {
        return ordersGrid.locator("tbody tr").count();
    }
}
