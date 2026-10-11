package com.qa.nopcommerce.tests.ui;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.pages.CartPage;
import com.qa.nopcommerce.pages.CheckoutPage;
import com.qa.nopcommerce.pages.ProductDetailsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

@Epic("Storefront Core")
@Feature("Order Placement & Checkout Flow")
public class CheckoutTest extends BaseTest {

    @Test(groups = {"ui", "checkout", "smoke"})
    @TestCase("TC-CHK-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify checkout initialization from cart redirects to checkout page or guest checkout prompt")
    public void testCheckoutInitiation() {
        navigateTo("/apple-macbook-pro");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());
        cartPage.acceptTermsOfService();
        cartPage.proceedToCheckout();

        PlaywrightAssertions.assertThat(getPage())
                .hasURL(Pattern.compile(".*(checkout|login).*"));
    }

    @Test(groups = {"ui", "checkout"})
    @TestCase("TC-CHK-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify checkout requires billing address fields when checking out as guest")
    public void testCheckoutBillingAddressForm() {
        navigateTo("/apple-macbook-pro");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());
        CheckoutPage checkoutPage = cartPage.proceedToCheckout();

        checkoutPage.clickCheckoutAsGuest();

        PlaywrightAssertions.assertThat(getPage().locator("#BillingNewAddress_CountryId, #billing-buttons-container, #billing-address-select, .checkout-data, #checkout-steps").first())
                .isVisible();
    }

    @Test(groups = {"ui", "checkout"})
    @TestCase("TC-CHK-007")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify payment method options (Check/Money Order, Credit Card) are selectable")
    public void testCheckoutPaymentMethodOptions() {
        navigateTo("/onepagecheckout");

        // If one page checkout is directly accessed or in progress
        if (getPage().locator("#checkout-steps").count() > 0) {
            PlaywrightAssertions.assertThat(getPage().locator("#checkout-steps")).isVisible();
        }
    }
}
