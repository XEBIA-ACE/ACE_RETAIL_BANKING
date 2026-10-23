# Implementation Plan: US-009

## Status

> **⚠ DEGRADED / NET-NEW CAPABILITY**
> This is a net-new capability. The impact analysis (grounded against CAST snapshot `553b39f8ceb22553`, commit `3feadfcc0d3ed193c698fd4f2e06d49737f2770e`) found zero `affected_services`, `affected_symbols`, or `affected_files` for US-009. No existing notification history screen, notification list component, notification event store, or in-app notification delivery service was identified in the codebase. All design steps below describe work that must be built from scratch and are not traceable to any confirmed provider component.
>
> The architecture context is marked degraded (`missing_core: api_contracts`). No API contracts are available to ground interface design. The only known service entry is the repository-level service `https-github-com-xebia-ace-ace-retail-banking`; no sub-service decomposition or component inventory was returned.

---

## Technical Context

- Codebase grounding snapshot: `553b39f8ceb22553`
- Accepted impact analysis: `sha256:c573337b9ffd11b7b27a757f022242d9e360ce9fa4f6d63b4e3f0eaae78892f1` (revision 1)
- Determinism snapshot: `determinism-snapshot:sha256:2860d1afa4072edc68fda586bc1cf9f017cef756ff8ffb55a78f1e69aeebab11`
- CAST repository pin: `cast:repo:https://github.com/XEBIA-ACE/ACE_RETAIL_BANKING:commit:3feadfcc0d3ed193c698fd4f2e06d49737f2770e`
- Implement only against Accepted impact analysis (revision 1) (fingerprint `sha256:c573337b9ffd11b7b27a757f022242d9e360ce9fa4f6d63b4e3f0eaae78892f1`)

---

## Open Gaps (must be resolved before implementation)

- **Open gap – retention rules**: FR-003 references 'defined retention rules' for notification history persistence. The specific retention duration, maximum record count, and archival policy are not defined in the acceptance criteria or any grounding artifact. This gap must be resolved before implementation of the persistence step (STEP 5 in spec_technical_design).
- **Open gap – NFR thresholds**: FR-004 references NFR-001, NFR-002, and NFR-003 for performance targets. The numeric thresholds for these NFRs are not present in any grounding artifact. The implementation must conform to those NFR definitions as the authoritative source once they are made available.

---

## Implementation-Readiness Items (resolve or explicitly accept before proceeding)

- Candidate verification found the selected focus object implausible for this requirement: The selected object '&&COPYLINK' is a Temporary Dataset (a JCL intermediate file used during job processing), which has no semantic relationship to viewing a smart notification history list for retail banking customers. None of the candidates in the list — including JCL Jobs, JCL Data Sets, JCL Steps, or generic COBOL paragraphs like 0000-MAIN — appear to be a clearly better match for this requirement either, as none relate to smart notification history display logic.
- No CAST object representing a notification history screen, notification list component, or in-app notification delivery service was returned.
- No REST, CICS web service, or mobile API endpoints for notification history retrieval were returned by the API inventory query.
- No UI framework (web or mobile) components are present in the application inventory to support screen-level performance mapping (NFR-001 to NFR-003).
- No database tables storing notification events or retention metadata were found.
- No pre-existing implementation was found for this requirement; scope is derived solely from the requirement text, not from measured code evidence.
- No-match classification: Initial classification (net-new capability) has a noted caveat: The capability described — a notification history list — is a foundational, common feature in retail banking apps, and the search terms, while reasonable, did not try plausible synonyms such as "notificationLog", "alertLog", "messageHistory", or "notificationCenter" that many implementations use. However, there is no concrete evidence this codebase has such a feature under a different name.
- No-match consistency check raised a caveat about this classification: The capability described — a notification history list — is a foundational, common feature in retail banking apps, and the search terms, while reasonable, did not try plausible synonyms such as "notificationLog", "alertLog", "messageHistory", or "notificationCenter" that many implementations use. However, there is no concrete evidence this codebase has such a feature under a different name.
- Requirement detail is incomplete (see evidence_gaps); no conflicting existing implementation was found.
- Searched 5 term(s) derived from this requirement (notificationHistory, smartNotification, alertHistory, notificationList, inAppNotification) against Cast; none resolved to an existing code object.
- The seed resolution mapped 'inAppNotification' to a JCL Temporary Dataset (&&COPYLINK), which is a build artifact — the semantic match to the user story's notification history feature is not supported by the structural facts.
- Which components will need to be built to implement each acceptance criterion, since no existing implementation was found?
- Architecture context is degraded (missing_core: api_contracts); no API contracts are available to ground interface design. No sub-service decomposition or component inventory was returned beyond the repository-level service entry 'https-github-com-xebia-ace-ace-retail-banking'.

