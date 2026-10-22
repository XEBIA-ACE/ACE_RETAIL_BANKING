# Implementation Plan: US-011
## View Notification Details with Delivery Status

> **Provenance**
> - **Policy version**: `ace-spec-format/v1`
> - **Accepted impact analysis revision**: `1`
> - **Accepted impact analysis**: `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`
> - **Repository baseline commit**: `3feadfcc0d3ed193c698fd4f2e06d49737f2770e`
> - **Deterministic grounding reference**: `553b39f8ceb22553`
> - **Run ID**: `33fadfa7-7042-522f-b6f9-f245c122a451`

---

## Technical Context

- Codebase grounding snapshot: `553b39f8ceb22553`
- Accepted impact analysis: `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`
- Unresolved reference: `determinism-snapshot:sha256:911e781e30b6e26379dcd45c9e881aaeb0d43cb34475c570675e78b4bc1a337a`
- Unresolved reference: `requirements_agent:7d4b3e20-2fb9-4cf3-aee0-bb[REDACTED-PHONE]d1:US-011`
- Unresolved reference: `cast:repo:https://github.com/XEBIA-ACE/ACE_RETAIL_BANKING:commit:3feadfcc0d3ed193c698fd4f2e06d49737f2770e`
- Implement only against Accepted impact analysis (revision 1) (fingerprint `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`)

> **Degraded-mode disclosure**: The architecture context is marked `degraded=true` (missing_core: api_contracts). No grounded service, symbol, or file was identified by the impact analysis. All design steps below describe work that must be scoped and grounded by the implementation team before execution; they are derived solely from the functional specification and the accepted impact analysis, not from measured code evidence.

---

## Implementation-Readiness Items

The following items must be resolved or explicitly accepted before implementation proceeds:

- API inventory query returned no endpoints, confirming no REST/HTTP surface for a notification detail API.
- Candidate verification flagged a possibly better-matching alternative that was not selected: 1120-WRITE-DETAIL (13974) — review before accepting.
- Candidate verification found the selected focus object implausible for this requirement: The requirement is about viewing notification details and delivery status for individual notifications in a retail banking context. The selected object '1240-EDIT-CARDSTATUS' is a COBOL paragraph related to editing card status, which has no semantic relationship to notification detail viewing or delivery status tracking. None of the candidates are a strong match, but '1120-WRITE-DETAIL' is the closest alternative as it involves writing/displaying detail information, which is more aligned with rendering a notification detail view than card status editing.
- Database object query returned empty for the focus object; no notification data store is evidenced.
- No accessibility or UI framework metadata is present in the CAST inventory.
- No channel-logging (push, SMS, email) construct is measured in any COBOL program or CICS transaction.
- No data-masking or privacy-filter logic is present in the measured structural facts.
- No delivery status field, record, or paragraph is present in the measured codebase.
- No notification detail view object exists in the CAST-measured CardDemo object graph.
- No pre-existing implementation was found for this requirement; scope is derived solely from the requirement text, not from measured code evidence.
- No-match classification: Initial classification (net-new capability) has a noted caveat: The search terms were reasonable but skipped some plausible synonyms — for example, "notificationStatus", "alertDetail", "messageStatus", or "pushNotification" — that a banking app might use for related notification infrastructure. However, there is no concrete evidence this codebase already has a detail/delivery-status view; the doubt is non-specific and based on the generic nature of notification tracking in retail banking apps.
- No-match consistency check raised a caveat about this classification: The search terms were reasonable but skipped some plausible synonyms — for example, "notificationStatus", "alertDetail", "messageStatus", or "pushNotification" — that a banking app might use for related notification infrastructure. However, there is no concrete evidence this codebase already has a detail/delivery-status view; the doubt is non-specific and based on the generic nature of notification tracking in retail banking apps.
- Requirement detail is incomplete (see evidence_gaps); no conflicting existing implementation was found.
- Requirement evidence was gathered from 3 unrelated services/packages ('notificationDetail' (cbl/CBTRN03C.cbl), 'deliveryStatus' (cbl/COCRDUPC.cbl), 'messageDelivery' (cbl/COCRDLIC.cbl)) — the measured blast radius below may mix facts that do not all belong to the single focus object named above. Review each named symbol's own source before accepting.
- Searched 5 term(s) derived from this requirement (notificationDetail, deliveryStatus, notificationHistory, NotificationChannel, messageDelivery) against Cast; none resolved to an existing code object.
- Which components will need to be built to implement each acceptance criterion, since no existing implementation was found?
- Architecture context is degraded (missing_core: api_contracts); no grounded API contracts are available to assess contract change impact. Contract change assessment is therefore based solely on the absence of any measured REST/HTTP surface in the codebase.
- The specific fields subject to masking and the masking rules themselves must be formally specified before the privacy/masking layer (STEP 6 of technical design) can be implemented (FR-005 open ambiguity).
- The mechanism for opening the notification detail view (modal, new screen, drawer) is an open design decision that must be resolved before implementation (FR-001 ambiguity).
- The specific assistive technologies to be supported and the WCAG 2.1 AA verification method must be defined during test planning (FR-006 open ambiguity).
- The exact timestamp format and channel ordering for the NotificationDetail schema must be resolved during design review (STEP 3 of technical design).

