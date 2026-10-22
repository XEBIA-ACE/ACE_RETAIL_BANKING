# Tasks: US-011
## View Notification Details with Delivery Status

> **Provenance**
> - **Policy version**: `ace-spec-format/v1`
> - **Accepted impact analysis revision**: `1`
> - **Accepted impact analysis**: `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`
> - **Repository baseline commit**: `3feadfcc0d3ed193c698fd4f2e06d49737f2770e`
> - **Deterministic grounding reference**: `553b39f8ceb22553`
> - **Run ID**: `33fadfa7-7042-522f-b6f9-f245c122a451`

---

### Implementation Tasks

- [ ] **T001** — FR-001 [US-011-ac-1]: The system shall provide a notification detail view that is navigable from the notification history list. When the customer selects any notification entry from a non-empty history list, the system shall open the detail view scoped to that specific notification. The mechanism for opening the detail view (e.g., modal, new screen, drawer) is not specified by the acceptance criterion and must be resolved during design; this ambiguity is preserved here rather than silently resolved. NOTE: No existing notification detail view was found in the measured CardDemo codebase; this view must be built as a net-new component.

- [ ] **T002** — FR-002 [US-011-ac-2]: When the notification detail view is opened for a notification whose delivery status is recorded as 'delivered', the system shall display (a) the delivery status labelled or indicated as 'delivered', and (b) the timestamp of the notification event. The exact format and label for the timestamp are not specified by the acceptance criterion and must be defined during design.

- [ ] **T003** — FR-003 [US-011-ac-3]: When the notification detail view is opened for a notification whose delivery status is recorded as 'failed', the system shall display the delivery status as 'failed'. The system shall not expose internal error codes, stack traces, system messages, or any other technical failure details that are not intended for customer consumption. The boundary between customer-safe failure messaging and internal error detail must be defined by the implementation team, as the acceptance criterion does not enumerate specific suppressed fields.

- [ ] **T004** — FR-004 [US-011-ac-4]: When a notification was configured for multiple delivery channels and the channel-usage log reflects the channels used or attempted, the notification detail view shall display each channel (e.g., push, SMS, email) that was attempted or used for that notification. The acceptance criterion does not specify the visual treatment or ordering of channels; these must be resolved during design. NOTE: No channel-logging construct was found in the measured codebase; channel logging infrastructure must be built as a net-new capability.

- [ ] **T005** — FR-005 [US-011-ac-5]: The notification detail view shall enforce defined privacy and masking rules when rendering notification history content. Only the approved core message content shall be displayed to the customer; any sensitive data fields subject to masking rules shall be masked or suppressed in accordance with those rules. The specific fields subject to masking and the masking rules themselves are not defined in the acceptance criterion and must be specified separately before implementation. NOTE: No data-masking or privacy-filter logic was found in the measured codebase; this must be built as a net-new capability.

- [ ] **T006** — FR-006 [US-011-ac-6]: All content and interactive controls on the notification detail view shall conform to WCAG 2.1 Level AA accessibility requirements. This includes, but is not limited to, compatibility with screen readers and high-contrast display modes for authenticated customers using supported assistive technology. The specific assistive technologies to be supported and the verification method are not enumerated in the acceptance criterion; these must be defined during design and test planning. The impact analysis identifies this as an accessibility obligation requiring explicit verification.

---

### Design & Architecture Tasks

- [ ] **T007** — STEP 3: Define NotificationDetail data contract (notificationId, deliveryStatus enum, eventTimestamp, channelsAttempted list, coreMessageContent). Resolve timestamp format and channel ordering. Prerequisite for STEP 4 and STEP 6. Evidence: `553b39f8ceb22553`.

- [ ] **T008** — STEP 4: Build net-new NotificationDetail API endpoint (e.g., GET /notifications/{notificationId}). Enforce authentication; suppress internal error detail on failed status; include channelsAttempted. Evidence: `553b39f8ceb22553`, `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`.

- [ ] **T009** — STEP 5: Build net-new channel-logging infrastructure to record push/SMS/email delivery attempts per notificationId. Evidence: `553b39f8ceb22553`.

