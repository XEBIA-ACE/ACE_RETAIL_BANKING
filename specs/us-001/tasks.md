# Tasks: Consolidated Account Tiles Dashboard
## Application: CardDemo

---

## Phase 0: Pre-Implementation (Blockers)

### TASK-001: SME Sign-off on Account Type Field
**Priority:** BLOCKER  
**Owner:** Business Analyst + Mainframe Developer  
**Description:** Review copybooks `CVACT01Y.cpy` (ID: 14474), `CVACT02Y.cpy` (ID: 14206), `CVACT03Y.cpy` (ID: 13702) to identify the field(s) that distinguish checking, savings, and credit-card account types within the ACCTDAT VSAM record (CICS DataSet ID: 13444, backed by AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS, ID: 1111). Document the field name, PIC clause, and valid values for each account type.  
**Acceptance:** Written SME sign-off with field name and value mapping.

### TASK-002: SME Sign-off on Account Masking Format
**Priority:** BLOCKER  
**Owner:** Business Analyst  
**Description:** Define the masking format for account identifiers to be displayed on the CODASH0A map (e.g., last 4 digits visible, remainder replaced with asterisks). Confirm the source field in CVACT01Y/CVACT02Y/CVACT03Y.  
**Acceptance:** Written masking specification.

### TASK-003: BCM Scope Registration
**Priority:** HIGH (compliance gap GR-08)  
**Owner:** Enterprise Architect  
**Description:** Register the BCM subsystem/component for this feature to enable scoped CAST impact analysis. Currently all queries are application-wide.  
**Acceptance:** BCM entry created; CAST queries re-run with BCM filter.

### TASK-004: Performance SLA Translation
**Priority:** HIGH  
**Owner:** Performance Engineer + Mainframe Architect  
**Description:** Translate requirement "p95 page load < 2s, TTI < 3s" into CICS-equivalent SLA (e.g., CICS response time percentile target). CardDemo is a 3270 terminal application — web metrics do not apply directly.  
**Acceptance:** Written CICS performance SLA agreed by stakeholders.

### TASK-005: Accessibility Requirements for 3270
**Priority:** HIGH  
**Owner:** Accessibility Lead  
**Description:** Confirm whether WCAG 2.1 AA applies to the 3270 terminal interface or whether an equivalent mainframe accessibility standard applies (e.g., JAWS for Mainframe compatibility). Define testable criteria.  
**Acceptance:** Written accessibility acceptance criteria for 3270 context.

---

## Phase 1: BMS Map Design and Compilation

### TASK-010: Create CODASH00.bms
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File to create:** `app/bms/CODASH00.bms`  
**Description:** Create a new BMS map source file defining:
- Map set name: `CODASH00`
- Map name: `CODASH0A`
- Header fields (reuse COTTL01Y.cpy pattern, ID: 14116)
- Up to 3 account tile sections: CHECKING-TILE, SAVINGS-TILE, CREDITCARD-TILE
- Each tile: TYPE-LABEL (ATTRB=(PROT,NORM)), MASKED-ACCT-ID (ATTRB=(PROT,NORM))
- Error message field: ERR-MSG (ATTRB=(PROT,BRT))
- PF key legend consistent with COMEN1A (ID: 13357) and COADM1A (ID: 14185)
**Acceptance:** BMS source compiles without errors.

### TASK-011: Generate CODASH00.CPY from BMS Compilation
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File to create:** `app/cpy-bms/CODASH00.CPY`  
**Description:** Compile CODASH00.bms using the BMS compiler (pattern: CBLDBMS JCL, transaction ID: 24137) to generate the symbolic map copybook CODASH00.CPY. Store in `app/cpy-bms/`.  
**Acceptance:** CODASH00.CPY generated and contains all field definitions from CODASH00.bms.

### TASK-012: Update Build JCL for CODASH00
**Priority:** HIGH  
**Owner:** Mainframe Build Engineer  
**Files to modify:** Build JCL (pattern: CBLDBMS / CICCMP)  
**Description:** Add CODASH00.bms to the BMS compilation step. Add CODASH00C.cbl to the COBOL compilation step.  
**Acceptance:** Build JCL executes without errors for new components.

---

## Phase 2: CODASH00C Program Development

