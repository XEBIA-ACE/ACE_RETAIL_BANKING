# Regression Test Results: COADM01C Inward Callers
## Task: TASK-055
## Application: CardDemo (CAST Snapshot: 2026-03-10T16:28)
## COADM01C CAST ID: 14029
## File: app/cbl/COADM01C.cbl

---

## 1. Test Scope and Objective

This document records the regression test results for all 15 inward callers of `COADM01C` (ID: 14029) following the menu option additions implemented in:

- **TASK-042:** Added "View Account Dashboard" option to `BUILD-MENU-OPTIONS` paragraph (ID: 13453) in `COADM01C`
- **TASK-043:** Added WHEN clause for new dashboard option in `PROCESS-ENTER-KEY` paragraph (ID: 14096) in `COADM01C`

**Objective:** Verify that all existing admin menu options retain their original option numbers, navigate to the correct programs, and that no existing caller behaviour has been broken by the menu changes introduced in TASK-042 and TASK-043.

**Test Execution Date:** 2026-03-10  
**Tester:** QA Engineer  
**Environment:** CICS Region — CardDemo (Onboarding-202603101628)  
**CAST Snapshot:** 2026-03-10T16:28  

---

## 2. Pre-Test Baseline: COADM01C Menu Options (Pre-TASK-042/043)

The following table records the menu option numbers and target programs as they existed **before** TASK-042 and TASK-043 were applied. This baseline was captured from the CAST snapshot and source review of `app/cbl/COADM01C.cbl`.

| Option # | Description | Target Program / Transaction | CAST ID |
|---|---|---|---|
| 1 | User Management — List Users | COUSR00C | 14296 |
| 2 | User Management — Add User | COUSR01C | 14354 |
| 3 | User Management — Update User | COUSR02C | 13646 |
| 4 | User Management — Delete User | COUSR03C | 14126 |
| 5 | Transaction Report — List | COTRTLIC | 13636 |
| 6 | Transaction Report — Update | COTRTUPC | 14540 |

> **Note:** The exact pre-change option count and numbering must be confirmed against the live source file at `app/cbl/COADM01C.cbl` prior to test execution. The table above reflects the callers identified by CAST MCP query #18 and the named callers in the task description. SME sign-off on the baseline option table is required before test execution begins.

---

## 3. Post-Change Baseline: COADM01C Menu Options (Post-TASK-042/043)

The following table records the menu option numbers **after** TASK-042 and TASK-043 were applied. Per the acceptance criteria and backward-compatibility standard (constitution.md §4.1), existing option numbers must not be renumbered — the new dashboard option must be appended as the next available number.

| Option # | Description | Target Program / Transaction | CAST ID | Status |
|---|---|---|---|---|
| 1 | User Management — List Users | COUSR00C | 14296 | Unchanged |
| 2 | User Management — Add User | COUSR01C | 14354 | Unchanged |
| 3 | User Management — Update User | COUSR02C | 13646 | Unchanged |
| 4 | User Management — Delete User | COUSR03C | 14126 | Unchanged |
| 5 | Transaction Report — List | COTRTLIC | 13636 | Unchanged |
| 6 | Transaction Report — Update | COTRTUPC | 14540 | Unchanged |
| 7 | View Account Dashboard | CODASH00C (via CD00) | new | **Added by TASK-042/043** |

---

## 4. Inward Callers Under Test

CAST MCP query #18 identified **15 inward callers** of `COADM01C` (ID: 14029). The following table lists all callers to be regression-tested.

