# Tasks: US-027

## Implementation Tasks

- [ ] **T001** FR-001: The system shall generate a bill due-date reminder notification when a customer has enabled bill due-date reminders, provided consent for this notification type, bill-payment or scheduling data exists for an upcoming bill, and the configured reminder timing threshold is reached (e.g., a business-defined number of days before due date).

- [ ] **T002** FR-002: The system shall deliver generated bill due-date reminder notifications via the customer's selected channels using the Notification Service.

- [ ] **T003** FR-003: The system shall suppress bill due-date reminder notifications on all channels when a customer has disabled bill due-date reminders or withdrawn consent for this notification type.

- [ ] **T004** FR-004: The system shall log the reason for not sending a reminder notification for audit purposes when a reminder is suppressed due to disabled preferences or withdrawn consent.

- [ ] **T005** FR-005: The system shall deliver reminder notifications only on channels that the customer has enabled, and shall not attempt delivery via channels the customer has disabled.

- [ ] **T006** FR-006: The system shall support configuration of one or more reminder timings per bill and generate reminders according to the defined schedule when each timing elapses for an unpaid bill.

- [ ] **T007** FR-007: The system shall enforce business rules limiting the maximum number of reminders per bill, ensuring reminders do not exceed the allowed count regardless of how many timing thresholds elapse.

- [ ] **T008** FR-008: The system shall store notification lifecycle events (created, sent, delivered, and failed) with notification type, channel, timestamps, and correlation identifiers when delivery attempts complete for each relevant channel.

- [ ] **T009** FR-009: The system shall not persist sensitive bill or payee data beyond policy-compliant fields when storing notification events.

- [ ] **T010** FR-010: The system shall display past bill due-date reminders within the retention window when a customer opens notification history from the bill payments journey or dashboard, showing notification type, localized timestamp, and high-level description.

- [ ] **T011** FR-011: The system shall ensure notification history navigation respects accessibility and localization standards.

## Verification Tasks

- [ ] **T012** Verify US-027-ac-1: Given a customer has enabled bill due-date reminders and provided consent for this type And bill-payment or scheduling data exists for an upcoming bill When the system reaches a configured reminder timing (for example, a business-defined number of days before due date) Then a bill due-date reminder notification is generated And is sent via the customer's selected channels using the Notification Service

- [ ] **T013** Verify US-027-ac-2: Given a customer has disabled bill due-date reminders or withdrawn consent for this type When a reminder timing is reached for one of the customer's bills Then no bill due-date reminder notification is sent on any channel And the reason for not sending is logged for audit

- [ ] **T014** Verify US-027-ac-3: Given a customer has enabled bill reminders for some channels and disabled others When a reminder timing is reached Then notifications are sent only on the enabled channels And no delivery is attempted via disabled channels

- [ ] **T015** Verify US-027-ac-4: Given one or more reminder timings are configured per bill When multiple reminder timings elapse for the same unpaid bill Then reminders are generated according to the defined schedule without exceeding the allowed number of reminders per bill as defined by business rules

- [ ] **T016** Verify US-027-ac-5: Given a bill due-date reminder notification is processed When delivery attempts complete for each relevant channel Then created, sent, delivered, and failed events are stored with notification type, channel, timestamps, and correlation identifiers And sensitive bill or payee data is not persisted beyond policy-compliant fields

- [ ] **T017** Verify US-027-ac-6: Given a customer has past bill due-date reminders within the retention window When the customer opens notification history from the bill payments journey or dashboard Then those reminders appear with type, localized timestamp, and high-level description And navigation respects accessibility and localization standards

## Impact-Derived Verification Tasks

- [ ] **T018** Verify IMPACT: Test reminder generation when customer has enabled reminders and consent is provided

- [ ] **T019** Verify IMPACT: Test that no reminders are sent when customer has disabled reminders or withdrawn consent

- [ ] **T020** Verify IMPACT: Test channel-specific delivery based on enabled/disabled channel preferences

- [ ] **T021** Verify IMPACT: Test reminder frequency limits per bill according to business rules

- [ ] **T022** Verify IMPACT: Test event storage for all notification delivery states (created, sent, delivered, failed)

- [ ] **T023** Verify IMPACT: Test notification history retrieval with proper localization