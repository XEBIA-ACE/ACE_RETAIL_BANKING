# Implementation Plan: US-024

## Technical Context

### Grounding References
- Codebase grounding snapshot: `553b39f8ceb22553`
- Accepted impact analysis: `sha256:5f66a3a4d04b9bede18c9a6e252030a884c97f9203f88925dbba8348b9aba443`

### Implementation Mode
This is a **net-new capability**. Impact analysis confirms no pre-existing implementation was found for this requirement. Scope is derived solely from the requirement text, not from measured code evidence.

### Implementation-Readiness Items to Resolve
1. No pre-existing implementation was found for this requirement; scope is derived solely from the requirement text, not from measured code evidence.
2. No-match classification: The requirement asks to "configure notification preferences and consent by alert type and channel," which is a specific new feature for managing customer notification settings. Comprehensive searches for NotificationPreference, consent, AlertChannel, NotificationSettings, subscription, Notification, Preferences, Configure, Consent, and Channel all returned nothing. This strongly suggests the notification preference and consent management capability does not yet exist in the codebase, which is expected for a backlog item adding new functionality rather than a search miss.
3. Searched 10 term(s) derived from this requirement (NotificationPreference, consent, AlertChannel, NotificationSettings, subscription, Notification, Preferences, Configure, Consent, Channel) against Cast; none resolved to an existing code object.
4. Which components will need to be built to implement each acceptance criterion, since no existing implementation was found?

## Design Steps

- **DS-001:** Introduce a new NotificationPreferences module to manage customer notification settings. This is a net-new capability as confirmed by impact analysis (no existing implementation found). Evidence: impact_analysis.no_match_verdict=net_new_capability, determinism-snapshot:553b39f8ceb22553.

- **DS-002:** Create NotificationPreference data entity with fields: customer_id, notification_type (enum: LARGE_TRANSACTION_ALERT, LOW_BALANCE_ALERT, BILL_DUE_REMINDER), channel_preferences (push: boolean, sms: boolean, email: boolean), consent_status (boolean), consent_timestamp (datetime), last_modified_by (user_id). Evidence: FR-001, FR-002, FR-003 from spec_functional.

- **DS-003:** Create ConsentAuditLog data entity with fields: audit_id, customer_id, notification_type, action (GRANT/WITHDRAW), channels_affected, timestamp, user_identifier. This supports auditable consent recording per FR-002 and FR-003. Evidence: US-024-ac-2, US-024-ac-3.

- **DS-004:** Implement GET /notification-preferences/{customerId} endpoint to retrieve current notification preferences and channel states for all notification types. Returns localized labels based on Accept-Language header. Evidence: FR-001, FR-007, US-024-ac-1, US-024-ac-7.

- **DS-005:** Implement PUT /notification-preferences/{customerId} endpoint to save updated preferences. On save: validate consent changes, persist to NotificationPreference store, write ConsentAuditLog entry with timestamp and user_id, return localized confirmation message. Evidence: FR-002, FR-003, FR-005, US-024-ac-2, US-024-ac-3, US-024-ac-5.

- **DS-006:** Implement notification delivery filter logic that evaluates customer channel preferences before dispatching. Only enabled channels are considered; disabled channels are excluded. Evidence: FR-004, US-024-ac-4.

- **DS-007:** Implement localization resource bundles for German (de), French (fr), Italian (it), and English (en) covering all UI labels, helper text, error messages, and confirmation messages. Include locale-appropriate date/time formatting. Evidence: FR-007, US-024-ac-7.

- **DS-008:** Ensure all UI components for the notification preferences screen meet WCAG 2.1 AA accessibility requirements: keyboard-navigable toggles and buttons, ARIA labels for notification type and channel controls, focus management, and screen reader compatibility. Evidence: FR-006, US-024-ac-6.

## Contract Changes

No material API, event, or data contract change is evidenced against existing systems. This is a net-new capability.

## Security and Quality Obligations

### Security
- Validate credential and personal-data handling against the Jira requirement
- Validate credential and personal-data handling against the story's acceptance criteria

### Accessibility
- Verify the changed user flow against the stated accessibility criteria (WCAG 2.1 AA)

## Verification Plan

| Criterion ID | Verification Obligation |
|--------------|-------------------------|
| US-024-ac-1 | Given a signed-in retail banking customer with access to notification settings, when the customer opens the notification preferences screen, then the screen lists large transaction alerts, low balance alerts, and bill due-date reminders as distinct types, and for each type the available channels (push, SMS, email) are visible with their current on/off state |
| US-024-ac-2 | Given a customer has not yet given consent for a specific notification type, when the customer enables that type and at least one channel and confirms, then the system records explicit consent for that notification type and selected channels in an auditable store, and subsequent notifications of that type are eligible to be sent only via the enabled channels |
| US-024-ac-3 | Given a customer previously consented to a notification type, when the customer disables that type entirely and saves changes, then the system records the withdrawal of consent for that type with a timestamp and user identifier, and no subsequent notifications of that type are sent on any channel until it is re-enabled |
| US-024-ac-4 | Given a customer has some channels enabled and others disabled for a notification type, when an event for that type is later processed, then only the enabled channels are considered for delivery, and any disabled channels are not used |
| US-024-ac-5 | Given the customer changes notification preferences or consent, when the customer saves the changes, then a confirmation is displayed in the selected language, and the updated preferences take effect for all new notifications triggered after the save time |
| US-024-ac-6 | Given accessibility support is required, when the preferences screen is used with a screen reader or keyboard-only navigation, then all interactive elements for notification types and channels are reachable and labelled according to WCAG 2.1 AA guidelines |
| US-024-ac-7 | Given the customer's preferred language is German, French, Italian, or English, when the notification preferences screen is opened, then all labels, helper text, and messages are displayed in that language, and date/time and timezone information on the screen (if any) is localized appropriately |

## Evidence References

- Deterministic grounding snapshot: `553b39f8ceb22553`
- Accepted impact analysis: `sha256:5f66a3a4d04b9bede18c9a6e252030a884c97f9203f88925dbba8348b9aba443`