| # | Caller Program | CAST ID | File | Caller Type | Test Case ID |
|---|---|---|---|---|---|
| 1 | COSGN00C | 13954 | app/cbl/COSGN00C.cbl | EXEC CICS XCTL (post-authentication transfer) | TC-055-001 |
| 2 | COTRTLIC | 13636 | app/cbl/COTRTLIC.cbl | EXEC CICS XCTL (return to admin menu) | TC-055-002 |
| 3 | COTRTUPC | 14540 | app/cbl/COTRTUPC.cbl | EXEC CICS XCTL (return to admin menu) | TC-055-003 |
| 4 | COUSR00C | 14296 | app/cbl/COUSR00C.cbl | EXEC CICS XCTL (return to admin menu) | TC-055-004 |
| 5 | COUSR01C | 14354 | app/cbl/COUSR01C.cbl | EXEC CICS XCTL (return to admin menu) | TC-055-005 |
| 6 | COUSR02C | 13646 | app/cbl/COUSR02C.cbl | EXEC CICS XCTL (return to admin menu) | TC-055-006 |
| 7 | COUSR03C | 14126 | app/cbl/COUSR03C.cbl | EXEC CICS XCTL (return to admin menu) | TC-055-007 |
| 8 | COADM01C (self — COMMON-RETURN) | 14029 | app/cbl/COADM01C.cbl | EXEC CICS RETURN TRANSID('CA00') | TC-055-008 |
| 9 | CODASH00C (PF3 return path) | new | app/cbl/CODASH00C.cbl | EXEC CICS XCTL (PF3 → back to COADM01C) | TC-055-009 |
| 10 | Caller #10 (CAST query #18 — TBD) | TBD | TBD | TBD | TC-055-010 |
| 11 | Caller #11 (CAST query #18 — TBD) | TBD | TBD | TBD | TC-055-011 |
| 12 | Caller #12 (CAST query #18 — TBD) | TBD | TBD | TBD | TC-055-012 |
| 13 | Caller #13 (CAST query #18 — TBD) | TBD | TBD | TBD | TC-055-013 |
| 14 | Caller #14 (CAST query #18 — TBD) | TBD | TBD | TBD | TC-055-014 |
| 15 | Caller #15 (CAST query #18 — TBD) | TBD | TBD | TBD | TC-055-015 |

> **Note:** Callers #10–#15 are identified as "remaining callers identified by CAST MCP query #18" per the task description. Their program names and CAST IDs must be retrieved by executing CAST MCP query #18 against the CardDemo snapshot (Onboarding-202603101628) before test execution. Rows marked TBD must be completed before the test suite is considered complete. The test suite is **blocked** on retrieval of these caller identities.

---

## 5. Test Cases

### TC-055-001: COSGN00C (ID: 13954) — Admin Sign-on Transfer

**Caller:** COSGN00C (ID: 13954), `app/cbl/COSGN00C.cbl`  
**Relationship:** After successful authentication of an admin-role user, COSGN00C executes `EXEC CICS XCTL PROGRAM('COADM01C')` to transfer control to the admin menu.  
**Risk:** If COADM01C's COMMAREA interface or entry behaviour changed, the transfer could fail or render incorrectly.

#### Test Steps

| Step | Action | Expected Result |
|---|---|---|
| 1 | Launch CICS transaction `CC00` | COSGN00C sign-on screen displayed |
| 2 | Enter valid admin-role user credentials (user ID + password) | Authentication succeeds; COSGN00C reads USRSEC (ID: 13993) |
| 3 | Press ENTER to submit credentials | COSGN00C executes XCTL to COADM01C |
| 4 | Observe COADM01C admin menu screen (COADM1A map, ID: 14185) | Admin menu displayed with all options 1–7 visible; no map error; no abend |
| 5 | Verify option 1 label and number | Option 1: "User Management — List Users" — unchanged |
| 6 | Verify option 2 label and number | Option 2: "User Management — Add User" — unchanged |
| 7 | Verify option 3 label and number | Option 3: "User Management — Update User" — unchanged |
| 8 | Verify option 4 label and number | Option 4: "User Management — Delete User" — unchanged |
| 9 | Verify option 5 label and number | Option 5: "Transaction Report — List" — unchanged |
| 10 | Verify option 6 label and number | Option 6: "Transaction Report — Update" — unchanged |
| 11 | Verify option 7 label and number | Option 7: "View Account Dashboard" — new option appended |
| 12 | Verify COMMAREA passed from COSGN00C is accepted by COADM01C | No session-expired redirect; menu renders correctly |

**Result:** PASS / FAIL / BLOCKED  
**Defects:** None  
**Notes:**

---

### TC-055-002: COTRTLIC (ID: 13636) — Return to Admin Menu

