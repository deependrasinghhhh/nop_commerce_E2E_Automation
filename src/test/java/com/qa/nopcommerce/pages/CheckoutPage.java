package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

/**
 * Page Object representing nopCommerce One-Page Checkout (/checkout).
 */
public class CheckoutPage extends BasePage {

    // Guest checkout button (if not logged in)
    private final Locator checkoutAsGuestButton;

    // Billing step
    private final Locator billingCountryDropdown;
    private final Locator billingStateDropdown;
    private final Locator billingCityInput;
    private final Locator billingAddress1Input;
    private final Locator billingZipInput;
    private final Locator billingPhoneInput;
    private final Locator billingContinueButton;

    // Shipping step
    private final Locator shippingContinueButton;

    // Shipping method step
    private final Locator shippingMethodGroundRadio;
    private final Locator shippingMethodContinueButton;

    // Payment method step
    private final Locator paymentMethodCheckMoneyOrderRadio;
    private final Locator paymentMethodContinueButton;

    // Payment info step
    private final Locator paymentInfoContinueButton;

    // Confirm step
    private final Locator confirmOrderButton;

    public CheckoutPage(Page page) {
        super(page);
        this.checkoutAsGuestButton = page.locator("button.checkout-as-guest-button");

        this.billingCountryDropdown = page.locator("#BillingNewAddress_CountryId");
        this.billingStateDropdown = page.locator("#BillingNewAddress_StateProvinceId");
        this.billingCityInput = page.locator("#BillingNewAddress_City");
        this.billingAddress1Input = page.locator("#BillingNewAddress_Address1");
        this.billingZipInput = page.locator("#BillingNewAddress_ZipPostalCode");
        this.billingPhoneInput = page.locator("#BillingNewAddress_PhoneNumber");
        this.billingContinueButton = page.locator("#billing-buttons-container button.new-address-next-step-button");

        this.shippingContinueButton = page.locator("#shipping-buttons-container button.new-address-next-step-button");

        this.shippingMethodGroundRadio = page.locator("#shippingoption_0");
        this.shippingMethodContinueButton = page.locator("#shipping-method-buttons-container button.shipping-method-next-step-button");

        this.paymentMethodCheckMoneyOrderRadio = page.locator("input[value='Payments.CheckMoneyOrder']");
        this.paymentMethodContinueButton = page.locator("#payment-method-buttons-container button.payment-method-next-step-button");

        this.paymentInfoContinueButton = page.locator("#payment-info-buttons-container button.payment-info-next-step-button");
        this.confirmOrderButton = page.locator("#confirm-order-buttons-container button.confirm-order-next-step-button");
    }

    public void clickCheckoutAsGuest() {
        try {
            checkoutAsGuestButton.waitFor(new Locator.WaitForOptions().setTimeout(4000));
            checkoutAsGuestButton.click();
        } catch (Exception ignored) {
            // Already past login / guest prompt, or prompt not displayed
        }
    }

    public void fillBillingAddress(String country, String city, String address1, String zip, String phone) {
        billingCountryDropdown.selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(country));
        fill(billingCityInput, city);
        fill(billingAddress1Input, address1);
        fill(billingZipInput, zip);
        fill(billingPhoneInput, phone);
        click(billingContinueButton);
    }

    public void continueBilling() {
        click(billingContinueButton);
    }

    public void continueShipping() {
        if (isVisible(shippingContinueButton)) {
            click(shippingContinueButton);
        }
    }

    public void selectShippingMethodAndContinue() {
        if (isVisible(shippingMethodGroundRadio)) {
            shippingMethodGroundRadio.check();
        }
        click(shippingMethodContinueButton);
    }

    public void selectPaymentMethodAndContinue() {
        if (isVisible(paymentMethodCheckMoneyOrderRadio)) {
            paymentMethodCheckMoneyOrderRadio.check();
        }
        click(paymentMethodContinueButton);
    }

    public void continuePaymentInfo() {
        click(paymentInfoContinueButton);
    }

    public OrderSuccessPage confirmOrder() {
        click(confirmOrderButton);
        return new OrderSuccessPage(page);
    }
}
