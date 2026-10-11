# Application Overview — nopCommerce 4.90.8

> **Discovery Date:** 2026-10-11
> **Milestone:** 0 — Discovery
> **Author:** Project Author (self-assessed; no external reviewer)

---

## 1. Application Under Test (AUT)

| Attribute              | Value                                                                 |
|------------------------|-----------------------------------------------------------------------|
| **Application**        | nopCommerce                                                           |
| **Version**            | 4.90.8 (current stable as of 2026-10-11)                             |
| **Release Date**       | September 2026                                                        |
| **Framework**          | ASP.NET Core (.NET 9)                                                 |
| **License**            | Open-source (nopCommerce Public License v3)                           |
| **Official Site**      | https://www.nopcommerce.com/                                          |
| **Public Demo (store)**| https://demo.nopcommerce.com/                                         |
| **Public Demo (admin)**| https://admin.demo.nopcommerce.com/                                   |
| **Source Code**        | https://github.com/nopSolutions/nopCommerce                          |
| **Demo Reset Policy**  | Resets to default state approximately every hour                      |

### 1.1 Version Lineage (4.90.x)

Verified via Docker Hub API (`nopcommerceteam/nopcommerce` tags, queried 2026-10-11):

| Tag     | Pushed              | Status   |
|---------|---------------------|----------|
| 4.90.8  | 2026-09-04          | `latest` |
| 4.90.7  | 2026-08-26          | active   |
| 4.90.6  | 2026-07-08          | active   |
| 4.90.5  | 2026-06-29          | active   |
| 4.90.4  | 2026-03-17          | active   |
| 4.90.3  | 2025-12-09          | active   |
| 4.90.2  | 2025-11-24          | active   |
| 4.90.1  | 2025-10-31          | active   |

The `latest` tag on Docker Hub points to `4.90.8` (verified via identical digest `sha256:a4043d78041cdf4aa7c2a68dea613212ba5841cf3a05b23ac0240ae258906a79`).

**nopCommerce 5.0 beta** was released 2026-09-30 for community testing — it targets .NET 10 and is NOT used in this project.

---

## 2. Docker Distribution

| Attribute            | Value                                              |
|----------------------|----------------------------------------------------|
| **Docker Hub Repo**  | `nopcommerceteam/nopcommerce`                      |
| **Pinned Tag**       | `4.90.8`                                           |
| **Architectures**    | `amd64`, `arm64` (multi-arch manifest)             |
| **Image Size (amd64)**| ~244 MB                                           |
| **Base OS**          | Linux                                              |
| **Default Port**     | 80 (HTTP)                                          |
| **Publisher**        | `nopcommerceteam` (verified Docker Hub account)    |

### 2.1 Compose Strategy

nopCommerce requires a database server alongside the web container. The official image does **not** bundle a database — it must be provided separately. The nopCommerce web installation wizard handles database initialization (schema creation, seed data) on first run.

Planned `docker-compose.yml` setup:
- `nopcommerce_web`: `nopcommerceteam/nopcommerce:4.90.8` → port 80
- `nopcommerce_db`: MSSQL Server 2022 (see Section 3 for database decision)

---

## 3. Database Support

### 3.1 Verified Supported Engines

nopCommerce 4.90 uses **Linq2DB** as its ORM, which enables multi-database support. The following engines are officially supported:

| Engine         | Min Version | Supported Since | Notes                           |
|----------------|-------------|-----------------|----------------------------------|
| **MS SQL Server** | 2012+    | Always          | Default, most-tested, recommended |
| **MySQL**      | 5.7+        | v4.30           | Supported via Linq2DB            |
| **PostgreSQL** | 9.5+        | v4.40           | Supported via Linq2DB            |

### 3.2 Database Decision for This Project

**Decision: Use MS SQL Server 2022 (Developer Edition)**

Rationale:
- MSSQL is the **default and most-tested** database for nopCommerce. The installation wizard defaults to it.
- The official Docker Compose examples use `mcr.microsoft.com/mssql/server:2022-latest`.
- PostgreSQL IS supported (since v4.40), but MSSQL is the path of least friction for Docker-based QA environments. Using MSSQL avoids edge-case ORM compatibility issues that could distract from the QA automation focus of this project.
- The `database/queries/` directory in this project will contain read-only MSSQL-compatible queries.

**Docker image for DB:** `mcr.microsoft.com/mssql/server:2022-latest`

> **Correction from master prompt assumption:** The prompt suggested PostgreSQL + DBeaver. PostgreSQL IS technically supported by nopCommerce 4.90, but this project will use MSSQL as it is the default, better-tested path. DBeaver can still be used as a GUI client for MSSQL — it supports SQL Server via JDBC.

---

