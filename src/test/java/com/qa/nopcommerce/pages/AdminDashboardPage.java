package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Administration Dashboard (/admin).
 */
public class AdminDashboardPage extends BasePage {

    private final Locator dashboardHeader;
    private final Locator logoutLink;
    private final Locator catalogMenu;
    private final Locator salesMenu;
    private final Locator customersMenu;
    private final Locator statCards;

    public AdminDashboardPage(Page page) {
        super(page);
        this.dashboardHeader = page.locator(".content-header h1");
        this.logoutLink = page.locator("a[href*='logout']");
        this.catalogMenu = page.locator(".nav-sidebar a:has-text('Catalog')").first();
        this.salesMenu = page.locator(".nav-sidebar a:has-text('Sales')").first();
        this.customersMenu = page.locator(".nav-sidebar a:has-text('Customers')").first();
        this.statCards = page.locator(".small-box");
    }

    public boolean isDashboardLoaded() {
        return isVisible(dashboardHeader) && getText(dashboardHeader).contains("Dashboard");
    }

    public int getStatisticsCardCount() {
        return statCards.count();
    }

    public void openCatalogMenu() {
        click(catalogMenu);
    }

    public void openSalesMenu() {
        click(salesMenu);
    }

    public void openCustomersMenu() {
        click(customersMenu);
    }

    public AdminLoginPage logout() {
        click(logoutLink);
        return new AdminLoginPage(page);
    }

    public Locator getDashboardHeader() {
        return dashboardHeader;
    }
}
