package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.util.List;

/**
 * Page Object representing nopCommerce Shopping Cart page (/cart).
 */
public class CartPage extends BasePage {

    private final Locator pageTitle;
    private final Locator cartTable;
    private final Locator cartRows;
    private final Locator updateCartButton;
    private final Locator discountCodeInput;
    private final Locator applyDiscountButton;
    private final Locator couponMessage;
    private final Locator removeDiscountButton;
    private final Locator discountRowSummary;
    private final Locator subTotalSummary;
    private final Locator termsOfServiceCheckbox;
    private final Locator checkoutButton;
    private final Locator orderTotal;
    private final Locator emptyCartMessage;

    public CartPage(Page page) {
        super(page);
        this.pageTitle = page.locator(".page-title h1");
        this.cartTable = page.locator("table.cart");
        this.cartRows = page.locator("table.cart tbody tr");
        this.updateCartButton = page.locator("#updatecart");
        this.discountCodeInput = page.locator("#discountcouponcode");
        this.applyDiscountButton = page.locator("#applydiscountcouponcode");
        this.couponMessage = page.locator(".coupon-box .message-success, .coupon-box .message-failure, .coupon-box .message, .message-error");
        this.removeDiscountButton = page.locator("button.remove-discount-button, button[name*='removediscount']");
        this.discountRowSummary = page.locator(".order-total-discount .value-summary, .cart-total .discount-total");
        this.subTotalSummary = page.locator(".order-subtotal .value-summary");
        this.termsOfServiceCheckbox = page.locator("#termsofservice");
        this.checkoutButton = page.locator("#checkout");
        this.orderTotal = page.locator(".order-total .value-summary");
        this.emptyCartMessage = page.locator(".no-data");
    }

    public Locator getCartRows() {
        return cartRows;
    }

    public int getCartItemCount() {
        return cartRows.count();
    }

    public List<String> getCartProductNames() {
        return page.locator("table.cart .product-name").allInnerTexts();
    }

    public void updateQuantity(int rowIndex, int newQty) {
        Locator qtyInput = cartRows.nth(rowIndex).locator("input.qty-input");
        fill(qtyInput, String.valueOf(newQty));
        qtyInput.press("Enter");
        try {
            if (updateCartButton.isVisible(new Locator.IsVisibleOptions().setTimeout(1000))) {
                click(updateCartButton);
            }
        } catch (Exception ignored) {}
        waitForLoad();
    }

    public void removeItem(int rowIndex) {
        Locator removeBtn = cartRows.nth(rowIndex).locator("button.remove-btn");
        click(removeBtn);
        waitForLoad();
    }

    public void clearCart() {
        while (cartRows.count() > 0) {
            Locator removeBtn = cartRows.first().locator("button.remove-btn");
            if (isVisible(removeBtn)) {
                click(removeBtn);
                waitForLoad();
            } else {
                break;
            }
        }
    }

    public void applyCouponCode(String couponCode) {
        fill(discountCodeInput, couponCode);
        click(applyDiscountButton);
    }

    public void removeAppliedCouponIfPresent() {
        if (isVisible(removeDiscountButton)) {
            click(removeDiscountButton);
            waitForLoad();
        }
    }

    public Locator getCouponMessage() {
        return couponMessage;
    }

    public Locator getDiscountRowSummary() {
        return discountRowSummary;
    }

    public Locator getSubTotalSummary() {
        return subTotalSummary;
    }

    public Locator getEmptyCartMessage() {
        return emptyCartMessage;
    }

    public void acceptTermsOfService() {
        try {
            if (termsOfServiceCheckbox.isVisible(new Locator.IsVisibleOptions().setTimeout(2000))) {
                termsOfServiceCheckbox.check();
            }
        } catch (Exception ignored) {}
    }

    public CheckoutPage proceedToCheckout() {
        acceptTermsOfService();
        click(checkoutButton);
        return new CheckoutPage(page);
    }

    public String getOrderTotal() {
        return getText(orderTotal);
    }

    public Locator getOrderTotalLocator() {
        return orderTotal;
    }

    public boolean isCartEmpty() {
        return isVisible(emptyCartMessage) && getText(emptyCartMessage).contains("empty");
    }

    public Locator getCheckoutButton() {
        return checkoutButton;
    }
}