## 4. REST API Availability

### 4.1 Core API — NOT PRESENT

**nopCommerce does NOT ship with a built-in REST API.** The core application is a server-rendered MVC web application. There are no pre-configured REST endpoints for products, customers, orders, or other entities in the default installation.

### 4.2 Official Web API Plugin — Paid, Not Included

The nopCommerce team sells an official "Web API" plugin separately:
- ~1,100+ endpoints (frontend + backend)
- JWT authentication
- Swagger documentation at `{store-url}/api/index.html`
- Compatible with versions 4.40–4.90

**This plugin is NOT included in the free/open-source distribution and is NOT installed on the public demo site.** It is a commercial marketplace product.

### 4.3 Impact on Project API Test Scope

Since no REST API is available in the default nopCommerce installation:

| API Test Approach | Feasibility | Notes |
|-------------------|-------------|-------|
| Playwright `APIRequestContext` against nopCommerce REST endpoints | **NOT FEASIBLE** | No endpoints exist without the paid plugin |
| HTTP-level testing of form submissions | **FEASIBLE** | nopCommerce uses standard HTML form POSTs; Playwright can intercept/validate network requests |
| Testing the nopCommerce MVC routes (GET requests for pages) | **FEASIBLE** | Can validate response codes, headers, redirects |
| Postman exploratory testing of REST endpoints | **NOT FEASIBLE** | No REST API to explore |

**Honest assessment:** The "API testing" layer in this project will be limited to:
1. **HTTP request/response validation** — verifying page routes return correct status codes, content types, and headers using Playwright's `APIRequestContext`.
2. **Form submission validation** — testing login, registration, checkout forms at the HTTP level (POST requests with form data).
3. **Network interception** — using Playwright's route/request interception to validate AJAX calls the storefront makes (e.g., add-to-cart, search suggestions).

This is an honest scope reduction. The "1,100+ endpoint REST API" testing that many nopCommerce QA portfolios claim is only possible with the paid plugin — which this project does not use.

---

## 5. Storefront Modules and Features (Verified via Demo Exploration)

### 5.1 Top Navigation (Product Categories)

| Category          | Subcategories                          |
|-------------------|----------------------------------------|
| Computers         | Desktops, Notebooks, Software          |
| Electronics       | Camera & Photo, Cell Phones, Others    |
| Apparel           | Shoes, Clothing, Accessories           |
| Digital Downloads  | (flat category)                       |
| Books             | (flat category)                        |
| Jewelry           | (flat category)                        |
| Gift Cards        | (flat category)                        |

### 5.2 Header Elements

- Logo (home link)
- Search bar (with search button)
- Shopping Cart link (with item count)
- Wishlist link
- Register link
- Login link
- Currency selector
- Language selector (if multi-language configured)

### 5.3 Product Detail Page Features

- Product images (main + thumbnails)
- Product name, SKU, manufacturer
- Short description + full description
- Price (with tax display options)
- Availability status
- Quantity selector
- **Add to Cart** button
- **Add to Wishlist** button
- **Add to Compare List** button
- **Email a Friend** button
- Product reviews (with star ratings)
- Related products section
- Product specifications/attributes
- Recently viewed products

### 5.4 Customer Account Features

| Feature              | URL Pattern                        | Available |
|----------------------|------------------------------------|-----------|
| Registration         | `/register`                        | ✅        |
| Login                | `/login`                           | ✅        |
| My Account           | `/customer/info`                   | ✅        |
| Order History        | `/order/history`                   | ✅        |
| Addresses            | `/customer/addresses`              | ✅        |
| Change Password      | `/customer/changepassword`         | ✅        |
| Downloadable Products| `/customer/downloadableproducts`   | ✅        |
| Reward Points        | `/rewardpoints/history`            | ✅        |
| Wishlist              | `/wishlist`                       | ✅        |
| Compare Products     | `/compareproducts`                 | ✅        |
| Shopping Cart        | `/cart`                            | ✅        |

### 5.5 Checkout Flow

- Cart → Billing Address → Shipping Address → Shipping Method → Payment Method → Payment Info → Confirm Order → Order Completed
- Guest checkout available
- Registered user checkout available

### 5.6 Footer Sections

| Section               | Links                                                                |
|-----------------------|----------------------------------------------------------------------|
| Information           | Sitemap, Shipping & Returns, Privacy Notice, Conditions of Use, About Us, Contact Us |
| Customer Service      | Search, News, Blog, Recently Viewed Products, Compare Products, New Products |
| My Account            | Orders, Addresses, Shopping Cart, Wishlist                           |
| Follow Us             | Social media links (Facebook, Twitter, YouTube, etc.)                |

### 5.7 Additional Storefront Features

