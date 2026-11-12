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

    private static final String SIMPLE_PRODUCT_SLUG = "/apple-macbook-pro";

    @Test(groups = {"ui", "checkout", "smoke"})
    @TestCase(value = "TC-CHK-001", requirementId = "REQ-CHK-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify checkout initialization from cart enforces Terms of Service and redirects to checkout/login")
    public void testCheckoutInitiation() {
        navigateTo(SIMPLE_PRODUCT_SLUG);
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();
        Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());
        cartPage.acceptTermsOfService();
        cartPage.proceedToCheckout();

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*(checkout|login).*"));
    }

    @Test(groups = {"ui", "checkout"})
    @TestCase(value = "TC-CHK-002", requirementId = "REQ-CHK-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify guest checkout option opens the billing address form")
    public void testCheckoutBillingAddressForm() {
        navigateTo(SIMPLE_PRODUCT_SLUG);
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();
        Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());
        CheckoutPage checkoutPage = cartPage.proceedToCheckout();

        checkoutPage.clickCheckoutAsGuest();

        Assert.assertTrue(checkoutPage.isBillingStepVisible(),
                "Expected billing address form or container to be visible");
    }

    @Test(groups = {"ui", "checkout"})
    @TestCase(value = "TC-CHK-008", requirementId = "REQ-CHK-005")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify payment method step (Check / Money Order) is reachable after billing and shipping steps")
    public void testCheckoutPaymentMethodOptions() {
        String email = "qa_checkout_" + java.util.UUID.randomUUID().toString().replace("-", "") + "@example.com";
        try {
            navigateTo(SIMPLE_PRODUCT_SLUG);
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.clickAddToCart();
            Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

            navigateTo("/cart");
            CartPage cartPage = new CartPage(getPage());
            CheckoutPage checkoutPage = cartPage.proceedToCheckout();
            checkoutPage.clickCheckoutAsGuest();
            Assert.assertTrue(checkoutPage.isBillingStepVisible(), "Expected guest billing form to be visible");

            checkoutPage.fillBillingAddress("QA", "Checkout", email, "United States of America", "Texas",
                    "Austin", "101 QA Automation Way", "78701", "5125550199");
            checkoutPage.continueToPaymentMethod();

            Assert.assertTrue(checkoutPage.isPaymentMethodOptionVisible(),
                    "Expected Check / Money Order payment option to be visible after billing and shipping");
            Assert.assertTrue(checkoutPage.getPaymentMethodOptionCount() >= 1,
                    "Expected at least one enabled payment method option");
        } finally {
            navigateTo("/cart");
            new CartPage(getPage()).clearCart();
        }
    }
}
