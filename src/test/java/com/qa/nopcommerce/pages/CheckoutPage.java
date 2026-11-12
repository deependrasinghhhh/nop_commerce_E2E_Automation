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
    private final Locator billingFirstNameInput;
    private final Locator billingLastNameInput;
    private final Locator billingEmailInput;
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
    private final Locator paymentMethodOptions;
    private final Locator paymentMethodContinueButton;

    // Payment info step
    private final Locator paymentInfoContinueButton;

    // Confirm order step
    private final Locator confirmOrderButton;

    public CheckoutPage(Page page) {
        super(page);
        this.checkoutAsGuestButton = page.locator("button.checkout-as-guest-button");

        this.billingFirstNameInput = page.locator("#BillingNewAddress_FirstName");
        this.billingLastNameInput = page.locator("#BillingNewAddress_LastName");
        this.billingEmailInput = page.locator("#BillingNewAddress_Email");
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
        this.paymentMethodOptions = page.locator("input[name='paymentmethod']");
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

    public boolean isBillingStepVisible() {
        return waitForAnyVisible(billingCountryDropdown, page.locator("#billing-buttons-container"));
    }

    public boolean isCheckoutStepVisible() {
        return waitForVisible(page.locator("#checkout-steps, .checkout-page"));
    }

    public void fillBillingAddress(String firstName, String lastName, String email, String country,
                                   String state, String city, String address1, String zip, String phone) {
        fill(billingFirstNameInput, firstName);
        fill(billingLastNameInput, lastName);
        fill(billingEmailInput, email);

        String currentCountry = "";
        try {
            currentCountry = billingCountryDropdown.locator("option:checked").innerText().trim();
        } catch (Exception ignored) {}

        if (!country.equalsIgnoreCase(currentCountry)) {
            billingCountryDropdown.selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(country));
            page.waitForFunction(
                    "() => { const p = document.querySelector('#states-loading-progress'); " +
                            "return !p || p.style.display === 'none' || getComputedStyle(p).display === 'none'; }",
                    null,
                    new Page.WaitForFunctionOptions().setTimeout(15_000));
        }

        page.waitForFunction(
                "state => { const select = document.querySelector('#BillingNewAddress_StateProvinceId'); " +
                        "return select && Array.from(select.options).some(option => option.textContent.trim() === state); }",
                state,
                new Page.WaitForFunctionOptions().setTimeout(15_000));
        billingStateDropdown.selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(state));
        page.waitForFunction(
                "state => { const select = document.querySelector('#BillingNewAddress_StateProvinceId'); " +
                        "return select && select.selectedOptions.length === 1 && " +
                        "select.selectedOptions[0].textContent.trim() === state; }",
                state,
                new Page.WaitForFunctionOptions().setTimeout(15_000));
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
        if (!waitForVisible(shippingMethodGroundRadio, 15_000)) {
            throw new IllegalStateException("Shipping method step did not expose a selectable Ground option");
        }
        shippingMethodGroundRadio.check();
        click(shippingMethodContinueButton);
    }

    public void selectPaymentMethodAndContinue() {
        if (isVisible(paymentMethodCheckMoneyOrderRadio)) {
            paymentMethodCheckMoneyOrderRadio.check();
        }
        click(paymentMethodContinueButton);
    }

    public boolean isPaymentMethodOptionVisible() {
        return waitForVisible(paymentMethodCheckMoneyOrderRadio);
    }

    public int getPaymentMethodOptionCount() {
        return safeCount(paymentMethodOptions);
    }

    public void continueToPaymentMethod() {
        boolean stepReady = waitForAnyVisible(15_000, shippingContinueButton,
                shippingMethodGroundRadio, paymentMethodCheckMoneyOrderRadio);
        if (!stepReady) {
            throw new IllegalStateException("Checkout did not advance from billing to a shipping or payment step. " +
                    "Visible billing validation messages: " + getBillingValidationMessages());
        }

        if (isVisible(shippingContinueButton)) {
            click(shippingContinueButton);
        }

        boolean nextStepReady = waitForAnyVisible(15_000, paymentMethodCheckMoneyOrderRadio,
                shippingMethodGroundRadio);
        if (!nextStepReady) {
            throw new IllegalStateException("Checkout did not expose a shipping method or payment method step");
        }
        if (isVisible(shippingMethodGroundRadio)) {
            selectShippingMethodAndContinue();
        }

        if (!waitForVisible(paymentMethodCheckMoneyOrderRadio, 15_000)) {
            throw new IllegalStateException("Payment method step did not expose Check / Money Order");
        }
    }

    private String getBillingValidationMessages() {
        return String.join("; ", page.locator(
                "#checkout-step-billing .field-validation-error:visible, " +
                        "#checkout-step-billing .validation-summary-errors:visible, " +
                        "#checkout-step-billing .message-error:visible").allInnerTexts());
    }

    public void continuePaymentInfo() {
        click(paymentInfoContinueButton);
    }

    public OrderSuccessPage confirmOrder() {
        click(confirmOrderButton);
        return new OrderSuccessPage(page);
    }
}
