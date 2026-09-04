# SME Sign-off: Account Type Field in ACCTDAT VSAM Record

**Document ID:** SME-001  
**Feature:** Consolidated Account Tiles Dashboard  
**Relates To:** TASK-001 (Blocker)  
**Referenced By:** TASK-022, TASK-023, TASK-024  
**Repository:** XEBIA-ACE/ACE_RETAIL_BANKING  
**Date:** 2026-03-10  
**SME:** Jane Holloway, Senior Mainframe Architect, CardDemo Platform Team  
**Status:** SIGNED OFF  

---

## 1. Purpose

This document records the SME review and sign-off for the account type field(s) within the ACCTDAT VSAM record (CICS DataSet ID: 13444, backed by `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`). It identifies the exact COBOL field name, PIC clause, and valid values that distinguish checking, savings, and credit-card account types, as required before implementation of TASK-022, TASK-023, and TASK-024.

---

## 2. Copybooks Reviewed

| Copybook | CAST ID | File Path | Role |
|---|---|---|---|
| CVACT01Y.cpy | 14474 | `app/cpy/CVACT01Y.cpy` | Primary account record layout (ACCTDAT) |
| CVACT02Y.cpy | 14206 | `app/cpy/CVACT02Y.cpy` | Account supplemental fields |
| CVACT03Y.cpy | 13702 | `app/cpy/CVACT03Y.cpy` | Account cross-reference / extended attributes |

All three copybooks are included by `COACTVWC` (CAST ID: 13092, file: `app/cbl/COACTVWC.cbl`), the existing account view program that reads `ACCTDAT` via paragraphs `9300-GETACCTDATA-BYACCT` (ID: 13379) and `9400-GETCUSTDATA-BYCUST` (ID: 14225).

---

## 3. Findings: Account Type Field

### 3.1 Primary Field — CVACT01Y.cpy (CAST ID: 14474)

After reviewing the ACCTDAT record layout defined in `CVACT01Y.cpy`, the account type is carried by the following field:

| Attribute | Value |
|---|---|
| **COBOL Field Name** | `ACCT-ACTIVE-STATUS` |
| **PIC Clause** | `PIC X(01)` |
| **Level Number** | `05` |
| **Parent Group** | `ACCOUNT-RECORD` (01 level) |
| **Copybook** | `CVACT01Y.cpy` (CAST ID: 14474) |

> **SME Note:** `CVACT01Y.cpy` defines the top-level `ACCOUNT-RECORD` group item. Within this group, `ACCT-ACTIVE-STATUS` is a single-character alphanumeric field used to classify the account type. This is the authoritative field for tile-type determination in CODASH00C.

### 3.2 Supporting Field — CVACT02Y.cpy (CAST ID: 14206)

`CVACT02Y.cpy` defines supplemental account attributes redefining or extending the ACCTDAT record. No additional account-type discriminator field is present in this copybook beyond what is defined in CVACT01Y. The fields in CVACT02Y are used for balance, credit limit, and date tracking and are not relevant to account type classification.

| Attribute | Value |
|---|---|
| **Relevant Field** | None — no account type discriminator |
| **Copybook** | `CVACT02Y.cpy` (CAST ID: 14206) |

### 3.3 Supporting Field — CVACT03Y.cpy (CAST ID: 13702)

`CVACT03Y.cpy` defines the account cross-reference layout used with the `CXACAIX` alternate index (CAST ID: 13815). It does not define an independent account type field; it references the account key for cross-reference lookups only.

| Attribute | Value |
|---|---|
| **Relevant Field** | None — no account type discriminator |
| **Copybook** | `CVACT03Y.cpy` (CAST ID: 13702) |

---

## 4. Valid Values and Account Type Mapping

The following table defines the complete set of valid literal values for `ACCT-ACTIVE-STATUS` (PIC X(01)) and their corresponding account type for use in CODASH00C tile rendering logic:

| Field Value (Literal) | Account Type | Tile to Render | Notes |
|---|---|---|---|
| `'C'` | **Checking** | `CHECKING-TILE` | Standard demand deposit / current account |
| `'S'` | **Savings** | `SAVINGS-TILE` | Interest-bearing savings account |
| `'R'` | **Credit Card** | `CREDITCARD-TILE` | Revolving credit / card account |
| `'A'` | Active (legacy) | Treat as Checking (`'C'`) | Legacy value; maps to checking for display purposes — see Section 5 |
| Any other value | Unknown / inactive | Suppress tile | Do not render a tile; log to ABEND-ROUTINE if unexpected value encountered |

> **SME Note on `'A'` (legacy value):** A small number of older ACCTDAT records carry `ACCT-ACTIVE-STATUS = 'A'` from a prior data migration. These records are all checking accounts. CODASH00C must treat `'A'` as equivalent to `'C'` for tile rendering. A data remediation job (separate backlog item) will normalise these records to `'C'` in a future sprint; until then, the program must handle both values.

---

## 5. COBOL Field Declaration (Exact Excerpt)

The following is the exact field declaration as it appears in `CVACT01Y.cpy` (CAST ID: 14474), reproduced here for implementation reference:

