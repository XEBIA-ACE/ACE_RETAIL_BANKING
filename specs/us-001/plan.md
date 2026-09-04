# Implementation Plan: Consolidated Account Tiles Dashboard
## Application: CardDemo

---

## GR-12/13 Note
This is a feature specification, not a decomposition exercise. GR-12 (batch boundary) and GR-13 (fine-grained boundary) are N/A for this use case.

---

## Phase 0: Pre-Implementation Prerequisites

### 0.1 SME Validations Required Before Coding

| Item | Question | Source |
|---|---|---|
| Account type field | What field(s) in the ACCTDAT VSAM record (copybooks CVACT01Y, CVACT02Y, CVACT03Y) distinguish checking, savings, and credit-card account types? | ⚠️ SME required |
| Account masking | What is the required masking format for account identifiers on the 3270 screen? | ⚠️ SME required |
| COMMAREA structure | Confirm COCOM01Y.cpy (ID: 13504) is the correct session token copybook for CODASH00C to include | ⚠️ SME required |
| Performance SLA | Translate "p95 < 2s, TTI < 3s" into CICS response time SLA for 3270 terminal | ⚠️ SME required |
| Accessibility | Confirm 3270 screen reader compatibility requirements (JAWS for Mainframe or equivalent) | ⚠️ SME required |
| BCM scope | Provide BCM subsystem/component for scoped impact analysis | ⚠️ Compliance gap (GR-08) |

---

## Phase 1: BMS Map Design and Compilation

**Objective:** Create the new CICS BMS map `CODASH00` that defines the consolidated tile layout.

### 1.1 New BMS Map: CODASH00

- Create `app/bms/CODASH00.bms` defining map set `CODASH00` with map `CODASH0A`
- Map must include fields for:
  - Screen title / header (reuse COTTL01Y.cpy pattern, ID: 14116)
  - Up to 3 account tile sections (checking, savings, credit card)
  - Each tile: account type label field, masked account number field, account status field
  - Error message field
  - PF key legend (consistent with existing maps)
- Create `app/cpy-bms/CODASH00.CPY` (BMS-generated copybook)

### 1.2 Build JCL Update

- Update build JCL (pattern: CBLDBMS / CICCMP transactions, IDs: 24137, 24136) to compile CODASH00.bms
- Register CODASH00 map set in CARDDEMO.CSD

**Dependencies:** None (can proceed in parallel with Phase 2 design)

---

## Phase 2: CODASH00C Program Development

**Objective:** Implement the dashboard controller program.

### 2.1 Program Structure (following CardDemo conventions)

CODASH00C must follow the paragraph structure pattern of existing programs (COACTVWC, ID: 13092; COMEN01C, ID: 13577):

| Paragraph | Purpose |
|---|---|
| 0000-MAIN | Entry point; context store, COMMAREA check, PF key dispatch |
| 1000-SEND-MAP | Send CODASH0A map to terminal |
| 1100-SCREEN-INIT | Initialize screen fields |
| 2000-PROCESS-INPUTS | Receive and process user input |
| 9100-READ-ACCT-TILES | Read ACCTDAT (CICS DataSet, ID: 13444) for all accounts owned by customer |
| 9200-BUILD-TILE-CHECKING | Populate checking tile fields (conditional on account type) |
| 9300-BUILD-TILE-SAVINGS | Populate savings tile fields (conditional on account type) |
| 9400-BUILD-TILE-CREDITCARD | Populate credit card tile fields (conditional on account type) |
| 9500-MASK-ACCOUNT-ID | Apply masking to account identifier |
| COMMON-RETURN | CICS RETURN with COMMAREA |
| ABEND-ROUTINE | Error handling |

### 2.2 Required CopyBook Includes (following COACTVWC pattern, ID: 13092)

| CopyBook | CAST ID | Purpose |
|---|---|---|
| CODASH00.CPY | new | BMS map fields |
| COCOM01Y.cpy | 13504 | COMMAREA / session token |
| COTTL01Y.cpy | 14116 | Screen title fields |
| CSDAT01Y.cpy | 14153 | Date fields |
| CSMSG01Y.cpy | 13263 | Message fields |
| CSUSR01Y.cpy | 13507 | User security fields |
| CVACT01Y.cpy | 14474 | Account data layout (from COACTVWC) |
| CVACT02Y.cpy | 14206 | Account data layout (from COACTVWC) |
| CVACT03Y.cpy | 13702 | Account data layout (from COACTVWC) |
| DFHAID | 1003 | CICS AID keys |
| DFHBMSCA | 1002 | BMS attributes |

