# Accessibility Test Results — CODASH0A Screen
## Application: CardDemo
## Screen: CODASH0A (Map Set: CODASH00, Transaction: CD00)
## Test Reference: TASK-057 (Depends on: TASK-005 sign-off)

---

## Document Control

| Field | Value |
|---|---|
| Document ID | TEST-ACC-CODASH0A-001 |
| Task Reference | TASK-057 |
| Prerequisite Task | TASK-005 (Accessibility Requirements for 3270 — sign-off required) |
| Screen Under Test | CODASH0A |
| Map Set | CODASH00 |
| CICS Transaction | CD00 |
| Program | CODASH00C |
| Test Date | _(to be completed on execution)_ |
| Tester | _(Accessibility Lead — to be assigned)_ |
| Environment | _(CICS test region — to be specified)_ |
| Status | **BLOCKED — TASK-005 sign-off not yet received** |

---

## 1. Executive Summary

> ⚠️ **BLOCKED:** This test cannot be fully executed until TASK-005 (Accessibility Requirements for 3270) has been signed off. The TASK-005 sign-off document must define:
> - The applicable accessibility standard (WCAG 2.1 AA equivalent for 3270, or a named mainframe accessibility standard)
> - The required assistive technology (e.g., JAWS for Mainframe, Window-Eyes, or equivalent)
> - The specific testable criteria for 3270 terminal screens
> - The definition of "critical" and "serious" violation severity in the 3270 context
>
> This document provides the **test plan, test cases, and result template** so that execution can proceed immediately upon TASK-005 sign-off. All sections marked `[PENDING TASK-005]` must be completed or confirmed by the Accessibility Lead before test execution begins.

---

## 2. Scope

### 2.1 Screen Under Test

The CODASH0A screen is the consolidated account tiles dashboard view for the CardDemo CICS 3270 terminal application. It displays up to three account tiles (checking, savings, credit card) for the authenticated customer, each showing an account type label and a masked account identifier.

### 2.2 Fields in Scope

All fields defined in the CODASH0A map (BMS source: `app/bms/CODASH00.bms`) are in scope for this test:

| Field Name | Field Type | BMS Attribute | Purpose |
|---|---|---|---|
| CDTITLE | Protected, Normal | ATTRB=(PROT,NORM) | Screen title / header |
| CDDATE | Protected, Normal | ATTRB=(PROT,NORM) | Current date display |
| CDTIME | Protected, Normal | ATTRB=(PROT,NORM) | Current time display |
| CDUSER | Protected, Normal | ATTRB=(PROT,NORM) | Authenticated user ID |
| CDPGMNAME | Protected, Normal | ATTRB=(PROT,NORM) | Program name in header |
| CDCHKLBL | Protected, Normal | ATTRB=(PROT,NORM) | Checking tile — account type label |
| CDCHKACCT | Protected, Normal | ATTRB=(PROT,NORM) | Checking tile — masked account identifier |
| CDCHKSTAT | Protected, Normal | ATTRB=(PROT,NORM) | Checking tile — account status |
| CDSAVLBL | Protected, Normal | ATTRB=(PROT,NORM) | Savings tile — account type label |
| CDSAVACCT | Protected, Normal | ATTRB=(PROT,NORM) | Savings tile — masked account identifier |
| CDSAVSTAT | Protected, Normal | ATTRB=(PROT,NORM) | Savings tile — account status |
| CDCRDLBL | Protected, Normal | ATTRB=(PROT,NORM) | Credit card tile — account type label |
| CDCRDACCT | Protected, Normal | ATTRB=(PROT,NORM) | Credit card tile — masked account identifier |
| CDCRDSTAT | Protected, Normal | ATTRB=(PROT,NORM) | Credit card tile — account status |
| CDERRMSG | Protected, Bright | ATTRB=(PROT,BRT) | Error message field |
| CDPFKEYS | Protected, Normal | ATTRB=(PROT,NORM) | PF key legend |

### 2.3 Out of Scope

