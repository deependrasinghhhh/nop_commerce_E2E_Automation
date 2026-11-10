package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Response;
import com.microsoft.playwright.options.WaitForSelectorState;

/**
 * Page Object representing My Account - Customer Info, Address Book CRUD, and Orders history.
 */
public class CustomerInfoPage extends BasePage {

    private final Locator pageTitle;
    private final Locator firstNameInput;
    private final Locator lastNameInput;
    private final Locator emailInput;
    private final Locator saveButton;
    private final Locator ordersMenuLink;
    private final Locator addressesMenuLink;
    private final Locator changePasswordMenuLink;

    // Customer Address Book Locators (/customer/addresses)
    private final Locator addNewAddressButton;
    private final Locator addressItems;
    private final Locator noAddressesMessage;
    private final Locator addressFirstNameInput;
    private final Locator addressLastNameInput;
    private final Locator addressEmailInput;
    private final Locator addressCountryDropdown;
    private final Locator addressStateDropdown;
    private final Locator addressCityInput;
    private final Locator address1Input;
    private final Locator addressZipInput;
    private final Locator addressPhoneInput;
    private final Locator saveAddressButton;

    public CustomerInfoPage(Page page) {
        super(page);
        this.pageTitle = page.locator(".page-title h1");
        this.firstNameInput = page.locator("#FirstName");
        this.lastNameInput = page.locator("#LastName");
        this.emailInput = page.locator("#Email");
        this.saveButton = page.locator("#save-info-button");
        this.ordersMenuLink = page.locator("a[href*='order/history'], a[href*='customer/orders']");
        this.addressesMenuLink = page.locator("a[href*='customer/addresses']");
        this.changePasswordMenuLink = page.locator("a[href*='customer/changepassword']");

        this.addNewAddressButton = page.locator("button.add-address-button");
        this.addressItems = page.locator(".address-list .address-item");
        this.noAddressesMessage = page.locator(".address-list .no-data, div.no-data");
        this.addressFirstNameInput = page.locator("#Address_FirstName");
        this.addressLastNameInput = page.locator("#Address_LastName");
        this.addressEmailInput = page.locator("#Address_Email");
        this.addressCountryDropdown = page.locator("#Address_CountryId");
        this.addressStateDropdown = page.locator("#Address_StateProvinceId");
        this.addressCityInput = page.locator("#Address_City");
        this.address1Input = page.locator("#Address_Address1");
        this.addressZipInput = page.locator("#Address_ZipPostalCode");
        this.addressPhoneInput = page.locator("#Address_PhoneNumber");
        this.saveAddressButton = page.locator("button.save-address-button");
    }

    public String getFirstName() {
        return firstNameInput.inputValue();
    }

    public String getLastName() {
        return lastNameInput.inputValue();
    }

    public String getEmail() {
        return emailInput.inputValue();
    }

    public Locator getFirstNameInput() {
        return firstNameInput;
    }

    public Locator getEmailInput() {
        return emailInput;
    }

    public void updateName(String firstName, String lastName) {
        fill(firstNameInput, firstName);
        fill(lastNameInput, lastName);
        click(saveButton);
        waitForLoad();
    }

    public void clickOrders() {
        click(ordersMenuLink);
        waitForLoad();
    }

    public void clickAddresses() {
        click(addressesMenuLink);
        waitForLoad();
    }

    public void clickChangePassword() {
        click(changePasswordMenuLink);
        waitForLoad();
    }

    public void addNewAddress(String firstName, String lastName, String email,
                              String countryLabel, String stateProvinceLabel, String city, String address1,
                              String zip, String phone) {
        click(addNewAddressButton);
        fillAddressForm(firstName, lastName, email, countryLabel, stateProvinceLabel, city, address1, zip, phone);
        click(saveAddressButton);
    }

    public void editAddressByStreet(String existingAddress1, String updatedStateProvince,
                                    String updatedCity, String updatedAddress1, String updatedZip) {
        Locator card = getAddressCardByText(existingAddress1);
        Locator editBtn = card.locator("button.edit-address-button");
        click(editBtn);
        selectAddressState(updatedStateProvince);
        fill(addressCityInput, updatedCity);
        fill(address1Input, updatedAddress1);
        fill(addressZipInput, updatedZip);
        click(saveAddressButton);
    }

    public void deleteAddressByStreet(String address1Text) {
        Locator card = getAddressCardByText(address1Text);
        Locator deleteBtn = card.locator("button.delete-address-button");
        page.onceDialog(dialog -> dialog.accept());
        click(deleteBtn);
        page.waitForLoadState();
    }

    private void fillAddressForm(String firstName, String lastName, String email,
                                 String countryLabel, String stateProvinceLabel, String city, String address1,
                                 String zip, String phone) {
        fill(addressFirstNameInput, firstName);
        fill(addressLastNameInput, lastName);
        fill(addressEmailInput, email);
        Response statesResponse = page.waitForResponse(
                response -> response.url().contains("/country/getstatesbycountryid") && response.ok(),
                () -> addressCountryDropdown.selectOption(
                        new com.microsoft.playwright.options.SelectOption().setLabel(countryLabel)));
        String responseFailure = statesResponse.finished();
        if (responseFailure != null) {
            throw new IllegalStateException("Could not load state/province options for " + countryLabel
                    + ": " + responseFailure);
        }
        page.locator("#states-loading-progress").waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.HIDDEN)
                .setTimeout(15_000));
        if ("United States of America".equals(countryLabel) || (stateProvinceLabel != null && !stateProvinceLabel.isBlank())) {
            selectAddressState(stateProvinceLabel);
        }
        fill(addressCityInput, city);
        fill(address1Input, address1);
        fill(addressZipInput, zip);
        fill(addressPhoneInput, phone);
    }

    private void selectAddressState(String stateProvinceLabel) {
        if (stateProvinceLabel == null || stateProvinceLabel.isBlank()) {
            throw new IllegalArgumentException("A state/province label is required for this address.");
        }

        Locator requestedState = addressStateDropdown.locator("option")
                .filter(new Locator.FilterOptions().setHasText(stateProvinceLabel)).first();
        requestedState.waitFor(new Locator.WaitForOptions()
                .setState(WaitForSelectorState.ATTACHED)
                .setTimeout(15_000));

        String stateValue = requestedState.getAttribute("value");
        if (stateValue == null || stateValue.isBlank() || "0".equals(stateValue)) {
            throw new IllegalStateException("The requested state/province is not selectable: " + stateProvinceLabel);
        }

        addressStateDropdown.selectOption(stateValue);
        if (!stateValue.equals(addressStateDropdown.inputValue())) {
            throw new IllegalStateException("State/province selection did not persist: " + stateProvinceLabel);
        }
    }

    public Locator getAddressCardByText(String text) {
        return addressItems.filter(new Locator.FilterOptions().setHasText(text)).first();
    }

    public Locator getAddressItems() {
        return addressItems;
    }

    public Locator getNoAddressesMessage() {
        return noAddressesMessage;
    }
}
