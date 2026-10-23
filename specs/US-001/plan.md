# Implementation Plan: US-001

## Technical Context

- **Codebase grounding snapshot**: `553b39f8ceb22553`
- **Accepted impact analysis**: `sha256:c1e980b9a7955afb543f534ffdce23877a4c57eae38e3e43760724fd48d7a4fe` (revision 1)
- **Unresolved reference**: `determinism-snapshot:sha256:ede1fb09fa5e7c07abb25853f0942b8a38a2a15111ef8427dd8a2f38e67b44c3`
- **Repository**: `https://github.com/XEBIA-ACE/ACE_RETAIL_BANKING` @ commit `3feadfcc0d3ed193c698fd4f2e06d49737f2770e` (ref: `main`)
- **Implementation scope**: Net-new capability — no existing counterpart was found in the CardDemo CAST snapshot.

> **⚠ DEGRADED TECHNICAL DESIGN:** No confirmed affected_services, affected_symbols, or affected_files were returned by the impact analysis for US-001. All six acceptance criteria (US-001-ac-1 through US-001-ac-6) are classified as `net_new_capability` with no mapping to any existing provider-confirmed component in the CardDemo CAST snapshot (fingerprint: `553b39f8ceb22553`). No design steps touching confirmed components can be produced at this time.

## Design Steps (Pending Grounding)

The following logical design steps are identified from the requirement but **cannot be accepted as grounded design steps** until provider evidence confirms the target service(s), UI component library, session-state mechanism, and i18n framework:

1. **[FR-001, FR-002] Client-side session-state store** — Implement a client-side session-state store to satisfy FR-001 and FR-002 (in-session data retention and pre-population across all steps). Scope of retention is limited to the current session; cross-session persistence must not be assumed.
2. **[FR-003] StepIndicator component** — Build a StepIndicator component satisfying FR-003 (visible current-step and total-step count, persistent throughout form completion).
3. **[FR-004] Account-type-conditional step routing** — Implement account-type-conditional step routing satisfying FR-004 (checking / savings / credit card step sets). Note: the specific step sets per account type are not defined and represent an evidence gap requiring further specification.
4. **[FR-005] Keyboard accessibility and WCAG 2.1 AA compliance** — Ensure all navigation controls (Next, Back, step indicator) are keyboard-operable and WCAG 2.1 AA compliant per FR-005. Applies to all supported locales and account types.
5. **[FR-006] i18n/locale framework integration** — Integrate an i18n/locale framework to render all step labels, navigation controls, and instructions in DE, FR, IT, and EN per FR-006. Note: the mechanism for locale selection and handling of unsupported/unset locales are not specified and represent an evidence gap.

Each step must cite the confirmed service identifier and component name before it can be accepted as a grounded design step.

## Implementation-Readiness Blockers

The following items must be resolved or explicitly accepted before implementation proceeds:

1. API inventory query returned errors — no REST or SOAP endpoints were confirmed for CardDemo.
2. Candidate verification found the selected focus object implausible for this requirement: The requirement is about multi-step application form navigation with in-session data persistence for a digital banking onboarding UI. The selected object '9999-FORMAT-DB2-MESSAGE' is a COBOL paragraph focused on formatting DB2 database error/status messages, which has no semantic relationship to form navigation, step management, or session data persistence. None of the candidates in the list (JCL Steps and DB2-related COBOL paragraphs) appear to be a clearly better match for this UI/session-management requirement either.
3. Intra-object structural facts (loops, SQL-in-loop) were unavailable from CAST MCP for the focus object.
4. No database tables were returned for the focus object — direct DB table exposure cannot be confirmed.
5. No formWizard object was returned by CAST — the named focus object resolves only to a DB2 message-formatting paragraph unrelated to form navigation.
6. No i18n or locale framework components were found in the dependency inventory.
7. No pre-existing implementation was found for this requirement; scope is derived solely from the requirement text, not from measured code evidence.
8. No session-state management objects (CICS temporary storage definitions, web session beans, etc.) were returned by CAST.
9. No web UI, JavaScript, or HTML components were found in CAST measurements — AC-1 through AC-6 cannot be mapped to any existing structural component.
10. No-match classification caveat: The search terms tried were highly specific compound identifiers (multiStep, stepNavigation, formWizard), but common implementations of this pattern in banking/fintech codebases often use more generic terms like "stepper", "wizard", "accordion", or session/state management hooks. No attempt was made to search for these synonyms, leaving some doubt — though there is no concrete evidence the capability already exists in this codebase.
11. No-match consistency check raised a caveat about this classification (same as above).
12. Requirement detail is incomplete (see evidence_gaps); no conflicting existing implementation was found.
13. Requirement evidence was gathered from 2 unrelated services/packages ('multiStep' (app-authorization-ims-db2-mq/jcl), 'formWizard' (app-transaction-type-db2/cpy)) — the measured blast radius may mix facts that do not all belong to the single focus object named above. Review each named symbol's own source before accepting.
14. Searched 3 term(s) derived from this requirement (multiStep, stepNavigation, formWizard) against Cast; none resolved to an existing code object.
15. Which components will need to be built to implement each acceptance criterion, since no existing implementation was found?
16. Architecture context is marked degraded (missing_core: api_contracts) — no REST or SOAP endpoints are confirmed for CardDemo; any API surface required to support the new form capability must be specified and confirmed before implementation steps can be grounded.
17. Target service(s), UI component library, session-state mechanism, and i18n framework are all unconfirmed — grounded design steps cannot be produced until provider evidence is available for each.