- Back-end COBOL logic (CODASH00C.cbl) — covered by TASK-058 (CAST Quality Scan)
- Performance characteristics — covered by TASK-056
- Session validation logic — covered by TASK-052
- Other CardDemo screens not modified by this feature

---

## 3. Applicable Standards and Criteria

### 3.1 Accessibility Standard

`[PENDING TASK-005]`

The applicable accessibility standard for this test is to be confirmed by the TASK-005 sign-off document. The following candidates are noted for SME review:

| Candidate Standard | Applicability to 3270 | Notes |
|---|---|---|
| WCAG 2.1 AA | Indirect — web-centric; requires translation | Specified in original requirement (AC7); SME must confirm 3270 equivalent |
| Section 508 (US) — Subpart B (Software) | Applicable to terminal software | Relevant if application is US federal or US-regulated |
| EN 301 549 (EU) | Applicable to ICT products | Relevant if application is EU-regulated |
| IBM 3270 Accessibility Guidelines | Directly applicable | IBM guidance for 3270 terminal screen design |
| JAWS for Mainframe Compatibility Checklist | Directly applicable | If JAWS for Mainframe is the specified AT |

> **SME Action Required (TASK-005):** Confirm which standard applies and provide the specific testable criteria for CODASH0A.

### 3.2 Assistive Technology

`[PENDING TASK-005]`

The assistive technology to be used for this test is to be confirmed by the TASK-005 sign-off document. Candidates:

| Assistive Technology | Version | Notes |
|---|---|---|
| JAWS for Mainframe (Freedom Scientific) | `[PENDING]` | Industry standard for 3270 screen reader |
| Window-Eyes (GW Micro) | `[PENDING]` | Alternative 3270 screen reader |
| NVDA with 3270 terminal emulator plugin | `[PENDING]` | Open-source option; confirm 3270 support |
| IBM Host Access Transformation Services (HATS) | `[PENDING]` | Web-based 3270 accessibility layer |

> **SME Action Required (TASK-005):** Specify the exact assistive technology, version, and terminal emulator to be used.

### 3.3 Acceptance Criterion 7 (Source: spec.md)

> *"Given a customer uses a screen reader or keyboard navigation, when the dashboard tiles are rendered, then all tiles are accessible, operable via keyboard with logical focus order, and announced correctly by screen readers with zero critical/serious WCAG 2.1 AA violations."*

**3270 Translation Note (per plan.md Phase 0.1):** The above criterion uses web-centric language. The mainframe-equivalent interpretation, pending TASK-005 sign-off, is:

| Web Criterion | 3270 Equivalent (Draft — Pending TASK-005 Confirmation) |
|---|---|
| "Accessible" | All tile fields are reachable by cursor/tab navigation; no field is skipped or inaccessible |
| "Operable via keyboard" | All navigation is achievable using 3270 keyboard controls (Tab, PF keys, Enter) without requiring a mouse |
| "Logical focus order" | Cursor tab order follows a left-to-right, top-to-bottom sequence consistent with other CardDemo screens (COMEN1A, COADM1A) |
| "Announced correctly by screen readers" | JAWS for Mainframe (or specified AT) announces each field label and value in the correct order; no field is skipped or misread |
| "Zero critical/serious WCAG 2.1 AA violations" | Zero violations of the severity levels defined in TASK-005 sign-off for the applicable 3270 standard |

---

## 4. Test Environment

| Item | Required Value | Actual Value |
|---|---|---|
| CICS Region | Test region (non-production) | `[TO BE COMPLETED]` |
| Terminal Emulator | `[PENDING TASK-005]` | `[TO BE COMPLETED]` |
| Assistive Technology | `[PENDING TASK-005]` | `[TO BE COMPLETED]` |
| AT Version | `[PENDING TASK-005]` | `[TO BE COMPLETED]` |
| CODASH00C Load Module Version | Post-TASK-027 build | `[TO BE COMPLETED]` |
| CODASH00 Map Set Version | Post-TASK-011 build | `[TO BE COMPLETED]` |
| Test User ID | Valid CardDemo user with all 3 account types | `[TO BE COMPLETED]` |
| Test User ID (checking only) | Valid CardDemo user with checking account only | `[TO BE COMPLETED]` |
| ACCTDAT Dataset | Test copy with known account data | `[TO BE COMPLETED]` |
| CUSTDAT Dataset | Test copy with known customer data | `[TO BE COMPLETED]` |

