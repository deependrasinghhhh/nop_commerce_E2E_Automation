# System Requirements Specification (SRS) — nopCommerce 4.90.8

> **Document ID:** SRS-NOP-4.90.8  
> **Status:** Baseline Approved (Self-Assessed)  
> **Target AUT:** nopCommerce 4.90.8 on ASP.NET Core (.NET 9)  
> **Scope:** Functional and Non-Functional Requirements for QA Automation Scope  

---

## 1. Requirement Taxonomy & Conventions

To ensure end-to-end traceability across the STLC, every requirement follows a strict, non-divergent identifier structure:

| Prefix | Functional Domain | Scope Summary |
|---|---|---|
| `REQ-AUTH` | Authentication & Authorization | Registration, login, logout, password recovery, session handling |
| `REQ-CAT` | Catalog & Navigation | Categories, product details, search, filtering, sorting, comparison |
| `REQ-CART` | Shopping Cart & Wishlist | Item addition, quantity modification, removal, wishlist operations |
| `REQ-CHK` | Checkout & Orders | Multi-step checkout, address management, shipping/payment selection |
| `REQ-CUST` | Customer Account & Engagement | Profile details, address book, order history, product reviews, contact us |
| `REQ-ADM` | Administrative Panel | Admin authentication, dashboard metrics, catalog viewing, order lookup |
| `REQ-API` | HTTP Protocol & Endpoints | Route status codes, headers, anti-forgery validation, form POST payloads |
| `REQ-A11Y` | Accessibility Compliance | WCAG 2.1 Level AA automated rule compliance via axe-core |
| `REQ-NFR` | Non-Functional Requirements | Performance benchmarks, cross-browser compatibility, automation stability |

### Priority Legend
* **P1 (Critical):** Core commercial path (blocker if failed; smoke test scope).
* **P2 (High):** Standard user journeys and key secondary features (regression scope).
* **P3 (Medium):** Edge cases, validation messages, and supplementary interactions.

---

## 2. Functional Requirements

### 2.1 Authentication & Authorization (`REQ-AUTH`)

| ID | Priority | Title | Description & Acceptance Criteria |
|---|---|---|---|
| `REQ-AUTH-001` | P1 | User Registration - Valid Data | The system shall allow a new user to register with valid mandatory fields (Gender, First Name, Last Name, Email, Password, Confirm Password). On success, display `"Your registration completed"` and establish an authenticated session. |
| `REQ-AUTH-002` | P2 | Registration - Mandatory Field Validation | The system shall display inline error messages (`"First name is required."`, `"Email is required."`, `"Password is required."`) when mandatory fields are omitted upon submission. |
| `REQ-AUTH-003` | P2 | Registration - Duplicate Email Prevention | The system shall reject registration attempts with an already registered email address and display `"The specified email already exists"`. |
| `REQ-AUTH-004` | P2 | Registration - Password Policy Validation | The system shall reject passwords failing complexity rules (minimum 6 characters) with an appropriate inline validation warning. |
| `REQ-AUTH-005` | P1 | User Authentication - Valid Credentials | The system shall authenticate a registered customer upon receiving valid Email and Password credentials, redirecting to the requested page or homepage and showing `"Log out"` and `"My account"` links. |
| `REQ-AUTH-006` | P1 | User Authentication - Invalid Credentials | The system shall reject authentication with invalid credentials and display an error: `"Login was unsuccessful. Please correct the errors and try again. No customer account found"` or equivalent. |
| `REQ-AUTH-007` | P2 | User Session Logout | The system shall terminate the active customer session when `"Log out"` is clicked, clearing authenticated cookies and updating the header to display `"Log in"`. |
| `REQ-AUTH-008` | P3 | Password Recovery Initiation | The system shall accept an email address on `/passwordrecovery` and display `"Email with instructions has been sent to you."` for valid registered accounts. |

---

### 2.2 Catalog & Browsing (`REQ-CAT`)

