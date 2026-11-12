package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Administration Customers List (/Admin/Customer/List).
 */
public class AdminCustomerListPage extends BasePage {

    private final Locator customersGrid;
    private final Locator searchEmailInput;
    private final Locator searchButton;

    public AdminCustomerListPage(Page page) {
        super(page);
        this.customersGrid = page.locator("table#customers-grid");
        this.searchEmailInput = page.locator("#SearchEmail");
        this.searchButton = page.locator("#search-customers");
    }

    public void waitForGrid() {
        customersGrid.waitFor();
    }

    public boolean isGridVisible() {
        return waitForVisible(customersGrid);
    }

    public int getCustomerRowCount() {
        return customersGrid.locator("tbody tr").count();
    }
}