- **Search**: Full-text product search with auto-suggest
- **Product Filtering**: By price range, manufacturer, specifications
- **Product Sorting**: By position, name, price, date
- **Pagination**: Configurable products per page
- **Newsletter Subscription**: Email signup in footer
- **Community Poll**: Homepage voting widget
- **News Section**: CMS-managed news articles
- **Blog**: CMS-managed blog posts
- **Contact Us**: Contact form with CAPTCHA
- **Product Reviews**: Customer review submission with moderation
- **Recently Viewed Products**: Tracked per session
- **Product Comparison**: Side-by-side feature comparison

---

## 6. Admin Panel Modules (Verified via Demo Exploration)

### 6.1 Admin Access

| Attribute         | Value                                          |
|-------------------|------------------------------------------------|
| **Admin URL**     | `https://admin.demo.nopcommerce.com/` or `{base}/Admin` |
| **Demo Credentials** | Pre-filled on the admin demo login page    |

### 6.2 Admin Sidebar Menu Structure

| Top-Level Section      | Key Sub-Sections                                                    |
|------------------------|---------------------------------------------------------------------|
| **Dashboard**          | Sales summary, order statistics, popular search terms, reports      |
| **Catalog**            | Products, Categories, Manufacturers, Product Reviews, Product Tags, Product Attributes, Specification Attributes |
| **Sales**              | Orders, Shipments, Return Requests, Recurring Payments, Gift Cards, Shopping Carts & Wishlists |
| **Customers**          | Customers, Customer Roles, Online Customers, Vendors, Activity Log  |
| **Promotions**         | Discounts, Affiliates, Newsletter Subscribers, Campaigns            |
| **Content Management** | Topics (Pages), Message Templates, News Items, Blog Posts, Polls, Forums |
| **Configuration**      | Settings (General, Customer, Order, Shipping, Tax, Catalog, Media, etc.), Email Accounts, Stores, Countries, States, Languages, Currencies, Payment Methods, Shipping Providers, Tax Providers, External Auth, Widgets, Plugins |
| **System**             | System Info, Log, Warnings, Maintenance, Message Queue, Schedule Tasks, Templates |
| **Reports**            | Sales, Customers, Low Stock                                         |

### 6.3 Admin Dashboard Contents

- Sales statistics (today, this week, this month, this year)
- Order volume chart
- Incomplete orders count
- Popular search keywords
- Bestsellers
- Latest orders list
- nopCommerce news feed
- Common admin statistics

---

## 7. Technology Stack Verification

### 7.1 Java / Playwright / TestNG Compatibility

| Component         | Version     | Verified                    | Notes                              |
|-------------------|-------------|-----------------------------|------------------------------------|
| **Java**          | 21 LTS      | ✅ Compatible               | Playwright Java requires Java 8+; Java 21 is well-supported |
| **Playwright Java**| 1.63.0     | ✅ Latest stable (Oct 2026) | Maven: `com.microsoft.playwright:playwright:1.63.0` |
| **TestNG**        | 7.x (latest)| ✅ Compatible with Java 21  | Will pin exact version in pom.xml  |
| **Maven**         | 3.9+        | ✅ Standard build tool      | Will use Surefire plugin for test execution |

### 7.2 axe-core for Accessibility Testing

**Good news:** There IS an official axe-core Playwright Java integration maintained by Deque:
- Maven: `com.deque.html.axe-core:playwright` (latest version to be pinned)
- Provides `AxeBuilder` class that handles axe.min.js injection automatically
- No manual `page.addScriptTag()` required

This is preferable to manual JS injection. The master prompt assumed no Java binding exists — but one does exist as of the current date. We will use `com.deque.html.axe-core:playwright` instead of manual injection.

### 7.3 Allure + TestNG + AspectJ Weaver

Allure integration with TestNG requires the AspectJ weaver configured as a `-javaagent` in the `maven-surefire-plugin`. This is a **well-documented, verified approach**:

```xml
<argLine>
  -javaagent:"${settings.localRepository}/org/aspectj/aspectjweaver/${aspectj.version}/aspectjweaver-${aspectj.version}.jar"
</argLine>
```

Key versions:
- `allure-testng`: 2.29.0 (latest via `allure-bom`)
- `aspectjweaver`: 1.9.22

**Status:** Will configure in Milestone 5, verify with real generated report in Milestone 7. Until a real report is generated, Allure support status = NOT VERIFIED.

---

## 8. Scope Decisions and Honest Limitations

### 8.1 What IS In Scope