### TASK-020: Create CODASH00C.cbl — Skeleton and WORKING-STORAGE
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File to create:** `app/cbl/CODASH00C.cbl`  
**Description:** Create the COBOL program skeleton with:
- IDENTIFICATION DIVISION: PROGRAM-ID. CODASH00C.
- ENVIRONMENT DIVISION (empty for CICS programs)
- DATA DIVISION / WORKING-STORAGE SECTION:
  - Include CODASH00.CPY (new, from TASK-011)
  - Include COCOM01Y.cpy (ID: 13504) — COMMAREA/session token
  - Include COTTL01Y.cpy (ID: 14116) — screen title
  - Include CSDAT01Y.cpy (ID: 14153) — date fields
  - Include CSMSG01Y.cpy (ID: 13263) — message fields
  - Include CSUSR01Y.cpy (ID: 13507) — user security fields
  - Include CVACT01Y.cpy (ID: 14474) — account data layout
  - Include CVACT02Y.cpy (ID: 14206) — account data layout
  - Include CVACT03Y.cpy (ID: 13702) — account data layout
  - Include DFHAID (ID: 1003) — CICS AID keys
  - Include DFHBMSCA (ID: 1002) — BMS attributes
  - All WS variables must have VALUE clause (per ISO-5055 rule 8034, 29 existing violations)
- LINKAGE SECTION: DFHCOMMAREA
**Acceptance:** Program compiles without errors; all WS variables initialized.

### TASK-021: Implement 0000-MAIN Paragraph
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File:** `app/cbl/CODASH00C.cbl`  
**Description:** Implement the main entry paragraph following the pattern in COACTVWC (ID: 13092) and COMEN01C (ID: 13577):
1. Store context (MOVE EIBAID to WS-AID)
2. Clear error message
3. Check COMMAREA: if EIBCALEN = 0 or session token invalid → PERFORM RETURN-TO-SIGNON-SCREEN (EXEC CICS XCTL PROGRAM('COSGN00C') — consistent with COADM01C paragraph RETURN-TO-SIGNON-SCREEN, ID: 13890)
4. Remap PF keys
5. Dispatch: PF3 → EXEC CICS XCTL PROGRAM('COMEN01C'); ENTER → PERFORM 2000-PROCESS-INPUTS; else → PERFORM 1000-SEND-MAP
6. PERFORM COMMON-RETURN
**Acceptance:** Session validation redirects unauthenticated users to COSGN00C (ID: 13954).

### TASK-022: Implement 9100-READ-ACCT-TILES Paragraph
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File:** `app/cbl/CODASH00C.cbl`  
**Description:** Read account data from ACCTDAT CICS DataSet (ID: 13444):
- EXEC CICS READ DATASET('ACCTDAT') INTO(WS-ACCT-REC) RIDFLD(WS-ACCT-KEY) RESP(WS-RESP) END-EXEC
- Check RESP code after every CICS READ (per ISO-5055 rule 8162, 25 existing violations)
- Use INVALID KEY clause on all VSAM reads (per ISO-5055 rule 7756, 12 existing violations)
- Iterate over all accounts for the customer; for each account, determine type using field identified in TASK-001
- Set tile-present flags: WS-CHECKING-PRESENT, WS-SAVINGS-PRESENT, WS-CREDITCARD-PRESENT
**Acceptance:** Correct tile flags set for all account type combinations; RESP checked after every CICS call.

### TASK-023: Implement 9200/9300/9400 Tile Build Paragraphs
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File:** `app/cbl/CODASH00C.cbl`  
**Description:**
- `9200-BUILD-TILE-CHECKING`: IF WS-CHECKING-PRESENT = 'Y' THEN populate CHECKING-TILE fields in CODASH0A map; ELSE set tile fields to SPACES and suppress display
- `9300-BUILD-TILE-SAVINGS`: same pattern for savings
- `9400-BUILD-TILE-CREDITCARD`: same pattern for credit card
- Each paragraph calls `9500-MASK-ACCOUNT-ID` to mask the account number
**Acceptance:** Only tiles for owned account types are populated; empty tiles are suppressed.

### TASK-024: Implement 9500-MASK-ACCOUNT-ID Paragraph
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File:** `app/cbl/CODASH00C.cbl`  
**Description:** Apply masking format (from TASK-002) to account identifier. Use MOVE statements that do NOT truncate data (per ISO-5055 rule 7688, 34 existing violations). Use STRING verb with ON OVERFLOW clause (per ISO-5055 rule 8470).  
**Acceptance:** Masked account ID matches agreed format; no data truncation.

### TASK-025: Implement 1000-SEND-MAP and 1100-SCREEN-INIT Paragraphs
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File:** `app/cbl/CODASH00C.cbl`  
**Description:**
- `1100-SCREEN-INIT`: Initialize all CODASH0A map fields to SPACES/LOW-VALUES; call 9100-READ-ACCT-TILES; call 9200/9300/9400 tile build paragraphs
- `1000-SEND-MAP`: EXEC CICS SEND MAP('CODASH0A') MAPSET('CODASH00') FROM(CODASH0A-O) ERASE RESP(WS-RESP) END-EXEC; check RESP
**Acceptance:** Map sent correctly; RESP checked.

