# Jira Defect Ticket Template (Jira / Xray Standard)

```markdown
**Project:** nopCommerce QA Engineering (NOPQA)
**Issue Type:** Bug / Defect
**Summary:** [Component] Brief, descriptive statement of failure behavior

---

### Description

#### 1. Environment & Build
- **Application Under Test:** nopCommerce 4.90.8 (.NET 9)
- **Environment:** Containerized Local (Docker) / Public Demo Fallback
- **Browser & OS:** Google Chrome 154 / Windows 11 / Linux Ubuntu 24.04
- **Database:** Microsoft SQL Server 2022
- **Automation Runner:** Java 21 LTS + Playwright Java 1.48.0 + TestNG 7.10.2

#### 2. Traceability
- **Requirement ID:** REQ-XXX-YYY
- **Associated Test Case:** TC-XXX-YYY
- **Git Commit SHA:** <commit-sha>

#### 3. Steps to Reproduce
1. Navigate to `<url>`
2. Perform action `<step 1>`
3. Perform action `<step 2>`
4. Click `<element>`

#### 4. Expected vs Actual Result
- **Expected:** Expected system state according to requirements specification.
- **Actual:** Observed incorrect state, unhandled error, or assertion failure.

#### 5. Logs & Error Stack Trace
```
<insert exception stack trace or surefire failure excerpt here>
```

#### 6. Retained Evidence Attachments
- **Screenshot:** `evidence/failures/<test-name>.png`
- **Playwright Trace Archive:** `evidence/failures/<test-name>.zip`
- **Video Recording:** `evidence/recordings/<recording>.webp`

#### 7. Severity & Priority Rationale
- **Severity:** Blocker / Major / Normal / Minor (technical impact rationale)
- **Priority:** P1 / P2 / P3 / P4 (business urgency rationale)
```
