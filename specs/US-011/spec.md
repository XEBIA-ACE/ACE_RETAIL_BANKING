# Feature Specification: US-011
## View Notification Details with Delivery Status

> **Provenance**
> - **Policy version**: `ace-spec-format/v1`
> - **Accepted impact analysis revision**: `1`
> - **Accepted impact analysis**: `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`
> - **Repository baseline commit**: `3feadfcc0d3ed193c698fd4f2e06d49737f2770e`
> - **Deterministic grounding reference**: `553b39f8ceb22553`
> - **Run ID**: `33fadfa7-7042-522f-b6f9-f245c122a451`
> - **Story ID**: `US-011`

---

## Story

**As a** retail banking customer, **I want to** view detailed information and delivery status for an individual notification, **so that** I can understand how and whether it was delivered.

When I select a notification from my notification history list, a detail view opens showing the delivery status (delivered or failed), the event timestamp, the channels used or attempted, and the core message content — with sensitive data masked per defined privacy rules and all controls accessible in line with WCAG 2.1 AA requirements.

> **Net-new capability notice**: No existing implementation was found in the measured CardDemo codebase (CAST grounding snapshot `553b39f8ceb22553`); all components must be built from scratch.

---

## User Scenarios & Acceptance Criteria

- **US-011-ac-1**: Given a customer is viewing their notification history list and at least one notification is present
  When the customer selects a specific notification entry
  Then a notification detail view opens for that notification

- **US-011-ac-2**: Given a notification has completed delivery processing and a delivery status is recorded as delivered
  When the customer opens the notification detail view for that notification
  Then the delivery status is shown as delivered and the timestamp of the notification event is displayed

- **US-011-ac-3**: Given a notification has completed delivery processing and a delivery status is recorded as failed
  When the customer opens the notification detail view for that notification
  Then the delivery status is shown as failed without exposing internal error details not intended for customers

- **US-011-ac-4**: Given a notification was configured for multiple channels (for example, push and email) and the logging reflects the channels used
  When the customer opens the notification detail view
  Then the channels that were attempted or used (push, SMS, email) are displayed to the customer

- **US-011-ac-5**: Given privacy and masking rules have been defined for notification history content
  When the customer views the notification detail screen
  Then only the approved core message content is displayed and any sensitive data is masked according to those rules

- **US-011-ac-6**: Given an authenticated customer uses supported assistive technology (such as a screen reader or high-contrast mode)
  When the customer navigates to and uses the notification detail view
  Then all content and controls on the detail view are usable in line with WCAG 2.1 AA-aligned accessibility requirements

---

## Functional Requirements

- **FR-001** [US-011-ac-1]: The system shall provide a notification detail view that is navigable from the notification history list. When the customer selects any notification entry from a non-empty history list, the system shall open the detail view scoped to that specific notification. The mechanism for opening the detail view (e.g., modal, new screen, drawer) is not specified by the acceptance criterion and must be resolved during design; this ambiguity is preserved here rather than silently resolved. NOTE: No existing notification detail view was found in the measured CardDemo codebase; this view must be built as a net-new component.

- **FR-002** [US-011-ac-2]: When the notification detail view is opened for a notification whose delivery status is recorded as 'delivered', the system shall display (a) the delivery status labelled or indicated as 'delivered', and (b) the timestamp of the notification event. The exact format and label for the timestamp are not specified by the acceptance criterion and must be defined during design.

- **FR-003** [US-011-ac-3]: When the notification detail view is opened for a notification whose delivery status is recorded as 'failed', the system shall display the delivery status as 'failed'. The system shall not expose internal error codes, stack traces, system messages, or any other technical failure details that are not intended for customer consumption. The boundary between customer-safe failure messaging and internal error detail must be defined by the implementation team, as the acceptance criterion does not enumerate specific suppressed fields.

- **FR-004** [US-011-ac-4]: When a notification was configured for multiple delivery channels and the channel-usage log reflects the channels used or attempted, the notification detail view shall display each channel (e.g., push, SMS, email) that was attempted or used for that notification. The acceptance criterion does not specify the visual treatment or ordering of channels; these must be resolved during design. NOTE: No channel-logging construct was found in the measured codebase; channel logging infrastructure must be built as a net-new capability.

- **FR-005** [US-011-ac-5]: The notification detail view shall enforce defined privacy and masking rules when rendering notification history content. Only the approved core message content shall be displayed to the customer; any sensitive data fields subject to masking rules shall be masked or suppressed in accordance with those rules. The specific fields subject to masking and the masking rules themselves are not defined in the acceptance criterion and must be specified separately before implementation. NOTE: No data-masking or privacy-filter logic was found in the measured codebase; this must be built as a net-new capability.

