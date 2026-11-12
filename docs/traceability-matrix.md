# Requirements Traceability Matrix (RTM)

**Reconciliation Timestamp:** 2026-11-12 10:15:57 UTC
**Execution Window:** 2026-11-12T10:11:03Z to 2026-11-12T10:15:57Z (294.00s)
**Target AUT:** nopCommerce 4.90.8 (.NET 9, Isolated Docker Stack `http://127.0.0.1:5000`)

> **Evidence Notice:** Reconciled from `docs/test-cases.csv`, Java `@TestCase` annotations, and `evidence/surefire-reports/testng-results.xml`.

## 1. Traceability Summary

- **Total Specified Test Cases:** 108
- **Automated Test Methods (1:1 Mapped):** 61 (56.5%)
- **Executed:** 61/61 automated methods (100.0%)
- **Passed / Failed / Skipped:** 61 / 0 / 0 (100.0% pass rate of executed)
- **Not Executed / Manual Scope:** 47

## 2. Granular Traceability Table

| Test Case ID | Req ID | Scope | Title | Priority | Automated | Implementation | Execution Status |
|--------------|--------|-------|-------|----------|-----------|----------------|------------------|
| TC-AUTH-001 | REQ-AUTH-001 | UI | Successful User Registration with Valid Data | P1 | YES | [AuthTest#testValidCustomerRegistration](../src/test/java/com/qa/nopcommerce/tests/ui/AuthTest.java) | PASS |
| TC-AUTH-002 | REQ-AUTH-002 | UI | Registration with Missing First Name | P2 | YES | [AuthTest#testRegistrationMandatoryFieldValidation](../src/test/java/com/qa/nopcommerce/tests/ui/AuthTest.java) | PASS |
| TC-AUTH-003 | REQ-AUTH-002 | UI | Registration with Missing Last Name | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-AUTH-004 | REQ-AUTH-002 | UI | Registration with Missing Email | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-AUTH-005 | REQ-AUTH-002 | UI | Registration with Invalid Email Format | P2 | YES | [AuthTest#testRegistrationMalformedEmailValidation](../src/test/java/com/qa/nopcommerce/tests/ui/AuthTest.java) | PASS |
| TC-AUTH-006 | REQ-AUTH-003 | UI | Registration with Existing Registered Email | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-AUTH-007 | REQ-AUTH-004 | UI | Registration with Password Shorter than Policy | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-AUTH-008 | REQ-AUTH-004 | UI | Registration with Mismatched Password Confirmation | P2 | YES | [AuthTest#testRegistrationPasswordMismatch](../src/test/java/com/qa/nopcommerce/tests/ui/AuthTest.java) | PASS |
| TC-AUTH-009 | REQ-AUTH-005 | UI | Successful Login with Valid Registered Credentials | P1 | YES | [AuthTest#testCustomerLoginValidCredentials](../src/test/java/com/qa/nopcommerce/tests/ui/AuthTest.java) | PASS |
| TC-AUTH-010 | REQ-AUTH-006 | UI | Login with Unregistered Email Address | P1 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-AUTH-011 | REQ-AUTH-006 | UI | Login with Incorrect Password | P2 | YES | [AuthTest#testCustomerLoginInvalidPassword](../src/test/java/com/qa/nopcommerce/tests/ui/AuthTest.java) | PASS |
| TC-AUTH-012 | REQ-AUTH-006 | UI | Login with Blank Credentials | P2 | YES | [AuthTest#testCustomerLoginBlankEmail](../src/test/java/com/qa/nopcommerce/tests/ui/AuthTest.java) | PASS |
| TC-AUTH-013 | REQ-AUTH-007 | UI | Successful Customer Logout | P2 | YES | [AuthTest#testCustomerLogout](../src/test/java/com/qa/nopcommerce/tests/ui/AuthTest.java) | PASS |
| TC-AUTH-014 | REQ-AUTH-008 | UI | Password Recovery with Valid Email | P3 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-AUTH-015 | REQ-AUTH-008 | UI | Password Recovery with Non-Existent Email | P3 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-001 | REQ-CAT-001 | UI | Storefront Homepage & Top-Level Category Rendering | P1 | YES | [SmokeTest#testHomePageTitleAndHeaderRendering](../src/test/java/com/qa/nopcommerce/tests/smoke/SmokeTest.java) | PASS |
| TC-CAT-002 | REQ-CAT-001 | UI | Navigate Category - Computers & Desktops Subcategory | P1 | YES | [CatalogTest#testCategoryNavigation](../src/test/java/com/qa/nopcommerce/tests/ui/CatalogTest.java) | PASS |
| TC-CAT-003 | REQ-CAT-001 | UI | Top Menu Standard Categories Rendering (Computers, Electronics, Apparel) | P2 | YES | [CatalogTest#testTopMenuCategoriesRendering](../src/test/java/com/qa/nopcommerce/tests/ui/CatalogTest.java) | PASS |
| TC-CAT-004 | REQ-CAT-001 | UI | Navigate Top-Level Category - Apparel | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-005 | REQ-CAT-001 | UI | Navigate Category - Digital Downloads | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-006 | REQ-CAT-001 | UI | Navigate Category - Books | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-007 | REQ-CAT-001 | UI | Navigate Category - Jewelry | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-008 | REQ-CAT-001 | UI | Navigate Category - Gift Cards | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-009 | REQ-CAT-002 | UI | Product Detail Page Elements Rendering | P1 | YES | [CatalogTest#testProductDetailsRendering](../src/test/java/com/qa/nopcommerce/tests/ui/CatalogTest.java) | PASS |
| TC-CAT-010 | REQ-CAT-002 | UI | Product Detail Page Image Gallery Display | P1 | YES | [CatalogTest#testProductImageGalleryDisplay](../src/test/java/com/qa/nopcommerce/tests/ui/CatalogTest.java) | PASS |
| TC-CAT-011 | REQ-CAT-003 | UI | Search with Valid Keyword via Top Search Bar | P1 | YES | [SmokeTest#testStorefrontSearchBoxInput](../src/test/java/com/qa/nopcommerce/tests/smoke/SmokeTest.java) | PASS |
| TC-CAT-012 | REQ-CAT-003 | UI | Search Auto-Suggest Dropdown Display | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-013 | REQ-CAT-004 | UI | Search with Non-Existent Keyword | P2 | YES | [CatalogTest#testSearchWithNonExistentKeyword](../src/test/java/com/qa/nopcommerce/tests/ui/CatalogTest.java) | PASS |
| TC-CAT-014 | REQ-CAT-004 | UI | Search with Minimum Length / Single-Character Warning | P2 | YES | [CatalogTest#testShortSearchTermNotice](../src/test/java/com/qa/nopcommerce/tests/ui/CatalogTest.java) | PASS |
| TC-CAT-015 | REQ-CAT-004 | UI | Advanced Search Filtering by Category | P3 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-016 | REQ-CAT-005 | UI | Sort Products by Price Low to High | P2 | YES | [CatalogTest#testProductSorting](../src/test/java/com/qa/nopcommerce/tests/ui/CatalogTest.java) | PASS |
| TC-CAT-017 | REQ-CAT-005 | UI | Sort Products by Price High to Low | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-018 | REQ-CAT-005 | UI | Sort Products by Name A to Z | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-019 | REQ-CAT-006 | UI | Change Products Per Page Display to 3 | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CAT-020 | REQ-CAT-007 | UI | Add Products to Comparison List and Verify | P3 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CART-001 | REQ-CART-001 | UI | Add Simple Product to Cart from PDP | P1 | YES | [CartTest#testAddProductToCart](../src/test/java/com/qa/nopcommerce/tests/ui/CartTest.java) | PASS |
| TC-CART-002 | REQ-CART-001 | UI | Add Product to Cart with Required Attributes | P1 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CART-003 | REQ-CART-001 | UI | Add Product to Cart with Missing Required Attribute | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CART-004 | REQ-CART-002 | UI | View Shopping Cart Table & Verify Added Item | P1 | YES | [CartTest#testViewShoppingCart](../src/test/java/com/qa/nopcommerce/tests/ui/CartTest.java) | PASS |
| TC-CART-005 | REQ-CART-002 | UI | Verify Flyout Mini-Cart Contents on Hover | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CART-006 | REQ-CART-003 | UI | Update Cart Item Quantity and Recalculate Subtotal | P1 | YES | [CartTest#testUpdateCartItemQuantity](../src/test/java/com/qa/nopcommerce/tests/ui/CartTest.java) | PASS |
| TC-CART-007 | REQ-CART-004 | UI | Remove Item from Shopping Cart and Verify Empty State | P1 | YES | [CartTest#testRemoveItemFromCart](../src/test/java/com/qa/nopcommerce/tests/ui/CartTest.java) | PASS |
| TC-CART-008 | REQ-CART-004 | UI | Empty Shopping Cart State Display | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CART-009 | REQ-CART-005 | UI | Add Product to Wishlist from PDP and Verify Counter Increment | P1 | YES | [CartTest#testAddProductToWishlistAndVerifyCounter](../src/test/java/com/qa/nopcommerce/tests/ui/CartTest.java) | PASS |
| TC-CART-010 | REQ-CART-005 | UI | Verify Wishlist Sharing URL Page Rendering | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CART-011 | REQ-CART-006 | UI | Move Item from Wishlist to Shopping Cart | P1 | YES | [CartTest#testMoveItemFromWishlistToShoppingCart](../src/test/java/com/qa/nopcommerce/tests/ui/CartTest.java) | PASS |
| TC-CART-012 | REQ-CART-007 | UI | Apply Valid Discount Coupon Code in Shopping Cart | P1 | YES | [CartTest#testApplyValidCouponCode](../src/test/java/com/qa/nopcommerce/tests/ui/CartTest.java) | PASS |
| TC-CART-013 | REQ-CART-001 | UI | Add Gift Card with Valid Recipient Details | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CART-014 | REQ-CART-001 | UI | Add Gift Card with Missing Recipient Email | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CART-015 | REQ-CART-003 | UI | Apply Invalid Coupon Code Validation in Shopping Cart | P2 | YES | [CartTest#testApplyInvalidCouponCode](../src/test/java/com/qa/nopcommerce/tests/ui/CartTest.java) | PASS |
| TC-CHK-001 | REQ-CHK-001 | UI | Enforce Terms of Service Checkbox and Initiate Checkout | P1 | YES | [CheckoutTest#testCheckoutInitiation](../src/test/java/com/qa/nopcommerce/tests/ui/CheckoutTest.java) | PASS |
| TC-CHK-002 | REQ-CHK-002 | UI | Initiate Guest Checkout and Verify Billing Address Form | P1 | YES | [CheckoutTest#testCheckoutBillingAddressForm](../src/test/java/com/qa/nopcommerce/tests/ui/CheckoutTest.java) | PASS |
| TC-CHK-003 | REQ-CHK-003 | UI | Complete Billing Address Step with Valid Details | P1 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-004 | REQ-CHK-003 | UI | Billing Address Missing Mandatory City | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-005 | REQ-CHK-003 | UI | Billing Address Missing Mandatory Phone | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-006 | REQ-CHK-004 | UI | Select Shipping Method - Ground | P1 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-007 | REQ-CHK-004 | UI | Select Shipping Method - Next Day Air | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-008 | REQ-CHK-005 | UI | Verify Guest Checkout Exposes a Payment Method Option | P1 | YES | [CheckoutTest#testCheckoutPaymentMethodOptions](../src/test/java/com/qa/nopcommerce/tests/ui/CheckoutTest.java) | PASS |
| TC-CHK-009 | REQ-CHK-005 | UI | Verify Payment Information Instructions | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-010 | REQ-CHK-006 | UI | Confirm and Finalize Guest Order Placement | P1 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-011 | REQ-CHK-007 | UI | Checkout with Authenticated User and Saved Address | P1 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-012 | REQ-CHK-006 | UI | Verify Order Details Link on Completed Order Page | P1 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-013 | REQ-CHK-006 | UI | Verify Order PDF Invoice Download Link | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-014 | REQ-CHK-004 | UI | Checkout for Downloadable Product (No Shipping Required) | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CHK-015 | REQ-CHK-006 | UI | Cart Cleared After Successful Order Placement | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CUST-001 | REQ-CUST-001 | UI | View and Update Customer Personal Info | P1 | YES | [CustomerAccountTest#testCustomerInfoPageRendering](../src/test/java/com/qa/nopcommerce/tests/ui/CustomerAccountTest.java) | PASS |
| TC-CUST-002 | REQ-CUST-002 | UI | Create, Edit, and Delete Address in Customer Address Book | P1 | YES | [CustomerAccountTest#testCustomerAddressCreateEditAndDelete](../src/test/java/com/qa/nopcommerce/tests/ui/CustomerAccountTest.java) | PASS |
| TC-CUST-003 | REQ-CUST-002 | UI | Delete Address from Customer Address Book and Verify Empty State | P2 | YES | [CustomerAccountTest#testDeleteCustomerAddressFromAddressBook](../src/test/java/com/qa/nopcommerce/tests/ui/CustomerAccountTest.java) | PASS |
| TC-CUST-004 | REQ-CUST-003 | UI | View Customer Order History Page | P1 | YES | [CustomerAccountTest#testCustomerOrdersPage](../src/test/java/com/qa/nopcommerce/tests/ui/CustomerAccountTest.java) | PASS |
| TC-CUST-005 | REQ-CUST-004 | UI | Submit Product Review as Registered User with Cleanup | P1 | YES | [CustomerAccountTest#testSubmitProductReviewAsRegisteredUser](../src/test/java/com/qa/nopcommerce/tests/ui/CustomerAccountTest.java) | PASS |
| TC-CUST-006 | REQ-CUST-004 | UI | Product Review Submission Blocked for Unauthenticated Guest | P2 | YES | [CustomerAccountTest#testProductReviewBlockedForUnauthenticatedGuest](../src/test/java/com/qa/nopcommerce/tests/ui/CustomerAccountTest.java) | PASS |
| TC-CUST-007 | REQ-CUST-005 | UI | Submit Contact Us Form with Valid Data | P3 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CUST-008 | REQ-CUST-005 | UI | Submit Contact Us Form with Missing Email | P3 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CUST-009 | REQ-CUST-001 | UI | Change Password from Customer Account | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-CUST-010 | REQ-CUST-001 | UI | View Reward Points History | P3 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-ADM-001 | REQ-ADM-001 | UI | Admin Panel Login with Valid Credentials | P1 | YES | [AdminTest#testAdminLoginValidCredentials](../src/test/java/com/qa/nopcommerce/tests/ui/AdminTest.java) | PASS |
| TC-ADM-002 | REQ-ADM-001 | UI | Admin Login Page Rendering & Unauthenticated Gate | P1 | YES | [SmokeTest#testAdminLoginPageRendering](../src/test/java/com/qa/nopcommerce/tests/smoke/SmokeTest.java) | PASS |
| TC-ADM-003 | REQ-ADM-002 | UI | Admin Dashboard Metrics Card Display | P2 | YES | [AdminTest#testAdminDashboardElements](../src/test/java/com/qa/nopcommerce/tests/ui/AdminTest.java) | PASS |
| TC-ADM-004 | REQ-ADM-003 | UI | Admin Catalog Product List Page Verification | P2 | YES | [AdminTest#testAdminCatalogProductsPage](../src/test/java/com/qa/nopcommerce/tests/ui/AdminTest.java) | PASS |
| TC-ADM-005 | REQ-ADM-004 | UI | Admin Sales Order List Page Verification | P2 | YES | [AdminTest#testAdminSalesOrdersPage](../src/test/java/com/qa/nopcommerce/tests/ui/AdminTest.java) | PASS |
| TC-ADM-006 | REQ-ADM-003 | UI | Admin Catalog Category List Rendering | P2 | YES | [AdminTest#testAdminCatalogCategoriesPage](../src/test/java/com/qa/nopcommerce/tests/ui/AdminTest.java) | PASS |
| TC-ADM-007 | REQ-ADM-002 | UI | Admin Customer List Verification | P2 | YES | [AdminTest#testAdminCustomersPage](../src/test/java/com/qa/nopcommerce/tests/ui/AdminTest.java) | PASS |
| TC-ADM-008 | REQ-ADM-001 | UI | Admin Login Invalid Password Rejection | P2 | YES | [AdminTest#testAdminLoginInvalidPassword](../src/test/java/com/qa/nopcommerce/tests/ui/AdminTest.java) | PASS |
| TC-API-001 | REQ-API-001 | API | HTTP GET Public Route - Home Page Status | P1 | YES | [ApiEndpointTest#testStorefrontRootEndpoint](../src/test/java/com/qa/nopcommerce/tests/api/ApiEndpointTest.java) | PASS |
| TC-API-002 | REQ-API-001 | API | HTTP GET Public Route - Register Page Status | P1 | YES | [ApiEndpointTest#testCustomerRegisterEndpoint](../src/test/java/com/qa/nopcommerce/tests/api/ApiEndpointTest.java) | PASS |
| TC-API-003 | REQ-API-001 | API | HTTP GET Public Route - Login Page Status | P1 | YES | [ApiEndpointTest#testCustomerLoginEndpoint](../src/test/java/com/qa/nopcommerce/tests/api/ApiEndpointTest.java) | PASS |
| TC-API-004 | REQ-API-001 | API | HTTP GET Public Route - Cart Page Status | P1 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-API-005 | REQ-API-001 | API | HTTP GET Non-Existent Route 404 Handling | P2 | YES | [ApiEndpointTest#testNonExistentRouteHandling](../src/test/java/com/qa/nopcommerce/tests/api/ApiEndpointTest.java) | PASS |
| TC-API-006 | REQ-API-002 | API | HTTP GET Protected Route - Customer Info Redirect | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-API-007 | REQ-API-002 | API | HTTP GET Protected Route - Admin Dashboard Redirect | P2 | YES | [ApiEndpointTest#testAdminPortalEndpoint](../src/test/java/com/qa/nopcommerce/tests/api/ApiEndpointTest.java) | PASS |
| TC-API-008 | REQ-API-003 | API | Anti-Forgery Token Issuance in Registration Form | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-API-009 | REQ-API-004 | API | HTTP POST Login without Anti-Forgery Token Rejection | P2 | YES | [ApiEndpointTest#testPostWithoutAntiForgeryTokenRejection](../src/test/java/com/qa/nopcommerce/tests/api/ApiEndpointTest.java) | PASS |
| TC-API-010 | REQ-API-005 | API | Security Headers Presence on HTTP Responses | P3 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-DB-001 | REQ-DB-001 | DB | Verify Customer Persistence in Database | P1 | YES | [DatabaseVerificationTest#testCustomerRecordPersistenceInDatabase](../src/test/java/com/qa/nopcommerce/tests/db/DatabaseVerificationTest.java) | PASS |
| TC-DB-002 | REQ-DB-002 | DB | Verify Active Order and Order Item Rows in Seeded Database | P1 | YES | [DatabaseVerificationTest#testOrderRecordPersistenceInDatabase](../src/test/java/com/qa/nopcommerce/tests/db/DatabaseVerificationTest.java) | PASS |
| TC-DB-003 | REQ-DB-003 | DB | Verify Storefront Cart Item Persistence in Database | P2 | YES | [DatabaseVerificationTest#testCartItemPersistenceInDatabase](../src/test/java/com/qa/nopcommerce/tests/db/DatabaseVerificationTest.java) | PASS |
| TC-DB-004 | REQ-DB-004 | DB | Verify Database Connection Health & Query Execution | P2 | YES | [DatabaseVerificationTest#testDatabaseConnectionHealth](../src/test/java/com/qa/nopcommerce/tests/db/DatabaseVerificationTest.java) | PASS |
| TC-DB-005 | REQ-DB-002 | DB | Verify Customer Address Record Persistence | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-A11Y-001 | REQ-A11Y-001 | A11Y | Storefront Home Page Automated Accessibility Scan | P2 | YES | [AccessibilityTest#testHomePageAccessibility](../src/test/java/com/qa/nopcommerce/tests/a11y/AccessibilityTest.java) | PASS |
| TC-A11Y-002 | REQ-A11Y-001 | A11Y | Registration Page Automated Accessibility Scan | P2 | YES | [AccessibilityTest#testRegisterPageAccessibility](../src/test/java/com/qa/nopcommerce/tests/a11y/AccessibilityTest.java) | PASS |
| TC-A11Y-003 | REQ-A11Y-001 | A11Y | Login Page Automated Accessibility Scan | P2 | YES | [AccessibilityTest#testLoginPageAccessibility](../src/test/java/com/qa/nopcommerce/tests/a11y/AccessibilityTest.java) | PASS |
| TC-A11Y-004 | REQ-A11Y-001 | A11Y | Shopping Cart Page Automated Accessibility Scan | P2 | YES | [AccessibilityTest#testCartPageAccessibility](../src/test/java/com/qa/nopcommerce/tests/a11y/AccessibilityTest.java) | PASS |
| TC-A11Y-005 | REQ-A11Y-001 | A11Y | Category Desktops Page Automated Accessibility Scan | P2 | YES | [AccessibilityTest#testCategoryPageAccessibility](../src/test/java/com/qa/nopcommerce/tests/a11y/AccessibilityTest.java) | PASS |
| TC-A11Y-006 | REQ-A11Y-002 | A11Y | Checkout Billing Step Accessibility Scan | P2 | NO | Manual / Planned Scope | NOT RUN (Manual Scope) |
| TC-NFR-001 | REQ-NFR-001 | NFR | Responsive Viewport Layout Adaptation (Desktop & Mobile) | P1 | YES | [NonFunctionalTest#testResponsiveViewportLayout](../src/test/java/com/qa/nopcommerce/tests/nfr/NonFunctionalTest.java) | PASS |
| TC-NFR-002 | REQ-NFR-001 | NFR | Mobile Viewport Navigation Hamburger Menu Trigger | P2 | YES | [NonFunctionalTest#testMobileNavigationMenuTrigger](../src/test/java/com/qa/nopcommerce/tests/nfr/NonFunctionalTest.java) | PASS |
| TC-NFR-003 | REQ-NFR-002 | NFR | Storefront Page Load Performance SLA (<15000ms) | P1 | YES | [NonFunctionalTest#testPageLoadPerformanceSla](../src/test/java/com/qa/nopcommerce/tests/nfr/NonFunctionalTest.java) | PASS |
| TC-NFR-004 | REQ-NFR-003 | NFR | Verify Zero HTTP 500 Internal Server Errors on Core Assets | P2 | YES | [NonFunctionalTest#testNo500InternalServerErrorsOnAssets](../src/test/java/com/qa/nopcommerce/tests/nfr/NonFunctionalTest.java) | PASS |
