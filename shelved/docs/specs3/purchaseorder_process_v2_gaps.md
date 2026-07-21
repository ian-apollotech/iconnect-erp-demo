# Purchase Order Process v2 — Open Gaps

Working document for pending clarifications. Answer any or all gaps at once.
Full Q&A history: `purchaseorder_process_v2_questions.md` (archive, Q1–Q147)
Source spec: `purchaseorder_process_v2.md`

---

## How to use this file

- Answer gaps inline below each item (replace `ANSWER:` placeholder)
- Multiple gaps in the same category can be answered together
- Resolved gaps are moved to the Resolved section at the bottom

---

## Open Gaps

*No open gaps. Spec fully clarified as of Q147.*

---

## Resolved (all rounds)

**R1** — Supervisor can mark ghost rows (removed items) as rejected with per-item rejection notes.
**R1a** — Merchandiser re-adds via normal add-item flow; optional "Undo removal" shortcut on ghost row in Rejected PO.
**R2** — Two highlight types (changed-field vs. validation error) are visually distinct; combined in one tooltip.
**R3** — Item statuses remain `Submitted` throughout in-progress review; change to `Rejected` only on formal rejection submission.
**R4** — Edit lock applies to Rejected POs during Merchandiser revision, same as Draft POs.
**C1** — Auto-cancel system note is a fixed standard message.
**C2** — `Automatic Cancel` on partially received PO falls back to `Notify Only`.
**C2a** — `Do Nothing` stays `Do Nothing` for partial POs; fallback only applies to `Automatic Cancel` config.
**S1** — SOF pre-population always uses original full SO quantity (not remaining unfulfilled).
**M1** — Facility may have zero quantity for some items; removal prompt only fires when ALL items are zero.
**F1** — Returns Announcements handled by separate Returns module; only link appears on PO.