**Caller:** COTRTLIC (ID: 13636), `app/cbl/COTRTLIC.cbl`  
**Relationship:** COTRTLIC (Transaction Report — List) returns to COADM01C via PF3 or equivalent return navigation.  
**Risk:** If COADM01C's map or option numbering changed in a way that corrupts the screen on re-entry, the return path would be broken.

#### Test Steps

| Step | Action | Expected Result |
|---|---|---|
| 1 | From COADM01C admin menu, enter option 5 and press ENTER | COTRTLIC transaction report list screen displayed |
| 2 | Press PF3 (or configured return key) from COTRTLIC | EXEC CICS XCTL to COADM01C executed |
| 3 | Observe COADM01C admin menu screen | Admin menu re-displayed correctly; all options 1–7 visible; no map error |
| 4 | Verify option 5 is still "Transaction Report — List" | Option number and label unchanged |
| 5 | Verify no screen corruption or field overlap on COADM1A map | Map renders cleanly; no truncated labels; no overlapping fields |
| 6 | Verify COMMAREA is preserved across the round-trip | Session remains valid; no redirect to COSGN00C |

**Result:** PASS / FAIL / BLOCKED  
**Defects:** None  
**Notes:**

---

### TC-055-003: COTRTUPC (ID: 14540) — Return to Admin Menu

**Caller:** COTRTUPC (ID: 14540), `app/cbl/COTRTUPC.cbl`  
**Relationship:** COTRTUPC (Transaction Report — Update) returns to COADM01C via PF3 or equivalent return navigation.

#### Test Steps

| Step | Action | Expected Result |
|---|---|---|
| 1 | From COADM01C admin menu, enter option 6 and press ENTER | COTRTUPC transaction report update screen displayed |
| 2 | Press PF3 (or configured return key) from COTRTUPC | EXEC CICS XCTL to COADM01C executed |
| 3 | Observe COADM01C admin menu screen | Admin menu re-displayed correctly; all options 1–7 visible; no map error |
| 4 | Verify option 6 is still "Transaction Report — Update" | Option number and label unchanged |
| 5 | Verify no screen corruption on COADM1A map | Map renders cleanly |
| 6 | Verify COMMAREA is preserved | Session remains valid |

**Result:** PASS / FAIL / BLOCKED  
**Defects:** None  
**Notes:**

---

### TC-055-004: COUSR00C (ID: 14296) — Return to Admin Menu

**Caller:** COUSR00C (ID: 14296), `app/cbl/COUSR00C.cbl`  
**Relationship:** COUSR00C (User Management — List Users) returns to COADM01C via PF3 or equivalent return navigation.

#### Test Steps

| Step | Action | Expected Result |
|---|---|---|
| 1 | From COADM01C admin menu, enter option 1 and press ENTER | COUSR00C user list screen displayed |
| 2 | Press PF3 (or configured return key) from COUSR00C | EXEC CICS XCTL to COADM01C executed |
| 3 | Observe COADM01C admin menu screen | Admin menu re-displayed correctly; all options 1–7 visible |
| 4 | Verify option 1 is still "User Management — List Users" | Option number and label unchanged |
| 5 | Verify no screen corruption on COADM1A map | Map renders cleanly |
| 6 | Verify COMMAREA is preserved | Session remains valid |

**Result:** PASS / FAIL / BLOCKED  
**Defects:** None  
**Notes:**

---

### TC-055-005: COUSR01C (ID: 14354) — Return to Admin Menu

**Caller:** COUSR01C (ID: 14354), `app/cbl/COUSR01C.cbl`  
**Relationship:** COUSR01C (User Management — Add User) returns to COADM01C via PF3 or equivalent return navigation.

#### Test Steps

| Step | Action | Expected Result |
|---|---|---|
| 1 | From COADM01C admin menu, enter option 2 and press ENTER | COUSR01C add user screen displayed |
| 2 | Press PF3 (or configured return key) from COUSR01C | EXEC CICS XCTL to COADM01C executed |
| 3 | Observe COADM01C admin menu screen | Admin menu re-displayed correctly; all options 1–7 visible |
| 4 | Verify option 2 is still "User Management — Add User" | Option number and label unchanged |
| 5 | Verify no screen corruption on COADM1A map | Map renders cleanly |
| 6 | Verify COMMAREA is preserved | Session remains valid |

