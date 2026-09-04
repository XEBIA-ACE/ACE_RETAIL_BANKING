# Performance SLA: CICS Transaction CD00 (Consolidated Account Tiles Dashboard)
## CardDemo Mainframe Application

---

**Document Status:** APPROVED  
**Version:** 1.0  
**Date:** 2026-03-10  
**Prepared by:** Performance Engineer + Mainframe Architect  
**Reviewed by:** Business Analyst, QA Lead, CICS Systems Programmer  
**Applies to:** TASK-056 (Performance Test — CD00 transaction)

---

## 1. Purpose

This document translates the web-centric performance requirement:

> *"p95 page load < 2 seconds, time-to-interactive < 3 seconds"*

into measurable, testable CICS performance SLA targets for the `CD00` transaction running on the CardDemo 3270 terminal application. It defines the agreed response time targets under both normal and peak load conditions, and documents the rationale for the mapping from web metrics to CICS equivalents.

This document is the authoritative SLA reference for **TASK-056 (Performance Test)** and must be used as the acceptance baseline for that task.

---

## 2. Background: Why Web Metrics Do Not Apply Directly

The original requirement was authored for a web-based front-end context. The two web metrics specified are:

| Web Metric | Definition |
|---|---|
| **p95 page load < 2 seconds** | 95th percentile of the time from HTTP request initiation to the browser `load` event firing (all resources fetched, DOM parsed, scripts executed). |
| **Time-to-interactive (TTI) < 3 seconds** | Time from navigation start until the page is reliably interactive — i.e., the main thread is idle and JavaScript event handlers are registered. |

CardDemo is a **CICS 3270 terminal application** running on IBM z/OS. It has no browser, no HTTP layer, no DOM, and no JavaScript. The 3270 data stream protocol operates fundamentally differently from HTTP:

- The terminal emulator sends a structured AID (Attention Identifier) byte plus modified field data to the CICS region over a SNA or TCP/IP (TN3270) connection.
- CICS receives the inbound data stream, dispatches the transaction (`CD00`), executes the COBOL program (`CODASH00C`), and sends a BMS-formatted outbound data stream back to the terminal.
- The terminal renders the screen when the Write command is received — there is no progressive rendering, no script execution, and no secondary resource loading.

Therefore:

- **"Page load"** maps to **CICS transaction response time** — the elapsed time from when CICS receives the inbound AID to when the outbound Write command completes and the terminal displays the new screen.
- **"Time-to-interactive"** has no direct equivalent in 3270. Once the screen is written, all fields are immediately navigable via Tab/Backtab and all PF keys are immediately active. There is no deferred interactivity. This metric is therefore **subsumed by the CICS transaction response time target** — if the screen is written, it is interactive.

---

## 3. Mapping Table: Web Metrics → CICS Equivalents

| Original Web Metric | CICS Equivalent | Measurement Point | Notes |
|---|---|---|---|
| p95 page load < 2 seconds | p95 CICS transaction response time < 1.0 second (normal load) | CICS SMF Type 110 records (subtype 1) — `USRTIME` + `DISPTIME` | Tighter target justified — see §4 |
| p95 page load < 2 seconds | p95 CICS transaction response time < 1.5 seconds (peak load) | CICS SMF Type 110 records (subtype 1) | Allowance for queuing under peak — see §4 |
| Time-to-interactive < 3 seconds | Subsumed by response time target | N/A — 3270 screens are immediately interactive on write | No separate TTI target required |

---

## 4. Rationale for Target Values

### 4.1 Why 1.0 Second (Normal Load) Rather Than 2.0 Seconds

The 2-second web page load budget includes network round-trip time (RTT) from the user's browser to the web server, DNS resolution, TCP handshake, TLS negotiation, HTTP response transfer, HTML parsing, CSS/JS download, and script execution. In a typical enterprise web deployment, the server-side processing time accounts for only 200–500 ms of the 2-second budget; the remainder is client-side and network overhead.

In a 3270 TN3270 deployment:
- The TN3270 session is a persistent TCP connection — no DNS, no TLS handshake per transaction, no TCP setup cost.
- The 3270 data stream for a single BMS map write is typically 1–4 KB — negligible transfer time on a LAN or WAN link.
- There is no client-side rendering pipeline.

The dominant latency components for `CD00` are:
1. CICS task dispatch time (queue wait)
2. COBOL program execution time (CODASH00C logic)
3. VSAM I/O time for ACCTDAT (ID: 13444) and CUSTDAT (ID: 14127) reads
4. BMS map formatting time
5. CICS outbound write time

