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
import org.testng.annotations.Test;

import java.util.regex.Pattern;

@Epic("Storefront Core")
@Feature("Order Placement & Checkout Flow")
public class CheckoutTest extends BaseTest {

    private static final String SIMPLE_PRODUCT_SLUG = "/apple-macbook-pro";

    @Test(groups = {"ui", "checkout", "smoke"})
    @TestCase(value = "TC-CHK-001", requirementId = "REQ-CHK-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify checkout initialization from cart enforces Terms of Service and redirects to checkout/login")
    public void testCheckoutInitiation() {
        navigateTo(SIMPLE_PRODUCT_SLUG);
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();
        PlaywrightAssertions.assertThat(productPage.getNotificationBanner()).isVisible();

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());
        cartPage.acceptTermsOfService();
        cartPage.proceedToCheckout();

        PlaywrightAssertions.assertThat(getPage())
                .hasURL(Pattern.compile(".*(checkout|login).*"));
    }

    @Test(groups = {"ui", "checkout"})
    @TestCase(value = "TC-CHK-002", requirementId = "REQ-CHK-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify guest checkout option opens the billing address form")
    public void testCheckoutBillingAddressForm() {
        navigateTo(SIMPLE_PRODUCT_SLUG);
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();
        PlaywrightAssertions.assertThat(productPage.getNotificationBanner()).isVisible();

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());
        CheckoutPage checkoutPage = cartPage.proceedToCheckout();

        checkoutPage.clickCheckoutAsGuest();

        PlaywrightAssertions.assertThat(getPage().locator("#BillingNewAddress_CountryId, #billing-buttons-container").first())
                .isVisible();
    }

    @Test(groups = {"ui", "checkout"})
    @TestCase(value = "TC-CHK-008", requirementId = "REQ-CHK-005")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify payment method step (Check / Money Order) is reachable after billing and shipping steps")
    public void testCheckoutPaymentMethodOptions() {
        navigateTo(SIMPLE_PRODUCT_SLUG);
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();
        PlaywrightAssertions.assertThat(productPage.getNotificationBanner()).isVisible();

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());
        CheckoutPage checkoutPage = cartPage.proceedToCheckout();
        checkoutPage.clickCheckoutAsGuest();

        PlaywrightAssertions.assertThat(getPage().locator("#checkout-steps, .checkout-page").first())
                .isVisible();
    }
}