**Result:** PASS / FAIL / BLOCKED  
**Defects:** None  
**Notes:**

---

### TC-055-006: COUSR02C (ID: 13646) — Return to Admin Menu

**Caller:** COUSR02C (ID: 13646), `app/cbl/COUSR02C.cbl`  
**Relationship:** COUSR02C (User Management — Update User) returns to COADM01C via PF3 or equivalent return navigation.

#### Test Steps

| Step | Action | Expected Result |
|---|---|---|
| 1 | From COADM01C admin menu, enter option 3 and press ENTER | COUSR02C update user screen displayed |
| 2 | Press PF3 (or configured return key) from COUSR02C | EXEC CICS XCTL to COADM01C executed |
| 3 | Observe COADM01C admin menu screen | Admin menu re-displayed correctly; all options 1–7 visible |
| 4 | Verify option 3 is still "User Management — Update User" | Option number and label unchanged |
| 5 | Verify no screen corruption on COADM1A map | Map renders cleanly |
| 6 | Verify COMMAREA is preserved | Session remains valid |

**Result:** PASS / FAIL / BLOCKED  
**Defects:** None  
**Notes:**

---

### TC-055-007: COUSR03C (ID: 14126) — Return to Admin Menu

**Caller:** COUSR03C (ID: 14126), `app/cbl/COUSR03C.cbl`  
**Relationship:** COUSR03C (User Management — Delete User) returns to COADM01C via PF3 or equivalent return navigation.

#### Test Steps

| Step | Action | Expected Result |
|---|---|---|
| 1 | From COADM01C admin menu, enter option 4 and press ENTER | COUSR03C delete user screen displayed |
| 2 | Press PF3 (or configured return key) from COUSR03C | EXEC CICS XCTL to COADM01C executed |
| 3 | Observe COADM01C admin menu screen | Admin menu re-displayed correctly; all options 1–7 visible |
| 4 | Verify option 4 is still "User Management — Delete User" | Option number and label unchanged |
| 5 | Verify no screen corruption on COADM1A map | Map renders cleanly |
| 6 | Verify COMMAREA is preserved | Session remains valid |

**Result:** PASS / FAIL / BLOCKED  
**Defects:** None  
**Notes:**

---

### TC-055-008: COADM01C Self — COMMON-RETURN (CA00 Transaction Re-entry)

**Caller:** COADM01C (ID: 14029) — self-referential via `EXEC CICS RETURN TRANSID('CA00')`  
**Relationship:** COADM01C returns to itself via `COMMON-RETURN` paragraph, re-entering on the next terminal interaction under transaction `CA00` (ID: 14081).  
**Risk:** If the COMMAREA length or structure changed, the re-entry could fail COMMAREA validation.

#### Test Steps

| Step | Action | Expected Result |
|---|---|---|
| 1 | From COADM01C admin menu, press a non-option key (e.g., PF5 if not mapped) | COADM01C executes COMMON-RETURN; CICS RETURN with TRANSID('CA00') |
| 2 | Press ENTER on the admin menu without entering an option | COADM01C re-enters via CA00; menu re-displayed |
| 3 | Verify COMMAREA length is consistent across re-entry | No COMMAREA length mismatch error; no abend |
| 4 | Verify all 7 options still displayed correctly after re-entry | Options 1–7 intact; no corruption |
| 5 | Verify session token in COMMAREA is still valid after re-entry | No redirect to COSGN00C |

**Result:** PASS / FAIL / BLOCKED  
**Defects:** None  
**Notes:**

---

### TC-055-009: CODASH00C (New) — PF3 Return to COADM01C

**Caller:** CODASH00C (new program, `app/cbl/CODASH00C.cbl`)  
**Relationship:** CODASH00C is the new dashboard program added by TASK-042/043. When the user presses PF3 from the dashboard, CODASH00C executes `EXEC CICS XCTL PROGRAM('COADM01C')` to return to the admin menu.  
**Risk:** This is the primary new navigation path introduced by TASK-042/043. If the XCTL from CODASH00C to COADM01C fails, or if COADM01C does not render correctly on return, the acceptance criterion for return navigation is not met.

#### Test Steps

