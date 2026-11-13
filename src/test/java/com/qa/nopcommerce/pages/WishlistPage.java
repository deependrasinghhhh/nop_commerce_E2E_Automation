package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing the nopCommerce Wishlist page (/wishlist).
 */
public class WishlistPage extends BasePage {

    private final Locator wishlistTable;
    private final Locator wishlistRows;
    private final Locator moveToCartCheckboxes;
    private final Locator addToCartButton;
    private final Locator updateWishlistButton;
    private final Locator emptyWishlistMessage;
    private final Locator headerWishlistQty;

    public WishlistPage(Page page) {
        super(page);
        this.wishlistTable = page.locator("table.cart");
        this.wishlistRows = page.locator("table.cart tbody tr");
        this.moveToCartCheckboxes = page.locator("table.cart tbody tr input[name='addtocart']");
        this.addToCartButton = page.locator("button.wishlist-add-to-cart-button, button[name='addtocartbutton']");
        this.updateWishlistButton = page.locator("#updatecart, button.update-wishlist-button");
        this.emptyWishlistMessage = page.locator(".no-data");
        this.headerWishlistQty = page.locator("span.wishlist-qty");
    }

    public int getWishlistItemCount() {
        return getCount(wishlistRows);
    }

    public boolean hasWishlistItemCount(int expectedCount) {
        return waitForCount(wishlistRows, expectedCount);
    }

    public String getHeaderWishlistQuantityText() {
        return getText(headerWishlistQty);
    }

    public boolean hasHeaderWishlistQuantity(String expectedText) {
        return waitForTextContains(headerWishlistQty, expectedText);
    }

    public String getEmptyWishlistText() {
        return getText(emptyWishlistMessage);
    }

    public boolean isProductInWishlist(String productName) {
        return waitForVisible(wishlistTable.locator(String.format("a.product-name:has-text('%s')", productName)));
    }

    public CartPage moveProductToCart(String productName) {
        Locator row = page.locator("table.cart tbody tr").filter(new Locator.FilterOptions().setHasText(productName)).first();
        Locator addToCartCheckbox = row.locator("input[name='addtocart']");
        addToCartCheckbox.check();
        click(addToCartButton);
        waitForLoad();
        return new CartPage(page);
    }

    public void removeProductFromWishlist(String productName) {
        int before = getCount(wishlistRows);
        Locator row = page.locator("table.cart tbody tr").filter(new Locator.FilterOptions().setHasText(productName)).first();
        Locator removeBtn = row.locator("button.remove-btn");
        if (isVisible(removeBtn)) {
            click(removeBtn);
            if (before > 0) {
                waitForCountLessThan(wishlistRows, before);
            }
            waitForLoad();
        }
    }

    public void clearWishlist() {
        waitForLoad();
        while (getCount(wishlistRows) > 0) {
            int before = getCount(wishlistRows);
            Locator removeBtn = wishlistRows.first().locator("button.remove-btn");
            if (waitForVisible(removeBtn, 5000)) {
                click(removeBtn);
                waitForCountLessThan(wishlistRows, before);
                waitForLoad();
            } else {
                break;
            }
        }
    }
}
