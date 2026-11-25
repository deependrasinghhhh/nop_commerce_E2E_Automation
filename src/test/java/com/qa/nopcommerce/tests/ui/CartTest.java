package com.qa.nopcommerce.tests.ui;

import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.config.ConfigReader;
import com.qa.nopcommerce.database.DatabaseManager;
import com.qa.nopcommerce.pages.AdminDiscountPage;
import com.qa.nopcommerce.pages.AdminLoginPage;
import com.qa.nopcommerce.pages.CartPage;
import com.qa.nopcommerce.pages.ProductDetailsPage;
import com.qa.nopcommerce.pages.WishlistPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigDecimal;
import java.util.UUID;
import java.util.regex.Pattern;

@Epic("Storefront Core")
@Feature("Shopping Cart, Wishlist & Coupon Operations")
public class CartTest extends BaseTest {

    private static final String TEST_PRODUCT_SLUG = "/apple-icam";
    private static final String TEST_PRODUCT_NAME = "Apple iCam";

    @Test(groups = {"ui", "cart", "smoke"})
    @TestCase(value = "TC-CART-001", requirementId = "REQ-CART-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify adding simple product to shopping cart from product details page")
    public void testAddProductToCart() {
        runWithCleanup("add product to cart cleanup", () -> {
            navigateTo(TEST_PRODUCT_SLUG);
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.clickAddToCart();

            Assert.assertTrue(productPage.getNotificationText().contains("The product has been added to your shopping cart"),
                    "Expected add to cart notification banner");
        }, () -> {
            navigateTo("/cart");
            new CartPage(getPage()).clearCart();
        });
    }

    @Test(groups = {"ui", "cart"})
    @TestCase(value = "TC-CART-004", requirementId = "REQ-CART-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify shopping cart table renders added product and updates header cart quantity")
    public void testViewShoppingCart() {
        runWithCleanup("view shopping cart cleanup", () -> {
            navigateTo(TEST_PRODUCT_SLUG);
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.clickAddToCart();
            Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

            navigateTo("/cart");
            CartPage cartPage = new CartPage(getPage());

            Assert.assertTrue(cartPage.hasCartItemCount(1), "Expected 1 item in cart");
            Assert.assertTrue(cartPage.containsProduct(TEST_PRODUCT_NAME),
                    "Expected cart table to contain " + TEST_PRODUCT_NAME);
            Assert.assertTrue(cartPage.isCheckoutButtonVisible(), "Expected checkout button to be visible");
        }, () -> {
            navigateTo("/cart");
            new CartPage(getPage()).clearCart();
        });
    }

    @Test(groups = {"ui", "cart"})
    @TestCase(value = "TC-CART-006", requirementId = "REQ-CART-003")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify updating line item quantity in shopping cart recalculates subtotal")
    public void testUpdateCartItemQuantity() {
        runWithCleanup("update cart quantity cleanup", () -> {
            navigateTo(TEST_PRODUCT_SLUG);
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.clickAddToCart();
            Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

            navigateTo("/cart");
            CartPage cartPage = new CartPage(getPage());
            Assert.assertTrue(cartPage.hasCartItemCount(1), "Expected 1 item in cart");
            BigDecimal initialSubtotal = cartPage.getSubTotalAmount();

            cartPage.updateQuantity(0, 3);
            Assert.assertTrue(cartPage.hasItemQuantity(0, "3"), "Expected quantity to be updated to 3");
            BigDecimal updatedSubtotal = cartPage.getSubTotalAmount();
            Assert.assertEquals(updatedSubtotal.compareTo(initialSubtotal.multiply(BigDecimal.valueOf(3))), 0,
                    "Expected subtotal to triple from " + initialSubtotal + " to " + initialSubtotal.multiply(BigDecimal.valueOf(3))
                            + ", actual: " + updatedSubtotal);
        }, () -> {
            navigateTo("/cart");
            new CartPage(getPage()).clearCart();
        });
    }

    @Test(groups = {"ui", "cart"})
    @TestCase(value = "TC-CART-007", requirementId = "REQ-CART-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify removing item from shopping cart displays empty cart state")
    public void testRemoveItemFromCart() {
        navigateTo(TEST_PRODUCT_SLUG);
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();
        Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());
        Assert.assertTrue(cartPage.hasCartItemCount(1), "Expected 1 item in cart");

