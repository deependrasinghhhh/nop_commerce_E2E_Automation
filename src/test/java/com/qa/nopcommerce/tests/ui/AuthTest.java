package com.qa.nopcommerce.tests.ui;

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

@Epic("Storefront Core")
@Feature("Authentication & Account Lifecycle")
public class AuthTest extends BaseTest {

    @Test(groups = {"ui", "auth", "smoke"})
    @TestCase(value = "TC-AUTH-001", requirementId = "REQ-AUTH-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify valid customer registration with all mandatory fields completed")
    public void testValidCustomerRegistration() {
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(getPage());

        String uniqueEmail = "nop_qa_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String dynamicPassword = "QaAuth@" + UUID.randomUUID().toString().substring(0, 8);
        registerPage.selectGender("male");
        registerPage.fillRegistrationForm("Alex", "QA Tester", uniqueEmail, "QA Corp", dynamicPassword, dynamicPassword);
        registerPage.clickRegisterButton();

        Assert.assertTrue(registerPage.isRegistrationSuccess(), "Expected registration to complete successfully");
    }

    @Test(groups = {"ui", "auth"})
    @TestCase(value = "TC-AUTH-002", requirementId = "REQ-AUTH-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify registration form field validation when mandatory fields are omitted")
    public void testRegistrationMandatoryFieldValidation() {
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(getPage());

        registerPage.clickRegisterButton();

        Assert.assertTrue(registerPage.isFirstNameErrorVisible(), "Expected FirstName error");
        Assert.assertTrue(registerPage.isLastNameErrorVisible(), "Expected LastName error");
        Assert.assertTrue(registerPage.isEmailErrorVisible(), "Expected Email error");
        Assert.assertTrue(registerPage.isPasswordOrConfirmErrorVisible(), "Expected Password or ConfirmPassword error");
    }

    @Test(groups = {"ui", "auth"})
    @TestCase(value = "TC-AUTH-005", requirementId = "REQ-AUTH-002")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify registration with malformed email format triggers inline error")
    public void testRegistrationMalformedEmailValidation() {
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(getPage());

        String dynamicPassword = "QaAuth@" + UUID.randomUUID().toString().substring(0, 8);
        registerPage.fillRegistrationForm("Alex", "QA Tester", "invalid-email-format", "QA Corp", dynamicPassword, dynamicPassword);
        registerPage.clickRegisterButton();

        Assert.assertTrue(registerPage.isEmailErrorVisible(), "Expected Email format error");
    }

    @Test(groups = {"ui", "auth"})
    @TestCase(value = "TC-AUTH-008", requirementId = "REQ-AUTH-004")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify registration fails when password and confirm password do not match")
    public void testRegistrationPasswordMismatch() {
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(getPage());

        String uniqueSuffix = UUID.randomUUID().toString().substring(0, 8);
        registerPage.fillRegistrationForm("Alex", "QA Tester", "mismatch_" + uniqueSuffix + "@example.com", "QA Corp",
                "PassA_" + uniqueSuffix, "PassB_" + uniqueSuffix);
        registerPage.clickRegisterButton();

        Assert.assertTrue(registerPage.getConfirmPasswordError().contains("The password and confirmation password do not match."),
                "Expected password mismatch confirmation error");
    }

    @Test(groups = {"ui", "auth", "smoke"})
    @TestCase(value = "TC-AUTH-009", requirementId = "REQ-AUTH-005")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify customer login with valid registered credentials")
    public void testCustomerLoginValidCredentials() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(getPage());

        loginPage.login(ConfigReader.getAdminEmail(), ConfigReader.getAdminPassword());

        HomePage homePage = new HomePage(getPage());
        Assert.assertTrue(homePage.isCustomerLoggedIn(), "Expected customer account or logout link to be visible");
    }

    @Test(groups = {"ui", "auth"})
    @TestCase(value = "TC-AUTH-011", requirementId = "REQ-AUTH-006")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer login with invalid password displays authentication error")
    public void testCustomerLoginInvalidPassword() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(getPage());

        String invalidPassword = "Invalid_" + UUID.randomUUID().toString().substring(0, 8);
        loginPage.login(ConfigReader.getAdminEmail(), invalidPassword);

        Assert.assertTrue(loginPage.isErrorMessageDisplayed(), "Expected login error message");
        Assert.assertTrue(loginPage.getErrorMessage().contains("Login was unsuccessful"),
                "Expected error message indicating login failure");
    }

    @Test(groups = {"ui", "auth"})
    @TestCase(value = "TC-AUTH-012", requirementId = "REQ-AUTH-006")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify customer login validation when email field is blank")
    public void testCustomerLoginBlankEmail() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(getPage());

        loginPage.login("", "Invalid_" + UUID.randomUUID().toString().substring(0, 8));

        Assert.assertTrue(loginPage.getEmailError().contains("Please enter your email"),
                "Expected please enter your email error");
    }

    @Test(groups = {"ui", "auth"})
    @TestCase(value = "TC-AUTH-013", requirementId = "REQ-AUTH-007")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer logout clears session and restores login link in header")
    public void testCustomerLogout() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.login(ConfigReader.getAdminEmail(), ConfigReader.getAdminPassword());

        HomePage homePage = new HomePage(getPage());
        homePage.clickLogout();

        Assert.assertTrue(homePage.isLoginLinkVisible(), "Expected login link to be visible after logout");
    }
}
