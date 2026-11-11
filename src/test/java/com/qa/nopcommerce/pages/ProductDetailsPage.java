package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Product Details and Product Reviews pages.
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
    private final Locator wishlistLink;

    // Product Review locators
    private final Locator addReviewLink;
    private final Locator reviewTitleInput;
    private final Locator reviewTextInput;
    private final Locator rating5Radio;
    private final Locator submitReviewButton;
    private final Locator reviewResultNotification;
    private final Locator reviewListItems;
    private final Locator reviewValidationError;

    public ProductDetailsPage(Page page) {
        super(page);
        this.productName = page.locator(".product-name h1");
        this.productPrice = page.locator(".product-price span, [id^='price-value-']").first();
        this.quantityInput = page.locator("input.qty-input, [id^='product_enteredQuantity_']").first();
        this.addToCartButton = page.locator("button.add-to-cart-button, [id^='add-to-cart-button-']").first();
        this.addToWishlistButton = page.locator("button.add-to-wishlist-button, [id^='add-to-wishlist-button-']").first();
        this.notificationBanner = page.locator("#bar-notification .content").first();
        this.notificationClose = page.locator("#bar-notification .close").first();
        this.cartLink = page.locator("#topcartlink a");
        this.wishlistLink = page.locator("a.ico-wishlist");

        this.addReviewLink = page.locator("a[href*='productreviews'], a[href='#productreviews'], .product-review-links a").first();
        this.reviewTitleInput = page.locator("#AddProductReview_Title");
        this.reviewTextInput = page.locator("#AddProductReview_ReviewText");
        this.rating5Radio = page.locator("#addproductrating_5");
        this.submitReviewButton = page.locator("button[name='add-review'], button.write-product-review-button");
        this.reviewResultNotification = page.locator("div.result, #bar-notification .content");
        this.reviewListItems = page.locator(".product-review-item");
        this.reviewValidationError = page.locator(".review-already-added, .message-error, .validation-summary-errors");
    }

    public String getProductName() {
        return getText(productName);
    }

    public String getProductPrice() {
        return getText(productPrice);
    }

    public Locator getProductNameHeading() {
        return productName;
    }

    public Locator getProductPriceLocator() {
        return productPrice;
    }

    public void setQuantity(int qty) {
        fill(quantityInput, String.valueOf(qty));
    }

    public void clickAddToCart() {
        try {
            page.waitForFunction("() => typeof window.AjaxCart !== 'undefined' && typeof window.jQuery !== 'undefined'", null, new Page.WaitForFunctionOptions().setTimeout(10000));
        } catch (Exception ignored) {}
        click(addToCartButton);
    }

    public void addToCart() {
        clickAddToCart();
    }

    public void clickAddToWishlist() {
        try {
            page.waitForFunction("() => typeof window.AjaxCart !== 'undefined' && typeof window.jQuery !== 'undefined'", null, new Page.WaitForFunctionOptions().setTimeout(10000));
        } catch (Exception ignored) {}
        click(addToWishlistButton);
    }

    public boolean isNotificationBannerVisible() {
        try {
            notificationBanner.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            return notificationBanner.isVisible();
        } catch (Exception e) {
            return false;
        }
    }

    public Locator getNotificationBanner() {
        return notificationBanner;
    }

    public String getNotificationText() {
        return getText(notificationBanner);
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

    public WishlistPage goToWishlist() {
        click(wishlistLink);
        return new WishlistPage(page);
    }

    public void openProductReviewsSection() {
        try {
            addReviewLink.waitFor(new Locator.WaitForOptions().setTimeout(5000));
            click(addReviewLink);
        } catch (Exception e) {
            log.debug("Add review link not immediately visible: {}", e.getMessage());
            if (isVisible(addReviewLink)) {
                click(addReviewLink);
            }
        }
    }

    public void submitProductReview(String title, String reviewBody) {
        fill(reviewTitleInput, title);
        fill(reviewTextInput, reviewBody);
        if (isVisible(rating5Radio)) {
            rating5Radio.check();
        }
        click(submitReviewButton);
    }

    public Locator getReviewResultNotification() {
        return reviewResultNotification;
    }

    public Locator getReviewListItems() {
        return reviewListItems;
    }

    public Locator getReviewValidationError() {
        return reviewValidationError;
    }

    public Locator getSubmitReviewButton() {
        return submitReviewButton;
    }
}
