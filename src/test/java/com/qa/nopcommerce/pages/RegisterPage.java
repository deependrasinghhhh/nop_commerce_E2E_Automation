package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Customer Registration page (/register).
 */
public class RegisterPage extends BasePage {

    private final Locator genderMaleRadio;
    private final Locator genderFemaleRadio;
    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator emailInput;
    private final Locator companyInput;
    private final Locator newsletterCheckbox;
    private final Locator passwordInput;
    private final Locator confirmPasswordInput;
    private final Locator registerButton;
    private final Locator resultMessage;
    private final Locator continueButton;

    // Validation error locators
    private final Locator firstNameError;
    private final Locator lastNameError;
    private final Locator emailError;
    private final Locator passwordError;
    private final Locator confirmPasswordError;
    private final Locator validationSummaryError;

    public RegisterPage(Page page) {
        super(page);
        this.genderMaleRadio = page.locator("#gender-male");
        this.genderFemaleRadio = page.locator("#gender-female");
        this.firstNameInput = page.locator("#FirstName");
        this.lastNameInput = page.locator("#LastName");
        this.emailInput = page.locator("#Email");
        this.companyInput = page.locator("#Company");
        this.newsletterCheckbox = page.locator("#Newsletter");
        this.passwordInput = page.locator("#Password");
        this.confirmPasswordInput = page.locator("#ConfirmPassword");
        this.registerButton = page.locator("#register-button");
        this.resultMessage = page.locator(".result");
        this.continueButton = page.locator("a.register-continue-button");

        this.firstNameError = page.locator("#FirstName-error");
        this.lastNameError = page.locator("#LastName-error");
        this.emailError = page.locator("#Email-error");
        this.passwordError = page.locator("#Password-error");
        this.confirmPasswordError = page.locator("#ConfirmPassword-error");
        this.validationSummaryError = page.locator(".message-error.validation-summary-errors");
    }

    public void selectGender(String gender) {
        if ("male".equalsIgnoreCase(gender)) {
            genderMaleRadio.check();
        } else if ("female".equalsIgnoreCase(gender)) {
            genderFemaleRadio.check();
        }
    }

    public void fillRegistrationForm(String firstName, String lastName, String email,
                                   String company, String password, String confirmPassword) {
        fill(firstNameInput, firstName);
        fill(lastNameInput, lastName);
        fill(emailInput, email);
        if (company != null && !company.isEmpty()) {
            fill(companyInput, company);
        }
        fill(passwordInput, password);
        fill(confirmPasswordInput, confirmPassword);
    }

    public void clickRegisterButton() {
        click(registerButton);
        handleCloudflareIfPresent();
    }

    public String getSuccessMessage() {
        return getText(resultMessage);
    }

    public HomePage clickContinue() {
        click(continueButton);
        return new HomePage(page);
    }

    public String getFirstNameError() {
        return getText(firstNameError);
    }

    public String getLastNameError() {
        return getText(lastNameError);
    }

    public String getEmailError() {
        return getText(emailError);
    }

    public String getPasswordError() {
        return getText(passwordError);
    }

    public String getConfirmPasswordError() {
        return getText(confirmPasswordError);
    }

    public String getValidationSummary() {
        return getText(validationSummaryError);
    }

    public boolean isRegistrationSuccess() {
        return isVisible(resultMessage) && getText(resultMessage).contains("Your registration completed");
    }
}
