# Quality Standards and Design Principles
## Consolidated Account Tiles Dashboard — CardDemo

---

## 1. Code Conventions (CardDemo COBOL/CICS Standards)

### 1.1 Program Structure
- All new COBOL programs must follow the paragraph naming convention observed in existing CardDemo programs: `NNNN-PARAGRAPH-NAME` for numbered paragraphs, `NNNN-PARAGRAPH-NAME-EXIT` for exit paragraphs.
- Entry paragraph must be `0000-MAIN` or `MAIN-PARA` (consistent with COACTVWC, ID: 13092; COMEN01C, ID: 13577).
- All programs must include a `COMMON-RETURN` paragraph and an `ABEND-ROUTINE` paragraph.
- Programs must use EXEC CICS XCTL (not CALL) for inter-program transfers, consistent with the EXEC_TRAN link type observed throughout CardDemo.

### 1.2 CopyBook Inclusion
- All new programs must include the standard CardDemo copybooks: COCOM01Y.cpy (ID: 13504), COTTL01Y.cpy (ID: 14116), CSDAT01Y.cpy (ID: 14153), CSMSG01Y.cpy (ID: 13263), CSUSR01Y.cpy (ID: 13507), DFHAID (ID: 1003), DFHBMSCA (ID: 1002).
- BMS-generated copybooks must be stored in `app/cpy-bms/`.
- Data layout copybooks must be stored in `app/cpy/`.

### 1.3 WORKING-STORAGE
- All WORKING-STORAGE variables must be initialized with a VALUE clause (ISO-5055 rule 8034 — 29 existing violations in CardDemo; new code must not add to this count).
- Group items must have VALUE SPACES or VALUE LOW-VALUES as appropriate.

### 1.4 CICS Programming
- Every EXEC CICS statement must include a RESP clause and the RESP code must be checked immediately after (ISO-5055 rule 8162 — 25 existing violations; new code must not add to this count).
- All VSAM READ statements must include an INVALID KEY clause (ISO-5055 rule 7756 — 12 existing violations; new code must not add to this count).
- All VSAM files must be declared with FILE-STATUS (ISO-5055 rule 7698).

### 1.5 Data Movement
- MOVE statements must not truncate data — receiving field must be at least as large as the sending field (ISO-5055 rule 7688 — 34 existing violations; new code must not add to this count).
- STRING verb must always include ON OVERFLOW clause (ISO-5055 rule 8470).
- Arithmetic statements inside PERFORM loops must use ON SIZE ERROR (ISO-5055 rule 8478 — 13 existing violations; new code must not add to this count).

### 1.6 Control Flow
- GOTO statements are prohibited in new code (ISO-5055 rule 5144 — 10 existing violations).
- EVALUATE statements must include a WHEN OTHER clause (ISO-5055 rule 5092 — 8 existing violations).
- Cyclic PERFORM calls are prohibited (ISO-5055 rule 7288).
- GOTO jumps out of PERFORM range are prohibited (ISO-5055 rule 7274).

### 1.7 Paragraph Size
- No paragraph may exceed 100 lines of code (ISO-5055 rule 7300 — 14 existing violations; new code must not add to this count).
- Inline PERFORM blocks must not exceed 80 lines (ISO-5055 rule 7370).

---

## 2. Security Standards

### 2.1 Session Validation
- Every new CICS transactional program must validate the COMMAREA session token on entry. If absent or expired, the program must XCTL to COSGN00C (ID: 13954) before accessing any data.
- No account data may be rendered before session validation is confirmed.

### 2.2 Account Data Masking
- Account identifiers displayed on any CICS map must be masked per the format agreed in TASK-002. No unmasked account numbers may appear on any screen.

### 2.3 Audit Logging
- ⚠️ SME validation required: Confirm whether CICS audit logging (e.g., SMF records) is required for dashboard access events. If required, add EXEC CICS WRITE OPERATOR or equivalent.

---

## 3. Test Coverage Standards

### 3.1 Minimum Coverage
- All acceptance criteria (AC1–AC7) must have at least one automated or manual test case.
- All CICS RESP code paths (NORMAL, NOTFND, NOTOPEN, IOERR) must be tested for each EXEC CICS READ.
- Session validation must be tested with: no COMMAREA, expired token, valid token.

### 3.2 Regression
- All 25 inward callers of COMEN01C (ID: 13577) must be regression-tested after any modification to that program.
- All 15 inward callers of COADM01C (ID: 14029) must be regression-tested after any modification to that program.

### 3.3 CAST Quality Gate
- After implementation, a CAST re-analysis must be run. The new program CODASH00C must have zero violations of rules: 8162, 8034, 7688, 8478, 7756, 5144, 5092, 7288, 7274, 7300, 7370.

---

## 4. Backward Compatibility

### 4.1 Existing Transactions
- No existing CICS transaction (CA00, CM00, CAVW, CAUP, CC00, etc.) may be modified in a way that changes its behavior for existing users.
- Menu option numbers in COMEN01C and COADM01C must not be renumbered — the new dashboard option must be appended as the next available number.

### 4.2 Data Stores
- CODASH00C must access ACCTDAT (ID: 13444) and CUSTDAT (ID: 14127) in READ-ONLY mode. No writes to these datasets are permitted by the dashboard program.
- No schema changes to VSAM files are permitted.

### 4.3 CSD
- No existing CSD entries may be modified. New entries (CD00, CODASH00) are additive only.

---

## 5. Compliance Standards

### 5.1 GDPR
- Account data displayed on the dashboard (account type, masked identifier) must be included in the GDPR data inventory. Confirm with compliance that GDPR export/deletion procedures cover ACCTDAT (ID: 13444) and CUSTDAT (ID: 14127) records.

### 5.2 BCM Gap
- The absence of a BCM scope (GR-08) is a standing compliance gap. This must be resolved before the feature is promoted to production.

---

## 6. Documentation Standards

- All new paragraphs must have inline comments explaining their purpose (consistent with the comment style observed in COTRTLIC and COCRDUPC source code comments retrieved by CAST).
- The CODASH00C program header must document: program name, author, date, purpose, COMMAREA layout, and list of included copybooks.
- Post-implementation: add a CAST post-it/document to CODASH00C describing its role in the dashboard flow.
