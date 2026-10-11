package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing My Account - Customer Info and Orders history (/customer/info).
 */
public class CustomerInfoPage extends BasePage {

    private final Locator pageTitle;
    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator emailInput;
    private final Locator saveButton;
    private final Locator ordersMenuLink;
    private final Locator addressesMenuLink;
    private final Locator changePasswordMenuLink;

    public CustomerInfoPage(Page page) {
        super(page);
        this.pageTitle = page.locator(".page-title h1");
        this.firstNameInput = page.locator("#FirstName");
        this.lastNameInput = page.locator("#LastName");
        this.emailInput = page.locator("#Email");
        this.saveButton = page.locator("#save-info-button");
        this.ordersMenuLink = page.locator("a[href*='customer/orders']");
        this.addressesMenuLink = page.locator("a[href*='customer/addresses']");
        this.changePasswordMenuLink = page.locator("a[href*='customer/changepassword']");
    }

    public String getFirstName() {
        return firstNameInput.inputValue();
    }

    public String getLastName() {
        return lastNameInput.inputValue();
    }

    public String getEmail() {
        return emailInput.inputValue();
    }

    public void updateName(String firstName, String lastName) {
        fill(firstNameInput, firstName);
        fill(lastNameInput, lastName);
        click(saveButton);
        waitForLoad();
    }

    public void clickOrders() {
        click(ordersMenuLink);
        waitForLoad();
    }

    public void clickAddresses() {
        click(addressesMenuLink);
        waitForLoad();
    }

    public void clickChangePassword() {
        click(changePasswordMenuLink);
        waitForLoad();
    }

    public boolean isPageLoaded() {
        return isVisible(firstNameInput) && isVisible(emailInput);
    }
}