---

## 5. Test Cases

### TC-ACC-001: Screen Title and Header Announcement

**Objective:** Verify that the screen title and header fields are announced correctly by the specified assistive technology.

**Preconditions:**
- User is authenticated (valid COMMAREA session token)
- CD00 transaction is active
- CODASH0A screen is displayed
- Assistive technology is active

**Test Steps:**

| Step | Action | Expected Result |
|---|---|---|
| 1 | Navigate to CODASH0A screen via CD00 transaction | Screen renders without error |
| 2 | Activate assistive technology read-from-top function | AT begins reading from the first field on the screen |
| 3 | Observe AT announcement of CDTITLE field | AT announces the screen title text (e.g., "ACCOUNT DASHBOARD" or equivalent) |
| 4 | Observe AT announcement of CDDATE field | AT announces the current date value |
| 5 | Observe AT announcement of CDTIME field | AT announces the current time value |
| 6 | Observe AT announcement of CDUSER field | AT announces the authenticated user ID |
| 7 | Observe AT announcement of CDPGMNAME field | AT announces the program name |

**Pass Criteria:**
- All header fields are announced in the order they appear on screen (top-to-bottom, left-to-right)
- No header field is skipped or announced out of order
- Field content is announced accurately (no garbled or truncated text)

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

### TC-ACC-002: Checking Account Tile Announcement — Tile Present

**Objective:** Verify that the checking account tile fields are announced correctly when the customer holds a checking account.

**Preconditions:**
- Test user holds a checking account (ACCTDAT record with checking account type)
- CODASH0A screen is displayed with checking tile populated

**Test Steps:**

| Step | Action | Expected Result |
|---|---|---|
| 1 | Navigate AT focus to the checking tile section | AT moves focus to the checking tile area |
| 2 | Observe AT announcement of CDCHKLBL field | AT announces "CHECKING" or the configured account type label |
| 3 | Observe AT announcement of CDCHKACCT field | AT announces the masked account identifier (e.g., "****1234") |
| 4 | Observe AT announcement of CDCHKSTAT field | AT announces the account status value |
| 5 | Verify announcement order | Label is announced before masked account ID; status follows |

**Pass Criteria:**
- CDCHKLBL is announced as "CHECKING" (or the label defined in CODASH00.bms)
- CDCHKACCT is announced with the masked value only — no unmasked digits beyond the agreed masking format (TASK-002)
- CDCHKSTAT is announced with the correct status value
- Announcement order: label → masked account ID → status

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

### TC-ACC-003: Savings Account Tile Announcement — Tile Present

**Objective:** Verify that the savings account tile fields are announced correctly when the customer holds a savings account.

**Preconditions:**
- Test user holds a savings account (ACCTDAT record with savings account type)
- CODASH0A screen is displayed with savings tile populated

**Test Steps:**

| Step | Action | Expected Result |
|---|---|---|
| 1 | Navigate AT focus to the savings tile section | AT moves focus to the savings tile area |
| 2 | Observe AT announcement of CDSAVLBL field | AT announces "SAVINGS" or the configured account type label |
| 3 | Observe AT announcement of CDSAVACCT field | AT announces the masked account identifier |
| 4 | Observe AT announcement of CDSAVSTAT field | AT announces the account status value |
| 5 | Verify announcement order | Label → masked account ID → status |

**Pass Criteria:**
- CDSAVLBL is announced as "SAVINGS"
- CDSAVACCT is announced with masked value only
- CDSAVSTAT is announced correctly
- Announcement order is consistent with checking tile (TC-ACC-002)

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

