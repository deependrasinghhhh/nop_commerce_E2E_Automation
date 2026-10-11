# Defect Log & Triage Register

> **Evidence & Honesty Contract**: This log contains both authentic environmental defects discovered during real test execution against nopCommerce 4.90.8 and explicitly marked simulated defects designed to validate the defect triage lifecycle.

---

## 1. Defect Summary Table

| Defect ID | Type | Title | Severity | Priority | Status | Traceable Requirement |
|---|---|---|---|---|---|---|
| **DEF-001** | REAL | Public Storefront & Admin Intercepted by Cloudflare Turnstile Bot Challenge | Blocker | P1 | Open (Blocked by Env) | REQ-NFR-001, REQ-API-001 |
| **DEF-002** | [SIMULATED] | Order Subtotal Currency Rounding Discrepancy on Fractional Discount Coupons | Major | P2 | Under Review | REQ-CART-005, REQ-CHK-005 |
| **DEF-003** | [SIMULATED] | Address Line 2 Silent String Truncation Beyond 128 Characters in Customer Address Book | Minor | P3 | Ready for Retest | REQ-CUST-002 |

---

## 2. Granular Defect Reports

### DEF-001: Public Storefront & Admin Intercepted by Cloudflare Turnstile Bot Challenge
- **Type:** REAL / ENVIRONMENTAL BLOCKER
- **Component:** Storefront Web Gateway / Administration Gateway
- **Severity:** Blocker (S1) | **Priority:** Immediate (P1)
- **Reported By:** QA Automation Lead
- **Date Discovered:** 2026-10-11
- **Affected Endpoints:** `https://demo.nopcommerce.com`, `https://admin-demo.nopcommerce.com`
- **Steps to Reproduce:**
  1. Launch Playwright test runner in headless or standard CDP mode (`mvn test -DsuiteFile=src/test/resources/suites/smoke.xml`).
  2. Navigate to `https://demo.nopcommerce.com/`.
  3. Observe initial response headers and page document.
- **Expected Result:**
  - Storefront homepage renders with HTTP 200, title matching `nopCommerce demo store`, displaying header navigation and logo.
- **Actual Result:**
  - Cloudflare edge returns interactive Turnstile challenge (`<title>Just a moment...</title>`). Automated Playwright assertions fail on page title timeout (`received: Just a moment...`).
  - Automated HTTP probe requests (`curl -I` / `Invoke-WebRequest`) receive `HTTP/1.1 403 Forbidden`.
- **Retained Evidence Artifacts:**
  - Screenshot: `evidence/failures/testHomePageTitleAndHeaderRendering.png`
  - Screenshot: `evidence/failures/testStorefrontSearchBoxInput.png`
  - Surefire Failure Log: `evidence/surefire-reports/TEST-com.qa.nopcommerce.tests.smoke.SmokeTest.xml`
- **Mitigation / Workaround:**
  - Deploy and execute tests against local containerized nopCommerce 4.90.8 instance via `docker/docker-compose.yml` (free of public WAF layers).

---

### DEF-002: [SIMULATED] Order Subtotal Currency Rounding Discrepancy on Fractional Discount Coupons
- **Type:** [SIMULATED] (Demonstration Only)
- **Component:** Shopping Cart Calculation Engine (`Nop.Services.Orders.OrderTotalCalculationService`)
- **Severity:** Major (S2) | **Priority:** High (P2)
- **Reported By:** QA Automation Lead
- **Traceability:** REQ-CART-005, REQ-CHK-005
- **Steps to Reproduce:**
  1. Add 3 units of "Apple MacBook Pro 13-inch" ($1,800.00 each, subtotal $5,400.00).
  2. Apply promotional coupon code providing a 15.75% fractional discount.
  3. Navigate to Shopping Cart and observe Order Total before tax calculation.
- **Expected Result:**
  - Discount is rounded using Banker's Rounding (MidpointRounding.ToEven) to 2 decimal places: `$5,400.00 * 0.1575 = $850.50`, Order Subtotal = `$4,549.50`.
- **Actual Result [SIMULATED]:**
  - Order subtotal displays `$4,549.49` due to floating point truncation prior to decimal quantization.
- **Regression Test:** `CartTest.testApplyCouponCalculationAccuracy` (tagged `[SIMULATED]`).

---

### DEF-003: [SIMULATED] Address Line 2 Silent String Truncation Beyond 128 Characters in Customer Address Book
- **Type:** [SIMULATED] (Demonstration Only)
- **Component:** Customer Profile & Address Management (`Nop.Web.Controllers.CustomerController`)
- **Severity:** Minor (S3) | **Priority:** Medium (P3)
- **Reported By:** QA Automation Lead
- **Traceability:** REQ-CUST-002
- **Steps to Reproduce:**
  1. Log in as registered customer and navigate to `/customer/addressadd`.
  2. Fill Address 1 with valid address.
  3. Enter 140 alphanumeric characters into Address 2.
  4. Submit form by clicking Save.
  5. Inspect saved address in `/customer/addresses`.
- **Expected Result:**
  - Form validation informs user that Address 2 exceeds the 128-character limit, or preserves extended string without silent data loss.
- **Actual Result [SIMULATED]:**
  - Form saves without client or server validation warning, but database cuts off characters past index 128 silently.
- **Regression Test:** `CustomerAccountTest.testAddressLine2LengthValidation` (tagged `[SIMULATED]`).
