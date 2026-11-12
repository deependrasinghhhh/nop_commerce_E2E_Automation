package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

import java.math.BigDecimal;
import java.util.List;

/**
 * Page Object representing Category Listing and Catalog Browsing.
 */
public class CategoryPage extends BasePage {

    private final Locator pageTitle;
    private final Locator productItems;
    private final Locator sortDropdown;
    private final Locator pageSizeDropdown;
    private final Locator subcategories;

    public CategoryPage(Page page) {
        super(page);
        this.pageTitle = page.locator(".page-title h1");
        this.productItems = page.locator(".product-item");
        this.sortDropdown = page.locator("#products-orderby");
        this.pageSizeDropdown = page.locator("#products-pagesize");
        this.subcategories = page.locator(".sub-category-item");
    }

    public String getCategoryTitle() {
        return getText(pageTitle);
    }

    public int getProductCount() {
        return safeCount(productItems);
    }

    public List<String> getProductTitles() {
        return page.locator(".product-title a").allInnerTexts();
    }

    public List<String> getProductPrices() {
        return page.locator(".product-item .prices .actual-price").allInnerTexts();
    }

    public boolean waitForProductPricesAscending() {
        return waitForCondition(() -> {
            List<String> displayedPrices = getProductPrices();
            if (displayedPrices.size() < 2) {
                return false;
            }
            List<BigDecimal> prices = displayedPrices.stream().map(CategoryPage::parsePrice).toList();
            for (int index = 1; index < prices.size(); index++) {
                if (prices.get(index - 1).compareTo(prices.get(index)) > 0) {
                    return false;
                }
            }
            return true;
        }, 15_000);
    }

    private static BigDecimal parsePrice(String text) {
        String normalized = text.replaceAll("[^0-9,.-]", "").replace(",", "");
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("Product price was empty or not numeric: " + text);
        }
        return new BigDecimal(normalized);
    }

    public ProductDetailsPage selectProductByName(String productName) {
        Locator target = page.locator(String.format(".product-title a:has-text('%s')", productName)).first();
        click(target);
        return new ProductDetailsPage(page);
    }

    public void selectSortOption(String sortOption) {
        sortDropdown.selectOption(sortOption);
        waitForLoad();
    }

    public void selectPageSize(String pageSizeOption) {
        pageSizeDropdown.selectOption(pageSizeOption);
        waitForLoad();
    }

    public void clickSubcategory(String subcategoryName) {
        Locator sub = page.locator(String.format(".sub-category-item a:has-text('%s')", subcategoryName)).first();
        click(sub);
    }

    public boolean hasProducts() {
        return waitForMinCount(productItems, 1);
    }
}
