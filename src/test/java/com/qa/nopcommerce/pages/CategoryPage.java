package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

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
        return productItems.count();
    }

    public List<String> getProductTitles() {
        return page.locator(".product-title a").allInnerTexts();
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

    public Locator getProductItems() {
        return productItems;
    }
}