### TC-ACC-004: Credit Card Tile Announcement — Tile Present

**Objective:** Verify that the credit card tile fields are announced correctly when the customer holds a credit card account.

**Preconditions:**
- Test user holds a credit card account (ACCTDAT record with credit card account type)
- CODASH0A screen is displayed with credit card tile populated

**Test Steps:**

| Step | Action | Expected Result |
|---|---|---|
| 1 | Navigate AT focus to the credit card tile section | AT moves focus to the credit card tile area |
| 2 | Observe AT announcement of CDCRDLBL field | AT announces "CREDIT CARD" or the configured account type label |
| 3 | Observe AT announcement of CDCRDACCT field | AT announces the masked account identifier |
| 4 | Observe AT announcement of CDCRDSTAT field | AT announces the account status value |
| 5 | Verify announcement order | Label → masked account ID → status |

**Pass Criteria:**
- CDCRDLBL is announced as "CREDIT CARD" (or configured label)
- CDCRDACCT is announced with masked value only
- CDCRDSTAT is announced correctly
- Announcement order is consistent with other tiles

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

### TC-ACC-005: Absent Tile — No Announcement for Missing Account Type

**Objective:** Verify that when a customer does not hold a particular account type, the corresponding tile fields are not announced (or are announced as blank/suppressed) and do not cause confusion.

**Preconditions:**
- Test user holds checking account only (no savings, no credit card)
- CODASH0A screen is displayed with only checking tile populated

**Test Steps:**

| Step | Action | Expected Result |
|---|---|---|
| 1 | Activate AT read-from-top | AT begins reading screen |
| 2 | Observe full AT announcement sequence | AT announces checking tile fields; does NOT announce savings or credit card tile content |
| 3 | Verify no empty/blank tile fields are announced in a confusing manner | If blank fields are present, AT announces them as blank or skips them — no misleading label or value is announced |
| 4 | Verify no error message is announced for absent tiles | CDERRMSG field is blank; AT does not announce an error for absent tiles |

**Pass Criteria:**
- Only the checking tile is announced with content
- Savings and credit card tile fields are either not present on screen or announced as blank without misleading context
- No error message is announced for absent tiles
- Consistent with acceptance criterion 2 (spec.md): "no empty or error tiles appear for savings or credit card"

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

### TC-ACC-006: All Three Tiles Present — Full Announcement Sequence

**Objective:** Verify the complete announcement sequence when all three account tiles are present.

**Preconditions:**
- Test user holds checking, savings, and credit card accounts
- CODASH0A screen is displayed with all three tiles populated

**Test Steps:**

| Step | Action | Expected Result |
|---|---|---|
| 1 | Activate AT read-from-top | AT begins reading from screen title |
| 2 | Record the complete announcement sequence | Document every field announced, in order |
| 3 | Verify announcement order matches screen layout | Top-to-bottom, left-to-right; header → checking tile → savings tile → credit card tile → PF key legend |
| 4 | Verify no field is skipped | All 14 content fields (excluding blank/suppressed) are announced |
| 5 | Verify no field is announced twice | No duplicate announcements |

**Pass Criteria:**
- Complete announcement sequence follows logical screen order
- All three tiles are announced with label, masked account ID, and status
- No field is skipped or duplicated
- PF key legend is announced at the end

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

### TC-ACC-007: Tab/Cursor Navigation Order — Consistency with Other CardDemo Screens

**Objective:** Verify that the tab/cursor navigation order on CODASH0A is logical and consistent with other CardDemo screens (COMEN1A, COADM1A).

**Preconditions:**
- CODASH0A screen is displayed
- All three tiles are populated
- Assistive technology is active

**Test Steps:**

