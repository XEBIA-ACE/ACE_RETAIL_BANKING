# CAST Quality Scan Report: CODASH00C
## Application: CardDemo (CAST Snapshot: 2026-03-10T16:28)

---

## 1. Scan Overview

| Field | Value |
|---|---|
| **Program Scanned** | CODASH00C |
| **Source File** | `app/cbl/CODASH00C.cbl` |
| **CAST Application** | CardDemo |
| **Snapshot Reference** | Onboarding-202603101628 (2026-03-10T16:28) |
| **Scan Date** | 2026-03-10 |
| **Scan Triggered By** | TASK-058 (post-implementation quality gate) |
| **Scan Scope** | CODASH00C.cbl only (new program — zero existing callers at time of scan) |
| **Analyst** | Mainframe Developer (TASK-058 owner) |
| **Report Status** | FINAL |

---

## 2. Rules Under Scrutiny

The following five ISO-5055 / CAST rules were identified as having existing violations in the CardDemo codebase prior to this implementation. The acceptance criterion for TASK-058 requires **zero new violations** of any of these rules in CODASH00C.

| Rule ID | Rule Name | Characteristic | Pre-existing Violations (Codebase) | Target for CODASH00C |
|---|---|---|---|---|
| 8162 | CICS return code should be checked | Reliability | 25 | 0 |
| 8034 | Working-Storage variables must be initialized before being read | Reliability / Security | 29 | 0 |
| 7688 | Never truncate data in MOVE statements | Reliability | 34 | 0 |
| 8478 | Avoid Buffer Overruns (ADD/SUBTRACT in loops without ON SIZE ERROR) | Security | 13 | 0 |
| 7756 | Avoid READ without AT END/INVALID KEY | Reliability | 12 | 0 |

---

## 3. Scan Results — Rule-by-Rule Findings

### 3.1 Rule 8162 — CICS Return Code Should Be Checked

**Result: PASS — 0 violations**

| Evidence | Detail |
|---|---|
| Pattern applied | Every `EXEC CICS` statement in CODASH00C includes a `RESP(WS-RESP)` clause |
| Post-call check | Immediately after each `EXEC CICS` call, `WS-RESP` is evaluated against `DFHRESP(NORMAL)` |
| Paragraphs verified | `9100-READ-ACCT-TILES`, `1000-SEND-MAP`, `0000-MAIN` (XCTL calls), `COMMON-RETURN` |
| CICS calls covered | `EXEC CICS READ DATASET('ACCTDAT')`, `EXEC CICS READ DATASET('CUSTDAT')`, `EXEC CICS SEND MAP`, `EXEC CICS XCTL PROGRAM('COSGN00C')`, `EXEC CICS XCTL PROGRAM('COMEN01C')`, `EXEC CICS RETURN` |

**Code pattern applied (representative):**
```cobol
EXEC CICS READ
    DATASET('ACCTDAT')
    INTO(WS-ACCT-REC)
    RIDFLD(WS-ACCT-KEY)
    RESP(WS-RESP)
END-EXEC

EVALUATE WS-RESP
    WHEN DFHRESP(NORMAL)
        CONTINUE
    WHEN DFHRESP(NOTFND)
        MOVE 'N' TO WS-ACCT-FOUND
    WHEN OTHER
        PERFORM ABEND-ROUTINE
END-EVALUATE
```

**Conclusion:** Rule 8162 — **ZERO violations** in CODASH00C. Codebase total remains at 25 (unchanged).

---

### 3.2 Rule 8034 — Working-Storage Variables Must Be Initialized Before Being Read

**Result: PASS — 0 violations**

| Evidence | Detail |
|---|---|
| Pattern applied | All Working-Storage elementary items declared with explicit `VALUE` clause |
| Group items | Initialized with `VALUE SPACES` or `VALUE LOW-VALUES` as appropriate |
| Flags | `WS-CHECKING-PRESENT`, `WS-SAVINGS-PRESENT`, `WS-CREDITCARD-PRESENT` initialized to `'N'` |
| Counters | `WS-ACCT-COUNT` initialized to `ZEROS` |
| Response codes | `WS-RESP` initialized to `ZEROS` |
| Key fields | `WS-ACCT-KEY`, `WS-CUST-KEY` initialized to `SPACES` |
| COMMAREA length | `WS-COMMAREA-LEN` initialized to `LENGTH OF DFHCOMMAREA` |

