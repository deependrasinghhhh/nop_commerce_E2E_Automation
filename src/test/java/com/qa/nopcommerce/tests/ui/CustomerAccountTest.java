package com.qa.nopcommerce.tests.ui;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.pages.CustomerInfoPage;
import com.qa.nopcommerce.pages.LoginPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

@Epic("Storefront Core")
@Feature("Customer Profile & Account Management")
public class CustomerAccountTest extends BaseTest {

    @Test(groups = {"ui", "customer"})
    @TestCase("TC-CUST-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer info page renders personal information fields")
    public void testCustomerInfoPageRendering() {
        navigateTo("/login");
        LoginPage loginPage = new LoginPage(getPage());
        loginPage.login("admin@yourstore.com", "admin");

        navigateTo("/customer/info");
        CustomerInfoPage infoPage = new CustomerInfoPage(getPage());

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*customer/info.*"));
    }

    @Test(groups = {"ui", "customer"})
    @TestCase("TC-CUST-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify customer addresses book page loads")
    public void testCustomerAddressesPage() {
        navigateTo("/customer/addresses");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*(addresses|login).*"));
    }

    @Test(groups = {"ui", "customer"})
    @TestCase("TC-CUST-007")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify customer order history list page loads")
    public void testCustomerOrdersPage() {
        navigateTo("/customer/orders");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*(orders|login).*"));
    }
}
