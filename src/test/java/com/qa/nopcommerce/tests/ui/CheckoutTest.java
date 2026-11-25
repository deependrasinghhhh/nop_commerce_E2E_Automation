package com.qa.nopcommerce.tests.ui;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.database.DatabaseManager;
import com.qa.nopcommerce.pages.CartPage;
import com.qa.nopcommerce.pages.CheckoutPage;
import com.qa.nopcommerce.pages.OrderSuccessPage;
import com.qa.nopcommerce.pages.ProductDetailsPage;
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
@Feature("Order Placement & Checkout Flow")
public class CheckoutTest extends BaseTest {

    private static final String SIMPLE_PRODUCT_SLUG = "/apple-macbook-pro";

    @Test(groups = {"ui", "checkout", "smoke"})
    @TestCase(value = "TC-CHK-001", requirementId = "REQ-CHK-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify checkout initialization from cart enforces Terms of Service before redirecting to checkout/login")
    public void testCheckoutInitiation() {
        runWithCleanup("checkout Terms of Service enforcement", () -> {
            navigateTo(SIMPLE_PRODUCT_SLUG);
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.clickAddToCart();
            Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

            navigateTo("/cart");
            CartPage cartPage = new CartPage(getPage());
            cartPage.clickCheckoutWithoutTerms();
            Assert.assertTrue(cartPage.isTermsWarningVisible(),
                    "Expected Terms of Service warning dialog when clicking Checkout unchecked");
            PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*/cart.*"));
            cartPage.closeTermsWarningDialog();

            cartPage.proceedToCheckout();
            PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*(checkout|login).*"));
        }, () -> {
            navigateTo("/cart");
            new CartPage(getPage()).clearCart();
        });
    }

    @Test(groups = {"ui", "checkout"})
    @TestCase(value = "TC-CHK-002", requirementId = "REQ-CHK-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify guest checkout option opens the billing address form")
    public void testCheckoutBillingAddressForm() {
        runWithCleanup("guest checkout billing form", () -> {
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
        }, () -> {
            navigateTo("/cart");
            new CartPage(getPage()).clearCart();
        });
    }

    @Test(groups = {"ui", "checkout"})
    @TestCase(value = "TC-CHK-008", requirementId = "REQ-CHK-005")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify payment method step (Check / Money Order) is reachable after billing and shipping steps")
    public void testCheckoutPaymentMethodOptions() {
        String email = "qa_checkout_" + UUID.randomUUID().toString().replace("-", "") + "@example.com";
        runWithCleanup("guest checkout payment options " + email, () -> {
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
        }, () -> {
            navigateTo("/cart");
            new CartPage(getPage()).clearCart();
        }, () -> DatabaseManager.deleteIsolatedCustomerByEmail(email));
    }

    @Test(groups = {"ui", "checkout"})
    @TestCase(value = "TC-CHK-010", requirementId = "REQ-CHK-006")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify end-to-end guest order confirmation completes placement, persists in SQL Server, and clears cart")
    public void testGuestOrderConfirmationAndCompletion() {
        String email = "qa_checkout_" + UUID.randomUUID().toString().replace("-", "") + "@example.com";
        runWithCleanup("guest order placement " + email, () -> {
            navigateTo(SIMPLE_PRODUCT_SLUG);
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.clickAddToCart();
            Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

            navigateTo("/cart");
            CartPage cartPage = new CartPage(getPage());
            CheckoutPage checkoutPage = cartPage.proceedToCheckout();
            checkoutPage.clickCheckoutAsGuest();
            Assert.assertTrue(checkoutPage.isBillingStepVisible(), "Expected guest billing form to be visible");

            checkoutPage.fillBillingAddress("QA", "OrderE2E", email, "United States of America", "Texas",
                    "Austin", "202 E2E Checkout Blvd", "78701", "5125550188");
            checkoutPage.continueToPaymentMethod();
            checkoutPage.selectPaymentMethodAndContinue();
            checkoutPage.continuePaymentInfo();

            OrderSuccessPage successPage = checkoutPage.confirmOrder();
            Assert.assertTrue(successPage.isOrderSuccessful(),
                    "Expected 'Your order has been successfully processed!' confirmation message");
            Assert.assertTrue(successPage.isOrderDetailsLinkVisible(),
                    "Expected order details link on completed checkout page");

            int orderId = successPage.getOrderId();
            Assert.assertTrue(orderId > 0, "Expected positive generated Order ID, got: " + orderId);
            if (DatabaseManager.isDatabaseAccessible()) {
                Assert.assertTrue(DatabaseManager.orderExistsForCustomerEmail(orderId, email),
                        "Expected persisted Order row #" + orderId + " in SQL Server for guest " + email);
            }

            successPage.clickContinue();
            navigateTo("/cart");
            Assert.assertTrue(new CartPage(getPage()).isCartEmpty(),
                    "Expected shopping cart to be empty after successful order placement");
        }, () -> {
            navigateTo("/cart");
            new CartPage(getPage()).clearCart();
        }, () -> DatabaseManager.deleteIsolatedCustomerByEmail(email));
    }
}
