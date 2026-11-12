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
        this.loginButton = page.locator(".login-page button.login-button, button.login-button, button[type='submit'].login-button, .returning-wrapper button[type='submit']").first();
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
        return waitForVisible(errorMessage);
    }

    public boolean isEmailInputVisible() {
        return waitForVisible(emailInput);
    }

    public boolean isPasswordInputVisible() {
        return waitForVisible(passwordInput);
    }

    public boolean isLoginButtonVisible() {
        return waitForVisible(loginButton);
    }
}
