# Xray Test Execution Ticket Template

```markdown
**Project:** nopCommerce QA Engineering (NOPQA)
**Issue Type:** Test Execution
**Key:** NOPQA-TE-501
**Summary:** Automated CI/CD Regression Run - Build #<BUILD_NUMBER>

---

### Execution Details
- **Test Environment:** Docker Containerized (nopcommerce:4.90.8)
- **Execution Engine:** Maven Surefire / TestNG Runner
- **Browser:** Google Chrome (Headless)
- **Revision:** Git commit `<COMMIT_SHA>`

### Execution Summary
| Overall Status | Total Tests | Passed | Failed | Blocked / Skipped | Duration |
|---|---|---|---|---|---|
| PASS / FAIL | <count> | <pass> | <fail> | <skip> | <duration>s |

### Associated Defect Links
- Blocks: `DEF-001` (Cloudflare Turnstile challenge on demo fallback)
```
