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
        this.termsOfServiceCheckbox = page.locator("#termsofservice");
        this.checkoutButton = page.locator("#checkout");
        this.orderTotal = page.locator(".order-total .value-summary");
        this.emptyCartMessage = page.locator(".no-data");
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
        click(updateCartButton);
        waitForLoad();
    }

    public void removeItem(int rowIndex) {
        Locator removeBtn = cartRows.nth(rowIndex).locator("button.remove-btn");
        click(removeBtn);
        waitForLoad();
    }

    public void applyCouponCode(String couponCode) {
        fill(discountCodeInput, couponCode);
        click(applyDiscountButton);
    }

    public void acceptTermsOfService() {
        termsOfServiceCheckbox.check();
    }

    public CheckoutPage proceedToCheckout() {
        acceptTermsOfService();
        click(checkoutButton);
        return new CheckoutPage(page);
    }

    public String getOrderTotal() {
        return getText(orderTotal);
    }

    public boolean isCartEmpty() {
        return isVisible(emptyCartMessage) && getText(emptyCartMessage).contains("empty");
    }

    public Locator getCheckoutButton() {
        return checkoutButton;
    }
}
