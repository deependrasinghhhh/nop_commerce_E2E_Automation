# Jira / Xray Test Plan Ticket Template

```markdown
**Project:** nopCommerce QA Engineering (NOPQA)
**Issue Type:** Test Plan
**Key:** NOPQA-TP-101
**Summary:** [Release 4.90.8] End-to-End Test Plan & Regression Scope

---

### 1. Scope & Objective
Validation of core e-commerce capabilities across nopCommerce 4.90.8 storefront, checkout, and administration modules.

### 2. Associated Test Sets
- `NOPQA-TS-01`: Authentication & Account Lifecycle (`REQ-AUTH`)
- `NOPQA-TS-02`: Catalog Browsing & Search (`REQ-CAT`)
- `NOPQA-TS-03`: Shopping Cart Operations (`REQ-CART`)
- `NOPQA-TS-04`: Checkout & Order Processing (`REQ-CHK`)
- `NOPQA-TS-05`: Administration Portal Management (`REQ-ADM`)
- `NOPQA-TS-06`: Accessibility Compliance (WCAG 2.1 AA) (`REQ-A11Y`)
- `NOPQA-TS-07`: Database Persistence Verification (`REQ-DB`)

### 3. Execution Gates
- **Entry Gate:** Docker container healthy on port 5000, MSSQL 2022 seeded, smoke tests pass 100%.
- **Exit Gate:** 0 Blocker/Critical defects open, all P1 test cases executed.
```