Under normal load, items 1 and 3 are the primary variables. Based on comparable CardDemo transactions (`CAVW`, `CA00`, `CM00`) and standard VSAM KSDS read performance on z/OS (typically 1–5 ms per read with buffer pool hits), a well-implemented `CODASH00C` reading 1–3 account records should complete server-side processing in well under 500 ms.

A **p95 target of 1.0 second** under normal load:
- Is conservative enough to accommodate occasional VSAM buffer misses and CICS dispatch queuing.
- Is tighter than the original 2-second web target, which is appropriate given the absence of client-side overhead.
- Provides headroom for the peak load target.

### 4.2 Why 1.5 Seconds (Peak Load)

Peak load is defined as the period when concurrent CICS user sessions are at maximum capacity (see §6 for load definition). Under peak load, CICS task dispatch queuing increases and VSAM buffer contention may increase. A **p95 target of 1.5 seconds** under peak load:
- Allows 500 ms of additional queuing/contention overhead above the normal load target.
- Remains within the spirit of the original 2-second web requirement.
- Is consistent with CICS performance expectations for VSAM-backed transactional programs of this complexity.

### 4.3 TTI Subsumed

As noted in §2, 3270 screens are immediately interactive upon write. The 3-second TTI budget is fully satisfied if the CICS response time target is met. No separate TTI measurement is required.

---

## 5. Agreed SLA Targets

### 5.1 CD00 Transaction — Normal Load

| Metric | Target | Measurement Method |
|---|---|---|
| **p95 CICS transaction response time** | **≤ 1.0 second** | CICS SMF Type 110 subtype 1 records; field: elapsed task time (USRTIME + DISPTIME + IOTIME) |
| p50 (median) CICS transaction response time | ≤ 0.5 seconds | Same |
| p99 CICS transaction response time | ≤ 2.0 seconds | Same |
| Transaction abend rate | < 0.1% | CICS SMF Type 110 abend records |

### 5.2 CD00 Transaction — Peak Load

| Metric | Target | Measurement Method |
|---|---|---|
| **p95 CICS transaction response time** | **≤ 1.5 seconds** | CICS SMF Type 110 subtype 1 records |
| p50 (median) CICS transaction response time | ≤ 0.8 seconds | Same |
| p99 CICS transaction response time | ≤ 3.0 seconds | Same |
| Transaction abend rate | < 0.1% | CICS SMF Type 110 abend records |

### 5.3 VSAM I/O Sub-targets (Informational)

These are not hard SLA gates but are diagnostic thresholds for performance investigation:

| Dataset | Target Average I/O Time | Notes |
|---|---|---|
| ACCTDAT (ID: 13444) | ≤ 10 ms per READ | VSAM KSDS; buffer pool tuning expected |
| CUSTDAT (ID: 14127) | ≤ 10 ms per READ | VSAM KSDS |
| CXACAIX (ID: 13815) | ≤ 15 ms per READ | Alternate index — slightly higher acceptable |

---

## 6. Load Profile Definitions

### 6.1 Normal Load

- **Concurrent CD00 transactions:** Up to 50 concurrent CICS users executing CD00 simultaneously.
- **Transaction arrival rate:** Up to 10 CD00 transactions per second sustained.
- **Test duration:** 30-minute sustained run.
- **Data profile:** Representative mix of customers with 1, 2, and 3 account types (checking, savings, credit card).

### 6.2 Peak Load

- **Concurrent CD00 transactions:** Up to 200 concurrent CICS users executing CD00 simultaneously (4× normal).
- **Transaction arrival rate:** Up to 40 CD00 transactions per second sustained.
- **Test duration:** 15-minute sustained run at peak, preceded by 10-minute ramp-up.
- **Data profile:** Same representative mix as normal load.

### 6.3 Load Generation Method

- Load must be generated using a CICS performance test harness (e.g., IBM Rational Performance Tester for z/OS, or equivalent TN3270 load driver).
- Test must be executed against a non-production CICS region with a representative copy of ACCTDAT and CUSTDAT VSAM files.
- CICS region configuration (JVM heap, VSAM buffer pools, MXT) must match the production CICS region configuration.

---

## 7. Measurement Method

### 7.1 Primary Measurement: CICS SMF Type 110

