# Accessibility Requirements: CODASH0A Screen (3270 Terminal Interface)
## CardDemo — Consolidated Account Tiles Dashboard

---

## Document Control

| Field | Value |
|---|---|
| Document ID | ACC-REQ-001 |
| Feature | Consolidated Account Tiles Dashboard (CODASH0A) |
| Application | CardDemo (CICS 3270 Terminal) |
| Status | **APPROVED** |
| Version | 1.0 |
| Date of Sign-off | 2026-03-10 |
| Accessibility Lead | J. Hartwell, Accessibility Lead — Digital Channels & Mainframe |
| Reviewed By | M. Okonkwo (QA Lead), P. Srinivasan (Mainframe Architect) |
| Feeds Into | TASK-057 (Accessibility Test) |

---

## 1. Applicability Determination: WCAG 2.1 AA vs. Mainframe Standard

### 1.1 Decision

**WCAG 2.1 AA does NOT directly apply to the CODASH0A 3270 terminal screen.**

WCAG 2.1 AA is a web content accessibility standard published by the W3C. Its success criteria are defined in terms of HTML/DOM constructs, browser rendering, and web-based assistive technology APIs (e.g., ARIA, focus management via CSS/JavaScript). The CardDemo application is a **CICS 3270 terminal application** running on IBM z/OS, rendered via IBM 3270 data stream protocol to terminal emulators. There is no DOM, no browser, and no web rendering pipeline.

Acceptance criterion AC7 in the feature specification (spec.md §5) references WCAG 2.1 AA and "screen readers" in a web-centric framing. This document resolves that ambiguity for the 3270 context.

### 1.2 Applicable Standard

The applicable accessibility standard for the CODASH0A screen is:

**JAWS for Mainframe (Freedom Scientific) compatibility**, governed by the following reference framework:

| Reference | Description |
|---|---|
| IBM 3270 Data Stream Programmer's Reference | Defines field attributes (protected, unprotected, intensified, hidden) that assistive technology interprets |
| Section 508 (36 CFR Part 1194) — Functional Performance Criteria | US federal accessibility law; applies to information technology used in federal/regulated contexts; technology-neutral criteria map to 3270 |
| EN 301 549 (European Accessibility Standard) — Clause 11 (Software) | EU equivalent; technology-neutral software accessibility criteria applicable to terminal applications |
| JAWS for Mainframe Compatibility Guidelines (Freedom Scientific) | Vendor-specific guidance for 3270 screen reader field announcement, tab order, and attribute interpretation |

### 1.3 Rationale

The 3270 data stream provides accessibility primitives that are analogous — but not identical — to web accessibility primitives:

| Web / WCAG Concept | 3270 / Mainframe Equivalent |
|---|---|
| Semantic HTML labels (`<label>`) | BMS field labels (ATTRB=PROT) immediately preceding input/display fields |
| Focus order (tab index) | 3270 tab order determined by field sequence in BMS map definition |
| Screen reader announcement | JAWS for Mainframe reads field content in tab order; field attributes control announcement |
| Colour contrast | 3270 extended attributes: HILIGHT (NORM, BLINK, REVERSE, UNDERLINE); colour attributes (GREEN, RED, BLUE, etc.) |
| Hidden / decorative content | ATTRB=DRK (dark/non-display) suppresses field from screen reader |
| Error identification | Intensified field attribute (ATTRB=BRT) for error messages |

---

## 2. Testable Acceptance Criteria for CODASH0A

The following criteria replace the web-centric AC7 from spec.md §5 for the 3270 context. All criteria are testable using the tooling specified in §3.

### 2.1 Field Announcement by JAWS for Mainframe

| ID | Criterion | Pass Condition | Fail Condition |
|---|---|---|---|
| ACC-01 | Each account tile label field (CHECKING-LABEL, SAVINGS-LABEL, CREDITCARD-LABEL) must be announced by JAWS for Mainframe when focus enters the tile region | JAWS reads the label text (e.g., "CHECKING ACCOUNT") before reading the masked account identifier field | JAWS skips the label or reads it after the account identifier |
| ACC-02 | The masked account identifier field for each tile must be announced with its full masked value (e.g., "****1234") | JAWS reads the complete masked string including masking characters | JAWS reads only partial content or omits masking characters |
| ACC-03 | The screen title / header field must be the first element announced when the CODASH0A screen is displayed | JAWS announces the screen title before any tile content | Screen title is skipped or announced out of order |
| ACC-04 | The error message field (ERR-MSG, ATTRB=BRT) must be announced by JAWS when it contains a non-blank value | JAWS reads the error message text when present | Error message is not announced or is read after tile content |
| ACC-05 | The PF key legend must be announced when focus reaches the bottom of the screen | JAWS reads PF key assignments (e.g., "PF3=Return PF12=Cancel") | PF key legend is skipped |
| ACC-06 | Fields with ATTRB=DRK (non-display / suppressed tiles) must NOT be announced by JAWS | JAWS produces no output for suppressed tile fields | JAWS announces blank or suppressed tile fields |

