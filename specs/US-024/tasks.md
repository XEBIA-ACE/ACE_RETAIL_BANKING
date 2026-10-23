# Tasks: US-024

## Implementation Tasks

- [ ] **T001** Implement FR-001: The notification preferences screen shall display three distinct notification types: large transaction alerts, low balance alerts, and bill due-date reminders. For each notification type, the screen shall display the available delivery channels (push, SMS, email) with their current enabled/disabled state.

- [ ] **T002** Implement FR-002: When a customer who has not previously consented to a notification type enables that type with at least one channel and confirms, the system shall record explicit consent including the notification type, selected channels, timestamp, and user identifier in an auditable data store. The system shall only permit notifications of that type to be sent via the channels for which consent was recorded.

- [ ] **T003** Implement FR-003: When a customer who previously consented to a notification type disables that type entirely and saves, the system shall record the withdrawal of consent with a timestamp and user identifier in an auditable data store. The system shall suppress all notifications of that type on all channels until the customer re-enables it.

- [ ] **T004** Implement FR-004: When processing a notification event, the system shall evaluate the customer's channel preferences for that notification type and shall only consider enabled channels for delivery. Disabled channels shall be excluded from the delivery consideration.

- [ ] **T005** Implement FR-005: Upon successful save of notification preference or consent changes, the system shall display a confirmation message in the customer's selected language. All preference changes shall take effect immediately for notifications triggered after the save timestamp.

- [ ] **T006** Implement FR-006: All interactive elements on the notification preferences screen (notification type toggles, channel selection controls, save/cancel buttons) shall be accessible via keyboard-only navigation and shall include appropriate ARIA labels and roles to meet WCAG 2.1 AA compliance for screen reader compatibility.

- [ ] **T007** Implement FR-007: The notification preferences screen shall support localization for German, French, Italian, and English languages. All UI labels, helper text, error messages, and confirmation messages shall be displayed in the customer's preferred language. Any date/time and timezone information displayed shall be formatted according to the locale conventions of the selected language.

## Verification Tasks

- [ ] **T008** Verify US-024-ac-1: Given a signed-in retail banking customer with access to notification settings, when the customer opens the notification preferences screen, then the screen lists large transaction alerts, low balance alerts, and bill due-date reminders as distinct types, and for each type the available channels (push, SMS, email) are visible with their current on/off state.

- [ ] **T009** Verify US-024-ac-2: Given a customer has not yet given consent for a specific notification type, when the customer enables that type and at least one channel and confirms, then the system records explicit consent for that notification type and selected channels in an auditable store, and subsequent notifications of that type are eligible to be sent only via the enabled channels.

- [ ] **T010** Verify US-024-ac-3: Given a customer previously consented to a notification type, when the customer disables that type entirely and saves changes, then the system records the withdrawal of consent for that type with a timestamp and user identifier, and no subsequent notifications of that type are sent on any channel until it is re-enabled.

- [ ] **T011** Verify US-024-ac-4: Given a customer has some channels enabled and others disabled for a notification type, when an event for that type is later processed, then only the enabled channels are considered for delivery, and any disabled channels are not used.

- [ ] **T012** Verify US-024-ac-5: Given the customer changes notification preferences or consent, when the customer saves the changes, then a confirmation is displayed in the selected language, and the updated preferences take effect for all new notifications triggered after the save time.

- [ ] **T013** Verify US-024-ac-6: Given accessibility support is required, when the preferences screen is used with a screen reader or keyboard-only navigation, then all interactive elements for notification types and channels are reachable and labelled according to WCAG 2.1 AA guidelines.

- [ ] **T014** Verify US-024-ac-7: Given the customer's preferred language is German, French, Italian, or English, when the notification preferences screen is opened, then all labels, helper text, and messages are displayed in that language, and date/time and timezone information on the screen (if any) is localized appropriately.

## Security and Quality Tasks

- [ ] **T015** Validate credential and personal-data handling against the Jira requirement.

- [ ] **T016** Validate credential and personal-data handling against the story's acceptance criteria.

- [ ] **T017** Verify the changed user flow against the stated accessibility criteria (WCAG 2.1 AA).