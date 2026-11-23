# Sales Guru+ - End-to-End Verification Report

Environment: Spring Boot 3.5.6, PostgreSQL, HTTPS localhost:8443

### Result: 15 / 15 passed

| Test | Result | Actual evidence |
|---|---|---|
| Public version endpoint | PASS | HTTP 200 |
| Anonymous access rejected | PASS | HTTP 401 |
| Company list | PASS | HTTP 200; 2 companies |
| Representative list | PASS | HTTP 200 |
| Representatives filtered by company | PASS | HTTP 200; customer 1 |
| Create first product | PASS | HTTP 201; item 3 |
| Create second product | PASS | HTTP 201; item 4 |
| Create two-line invoice | PASS | HTTP 201; invoice 2 |
| Retrieve aggregated invoice | PASS | HTTP 200; 2 matching items |
| Invoice appears in list | PASS | HTTP 200; invoice 2 |
| Reject representative from another company | PASS | HTTP 400 |
| Reject zero quantity | PASS | HTTP 400 |
| Reject nonexistent product | PASS | HTTP 400 |
| Invalid requests leave invoice count unchanged | PASS | Before 2; after 2 |
| New invoice endpoint requires authentication | PASS | HTTP 401 |

