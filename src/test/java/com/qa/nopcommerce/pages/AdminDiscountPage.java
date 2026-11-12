package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce Administration Discount management (/Admin/Discount/List, /Admin/Discount/Create).
 */
public class AdminDiscountPage extends BasePage {

    private final Locator discountsGrid;
    private final Locator addNewButton;
    private final Locator nameInput;
    private final Locator usePercentageCheckbox;
    private final Locator discountPercentageInput;
    private final Locator requiresCouponCodeCheckbox;
    private final Locator couponCodeInput;
    private final Locator saveButton;
    private final Locator successAlert;
    private final Locator licenseTermsModal;
    private final Locator acceptLicenseTermsButton;

    public AdminDiscountPage(Page page) {
        super(page);
        this.discountsGrid = page.locator("table#discounts-grid");
        this.addNewButton = page.locator("a[href*='Discount/Create'], a.btn-primary:has-text('Add new')").first();
        this.nameInput = page.locator("#Name");
        this.usePercentageCheckbox = page.locator("#UsePercentage");
        this.discountPercentageInput = page.locator("#DiscountPercentage");
        this.requiresCouponCodeCheckbox = page.locator("#RequiresCouponCode");
        this.couponCodeInput = page.locator("#CouponCode");
        this.saveButton = page.locator("button[name='save']");
        this.successAlert = page.locator(".alert-success");
        this.licenseTermsModal = page.locator("#license-terms");
        this.acceptLicenseTermsButton = page.locator("#accept-license-terms-button");
    }

    public void waitForGrid() {
        discountsGrid.waitFor();
    }

    public boolean isGridVisible() {
        return waitForVisible(discountsGrid);
    }

    public int getDiscountRowCount() {
        return safeCount(discountsGrid.locator("tbody tr"));
    }

    private void dismissLicenseTermsIfPresent() {
        waitForLoad();
        if (waitForVisible(acceptLicenseTermsButton, 2_500)) {
            click(acceptLicenseTermsButton);
            waitForCondition(() -> !isVisible(licenseTermsModal), 5_000);
        }
    }

    public void openCreateDiscountPage() {
        dismissLicenseTermsIfPresent();
        if (isVisible(addNewButton)) {
            click(addNewButton);
        }
    }

    public void fillDiscountForm(String name, int percentage, String couponCode) {
        dismissLicenseTermsIfPresent();
        fill(nameInput, name);
        usePercentageCheckbox.check();
        fill(discountPercentageInput, String.valueOf(percentage));
        requiresCouponCodeCheckbox.check();
        fill(couponCodeInput, couponCode);
    }

    public void saveDiscount() {
        click(saveButton);
        waitForLoad();
    }

    public void createPercentageDiscount(String name, int percentage, String couponCode) {
        fillDiscountForm(name, percentage, couponCode);
        saveDiscount();
    }

    public boolean isDiscountCreatedSuccessfully() {
        return waitForVisible(successAlert)
                && getText(successAlert).contains("The new discount has been added successfully");
    }

    public String getSuccessAlertText() {
        return getText(successAlert);
    }
}
