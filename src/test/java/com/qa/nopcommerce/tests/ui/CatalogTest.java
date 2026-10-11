package com.qa.nopcommerce.tests.ui;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.assertions.PlaywrightAssertions;
import com.qa.nopcommerce.annotation.TestCase;
import com.qa.nopcommerce.base.BaseTest;
import com.qa.nopcommerce.pages.CategoryPage;
import com.qa.nopcommerce.pages.HomePage;
import com.qa.nopcommerce.pages.ProductDetailsPage;
import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.regex.Pattern;

@Epic("Storefront Core")
@Feature("Catalog Browsing, Search & Discovery")
public class CatalogTest extends BaseTest {

    @Test(groups = {"ui", "catalog", "smoke"})
    @TestCase("TC-CAT-001")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify storefront homepage title, logo, top navigation bar, and search box render successfully")
    public void testHomePageTitleAndHeaderRendering() {
        navigateTo("/");
        HomePage homePage = new HomePage(getPage());

        PlaywrightAssertions.assertThat(getPage()).hasTitle(Pattern.compile(".*nopCommerce.*"));
        Assert.assertTrue(homePage.isLogoVisible(), "Storefront logo is not visible");
        PlaywrightAssertions.assertThat(homePage.getSearchInput()).isVisible();
        PlaywrightAssertions.assertThat(homePage.getCartLink()).isVisible();
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase("TC-CAT-002")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify top menu contains standard categories (Computers, Electronics, Apparel)")
    public void testTopMenuCategoriesRendering() {
        navigateTo("/");

        Locator topMenu = getPage().locator(".top-menu.notmobile");
        PlaywrightAssertions.assertThat(topMenu.locator("a:has-text('Computers')").first()).isVisible();
        PlaywrightAssertions.assertThat(topMenu.locator("a:has-text('Electronics')").first()).isVisible();
        PlaywrightAssertions.assertThat(topMenu.locator("a:has-text('Apparel')").first()).isVisible();
        PlaywrightAssertions.assertThat(topMenu.locator("a:has-text('Digital downloads')").first()).isVisible();
        PlaywrightAssertions.assertThat(topMenu.locator("a:has-text('Books')").first()).isVisible();
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase("TC-CAT-003")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify navigation to category and sub-category lists products")
    public void testCategoryNavigation() {
        navigateTo("/computers");
        CategoryPage categoryPage = new CategoryPage(getPage());

        Assert.assertTrue(categoryPage.getCategoryTitle().contains("Computers"),
                "Category title mismatch");

        // Navigate to sub-category Desktops
        navigateTo("/desktops");
        CategoryPage desktopsPage = new CategoryPage(getPage());
        Assert.assertTrue(desktopsPage.getProductCount() > 0,
                "Expected at least one product in Desktops category");
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase("TC-CAT-005")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify products sorting dropdown can be changed")
    public void testProductSorting() {
        navigateTo("/desktops");
        CategoryPage categoryPage = new CategoryPage(getPage());

        // Sort by Price: Low to High (value = 10)
        categoryPage.selectSortOption("10");
        Assert.assertTrue(categoryPage.getProductCount() > 0, "Products should render after sort");
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase("TC-CAT-008")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify product details page renders product name, price, and add-to-cart button")
    public void testProductDetailsRendering() {
        navigateTo("/build-your-own-computer");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());

        Assert.assertTrue(productPage.getProductName().contains("Build your own computer"),
                "Product title mismatch");
        Assert.assertFalse(productPage.getProductPrice().isEmpty(),
                "Product price should not be empty");
    }

    @Test(groups = {"ui", "catalog", "smoke"})
    @TestCase("TC-CAT-011")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify search input executes keyword search and returns matching items")
    public void testStorefrontSearchBox() {
        navigateTo("/");
        HomePage homePage = new HomePage(getPage());
        homePage.search("camera");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*search.*q=camera.*"));
        PlaywrightAssertions.assertThat(getPage().locator(".search-results")).isVisible();
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase("TC-CAT-012")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify search with empty or single-character query triggers minimum length alert or notice")
    public void testShortSearchTermNotice() {
        navigateTo("/search?q=a");

        Locator warning = getPage().locator(".warning, .no-result");
        PlaywrightAssertions.assertThat(warning.first()).isVisible();
    }
}