### 2.2 Tab Order

| ID | Criterion | Pass Condition | Fail Condition |
|---|---|---|---|
| ACC-07 | Tab order on CODASH0A must follow a logical top-to-bottom, left-to-right sequence consistent with the visual layout of the screen | Pressing Tab moves focus from screen title → tile 1 label → tile 1 account ID → tile 2 label (if present) → tile 2 account ID (if present) → tile 3 label (if present) → tile 3 account ID (if present) → PF key legend | Tab order skips tiles, reverses direction, or lands on suppressed fields |
| ACC-08 | Tab must not land on suppressed (non-displayed) tile fields | When a customer has only one account type, Tab skips the fields for absent tile types | Tab lands on blank/suppressed fields, causing JAWS to announce empty content |
| ACC-09 | Tab must not land on protected label fields that are purely decorative (i.e., fields that are ATTRB=PROT,NORM and serve only as static labels) unless they carry meaningful content | Static separator lines and decorative characters are skipped by Tab | Tab lands on decorative fields, interrupting logical navigation flow |

### 2.3 Field Attribute Requirements (BMS Map — CODASH00.bms)

These criteria are verified at the BMS map source level (TASK-010) and confirmed during accessibility testing (TASK-057):

| ID | Criterion | Required BMS Attribute | Rationale |
|---|---|---|---|
| ACC-10 | Account tile label fields must be protected and non-intensified | ATTRB=(PROT,NORM) | Prevents user modification; JAWS announces as informational text |
| ACC-11 | Masked account identifier fields must be protected and non-intensified | ATTRB=(PROT,NORM) | Read-only display; JAWS announces value |
| ACC-12 | Error message field must be protected and intensified | ATTRB=(PROT,BRT) | Intensified attribute signals importance; JAWS for Mainframe treats BRT fields as high-priority announcements |
| ACC-13 | No field on CODASH0A may use ATTRB=DRK for content that is required to be read by the user | ATTRB=DRK is reserved for suppressed/absent tile fields only | Prevents content from being hidden from assistive technology |
| ACC-14 | Screen title field must use a consistent colour attribute (GREEN or WHITE) matching existing CardDemo screens (COMEN1A, COADM1A) | COLOR=GREEN (or equivalent per site standard) | Consistent colour scheme supports users with low vision using colour-capable 3270 emulators |

### 2.4 Keyboard Operability

| ID | Criterion | Pass Condition | Fail Condition |
|---|---|---|---|
| ACC-15 | All navigation actions available on CODASH0A must be operable via PF keys without a pointing device | PF3 (Return to menu) and PF12 (Cancel/Exit) are functional and announced in the PF key legend | Any action requires mouse or non-keyboard input |
| ACC-16 | PF key assignments on CODASH0A must be consistent with existing CardDemo screens | PF3=Return, PF12=Cancel (matching COMEN1A and COADM1A conventions) | PF key assignments differ from site standard, causing user confusion |

### 2.5 Content Equivalence

| ID | Criterion | Pass Condition | Fail Condition |
|---|---|---|---|
| ACC-17 | Account type information must be conveyed by text label, not by screen position alone | Each tile contains an explicit text label (e.g., "CHECKING", "SAVINGS", "CREDIT CARD") as a protected field | Account type is indicated only by column position or colour, with no text label |
| ACC-18 | Masking characters must be consistent and unambiguous | Masked account identifier uses a consistent character (e.g., asterisk `*`) for all masked positions, with the last 4 digits visible (per TASK-002 masking specification) | Masking uses spaces, dashes, or inconsistent characters that JAWS may misread |

---

## 3. Tooling and Testing Method

### 3.1 Primary Testing Tool

| Tool | Version | Purpose |
|---|---|---|
| **JAWS for Mainframe** (Freedom Scientific) | Current site-licensed version (minimum: JAWS 2022) | Primary screen reader for 3270 accessibility testing; used to verify field announcement (ACC-01 to ACC-06), tab order (ACC-07 to ACC-09), and keyboard operability (ACC-15 to ACC-16) |

### 3.2 Terminal Emulator

| Tool | Configuration | Purpose |
|---|---|---|
| **IBM Personal Communications (PCOMM)** or **Micro Focus Rumba** | 3270 model 2 (24×80) or model 3 (32×80); colour support enabled | Host terminal emulator through which JAWS for Mainframe interfaces with the 3270 data stream |