| Step | Action | Expected Result |
|---|---|---|
| 1 | From COADM01C admin menu, enter option 7 and press ENTER | CODASH00C dashboard screen (CODASH0A map) displayed |
| 2 | Verify dashboard screen renders without abend | CODASH0A map displayed; no CICS abend; no map error |
| 3 | Press PF3 from CODASH00C dashboard screen | CODASH00C executes EXEC CICS XCTL PROGRAM('COADM01C') |
| 4 | Observe COADM01C admin menu screen | Admin menu re-displayed correctly; all options 1–7 visible |
| 5 | Verify option 7 is "View Account Dashboard" | New option present and correctly labelled |
| 6 | Verify options 1–6 are unchanged | All pre-existing options retain original numbers and labels |
| 7 | Verify COMMAREA is passed correctly from CODASH00C to COADM01C | Session remains valid; no redirect to COSGN00C |
| 8 | Verify no screen corruption on COADM1A map after return | Map renders cleanly; no field overlap |
| 9 | Re-enter option 7 again from the returned admin menu | CODASH00C dashboard displayed again correctly |
| 10 | Verify round-trip navigation is repeatable | Multiple PF3 returns from CODASH00C all render COADM01C correctly |

**Result:** PASS / FAIL / BLOCKED  
**Defects:** None  
**Notes:** This test case directly validates the acceptance criterion: "Return navigation from CODASH00C back to COADM01C (via PF3) works correctly."

---

### TC-055-010 through TC-055-015: Remaining Callers (CAST MCP Query #18)

> **STATUS: BLOCKED — Pending CAST MCP Query #18 Execution**

The following test cases are reserved for callers #10–#15 identified by CAST MCP query #18. These test cases cannot be written or executed until the query is run against the CardDemo snapshot (Onboarding-202603101628) and the caller program names and CAST IDs are retrieved.

**Action Required:** Execute CAST MCP query #18 — "inward callers of COADM01C (ID: 14029)" — and populate the table in Section 4 with the remaining 6 caller identities. Then instantiate TC-055-010 through TC-055-015 following the same test step pattern as TC-055-002 through TC-055-007 above.

**Template for each remaining test case:**

```
### TC-055-0NN: <PROGRAM NAME> (ID: <CAST ID>) — Return to Admin Menu

**Caller:** <PROGRAM NAME> (ID: <CAST ID>), app/cbl/<PROGRAM NAME>.cbl
**Relationship:** <PROGRAM NAME> returns to COADM01C via PF3 or equivalent return navigation.

#### Test Steps
| Step | Action | Expected Result |
|---|---|---|
| 1 | From COADM01C admin menu, enter the option that launches <PROGRAM NAME> and press ENTER | <PROGRAM NAME> screen displayed |
| 2 | Press PF3 (or configured return key) from <PROGRAM NAME> | EXEC CICS XCTL to COADM01C executed |
| 3 | Observe COADM01C admin menu screen | Admin menu re-displayed correctly; all options 1–7 visible |
| 4 | Verify the option that launched <PROGRAM NAME> retains its original number | Option number and label unchanged |
| 5 | Verify no screen corruption on COADM1A map | Map renders cleanly |
| 6 | Verify COMMAREA is preserved | Session remains valid |

Result: BLOCKED (pending CAST MCP query #18)
```

| Test Case | Caller | CAST ID | Status |
|---|---|---|---|
| TC-055-010 | TBD | TBD | BLOCKED — pending CAST MCP query #18 |
| TC-055-011 | TBD | TBD | BLOCKED — pending CAST MCP query #18 |
| TC-055-012 | TBD | TBD | BLOCKED — pending CAST MCP query #18 |
| TC-055-013 | TBD | TBD | BLOCKED — pending CAST MCP query #18 |
| TC-055-014 | TBD | TBD | BLOCKED — pending CAST MCP query #18 |
| TC-055-015 | TBD | TBD | BLOCKED — pending CAST MCP query #18 |

---

## 6. Cross-Cutting Regression Checks

The following checks apply across **all 15 callers** and must be verified for each test case, regardless of the specific caller under test.

### 6.1 Menu Option Number Integrity

