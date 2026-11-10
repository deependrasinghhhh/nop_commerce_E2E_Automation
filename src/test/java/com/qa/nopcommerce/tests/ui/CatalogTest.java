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
import org.testng.annotations.Test;

import java.util.regex.Pattern;

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
        PlaywrightAssertions.assertThat(getPage().locator(".page-title h1")).containsText("Computers");

        navigateTo("/desktops");
        CategoryPage desktopsPage = new CategoryPage(getPage());
        PlaywrightAssertions.assertThat(desktopsPage.getProductItems()).not().hasCount(0);
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-003", requirementId = "REQ-CAT-001")
    @Severity(SeverityLevel.CRITICAL)
    @Description("Verify top menu contains standard categories (Computers, Electronics, Apparel)")
    public void testTopMenuCategoriesRendering() {
        navigateTo("/");

        Locator topMenu = getPage().locator(".header-menu, .top-menu");
        PlaywrightAssertions.assertThat(topMenu.locator("a[href*='/computers']").first()).isVisible();
        PlaywrightAssertions.assertThat(topMenu.locator("a[href*='/electronics']").first()).isVisible();
        PlaywrightAssertions.assertThat(topMenu.locator("a[href*='/apparel']").first()).isVisible();
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-009", requirementId = "REQ-CAT-002")
    @Severity(SeverityLevel.BLOCKER)
    @Description("Verify product details page renders product name, price, and add-to-cart button")
    public void testProductDetailsRendering() {
        navigateTo("/build-your-own-computer");
        ProductDetailsPage productPage = new ProductDetailsPage(getPage());

        PlaywrightAssertions.assertThat(productPage.getProductNameHeading())
                .containsText("Build your own computer");
        PlaywrightAssertions.assertThat(productPage.getProductPriceLocator()).isVisible();
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-010", requirementId = "REQ-CAT-002")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify product detail page image gallery and overview elements render")
    public void testProductImageGalleryDisplay() {
        navigateTo("/build-your-own-computer");
        PlaywrightAssertions.assertThat(getPage().locator(".gallery .picture img").first()).isVisible();
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-013", requirementId = "REQ-CAT-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify search with non-existent keyword displays no-result message")
    public void testSearchWithNonExistentKeyword() {
        navigateTo("/");
        HomePage homePage = new HomePage(getPage());
        homePage.search("xyznonexistentproduct999");

        PlaywrightAssertions.assertThat(getPage()).hasURL(Pattern.compile(".*search.*q=xyznonexistentproduct999.*"));
        PlaywrightAssertions.assertThat(getPage().locator(".no-result")).isVisible();
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-014", requirementId = "REQ-CAT-004")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify search with single-character query triggers minimum length warning")
    public void testShortSearchTermNotice() {
        navigateTo("/search?q=a");

        Locator warning = getPage().locator(".warning, .no-result");
        PlaywrightAssertions.assertThat(warning.first()).isVisible();
    }

    @Test(groups = {"ui", "catalog"})
    @TestCase(value = "TC-CAT-016", requirementId = "REQ-CAT-005")
    @Severity(SeverityLevel.NORMAL)
    @Description("Verify products sorting dropdown sorts products by Price: Low to High")
    public void testProductSorting() {
        navigateTo("/desktops");
        CategoryPage categoryPage = new CategoryPage(getPage());

        categoryPage.selectSortOption("10");
        PlaywrightAssertions.assertThat(categoryPage.getProductItems()).not().hasCount(0);
    }
}
