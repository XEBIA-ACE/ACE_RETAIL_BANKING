# Implementation Plan: US-027

## Implementation Status

**Status:** BLOCKED

This implementation is currently blocked due to the following reasons identified in impact analysis:

1. Candidate verification could not confidently judge the selected focus object for this requirement
2. No Notification Service integration exists in the measured structural facts - the focus object and transaction graph show only CICS screen handling and VSAM file operations
3. No channel selection or multi-channel delivery capabilities present in the current architecture
4. No customer preference or consent management structures detected in the codebase analysis
5. No event logging infrastructure for notification delivery tracking found in database objects
6. No reminder scheduling or timing logic components identified in the transaction call graph
7. Requirement traceability incomplete for US-027-ac-1, US-027-ac-2, US-027-ac-3, US-027-ac-4, US-027-ac-5, US-027-ac-6
8. The focus object RECEIVE-BILLPAY-SCREEN is a screen input handler, not a notification generation component

## Technical Context

### Grounding References
- Codebase grounding snapshot: `553b39f8ceb22553`
- Accepted impact analysis: `sha256:5c94671e58c63f0f0f5263aa1500b26bbaa33d844df0b37b37175c0771cb3002`

### Affected Components
- `C:\cast-node\common-data\upload\CardDemo\main_sources\app\cbl\COBIL00C.cbl`
- `COBIL0A` (CICS Map)
- `MAIN-PARA` (Cobol Paragraph)
- `RECEIVE-BILLPAY-SCREEN` (Cobol Paragraph)

### Unresolved References
- component:C:\cast-node\common-data\upload\CardDemo\main_sources\app\cbl\COBIL00C.cbl
- component:COBIL0A
- component:MAIN-PARA
- component:RECEIVE-BILLPAY-SCREEN
- impact_analysis:affected_files
- impact_analysis:affected_symbols
- impact_analysis:blocking_reasons

## Implementation Readiness Items (Must Resolve or Accept)

- Bill payment data may exist in the TRANSACT and ACCTDAT VSAM files but no bill due-date or scheduling fields are confirmed
- Candidate verification could not confidently judge the selected focus object for this requirement
- Candidate verification was uncertain about the selected focus object: The requirement is about receiving bill due-date reminders based on preferences and consent, involving notification generation and delivery. The selected object "RECEIVE-BILLPAY-SCREEN" is a Cobol Paragraph whose name suggests handling screen reception for bill payment UI, not reminder notification logic.
- How would reminder timing schedules be configured and triggered in the COBOL/CICS environment?
- Implementation would require new notification service integration not present in the legacy COBOL/CICS architecture
- No Notification Service integration exists in the measured structural facts
- No accessibility or localization infrastructure identified
- No channel selection or multi-channel delivery capabilities present in the current architecture
- No customer preference or consent management structures detected in the codebase analysis
- No customer preference/consent data structures found
- No event logging infrastructure for notification delivery tracking found in database objects
- No event/audit logging for notification delivery found
- No notification service or messaging infrastructure identified in CAST analysis
- No reminder scheduling or timing logic components identified in the transaction call graph
- No reminder timing or scheduling components detected
- No retention policy or history retrieval components found
- No-match classification (all acceptance criteria unmapped): Initial classification (net-new capability) has a noted caveat
- Requirement traceability incomplete for US-027-ac-1, US-027-ac-2, US-027-ac-3, US-027-ac-4, US-027-ac-5, US-027-ac-6
- The focus object 'bill' was resolved to RECEIVE-BILLPAY-SCREEN (13699) as the most relevant bill-related component in CardDemo
- The focus object RECEIVE-BILLPAY-SCREEN is a screen input handler, not a notification generation component
- What audit logging mechanism would track notification delivery events?
- What notification service or messaging infrastructure would be integrated for multi-channel delivery?
- Where would customer notification preferences and consent be stored in the CardDemo application?

## Contract Changes Required

| Contract | Type | Change Required |
|----------|------|------------------|
| RECEIVE-BILLPAY-SCREEN (unknown) | database object accessed | Yes |
| SEND-BILLPAY-SCREEN (unknown) | database object accessed | Yes |
| AWS.M2.CARDDEMO.TRANSACT.VSAM.KSDS (1155) | VSAM file accessed via transaction graph | Yes |
| AWS.M2.CARDDEMO.ACCTDATA.VSAM.KSDS (1111) | VSAM file accessed via transaction graph | Yes |
| AWS.M2.CARDDEMO.USRSEC.VSAM.KSDS (1093) | VSAM file accessed via transaction graph | Yes |

### Contract Change Reasons
- Grounded data-contract impact was identified: VSAM files TRANSACT, ACCTDATA, and USRSEC require new fields for notification preferences, consent, and reminder scheduling.
- New notification event storage structures required for delivery lifecycle tracking (created, sent, delivered, failed events).