| Check | Verification Method | Expected Result |
|---|---|---|
| Option 1 number unchanged | Visual inspection of COADM1A map | "1" displayed next to "User Management — List Users" |
| Option 2 number unchanged | Visual inspection of COADM1A map | "2" displayed next to "User Management — Add User" |
| Option 3 number unchanged | Visual inspection of COADM1A map | "3" displayed next to "User Management — Update User" |
| Option 4 number unchanged | Visual inspection of COADM1A map | "4" displayed next to "User Management — Delete User" |
| Option 5 number unchanged | Visual inspection of COADM1A map | "5" displayed next to "Transaction Report — List" |
| Option 6 number unchanged | Visual inspection of COADM1A map | "6" displayed next to "Transaction Report — Update" |
| Option 7 is new dashboard option | Visual inspection of COADM1A map | "7" displayed next to "View Account Dashboard" |
| No option numbers skipped | Visual inspection of COADM1A map | Options 1–7 contiguous; no gaps |
| No option numbers duplicated | Visual inspection of COADM1A map | Each number appears exactly once |

### 6.2 Navigation Integrity

| Check | Verification Method | Expected Result |
|---|---|---|
| Option 1 → COUSR00C | Enter "1", press ENTER | COUSR00C screen displayed; no abend |
| Option 2 → COUSR01C | Enter "2", press ENTER | COUSR01C screen displayed; no abend |
| Option 3 → COUSR02C | Enter "3", press ENTER | COUSR02C screen displayed; no abend |
| Option 4 → COUSR03C | Enter "4", press ENTER | COUSR03C screen displayed; no abend |
| Option 5 → COTRTLIC | Enter "5", press ENTER | COTRTLIC screen displayed; no abend |
| Option 6 → COTRTUPC | Enter "6", press ENTER | COTRTUPC screen displayed; no abend |
| Option 7 → CODASH00C | Enter "7", press ENTER | CODASH00C dashboard screen displayed; no abend |
| Invalid option → error message | Enter "8" or "0", press ENTER | Error message displayed; no abend; no unintended navigation |
| PF3 from any sub-program → COADM01C | Press PF3 from each sub-program | COADM01C admin menu re-displayed correctly |

### 6.3 COMMAREA Integrity

| Check | Verification Method | Expected Result |
|---|---|---|
| COMMAREA length consistent | CICS trace / EIBCALEN check | EIBCALEN matches expected COMMAREA length on every re-entry |
| Session token valid after round-trip | No redirect to COSGN00C | Admin menu displayed without sign-on prompt |
| User ID preserved in COMMAREA | Verify user ID displayed on COADM1A header | Correct user ID shown after return from any sub-program |

### 6.4 Map Rendering Integrity

| Check | Verification Method | Expected Result |
|---|---|---|
| COADM1A map renders without BMS error | CICS message area | No BMS error code in EIBRESP |
| No field truncation on COADM1A | Visual inspection | All option labels fully visible; no truncated text |
| No field overlap on COADM1A | Visual inspection | New option 7 does not overlap existing fields |
| PF key legend unchanged | Visual inspection of COADM1A footer | PF key assignments identical to pre-change baseline |
| Screen title unchanged | Visual inspection of COADM1A header | Title field content and position unchanged |

### 6.5 Error Handling

| Check | Verification Method | Expected Result |
|---|---|---|
| CICS RESP checked after XCTL | Code review of COADM01C PROCESS-ENTER-KEY (ID: 14096) | RESP clause present on all EXEC CICS XCTL statements |
| Invalid option handled gracefully | Enter out-of-range option number | Error message displayed; no abend; COADM01C remains active |
| Unauthenticated access redirected | Access CA00 without valid COMMAREA | Redirect to COSGN00C (ID: 13954); no menu displayed |

---

## 7. Test Execution Summary

### 7.1 Results by Test Case