### 2.3 Data Access Pattern

- Read `ACCTDAT` CICS DataSet (ID: 13444) — READ with INVALID KEY clause (per rule 7756)
- Read `CUSTDAT` CICS DataSet (ID: 14127) — READ with INVALID KEY clause
- All CICS EXEC calls must check RESP code (per rule 8162)
- All Working-Storage variables must be initialized with VALUE clause (per rule 8034)
- No MOVE statements that truncate data (per rule 7688)
- All arithmetic in loops must use ON SIZE ERROR (per rule 8478)

### 2.4 Session Validation

- On entry, check COMMAREA for valid session token (pattern from COCOM01Y.cpy, ID: 13504)
- If COMMAREA absent or session expired: EXEC CICS XCTL to COSGN00C (ID: 13954) — consistent with RETURN-TO-SIGNON-SCREEN pattern used by COADM01C (ID: 14029) and COMEN01C (ID: 13577)

---

## Phase 3: CICS Transaction Registration

**Objective:** Register new transaction CD00 in CARDDEMO.CSD.

### 3.1 CSD Update

- Add transaction definition: `TRANSACTION(CD00) PROGRAM(CODASH00C)` to CARDDEMO.CSD
- Add map set definition: `MAPSET(CODASH00)` to CARDDEMO.CSD
- Pattern: follow existing entries in `app/csd/CARDDEMO.CSD`

---

## Phase 4: Menu Integration

**Objective:** Add dashboard option to existing menu programs.

### 4.1 COMEN01C Modification (ID: 13577, file: app/cbl/COMEN01C.cbl)

- Modify `BUILD-MENU-OPTIONS` paragraph (ID: 13851) to add "View Account Dashboard" option
- Modify `PROCESS-ENTER-KEY` paragraph (ID: 14234) to handle new option: EXEC CICS XCTL PROGRAM(CODASH00C)
- ⚠️ Regression risk: COMEN01C has 25 inward callers (CAST MCP, query #21). Any change to menu option numbering must be regression-tested against all callers.

### 4.2 COADM01C Modification (ID: 14029, file: app/cbl/COADM01C.cbl)

- Modify `BUILD-MENU-OPTIONS` paragraph (ID: 13453) to add "View Account Dashboard" option
- Modify `PROCESS-ENTER-KEY` paragraph (ID: 14096) to handle new option
- ⚠️ Regression risk: COADM01C has 15 inward callers (CAST MCP, query #18).

---

## Phase 5: Testing

### 5.1 Unit Testing

- Test CODASH00C with ACCTDAT records containing: all 3 account types, only checking, only savings, only credit card, no accounts
- Test session validation: expired COMMAREA → redirect to COSGN00C (ID: 13954)
- Test account masking logic

### 5.2 Integration Testing

- Test full flow: CC00 → COSGN00C → COMEN01C → CD00 → CODASH00C
- Test full flow: CC00 → COSGN00C → COADM01C → CD00 → CODASH00C
- Test CICS DataSet access: ACCTDAT (ID: 13444), CUSTDAT (ID: 14127)
- Regression test all 25 callers of COMEN01C and 15 callers of COADM01C

### 5.3 Performance Testing

- Measure CICS response time under normal and peak load
- Target: ⚠️ SME to confirm equivalent of "p95 < 2s" in CICS terms

### 5.4 Accessibility Testing

- ⚠️ SME to confirm 3270 screen reader compatibility requirements

---

## Rollback Strategy

1. Remove CD00 transaction from CARDDEMO.CSD (no existing transaction displaced)
2. Revert COMEN01C (ID: 13577) to previous version — restore BUILD-MENU-OPTIONS and PROCESS-ENTER-KEY paragraphs
3. Revert COADM01C (ID: 14029) to previous version — restore BUILD-MENU-OPTIONS and PROCESS-ENTER-KEY paragraphs
4. Remove CODASH00C load module from CICS load library
5. Remove CODASH00 map set from CICS load library
6. No data store changes are made by this feature — rollback does not require data migration

---

## Dependency Upgrade Table

Not applicable — this is a feature addition to an existing mainframe COBOL/CICS application. No framework version upgrades are required. (Source: Requirement Document — no version upgrade targets specified.)
