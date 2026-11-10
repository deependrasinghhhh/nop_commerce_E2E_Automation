package com.qa.nopcommerce.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;

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
                              String countryLabel, String city, String address1,
                              String zip, String phone) {
        click(addNewAddressButton);
        fillAddressForm(firstName, lastName, email, countryLabel, city, address1, zip, phone);
        click(saveAddressButton);
    }

    public void editAddressByStreet(String existingAddress1, String updatedCity, String updatedAddress1, String updatedZip) {
        Locator card = getAddressCardByText(existingAddress1);
        Locator editBtn = card.locator("button.edit-address-button");
        click(editBtn);
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
                                 String countryLabel, String city, String address1,
                                 String zip, String phone) {
        fill(addressFirstNameInput, firstName);
        fill(addressLastNameInput, lastName);
        fill(addressEmailInput, email);
        addressCountryDropdown.selectOption(new com.microsoft.playwright.options.SelectOption().setLabel(countryLabel));
        page.waitForTimeout(500);
        try {
            if (addressStateDropdown.count() > 0) {
                addressStateDropdown.selectOption(new com.microsoft.playwright.options.SelectOption().setIndex(1));
            }
        } catch (Exception ignored) {}
        fill(addressCityInput, city);
        fill(address1Input, address1);
        fill(addressZipInput, zip);
        fill(addressPhoneInput, phone);
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