| Test Case | Caller | CAST ID | Result | Defect ID | Notes |
|---|---|---|---|---|---|
| TC-055-001 | COSGN00C | 13954 | PENDING | — | — |
| TC-055-002 | COTRTLIC | 13636 | PENDING | — | — |
| TC-055-003 | COTRTUPC | 14540 | PENDING | — | — |
| TC-055-004 | COUSR00C | 14296 | PENDING | — | — |
| TC-055-005 | COUSR01C | 14354 | PENDING | — | — |
| TC-055-006 | COUSR02C | 13646 | PENDING | — | — |
| TC-055-007 | COUSR03C | 14126 | PENDING | — | — |
| TC-055-008 | COADM01C (self) | 14029 | PENDING | — | — |
| TC-055-009 | CODASH00C (PF3 return) | new | PENDING | — | — |
| TC-055-010 | TBD | TBD | BLOCKED | — | Pending CAST MCP query #18 |
| TC-055-011 | TBD | TBD | BLOCKED | — | Pending CAST MCP query #18 |
| TC-055-012 | TBD | TBD | BLOCKED | — | Pending CAST MCP query #18 |
| TC-055-013 | TBD | TBD | BLOCKED | — | Pending CAST MCP query #18 |
| TC-055-014 | TBD | TBD | BLOCKED | — | Pending CAST MCP query #18 |
| TC-055-015 | TBD | TBD | BLOCKED | — | Pending CAST MCP query #18 |

### 7.2 Acceptance Criteria Coverage

| Acceptance Criterion | Covered By | Status |
|---|---|---|
| All 15 inward callers of COADM01C are tested | TC-055-001 through TC-055-015 | PARTIALLY BLOCKED (TC-055-010 to TC-055-015 blocked on CAST MCP query #18) |
| All existing menu options retain original option numbers | §6.1 cross-cutting checks; all test cases steps 3–5 | PENDING |
| All existing menu options navigate to correct programs | §6.2 navigation integrity checks | PENDING |
| No existing caller behaviour broken by menu changes | All test cases; §6.3 COMMAREA integrity | PENDING |
| Return navigation from CODASH00C back to COADM01C (via PF3) works correctly | TC-055-009 steps 3–10 | PENDING |
| Test results documented in docs/test-results/regression-coadm01c-callers.md | This document | IN PROGRESS |

### 7.3 Overall Test Suite Status

| Metric | Value |
|---|---|
| Total test cases | 15 |
| Executed | 0 |
| Passed | 0 |
| Failed | 0 |
| Blocked | 6 (TC-055-010 to TC-055-015) |
| Pending execution | 9 (TC-055-001 to TC-055-009) |

**Overall Status: BLOCKED — Cannot achieve 100% coverage until CAST MCP query #18 is executed and callers #10–#15 are identified.**

---

## 8. Defect Log

No defects raised at time of document creation. Defects will be logged here as test execution proceeds.

| Defect ID | Test Case | Severity | Description | Status | Resolution |
|---|---|---|---|---|---|
| — | — | — | — | — | — |

---

## 9. Blockers and Open Items

| # | Item | Owner | Priority | Status |
|---|---|---|---|---|
| B-001 | Execute CAST MCP query #18 to identify callers #10–#15 of COADM01C (ID: 14029) | Mainframe Developer / CAST Administrator | HIGH — blocks TC-055-010 to TC-055-015 | OPEN |
| B-002 | Confirm pre-change baseline option numbers from live source of app/cbl/COADM01C.cbl | QA Engineer + Mainframe Developer | HIGH — required before any test execution | OPEN |
| B-003 | Confirm TASK-042 and TASK-043 have been applied to app/cbl/COADM01C.cbl in the test environment | QA Engineer | HIGH — prerequisite for all test cases | OPEN |
| B-004 | Confirm CODASH00C load module is installed in CICS load library (TASK-027 complete) | Mainframe Build Engineer | HIGH — required for TC-055-009 | OPEN |
| B-005 | Confirm CD00 transaction is registered in CARDDEMO.CSD (TASK-030 complete) | CICS Systems Programmer | HIGH — required for TC-055-009 | OPEN |

---

## 10. Sign-off

| Role | Name | Signature | Date |
|---|---|---|---|
| QA Engineer (Test Author) | | | |
| Mainframe Developer (TASK-042/043 Implementer) | | | |
| QA Lead (Test Approver) | | | |
| Release Manager | | | |

---

## 11. Document History

| Version | Date | Author | Change |
|---|---|---|---|
| 0.1 | 2026-03-10 | QA Engineer | Initial draft — test cases TC-055-001 to TC-055-009 written; TC-055-010 to TC-055-015 blocked pending CAST MCP query #18 |