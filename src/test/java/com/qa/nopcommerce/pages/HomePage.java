package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Storefront Homepage.
 */
public class HomePage extends BasePage {

    // Locators
    private final Locator logo;
    private final Locator searchInput;
    private final Locator searchButton;
    private final Locator registerLink;
    private final Locator loginLink;
    private final Locator wishlistLink;
    private final Locator cartLink;
    private final Locator customerInfoLink;
    private final Locator logoutLink;
    private final Locator currencyDropdown;
    private final Locator topMenu;
    private final Locator featuredProductsGrid;

    public HomePage(Page page) {
        super(page);
        this.logo = page.locator(".header-logo");
        this.searchInput = page.locator("#small-searchterms");
        this.searchButton = page.locator("button.search-box-button");
        this.registerLink = page.locator("a.ico-register");
        this.loginLink = page.locator("a.ico-login");
        this.wishlistLink = page.locator("a.ico-wishlist");
        this.cartLink = page.locator("#topcartlink a");
        this.customerInfoLink = page.locator("a.ico-account");
        this.logoutLink = page.locator("a.ico-logout");
        this.currencyDropdown = page.locator("#customerCurrency");
        this.topMenu = page.locator(".top-menu.notmobile");
        this.featuredProductsGrid = page.locator(".product-grid.home-page-product-grid");
    }

    public boolean isLogoVisible() {
        return logo.count() > 0;
    }

    public void search(String keyword) {
        fill(searchInput, keyword);
        click(searchButton);
    }

    public RegisterPage clickRegister() {
        click(registerLink);
        return new RegisterPage(page);
    }

    public LoginPage clickLogin() {
        click(loginLink);
        return new LoginPage(page);
    }

    public CartPage clickCart() {
        click(cartLink);
        return new CartPage(page);
    }

    public CustomerInfoPage clickMyAccount() {
        click(customerInfoLink);
        return new CustomerInfoPage(page);
    }

    public void clickLogout() {
        try {
            logoutLink.waitFor();
            click(logoutLink);
        } catch (Exception e) {
            if (isVisible(logoutLink)) {
                click(logoutLink);
            }
        }
    }

    public CategoryPage navigateToCategory(String categoryName) {
        Locator catLink = topMenu.locator(String.format("a:has-text('%s')", categoryName)).first();
        click(catLink);
        return new CategoryPage(page);
    }

    public void selectCurrency(String currencyText) {
        currencyDropdown.selectOption(currencyText);
    }

    public boolean isFeaturedProductsVisible() {
        return isVisible(featuredProductsGrid);
    }

    public Locator getSearchInput() {
        return searchInput;
    }

    public Locator getCartLink() {
        return cartLink;
    }
}
