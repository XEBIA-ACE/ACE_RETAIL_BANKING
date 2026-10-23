# Implementation Plan: US-001

## Technical Context

### Grounding References
- Codebase grounding snapshot: `553b39f8ceb22553`
- Accepted impact analysis: `sha256:1ba923446628e45036f6bab5b2f7d9e5796f2f515d99dfcd6032518685356da9`

### Unresolved References
- `determinism-snapshot`
- `impact_analysis:no_match_verdict:net_new_capability`
- `requirement_traceability:US-001-ac-1:unmapped`
- `requirement_traceability:US-001-ac-2:unmapped`
- `requirement_traceability:US-001-ac-3:unmapped`
- `requirement_traceability:US-001-ac-4:unmapped`
- `requirement_traceability:US-001-ac-5:unmapped`
- `requirement_traceability:US-001-ac-6:unmapped`
- `requirement_traceability:US-001-ac-7:unmapped`

### Implementation Constraints
- Implement only against Accepted impact analysis (revision 1) (fingerprint sha256:1ba923446628e45036f6bab5b2f7d9e5796f2f515d99dfcd6032518685356da9)
- Resolve or explicitly accept the following implementation-readiness items:
  - No pre-existing implementation was found for this requirement; scope is derived solely from the requirement text, not from measured code evidence.
  - No-match classification: The requirement describes a new feature for customers to configure notification preferences and consent by type and channel. The search terms tried (NotificationPreferences, consent, notification_channel, alert_settings, subscription, Notification, Preferences, Configure, Consent, Channel) returned nothing. This is a specific enhancement story asking for a new notification preferences management capability with consent tracking, multi-channel support, and regulatory compliance features. Such a feature would be new development rather than something that must already exist in a typical codebase, making the zero results expected for a net-new capability.
  - Searched 10 term(s) derived from this requirement (NotificationPreferences, consent, notification_channel, alert_settings, subscription, Notification, Preferences, Configure, Consent, Channel) against Cast; none resolved to an existing code object.
  - Which components will need to be built to implement each acceptance criterion, since no existing implementation was found?

## Design Steps

1. **Step 1:** Create new NotificationPreferences domain entity to store customer notification type preferences (enabled/disabled state) and channel preferences (SMS, email) per notification type. This is net-new capability with no existing affected_services or affected_symbols identified. Evidence: impact_analysis shows no_match_verdict=net_new_capability for all acceptance criteria.

2. **Step 2:** Create new ConsentRecord domain entity to capture regulatory consent decisions with approved language text, timestamp, and consent status per notification type and channel. Evidence: requirement_traceability US-001-ac-3 unmapped as net_new_capability.

3. **Step 3:** Implement NotificationPreferencesService with methods: getPreferences(customerId), updateChannelPreference(customerId, notificationType, channel, enabled), recordConsent(customerId, notificationType, channel, consentDecision). Evidence: FR-001, FR-002, FR-003 from spec_functional.

4. **Step 4:** Implement NotificationPreferencesController exposing REST endpoints: GET /api/v1/customers/{customerId}/notification-preferences, PUT /api/v1/customers/{customerId}/notification-preferences/{type}/channels/{channel}, POST /api/v1/customers/{customerId}/notification-preferences/{type}/consent. Evidence: acceptance criteria US-001-ac-1, US-001-ac-2, US-001-ac-3.

5. **Step 5:** Implement multi-language confirmation and error message support (de/fr/it/en) using i18n resource bundles for notification preference operations. Evidence: FR-004, FR-005 from spec_functional, US-001-ac-4, US-001-ac-5.

6. **Step 6:** Implement NotificationSuppressionFilter in notification dispatch flow to check customer preferences before sending any notification, suppressing notifications for disabled types. Evidence: FR-006, US-001-ac-6.

7. **Step 7:** Implement WCAG 2.1 AA compliant UI components for notification preference management including high-contrast mode support, larger text scaling, and screen reader compatibility with ARIA labels. Evidence: FR-007, US-001-ac-7.

## Contract Changes

No material API, event, or data contract change is evidenced.

## Security and Quality Obligations

### Security
- Validate credential and personal-data handling against the Jira requirement
- Validate credential and personal-data handling against the story's acceptance criteria

### Accessibility
- Verify the changed user flow against the stated accessibility criteria

## Verification Plan

| Criterion ID | Verification Obligation |
|--------------|------------------------|
| US-001-ac-1 | Given an authenticated customer, When they open notification settings, Then the available notification types (including large transaction, low balance, and bill due reminders) and their current enable/disable state are displayed. |
| US-001-ac-2 | Given a notification type that supports multiple channels, When the customer updates their channel preferences for that type (e.g., enables SMS and disables email), Then the preferences are persisted and reflected the next time settings are opened. |
| US-001-ac-3 | Given a notification type that requires explicit consent under applicable regulations, When the customer first enables that type or a regulated channel for it, Then the application requests consent using approved language and records the consent decision. |
| US-001-ac-4 | Given the customer successfully updates any notification preference or consent, When the update is saved, Then a clear confirmation message is displayed to the customer in their selected language (de/fr/it/en). |
| US-001-ac-5 | Given the customer attempts to update notification preferences and an application-level validation error occurs (for example, missing required consent acknowledgement), When the update fails, Then an understandable, non-technical error message explains what is wrong and how to fix it. |
| US-001-ac-6 | Given a customer disables a specific notification type, When its trigger condition later occurs, Then that type of notification is not sent on any channel configured for that customer. |
| US-001-ac-7 | Given the application is used with high-contrast mode or larger text enabled, When the customer manages notification preferences, Then all controls and confirmation/error messages remain readable and operable with screen readers as per WCAG 2.1 AA. |

## Readiness Status

**Status:** ready_with_gaps

### Unresolved Items
- No pre-existing implementation was found for this requirement; scope is derived solely from the requirement text, not from measured code evidence.
- No-match classification: The requirement describes a new feature for customers to configure notification preferences and consent by type and channel. The search terms tried (NotificationPreferences, consent, notification_channel, alert_settings, subscription, Notification, Preferences, Configure, Consent, Channel) returned nothing. This is a specific enhancement story asking for a new notification preferences management capability with consent tracking, multi-channel support, and regulatory compliance features. Such a feature would be new development rather than something that must already exist in a typical codebase, making the zero results expected for a net-new capability.
- Searched 10 term(s) derived from this requirement (NotificationPreferences, consent, notification_channel, alert_settings, subscription, Notification, Preferences, Configure, Consent, Channel) against Cast; none resolved to an existing code object.
- Which components will need to be built to implement each acceptance criterion, since no existing implementation was found?

## Evidence References

- `553b39f8ceb22553`
- `sha256:1ba923446628e45036f6bab5b2f7d9e5796f2f515d99dfcd6032518685356da9`
- `determinism-snapshot`
- `impact_analysis:no_match_verdict:net_new_capability`
- `requirement_traceability:US-001-ac-1:unmapped`
- `requirement_traceability:US-001-ac-2:unmapped`
- `requirement_traceability:US-001-ac-3:unmapped`
- `requirement_traceability:US-001-ac-4:unmapped`
- `requirement_traceability:US-001-ac-5:unmapped`
- `requirement_traceability:US-001-ac-6:unmapped`
- `requirement_traceability:US-001-ac-7:unmapped`