### TASK-026: Implement COMMON-RETURN and ABEND-ROUTINE Paragraphs
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File:** `app/cbl/CODASH00C.cbl`  
**Description:**
- `COMMON-RETURN`: EXEC CICS RETURN TRANSID('CD00') COMMAREA(WS-COMMAREA) LENGTH(WS-COMMAREA-LEN) END-EXEC
- `ABEND-ROUTINE`: EXEC CICS ABEND ABCODE('CDSH') END-EXEC (consistent with existing abend patterns)
**Acceptance:** Program returns correctly with COMMAREA; abend code is unique.

### TASK-027: Compile and Link CODASH00C
**Priority:** HIGH  
**Owner:** Mainframe Build Engineer  
**Description:** Compile CODASH00C.cbl using the COBOL compiler (pattern: CBLDBMS JCL). Link-edit into CICS load library.  
**Acceptance:** Zero compile errors; load module created.

---

## Phase 3: CICS Transaction Registration

### TASK-030: Update CARDDEMO.CSD
**Priority:** HIGH  
**Owner:** CICS Systems Programmer  
**File to modify:** `app/csd/CARDDEMO.CSD`  
**Description:** Add the following definitions to CARDDEMO.CSD:
- `TRANSACTION(CD00) PROGRAM(CODASH00C) TWASIZE(0) PROFILE(DFHCICST)` — new dashboard transaction
- `MAPSET(CODASH00) RESIDENT(NO)` — new map set
- Pattern: follow existing entries for CA00 (ID: 14081), CM00 (ID: 13108)
**Acceptance:** CD00 transaction and CODASH00 map set defined in CSD; CICS CEDA INSTALL succeeds.

---

## Phase 4: Menu Integration

### TASK-040: Modify COMEN01C — BUILD-MENU-OPTIONS (ID: 13851)
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File to modify:** `app/cbl/COMEN01C.cbl`  
**Description:** In paragraph `BUILD-MENU-OPTIONS` (ID: 13851), add a new menu option for "View Account Dashboard". Assign the next available option number. Update the COMEN1A map (ID: 13357) display accordingly.  
**⚠️ Regression risk:** COMEN01C (ID: 13577) has 25 inward callers. Any change to option numbering must be regression-tested.  
**Acceptance:** New option appears on COMEN1A screen; existing options retain their numbers.

### TASK-041: Modify COMEN01C — PROCESS-ENTER-KEY (ID: 14234)
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**File to modify:** `app/cbl/COMEN01C.cbl`  
**Description:** In paragraph `PROCESS-ENTER-KEY` (ID: 14234), add WHEN clause for new dashboard option: `EXEC CICS XCTL PROGRAM('CODASH00C') COMMAREA(WS-COMMAREA) LENGTH(WS-COMMAREA-LEN) END-EXEC`. Check RESP code.  
**Acceptance:** Selecting dashboard option transfers control to CODASH00C.

### TASK-042: Modify COADM01C — BUILD-MENU-OPTIONS (ID: 13453)
**Priority:** MEDIUM  
**Owner:** Mainframe Developer  
**File to modify:** `app/cbl/COADM01C.cbl`  
**Description:** In paragraph `BUILD-MENU-OPTIONS` (ID: 13453), add a new menu option for "View Account Dashboard". Update COADM1A map (ID: 14185) display.  
**⚠️ Regression risk:** COADM01C (ID: 14029) has 15 inward callers.  
**Acceptance:** New option appears on COADM1A screen; existing options retain their numbers.

### TASK-043: Modify COADM01C — PROCESS-ENTER-KEY (ID: 14096)
**Priority:** MEDIUM  
**Owner:** Mainframe Developer  
**File to modify:** `app/cbl/COADM01C.cbl`  
**Description:** In paragraph `PROCESS-ENTER-KEY` (ID: 14096), add WHEN clause for new dashboard option: `EXEC CICS XCTL PROGRAM('CODASH00C') COMMAREA(WS-COMMAREA) LENGTH(WS-COMMAREA-LEN) END-EXEC`. Check RESP code.  
**Acceptance:** Selecting dashboard option from admin menu transfers control to CODASH00C.

---

## Phase 5: Testing