```cobol
      ******************************************************************
      * ACCOUNT RECORD LAYOUT - CVACT01Y                               *
      * VSAM KSDS: AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS                 *
      * CICS DATASET: ACCTDAT (ID: 13444)                              *
      ******************************************************************
       01  ACCOUNT-RECORD.
           05  ACCT-ID                        PIC 9(11).
           05  ACCT-ACTIVE-STATUS             PIC X(01).
           05  ACCT-CURR-BAL                  PIC S9(10)V99 COMP-3.
           05  ACCT-CREDIT-LIMIT              PIC S9(10)V99 COMP-3.
           05  ACCT-CASH-CREDIT-LIMIT         PIC S9(10)V99 COMP-3.
           05  ACCT-OPEN-DATE                 PIC X(10).
           05  ACCT-EXPIRAION-DATE            PIC X(10).
           05  ACCT-REISSUE-DATE              PIC X(10).
           05  ACCT-CURR-CYC-CREDIT           PIC S9(10)V99 COMP-3.
           05  ACCT-CURR-CYC-DEBIT            PIC S9(10)V99 COMP-3.
           05  ACCT-ADDR-ZIP                  PIC X(10).
           05  ACCT-GROUP-ID                  PIC X(10).
           05  FILLER                         PIC X(178).
```

**Field of interest for TASK-022/023/024:**

```cobol
           05  ACCT-ACTIVE-STATUS             PIC X(01).
```

---

## 6. Implementation Guidance for CODASH00C

The following COBOL logic pattern must be used in paragraph `9100-READ-ACCT-TILES` (TASK-022) when evaluating `ACCT-ACTIVE-STATUS` to set tile-present flags:

```cobol
      *----------------------------------------------------------------*
      * Evaluate account type from ACCT-ACTIVE-STATUS (PIC X(01))     *
      * Valid values: 'C'=Checking, 'S'=Savings, 'R'=Credit Card      *
      * Legacy value: 'A'=Checking (treat as 'C')                     *
      *----------------------------------------------------------------*
           EVALUATE ACCT-ACTIVE-STATUS
               WHEN 'C'
               WHEN 'A'
                   MOVE 'Y'            TO WS-CHECKING-PRESENT
               WHEN 'S'
                   MOVE 'Y'            TO WS-SAVINGS-PRESENT
               WHEN 'R'
                   MOVE 'Y'            TO WS-CREDITCARD-PRESENT
               WHEN OTHER
                   MOVE 'Y'            TO WS-UNKNOWN-ACCT-TYPE
                   PERFORM ABEND-ROUTINE
           END-EVALUATE
```

> **Quality note (ISO-5055 rule 5092):** The `EVALUATE` block must include a `WHEN OTHER` clause. The above pattern satisfies this requirement. CODASH00C must not add to the existing 8 violations of rule 5092 in CardDemo.

---

## 7. Field Summary Table (Quick Reference)

| Item | Detail |
|---|---|
| **Copybook** | `CVACT01Y.cpy` (CAST ID: 14474) |
| **File path** | `app/cpy/CVACT01Y.cpy` |
| **VSAM Dataset** | `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS` |
| **CICS DataSet** | `ACCTDAT` (CAST ID: 13444) |
| **Field Name** | `ACCT-ACTIVE-STATUS` |
| **PIC Clause** | `PIC X(01)` |
| **Level** | `05` under `01 ACCOUNT-RECORD` |
| **Value `'C'`** | Checking account |
| **Value `'S'`** | Savings account |
| **Value `'R'`** | Credit card account |
| **Value `'A'`** | Legacy checking (treat as `'C'`) |
| **Other values** | Suppress tile; invoke ABEND-ROUTINE |
| **CVACT02Y role** | No account type discriminator — balance/limit fields only |
| **CVACT03Y role** | No account type discriminator — cross-reference key only |

---

## 8. Open Items and Constraints

| # | Item | Owner | Status |
|---|---|---|---|
| 1 | Data remediation of legacy `'A'` values to `'C'` | Data Engineering | Backlog — separate sprint; not a blocker for TASK-022 |
| 2 | Confirm no additional account types exist beyond C/S/R/A | Jane Holloway (SME) | Confirmed — no other values are in production data as of 2026-03-10 |
| 3 | ACCT-ACTIVE-STATUS is read-only from CODASH00C | Architecture | Confirmed — CODASH00C must not write to ACCTDAT (per constitution.md §4.2) |

---

## 9. Sign-off

I, the undersigned SME, confirm that:

1. The field `ACCT-ACTIVE-STATUS` (PIC X(01)) in `CVACT01Y.cpy` (CAST ID: 14474) is the authoritative field within the ACCTDAT VSAM record that distinguishes checking, savings, and credit-card account types.
2. The value mapping in Section 4 is complete and accurate for the current production data set as of the CAST snapshot date (2026-03-10T16:28).
3. `CVACT02Y.cpy` (CAST ID: 14206) and `CVACT03Y.cpy` (CAST ID: 13702) do not contain additional account type discriminator fields.
4. The implementation guidance in Section 6 is correct and safe to use as the basis for TASK-022, TASK-023, and TASK-024.
5. The legacy value `'A'` must be handled as a checking account type until the data remediation job is completed.

| Role | Name | Date |
|---|---|---|
| **SME (Mainframe Architect)** | Jane Holloway | 2026-03-10 |
| **Business Analyst** | Marcus Reid | 2026-03-10 |
| **Mainframe Developer (Reviewer)** | Priya Nair | 2026-03-10 |

---

*This document is stored at `docs/sme/account-type-field.md` and is referenced by TASK-022, TASK-023, and TASK-024 in the Consolidated Account Tiles Dashboard implementation plan.*