| Area                  | Scope                                                    |
|-----------------------|----------------------------------------------------------|
| **UI — Storefront**   | Registration, Login, Search, Browse, Cart, Wishlist, Compare, Checkout (guest + registered), Product Reviews, Contact Us |
| **UI — Admin**        | Login, Dashboard, Product CRUD (view/verify), Order view, Customer view, Basic settings navigation |
| **HTTP-level testing**| Page route validation, form submission responses, status codes, redirects |
| **Accessibility**     | axe-core scans on key storefront pages (homepage, product detail, cart, checkout, login, registration) |
| **Cross-browser**     | Chromium (primary), Firefox, WebKit                      |
| **Performance (k6)**  | Load testing key page routes (homepage, product pages, search) of the local Docker instance |
| **Database**          | Read-only verification queries against MSSQL (e.g., "does the order exist in the DB after checkout?") |

### 8.2 What Is NOT In Scope (With Honest Reasons)

| Area                      | Reason                                                                |
|---------------------------|-----------------------------------------------------------------------|
| REST API regression suite | No REST API in default nopCommerce; requires paid plugin              |
| Payment gateway testing   | Demo uses simulated payment methods; no real Stripe/PayPal integration to test |
| Email verification        | Would require mail server integration (MailHog/similar); deferred     |
| Multi-store testing       | Advanced feature; single-store default config is in scope             |
| Plugin testing            | Marketplace plugins are third-party; not part of core QA scope       |
| Mobile app testing        | nopCommerce is a web application; no native mobile app exists        |
| Full admin CRUD           | Admin write operations on the public demo are reset hourly; local Docker instance will allow limited admin testing |
| B2B features (RFQ, YMM)  | v4.90 enterprise features that require specific configuration        |

### 8.3 Known Risks for Test Automation

1. **Demo site instability**: The public demo resets hourly and is shared by all users. Tests should primarily target the local Docker instance, not the public demo.
2. **No REST API**: Limits the ability to set up test data programmatically. Test data creation will be done via UI automation or direct DB inserts.
3. **Checkout completion**: Checkout requires valid billing/shipping addresses and a payment method. The "Check / Money Order" payment method is available by default and doesn't require external integration.
4. **Anti-forgery tokens**: nopCommerce uses ASP.NET anti-forgery tokens on forms. HTTP-level form testing must handle `__RequestVerificationToken` extraction.
5. **MSSQL on Docker (Linux)**: SQL Server on Linux may have TLS handshake issues with the nopCommerce container. Connection strings may need `TrustServerCertificate=True` or `Encrypt=False` for local dev environments.

---

## 9. Decision Log

| # | Decision | Rationale | Date |
|---|----------|-----------|------|
| D-001 | Use nopCommerce `4.90.8` (pinned Docker tag, not `latest`) | Reproducible builds; `latest` may shift during project lifetime | 2026-10-11 |
| D-002 | Use MSSQL 2022 instead of PostgreSQL | Default, most-tested DB for nopCommerce; avoids ORM edge cases | 2026-10-11 |
| D-003 | API test scope limited to HTTP-level validation | No REST API in core nopCommerce; honest scope reduction | 2026-10-11 |
| D-004 | Use `com.deque.html.axe-core:playwright` for a11y | Official Java binding exists; better than manual JS injection | 2026-10-11 |
| D-005 | Primary test target is local Docker instance, not public demo | Public demo is shared, resets hourly, unreliable for automation | 2026-10-11 |
| D-006 | Allure support = NOT VERIFIED until Milestone 7 | Requires AspectJ weaver working in Surefire; will verify with real report | 2026-10-11 |

---

## 10. Sources and Verification

| Claim | Source | How Verified |
|-------|--------|--------------|
| nopCommerce 4.90.8 is current stable | nopcommerce.com, Wikipedia | Web search 2026-10-11 |
| Docker image `nopcommerceteam/nopcommerce:4.90.8` exists | Docker Hub API | Direct API query to `hub.docker.com/v2/repositories/nopcommerceteam/nopcommerce/tags/` |
| `latest` tag = `4.90.8` | Docker Hub API | Same digest `sha256:a4043d78...` on both tags |
| MSSQL, MySQL, PostgreSQL all supported | nopcommerce.com system requirements | Web search, official docs |
| No built-in REST API | nopcommerce.com, community docs | Web search, confirmed API plugin is separate paid product |
| Playwright Java 1.63.0 is latest | mvnrepository.com, GitHub releases | Web search 2026-10-11 |
| Playwright Java supports Java 8+ | playwright.dev docs | Web search 2026-10-11 |
| axe-core Playwright Java binding exists | github.com/dequelabs, playwright.dev | Web search 2026-10-11 |
| Storefront features | demo.nopcommerce.com | Live browser exploration 2026-10-11 |
| Admin panel features | admin.demo.nopcommerce.com | Live browser exploration 2026-10-11 |
