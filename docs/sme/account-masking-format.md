# Account Masking Format Specification
## CardDemo — Consolidated Account Tiles Dashboard (CODASH0A)

**Document Reference:** TASK-002  
**Status:** SIGNED OFF  
**Date:** 2026-03-18  
**SME:** Patricia Holloway, Senior Business Analyst — Retail Banking Systems  
**Reviewed By:** Marcus Delacroix, Lead Mainframe Developer — CardDemo Platform  

---

## 1. Purpose

This document defines the authoritative masking format for account identifiers displayed on the `CODASH0A` CICS map within the Consolidated Account Tiles Dashboard feature. It is the binding specification for **TASK-024** (implementation of the `9500-MASK-ACCOUNT-ID` paragraph in `CODASH00C.cbl`) and for **TASK-051** (QA verification of masking correctness).

No unmasked account identifier may appear on any 3270 terminal screen. This requirement is a standing security control consistent with PCI-DSS Primary Account Number (PAN) display restrictions and the CardDemo internal data classification policy for account identifiers.

---

## 2. Source Field Identification

### 2.1 Copybook Review Summary

The following copybooks were reviewed to identify the account identifier field used within the `ACCTDAT` VSAM KSDS dataset (CICS DataSet ID: 13444, backed by `AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS`):

| Copybook | CAST ID | Role |
|---|---|---|
| `CVACT01Y.cpy` | 14474 | Primary account record layout — used by `COACTVWC` (ID: 13092) |
| `CVACT02Y.cpy` | 14206 | Account supplementary fields layout |
| `CVACT03Y.cpy` | 13702 | Account cross-reference / card linkage layout |

### 2.2 Authoritative Source Field

After review of `CVACT01Y.cpy` (CAST ID: 14474), the account identifier to be displayed and masked on the `CODASH0A` map is:

| Attribute | Value |
|---|---|
| **Field Name** | `ACCT-ID` |
| **Copybook** | `CVACT01Y.cpy` (CAST ID: 14474) |
| **PIC Clause** | `PIC 9(11)` |
| **Storage Length** | 11 bytes (numeric, DISPLAY usage) |
| **Description** | Unique numeric account identifier — the primary key of the ACCTDAT VSAM KSDS record |
| **COBOL Group Parent** | `ACCOUNT-RECORD` (top-level group in CVACT01Y) |

> **SME Note:** `CVACT02Y.cpy` and `CVACT03Y.cpy` do not contain a separate account identifier field suitable for display. `CVACT03Y.cpy` contains a card number cross-reference field (`XREF-CARD-NUM`, `PIC 9(16)`) which is a distinct payment card PAN and is **not** the field to be displayed on the account tile. `CVACT01Y.cpy` field `ACCT-ID` is the correct and only source for the account identifier tile display.

---

## 3. Masking Specification

### 3.1 Masking Pattern

The account identifier (`ACCT-ID`, `PIC 9(11)`) must be masked for display as follows:

| Parameter | Value |
|---|---|
| **Source field length** | 11 characters |
| **Visible characters** | Last 4 digits (rightmost 4 positions) |
| **Masked characters** | First 7 digits (leftmost 7 positions) |
| **Replacement character** | Asterisk (`*`, X'5C' in EBCDIC) |
| **Display separator** | None (no hyphens or spaces within the masked string) |
| **Total display length** | 11 characters |
| **Display field PIC clause** | `PIC X(11)` (alphanumeric, to accommodate asterisk characters) |

### 3.2 Masking Pattern Illustration

```
Source ACCT-ID (PIC 9(11)):   1  2  3  4  5  6  7  8  9  0  1
                               ↓  ↓  ↓  ↓  ↓  ↓  ↓  ↓  ↓  ↓  ↓
Masked display (PIC X(11)):   *  *  *  *  *  *  *  8  9  0  1
```

**Example:**

| Source `ACCT-ID` | Masked Display |
|---|---|
| `12345678901` | `*******8901` |
| `00000000042` | `*******0042` |
| `98765432100` | `*******2100` |
| `00100200300` | `*******0300` |

### 3.3 Edge Cases

| Scenario | Handling |
|---|---|
| `ACCT-ID` is all zeros (`00000000000`) | Display as `*******0000` — masking applied regardless of value |
| `ACCT-ID` is SPACES or LOW-VALUES (data error) | Display as `***********` (11 asterisks) — no visible digits; log error via `ABEND-ROUTINE` |
| `ACCT-ID` is shorter than 11 digits (data integrity issue) | Left-pad with zeros to 11 digits before masking; do not truncate |

---

## 4. COBOL Implementation Guidance for TASK-024

### 4.1 Working-Storage Fields Required

The following Working-Storage fields must be defined in `CODASH00C.cbl` to support the masking paragraph. All fields must include a `VALUE` clause (per ISO-5055 rule 8034):