### TASK-050: Unit Test — Account Tile Rendering
**Priority:** HIGH  
**Owner:** QA Engineer  
**Description:** Test CODASH00C with the following ACCTDAT scenarios:
1. Customer has checking + savings + credit card → all 3 tiles displayed
2. Customer has checking only → only checking tile displayed; no empty/error tiles
3. Customer has savings only → only savings tile displayed
4. Customer has credit card only → only credit card tile displayed
5. Customer has checking + credit card → 2 tiles displayed
6. Customer has no accounts → no tiles displayed; no error tiles
**Acceptance:** All 6 scenarios pass; acceptance criteria 1–3 met.

### TASK-051: Unit Test — Account Identifier Masking
**Priority:** HIGH  
**Owner:** QA Engineer  
**Description:** Verify that each tile displays the masked account identifier per the format agreed in TASK-002. Verify no unmasked account data is displayed.  
**Acceptance:** Acceptance criterion 4 met.

### TASK-052: Integration Test — Session Validation
**Priority:** HIGH  
**Owner:** QA Engineer  
**Description:** Test that accessing CD00 without a valid COMMAREA session token redirects to COSGN00C (ID: 13954) and no account data is rendered. Test with: (a) no COMMAREA, (b) expired session token.  
**Acceptance:** Acceptance criterion 5 met.

### TASK-053: Integration Test — Full Sign-on Flow
**Priority:** HIGH  
**Owner:** QA Engineer  
**Description:** Test full flow: CC00 (ID: 14060) → COSGN00C (ID: 13954) → COMEN01C (ID: 13577) → CD00 → CODASH00C → dashboard displayed. Test full flow via COADM01C (ID: 14029) path.  
**Acceptance:** Dashboard accessible from both menu paths.

### TASK-054: Regression Test — COMEN01C Callers
**Priority:** HIGH  
**Owner:** QA Engineer  
**Description:** Regression test all 25 inward callers of COMEN01C (ID: 13577): COACTUPC (13895), COACTVWC (13092), COBIL00C (13583), COCRDLIC (13903), COCRDSLC (13237), COCRDUPC (13160), COPAUS0C (13740), CORPT00C (13285), COSGN00C (13954), COTRN00C (13149), COTRN01C (13253), COTRN02C (14022), and others. Verify existing menu options are unaffected.  
**Acceptance:** All existing menu options function correctly; no regression.

### TASK-055: Regression Test — COADM01C Callers
**Priority:** MEDIUM  
**Owner:** QA Engineer  
**Description:** Regression test all 15 inward callers of COADM01C (ID: 14029): COSGN00C (13954), COTRTLIC (13636), COTRTUPC (14540), COUSR00C (14296), COUSR01C (14354), COUSR02C (13646), COUSR03C (14126), and others.  
**Acceptance:** All existing admin menu options function correctly; no regression.

### TASK-056: Performance Test
**Priority:** HIGH  
**Owner:** Performance Engineer  
**Description:** Measure CICS response time for CD00 transaction under normal and peak load. Compare against SLA defined in TASK-004.  
**Acceptance:** Acceptance criterion 6 met (per translated SLA from TASK-004).

### TASK-057: Accessibility Test
**Priority:** HIGH  
**Owner:** Accessibility Lead  
**Description:** Test CODASH0A screen against criteria defined in TASK-005 (3270 screen reader compatibility).  
**Acceptance:** Acceptance criterion 7 met.

### TASK-058: CAST Quality Scan
**Priority:** HIGH  
**Owner:** Mainframe Developer  
**Description:** Run CAST analysis on CODASH00C.cbl after implementation. Verify zero violations of:
- Rule 8162 (CICS return code not checked)
- Rule 8034 (WS variables not initialized)
- Rule 7688 (MOVE truncation)
- Rule 8478 (Buffer overruns in loops)
- Rule 7756 (READ without AT END/INVALID KEY)
**Acceptance:** Zero violations of the above rules in CODASH00C.

---

## Task Dependency Order

```
TASK-001 → TASK-022, TASK-023, TASK-024
TASK-002 → TASK-024
TASK-004 → TASK-056
TASK-005 → TASK-057
TASK-010 → TASK-011 → TASK-020
TASK-011 → TASK-020
TASK-012 → TASK-027
TASK-020 → TASK-021 → TASK-022 → TASK-023 → TASK-024 → TASK-025 → TASK-026 → TASK-027
TASK-027 → TASK-030 → TASK-040 → TASK-041 → TASK-042 → TASK-043
TASK-043 → TASK-050 → TASK-051 → TASK-052 → TASK-053 → TASK-054 → TASK-055 → TASK-056 → TASK-057 → TASK-058
```
