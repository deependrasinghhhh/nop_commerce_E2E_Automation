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
        this.errorMessage = page.locator(".message-error, .validation-summary-errors");
    }

    public AdminDashboardPage login(String email, String password) {
        if (emailInput.isVisible()) {
            fill(emailInput, email);
            fill(passwordInput, password);
            click(loginButton);
            handleCloudflareIfPresent();
            try {
                page.waitForURL(java.util.regex.Pattern.compile(".*(admin|dashboard).*"), new Page.WaitForURLOptions().setTimeout(15000));
            } catch (Exception ignored) {}
        }
        return new AdminDashboardPage(page);
    }

    public void clickLogin() {
        click(loginButton);
        handleCloudflareIfPresent();
    }

    public String getErrorMessage() {
        handleCloudflareIfPresent();
        return getText(errorMessage.first());
    }

    public boolean isErrorMessageDisplayed() {
        handleCloudflareIfPresent();
        try {
            errorMessage.first().waitFor(new Locator.WaitForOptions().setTimeout(10000));
            return isVisible(errorMessage.first());
        } catch (Exception e) {
            return false;
        }
    }

    public Locator getEmailInput() {
        return emailInput;
    }

    public Locator getPasswordInput() {
        return passwordInput;
    }
}