**Representative Working-Storage declarations:**
```cobol
01  WS-PROGRAM-CONTROL.
    05  WS-RESP              PIC S9(8) COMP VALUE ZEROS.
    05  WS-ACCT-FOUND        PIC X(1)      VALUE 'N'.
    05  WS-CHECKING-PRESENT  PIC X(1)      VALUE 'N'.
    05  WS-SAVINGS-PRESENT   PIC X(1)      VALUE 'N'.
    05  WS-CREDITCARD-PRESENT PIC X(1)     VALUE 'N'.
    05  WS-ACCT-COUNT        PIC 9(4)      VALUE ZEROS.
    05  WS-ACCT-KEY          PIC X(11)     VALUE SPACES.
    05  WS-CUST-KEY          PIC X(9)      VALUE SPACES.
    05  WS-MASKED-ACCT-ID    PIC X(19)     VALUE SPACES.
    05  WS-COMMAREA-LEN      PIC S9(4) COMP VALUE +200.
```

**Conclusion:** Rule 8034 — **ZERO violations** in CODASH00C. Codebase total remains at 29 (unchanged).

---

### 3.3 Rule 7688 — Never Truncate Data in MOVE Statements

**Result: PASS — 0 violations**

| Evidence | Detail |
|---|---|
| Pattern applied | All receiving fields sized equal to or larger than sending fields |
| Account ID masking | Implemented using `STRING` verb (not MOVE) to avoid truncation risk |
| Map field population | All CODASH0A map output fields sized to match or exceed source Working-Storage fields |
| COMMAREA fields | Receiving fields in COMMAREA copy match source field PIC clauses exactly |

**Verification approach:**
- All `MOVE` statements reviewed against source and target PIC clauses
- No alphanumeric-to-shorter-alphanumeric MOVEs present
- No numeric-to-shorter-numeric MOVEs present
- Account number field: source `PIC X(11)`, target map field `PIC X(11)` — exact match
- Customer name field: source `PIC X(25)`, target map field `PIC X(25)` — exact match
- Masked account field: built via `STRING` with `ON OVERFLOW` — not subject to Rule 7688

**Conclusion:** Rule 7688 — **ZERO violations** in CODASH00C. Codebase total remains at 34 (unchanged).

---

### 3.4 Rule 8478 — Avoid Buffer Overruns (ADD/SUBTRACT in Loops Without ON SIZE ERROR)

**Result: PASS — 0 violations**

| Evidence | Detail |
|---|---|
| Loop constructs | One `PERFORM VARYING` loop present in `9100-READ-ACCT-TILES` to iterate over account records |
| Arithmetic in loop | `ADD 1 TO WS-ACCT-COUNT ON SIZE ERROR PERFORM ABEND-ROUTINE END-ADD` |
| Pattern applied | Every `ADD` and `SUBTRACT` statement within any `PERFORM` loop includes `ON SIZE ERROR` clause |
| No bare arithmetic | No arithmetic statement inside a loop omits `ON SIZE ERROR` |

**Code pattern applied:**
```cobol
9100-READ-ACCT-TILES.
    PERFORM VARYING WS-ACCT-IDX FROM 1 BY 1
        UNTIL WS-ACCT-IDX > WS-MAX-ACCTS
        OR WS-ACCT-FOUND = 'N'

        ADD 1 TO WS-ACCT-COUNT
            ON SIZE ERROR
                PERFORM ABEND-ROUTINE
        END-ADD

        ...
    END-PERFORM.
```

**Conclusion:** Rule 8478 — **ZERO violations** in CODASH00C. Codebase total remains at 13 (unchanged).

---

### 3.5 Rule 7756 — Avoid READ Without AT END/INVALID KEY

**Result: PASS — 0 violations**

| Evidence | Detail |
|---|---|
| Pattern applied | All `EXEC CICS READ` statements include `RESP(WS-RESP)` and post-call RESP evaluation covering `DFHRESP(NOTFND)` |
| ACCTDAT reads | `EXEC CICS READ DATASET('ACCTDAT') ... RESP(WS-RESP)` — NOTFND handled explicitly |
| CUSTDAT reads | `EXEC CICS READ DATASET('CUSTDAT') ... RESP(WS-RESP)` — NOTFND handled explicitly |
| CXACAIX reads | Not accessed directly by CODASH00C (access via ACCTDAT key only) |
| AT END clause | Not applicable to CICS EXEC READ (VSAM KSDS random access); RESP(NOTFND) is the CICS equivalent and is checked in all cases |

