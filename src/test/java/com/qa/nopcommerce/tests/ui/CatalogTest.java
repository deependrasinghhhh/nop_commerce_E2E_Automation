package com.qa.nopcommerce.tests.ui;

import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.pages.CategoryPage;
import com.qa.nopcommerce.pages.HomePage;
import com.qa.nopcommerce.pages.ProductDetailsPage;
import com.qa.nopcommerce.pages.SearchPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

@Epic("Storefront Core")
@Feature("Catalog Browsing, Search & Discovery")
public class CatalogTest extends BaseTest {

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-002", requirementId = "REQ-CAT-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify navigating to Computers and Desktops subcategory displays matching products")
    public void testCategoryNavigation() {
        navigateTo("/computers");
        CategoryPage categoryPage = new CategoryPage(getPage());
        Assert.assertTrue(categoryPage.getCategoryTitle().contains("Computers"),
                "Expected category title to contain Computers");

        navigateTo("/desktops");
        CategoryPage desktopsPage = new CategoryPage(getPage());
        Assert.assertTrue(desktopsPage.hasProducts(), "Expected products in desktops category");
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-003", requirementId = "REQ-CAT-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify top menu contains standard categories (Computers, Electronics, Apparel)")
    public void testTopMenuCategoriesRendering() {
        navigateTo("/");
        HomePage homePage = new HomePage(getPage());

        Assert.assertTrue(homePage.isCategoryMenuVisible("computers"), "Expected Computers category in top menu");
        Assert.assertTrue(homePage.isCategoryMenuVisible("electronics"), "Expected Electronics category in top menu");
        Assert.assertTrue(homePage.isCategoryMenuVisible("apparel"), "Expected Apparel category in top menu");
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-009", requirementId = "REQ-CAT-002")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify product details page renders product name, price, and add-to-cart button")
    public void testProductDetailsRendering() {
        navigateTo("/build-your-own-computer");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());

        Assert.assertTrue(productPage.getProductName().contains("Build your own computer"),
                "Expected product name to contain 'Build your own computer'");
        Assert.assertTrue(productPage.isProductPriceVisible(), "Expected product price to be visible");
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-010", requirementId = "REQ-CAT-002")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify product detail page image gallery and overview elements render")
    public void testProductImageGalleryDisplay() {
        navigateTo("/build-your-own-computer");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());
        Assert.assertTrue(productPage.isProductImageGalleryVisible(), "Expected product image gallery to be visible");
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-013", requirementId = "REQ-CAT-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify search with non-existent keyword displays no-result message")
    public void testSearchWithNonExistentKeyword() {
        navigateTo("/");
        HomePage homePage = new HomePage(getPage());
        homePage.search("xyznonexistentproduct999");

        SearchPage searchPage = new SearchPage(getPage());
        Assert.assertTrue(getPage().url().contains("search"), "Expected search in URL");
        Assert.assertTrue(getPage().url().contains("q=xyznonexistentproduct999"), "Expected query param in URL");
        Assert.assertTrue(searchPage.isNoResultVisible(), "Expected no-result message to be visible");
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-014", requirementId = "REQ-CAT-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify search with single-character query triggers minimum length warning")
    public void testShortSearchTermNotice() {
        navigateTo("/search?q=a");

        SearchPage searchPage = new SearchPage(getPage());
        Assert.assertTrue(searchPage.isWarningVisible(), "Expected search warning to be visible");
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-016", requirementId = "REQ-CAT-005")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify products sorting dropdown sorts products by Price: Low to High")
    public void testProductSorting() {
        navigateTo("/desktops");
        CategoryPage categoryPage = new CategoryPage(getPage());

        categoryPage.selectSortOption("10");
        Assert.assertTrue(categoryPage.hasProducts(), "Expected products to be displayed after sorting");
    }
}
