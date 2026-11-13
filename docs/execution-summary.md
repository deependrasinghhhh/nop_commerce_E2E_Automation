# Test Execution Summary

**Reconciled Run Timestamp:** 2026-11-13 10:52:44 UTC
**Execution Window:** 2026-11-13T10:51:46Z to 2026-11-13T10:52:44Z (58.00s)
**Target AUT:** nopCommerce 4.90.8 (.NET 9, Isolated Docker Stack `http://127.0.0.1:5000`)
**Stack:** Java 21 | Playwright Java 1.49.0 | TestNG 7.10.2 | Allure 2.29.0 | Axe Playwright 4.6.0 | k6 v0.54.0

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

## 3. Scope and Gate Notes

The 61 automated methods are mapped from a 108-case catalog; the remaining catalog cases are not automated and this run does not represent 100% planned-case coverage.
GitHub Actions runs the smoke and API suites, not every UI, database, accessibility, and NFR test.
A successful Jenkins dispatch confirms request acceptance only; the downstream Jenkins build result must be checked separately.
The axe scan threshold defaults to serious impact; serious and critical findings fail the scan.
Accessibility methods: 5 passed, 0 failed, 0 skipped in this TestNG report; scan details are retained in the configured axe reports.
Performance evidence: k6 v0.54.0 separate HTTP browse/search workload: 25 peak VUs for 110 seconds, 481 requests, p95 892.6 ms; not a browser checkout workload or current CI gate.

## 4. Retained Evidence

- TestNG XML: `evidence/surefire-reports/testng-results.xml`
- TestNG HTML report: `evidence/surefire-reports/index.html`
- k6 benchmark: `evidence/performance/k6-summary.json`
- Accessibility results: `evidence/a11y/`

## 5. Traceability Integrity

- Duplicate test-case IDs: 0
- Orphan test-case IDs: 0
- Pass rate: 61/61 executed automated methods.
