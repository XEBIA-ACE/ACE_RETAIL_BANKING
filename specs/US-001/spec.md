# Feature Specification: US-001

## Story
**US-001 — Multi-Step Application Form Navigation with In-Session Data Persistence**

As a New Bank Customer, I want to navigate forward and backward through a multi-step digital application form without losing previously entered data, so that I can complete my account opening application at my own pace without having to re-enter information. The form must: (1) retain and pre-populate data entered on any step when the customer navigates back to it within the same session; (2) display a step indicator showing the current step position and total number of steps; (3) present only the form steps relevant to the account type selected (checking, savings, or credit card); (4) ensure all step navigation controls are operable via keyboard and meet WCAG 2.1 AA standards; and (5) render all step labels, navigation controls, and instructions in the customer's selected locale (DE, FR, IT, or EN).

## User Scenarios & Acceptance Criteria

- **US-001-ac-1**: Given a New Bank Customer has started a new account application and entered data on a form step, When the customer navigates to the next step, Then the previously entered data on the prior step is retained and pre-populated when the customer navigates back.
- **US-001-ac-2**: Given a New Bank Customer is on any step of the multi-step form, When the customer navigates back to a previous step, Then all data entered in that step during the current session is displayed without requiring re-entry.
- **US-001-ac-3**: Given a New Bank Customer is completing the form, When the customer views the step indicator, Then the current step position and total number of steps are clearly visible.
- **US-001-ac-4**: Given a New Bank Customer selects an account type (checking, savings, or credit card) at the start of the form, When the form loads, Then only the form steps relevant to the selected account type are presented.
- **US-001-ac-5**: Given a New Bank Customer is using keyboard-only navigation, When the customer moves between form steps, Then all step navigation controls are operable via keyboard and meet WCAG 2.1 AA standards.
- **US-001-ac-6**: Given a New Bank Customer has selected a supported locale (DE, FR, IT, or EN), When the multi-step form is displayed, Then all step labels, navigation controls, and instructions are rendered in the selected locale.

## Functional Requirements

- **FR-001 [US-001-ac-1]**: The system shall retain all data entered by the customer on a given form step within the current session. When the customer navigates forward to a subsequent step and then navigates back to a prior step, the system shall pre-populate that step's fields with the previously entered values. Note: the scope of 'retained' is limited to the current session; cross-session persistence is not specified by this criterion and must not be assumed.
- **FR-002 [US-001-ac-2]**: The system shall ensure that, from any step of the multi-step form, when the customer navigates back to any previously visited step, all data entered in that step during the current session is displayed in the corresponding fields without requiring the customer to re-enter it. This requirement applies to all steps in the form, not only the immediately preceding step.
- **FR-003 [US-001-ac-3]**: The system shall display a persistent step indicator component that is visible to the customer at all times while completing the form. The indicator shall clearly show both the current step number (e.g., 'Step 2') and the total number of steps in the form sequence applicable to the customer's selected account type.
- **FR-004 [US-001-ac-4]**: The system shall require the customer to select an account type (checking, savings, or credit card) at the start of the application form. Upon form load following that selection, the system shall present only the form steps that are relevant to the selected account type; steps not applicable to the selected type shall not be displayed or navigable. Note: the specific step sets per account type are not defined in this criterion and represent an evidence gap requiring further specification.
- **FR-005 [US-001-ac-5]**: All step navigation controls (e.g., 'Next', 'Back', step indicator interactions) shall be fully operable using keyboard-only input. The implementation shall conform to WCAG 2.1 Level AA accessibility standards for all such controls. This requirement applies to all supported locales and account types.
- **FR-006 [US-001-ac-6]**: The system shall support locale selection for at least the following locales: German (DE), French (FR), Italian (IT), and English (EN). When the customer has selected a supported locale, all step labels, navigation control labels, and instructional text within the multi-step form shall be rendered in that locale. Note: the mechanism for locale selection and the handling of unsupported or unset locales are not specified by this criterion and represent an evidence gap requiring further specification.

## Success Criteria

- **US-001-ac-1**: Given a New Bank Customer has started a new account application and entered data on a form step, When the customer navigates to the next step, Then the previously entered data on the prior step is retained and pre-populated when the customer navigates back.
- **US-001-ac-2**: Given a New Bank Customer is on any step of the multi-step form, When the customer navigates back to a previous step, Then all data entered in that step during the current session is displayed without requiring re-entry.
- **US-001-ac-3**: Given a New Bank Customer is completing the form, When the customer views the step indicator, Then the current step position and total number of steps are clearly visible.
- **US-001-ac-4**: Given a New Bank Customer selects an account type (checking, savings, or credit card) at the start of the form, When the form loads, Then only the form steps relevant to the selected account type are presented.
- **US-001-ac-5**: Given a New Bank Customer is using keyboard-only navigation, When the customer moves between form steps, Then all step navigation controls are operable via keyboard and meet WCAG 2.1 AA standards.
- **US-001-ac-6**: Given a New Bank Customer has selected a supported locale (DE, FR, IT, or EN), When the multi-step form is displayed, Then all step labels, navigation controls, and instructions are rendered in the selected locale.
- **IMPACT**: Verify acceptance criterion: Given a New Bank Customer has started a new account application and entered data on a form step, When the customer navigates to the next step, Then the previously entered data on the prior step is retained and pre-populated when the customer navigates back.
- **IMPACT**: Verify acceptance criterion: Given a New Bank Customer is on any step of the multi-step form, When the customer navigates back to a previous step, Then all data entered in that step during the current session is displayed without requiring re-entry.
- **IMPACT**: Verify acceptance criterion: Given a New Bank Customer is completing the form, When the customer views the step indicator, Then the current step position and total number of steps are clearly visible.
- **IMPACT**: Verify acceptance criterion: Given a New Bank Customer selects an account type (checking, savings, or credit card) at the start of the form, When the form loads, Then only the form steps relevant to the selected account type are presented.
- **IMPACT**: Verify acceptance criterion: Given a New Bank Customer is using keyboard-only navigation, When the customer moves between form steps, Then all step navigation controls are operable via keyboard and meet WCAG 2.1 AA standards.
- **IMPACT**: Verify acceptance criterion: Given a New Bank Customer has selected a supported locale (DE, FR, IT, or EN), When the multi-step form is displayed, Then all step labels, navigation controls, and instructions are rendered in the selected locale.