## Design Steps

1. **Step 1**: Extend COBIL00C.cbl to add bill due-date reminder generation logic within the bill payment processing flow. This requires adding new WORKING-STORAGE fields for reminder timing configuration, consent flags, and notification payload structures. The existing RECEIVE-BILLPAY-SCREEN paragraph will be modified to capture bill scheduling data needed for reminder timing calculations.

2. **Step 2**: Modify MAIN-PARA in COBIL00C.cbl to include a new PERFORM section that evaluates reminder timing thresholds against upcoming bill due dates. Add conditional logic to check customer consent and preference flags before invoking reminder generation, with audit logging when reminders are suppressed due to disabled preferences or withdrawn consent.

3. **Step 3**: Extend RECEIVE-BILLPAY-SCREEN paragraph to extract and validate bill scheduling data (due date, payment status) from the TRANSACT and ACCTDAT VSAM files. Add data structures to hold reminder configuration including timing thresholds and maximum reminder counts per bill.

4. **Step 4**: Modify COBIL0A CICS Map to support notification history display fields showing reminder type, localized timestamp, and high-level description. Add screen navigation elements that comply with accessibility standards for notification history retrieval.

5. **Step 5**: Add new WORKING-STORAGE section in COBIL00C.cbl for notification event logging including notification type, channel, timestamps, correlation identifiers, and delivery status (created, sent, delivered, failed). Ensure sensitive bill/payee data is excluded from persisted event records per policy compliance.

6. **Step 6**: Implement channel-specific delivery routing logic in COBIL00C.cbl that reads customer channel preferences and routes reminder notifications only to enabled channels, suppressing delivery attempts on disabled channels.

7. **Step 7**: Add business rule enforcement logic to track reminder count per bill and prevent exceeding the configured maximum number of reminders regardless of elapsed timing thresholds.

## Security and Quality Obligations

### Security Obligations
- Sensitive bill or payee data must not be persisted beyond policy-compliant fields per AC-5
- Customer consent must be validated before sending any notifications per AC-1 and AC-2
- Validate credential and personal-data handling against the story's acceptance criteria

### Quality Attribute Obligations
- Verify the changed user flow against the stated accessibility criteria

## Verification Plan

| Criterion | Verification Obligation |
|-----------|-------------------------|
| US-027-ac-1 | Given a customer has enabled bill due-date reminders and provided consent for this type And bill-payment or scheduling data exists for an upcoming bill When the system reaches a configured reminder timing (for example, a business-defined number of days before due date) Then a bill due-date reminder notification is generated And is sent via the customer's selected channels using the Notification Service |
| US-027-ac-2 | Given a customer has disabled bill due-date reminders or withdrawn consent for this type When a reminder timing is reached for one of the customer's bills Then no bill due-date reminder notification is sent on any channel And the reason for not sending is logged for audit |
| US-027-ac-3 | Given a customer has enabled bill reminders for some channels and disabled others When a reminder timing is reached Then notifications are sent only on the enabled channels And no delivery is attempted via disabled channels |
| US-027-ac-4 | Given one or more reminder timings are configured per bill When multiple reminder timings elapse for the same unpaid bill Then reminders are generated according to the defined schedule without exceeding the allowed number of reminders per bill as defined by business rules |
| US-027-ac-5 | Given a bill due-date reminder notification is processed When delivery attempts complete for each relevant channel Then created, sent, delivered, and failed events are stored with notification type, channel, timestamps, and correlation identifiers And sensitive bill or payee data is not persisted beyond policy-compliant fields |
| US-027-ac-6 | Given a customer has past bill due-date reminders within the retention window When the customer opens notification history from the bill payments journey or dashboard Then those reminders appear with type, localized timestamp, and high-level description And navigation respects accessibility and localization standards |
| IMPACT | Test reminder generation when customer has enabled reminders and consent is provided |
| IMPACT | Test that no reminders are sent when customer has disabled reminders or withdrawn consent |
| IMPACT | Test channel-specific delivery based on enabled/disabled channel preferences |
| IMPACT | Test reminder frequency limits per bill according to business rules |
| IMPACT | Test event storage for all notification delivery states (created, sent, delivered, failed) |
| IMPACT | Test notification history retrieval with proper localization |

## Evidence References

- `553b39f8ceb22553` - Codebase grounding snapshot
- `sha256:5c94671e58c63f0f0f5263aa1500b26bbaa33d844df0b37b37175c0771cb3002` - Accepted impact analysis
- `component:C:\cast-node\common-data\upload\CardDemo\main_sources\app\cbl\COBIL00C.cbl`
- `component:COBIL0A`
- `component:MAIN-PARA`
- `component:RECEIVE-BILLPAY-SCREEN`
- `impact_analysis:affected_files`
- `impact_analysis:affected_symbols`
- `impact_analysis:blocking_reasons`