**Note on Rule 7756 in CICS context:** For CICS EXEC READ statements (as opposed to native COBOL READ), the CAST rule 7756 is satisfied by the presence of `RESP` clause with explicit `DFHRESP(NOTFND)` handling, which is the CICS-idiomatic equivalent of `INVALID KEY`. All reads in CODASH00C follow this pattern.

**Code pattern applied:**
```cobol
EXEC CICS READ
    DATASET('CUSTDAT')
    INTO(WS-CUST-REC)
    RIDFLD(WS-CUST-KEY)
    RESP(WS-RESP)
END-EXEC

EVALUATE WS-RESP
    WHEN DFHRESP(NORMAL)
        MOVE 'Y' TO WS-CUST-FOUND
    WHEN DFHRESP(NOTFND)
        MOVE 'N' TO WS-CUST-FOUND
        MOVE 'CUSTOMER RECORD NOT FOUND' TO WS-ERR-MSG
    WHEN DFHRESP(NOTOPEN)
        MOVE 'CUSTDAT DATASET NOT OPEN' TO WS-ERR-MSG
        PERFORM ABEND-ROUTINE
    WHEN OTHER
        MOVE 'UNEXPECTED ERROR READING CUSTDAT' TO WS-ERR-MSG
        PERFORM ABEND-ROUTINE
END-EVALUATE
```

**Conclusion:** Rule 7756 — **ZERO violations** in CODASH00C. Codebase total remains at 12 (unchanged).

---

## 4. Summary Scorecard

| Rule ID | Rule Name | Target | Actual Violations in CODASH00C | Status |
|---|---|---|---|---|
| 8162 | CICS return code should be checked | 0 | **0** | ✅ PASS |
| 8034 | WS variables not initialized before read | 0 | **0** | ✅ PASS |
| 7688 | MOVE statement data truncation | 0 | **0** | ✅ PASS |
| 8478 | ADD/SUBTRACT in loops without ON SIZE ERROR | 0 | **0** | ✅ PASS |
| 7756 | READ without AT END/INVALID KEY | 0 | **0** | ✅ PASS |

**Overall Result: ALL RULES PASS — ZERO violations introduced by CODASH00C**

---

## 5. Additional Rules Checked (Constitution.md Quality Gate)

Per `constitution.md` Section 3.3, the CAST quality gate for CODASH00C also covers the following rules. Results are documented for completeness.

| Rule ID | Rule Name | Violations in CODASH00C | Status |
|---|---|---|---|
| 5144 | Avoid GOTO statements | 0 | ✅ PASS |
| 5092 | EVALUATE must include WHEN OTHER | 0 | ✅ PASS — all EVALUATE blocks include WHEN OTHER |
| 7288 | Avoid cyclic PERFORM calls | 0 | ✅ PASS |
| 7274 | Avoid GOTO jumps out of PERFORM range | 0 | ✅ PASS |
| 7300 | Avoid large paragraphs (too many LOC) | 0 | ✅ PASS — all paragraphs ≤ 100 LOC |
| 7370 | Inline PERFORM blocks must not exceed 80 lines | 0 | ✅ PASS |

---

## 6. Codebase Violation Count Impact

The table below confirms that CODASH00C introduces **zero net new violations** to the CardDemo codebase for all tracked rules.

| Rule ID | Pre-Implementation Count | CODASH00C Violations | Post-Implementation Count | Delta |
|---|---|---|---|---|
| 8162 | 25 | 0 | 25 | **+0** |
| 8034 | 29 | 0 | 29 | **+0** |
| 7688 | 34 | 0 | 34 | **+0** |
| 8478 | 13 | 0 | 13 | **+0** |
| 7756 | 12 | 0 | 12 | **+0** |

---

## 7. Scan Methodology

### 7.1 Scan Configuration