> **Note:** Testing must be performed on a 3270 model 2 (24×80) screen size as this is the minimum supported terminal model for CardDemo. Results on model 3 (32×80) must also be recorded.

### 3.3 BMS Map Attribute Verification

| Method | Tool | Purpose |
|---|---|---|
| Static BMS source review | Manual code review of `app/bms/CODASH00.bms` | Verify field attributes (ACC-10 to ACC-14) at source level before compilation |
| Compiled map inspection | CICS CEDA DISPLAY MAPSET(CODASH00) | Confirm compiled map attributes match BMS source |

### 3.4 Tab Order Verification

| Method | Tool | Purpose |
|---|---|---|
| Manual tab traversal | JAWS for Mainframe + PCOMM/Rumba | Tab through all fields on CODASH0A and record announcement sequence; verify against expected order (ACC-07 to ACC-09) |
| BMS field sequence review | Manual review of `app/bms/CODASH00.bms` field declaration order | 3270 tab order is determined by field declaration sequence in BMS source; verify sequence matches logical layout |

### 3.5 Suppressed Field Verification

| Method | Tool | Purpose |
|---|---|---|
| Scenario-based testing | JAWS for Mainframe with test ACCTDAT records | Load CODASH0A with single-account-type customers; verify JAWS does not announce suppressed tile fields (ACC-06, ACC-08) |

### 3.6 No Automated Web Scanner Applicable

> **Explicit exclusion:** Tools such as axe-core, Deque axe, Lighthouse, or WAVE are **not applicable** to this screen. These tools operate against HTML/DOM and cannot analyse 3270 data streams. The Definition of Done in spec.md §6 references "automated accessibility scan (axe or equivalent)" — for the CODASH0A screen, this requirement is satisfied by the JAWS for Mainframe manual test protocol defined in this document, which is the recognised equivalent for 3270 terminal interfaces.

---

## 4. Out-of-Scope Items

The following items are explicitly out of scope for CODASH0A accessibility testing:

| Item | Reason |
|---|---|
| WCAG 2.1 AA success criteria (all) | Not applicable to 3270 terminal; see §1 |
| axe / Lighthouse automated scans | Web-only tooling; not applicable to 3270 |
| Mobile accessibility (iOS VoiceOver, Android TalkBack) | CardDemo is a mainframe terminal application; no mobile interface exists |
| Touch / pointer device accessibility | 3270 terminal is keyboard-only by design |
| Colour contrast ratio (WCAG 1.4.3) | 3270 colour is controlled by terminal emulator settings; WCAG contrast ratios are not measurable in the 3270 data stream. Site-standard colour attributes (§2.3, ACC-14) apply instead |

---

## 5. Relationship to Existing CardDemo Screens

The CODASH0A accessibility requirements are consistent with the accessibility posture of existing CardDemo screens (COMEN1A, COADM1A, CACTVWA). No existing CardDemo screen has been formally accessibility-tested against JAWS for Mainframe. CODASH0A will be the **first screen in CardDemo to have documented, testable accessibility criteria**. The criteria defined in this document may be used as a template for future accessibility assessments of other CardDemo screens.

---

## 6. Dependency on TASK-002 (Masking Format)

Criterion ACC-18 (masking character consistency) is dependent on the masking format agreed in TASK-002. If TASK-002 specifies a masking format other than "asterisk for masked positions, last 4 digits visible", ACC-18 must be updated to reflect the agreed format before TASK-057 testing commences.

**Current assumption (pending TASK-002 sign-off):** Masked format is `****-****-****-1234` (last 4 digits of account identifier visible; all other positions replaced with asterisk; groups separated by hyphens if account number is 16 digits). This assumption must be confirmed by the Business Analyst owner of TASK-002.

---

## 7. Sign-off

| Role | Name | Signature | Date |
|---|---|---|---|
| **Accessibility Lead** | J. Hartwell | *J. Hartwell* | 2026-03-10 |
| QA Lead | M. Okonkwo | *M. Okonkwo* | 2026-03-10 |
| Mainframe Architect | P. Srinivasan | *P. Srinivasan* | 2026-03-10 |

---

## 8. Document History

| Version | Date | Author | Change |
|---|---|---|---|
| 0.1 | 2026-03-05 | J. Hartwell | Initial draft — applicability determination |
| 0.2 | 2026-03-07 | J. Hartwell | Added testable criteria ACC-01 to ACC-18 |
| 0.3 | 2026-03-09 | M. Okonkwo | QA review — added tab order and suppressed field criteria |
| 1.0 | 2026-03-10 | J. Hartwell | Final — approved for use by TASK-057 |