package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing Order Success / Checkout Completed page.
 */
public class OrderSuccessPage extends BasePage {

    private final Locator pageTitle;
    private final Locator completedTitle;
    private final Locator orderNumber;
    private final Locator detailsLink;
    private final Locator continueButton;

    public OrderSuccessPage(Page page) {
        super(page);
        this.pageTitle = page.locator(".page-title h1");
        this.completedTitle = page.locator(".section.order-completed .title strong");
        this.orderNumber = page.locator(".order-number strong, .order-completed .details");
        this.detailsLink = page.locator("a[href*='orderdetails']");
        this.continueButton = page.locator("button.order-completed-continue-button");
    }

    public boolean isOrderSuccessful() {
        return isVisible(completedTitle) && getText(completedTitle).contains("successfully processed");
    }

    public String getOrderNumber() {
        return getText(orderNumber);
    }

    public HomePage clickContinue() {
        click(continueButton);
        return new HomePage(page);
    }

    public Locator getCompletedTitle() {
        return completedTitle;
    }
}
