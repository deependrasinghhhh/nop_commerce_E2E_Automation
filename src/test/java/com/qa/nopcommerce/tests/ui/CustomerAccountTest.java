package com.qa.nopcommerce.tests.ui;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.database.DatabaseManager;
import com.qa.nopcommerce.pages.CustomerInfoPage;
import com.qa.nopcommerce.pages.ProductDetailsPage;
import com.qa.nopcommerce.pages.RegisterPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.annotations.Test;

import java.util.UUID;
import java.util.regex.Pattern;

@Epic("Storefront Core")
@Feature("Customer Profile, Address CRUD & Product Reviews")
public class CustomerAccountTest extends BaseTest {

    private String registerIsolatedCustomer(String prefix) {
        String email = prefix + "_" + UUID.randomUUID().toString().substring(0, 8) + "@example.com";
        String password = "QaUser@" + UUID.randomUUID().toString().substring(0, 8);
        navigateTo("/register");
        RegisterPage registerPage = new RegisterPage(getPage());
        registerPage.selectGender("male");
        registerPage.fillRegistrationForm("Alex", "CustomerQA", email, "Isolated QA Org", password, password);
        registerPage.clickRegisterButton();
        PlaywrightAssertions.assertThat(getPage().locator(".result"))
                .hasText(Pattern.compile(".*Your registration completed.*"));
        return email;
    }

    @Test(groups = {"ui", "customer"})
    @TestCase(value = "TC-CUST-001", requirementId = "REQ-CUST-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer info page renders personal information fields and persists profile updates")
    public void testCustomerInfoPageRendering() {
        String email = registerIsolatedCustomer("nop_profile");

        navigateTo("/customer/info");
        CustomerInfoPage infoPage = new CustomerInfoPage(getPage());

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*customer/info.*"));
        PlaywrightAssertions.assertThat(infoPage.getFirstNameInput()).hasValue("Alex");
        PlaywrightAssertions.assertThat(infoPage.getEmailInput()).hasValue(email);

        infoPage.updateName("Alexander", "UpdatedQA");
        PlaywrightAssertions.assertThat(infoPage.getFirstNameInput()).hasValue("Alexander");
    }

    @Test(groups = {"ui", "customer", "address"})
    @TestCase(value = "TC-CUST-002", requirementId = "REQ-CUST-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify customer can create, edit, and delete an address in the Customer Address Book with full cleanup")
    public void testCustomerAddressCreateEditAndDelete() {
        String email = registerIsolatedCustomer("nop_addr");
        String initialStreet = "101 QA Automation Way " + UUID.randomUUID().toString().substring(0, 4);
        String updatedStreet = "202 Updated Suite Blvd " + UUID.randomUUID().toString().substring(0, 4);

        navigateTo("/customer/addresses");
        CustomerInfoPage infoPage = new CustomerInfoPage(getPage());
        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*customer/addresses.*"));

        // 1. Create Address
        infoPage.addNewAddress(
                "Alex",
                "CustomerQA",
                email,
                "United States of America",
                "Texas",
                "Austin",
                initialStreet,
                "78701",
                "5125550199"
        );
        PlaywrightAssertions.assertThat(infoPage.getAddressCardByText(initialStreet)).isVisible();

        // 2. Edit Address
        infoPage.editAddressByStreet(initialStreet, "Washington", "Seattle", updatedStreet, "98101");
        PlaywrightAssertions.assertThat(infoPage.getAddressCardByText(updatedStreet)).isVisible();
        PlaywrightAssertions.assertThat(infoPage.getAddressCardByText(updatedStreet)).containsText("Seattle");

        // 3. Delete Address (TC-CUST-003 cleanup)
        infoPage.deleteAddressByStreet(updatedStreet);
        PlaywrightAssertions.assertThat(infoPage.getNoAddressesMessage()).containsText("No addresses");
    }

    @Test(groups = {"ui", "customer", "address"})
    @TestCase(value = "TC-CUST-003", requirementId = "REQ-CUST-002")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify deleting a saved customer address removes it from the address book and leaves no residual records")
    public void testDeleteCustomerAddressFromAddressBook() {
        String email = registerIsolatedCustomer("nop_deladdr");
        String street = "303 Ephemeral Lane " + UUID.randomUUID().toString().substring(0, 4);

        navigateTo("/customer/addresses");
        CustomerInfoPage infoPage = new CustomerInfoPage(getPage());
        infoPage.addNewAddress(
                "Alex",
                "CustomerQA",
                email,
                "United States of America",
                "Colorado",
                "Denver",
                street,
                "80202",
                "3035550144"
        );
        PlaywrightAssertions.assertThat(infoPage.getAddressCardByText(street)).isVisible();

        infoPage.deleteAddressByStreet(street);
        PlaywrightAssertions.assertThat(infoPage.getNoAddressesMessage()).containsText("No addresses");
    }

    @Test(groups = {"ui", "customer"})
    @TestCase(value = "TC-CUST-004", requirementId = "REQ-CUST-003")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify authenticated customer order history page loads cleanly")
    public void testCustomerOrdersPage() {
        registerIsolatedCustomer("nop_orders");
        navigateTo("/order/history");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*order/history.*"));
        PlaywrightAssertions.assertThat(getPage().locator(".page-title h1")).containsText("My account - Orders");
    }

    @Test(groups = {"ui", "customer", "reviews"})
    @TestCase(value = "TC-CUST-005", requirementId = "REQ-CUST-004")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify registered user can submit a product review and verify its rendering on the product page")
    public void testSubmitProductReviewAsRegisteredUser() {
        registerIsolatedCustomer("nop_reviewer");
        String reviewTitle = "QA Verified Review " + UUID.randomUUID().toString().substring(0, 6);
        String reviewBody = "Automated E2E verification of product review submission with isolated customer account.";

        try {
            navigateTo("/build-your-own-computer");
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.openProductReviewsSection();

            productPage.submitProductReview(reviewTitle, reviewBody);

            PlaywrightAssertions.assertThat(productPage.getReviewResultNotification())
                    .containsText(Pattern.compile(".*Product review is successfully added.*"));
            PlaywrightAssertions.assertThat(productPage.getReviewListItems().filter(
                    new com.microsoft.playwright.Locator.FilterOptions().setHasText(reviewTitle)
            ).first()).isVisible();
        } finally {
            DatabaseManager.deleteProductReviewByTitle(reviewTitle);
        }
    }

    @Test(groups = {"ui", "customer", "reviews"})
    @TestCase(value = "TC-CUST-006", requirementId = "REQ-CUST-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify unauthenticated guest is blocked from submitting a product review")
    public void testProductReviewBlockedForUnauthenticatedGuest() {
        navigateTo("/build-your-own-computer");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.openProductReviewsSection();

        PlaywrightAssertions.assertThat(productPage.getReviewValidationError())
                .containsText(Pattern.compile(".*Only registered users can write reviews.*"));
    }
}