        cartPage.removeItem(0);
        Assert.assertTrue(cartPage.getEmptyCartText().contains("Your Shopping Cart is empty!"),
                "Expected empty cart message");
    }

    @Test(groups = {"ui", "cart", "wishlist"})
    @TestCase(value = "TC-CART-009", requirementId = "REQ-CART-005")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify adding a product to wishlist from PDP increments header wishlist counter and lists item")
    public void testAddProductToWishlistAndVerifyCounter() {
        runWithCleanup("wishlist add cleanup", () -> {
            navigateTo("/wishlist");
            new WishlistPage(getPage()).clearWishlist();

            navigateTo(TEST_PRODUCT_SLUG);
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.setQuantity(1);
            productPage.clickAddToWishlist();

            Assert.assertTrue(productPage.getNotificationText().contains("The product has been added to your wishlist"),
                    "Expected add to wishlist notification banner");

            WishlistPage wishlistPage = new WishlistPage(getPage());
            Assert.assertTrue(wishlistPage.hasHeaderWishlistQuantity("(1)"),
                    "Expected wishlist header quantity (1)");

            navigateTo("/wishlist");
            Assert.assertTrue(wishlistPage.hasWishlistItemCount(1), "Expected 1 item in wishlist");
            Assert.assertTrue(wishlistPage.isProductInWishlist(TEST_PRODUCT_NAME),
                    "Expected product link in wishlist");
        }, () -> {
            navigateTo("/wishlist");
            new WishlistPage(getPage()).clearWishlist();
        });
    }

    @Test(groups = {"ui", "cart", "wishlist"})
    @TestCase(value = "TC-CART-011", requirementId = "REQ-CART-006")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify moving an item from wishlist to shopping cart transfers item and empties wishlist")
    public void testMoveItemFromWishlistToShoppingCart() {
        runWithCleanup("move wishlist to cart cleanup", () -> {
            navigateTo(TEST_PRODUCT_SLUG);
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.setQuantity(1);
            productPage.clickAddToWishlist();
            Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

            navigateTo("/wishlist");
            WishlistPage wishlistPage = new WishlistPage(getPage());
            Assert.assertTrue(wishlistPage.hasWishlistItemCount(1), "Expected 1 item in wishlist");

            CartPage cartPage = wishlistPage.moveProductToCart(TEST_PRODUCT_NAME);
            PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*/cart.*"));
            Assert.assertTrue(cartPage.hasCartItemCount(1), "Expected 1 item in cart");
            Assert.assertTrue(cartPage.containsProduct(TEST_PRODUCT_NAME),
                    "Expected cart to contain product " + TEST_PRODUCT_NAME);

            navigateTo("/wishlist");
            Assert.assertTrue(wishlistPage.getEmptyWishlistText().contains("The wishlist is empty!"),
                    "Expected empty wishlist message");
        }, () -> {
            navigateTo("/wishlist");
            new WishlistPage(getPage()).clearWishlist();
        }, () -> {
            navigateTo("/cart");
            new CartPage(getPage()).clearCart();
        });
    }

    @Test(groups = {"ui", "cart", "coupon"})
    @TestCase(value = "TC-CART-012", requirementId = "REQ-CART-008")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify provisioning an isolated discount coupon and applying it to the cart reduces the order total")
    public void testApplyValidCouponCode() {
        String isolatedCouponCode = "QAE2E_" + UUID.randomUUID().toString().substring(0, 6).toUpperCase();
        String discountName = "QA_Automated_Discount_" + isolatedCouponCode;

        runWithCleanup("isolated coupon test " + isolatedCouponCode, () -> {
            // 1. Provision an isolated coupon code in Admin -> Promotions -> Discounts
            navigateTo(ConfigReader.getAdminUrl());
            AdminLoginPage adminLogin = new AdminLoginPage(getPage());
            adminLogin.login(ConfigReader.getAdminEmail(), ConfigReader.getAdminPassword());

            navigateTo("/Admin/Discount/Create");
            AdminDiscountPage discountPage = new AdminDiscountPage(getPage());
            discountPage.createPercentageDiscount(discountName, 10, isolatedCouponCode);
            Assert.assertTrue(discountPage.isDiscountCreatedSuccessfully(),
                    "Expected new discount added successfully alert");

            // 2. Drop Admin session cookies before storefront cart flow so shared Admin state is untouched
            getPage().context().clearCookies();

            // 3. Add isolated test item to anonymous storefront cart and apply the valid coupon code
            navigateTo(TEST_PRODUCT_SLUG);
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.clickAddToCart();
            Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

            navigateTo("/cart");
            CartPage cartPage = new CartPage(getPage());
            Assert.assertTrue(cartPage.hasCartItemCount(1), "Expected 1 item in cart");
            BigDecimal totalBeforeCoupon = cartPage.getOrderTotalAmount();

            cartPage.applyCouponCode(isolatedCouponCode);

            Assert.assertTrue(cartPage.isCouponMessageDisplayed(), "Expected coupon status banner to appear");
            Assert.assertTrue(cartPage.getCouponMessageText().contains("The coupon code was applied"),
                    "Expected coupon applied message");
            Assert.assertTrue(cartPage.isDiscountApplied(), "Expected discount row summary to be visible");
            BigDecimal totalAfterCoupon = cartPage.getOrderTotalAmount();
            Assert.assertTrue(totalAfterCoupon.compareTo(totalBeforeCoupon) < 0,
                    "Expected coupon to reduce total from " + totalBeforeCoupon + " to " + totalAfterCoupon);
        }, () -> {
            navigateTo("/cart");
            CartPage cleanupCart = new CartPage(getPage());
            cleanupCart.removeAppliedCouponIfPresent();
            cleanupCart.clearCart();
        }, () -> DatabaseManager.deleteDiscountCouponByCode(isolatedCouponCode));
    }

    @Test(groups = {"ui", "cart", "coupon"})
    @TestCase(value = "TC-CART-015", requirementId = "REQ-CART-008")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify applying an invalid coupon code displays a failure message in the cart")
    public void testApplyInvalidCouponCode() {
        runWithCleanup("invalid coupon cleanup", () -> {
            navigateTo(TEST_PRODUCT_SLUG);
            ProductDetailsPage productPage = new ProductDetailsPage(getPage());
            productPage.clickAddToCart();
            Assert.assertTrue(productPage.isNotificationBannerVisible(), "Expected notification banner");

            navigateTo("/cart");
            CartPage cartPage = new CartPage(getPage());
            Assert.assertTrue(cartPage.hasCartItemCount(1), "Expected 1 item in cart");

            cartPage.applyCouponCode("INVALID_DISCOUNT_CODE_999");
            Assert.assertTrue(cartPage.isCouponMessageDisplayed(), "Expected coupon error message to be displayed");
        }, () -> {
            navigateTo("/cart");
            new CartPage(getPage()).clearCart();
        });
    }
}