- **FR-006** [US-011-ac-6]: All content and interactive controls on the notification detail view shall conform to WCAG 2.1 Level AA accessibility requirements. This includes, but is not limited to, compatibility with screen readers and high-contrast display modes for authenticated customers using supported assistive technology. The specific assistive technologies to be supported and the verification method are not enumerated in the acceptance criterion; these must be defined during design and test planning. The impact analysis identifies this as an accessibility obligation requiring explicit verification.

---

## Clause Traceability

| Clause ID | Criterion ID | Tier | Trust Status | Load Bearing |
|---|---|---|---|---|
| US-011-ac-1 | US-011-ac-1 | B | OBSERVED_UNVERIFIED | Yes |
| US-011-ac-2 | US-011-ac-2 | B | OBSERVED_UNVERIFIED | Yes |
| US-011-ac-3 | US-011-ac-3 | C | REJECTED | No |
| US-011-ac-4 | US-011-ac-4 | C | REJECTED | No |
| US-011-ac-5 | US-011-ac-5 | C | REJECTED | No |
| US-011-ac-6 | US-011-ac-6 | C | REJECTED | No |

---

## Security Obligations

- Validate credential and personal-data handling against the Jira requirement
- Validate credential and personal-data handling against the story's acceptance criteria

## Quality / Accessibility Obligations

- Verify the changed user flow against the stated accessibility criteria

---

## Evidence Gaps & Grounding Disclosures

> The following gaps were identified by the impact analysis and are carried forward as-is per pipeline rules. They must be resolved or explicitly accepted before implementation.

- No notification detail view object exists in the CAST-measured CardDemo object graph.
- No delivery status field, record, or paragraph is present in the measured codebase.
- No channel-logging (push, SMS, email) construct is measured in any COBOL program or CICS transaction.
- No data-masking or privacy-filter logic is present in the measured structural facts.
- No accessibility or UI framework metadata is present in the CAST inventory.
- Database object query returned empty for the focus object; no notification data store is evidenced.
- API inventory query returned no endpoints, confirming no REST/HTTP surface for a notification detail API.
- Requirement evidence was gathered from 3 unrelated services/packages ('notificationDetail' (cbl/CBTRN03C.cbl), 'deliveryStatus' (cbl/COCRDUPC.cbl), 'messageDelivery' (cbl/COCRDLIC.cbl)) — the measured blast radius may mix facts that do not all belong to the single focus object named above. Review each named symbol's own source before accepting.
- Candidate verification found the selected focus object implausible for this requirement: The requirement is about viewing notification details and delivery status for individual notifications in a retail banking context. The selected object '1240-EDIT-CARDSTATUS' is a COBOL paragraph related to editing card status, which has no semantic relationship to notification detail viewing or delivery status tracking. None of the candidates are a strong match, but '1120-WRITE-DETAIL' is the closest alternative as it involves writing/displaying detail information, which is more aligned with rendering a notification detail view than card status editing.
- Candidate verification flagged a possibly better-matching alternative that was not selected: 1120-WRITE-DETAIL (13974) — review before accepting.
- Searched 5 term(s) derived from this requirement (notificationDetail, deliveryStatus, notificationHistory, NotificationChannel, messageDelivery) against Cast; none resolved to an existing code object.
- No-match classification: Initial classification (net-new capability) has a noted caveat: The search terms were reasonable but skipped some plausible synonyms — for example, "notificationStatus", "alertDetail", "messageStatus", or "pushNotification" — that a banking app might use for related notification infrastructure. However, there is no concrete evidence this codebase already has a detail/delivery-status view; the doubt is non-specific and based on the generic nature of notification tracking in retail banking apps.
- No pre-existing implementation was found for this requirement; scope is derived solely from the requirement text, not from measured code evidence.

---

## Requirement Traceability

| Requirement ID | Status | No-Match Verdict | Gap |
|---|---|---|---|
| US-011-ac-1 | unmapped | net_new_capability | No requirement-to-provider evidence mapping was produced |
| US-011-ac-2 | unmapped | net_new_capability | No requirement-to-provider evidence mapping was produced |
| US-011-ac-3 | unmapped | net_new_capability | No requirement-to-provider evidence mapping was produced |
| US-011-ac-4 | unmapped | net_new_capability | No requirement-to-provider evidence mapping was produced |
| US-011-ac-5 | unmapped | net_new_capability | No requirement-to-provider evidence mapping was produced |
| US-011-ac-6 | unmapped | net_new_capability | No requirement-to-provider evidence mapping was produced |
