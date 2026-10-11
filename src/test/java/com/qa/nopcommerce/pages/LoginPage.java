package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Customer Login page (/login).
 */
public class LoginPage extends BasePage {

    private final Locator emailInput;
    private final Locator passwordInput;
    private final Locator rememberMeCheckbox;
    private final Locator forgotPasswordLink;
    private final Locator loginButton;
    private final Locator emailError;
    private final Locator validationSummaryError;

    public LoginPage(Page page) {
        super(page);
        this.emailInput = page.locator("#Email");
        this.passwordInput = page.locator("#Password");
        this.rememberMeCheckbox = page.locator("#RememberMe");
        this.forgotPasswordLink = page.locator("a[href*='passwordrecovery']");
        this.loginButton = page.locator("button.login-button");
        this.emailError = page.locator("#Email-error");
        this.validationSummaryError = page.locator(".message-error.validation-summary-errors");
    }

    public void login(String email, String password) {
        fill(emailInput, email);
        fill(passwordInput, password);
        click(loginButton);
    }

    public void loginWithRememberMe(String email, String password) {
        fill(emailInput, email);
        fill(passwordInput, password);
        rememberMeCheckbox.check();
        click(loginButton);
    }

    public String getEmailError() {
        return getText(emailError);
    }

    public String getErrorMessage() {
        return getText(validationSummaryError);
    }

    public boolean isErrorMessageDisplayed() {
        return isVisible(validationSummaryError);
    }

    public void clickForgotPassword() {
        click(forgotPasswordLink);
    }
}