| ID | Priority | Title | Description & Acceptance Criteria |
|---|---|---|---|
| `REQ-CAT-001` | P1 | Category Hierarchy Navigation | The system shall display top-level categories (Computers, Electronics, Apparel, Digital downloads, Books, Jewelry, Gift Cards) and allow drilling down to subcategories (e.g., Computers > Desktops). |
| `REQ-CAT-002` | P1 | Product Detail Page (PDP) Rendering | The system shall render complete product specifications on the PDP, including title, short description, SKU, stock availability, unit price, and primary/thumbnail images. |
| `REQ-CAT-003` | P1 | Keyword Search - Basic & Auto-Suggest | The system shall return relevant product records when searching via the top search bar, displaying auto-suggest dropdown options as characters are typed (minimum 3 chars). |
| `REQ-CAT-004` | P2 | Advanced Search & No Results | The system shall display `"No products were found that matched your criteria."` when search queries return zero matches, providing advanced search options (category, manufacturer). |
| `REQ-CAT-005` | P2 | Product Sorting | The system shall reorder catalog listings dynamically when the `"Sort by"` dropdown is altered (Position, Name: A to Z, Name: Z to A, Price: Low to High, Price: High to Low, Created on). |
| `REQ-CAT-006` | P2 | Page Size Customization | The system shall adjust the number of visible products per page when the `"Display"` per page dropdown is toggled (e.g., 3, 6, 9 items). |
| `REQ-CAT-007` | P3 | Product Comparison List | The system shall allow users to add products to the compare list, displaying a side-by-side comparison table at `/compareproducts`. |

---

### 2.3 Shopping Cart & Wishlist (`REQ-CART`)

| ID | Priority | Title | Description & Acceptance Criteria |
|---|---|---|---|
| `REQ-CART-001` | P1 | Add Product to Cart from PDP | The system shall add a product to the cart with user-selected attributes and quantity, displaying a green confirmation notification banner (`"The product has been added to your shopping cart"`). |
| `REQ-CART-002` | P1 | Mini-Cart Flyout Counter Update | The system shall increment the header shopping cart counter (`(x)`) and display the newly added item in the flyout cart on mouse hover. |
| `REQ-CART-003` | P1 | Cart Item Quantity Modification | The system shall recalculate line totals and order subtotal dynamically when the quantity input on `/cart` is updated. |
| `REQ-CART-004` | P2 | Remove Item from Cart | The system shall remove an item from the cart upon clicking the remove button (`.remove-btn`), recalculating totals or displaying `"Your Shopping Cart is empty!"` if no items remain. |
| `REQ-CART-005` | P2 | Add Product to Wishlist | The system shall add a product to the customer wishlist upon clicking `"Add to wishlist"`, displaying a confirmation notification and updating the wishlist header badge. |
| `REQ-CART-006` | P2 | Move Wishlist Item to Shopping Cart | The system shall allow users on `/wishlist` to select items and click `"Add to cart"`, migrating selected items into the shopping cart. |
| `REQ-CART-007` | P3 | Estimate Shipping Calculation | The system shall calculate shipping fee estimates on `/cart` when Country, State, and Zip code are provided. |

---

### 2.4 Checkout & Orders (`REQ-CHK`)

| ID | Priority | Title | Description & Acceptance Criteria |
|---|---|---|---|
| `REQ-CHK-001` | P1 | Terms of Service Enactment | The system shall require user agreement to the `"Terms of service"` checkbox before allowing progression to `/checkout`. |
| `REQ-CHK-002` | P1 | Guest Checkout Flow | The system shall permit unauthenticated users to initiate checkout as a guest by providing an email and full billing address. |
| `REQ-CHK-003` | P1 | Billing Address Step Completion | The system shall validate mandatory billing address fields (Country, City, Address 1, Zip, Phone) and persist the address before unlocking subsequent checkout steps. |
| `REQ-CHK-004` | P1 | Shipping Method Selection | The system shall offer selectable shipping options (e.g., Ground, Next Day Air) and update the total order amount accordingly. |
| `REQ-CHK-005` | P1 | Payment Method Selection - Offline | The system shall support offline payment methods ("Check / Money Order") without requiring external gateway tokens. |
| `REQ-CHK-006` | P1 | Order Confirmation & Placement | The system shall present an order summary review step. Clicking `"Confirm"` shall finalize the transaction, generate a unique Order Number, and display `"Your order has been successfully processed!"`. |
| `REQ-CHK-007` | P2 | Registered User Checkout Address Reuse | The system shall prepopulate previously saved billing/shipping addresses for logged-in customers during checkout. |

---

### 2.5 Customer Account & Interactions (`REQ-CUST`)

| ID | Priority | Title | Description & Acceptance Criteria |
|---|---|---|---|
| `REQ-CUST-001` | P2 | Customer Information Management | The system shall allow an authenticated user to view and update personal profile data (First Name, Last Name, Email) on `/customer/info`. |
| `REQ-CUST-002` | P2 | Address Book Management | The system shall allow users to add, edit, and delete multiple delivery addresses on `/customer/addresses`. |
| `REQ-CUST-003` | P1 | Order History Visibility | The system shall list completed orders under `/order/history`, displaying Order Number, Order Status, Date, and Order Total with a link to detailed order information. |
| `REQ-CUST-004` | P2 | Product Review Submission | The system shall allow registered customers to submit product reviews with a title, review text, and 1–5 star rating on `/productreviews/{id}`. |
| `REQ-CUST-005` | P3 | Customer Service Contact Form | The system shall accept contact inquiries on `/contactus` (Full Name, Email, Enquiry) and display `"Your enquiry has been successfully sent to the store owner."`. |

