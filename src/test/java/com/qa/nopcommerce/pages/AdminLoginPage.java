package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Administration Login portal.
 */
public class AdminLoginPage extends BasePage {

    private final Locator emailInput;
    private final Locator passwordInput;
    private final Locator loginButton;
    private final Locator errorMessage;

    public AdminLoginPage(Page page) {
        super(page);
        this.emailInput = page.locator("#Email");
        this.passwordInput = page.locator("#Password");
        this.loginButton = page.locator("button[type='submit']");
        this.errorMessage = page.locator(".message-error");
    }

    public AdminDashboardPage login(String email, String password) {
        fill(emailInput, email);
        fill(passwordInput, password);
        click(loginButton);
        try {
            page.waitForLoadState();
        } catch (Exception ignored) {}
        return new AdminDashboardPage(page);
    }

    public void clickLogin() {
        click(loginButton);
    }

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isErrorMessageDisplayed() {
        return isVisible(errorMessage);
    }

    public Locator getEmailInput() {
        return emailInput;
    }

    public Locator getPasswordInput() {
        return passwordInput;
    }
}
