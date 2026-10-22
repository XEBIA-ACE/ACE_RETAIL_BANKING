# Implementation Plan: US-010

## Technical Context
- Codebase grounding snapshot (20116e7dc952cc4e)
- unresolved reference (component:1290-CROSS-EDITS)
- unresolved reference (component:9100-CHECK-FILTERS)
- unresolved reference (component:9100-CHECK-FILTERS-EXIT)
- unresolved reference (component:9910-DISPLAY-IO-STATUS)
- unresolved reference (component:9999-ABEND-PROGRAM)
- unresolved reference (component:9999-FORMAT-DB2-MESSAGE)
- unresolved reference (component:<BEGINNING-OF-PROCEDURE-DIVISION>)
- unresolved reference (component:AWS.M2.CARDDEMO.TRANSACT.BKUP)
- unresolved reference (component:AWS.M2.CARDDEMO.TRANSACT.DALY)
- unresolved reference (component:C:\\cast-node\\common-data\\upload\\CardDemo\\main_sources\\app\\app-transaction-type-db2\\cbl\\COTRTLIC.cbl)
- unresolved reference (component:SORT)
- unresolved reference (component:STEP05R)
- unresolved reference (component:TRANSACTION_TYPE)
- Accepted impact analysis (sha256:a9b1dc9142b8c4a35904641b3a6060493332f62ac302444b15c97f58990ef2dd)
- Implement only against Accepted impact analysis (revision 1) (fingerprint sha256:a9b1dc9142b8c4a35904641b3a6060493332f62ac302444b15c97f58990ef2dd)
- Resolve or explicitly accept the following implementation-readiness items:
  - Candidate verification could not confidently judge the selected focus object for this requirement
  - Candidate verification was uncertain about the selected focus object: The requirement involves filtering and sorting notification history by type and time period, but the selected Cobol paragraph 9100-CHECK-FILTERS only indicates generic filter checking without explicit reference to notifications, types, or dates.
  - Database object query returned empty for this object in MCP, while the transaction graph shows DB2 utility DSNTIAC (855) and table TRANSACTION_TYPE (396); full DB access pattern for notification history is not visible.
  - How, if at all, 9100-CHECK-FILTERS (13772) is mapped to notification history concepts versus generic transaction-type filtering; CAST facts do not show domain semantics.
  - No intra-object details for 9100-CHECK-FILTERS, so the internal filter logic and any loop/SQL patterns are unknown.
  - No-match classification (all acceptance criteria unmapped): Initial classification (net-new capability) has a noted caveat: Filtering and sorting lists (like notifications) are common, foundational capabilities; the absence of results for the specific notification-related terms does not rule out a more generic list-filtering or transaction-history filtering component that could be reused.
  - Requirement evidence was gathered from 3 unrelated services/packages ('notification filter' (app-transaction-type-db2/cbl), 'notification type' (cbl/CBTRN03C.cbl), 'sort notifications' (jcl/SORTTEST.jcl)) -- the measured blast radius below may mix facts that do not all belong to the single focus object named above. Review each named symbol's own source before accepting.
  - Requirement traceability incomplete for US-010-ac-1, US-010-ac-2, US-010-ac-3, US-010-ac-4, US-010-ac-5
  - Whether additional paragraphs or programs outside the provided call graph implement notification history storage, retrieval, or sorting.

## Contract Changes
- TRANSACTION_TYPE (396) table is directly accessed by 9100-CHECK-FILTERS, so any change to filter behavior may affect how transaction-type data is read or interpreted. — change required

## Security and Quality
- Preserve the security controls evidenced by 20116e7dc952cc4e
- Preserve the availability, reliability, and observability constraints grounded by 20116e7dc952cc4e

## Verification Plan
- **US-010-ac-1**: Given an authenticated customer with a mix of large-transaction, low-balance, and bill-reminder notifications in history
When the customer applies a filter for a single notification type
Then only notifications of that selected type are displayed in the list
- **US-010-ac-2**: Given an authenticated customer with notifications across multiple months
When the customer selects a supported time-period filter (for example, last 30 days) and applies it
Then only notifications whose timestamps fall within the selected period are displayed
- **US-010-ac-3**: Given an authenticated customer viewing notification history with many entries
When the customer selects a sort order of newest-first
Then the list is displayed with the most recent notification at the top and ordering consistent with timestamps
- **US-010-ac-4**: Given filters and sorting are applied to the notification history
When the customer changes filter or sort options
Then the list updates without requiring the customer to leave and re-enter the history screen
- **US-010-ac-5**: Given normal supported load and a typical customer history size
When the customer applies or changes filters or sorting
Then the filtered/sorted list is returned within the app-wide response-time targets for interactive filtering (per NFR-001 to NFR-003)
- **IMPACT**: Regression-test CICS transaction CTLI (13242) along the measured transaction path 24146, focusing on 9100-CHECK-FILTERS (13772) behavior and its interaction with 1290-CROSS-EDITS (13780) and 9999-FORMAT-DB2-MESSAGE (14263).