---

## Design Steps

**STEP 1 – Define notification history data contract (net-new)**
Design the data schema for a persisted smart notification record, capturing at minimum: customer identifier, notification type (e.g., large-transaction, low-balance, bill-reminder), notification timestamp, delivery status, and a retention expiry marker. This schema is required by FR-001 and FR-003.
- Evidence: `sha256:c573337b9ffd11b7b27a757f022242d9e360ce9fa4f6d63b4e3f0eaae78892f1`; provenance `553b39f8ceb22553`
- NOTE: No existing table or data model was found in the CAST scan; this is a net-new data model.

**STEP 2 – Design notification persistence endpoint / integration point (net-new)**
Specify the internal interface (API endpoint or event-driven integration) by which the Smart Notifications capability writes a completed notification record to the notification history store upon delivery completion (FR-003). The interface must be idempotent and must apply the retention rules resolved from the open gap above.
- Evidence: `sha256:c573337b9ffd11b7b27a757f022242d9e360ce9fa4f6d63b4e3f0eaae78892f1`; provenance `553b39f8ceb22553`

**STEP 3 – Design notification history retrieval API (net-new)**
Specify a customer-scoped API operation that retrieves the authenticated customer's notification history records in reverse-chronological order (FR-001). The operation must enforce authentication, scope results to the requesting customer, and return an empty collection (not an error) when no records exist (FR-002). Response payload must include all fields defined in STEP 1.
- Evidence: `sha256:c573337b9ffd11b7b27a757f022242d9e360ce9fa4f6d63b4e3f0eaae78892f1`; provenance `553b39f8ceb22553`
- NOTE: No existing REST, CICS web service, or mobile API endpoint for notification history was found in the CAST scan.

**STEP 4 – Design notification history screen / UI component (net-new)**
Specify the in-app notification history screen that calls the retrieval API from STEP 3 and renders the result list in reverse-chronological order (FR-001). The screen must render a graceful empty-state message (not an error) when the API returns an empty collection (FR-002). Screen load performance must be validated against NFR-001 to NFR-003 once thresholds are resolved (FR-004).
- Evidence: `sha256:c573337b9ffd11b7b27a757f022242d9e360ce9fa4f6d63b4e3f0eaae78892f1`; provenance `553b39f8ceb22553`
- NOTE: No UI framework components were found in the CAST application inventory.

**STEP 5 – Define retention enforcement mechanism (net-new, blocked on open gap)**
Once the retention rules gap is resolved, design the mechanism (scheduled job, TTL index, or archival process) that enforces the retention policy on the notification history store (FR-003). This step is blocked until the retention rules are defined.
- Evidence: `sha256:c573337b9ffd11b7b27a757f022242d9e360ce9fa4f6d63b4e3f0eaae78892f1`; provenance `553b39f8ceb22553`

