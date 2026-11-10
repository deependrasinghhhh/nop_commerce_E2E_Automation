# Test Execution Summary

**Reconciled Run Timestamp:** 2026-11-10 00:07:24 UTC  
**Execution Window:** 2026-11-10T00:02:53Z to 2026-11-10T00:04:42Z (109.15s)  
**Target AUT:** nopCommerce 4.90.8 (.NET 9, Isolated Docker Stack `http://127.0.0.1:5000`)  
**Stack:** Java 21 LTS | Playwright Java 1.49.0 | TestNG 7.10.2 | Allure 2.29.0 | Axe-core 4.10.0 | k6 0.54.0

## 1. Execution Overview

| Metric | Count | Percentage of Total Scope |
|---|---|---|
| Total Specified Test Cases (`docs/test-cases.csv`) | 108 | 100.0% |
| Automated Test Methods (`@TestCase` 1:1 Mapped) | 61 | 56.5% |
| Executed - Passed (`testng-results.xml`) | 61 | 56.5% (100.0% of automated) |
| Executed - Failed | 0 | 0.0% |
| Executed - Skipped | 0 | 0.0% |
| Not Run (Manual / Planned Catalog Scope) | 47 | 43.5% |

## 2. Retained Machine Evidence Artifacts

- **TestNG Execution XML:** `evidence/surefire-reports/testng-results.xml`
- **TestNG HTML Report:** `evidence/reports/testng-index.html`
- **k6 Load Benchmark Summary:** `evidence/performance/k6-summary.json`
- **WCAG 2.1 AA Accessibility Audits:** `evidence/a11y/`

## 3. Environmental Health & Defect Summary

- **AUT State:** Isolated containerized nopCommerce 4.90.8 and MSSQL 2022 stack (`http://127.0.0.1:5000`) verified healthy.
- **Pass Rate:** 61/61 (100.0%) automated tests passed with zero duplicate or orphan `@TestCase` mappings.
