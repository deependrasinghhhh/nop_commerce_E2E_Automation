package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Product Details page.
 */
public class ProductDetailsPage extends BasePage {

    private final Locator productName;
    private final Locator productPrice;
    private final Locator quantityInput;
    private final Locator addToCartButton;
    private final Locator addToWishlistButton;
    private final Locator notificationBanner;
    private final Locator notificationClose;
    private final Locator cartLink;

    public ProductDetailsPage(Page page) {
        super(page);
        this.productName = page.locator(".product-name h1");
        this.productPrice = page.locator(".product-price span, [id^='price-value-']").first();
        this.quantityInput = page.locator("input.qty-input, [id^='product_enteredQuantity_']").first();
        this.addToCartButton = page.locator("button.add-to-cart-button, [id^='add-to-cart-button-']").first();
        this.addToWishlistButton = page.locator("button.add-to-wishlist-button, [id^='add-to-wishlist-button-']").first();
        this.notificationBanner = page.locator("#bar-notification .content");
        this.notificationClose = page.locator("#bar-notification .close");
        this.cartLink = page.locator("#topcartlink a");
    }

    public String getProductName() {
        return getText(productName);
    }

    public String getProductPrice() {
        return getText(productPrice);
    }

    public void setQuantity(int qty) {
        fill(quantityInput, String.valueOf(qty));
    }

    public void clickAddToCart() {
        click(addToCartButton);
        try {
            notificationBanner.waitFor(new Locator.WaitForOptions().setTimeout(5000));
        } catch (Exception ignored) {}
    }

    public void clickAddToWishlist() {
        click(addToWishlistButton);
    }

    public String getNotificationText() {
        return getText(notificationBanner);
    }

    public boolean isNotificationDisplayed() {
        return isVisible(notificationBanner);
    }

    public void closeNotification() {
        if (isVisible(notificationClose)) {
            click(notificationClose);
        }
    }

    public CartPage goToCart() {
        click(cartLink);
        return new CartPage(page);
    }
}
