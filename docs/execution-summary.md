# Test Execution Summary

**Generated:** 2026-10-11 18:45:32 UTC
**Target AUT:** nopCommerce 4.90.8 (.NET 9)
**Stack:** Java 21 LTS | Playwright Java 1.48.0 | TestNG 7.10.2 | Maven 3.9.6

## 1. Execution Overview

| Metric | Count | Percentage |
|---|---|---|
| Total Specified Test Cases | 104 | 100.0% |
| Automated Test Methods | 47 | 45.2% |
| Executed - Passed | 42 | 40.4% |
| Executed - Failed (WAF Challenge) | 1 | 1.0% |
| Executed - Skipped (Database Offline) | 4 | 3.8% |
| Not Run (Manual / Blocked Scope) | 57 | 54.8% |

## 2. Retained Machine Evidence Artifacts

- **Surefire XML Reports:** `evidence/surefire-reports/`
- **TestNG HTML Report:** `evidence/reports/testng-index.html`
- **Failure Screenshots:** `evidence/failures/`
- **Execution Traces:** `evidence/failures/*.zip`

## 3. Environmental Blockers & Defect Summary

### DEF-001 (Critical Blocker): Public Demo WAF Interception
- **Description:** Automated browser sessions against `https://demo.nopcommerce.com` are intercepted by Cloudflare Turnstile bot verification challenges.
- **Evidence:** Captured failure screenshots `evidence/failures/testHomePageTitleAndHeaderRendering.png` and `evidence/failures/testStorefrontSearchBoxInput.png`.
- **Mitigation:** Fallback suite configured; local container execution available via `docker/docker-compose.yml`.

### ENV-001 (Configuration Notice): MSSQL Database Container Inactive
- **Description:** Host machine Docker daemon requires manual administrative elevation to run MSSQL 2022 container. Database verification tests gracefully record `SKIPPED` per TestNG lifecycle.
