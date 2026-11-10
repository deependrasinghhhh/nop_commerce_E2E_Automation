# nopCommerce 4.90.8 E2E Test Automation Framework & QA Portfolio

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Playwright](https://img.shields.io/badge/Playwright-1.49.0-green.svg)](https://playwright.dev/java/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-red.svg)](https://testng.org/)
[![Allure](https://img.shields.io/badge/Allure-2.29.0-yellow.svg)](https://allurereport.org/)
[![k6](https://img.shields.io/badge/k6-0.54.0-purple.svg)](https://k6.io/)
[![Test Suite](https://img.shields.io/badge/Tests-61%2F61%20Passed%20(100%25)-brightgreen.svg)](docs/execution-summary.md)

Production-grade Quality Engineering and End-to-End Test Automation framework targeting an isolated **nopCommerce 4.90.8** (.NET 9 + MSSQL 2022) environment.

## Reconciled Execution Summary (`2026-11-10 00:07:24 UTC`)

| Metric Category | Value | Source Artifact |
|---|---|---|
| **Reconciled Run Timestamp** | `2026-11-10 00:07:24 UTC` (`109.15s` duration) | `evidence/surefire-reports/testng-results.xml` |
| **Total Test Cases Specified** | `108` | `docs/test-cases.csv` |
| **Automated Test Methods (1:1 Mapped)** | `61` (`56.5%` of total catalog) | `docs/traceability-matrix.md` |
| **Executed — Passed** | `61 / 61` (`100.0%` of automated scope) | `docs/execution-summary.md` |
| **Executed — Failed / Skipped** | `0 Failed / 0 Skipped` | `docs/test-closure-report.md` |
| **Storefront Smoke Gate (`SmokeTest`)** | `3 / 3 Passed` | `src/test/java/com/qa/nopcommerce/tests/smoke/SmokeTest.java` |
| **Storefront UI (`Auth`, `Catalog`, `Cart`, `Checkout`, `Customer`, `Admin`)** | `39 / 39 Passed` | `src/test/java/com/qa/nopcommerce/tests/ui/` |
| **API Endpoint Integration (`ApiEndpointTest`)** | `6 / 6 Passed` | `src/test/java/com/qa/nopcommerce/tests/api/ApiEndpointTest.java` |
| **Database Persistence (`DatabaseVerificationTest`)** | `4 / 4 Passed` | `src/test/java/com/qa/nopcommerce/tests/db/DatabaseVerificationTest.java` |
| **WCAG 2.1 AA Accessibility (`AccessibilityTest`)** | `5 / 5 Passed` | `src/test/java/com/qa/nopcommerce/tests/a11y/AccessibilityTest.java` |
| **Non-Functional & SLA (`NonFunctionalTest`)** | `4 / 4 Passed` | `src/test/java/com/qa/nopcommerce/tests/nfr/NonFunctionalTest.java` |
| **k6 Storefront Load Benchmark** | `120` iterations (`481` reqs, `960/960` checks, `p95 = 892.6ms`) | `evidence/performance/k6-summary.json` |

## Required Secrets & Environment Variables

No credentials or fallback passwords are stored in the repository. Supply secrets via `.env` locally or GitHub Actions / Jenkins secret stores:

- `ADMIN_EMAIL` / `ADMIN_PASSWORD`: Administrator credentials (required by `AdminTest`, `AuthTest`, `CartTest`, `DatabaseVerificationTest`)
- `DB_USER` / `DB_PASSWORD`: MSSQL database credentials (required by `docker/docker-compose.yml` and `DatabaseManager`)
- `BASE_URL` / `ADMIN_URL`: Isolated nopCommerce test instance URLs (defaults to `http://127.0.0.1:5000`; `demo.nopcommerce.com` is rejected)
- `IGNORE_HTTPS_ERRORS`: Opt-in boolean (`false` by default)
