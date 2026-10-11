package com.qa.nopcommerce.tests.ui;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.config.ConfigReader;
import com.qa.nopcommerce.pages.HomePage;
import com.qa.nopcommerce.pages.LoginPage;
import com.qa.nopcommerce.pages.RegisterPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.regex.Pattern;

@Epic("Storefront Core")
@Feature("Authentication & Account Lifecycle")
public class AuthTest extends BaseTest {

    @Test(groups = {"ui", "auth", "smoke"})
    @TestCase("TC-AUTH-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify valid customer registration with all mandatory fields completed")
    public void testValidCustomerRegistration() {
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(getPage());

        String uniqueEmail = "nop_qa_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        registerPage.selectGender("male");
        registerPage.fillRegistrationForm("Alex", "QA Tester", uniqueEmail, "QA Corp", "Test@123456", "Test@123456");
        registerPage.clickRegisterButton();

        PlaywrightAssertions.assertThat(getPage().locator(".result"))
                .hasText(Pattern.compile(".*Your registration completed.*"));
    }

    @Test(groups = {"ui", "auth"})
    @TestCase("TC-AUTH-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify registration form field validation when mandatory fields are omitted")
    public void testRegistrationMandatoryFieldValidation() {
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(getPage());

        registerPage.clickRegisterButton();

        Assert.assertTrue(registerPage.getFirstNameError().toLowerCase().contains("first name") || getPage().locator(".field-validation-error").count() > 0,
                "First name validation error mismatch");
        Assert.assertTrue(registerPage.getLastNameError().toLowerCase().contains("last name") || getPage().locator(".field-validation-error").count() > 0,
                "Last name validation error mismatch");
        Assert.assertTrue(registerPage.getEmailError().toLowerCase().contains("email") || getPage().locator(".field-validation-error").count() > 0,
                "Email validation error mismatch");
        Assert.assertTrue(registerPage.getPasswordError().toLowerCase().contains("password") || getPage().locator(".field-validation-error").count() > 0,
                "Password validation error mismatch");
    }

    @Test(groups = {"ui", "auth"})
    @TestCase("TC-AUTH-003")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify registration with malformed email format triggers inline error")
    public void testRegistrationMalformedEmailValidation() {
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(getPage());

        registerPage.fillRegistrationForm("Alex", "QA Tester", "invalid-email-format", "QA Corp", "Test@123456", "Test@123456");
        registerPage.clickRegisterButton();

        String emailErr = registerPage.getEmailError();
        boolean hasEmailError = emailErr.toLowerCase().contains("email") || emailErr.toLowerCase().contains("wrong") || emailErr.toLowerCase().contains("valid") || getPage().locator("#Email:invalid, .field-validation-error").count() > 0;
        Assert.assertTrue(hasEmailError, "Expected email validation error or HTML5 invalid state");
    }

    @Test(groups = {"ui", "auth"})
    @TestCase("TC-AUTH-004")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify registration fails when password and confirm password do not match")
    public void testRegistrationPasswordMismatch() {
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(getPage());

        registerPage.fillRegistrationForm("Alex", "QA Tester", "mismatch@example.com", "QA Corp", "Password@1", "Password@2");
        registerPage.clickRegisterButton();

        Assert.assertTrue(registerPage.getConfirmPasswordError().contains("The password and confirmation password do not match"),
                "Expected password mismatch error");
    }

    @Test(groups = {"ui", "auth", "smoke"})
    @TestCase("TC-AUTH-006")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify customer login with valid credentials")
    public void testCustomerLoginValidCredentials() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(getPage());

        loginPage.login(ConfigReader.getAdminEmail(), ConfigReader.getAdminPassword());

        PlaywrightAssertions.assertThat(getPage().locator("a.ico-account, a.ico-logout, a:has-text('Log out'), a:has-text('My account')").first()).isVisible();
    }

    @Test(groups = {"ui", "auth"})
    @TestCase("TC-AUTH-007")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer login with invalid password displays authentication error")
    public void testCustomerLoginInvalidPassword() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(getPage());

        loginPage.login("admin@yourstore.com", "WrongPassword999!");

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Expected login error message");
        Assert.assertTrue(loginPage.getErrorMessage().contains("unsuccessful") || loginPage.getErrorMessage().contains("No customer account found"),
                "Expected error message indicating login failure");
    }

    @Test(groups = {"ui", "auth"})
    @TestCase("TC-AUTH-008")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify customer login validation when email field is blank")
    public void testCustomerLoginBlankEmail() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(getPage());

        loginPage.login("", "AnyPassword123");

        Assert.assertTrue(loginPage.getEmailError().contains("Please enter your email"),
                "Expected email required validation message");
    }

    @Test(groups = {"ui", "auth"})
    @TestCase("TC-AUTH-011")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer logout clears session and restores login link in header")
    public void testCustomerLogout() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.login(ConfigReader.getAdminEmail(), ConfigReader.getAdminPassword());

        HomePage homePage = new HomePage(getPage());
        homePage.clickLogout();

        PlaywrightAssertions.assertThat(getPage().locator("a.ico-login, a:has-text('Log in')").first()).isVisible();
    }
}
