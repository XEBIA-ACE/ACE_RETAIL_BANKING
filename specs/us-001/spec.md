# Specification: View Consolidated Account Tiles on Dashboard
## Application: CardDemo (CAST Snapshot: 2026-03-10T16:28)

---

## 1. Overview

This specification describes the enhancement to the CardDemo mainframe application to implement a **Consolidated Account Tiles Dashboard** view. The feature requires displaying all account types (checking, savings, credit card) held by a customer in a single, consolidated CICS screen, with each account type visually distinguished, labelled, and showing a masked account identifier.

**BCM Scope:** None provided — this is a standing compliance gap per GR-08. All queries were executed application-wide and flagged accordingly.

**Technology Stack (Source: CAST MCP):**
- Technologies: IBM COBOL, IBM CICS TM, IBM z/OS JCL, SQL, IBM IMS DC, IBM MQ Series, Assembler, Shell/CLI
- Total LOC: 50,259
- Total Objects: 1,569
- Total Interactions: 3,590
- CAST Snapshot Delivery: Onboarding-202603101628 (2026-03-10T16:28)

---

## 2. Current State

### 2.1 Existing Dashboard / Menu Architecture

CardDemo currently implements a **CICS-based terminal (3270) application** with no consolidated account tile view. The closest existing constructs are:

| Component | CAST ID | Type | File |
|---|---|---|---|
| COSGN00C | 13954 | Cobol Transactional Program | app/cbl/COSGN00C.cbl |
| COADM01C | 14029 | Cobol Transactional Program | app/cbl/COADM01C.cbl |
| COMEN01C | 13577 | Cobol Transactional Program | app/cbl/COMEN01C.cbl |
| COACTVWC | 13092 | Cobol Transactional Program | app/cbl/COACTVWC.cbl |
| COACTUPC | 13895 | Cobol Transactional Program | app/cbl/COACTUPC.cbl |

**Sign-on flow (CAST MCP):**
- CICS Transaction `CC00` (ID: 14060) → `COSGN00C` (ID: 13954) — sign-on screen
- `COSGN00C` reads `USRSEC` CICS DataSet (ID: 13993) for user authentication
- After successful sign-on, `COSGN00C` transfers to either `COADM01C` (admin menu, ID: 14029) or `COMEN01C` (customer menu, ID: 13577)

**Menu flow (CAST MCP):**
- CICS Transaction `CA00` (ID: 14081) → `COADM01C` (ID: 14029) — admin menu
- CICS Transaction `CM00` (ID: 13108) → `COMEN01C` (ID: 13577) — customer menu
- `COMEN01C` uses CICS Map `COMEN1A` (ID: 13357) for screen rendering
- `COADM01C` uses CICS Map `COADM1A` (ID: 14185) for screen rendering

**Account view flow (CAST MCP):**
- CICS Transaction `CAVW` (ID: 14516) → `COACTVWC` (ID: 13092) — account view
- `COACTVWC` reads CICS DataSets: `ACCTDAT` (ID: 13444), `CUSTDAT` (ID: 14127), `CXACAIX` (ID: 13815)
- `COACTVWC` uses CICS Map `CACTVWA` (ID: 14417)
- `COACTVWC` contains paragraphs: `9300-GETACCTDATA-BYACCT` (ID: 13379), `9400-GETCUSTDATA-BYCUST` (ID: 14225)

**Account update flow (CAST MCP):**
- CICS Transaction `CAUP` (ID: 13720) → `COACTUPC` (ID: 13895) — account update
- `COACTUPC` reads/writes CICS DataSets: `ACCTDAT` (ID: 13444), `CUSTDAT` (ID: 14127), `CXACAIX` (ID: 13815)

### 2.2 Data Stores (CAST MCP)

| Dataset | CAST ID | Type | Purpose |
|---|---|---|---|
| ACCTDAT | 13444 | CICS DataSet | Account data (backed by AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS) |
| CUSTDAT | 14127 | CICS DataSet | Customer data (backed by AWS.M2.CARDDEMO.CUSTDATA.VSAM.KSDS) |
| USRSEC | 13993 | CICS DataSet | User security/authentication (backed by AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS) |
| CXACAIX | 13815 | CICS DataSet | Card cross-reference alternate index |

### 2.3 What Does NOT Exist (CAST MCP — queries returned no results)

- No object named "dashboard", "tile", "checking", "savings", or "credit" exists in CardDemo.
- No object named "Consolidated" exists in CardDemo (confirmed by seed document and CAST query).
- The concept of "account tiles" (checking, savings, credit card as distinct tile types) does not exist in the current codebase.

### 2.4 Existing Quality Issues (CAST MCP)

| Rule ID | Rule Name | Violations | Characteristic |
|---|---|---|---|
| 7688 | Never truncate data in MOVE statements | 34 | Reliability |
| 8034 | Working-Storage variables must be initialized before being read | 29 | Reliability/Security |
| 8162 | CICS return code should be checked | 25 | Reliability |
| 7766 | Avoid Artifacts with High Cyclomatic Complexity | 33 | Maintainability |
| 7290 | Avoid unreferenced Sections and Paragraphs | 13 | Maintainability |
| 8478 | Avoid Buffer Overruns (ADD/SUBTRACT in loops without ON SIZE ERROR) | 13 | Security |
| 7756 | Avoid READ without AT END/INVALID KEY | 12 | Reliability |
| 7300 | Avoid large Paragraphs (too many LOC) | 14 | Maintainability |

---

## 3. Proposed Changes

### 3.1 New Feature: Consolidated Account Tiles Screen