**STEP 6 – Define test obligations**
Implement tests covering all four acceptance criteria (US-009-ac-1 through US-009-ac-4): (ac-1) authenticated customer with history sees reverse-chronological list; (ac-2) authenticated customer with no history sees empty-state, no error; (ac-3) notification written to history upon delivery completion and respects retention rules; (ac-4) list loads within NFR-001 to NFR-003 performance targets under normal load.
- Evidence: `sha256:c573337b9ffd11b7b27a757f022242d9e360ce9fa4f6d63b4e3f0eaae78892f1`; provenance `553b39f8ceb22553`

---

## Contract Changes

No material API, event, or data contract change is evidenced. `contract_change_required: false`.

---

## Security and Quality

- Preserve the security controls evidenced by `553b39f8ceb22553`
- Verify the changed user flow against the stated accessibility criteria

---

## API / Readiness Check Summary

| Check                    | Status  | Note |
|--------------------------|---------|------|
| accepted_impact          | blocked | accepted_impact_hash is set and matches built_from_accepted_impact_hash; however, the impact analysis next_action is 'accept_with_gaps' with all requirement_traceability entries unmapped (no_match_verdict=net_new_capability), no affected_files, no affected_symbols, and no affected_services. The capability is fully net-new with no grounded provider evidence, and blocking_reasons are present in the impact analysis. |
| grounded_components      | warning | No affected_files, affected_symbols, or affected_services were identified in the impact analysis. The CAST scan (snapshot 553b39f8ceb22553, commit 3feadfcc0d3ed193c698fd4f2e06d49737f2770e) returned zero objects matching any of the 5 search terms. All design steps in spec_technical_design are net-new with no traceable provider component. Architecture context is degraded (missing_core: api_contracts). |
| verification_obligations | passed  | All four acceptance criteria (US-009-ac-1 through US-009-ac-4) have corresponding test obligations defined. |

---

## Verification Plan

- **US-009-ac-1**: Given an authenticated retail banking customer with at least one enabled smart-notification type and past notification events exist
When the customer opens the notification history screen
Then a list of relevant past smart notifications (e.g., large transaction, low balance, bill reminders) generated for that customer is displayed in reverse-chronological order
  - Obligation ID: `8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-1`
  - Type: characterization | Environment: ci | Required before: release

- **US-009-ac-2**: Given an authenticated retail banking customer with no past smart notifications
When the customer opens the notification history screen
Then the screen indicates that no notification history is available without showing error messages
  - Obligation ID: `8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-2`
  - Type: characterization | Environment: ci | Required before: release

- **US-009-ac-3**: Given a smart notification is generated by the Smart Notifications capability for a customer
When delivery processing for that notification completes
Then the notification appears in the customer's in-app notification history in line with the defined retention rules
  - Obligation ID: `8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-3`
  - Type: characterization | Environment: ci | Required before: release

- **US-009-ac-4**: Given notification history is requested by a customer
When the notification history screen is opened under normal supported load
Then the list content loads within the app-wide performance targets defined for standard screens (per NFR-001 to NFR-003)
  - Obligation ID: `8b9cb496-7190-514c-af95-dac72a1437f9:test:characterization:US-009-ac-4`
  - Type: characterization | Environment: ci | Required before: release

- **IMPACT**: Verify acceptance criterion: Given an authenticated retail banking customer with at least one enabled smart-notification type and past notification events exist
When the customer opens the notification history screen
Then a list of relevant past smart notifications (e.g., large transaction, low balance, bill reminders) generated for that customer is displayed in reverse-chronological order

- **IMPACT**: Verify acceptance criterion: Given an authenticated retail banking customer with no past smart notifications
When the customer opens the notification history screen
Then the screen indicates that no notification history is available without showing error messages

- **IMPACT**: Verify acceptance criterion: Given a smart notification is generated by the Smart Notifications capability for a customer
When delivery processing for that notification completes
Then the notification appears in the customer's in-app notification history in line with the defined retention rules

- **IMPACT**: Verify acceptance criterion: Given notification history is requested by a customer
When the notification history screen is opened under normal supported load
Then the list content loads within the app-wide performance targets defined for standard screens (per NFR-001 to NFR-003)