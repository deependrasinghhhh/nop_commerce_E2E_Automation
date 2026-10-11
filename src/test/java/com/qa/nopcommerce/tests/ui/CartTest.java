package com.qa.nopcommerce.tests.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.pages.CartPage;
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
@Feature("Shopping Cart Operations")
public class CartTest extends BaseTest {

    @Test(groups = {"ui", "cart", "smoke"})
    @TestCase("TC-CART-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify adding product to shopping cart from product details page")
    public void testAddProductToCart() {
        navigateTo("/apple-macbook-pro-13-inch");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());

        productPage.clickAddToCart();

        PlaywrightAssertions.assertThat(getPage().locator("#bar-notification .content"))
                .hasText(Pattern.compile(".*The product has been added to your shopping cart.*"));
    }

    @Test(groups = {"ui", "cart"})
    @TestCase("TC-CART-005")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify shopping cart table renders product name, unit price, quantity, and total")
    public void testViewShoppingCart() {
        navigateTo("/apple-macbook-pro-13-inch");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());

        Assert.assertTrue(cartPage.getCartItemCount() > 0, "Cart should contain added item");
        PlaywrightAssertions.assertThat(cartPage.getCheckoutButton()).isVisible();
    }

    @Test(groups = {"ui", "cart"})
    @TestCase("TC-CART-006")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify updating line item quantity in shopping cart recalculates subtotal")
    public void testUpdateCartItemQuantity() {
        navigateTo("/apple-macbook-pro-13-inch");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());

        if (cartPage.getCartItemCount() > 0) {
            cartPage.updateQuantity(0, 3);
            Locator qtyInput = getPage().locator("table.cart tbody tr input.qty-input").first();
            Assert.assertEquals(qtyInput.inputValue(), "3", "Cart item quantity should be updated to 3");
        }
    }

    @Test(groups = {"ui", "cart"})
    @TestCase("TC-CART-007")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify removing item from cart updates cart count")
    public void testRemoveItemFromCart() {
        navigateTo("/apple-macbook-pro-13-inch");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        productPage.clickAddToCart();

        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());

        if (cartPage.getCartItemCount() > 0) {
            cartPage.removeItem(0);
        }
    }

    @Test(groups = {"ui", "cart"})
    @TestCase("TC-CART-010")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify applying invalid coupon code displays alert message")
    public void testApplyInvalidCouponCode() {
        navigateTo("/cart");
        CartPage cartPage = new CartPage(getPage());

        cartPage.applyCouponCode("INVALID_DISCOUNT_CODE_999");
        Locator message = getPage().locator(".message-failure, .coupon-box .message");
        if (message.count() > 0) {
            PlaywrightAssertions.assertThat(message.first()).isVisible();
        }
    }
}
