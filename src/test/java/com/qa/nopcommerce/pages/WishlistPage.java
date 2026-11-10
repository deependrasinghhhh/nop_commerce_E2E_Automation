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

    public Locator getWishlistRows() {
        return wishlistRows;
    }

    public Locator getHeaderWishlistQty() {
        return headerWishlistQty;
    }

    public Locator getEmptyWishlistMessage() {
        return emptyWishlistMessage;
    }

    public Locator getProductLinkByName(String productName) {
        return wishlistTable.locator(String.format("a.product-name:has-text('%s')", productName));
    }

    public CartPage moveProductToCart(String productName) {
        Locator row = page.locator("table.cart tbody tr").filter(new Locator.FilterOptions().setHasText(productName)).first();
        Locator addToCartCheckbox = row.locator("input[name='addtocart']");
        addToCartCheckbox.check();
        click(addToCartButton);
        return new CartPage(page);
    }

    public void removeProductFromWishlist(String productName) {
        Locator row = page.locator("table.cart tbody tr").filter(new Locator.FilterOptions().setHasText(productName)).first();
        Locator removeBtn = row.locator("button.remove-btn");
        if (isVisible(removeBtn)) {
            click(removeBtn);
        }
    }

    public void clearWishlist() {
        while (wishlistRows.count() > 0) {
            Locator removeBtn = wishlistRows.first().locator("button.remove-btn");
            if (isVisible(removeBtn)) {
                click(removeBtn);
                waitForLoad();
            } else {
                break;
            }
        }
    }
}