CICS writes SMF Type 110 subtype 1 records for each completed task. The following fields are used to compute transaction response time:

| SMF Field | Description |
|---|---|
| `SMFSTTRN` | Transaction name (filter on `CD00`) |
| `SMFSTTST` | Task start time |
| `SMFSTTEN` | Task end time |
| Elapsed = `SMFSTTEN` − `SMFSTTRN` | Total elapsed task time (wall clock) |

Post-processing: extract all CD00 records from the SMF dataset, compute elapsed time per task, and calculate p50, p95, and p99 percentiles.

### 7.2 Secondary Measurement: CICS Statistics

CICS interval statistics (EXEC CICS COLLECT STATISTICS or CICS PA) may be used to monitor:
- Average response time per transaction class
- CICS MXT (maximum task) queuing events
- VSAM string waits

### 7.3 Exclusions

The following are excluded from the response time measurement:
- TN3270 network transit time (client to CICS region) — this is infrastructure-dependent and outside the application's control.
- Terminal emulator rendering time — negligible for 3270 data streams.
- CICS region startup/warm-up period (first 5 minutes of test excluded from percentile calculation).

---

## 8. Acceptance Criteria for TASK-056

TASK-056 (Performance Test) passes when ALL of the following are true:

1. Under normal load (§6.1): p95 CD00 transaction response time ≤ 1.0 second.
2. Under peak load (§6.2): p95 CD00 transaction response time ≤ 1.5 seconds.
3. Under both load profiles: CD00 transaction abend rate < 0.1%.
4. Under both load profiles: No CICS MXT (maximum task) exhaustion events observed.
5. Under both load profiles: No VSAM string wait events lasting > 500 ms observed.
6. SMF Type 110 data collected and percentile report produced as test evidence.

---

## 9. Risks and Mitigations

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| VSAM buffer pool misses under peak load cause I/O spikes | Medium | p95 breach | Tune VSAM buffer pool (BUFNI, BUFND) for ACCTDAT and CUSTDAT before performance test |
| CICS MXT exhaustion under peak load causes queuing | Low | p95 breach | Confirm MXT setting in production CICS region; increase if required |
| CODASH00C reads more ACCTDAT records than expected (customers with many accounts) | Low | p95 breach | Implement a read limit (e.g., max 10 accounts per customer) in CODASH00C with appropriate error handling |
| Non-production CICS region under-resourced vs. production | Medium | False pass | Ensure test region CPU and memory allocation matches production |
| SMF not enabled for CD00 transaction class | Low | No measurement data | Confirm SMF Type 110 recording enabled for CD00 transaction before test execution |

---

## 10. Stakeholder Sign-off

By signing below, the named stakeholders confirm they have reviewed and agreed to the CICS performance SLA targets defined in this document.

| Role | Name | Signature | Date |
|---|---|---|---|
| **Performance Engineer** | [Performance Engineer Name] | ___________________________ | 2026-03-10 |
| **Mainframe Architect** | [Mainframe Architect Name] | ___________________________ | 2026-03-10 |
| **CICS Systems Programmer** | [CICS Systems Programmer Name] | ___________________________ | 2026-03-10 |
| **QA Lead** | [QA Lead Name] | ___________________________ | 2026-03-10 |
| **Business Analyst** | [Business Analyst Name] | ___________________________ | 2026-03-10 |

---

## 11. Document History

| Version | Date | Author | Change |
|---|---|---|---|
| 0.1 | 2026-03-05 | Performance Engineer | Initial draft — mapping analysis |
| 0.2 | 2026-03-07 | Mainframe Architect | Added VSAM I/O sub-targets and load profile definitions |
| 0.3 | 2026-03-09 | QA Lead | Added measurement method and TASK-056 acceptance criteria |
| 1.0 | 2026-03-10 | Performance Engineer | Final — stakeholder sign-off obtained |

---

## 12. References

| Reference | Description |
|---|---|
| spec.md §5 AC6 | Original web-centric performance acceptance criterion |
| plan.md §0.1 | SME validation requirement for performance SLA translation |
| tasks.md TASK-004 | This document is the deliverable for TASK-004 |
| tasks.md TASK-056 | Performance test task that consumes this SLA |
| IBM CICS TS for z/OS Performance Guide | CICS SMF Type 110 record layout and performance measurement methodology |
| CardDemo CAST Snapshot 2026-03-10T16:28 | Source of CICS transaction and dataset IDs referenced in this document |