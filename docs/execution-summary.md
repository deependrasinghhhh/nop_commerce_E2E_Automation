# Test Execution Summary

**Reconciled Run Timestamp:** 2026-11-10 16:39:42 UTC
**Execution Window:** 2026-11-10T16:37:29Z to 2026-11-10T16:39:42Z (133.00s)
**Target AUT:** nopCommerce 4.90.8 (.NET 9, Isolated Docker Stack `http://127.0.0.1:5000`)
**Stack:** Java 21 LTS | Playwright Java 1.49.0 | TestNG 7.10.2 | Allure 2.29.0 | Axe-core 4.10.0 | k6 0.54.0

## 1. Execution Overview

| Metric | Count | Percentage |
|---|---:|---:|
| Total Specified Test Cases | 108 | 100.0% |
| Automated Test Methods | 61 | 56.5% of catalog |
| Executed Automated Methods | 61 | 100.0% of automated |
| Passed | 61 | 100.0% of executed |
| Failed | 0 | 0.0% of executed |
| Skipped | 0 | 0.0% of executed |
| Not Executed / Manual Scope | 47 | 43.5% of catalog |

## 2. Test Class Results

| Test Class | Passed | Failed | Skipped |
|---|---:|---:|---:|
| Storefront Smoke Gate | 3 | 0 | 0 |
| Authentication & Account Lifecycle | 8 | 0 | 0 |
| Catalog Browsing, Search & Sorting | 7 | 0 | 0 |
| Shopping Cart, Wishlist & Coupons | 8 | 0 | 0 |
| Checkout Flow | 3 | 0 | 0 |
| Customer Profile, Address CRUD & Reviews | 6 | 0 | 0 |
| Administration Portal | 7 | 0 | 0 |
| API Endpoint Integration | 6 | 0 | 0 |
| Database Persistence | 4 | 0 | 0 |
| WCAG 2.1 AA Accessibility | 5 | 0 | 0 |
| Non-Functional & Responsive SLA | 4 | 0 | 0 |

## 3. Retained Evidence

- TestNG XML: `evidence/surefire-reports/testng-results.xml`
- TestNG HTML report: `evidence/surefire-reports/index.html`
- k6 benchmark: `evidence/performance/k6-summary.json` (separate load-test run)
- Accessibility results: `evidence/a11y/`

## 4. Traceability Integrity

- Duplicate test-case IDs: 0
- Orphan test-case IDs: 0
- Pass rate: 61/61 executed automated methods.