A new CICS screen must be created to display a consolidated view of all account types held by the authenticated customer. This is a **net-new capability** — no existing program or map implements it.

#### New Objects Required

| Object | Type | Purpose |
|---|---|---|
| CODASH00C | Cobol Transactional Program (new) | Dashboard controller — reads account data and renders tiles |
| CODASH00 (BMS) | CICS Map (new) | BMS map defining the consolidated tile layout |
| CODASH00.CPY | Cobol CopyBook (new) | BMS-generated copybook for CODASH00 map |
| CODASH0A | CICS Map entry (new) | Map set entry within CODASH00 BMS |
| CD00 | CICS Transaction (new) | Entry-point transaction for the dashboard |

#### Integration Points (CAST-confirmed existing objects)

| Existing Object | CAST ID | Change Required |
|---|---|---|
| COMEN01C | 13577 | Add menu option to transfer to CD00/CODASH00C |
| COADM01C | 14029 | Add menu option to transfer to CD00/CODASH00C (admin path) |
| COSGN00C | 13954 | No change — authentication gate remains unchanged |
| ACCTDAT (CICS DataSet) | 13444 | Read-only access from CODASH00C |
| CUSTDAT (CICS DataSet) | 14127 | Read-only access from CODASH00C |
| CXACAIX (CICS DataSet) | 13815 | Read-only access from CODASH00C |
| CARDDEMO.CSD | — | Register new transaction CD00 and map CODASH00 |

### 3.2 Account Type Tile Logic

The requirement specifies three account types: checking, savings, and credit card. CardDemo's current data model uses VSAM KSDS files for account data (`ACCTDAT`, backed by `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`). The account type field within the ACCTDAT record must be used to determine which tiles to render.

⚠️ **SME validation required:** The specific field name(s) within the ACCTDAT VSAM record that distinguish checking, savings, and credit-card account types are not determinable from CAST MCP alone (column-level schema detail is not available in this CAST configuration). The copybooks `CVACT01Y.cpy`, `CVACT02Y.cpy`, `CVACT03Y.cpy` (used by COACTVWC, ID: 13092) likely define the account record layout and must be reviewed by an SME.

### 3.3 Authentication / Session Gate

The existing sign-on program `COSGN00C` (ID: 13954) is the authentication gate. It reads the `USRSEC` CICS DataSet (ID: 13993). The new dashboard transaction `CD00` must be reachable only after successful sign-on — the COMMAREA-based session token pattern used by existing programs (confirmed by source code comments in COTRTLIC, COCRDUPC) must be applied to CODASH00C.

⚠️ **SME validation required:** The exact COMMAREA structure for session validation is defined in `COCOM01Y.cpy` (ID: 13504), which is included by all menu programs. CODASH00C must include and honour this copybook.

---

## 4. Breaking Changes

| Change | Affected Objects (CAST ID) | Impact |
|---|---|---|
| New CICS Transaction CD00 registered in CARDDEMO.CSD | CARDDEMO.CSD | CSD must be updated; no existing transaction is displaced |
| New menu option in COMEN01C | COMEN01C (13577) | Existing menu options shift; regression test required |
| New menu option in COADM01C | COADM01C (14029) | Existing menu options shift; regression test required |
| New BMS map CODASH00 compiled and linked | Build JCL (CBLDBMS, CICCMP) | Build pipeline change required |

**Blast radius (CAST-measured):** The seed document states a blast radius of 2. CAST MCP confirms: `COMEN01C` (ID: 13577) has 25 inward callers; `COADM01C` (ID: 14029) has 15 inward callers. Any change to these programs' menu option numbering could affect all callers. The new CODASH00C program itself has 0 existing callers (it is new).

---

## 5. Acceptance Criteria (Source: Requirement Document)

1. Given a customer holds checking, savings, and credit-card accounts, when the dashboard loads, then all three account tiles are displayed in one consolidated view, each visually distinguished and labelled by account type.
2. Given a customer holds only a checking account, when the dashboard loads, then only the checking tile is displayed; no empty or error tiles appear for savings or credit card.
3. Given a customer holds any combination of the three supported account types, when the dashboard loads, then only tiles for owned account types are rendered.
4. Given the dashboard is displayed, when a customer views the tiles, then each tile clearly shows the account type label and masked account identifier.
5. Given an unauthenticated or session-expired user attempts to access the dashboard, when the route is requested, then access is denied and the user is redirected before any account data is rendered.
6. Given the dashboard loads under normal conditions, when page load time is measured, then p95 page load is under 2 seconds and time-to-interactive is under 3 seconds.
7. Given a customer uses a screen reader or keyboard navigation, when the dashboard tiles are rendered, then all tiles are accessible, operable via keyboard with logical focus order, and announced correctly by screen readers with zero critical/serious WCAG 2.1 AA violations.

> **Note on AC6 and AC7:** CardDemo is a CICS 3270 terminal application. "Page load time" and "WCAG 2.1 AA" are web-centric metrics. ⚠️ SME validation required to translate these into mainframe-equivalent targets (e.g., CICS response time SLA, 3270 screen reader compatibility with assistive technology such as JAWS for Mainframe).

---

## 6. Definition of Done (Source: Requirement Document)

- Acceptance criteria met and verified by QA.
- Automated accessibility scan (axe or equivalent) shows zero critical/serious WCAG 2.1 AA violations.
- Performance targets validated under normal and peak load.
- Security controls (session validation, TLS, audit logging) verified.
- GDPR export/deletion inclusion confirmed by compliance.
- Integration adapter health/status endpoints tested.