---

## Design Steps

**STEP 1 — Net-new capability declaration** [US-011, FR-001]

The impact analysis (evidence: `553b39f8ceb22553`, `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`) confirms zero affected_services, affected_symbols, and affected_files in the CardDemo CAST-measured codebase. All six acceptance criteria (US-011-ac-1 through US-011-ac-6) are classified as net_new_capability with no existing implementation found. This design is therefore entirely additive; no existing CardDemo component is modified.

**STEP 2 — Degraded-mode disclosure**

The architecture context is marked degraded (missing_core: api_contracts) and no grounded service, symbol, or file was identified by the impact analysis. Per pipeline rules, `degraded=true` is mandatory. All design steps below describe work that must be scoped and grounded by the implementation team before execution; they are derived solely from the functional specification (spec_functional) and the accepted impact analysis, not from measured code evidence.

**STEP 3 — Define NotificationDetail data contract** [US-011-ac-2, US-011-ac-3, US-011-ac-4, FR-002, FR-003, FR-004]

Design a new NotificationDetail record/schema containing: (a) notificationId, (b) deliveryStatus (enum: delivered | failed), (c) eventTimestamp, (d) channelsAttempted (list: push | SMS | email), (e) coreMessageContent. The schema must explicitly exclude internal error codes, stack traces, and system messages from the customer-facing payload (FR-003). Field-level masking annotations must be added for any sensitive data fields once the privacy and masking rules are formally specified (FR-005). The exact timestamp format and channel ordering must be resolved during design review. Evidence: `553b39f8ceb22553`.

**STEP 4 — Build NotificationDetail API endpoint (net-new)** [US-011-ac-1, US-011-ac-2, US-011-ac-3, US-011-ac-4, FR-001, FR-002, FR-003, FR-004]

Implement a new read-only API endpoint (e.g., GET /notifications/{notificationId}) that retrieves the NotificationDetail record for a given notification identifier. The endpoint must: return only customer-safe delivery status and message content; suppress internal error detail on failed status; include channelsAttempted from the channel-logging infrastructure (to be built in STEP 5); enforce authentication before returning any data. NOTE: No REST/HTTP surface exists in the measured codebase (API inventory returned empty); this endpoint is entirely net-new. Evidence: `553b39f8ceb22553`, `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`.

**STEP 5 — Build channel-logging infrastructure (net-new)** [US-011-ac-4, FR-004]

Implement net-new channel-logging capability to record which delivery channels (push, SMS, email) were attempted or used for each notification event. This infrastructure must persist channel-attempt records linkable to a notificationId so that the NotificationDetail API (STEP 4) can retrieve them. NOTE: No channel-logging construct was found in the measured CardDemo codebase; this is a fully net-new capability. The visual treatment and ordering of channels in the UI must be resolved during design. Evidence: `553b39f8ceb22553`.

**STEP 6 — Implement privacy and data-masking layer (net-new)** [US-011-ac-5, FR-005]

Implement a net-new privacy/masking filter that is applied before notification content is returned to the customer. The filter must: apply defined masking rules to sensitive data fields in the NotificationDetail payload; pass only approved core message content to the presentation layer. PREREQUISITE: The specific fields subject to masking and the masking rules themselves must be formally specified before this step can be implemented (FR-005 open ambiguity). NOTE: No data-masking or privacy-filter logic exists in the measured codebase. Evidence: `553b39f8ceb22553`.

**STEP 7 — Build NotificationDetail UI view (net-new)** [US-011-ac-1, US-011-ac-2, US-011-ac-3, US-011-ac-4, US-011-ac-5, US-011-ac-6, FR-001, FR-006]

Implement a net-new notification detail view navigable from the notification history list. The view must: open when the customer selects a notification entry from a non-empty history list; display deliveryStatus (delivered or failed), eventTimestamp, and channelsAttempted; render only masked/approved coreMessageContent; support screen readers and high-contrast mode in conformance with WCAG 2.1 Level AA (FR-006). The mechanism for opening the detail view (modal, new screen, drawer) is an open design decision that must be resolved before implementation (FR-001 ambiguity). NOTE: No notification detail view or UI framework metadata exists in the measured codebase; this is entirely net-new. Evidence: `553b39f8ceb22553`, `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`.

**STEP 8 — Accessibility verification** [US-011-ac-6, FR-006]