## Evidence Gaps & Disclosures

> **⚠ DEGRADED — Net-New Capability:** No pre-existing implementation was found for this requirement; scope is derived solely from the requirement text, not from measured code evidence.
>
> The CAST scan of https://github.com/XEBIA-ACE/ACE_RETAIL_BANKING (commit `3feadfcc0d3ed193c698fd4f2e06d49737f2770e`, ref `main`) returned no web UI, JavaScript, HTML, or session-state management objects. All six acceptance criteria (US-001-ac-1 through US-001-ac-6) are classified as `net_new_capability` with no mapping to any existing provider-confirmed component in the CardDemo CAST snapshot (fingerprint: `553b39f8ceb22553`).

**Open evidence gaps (must be resolved or explicitly accepted before implementation):**

1. No database tables were returned for the focus object — direct DB table exposure cannot be confirmed.
2. No web UI, JavaScript, or HTML components were found in CAST measurements — AC-1 through AC-6 cannot be mapped to any existing structural component.
3. No session-state management objects (CICS temporary storage definitions, web session beans, etc.) were returned by CAST.
4. No i18n or locale framework components were found in the dependency inventory.
5. No formWizard object was returned by CAST — the named focus object resolves only to a DB2 message-formatting paragraph unrelated to form navigation.
6. Intra-object structural facts (loops, SQL-in-loop) were unavailable from CAST MCP for the focus object.
7. API inventory query returned errors — no REST or SOAP endpoints were confirmed for CardDemo.
8. Requirement evidence was gathered from 2 unrelated services/packages ('multiStep' (app-authorization-ims-db2-mq/jcl), 'formWizard' (app-transaction-type-db2/cpy)) — the measured blast radius may mix facts that do not all belong to the single focus object named above. Review each named symbol's own source before accepting.
9. Candidate verification found the selected focus object implausible for this requirement: The requirement is about multi-step application form navigation with in-session data persistence for a digital banking onboarding UI. The selected object '9999-FORMAT-DB2-MESSAGE' is a COBOL paragraph focused on formatting DB2 database error/status messages, which has no semantic relationship to form navigation, step management, or session data persistence. None of the candidates in the list (JCL Steps and DB2-related COBOL paragraphs) appear to be a clearly better match for this UI/session-management requirement either.
10. Searched 3 term(s) derived from this requirement (multiStep, stepNavigation, formWizard) against Cast; none resolved to an existing code object.
11. No-match classification caveat: The search terms tried were highly specific compound identifiers (multiStep, stepNavigation, formWizard), but common implementations of this pattern in banking/fintech codebases often use more generic terms like "stepper", "wizard", "accordion", or session/state management hooks. No attempt was made to search for these synonyms, leaving some doubt — though there is no concrete evidence the capability already exists in this codebase.
12. Architecture context is marked degraded (missing_core: api_contracts) — no REST or SOAP endpoints are confirmed for CardDemo; any API surface required to support the new form capability must be specified and confirmed before implementation steps can be grounded.
13. Target service(s), UI component library, session-state mechanism, and i18n framework are all unconfirmed — grounded design steps cannot be produced until provider evidence is available for each.
14. Which components will need to be built to implement each acceptance criterion, since no existing implementation was found?

## Provenance

- **Policy version**: `ace-spec-format/v1`
- **Accepted impact analysis revision**: `1`
- **Accepted impact analysis**: `sha256:c1e980b9a7955afb543f534ffdce23877a4c57eae38e3e43760724fd48d7a4fe`
- **Repository baseline commit**: `3feadfcc0d3ed193c698fd4f2e06d49737f2770e`
- **Repository ref**: `main`
- **Repository**: `https://github.com/XEBIA-ACE/ACE_RETAIL_BANKING`
- **Deterministic grounding reference**: `553b39f8ceb22553`
- **Unresolved reference**: `determinism-snapshot:sha256:ede1fb09fa5e7c07abb25853f0942b8a38a2a15111ef8427dd8a2f38e67b44c3`
- **Run ID**: `2037e17e-0ef9-523d-85e7-3fa766282e26`
- **Project ID**: `f53bb21d-f655-4fb8-9707-fe9cbaaded89`
- **Generation mode**: `enhancement`
- **Codegen target**: `manual`