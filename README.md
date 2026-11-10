# nopCommerce 4.90.8 E2E Test Automation Framework & QA Portfolio

[![Java](https://img.shields.io/badge/Java-21%20LTS-orange.svg)](https://www.oracle.com/java/)
[![Playwright](https://img.shields.io/badge/Playwright-1.49.0-green.svg)](https://playwright.dev/java/)
[![TestNG](https://img.shields.io/badge/TestNG-7.10.2-red.svg)](https://testng.org/)
[![Allure](https://img.shields.io/badge/Allure-2.29.0-yellow.svg)](https://allurereport.org/)
[![k6](https://img.shields.io/badge/k6-0.54.0-purple.svg)](https://k6.io/)
[![Test Suite](https://img.shields.io/badge/Tests-61%2F61%20Passed-brightgreen.svg)](docs/execution-summary.md)

Production-grade Quality Engineering and End-to-End Test Automation framework targeting an isolated **nopCommerce 4.90.8** (.NET 9 + MSSQL 2022) environment.

## Reconciled Execution Summary (`2026-11-10 16:39:42 UTC`)

**Execution Window:** `2026-11-10T16:37:29Z to 2026-11-10T16:39:42Z (133.00s)`
**Target AUT:** nopCommerce 4.90.8, isolated Docker stack
**Run Result:** 61 passed, 0 failed, 0 skipped (100.0% of 61 executed automated methods).

| Metric Category | Value | Source Artifact |
|---|---|---|
| Total Test Cases Specified | 108 | `docs/test-cases.csv` |
| Automated Test Methods | 61 (56.5% of catalog) | `docs/traceability-matrix.md` |
| Executed Automated Methods | 61 (100.0%) | `evidence/surefire-reports/testng-results.xml` |
| Passed / Failed / Skipped | 61 / 0 / 0 | `docs/execution-summary.md` |
| k6 Storefront Load Benchmark | Separate run; see benchmark artifact | `evidence/performance/k6-summary.json` |

## Required Secrets & Environment Variables

No credentials or fallback passwords are stored in the repository. Supply secrets via `.env` locally or GitHub Actions / Jenkins secret stores:

- `ADMIN_EMAIL` / `ADMIN_PASSWORD`: Administrator credentials (required by `AdminTest`, `AuthTest`, `CartTest`, `DatabaseVerificationTest`)
- `DB_USER` / `DB_PASSWORD`: MSSQL database credentials (required by `docker/docker-compose.yml` and `DatabaseManager`)
- `BASE_URL` / `ADMIN_URL`: Isolated nopCommerce test instance URLs (defaults to `http://127.0.0.1:5000`; `demo.nopcommerce.com` is rejected)
- `IGNORE_HTTPS_ERRORS`: Opt-in boolean (`false` by default)