## Contract Changes

- No material API, event, or data contract change is evidenced. `contract_change_required: false`.
- API readiness check status: **blocked** — API inventory query returned errors; no REST or SOAP endpoints were confirmed for CardDemo.

## Security and Quality Obligations

- Preserve the security controls evidenced by `553b39f8ceb22553`.
- Verify the changed user flow against the stated accessibility criteria (WCAG 2.1 AA — FR-005).

## Verification Plan

| Criterion | Obligation |
|-----------|------------|
| US-001-ac-1 | Given a New Bank Customer has started a new account application and entered data on a form step, When the customer navigates to the next step, Then the previously entered data on the prior step is retained and pre-populated when the customer navigates back. |
| US-001-ac-2 | Given a New Bank Customer is on any step of the multi-step form, When the customer navigates back to a previous step, Then all data entered in that step during the current session is displayed without requiring re-entry. |
| US-001-ac-3 | Given a New Bank Customer is completing the form, When the customer views the step indicator, Then the current step position and total number of steps are clearly visible. |
| US-001-ac-4 | Given a New Bank Customer selects an account type (checking, savings, or credit card) at the start of the form, When the form loads, Then only the form steps relevant to the selected account type are presented. |
| US-001-ac-5 | Given a New Bank Customer is using keyboard-only navigation, When the customer moves between form steps, Then all step navigation controls are operable via keyboard and meet WCAG 2.1 AA standards. |
| US-001-ac-6 | Given a New Bank Customer has selected a supported locale (DE, FR, IT, or EN), When the multi-step form is displayed, Then all step labels, navigation controls, and instructions are rendered in the selected locale. |
| IMPACT | Verify acceptance criterion: Given a New Bank Customer has started a new account application and entered data on a form step, When the customer navigates to the next step, Then the previously entered data on the prior step is retained and pre-populated when the customer navigates back. |
| IMPACT | Verify acceptance criterion: Given a New Bank Customer is on any step of the multi-step form, When the customer navigates back to a previous step, Then all data entered in that step during the current session is displayed without requiring re-entry. |
| IMPACT | Verify acceptance criterion: Given a New Bank Customer is completing the form, When the customer views the step indicator, Then the current step position and total number of steps are clearly visible. |
| IMPACT | Verify acceptance criterion: Given a New Bank Customer selects an account type (checking, savings, or credit card) at the start of the form, When the form loads, Then only the form steps relevant to the selected account type are presented. |
| IMPACT | Verify acceptance criterion: Given a New Bank Customer is using keyboard-only navigation, When the customer moves between form steps, Then all step navigation controls are operable via keyboard and meet WCAG 2.1 AA standards. |
| IMPACT | Verify acceptance criterion: Given a New Bank Customer has selected a supported locale (DE, FR, IT, or EN), When the multi-step form is displayed, Then all step labels, navigation controls, and instructions are rendered in the selected locale. |

All test obligations are of type `characterization`, required before `release`, in environment `ci`.

## Traceability

| Criterion | Functional Spec Ref | Accepted Impact Hash |
|-----------|--------------------|-----------------------|
| US-001-ac-1 | spec_functional | sha256:c1e980b9a7955afb543f534ffdce23877a4c57eae38e3e43760724fd48d7a4fe |
| US-001-ac-2 | spec_functional | sha256:c1e980b9a7955afb543f534ffdce23877a4c57eae38e3e43760724fd48d7a4fe |
| US-001-ac-3 | spec_functional | sha256:c1e980b9a7955afb543f534ffdce23877a4c57eae38e3e43760724fd48d7a4fe |
| US-001-ac-4 | spec_functional | sha256:c1e980b9a7955afb543f534ffdce23877a4c57eae38e3e43760724fd48d7a4fe |
| US-001-ac-5 | spec_functional | sha256:c1e980b9a7955afb543f534ffdce23877a4c57eae38e3e43760724fd48d7a4fe |
| US-001-ac-6 | spec_functional | sha256:c1e980b9a7955afb543f534ffdce23877a4c57eae38e3e43760724fd48d7a4fe |