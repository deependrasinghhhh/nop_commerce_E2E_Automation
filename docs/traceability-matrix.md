# Requirements Traceability Matrix (RTM) — nopCommerce 4.90.8

> **Document ID:** RTM-NOP-4.90.8  
> **Target Release:** nopCommerce 4.90.8  
> **Generation Method:** Initial baseline derived from `docs/requirements.md` and `docs/test-cases.csv`. Dynamic execution numbers are updated post-run by the RTM reconciliation engine.  
> **Status:** Baseline Ready (Unexecuted tests marked `NOT RUN` per Evidence Contract)  
> **Date:** October 2026  

---

## 1. Traceability Summary Metrics

| Metric | Target / Specification | Current State |
|---|---|---|
| **Total System Requirements** | 44 Requirements (`REQ-AUTH`, `REQ-CAT`, `REQ-CART`, `REQ-CHK`, `REQ-CUST`, `REQ-ADM`, `REQ-API`, `REQ-DB`, `REQ-A11Y`, `REQ-NFR`) | 44 |
| **Total Test Cases Mapped** | 80–120 Target Range | **104 Cases** |
| **Requirements Test Coverage** | 100% of Functional & Quality Requirements mapped to ≥ 1 test case | **100% Mapped** |
| **Test Execution Pass Rate** | Governed by retained Surefire XML artifacts in `evidence/` | `PENDING EXECUTION (NOT RUN)` |

---

## 2. Requirements to Test Cases Mapping

### 2.1 Authentication & Authorization (`REQ-AUTH`)

| Requirement ID | Requirement Title | Priority | Mapped Test Case IDs | Test Type | Current Status |
|---|---|---|---|---|---|
| `REQ-AUTH-001` | User Registration - Valid Data | P1 | `TC-AUTH-001` | UI | NOT RUN |
| `REQ-AUTH-002` | Registration - Mandatory Field Validation | P2 | `TC-AUTH-002`, `TC-AUTH-003`, `TC-AUTH-004`, `TC-AUTH-005` | UI | NOT RUN |
| `REQ-AUTH-003` | Registration - Duplicate Email Prevention | P2 | `TC-AUTH-006` | UI | NOT RUN |
| `REQ-AUTH-004` | Registration - Password Policy Validation | P2 | `TC-AUTH-007`, `TC-AUTH-008` | UI | NOT RUN |
| `REQ-AUTH-005` | User Authentication - Valid Credentials | P1 | `TC-AUTH-009` | UI | NOT RUN |
| `REQ-AUTH-006` | User Authentication - Invalid Credentials | P1 | `TC-AUTH-010`, `TC-AUTH-011`, `TC-AUTH-012` | UI | NOT RUN |
| `REQ-AUTH-007` | User Session Logout | P2 | `TC-AUTH-013` | UI | NOT RUN |
| `REQ-AUTH-008` | Password Recovery Initiation | P3 | `TC-AUTH-014`, `TC-AUTH-015` | UI | NOT RUN |

### 2.2 Catalog & Browsing (`REQ-CAT`)

| Requirement ID | Requirement Title | Priority | Mapped Test Case IDs | Test Type | Current Status |
|---|---|---|---|---|---|
| `REQ-CAT-001` | Category Hierarchy Navigation | P1 | `TC-CAT-001` to `TC-CAT-008` | UI | NOT RUN |
| `REQ-CAT-002` | Product Detail Page (PDP) Rendering | P1 | `TC-CAT-009`, `TC-CAT-010` | UI | NOT RUN |
| `REQ-CAT-003` | Keyword Search - Basic & Auto-Suggest | P1 | `TC-CAT-011`, `TC-CAT-012` | UI | NOT RUN |
| `REQ-CAT-004` | Advanced Search & No Results | P2 | `TC-CAT-013`, `TC-CAT-014`, `TC-CAT-015` | UI | NOT RUN |
| `REQ-CAT-005` | Product Sorting | P2 | `TC-CAT-016`, `TC-CAT-017`, `TC-CAT-018` | UI | NOT RUN |
| `REQ-CAT-006` | Page Size Customization | P2 | `TC-CAT-019` | UI | NOT RUN |
| `REQ-CAT-007` | Product Comparison List | P3 | `TC-CAT-020` | UI | NOT RUN |

### 2.3 Shopping Cart & Wishlist (`REQ-CART`)

| Requirement ID | Requirement Title | Priority | Mapped Test Case IDs | Test Type | Current Status |
|---|---|---|---|---|---|
| `REQ-CART-001` | Add Product to Cart from PDP | P1 | `TC-CART-001`, `TC-CART-002`, `TC-CART-003`, `TC-CART-013`, `TC-CART-014` | UI | NOT RUN |
| `REQ-CART-002` | Mini-Cart Flyout Counter Update | P1 | `TC-CART-004`, `TC-CART-005` | UI | NOT RUN |
| `REQ-CART-003` | Cart Item Quantity Modification | P1 | `TC-CART-006`, `TC-CART-015` | UI | NOT RUN |
| `REQ-CART-004` | Remove Item from Cart | P1 | `TC-CART-007`, `TC-CART-008` | UI | NOT RUN |
| `REQ-CART-005` | Add Product to Wishlist | P2 | `TC-CART-009`, `TC-CART-010` | UI | NOT RUN |
| `REQ-CART-006` | Move Wishlist Item to Shopping Cart | P2 | `TC-CART-011` | UI | NOT RUN |
| `REQ-CART-007` | Estimate Shipping Calculation | P3 | `TC-CART-012` | UI | NOT RUN |