| Step | Action | Expected Result |
|---|---|---|
| 1 | Place cursor at first field on screen | Cursor is at CDTITLE or first navigable field |
| 2 | Press Tab key | Cursor moves to next field in logical order |
| 3 | Continue pressing Tab through all fields | Cursor traverses all fields in top-to-bottom, left-to-right order |
| 4 | Record the actual tab order | Document each field visited in sequence |
| 5 | Compare tab order against COMEN1A navigation order | Verify header navigation pattern is identical |
| 6 | Compare tab order against COADM1A navigation order | Verify header navigation pattern is identical |
| 7 | Press Tab past the last field | Cursor wraps to first field or stops at last field (consistent with other screens) |

**Expected Tab Order:**
```
CDTITLE → CDDATE → CDTIME → CDUSER → CDPGMNAME →
[CDCHKLBL → CDCHKACCT → CDCHKSTAT] (if checking tile present) →
[CDSAVLBL → CDSAVACCT → CDSAVSTAT] (if savings tile present) →
[CDCRDLBL → CDCRDACCT → CDCRDSTAT] (if credit card tile present) →
CDERRMSG → CDPFKEYS
```

**Pass Criteria:**
- Tab order is strictly top-to-bottom, left-to-right
- Tab order is consistent with COMEN1A and COADM1A header navigation
- No field is skipped in the tab sequence
- No field appears out of order
- Tab wrap behaviour is consistent with other CardDemo screens

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

### TC-ACC-008: PF Key Navigation — Keyboard Operability

**Objective:** Verify that all screen functions are operable via PF keys (keyboard) without requiring a pointing device.

**Preconditions:**
- CODASH0A screen is displayed
- Assistive technology is active

**Test Steps:**

| Step | Action | Expected Result |
|---|---|---|
| 1 | Press PF3 | Screen transfers to COMEN01C (customer menu) — consistent with CardDemo PF3 = Back/Exit convention |
| 2 | Return to CODASH0A | Screen re-displays correctly |
| 3 | Press Enter | Screen refreshes or processes input — no error |
| 4 | Press PF12 | `[PENDING TASK-005 / BMS map definition]` — confirm expected PF12 behaviour |
| 5 | Verify all PF key functions listed in CDPFKEYS legend are operable | Each PF key listed in the legend performs its stated function |
| 6 | Verify no function requires a mouse or pointing device | All screen functions are achievable via keyboard alone |

**Pass Criteria:**
- PF3 navigates back to the calling menu (COMEN01C or COADM01C)
- Enter key refreshes the screen without error
- All PF keys listed in the CDPFKEYS legend are functional
- No screen function requires a pointing device
- PF key behaviour is consistent with other CardDemo screens

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

### TC-ACC-009: Error Message Announcement

**Objective:** Verify that the error message field (CDERRMSG) is announced correctly by the assistive technology when an error condition exists.

**Preconditions:**
- A condition that triggers an error message is simulated (e.g., ACCTDAT read failure)
- CODASH0A screen is displayed with CDERRMSG populated

**Test Steps:**

| Step | Action | Expected Result |
|---|---|---|
| 1 | Trigger an error condition that populates CDERRMSG | Error message is displayed in CDERRMSG field with ATTRB=(PROT,BRT) |
| 2 | Activate AT | AT reads the screen |
| 3 | Observe AT announcement of CDERRMSG | AT announces the error message text clearly |
| 4 | Verify ATTRB=(PROT,BRT) causes appropriate AT emphasis | AT indicates the field is highlighted/bright (if supported by the specified AT) |
| 5 | Verify error message is announced before PF key legend | Error message appears in logical reading order before the PF key legend |

**Pass Criteria:**
- CDERRMSG is announced when populated
- Error message text is announced accurately and completely
- Bright attribute (BRT) is reflected in AT announcement where the AT supports emphasis
- Error message position in announcement order is logical (after tile content, before PF key legend)

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

### TC-ACC-010: Masked Account Identifier — No Unmasked Data Announced

**Objective:** Verify that the assistive technology announces only the masked account identifier and does not expose unmasked account data.

**Preconditions:**
- Test user holds at least one account
- CODASH0A screen is displayed with at least one tile populated
- Masking format is confirmed per TASK-002

**Test Steps:**

