# Tasks: US-001

## Implementation Tasks

- [ ] **T001** FR-001: The system shall display all available notification types (including large transaction, low balance, and bill due reminders) with their current enable/disable state when an authenticated customer opens notification settings.

- [ ] **T002** FR-002: The system shall allow customers to configure channel preferences (e.g., SMS, email) independently for each notification type that supports multiple channels, and shall persist these preferences so they are reflected on subsequent access to the settings.

- [ ] **T003** FR-003: For notification types or channels that require explicit consent under applicable regulations, the system shall prompt the customer for consent using approved language when they first enable that type or regulated channel, and shall record the consent decision.

- [ ] **T004** FR-004: The system shall display a clear confirmation message in the customer's selected language (de/fr/it/en) when any notification preference or consent update is successfully saved.

- [ ] **T005** FR-005: When an application-level validation error occurs during a notification preference update (e.g., missing required consent acknowledgement), the system shall display an understandable, non-technical error message that explains what is wrong and how to correct it.

- [ ] **T006** FR-006: The system shall suppress notifications of a specific type on all configured channels for a customer when that customer has disabled that notification type, even when the trigger condition for that notification occurs.

- [ ] **T007** FR-007: All notification preference controls and confirmation/error messages shall remain readable and operable when high-contrast mode or larger text is enabled, and shall be compatible with screen readers in compliance with WCAG 2.1 AA.

## Verification Tasks

- [ ] **T008** Verify US-001-ac-1: Given an authenticated customer, When they open notification settings, Then the available notification types (including large transaction, low balance, and bill due reminders) and their current enable/disable state are displayed.

- [ ] **T009** Verify US-001-ac-2: Given a notification type that supports multiple channels, When the customer updates their channel preferences for that type (e.g., enables SMS and disables email), Then the preferences are persisted and reflected the next time settings are opened.

- [ ] **T010** Verify US-001-ac-3: Given a notification type that requires explicit consent under applicable regulations, When the customer first enables that type or a regulated channel for it, Then the application requests consent using approved language and records the consent decision.

- [ ] **T011** Verify US-001-ac-4: Given the customer successfully updates any notification preference or consent, When the update is saved, Then a clear confirmation message is displayed to the customer in their selected language (de/fr/it/en).

- [ ] **T012** Verify US-001-ac-5: Given the customer attempts to update notification preferences and an application-level validation error occurs (for example, missing required consent acknowledgement), When the update fails, Then an understandable, non-technical error message explains what is wrong and how to fix it.

- [ ] **T013** Verify US-001-ac-6: Given a customer disables a specific notification type, When its trigger condition later occurs, Then that type of notification is not sent on any channel configured for that customer.

- [ ] **T014** Verify US-001-ac-7: Given the application is used with high-contrast mode or larger text enabled, When the customer manages notification preferences, Then all controls and confirmation/error messages remain readable and operable with screen readers as per WCAG 2.1 AA.

## Provenance

- **Accepted impact analysis:** `sha256:1ba923446628e45036f6bab5b2f7d9e5796f2f515d99dfcd6032518685356da9`
- **Grounding snapshot:** `553b39f8ceb22553`