### 2.4 Checkout & Orders (`REQ-CHK`)

| Requirement ID | Requirement Title | Priority | Mapped Test Case IDs | Test Type | Current Status |
|---|---|---|---|---|---|
| `REQ-CHK-001` | Terms of Service Enactment | P1 | `TC-CHK-001` | UI | NOT RUN |
| `REQ-CHK-002` | Guest Checkout Flow | P1 | `TC-CHK-002` | UI | NOT RUN |
| `REQ-CHK-003` | Billing Address Step Completion | P1 | `TC-CHK-003`, `TC-CHK-004`, `TC-CHK-005` | UI | NOT RUN |
| `REQ-CHK-004` | Shipping Method Selection | P1 | `TC-CHK-006`, `TC-CHK-007`, `TC-CHK-014` | UI | NOT RUN |
| `REQ-CHK-005` | Payment Method Selection - Offline | P1 | `TC-CHK-008`, `TC-CHK-009` | UI | NOT RUN |
| `REQ-CHK-006` | Order Confirmation & Placement | P1 | `TC-CHK-010`, `TC-CHK-012`, `TC-CHK-013`, `TC-CHK-015` | UI | NOT RUN |
| `REQ-CHK-007` | Registered User Checkout Address Reuse | P2 | `TC-CHK-011` | UI | NOT RUN |

### 2.5 Customer Account & Interactions (`REQ-CUST`)

| Requirement ID | Requirement Title | Priority | Mapped Test Case IDs | Test Type | Current Status |
|---|---|---|---|---|---|
| `REQ-CUST-001` | Customer Information Management | P2 | `TC-CUST-001`, `TC-CUST-009`, `TC-CUST-010` | UI | NOT RUN |
| `REQ-CUST-002` | Address Book Management | P2 | `TC-CUST-002`, `TC-CUST-003` | UI | NOT RUN |
| `REQ-CUST-003` | Order History Visibility | P1 | `TC-CUST-004` | UI | NOT RUN |
| `REQ-CUST-004` | Product Review Submission | P2 | `TC-CUST-005`, `TC-CUST-006` | UI | NOT RUN |
| `REQ-CUST-005` | Customer Service Contact Form | P3 | `TC-CUST-007`, `TC-CUST-008` | UI | NOT RUN |

### 2.6 Admin Panel Operations (`REQ-ADM`)

| Requirement ID | Requirement Title | Priority | Mapped Test Case IDs | Test Type | Current Status |
|---|---|---|---|---|---|
| `REQ-ADM-001` | Admin Authentication | P1 | `TC-ADM-001`, `TC-ADM-002`, `TC-ADM-008` | UI | NOT RUN |
| `REQ-ADM-002` | Dashboard Metric Presentation | P2 | `TC-ADM-003`, `TC-ADM-007` | UI | NOT RUN |
| `REQ-ADM-003` | Catalog Product Inspection | P2 | `TC-ADM-004`, `TC-ADM-006` | UI | NOT RUN |
| `REQ-ADM-004` | Order Management Verification | P2 | `TC-ADM-005` | UI | NOT RUN |

### 2.7 HTTP Protocol & Endpoint Health (`REQ-API`)

| Requirement ID | Requirement Title | Priority | Mapped Test Case IDs | Test Type | Current Status |
|---|---|---|---|---|---|
| `REQ-API-001` | Core Route Status Codes | P1 | `TC-API-001` to `TC-API-005` | API | NOT RUN |
| `REQ-API-002` | Protected Route Redirects | P2 | `TC-API-006`, `TC-API-007` | API | NOT RUN |
| `REQ-API-003` | Anti-Forgery Token Issuance | P2 | `TC-API-008` | API | NOT RUN |
| `REQ-API-004` | HTTP Form Submission Handling | P2 | `TC-API-009` | API | NOT RUN |
| `REQ-API-005` | Security Headers Presence | P3 | `TC-API-010` | API | NOT RUN |

### 2.8 Database Verification (`REQ-DB`)

| Requirement ID | Requirement Title | Priority | Mapped Test Case IDs | Test Type | Current Status |
|---|---|---|---|---|---|
| `REQ-DB-001` | Customer Entity Persistence | P1 | `TC-DB-001` | DB | NOT RUN |
| `REQ-DB-002` | Order & OrderItem Integrity | P1 | `TC-DB-002`, `TC-DB-005` | DB | NOT RUN |
| `REQ-DB-003` | Shopping Cart Item State Persistence | P2 | `TC-DB-003` | DB | NOT RUN |
| `REQ-DB-004` | Product Inventory & Stock Consistency | P2 | `TC-DB-004` | DB | NOT RUN |

### 2.9 Accessibility (`REQ-A11Y`)

| Requirement ID | Requirement Title | Priority | Mapped Test Case IDs | Test Type | Current Status |
|---|---|---|---|---|---|
| `REQ-A11Y-001` | WCAG 2.1 AA Compliance - Storefront | P2 | `TC-A11Y-001` to `TC-A11Y-005` | A11Y | NOT RUN |
| `REQ-A11Y-002` | Accessibility Zero Critical Violations | P2 | `TC-A11Y-006` | A11Y | NOT RUN |

---

## 3. Reconciliation Mechanism Note
Per Section 4 of the project charter, this document is reconciled post-test run by a verification tool that checks test execution outputs from `target/surefire-reports` against the test case IDs declared via `@TestCase("TC-...")`. No pass percentages or coverage metrics are typed manually.