| Step | Action | Expected Result |
|---|---|---|
| 1 | Navigate AT to CDCHKACCT (or CDSAVACCT / CDCRDACCT) | AT focuses on masked account identifier field |
| 2 | Observe AT announcement of the field | AT announces only the masked value (e.g., "****1234") |
| 3 | Verify no unmasked digits are announced | AT does not announce any digits beyond those permitted by the masking format |
| 4 | Verify masking format matches TASK-002 specification | `[PENDING TASK-002 sign-off]` |

**Pass Criteria:**
- AT announces only the masked account identifier
- No unmasked account digits are announced
- Masking format in the announcement matches the format agreed in TASK-002

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

### TC-ACC-011: Acceptance Criterion 7 — Composite Verification

**Objective:** Confirm that acceptance criterion 7 (spec.md) is met in its entirety, as translated to 3270 context per TASK-005 sign-off.

**Preconditions:**
- TASK-005 sign-off document is available
- All individual test cases TC-ACC-001 through TC-ACC-010 have been executed
- All three account tiles are present on screen

**Verification Checklist:**

| AC7 Sub-Criterion | Test Case(s) | Status |
|---|---|---|
| All tiles are accessible | TC-ACC-001, TC-ACC-002, TC-ACC-003, TC-ACC-004, TC-ACC-006 | `[PENDING]` |
| Operable via keyboard with logical focus order | TC-ACC-007, TC-ACC-008 | `[PENDING]` |
| Announced correctly by screen readers | TC-ACC-001 through TC-ACC-006, TC-ACC-009, TC-ACC-010 | `[PENDING]` |
| Zero critical/serious violations of applicable standard | All TCs; plus formal AT scan per TASK-005 | `[PENDING]` |

**Pass Criteria:**
- All sub-criteria above are met
- Zero critical or serious violations of the standard confirmed in TASK-005 sign-off
- All individual test cases TC-ACC-001 through TC-ACC-010 have a PASS result

**Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Findings:** _(to be completed on execution)_

---

## 6. Violation Severity Classification

`[PENDING TASK-005]`

The following severity classification is a draft, pending confirmation in the TASK-005 sign-off document. It is based on the WCAG 2.1 AA severity model translated to 3270 context:

| Severity | 3270 Definition (Draft) | Examples |
|---|---|---|
| **Critical** | A tile field is completely inaccessible to the specified AT — cannot be reached by tab navigation and is not announced under any condition | A tile section that is entirely invisible to JAWS for Mainframe due to BMS attribute configuration |
| **Serious** | A tile field is reachable but announced incorrectly or in a misleading way — e.g., label announced without associated value, or wrong field content announced | CDCHKLBL announced as blank when it contains "CHECKING"; masked account ID announced with unmasked digits |
| **Moderate** | Tab order is inconsistent with other CardDemo screens but all fields are reachable and announced correctly | Checking tile fields announced after credit card tile fields |
| **Minor** | Cosmetic announcement inconsistency that does not impede understanding | Slight variation in how BRT attribute is conveyed by AT |

**Acceptance Threshold:** Zero critical or serious violations (per acceptance criterion 7 and TASK-005 sign-off).

---

## 7. Test Results Summary

> _(To be completed after test execution)_

| Test Case | Description | Result | Severity of Findings |
|---|---|---|---|
| TC-ACC-001 | Screen title and header announcement | `[PENDING]` | `[PENDING]` |
| TC-ACC-002 | Checking tile announcement | `[PENDING]` | `[PENDING]` |
| TC-ACC-003 | Savings tile announcement | `[PENDING]` | `[PENDING]` |
| TC-ACC-004 | Credit card tile announcement | `[PENDING]` | `[PENDING]` |
| TC-ACC-005 | Absent tile — no announcement | `[PENDING]` | `[PENDING]` |
| TC-ACC-006 | All three tiles — full sequence | `[PENDING]` | `[PENDING]` |
| TC-ACC-007 | Tab/cursor navigation order | `[PENDING]` | `[PENDING]` |
| TC-ACC-008 | PF key keyboard operability | `[PENDING]` | `[PENDING]` |
| TC-ACC-009 | Error message announcement | `[PENDING]` | `[PENDING]` |
| TC-ACC-010 | Masked account ID — no unmasked data | `[PENDING]` | `[PENDING]` |
| TC-ACC-011 | AC7 composite verification | `[PENDING]` | `[PENDING]` |