After the NotificationDetail UI view is built (STEP 7), conduct explicit WCAG 2.1 AA verification covering: screen reader compatibility, high-contrast mode rendering, keyboard navigability, and focus management for all controls on the detail view. The specific assistive technologies to be supported and the verification method must be defined during test planning (FR-006 open ambiguity). This step fulfils the accessibility obligation identified in the impact analysis. Evidence: `553b39f8ceb22553`.

**STEP 9 — Security review** [US-011-ac-2, US-011-ac-3, US-011-ac-5]

Validate that the NotificationDetail API endpoint (STEP 4) and the privacy/masking layer (STEP 6) correctly enforce: (a) authentication before any notification data is returned; (b) suppression of internal error details on failed delivery status; (c) masking of sensitive personal data per defined rules. This step fulfils the security obligation identified in the impact analysis ('Validate credential and personal-data handling against the Jira requirement'). Evidence: `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`.

**STEP 10 — Test coverage for all acceptance criteria** [US-011-ac-1 through US-011-ac-6]

Implement test cases covering each acceptance criterion as enumerated in the impact analysis test_obligations: (ac-1) navigation from history list opens detail view; (ac-2) delivered status and timestamp displayed; (ac-3) failed status shown without internal error detail; (ac-4) channels attempted/used displayed; (ac-5) sensitive data masked per rules; (ac-6) WCAG 2.1 AA accessibility verified. Evidence: `553b39f8ceb22553`, `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`.

---

## Contract Changes

- No material API, event, or data contract change is evidenced.
- `contract_change_required: false` (spec_api_impact gate 5)
- API impact status: **blocked** — accepted_impact_hash is set but the impact analysis is classified as blocked due to incomplete requirement detail and unresolved evidence gaps (next_action: accept_with_gaps).

---

## Security and Quality Obligations

- Validate credential and personal-data handling against the Jira requirement
- Validate credential and personal-data handling against the story's acceptance criteria
- Verify the changed user flow against the stated accessibility criteria

---

## Verification Plan

- **US-011-ac-1** (characterization, required before release, env: ci):
  Given a customer is viewing their notification history list and at least one notification is present
  When the customer selects a specific notification entry
  Then a notification detail view opens for that notification

- **US-011-ac-2** (characterization, required before release, env: ci):
  Given a notification has completed delivery processing and a delivery status is recorded as delivered
  When the customer opens the notification detail view for that notification
  Then the delivery status is shown as delivered and the timestamp of the notification event is displayed

- **US-011-ac-3** (characterization, required before release, env: ci):
  Given a notification has completed delivery processing and a delivery status is recorded as failed
  When the customer opens the notification detail view for that notification
  Then the delivery status is shown as failed without exposing internal error details not intended for customers

- **US-011-ac-4** (characterization, required before release, env: ci):
  Given a notification was configured for multiple channels (for example, push and email) and the logging reflects the channels used
  When the customer opens the notification detail view
  Then the channels that were attempted or used (push, SMS, email) are displayed to the customer

- **US-011-ac-5** (characterization, required before release, env: ci):
  Given privacy and masking rules have been defined for notification history content
  When the customer views the notification detail screen
  Then only the approved core message content is displayed and any sensitive data is masked according to those rules

- **US-011-ac-6** (characterization, required before release, env: ci):
  Given an authenticated customer uses supported assistive technology (such as a screen reader or high-contrast mode)
  When the customer navigates to and uses the notification detail view
  Then all content and controls on the detail view are usable in line with WCAG 2.1 AA-aligned accessibility requirements

- **IMPACT** (from impact analysis test_obligations):
  - Verify acceptance criterion: Given a customer is viewing their notification history list and at least one notification is present / When the customer selects a specific notification entry / Then a notification detail view opens for that notification
  - Verify acceptance criterion: Given a notification has completed delivery processing and a delivery status is recorded as delivered / When the customer opens the notification detail view for that notification / Then the delivery status is shown as delivered and the timestamp of the notification event is displayed
  - Verify acceptance criterion: Given a notification has completed delivery processing and a delivery status is recorded as failed / When the customer opens the notification detail view for that notification / Then the delivery status is shown as failed without exposing internal error details not intended for customers
  - Verify acceptance criterion: Given a notification was configured for multiple channels (for example, push and email) and the logging reflects the channels used / When the customer opens the notification detail view / Then the channels that were attempted or used (push, SMS, email) are displayed to the customer
  - Verify acceptance criterion: Given privacy and masking rules have been defined for notification history content / When the customer views the notification detail screen / Then only the approved core message content is displayed and any sensitive data is masked according to those rules
  - Verify acceptance criterion: Given an authenticated customer uses supported assistive technology (such as a screen reader or high-contrast mode) / When the customer navigates to and uses the notification detail view / Then all content and controls on the detail view are usable in line with WCAG 2.1 AA-aligned accessibility requirements