---

### 2.6 Admin Panel Operations (`REQ-ADM`)

| ID | Priority | Title | Description & Acceptance Criteria |
|---|---|---|---|
| `REQ-ADM-001` | P1 | Admin Authentication | The system shall grant access to `/Admin` only upon validating administrator credentials; unauthorized access attempts shall redirect to `/login`. |
| `REQ-ADM-002` | P2 | Dashboard Metric Presentation | The system shall render administrative overview statistics (Sales totals, Orders volume, Registered customers) on `/Admin`. |
| `REQ-ADM-003` | P2 | Catalog Product Inspection | The system shall allow administrators to search and view product records under `/Admin/Product/List` by product name, category, and vendor. |
| `REQ-ADM-004` | P2 | Order Management Verification | The system shall display placed customer orders under `/Admin/Order/List`, reflecting matching customer email, order status, and total. |

---

### 2.7 HTTP Protocol & Endpoint Health (`REQ-API`)

> **Architectural Note:** nopCommerce core does not feature a REST API without the commercial paid plugin. The API test layer validates HTTP-level mechanics via Playwright's `APIRequestContext`.

| ID | Priority | Title | Description & Acceptance Criteria |
|---|---|---|---|
| `REQ-API-001` | P1 | Core Route Status Codes | The system shall respond with HTTP `200 OK` for primary public routes (`/`, `/register`, `/login`, `/cart`, `/contactus`, `/search`). |
| `REQ-API-002` | P2 | Protected Route Redirects | The system shall respond with HTTP `302 Found` (redirecting to `/login`) when unauthenticated requests target protected routes like `/customer/info` or `/Admin`. |
| `REQ-API-003` | P2 | Anti-Forgery Token Issuance | The system shall include an ASP.NET anti-forgery cookie and hidden `__RequestVerificationToken` in HTML response payloads for state-changing forms (`/login`, `/register`). |
| `REQ-API-004` | P2 | HTTP Form Submission Handling | The system shall process valid form POST requests with extracted verification tokens and cookies, returning appropriate HTTP redirect/success codes. |
| `REQ-API-005` | P3 | Security Headers Presence | The system shall return standard security response headers (e.g., `X-Content-Type-Options: nosniff`) across web responses. |

---

## 3. Non-Functional Requirements

### 3.1 Accessibility (`REQ-A11Y`)

| ID | Priority | Title | Description & Acceptance Criteria |
|---|---|---|---|
| `REQ-A11Y-001` | P2 | WCAG 2.1 AA Compliance - Storefront | Core storefront pages (`/`, `/register`, `/login`, `/cart`, PDP) shall be evaluated using `com.deque.html.axe-core:playwright` against WCAG 2.1 AA rule tags (`wcag2a`, `wcag2aa`). |
| `REQ-A11Y-002` | P2 | Accessibility Zero Critical Violations | Automated axe scans shall identify and assert zero critical-severity violations (color contrast, missing form labels, button names) on the main navigation and checkout landing pages. |

---

### 3.2 Non-Functional & Quality Standards (`REQ-NFR`)

| ID | Priority | Title | Description & Acceptance Criteria |
|---|---|---|---|
| `REQ-NFR-001` | P1 | Cross-Browser Compatibility | The automated test suite shall execute deterministically across **Chromium**, **Firefox**, and **WebKit** using Playwright Java browser binaries. |
| `REQ-NFR-002` | P1 | Thread-Safe Concurrency | The test framework must manage browser contexts via `ThreadLocal<Playwright>` and `ThreadLocal<BrowserContext>`, supporting parallel TestNG execution with zero race conditions. |
| `REQ-NFR-003` | P1 | Local Container Performance Baseline | The local containerized instance shall handle typical user navigation flows with median page response latency under 1500 ms, measured via k6 performance scripts. |
| `REQ-NFR-004` | P1 | Zero Hardcoded Sleep Policies | Test automation code shall strictly prohibit `Thread.sleep()`. All synchronizations must leverage Playwright's auto-waiting assertions (`PlaywrightAssertions.assertThat()`) or explicit web assertions. |
| `REQ-NFR-005` | P1 | Automatic Evidence Capture | Test execution failures must automatically capture a full-resolution screenshot and Playwright zip trace through a TestNG failure listener, attached to surefire/allure reports. |