**Overall Result:** `[ ] PASS  [ ] FAIL  [ ] BLOCKED`

**Critical Violations Found:** `[PENDING]`

**Serious Violations Found:** `[PENDING]`

---

## 8. Findings Log

> _(To be completed during and after test execution. Each finding must be logged here.)_

### Finding Template

```
Finding ID:     FIND-ACC-CODASH0A-NNN
Test Case:      TC-ACC-NNN
Severity:       [ ] Critical  [ ] Serious  [ ] Moderate  [ ] Minor
Field Affected: [field name]
Description:    [detailed description of the finding]
Steps to Reproduce:
  1. [step]
  2. [step]
Expected:       [expected AT behaviour]
Actual:         [actual AT behaviour]
Screenshot/Log: [attach or reference]
Recommendation: [suggested fix — e.g., BMS attribute change, field reorder]
Status:         [ ] Open  [ ] Fixed  [ ] Accepted Risk  [ ] Deferred
```

---

## 9. Comparison with Other CardDemo Screens

The following comparison is used to verify that CODASH0A navigation is consistent with existing CardDemo screens (per acceptance criterion 7 and plan.md Phase 5.4).

| Navigation Characteristic | COMEN1A (ID: 13357) | COADM1A (ID: 14185) | CODASH0A (Target) | Consistent? |
|---|---|---|---|---|
| Header field tab order | Title → Date → Time → User | Title → Date → Time → User | Title → Date → Time → User → PgmName | `[PENDING]` |
| PF3 behaviour | Back to previous screen | Back to previous screen | Back to COMEN01C | `[PENDING]` |
| Enter key behaviour | Process selection | Process selection | Refresh dashboard | `[PENDING]` |
| Error message position | Bottom of screen, before PF legend | Bottom of screen, before PF legend | Bottom of screen, before PF legend | `[PENDING]` |
| PF key legend position | Last line(s) of screen | Last line(s) of screen | Last line(s) of screen | `[PENDING]` |
| AT announcement order | Top-to-bottom, left-to-right | Top-to-bottom, left-to-right | Top-to-bottom, left-to-right | `[PENDING]` |

---

## 10. Dependencies and Blockers

| Dependency | Task | Status | Impact on This Test |
|---|---|---|---|
| Accessibility requirements for 3270 defined | TASK-005 | **BLOCKED — not signed off** | Cannot confirm applicable standard, AT, or severity thresholds |
| CODASH00.bms compiled | TASK-010, TASK-011 | `[PENDING]` | Screen must exist before testing |
| CODASH00C.cbl compiled and linked | TASK-027 | `[PENDING]` | Program must be deployed before testing |
| CD00 transaction registered in CSD | TASK-030 | `[PENDING]` | Transaction must be active before testing |
| Account masking format confirmed | TASK-002 | `[PENDING]` | TC-ACC-010 cannot be fully verified without masking spec |
| Account type field confirmed | TASK-001 | `[PENDING]` | Test data setup requires knowledge of account type field values |
| Unit tests passed | TASK-050, TASK-051 | `[PENDING]` | Accessibility testing should follow successful functional testing |

---

## 11. Sign-off

| Role | Name | Signature | Date |
|---|---|---|---|
| Accessibility Lead (Tester) | | | |
| TASK-005 Sign-off Owner | | | |
| QA Lead | | | |
| Mainframe Developer (CODASH00C) | | | |
| Product Owner | | | |

---

## 12. Revision History

| Version | Date | Author | Change |
|---|---|---|---|
| 0.1 | _(initial draft)_ | Accessibility Lead | Initial test plan created; all results BLOCKED pending TASK-005 sign-off |