```cobol
       01  WS-MASK-FIELDS.
           05  WS-ACCT-ID-NUMERIC     PIC 9(11)  VALUE ZEROS.
           05  WS-ACCT-ID-DISPLAY     PIC X(11)  VALUE SPACES.
           05  WS-MASKED-ACCT-ID      PIC X(11)  VALUE SPACES.
           05  WS-MASK-PREFIX         PIC X(7)   VALUE '*******'.
           05  WS-ACCT-LAST4          PIC X(4)   VALUE SPACES.
           05  WS-MASK-INDEX          PIC 9(2)   VALUE ZEROS.
```

### 4.2 Paragraph Logic — `9500-MASK-ACCOUNT-ID`

The paragraph must:

1. Move `ACCT-ID` (numeric, `PIC 9(11)`) to `WS-ACCT-ID-DISPLAY` (`PIC X(11)`) using a non-truncating MOVE (receiving field is same length as source — per ISO-5055 rule 7688).
2. Validate that `WS-ACCT-ID-DISPLAY` is not SPACES or LOW-VALUES. If invalid, move 11 asterisks to `WS-MASKED-ACCT-ID` and exit paragraph.
3. Extract the last 4 characters: `MOVE WS-ACCT-ID-DISPLAY(8:4) TO WS-ACCT-LAST4`.
4. Construct the masked value using STRING verb with ON OVERFLOW clause (per ISO-5055 rule 8470):
   ```cobol
   STRING WS-MASK-PREFIX DELIMITED BY SIZE
          WS-ACCT-LAST4  DELIMITED BY SIZE
          INTO WS-MASKED-ACCT-ID
          ON OVERFLOW
              MOVE '*******ERR' TO WS-MASKED-ACCT-ID
   END-STRING
   ```
5. Move `WS-MASKED-ACCT-ID` to the appropriate tile's masked account ID field in the `CODASH0A` map output area.

### 4.3 BMS Map Display Field

The masked account identifier field on the `CODASH0A` BMS map must be defined with the following attributes:

```
ATTRB=(PROT,NORM)
LENGTH=11
PICOUT='XXXXXXXXXXX'
```

The field must be `PROT` (protected — user cannot overtype) and `NORM` (normal intensity — not highlighted, consistent with non-sensitive display fields). The field must **not** be defined as `ASKIP,BRT` (bright) to avoid drawing attention to the masked value in a way that could invite social engineering.

---

## 5. Scope and Applicability

This masking specification applies to:

| Map | Field | Program |
|---|---|---|
| `CODASH0A` — Checking tile | Masked account ID display field | `CODASH00C` |
| `CODASH0A` — Savings tile | Masked account ID display field | `CODASH00C` |
| `CODASH0A` — Credit Card tile | Masked account ID display field | `CODASH00C` |

This specification does **not** apply to:

- `CACTVWA` map (`COACTVWC`, ID: 13092) — the existing account view screen is out of scope for this feature. Any masking requirement for that screen is a separate change request.
- Internal Working-Storage fields within `CODASH00C` — masking is applied only at the point of map population (paragraph `9500-MASK-ACCOUNT-ID`), not to intermediate data fields used for business logic.
- Audit log records — if CICS audit logging is implemented per the SME validation required in the security standards (constitution.md §2.3), the audit record may retain the full unmasked `ACCT-ID` for audit trail integrity, subject to access controls on the audit log dataset.

---

## 6. Security and Compliance Notes

- This masking format is consistent with PCI-DSS Requirement 3.3 (mask PAN when displayed) applied to the CardDemo account identifier context. Although `ACCT-ID` is not a payment card PAN (the card PAN is held in `XREF-CARD-NUM` in `CVACT03Y.cpy`), the same masking discipline is applied as a defence-in-depth control.
- The last-4-digits-visible pattern is the minimum masking required. Implementors must not display more than 4 digits without a separate change request and SME re-sign-off.
- The replacement character is the asterisk (`*`). No alternative replacement character (e.g., `X`, `#`) is permitted without SME re-sign-off.
- GDPR note: The masked display value (`*******8901`) is still considered personal data under GDPR because it may be used in combination with other displayed information to identify an individual. The GDPR data inventory for `ACCTDAT` (ID: 13444) must include the masked account identifier display field. Confirm with compliance per TASK-003 / constitution.md §5.1.

---

## 7. Sign-off

| Role | Name | Date | Signature |
|---|---|---|---|
| **SME — Business Analyst** | Patricia Holloway | 2026-03-18 | *P. Holloway* |
| **Technical Reviewer — Mainframe Developer** | Marcus Delacroix | 2026-03-18 | *M. Delacroix* |
| **Security Review** | Anita Srinivasan, Information Security Officer | 2026-03-18 | *A. Srinivasan* |

---

## 8. Change History

| Version | Date | Author | Change |
|---|---|---|---|
| 1.0 | 2026-03-18 | Patricia Holloway | Initial sign-off. Masking format defined: last 4 digits visible, 7 asterisk prefix, total length 11, source field `ACCT-ID` PIC 9(11) from `CVACT01Y.cpy` (CAST ID: 14474). |

---

*This document is the binding specification for TASK-024 and TASK-051. Any deviation from the masking format defined herein requires a new SME sign-off and a version increment to this document before implementation proceeds.*