package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.math.BigDecimal;
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

    public int getCartItemCount() {
        return getCount(cartRows);
    }

    public boolean hasCartItemCount(int expectedCount) {
        return waitForCount(cartRows, expectedCount);
    }

    public boolean isCheckoutButtonVisible() {
        return waitForVisible(checkoutButton);
    }

    public String getItemQuantity(int rowIndex) {
        return cartRows.nth(rowIndex).locator("input.qty-input").inputValue();
    }

    public boolean hasItemQuantity(int rowIndex, String expectedQuantity) {
        return waitForCondition(() -> cartRows.count() > rowIndex
                && expectedQuantity.equals(cartRows.nth(rowIndex)
                        .locator("input.qty-input")
                        .inputValue(new Locator.InputValueOptions().setTimeout(1000))));
    }

    public List<String> getCartProductNames() {
        return page.locator("table.cart .product-name").allInnerTexts();
    }

    public boolean containsProduct(String productName) {
        return waitForCondition(() -> getCartProductNames().stream().anyMatch(name -> name.contains(productName)));
    }

    public void updateQuantity(int rowIndex, int newQty) {
        Locator qtyInput = cartRows.nth(rowIndex).locator("input.qty-input");
        fill(qtyInput, String.valueOf(newQty));
        qtyInput.press("Enter");
        if (updateCartButton.count() > 0 && updateCartButton.isVisible()) {
            click(updateCartButton);
        }
        waitForLoad();
    }

    public void removeItem(int rowIndex) {
        int before = getCount(cartRows);
        Locator removeBtn = cartRows.nth(rowIndex).locator("button.remove-btn");
        click(removeBtn);
        if (before > 0) {
            waitForCountLessThan(cartRows, before);
        }
        waitForLoad();
    }

    public void clearCart() {
        waitForLoad();
        while (true) {
            int before = cartRows.count();
            if (before == 0) {
                return;
            }
            Locator removeBtn = cartRows.first().locator("button.remove-btn");
            if (!removeBtn.isVisible()) {
                throw new IllegalStateException(String.format(
                        "Unable to clear cart: remove button is not visible for the first of %d cart row(s)", before));
            }
            click(removeBtn);
            if (!waitForCountLessThan(cartRows, before)) {
                throw new IllegalStateException(String.format(
                        "Unable to clear cart: removing the first item did not reduce cart row count from %d", before));
            }
            waitForLoad();
            int after = cartRows.count();
            if (after >= before) {
                throw new IllegalStateException(String.format(
                        "Unable to clear cart: cart row count did not decrease after removal (before=%d, after=%d)",
                        before, after));
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

    public String getCouponMessageText() {
        return getOptionalText(couponMessage);
    }

    public boolean isCouponMessageDisplayed() {
        return waitForVisible(couponMessage);
    }

    public boolean isDiscountApplied() {
        return waitForVisible(discountRowSummary);
    }

    public String getEmptyCartText() {
        return getText(emptyCartMessage);
    }

    public void acceptTermsOfService() {
        if (!waitForVisible(checkoutButton)) {
            throw new IllegalStateException("Checkout button did not become visible on the cart page");
        }
        int checkboxCount = termsOfServiceCheckbox.count();
        if (checkboxCount == 0) {
            return;
        }
        try {
            termsOfServiceCheckbox.first().check();
        } catch (com.microsoft.playwright.PlaywrightException e) {
            throw new IllegalStateException("Unable to accept the Terms of Service checkbox on the cart page", e);
        }
    }

    public CheckoutPage proceedToCheckout() {
        acceptTermsOfService();
        click(checkoutButton);
        return new CheckoutPage(page);
    }

    public String getOrderTotal() {
        return getText(orderTotal);
    }

    public BigDecimal getOrderTotalAmount() {
        String normalized = getOrderTotal().replaceAll("[^0-9,.-]", "").replace(",", "");
        if (normalized.isBlank()) {
            throw new IllegalStateException("Cart order total was empty or not numeric");
        }
        return new BigDecimal(normalized);
    }

    public boolean isCartEmpty() {
        return waitForVisible(emptyCartMessage) && getText(emptyCartMessage).contains("empty");
    }
}