- [ ] **T010** — STEP 6: Implement net-new privacy/masking filter. PREREQUISITE: Masking rules and sensitive fields must be formally specified first (FR-005 open ambiguity). Evidence: `553b39f8ceb22553`.

- [ ] **T011** — STEP 7: Build net-new NotificationDetail UI view navigable from notification history list. Resolve open design decision: mechanism for opening detail view (modal, new screen, drawer). Implement WCAG 2.1 AA support. Evidence: `553b39f8ceb22553`, `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`.

---

### Verification Tasks

- [ ] **T012** — Verify US-011-ac-1: Given a customer is viewing their notification history list and at least one notification is present
  When the customer selects a specific notification entry
  Then a notification detail view opens for that notification

- [ ] **T013** — Verify US-011-ac-2: Given a notification has completed delivery processing and a delivery status is recorded as delivered
  When the customer opens the notification detail view for that notification
  Then the delivery status is shown as delivered and the timestamp of the notification event is displayed

- [ ] **T014** — Verify US-011-ac-3: Given a notification has completed delivery processing and a delivery status is recorded as failed
  When the customer opens the notification detail view for that notification
  Then the delivery status is shown as failed without exposing internal error details not intended for customers

- [ ] **T015** — Verify US-011-ac-4: Given a notification was configured for multiple channels (for example, push and email) and the logging reflects the channels used
  When the customer opens the notification detail view
  Then the channels that were attempted or used (push, SMS, email) are displayed to the customer

- [ ] **T016** — Verify US-011-ac-5: Given privacy and masking rules have been defined for notification history content
  When the customer views the notification detail screen
  Then only the approved core message content is displayed and any sensitive data is masked according to those rules

- [ ] **T017** — Verify US-011-ac-6: Given an authenticated customer uses supported assistive technology (such as a screen reader or high-contrast mode)
  When the customer navigates to and uses the notification detail view
  Then all content and controls on the detail view are usable in line with WCAG 2.1 AA-aligned accessibility requirements

- [ ] **T018** — STEP 8 (Accessibility verification): Conduct explicit WCAG 2.1 AA verification after UI view is built — screen reader compatibility, high-contrast mode, keyboard navigability, focus management. Define supported assistive technologies and verification method during test planning (FR-006 open ambiguity). Evidence: `553b39f8ceb22553`.

- [ ] **T019** — STEP 9 (Security review): Validate NotificationDetail API endpoint and privacy/masking layer enforce authentication, suppress internal error details on failed status, and mask sensitive personal data per defined rules. Evidence: `sha256:a8df2023fa9bf530bef01e0f56a10191c323b4212b663ece644a8d7080187b40`.

---

### Impact-Derived Verification Tasks

- [ ] **T020** — Verify IMPACT: Verify acceptance criterion: Given a customer is viewing their notification history list and at least one notification is present
  When the customer selects a specific notification entry
  Then a notification detail view opens for that notification

- [ ] **T021** — Verify IMPACT: Verify acceptance criterion: Given a notification has completed delivery processing and a delivery status is recorded as delivered
  When the customer opens the notification detail view for that notification
  Then the delivery status is shown as delivered and the timestamp of the notification event is displayed

- [ ] **T022** — Verify IMPACT: Verify acceptance criterion: Given a notification has completed delivery processing and a delivery status is recorded as failed
  When the customer opens the notification detail view for that notification
  Then the delivery status is shown as failed without exposing internal error details not intended for customers

- [ ] **T023** — Verify IMPACT: Verify acceptance criterion: Given a notification was configured for multiple channels (for example, push and email) and the logging reflects the channels used
  When the customer opens the notification detail view
  Then the channels that were attempted or used (push, SMS, email) are displayed to the customer

- [ ] **T024** — Verify IMPACT: Verify acceptance criterion: Given privacy and masking rules have been defined for notification history content
  When the customer views the notification detail screen
  Then only the approved core message content is displayed and any sensitive data is masked according to those rules

- [ ] **T025** — Verify IMPACT: Verify acceptance criterion: Given an authenticated customer uses supported assistive technology (such as a screen reader or high-contrast mode)
  When the customer navigates to and uses the notification detail view
  Then all content and controls on the detail view are usable in line with WCAG 2.1 AA-aligned accessibility requirements
