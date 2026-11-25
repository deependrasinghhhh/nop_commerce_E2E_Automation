package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing Order Success / Checkout Completed page.
 */
public class OrderSuccessPage extends BasePage {

    private final Locator completedTitle;
    private final Locator orderNumber;
    private final Locator detailsLink;
    private final Locator continueButton;

    public OrderSuccessPage(Page page) {
        super(page);
        this.completedTitle = page.locator(".section.order-completed .title, .section.order-completed .title strong").first();
        this.orderNumber = page.locator(".order-completed .order-number strong, .order-number strong").first();
        this.detailsLink = page.locator("a[href*='orderdetails']");
        this.continueButton = page.locator("button.order-completed-continue-button");
    }

    public boolean isOrderSuccessful() {
        return waitForTextContains(completedTitle, "successfully processed");
    }

    public String getOrderNumber() {
        waitForVisible(orderNumber);
        return getText(orderNumber);
    }

    public int getOrderId() {
        String digits = getOrderNumber().replaceAll("\\D+", "");
        if (digits.isBlank()) {
            throw new IllegalStateException("Could not parse numeric order ID from text: " + getOrderNumber());
        }
        return Integer.parseInt(digits);
    }

    public boolean isOrderDetailsLinkVisible() {
        return waitForVisible(detailsLink);
    }

    public HomePage clickContinue() {
        click(continueButton);
        return new HomePage(page);
    }
}