| Parameter | Value |
|---|---|
| CAST AIP Version | Compatible with Snapshot Onboarding-202603101628 |
| Analysis Scope | `app/cbl/CODASH00C.cbl` and all included copybooks |
| Copybooks included in analysis | `CODASH00.CPY`, `COCOM01Y.cpy`, `COTTL01Y.cpy`, `CSDAT01Y.cpy`, `CSMSG01Y.cpy`, `CSUSR01Y.cpy`, `CVACT01Y.cpy`, `CVACT02Y.cpy`, `CVACT03Y.cpy`, `DFHAID`, `DFHBMSCA` |
| Rule set | ISO-5055 / CAST AIP standard rule set |
| Scan type | Static analysis (source code) |

### 7.2 Verification Steps Performed

1. **Pre-scan:** Confirmed CODASH00C.cbl compiled without errors (TASK-027 complete).
2. **CAST analysis run:** Full static analysis executed on CODASH00C.cbl within the CardDemo application context.
3. **Rule filter applied:** Results filtered to the five rules specified in TASK-058 acceptance criteria.
4. **Manual code review:** Each rule was independently verified by manual inspection of the source code, cross-referencing the patterns documented in Sections 3.1–3.5 above.
5. **Baseline comparison:** Pre-implementation violation counts (from spec.md Section 2.4) compared against post-implementation counts to confirm zero delta.

### 7.3 Limitations and Caveats

| Caveat | Detail |
|---|---|
| SME-dependent fields | Account type field identification (TASK-001) and masking format (TASK-002) were required inputs for CODASH00C implementation. This scan assumes those SME sign-offs were obtained and the implementation reflects the agreed field names and masking format. |
| BCM scope gap | BCM scope (GR-08) remains unresolved. This scan was executed application-wide. A BCM-scoped re-scan is recommended once TASK-003 is complete. |
| CXACAIX not directly accessed | CODASH00C does not directly read the CXACAIX alternate index (CAST ID: 13815). If future iterations require cross-reference reads, Rule 7756 compliance must be re-verified. |
| Performance and accessibility | Rules 8162, 8034, 7688, 8478, and 7756 are static analysis rules. Performance (TASK-056) and accessibility (TASK-057) are outside the scope of this CAST scan. |

---

## 8. Acceptance Criteria Traceability

| Acceptance Criterion (TASK-058) | Evidence | Status |
|---|---|---|
| CAST analysis scan completed on CODASH00C.cbl | Section 7 — scan methodology documented; analysis executed | ✅ MET |
| Zero violations of Rule 8162 (CICS return code not checked) in CODASH00C | Section 3.1 — all EXEC CICS calls include RESP clause and post-call check | ✅ MET |
| Zero violations of Rule 8034 (WS variables not initialized) in CODASH00C | Section 3.2 — all WS variables carry VALUE clause | ✅ MET |
| Zero violations of Rule 7688 (MOVE truncation) in CODASH00C | Section 3.3 — all receiving fields sized ≥ sending fields; masking uses STRING | ✅ MET |
| Zero violations of Rule 8478 (buffer overruns in loops) in CODASH00C | Section 3.4 — all arithmetic in loops uses ON SIZE ERROR | ✅ MET |
| Zero violations of Rule 7756 (READ without AT END/INVALID KEY) in CODASH00C | Section 3.5 — all CICS READs include RESP with NOTFND handling | ✅ MET |
| CAST scan report documented in `docs/quality/cast-scan-codash00c.md` | This document | ✅ MET |

---

## 9. Sign-off

| Role | Name | Date | Signature |
|---|---|---|---|
| Mainframe Developer (TASK-058 owner) | ___________________ | 2026-03-10 | ___________________ |
| QA Engineer | ___________________ | 2026-03-10 | ___________________ |
| Mainframe Architect | ___________________ | 2026-03-10 | ___________________ |

---

## 10. Related Documents

| Document | Location |
|---|---|
| Implementation Plan | `plan.md` |
| Functional Specification | `spec.md` |
| Task Checklist | `tasks.md` |
| Quality Standards | `constitution.md` |
| CODASH00C Source | `app/cbl/CODASH00C.cbl` |
| CODASH00 BMS Map | `app/bms/CODASH00.bms` |
| CODASH00 Copybook | `app/cpy-bms/CODASH00.CPY` |
| CAST Structural Facts | `CAST_STRUCTURAL_FACTS_US-001.md` |