# Purchase Order Process v3 — Gaps (User Flow & Business Flow)

This document lists gaps in `purchaseorder_process_v3.md` that would prevent or misdirect development of the PO system. Scope is **user flow and business flow only** — not technical/implementation details (data types, indexing, API shapes, etc.).

Each gap is a question that needs an answer before the corresponding behaviour can be built. Please answer inline under each item.

Legend:
- **[BLOCKER]** — without an answer, the feature cannot be built without guessing at business intent.
- **[CLARIFY]** — the spec implies one behaviour but is ambiguous; a confirmation is enough.

---

## 1. Scope of Access (Who Sees / Selects What)

### 1.1 [BLOCKER] Company / Buyer scope per user
Section B.1 says the Buyer is "Selected from companies under the organization." The spec nowhere defines whether a user sees **all** companies in the org or only a subset.

- Do users have a company/buyer-level access list (e.g., assigned companies)?
- If yes, how is it granted — via a permission, a role, or a direct user-to-company assignment?
- Does the same scope apply to the PO List (H), Dashboard (I, which says "organization-wide"), and creation (B.1)?

Answer: Users sees **all** companies in the organization. The same scope applies to the PO List, Dashboard, and creation


### 1.2 [BLOCKER] Facility scope per user
B.1 says the Facility List is "Drawn from facilities under the selected Buyer" and the Delivery Location "May be any facility under the selected Buyer." No facility-level access control is mentioned.

- Can every user see every facility under a Buyer they have access to, or is there a facility-level access list?
- Does the PO List (H) filter `Facility` by the user's facility scope, or show all facilities under accessible buyers?

Answer:


### 1.3 [CLARIFY] Dashboard scope vs. permission view scope
Section I says the dashboard is "organization-wide (not filtered by user's assigned companies/facilities)", but Section A defines per-permission view scopes that exclude certain statuses. When a user drills from a dashboard count into a list, the list is a subset of what the count represented (since the view scope filters states) — is that correct?

- Should the dashboard count itself be filtered by the user's permission view scope, or show the raw org-wide count (and then drilldown filters it)?
- If filtered, is there a scenario where a user sees a non-zero count but drills in and sees an empty list? How should that be presented?

Answer:


### 1.4 [BLOCKER] PO List scope when user has multiple permissions
A user may hold more than one permission (e.g., `purchaseorder_modify` + `purchaseorder_approve`). Each permission lists a different "View Scope (PO detail)" in Section A.1.

- Is the effective view scope the **union** of scopes from all held permissions?
- The PO List (H) is "available to users with any view-capable permission". Does the list show the union across those permissions, and does each row honour that union when the user clicks into the detail?

Answer:


### 1.5 [BLOCKER] Budgeted-PO visibility for non-budgeted roles
A.2 says "Once a PO becomes budgeted, only users with `purchaseorder_budgeted_modify` can view/edit it (users with `purchaseorder_view` can still view non-Draft states)."

- Does `purchaseorder_modify_cancel` (non-budgeted) still see Draft/Rejected budgeted POs they created before they became budgeted? (Spec says visibility is gated on the budgeted flag, not authorship.)
- If a Merchandiser with only `purchaseorder_modify` created a Draft and a colleague with `purchaseorder_budgeted_modify` later adds a budget item, the original creator loses access — is that the intended behaviour? How are they notified?
- What about `purchaseorder_approve` — can they approve budgeted POs without also holding `purchaseorder_budgeted_modify`? (C.2 lists "Approver permissions" as checked, but does not define what that means for budgeted POs.)

Answer:


### 1.6 [CLARIFY] Unit cost visibility for view-only users
B.4.2 says unit/extended cost is "Visible to any user with access to view/create/modify the PO." Confirm that `purchaseorder_view` users see costs (some organizations treat cost as confidential).

Answer:


---

## 2. Buyer-Seller Relationship (Foundational Data)

### 2.1 [BLOCKER] Definition of "active"
B.1 requires an "active Buyer-Seller relationship." The spec never defines what "active" means.

- Is it a simple enabled/disabled flag, or is it date-ranged (effective-from / effective-to)?
- When a relationship becomes inactive mid-lifecycle of a Draft PO, what happens to the Draft — blocked on save? On submit? Cancelled by the system?
- Is there a grace period, or immediate enforcement?

Answer:


### 2.2 [BLOCKER] What fields the Buyer-Seller relationship owns
The spec references the relationship as the source for "item costs, minimum order quantities (MOQ), order multiples, ordering schedules, and the UOM/SKU configuration per item."

- Is each item's MOQ / order multiple / schedule / cost configured **per Buyer-Seller relationship** or **per vendor** (i.e., same values used for all Buyers)?
- Can the same item appear in two Buyer-Seller relationships with different MOQ/cost? If yes, is it ever an issue that a single PO combines items from only one relationship (correct — PO is Buyer→Seller, so only one relationship). Confirm.
- Can an item be temporarily removed from a relationship? What state does that produce for in-flight Drafts referencing it? (Section B.6 covers clones, not Drafts.)

Answer:


### 2.3 [CLARIFY] Ordering schedule granularity
B.4.1 says the system "filters items based on the day-of-week schedule." B.1 references "item schedules" more broadly; C.2 validates "Item ordering schedule (day-of-week)."

- Is "day-of-week" the only schedule type, or are there also: specific-date schedules, seasonal blackouts, holidays, vendor-defined cutoffs (e.g., "order by 2pm for next-day")?
- Does the schedule include a time-of-day component (e.g., items must be ordered before 2pm the day before)?

Answer:


### 2.4 [BLOCKER] Currency of costs across companies
N says "single currency per organization." But Section B has Buyers that are **companies under the organization** and Sellers that are vendors. Can different companies within the org (or different vendors) transact in different currencies?

- Is all cost data stored and presented in the one organization currency, with vendor-side currency hidden?
- Or does the Buyer-Seller relationship have its own currency and we normalise to org currency at display time?
- For printed PO (PDF, M.1), which currency is shown to the vendor?

Answer:


---

## 3. Order Date, Time, and Thresholds

### 3.1 [BLOCKER] Granularity of Order Date
B.1 constraints: "Must be greater than the date the PO is approved." Is the comparison **date-only** or **date-and-time (timestamp)**?

- The phrase "greater than the date the PO is approved" suggests date-only, but "Minimum threshold between Approval date and Order Date is configurable per organization" suggests the threshold has a unit.
- What is the unit of the minimum threshold — days, hours, business days?
- If date-only, what time zone is the "today" boundary? organization time zone? UTC?

Answer:


### 3.2 [BLOCKER] Default values for configurable thresholds
Section N lists several thresholds as "configurable per organization" with no defaults. Development needs defaults for the admin UI and for orgs that haven't configured them yet.

Please state defaults for:
- Minimum threshold between Approval date and Order Date.
- Maximum allowed forward range for Order Date.
- Minimum gap between Order Date and Cancel if not Delivered by Date.
- User session idle timeout.

Answer:


### 3.3 [BLOCKER] Draft whose Order Date has passed
Nothing in the spec handles a Draft whose Order Date has slipped into the past (or into the min-threshold window from today).

- When a user reopens such a Draft, is the Order Date auto-cleared? Auto-bumped to a valid value? Flagged as a validation error?
- Does it block submission only, or also block save?
- Same question for Rejected POs that sat idle past their Order Date.

Answer:


### 3.4 [CLARIFY] Cancel-if-not-delivered action configuration
B.1 says the action-on-expiry (`Automatic Cancel` / `Notify Only` / `Do Nothing`) has `Notify Only` as default. Is this set **per PO** by the creator, or **per organization** by the admin, or both (admin default, overridable per PO)? The spec is ambiguous ("Configurable action on expiry").

Answer:


### 3.5 [CLARIFY] Post-approval editability of the cancel date action
B.1: "All date-sensitive values are locked after the PO is approved." Does the *action on expiry* of Cancel if not Delivered by Date also lock, or can a user with manual-close permission change `Notify Only` → `Do Nothing` post-approval?

Answer:


---

## 4. PO Lifecycle / State Edge Cases

### 4.1 [BLOCKER] Editable fields on Approved PO (non-date)
B says all date-sensitive values lock at approval. Nothing is said about non-date fields on an Approved PO (Notes, Attached Files, Delivery Location).

- Are **all** fields on an Approved PO read-only? (Spec implies yes through E.7 Irreversibility for Completed/Cancelled, but Approved is not called out.)
- Can Notes / Attached Files be added post-approval? If yes, by whom?
- Can Delivery Location be changed post-approval (e.g., vendor can't deliver to the intended facility)?

Answer:


### 4.2 [BLOCKER] Individual-item actions on Approved PO
E.3 lists 6 paths to Cancelled. None of them is "cancel a single item on an Approved PO without closing the whole PO." In practice, a vendor often short-supplies a single SKU and the buyer wants to zero-out the line while keeping the PO open.

- Is there no item-level cancellation on an Approved PO (must manual-close the whole PO)?
- If this is intentional, confirm — this is a major functional constraint.

Answer:


### 4.3 [BLOCKER] Role change during an in-flight PO
What happens when a user's permissions change mid-lifecycle?

- A user is editing a Draft and loses `purchaseorder_modify` — is the edit lock released immediately, their in-flight changes discarded, or do changes persist until save?
- A Draft exists that the original creator can no longer edit because someone added a budget item. What notification do they get? Does the Draft remain visible on their dashboard?
- A supervisor is mid-review (rejection marks placed) and loses `purchaseorder_approve` — are the marks preserved for the next supervisor?

Answer:


### 4.4 [CLARIFY] What counts as a "concurrent Draft"
B.9: warns on same Buyer + Seller + PO Type + Order Date.

- Is the check **across all users** in the org, or per-user only?
- Does the check include Rejected POs (which are effectively awaiting resubmission)?
- Does the warning link respect the viewing user's view scope (e.g., they may not have permission to see the existing Draft)?

Answer:


### 4.5 [BLOCKER] Document ID counter
B.3: `DFTPO-yyyyMMXXXXXX` and `PO-yyyyMMXXXXXX`. `XXXXXX` is not defined.

- Is `XXXXXX` a zero-padded 6-digit decimal (max 999,999 per month)?
- What happens if the monthly cap is hit?
- Does the DFTPO counter and the PO counter share a pool or are they separate? (Spec says "Separate counters reset monthly" — confirm whether DFTPO vs. PO are one of the "separate" axes.)
- When a Draft is submitted in a month different from its creation month, does the PO-ID use the submission month (yes per B.3) with that month's counter (i.e., does it consume a new counter value in the submission month)?

Answer:


### 4.6 [CLARIFY] Clone of mixed-permission PO
B.6: "Permission-gated: `purchaseorder_modify` clones non-budgeted POs; `purchaseorder_budgeted_modify` clones budgeted POs." A Completed/Cancelled PO may contain a mix.

- Can a user with only `purchaseorder_modify` even **see** the clone button for a budgeted PO? (If they can't see the PO at all per A.2, this is moot — confirm.)
- If a source PO had budget-category items that were cancelled/short-closed, is the clone still treated as budgeted (requiring the budgeted permission)?

Answer:


### 4.7 [BLOCKER] Auto-cancel of partially received Approved PO
B.1 and E.5: when Cancel-if-not-delivered-by date expires AND the PO is partially received AND action is `Automatic Cancel`, system falls back to `Notify Only`.

- Does this "fall back" happen only once (i.e., one notification), or daily until a user acts?
- Is there any timeout after which the system gives up and leaves the PO in Approved indefinitely?
- Does the dashboard item I.1.7 ("expired that are partially received") drive these notifications, or is it separate?

Answer:


### 4.8 [CLARIFY] Edit lock on browser close / refresh
B.7 says lock releases on navigation away, logout, or session expiry. What about:

- Browser tab/window close without logout?
- Browser refresh (F5) of the PO Details page?
- Opening the same PO in a second tab by the same user?

Answer:


---

## 5. Budget Interaction

### 5.1 [BLOCKER] Budget period boundary vs. Order Date
B.4.5: "The PO module reads the active budget as of the Order Date." Budget periods are defined externally.

- If the Order Date falls **between** two budget periods (gap), is the PO blocked?
- If the Order Date is **before** the first defined budget period, blocked?
- If an organization has no budget periods defined but has budget categories, what happens on save/submit/approve?

Answer:


### 5.2 [BLOCKER] Budget restoration on returns
F.4 says returns "do NOT alter PO item status or received quantities." But:

- Does a return restore budget that was consumed? (From a finance standpoint the money is coming back.)
- Or is budget consumption permanent once received, regardless of returns?

Answer:


### 5.3 [BLOCKER] Mid-lifecycle item re-categorisation
A product's category can be changed in the master data (today it's in Category A which has no budget; tomorrow it's moved to Category B which has a budget).

- If this happens while a Draft PO exists with that item, does the PO re-classify from non-budgeted to budgeted on next save/validation?
- What if an already-approved PO contains an item whose category is re-classified? Does anything happen? (The spec implies values lock at approval, so presumably no — confirm.)

Answer:


### 5.4 [CLARIFY] Budget consumption timing on auto-approved POs
Auto-approved POs (C.4) consume budget at approval — same as manually approved. Confirm that failed budget re-validation at auto-approval time **blocks submission** (since submission and approval are the same moment for auto-approval) — so the user sees a submit failure rather than a separate approval failure.

Answer:


### 5.5 [CLARIFY] Budget visibility to creator
The spec never mentions whether the creator (Merchandiser) sees the available budget balance while building a PO.

- Is available budget shown in the PO Items page as the user adds budget-category items (e.g., a remaining-budget widget)?
- Or is budget only surfaced as a pass/fail validation on Save/Submit?

Answer:


---

## 6. Approval Workflow

### 6.1 [BLOCKER] What "Approver permissions" means at C.2
C.2 lists "Approver permissions" as a validation check at approval time but doesn't define it.

- Does the approver need anything beyond `purchaseorder_approve`?
- Specifically for a budgeted PO: does the approver also need `purchaseorder_budgeted_modify` (or a hypothetical `purchaseorder_budgeted_approve` — not listed)?
- Currently only one approve permission is defined (A.1). Confirm a user with only `purchaseorder_approve` can approve any PO, budgeted or not.

Answer:


### 6.2 [CLARIFY] Self-approval audit
A.2 allows self-approval when the same role has both permissions. The activity log (K.2) logs approvals with actor.

- Is self-approval explicitly flagged (e.g., separate activity event "self-approved" or a visual marker) so audits can spot it?
- Is there any configuration to disallow self-approval at org level?

Answer:


### 6.3 [BLOCKER] Simultaneous approvers / notification after one acts
L.3: notifications go to all users with `purchaseorder_approve`.

- When one approver approves/rejects/cancels, are the pending notifications cleared / marked-read for everyone else?
- Does the real-time event push an update to other approvers currently viewing the same PO (read-only due to review lock)?

Answer:


### 6.4 [BLOCKER] Orphaned Submitted POs
If every user with `purchaseorder_approve` has their role removed (or the last approver leaves the company), Submitted POs become unreviewable.

- Is there any escalation, admin-reassign, or alert?
- Can the PO be auto-demoted back to Draft or cancelled?
- This may seem edge but is important for ops: what's the documented fallback?

Answer:


### 6.5 [CLARIFY] Review handoff after session expiry
C.3: review lock releases on session expiry. The spec says rejection marks are saved persistently as the supervisor reviews, so another supervisor can pick up where they left off. Confirm that:

- A second supervisor, opening the PO after the first's session expired, sees the first supervisor's in-progress marks **and can edit/remove them**.
- Activity log attributes who originally placed a mark vs. who submitted the final rejection.

Answer:


### 6.6 [CLARIFY] "Leave it as Submitted" visibility
C.1: supervisor may leave PO as Submitted. The spec says validation errors from the previous failed approval are retained.

- Is there any visible state on the PO that indicates "approval was attempted and failed" vs. "never attempted"? (For the Merchandiser and for other supervisors.)
- Does the Merchandiser get any notification that an approval attempt failed (e.g., so they know to address a data problem)?

Answer:


---

## 7. Rejection Workflow

### 7.1 [CLARIFY] Items remaining "Submitted" on a Rejected PO
D.2: "Items without rejection marks remain `Submitted`." Section J.3 item-level (with rejection) shows: `Submitted` → `Rejected` → `Submitted` → `Approved`.

- On a Rejected PO, is the non-rejected item's status the string `Submitted` (misleading when the PO is Rejected) or something different in the UI like "Pending re-submission"?
- On re-approval of the PO, do these items transition `Submitted` → `Approved` directly (no re-rejection), per J.3. Confirm.

Answer:


### 7.2 [BLOCKER] Rejection requires at least one mark or note
D.2: "At least one rejection mark and/or PO-level rejection note is required to formally submit a rejection."

- Can a supervisor submit a rejection with ONLY a PO-level rejection note (no item marks)? Spec says yes. What happens to items — all return to `Submitted`, none to `Rejected`? So the Merchandiser must re-examine without specific item guidance. Confirm intended.
- Can they submit with ONLY item-level marks (no PO-level note)? D.2 seems to allow this.

Answer:


### 7.3 [CLARIFY] Rejection note persistence vs. display
D.5: "All rejection notes on the PO are removed from the rejection-note display component. History is preserved in the activity log."

- On the Merchandiser's editing page of a Rejected PO, do they see the rejection notes to guide their edits?
- Once they resubmit, notes disappear from display but remain in activity log. Confirm.
- If the PO cycles rejection N times, the display only ever shows the latest cycle's notes? Confirm.

Answer:


---

## 8. Closing / Cancellation

### 8.1 [BLOCKER] Over-delivery handling for auto-completion
E.1: "total received quantities... equal the ordered quantity for every item. There is no tolerance threshold — exact equality is required."

- Is over-delivery possible from the Receiving module? If yes, does the PO auto-complete (exact equality fails), and if no auto-complete, what triggers completion — only manual close?
- Is under-delivery that sums to less than ordered expected to eventually reach equality via multiple RAs, or manual close? How long does the system wait?

Answer:


### 8.2 [BLOCKER] CD auto-completion criteria
E.1: "For Cross Dock Orders, receipt at the Delivery Location is sufficient to trigger auto-completion."

- Does this mean ALL items' aggregate qty received at the Delivery Location equals the ordered qty, or just that the Delivery Location receipt doesn't need to match per-facility allocations?
- What about per-facility quantities — are those just allocation hints that don't have to be satisfied at auto-completion time?

Answer:


### 8.3 [BLOCKER] Pending Receiving Advice definition for manual close
E.2: "Manual close is blocked if there are pending Receiving Advice documents linked to the PO. The definition of 'pending' is owned by the Receiving module."

- This is fine as an external contract, but what does the user see when they click Manual Close with pending RAs? A blocked message pointing to the RAs? Which RAs? A link to act on them?
- Does the user need any permission to act on the RAs, or can they always cancel them first and then close the PO?

Answer:


### 8.4 [CLARIFY] Cascade to pending RAs on cancellation
Four paths lead to PO Cancelled (from Draft, Rejected, Submitted, or manual close with zero receipt). A manual close with pending RAs is blocked (E.2). But:

- For Draft/Rejected/Submitted cancellation — can there even **be** a linked RA at this stage? (RAs are presumably created only against Approved POs.) Confirm.
- For system auto-cancel (E.5) when no items received but an RA has been *started* (e.g., an in-progress draft RA at the vendor side) — is that blocked by the same "pending RA" rule?

Answer:


### 8.5 [CLARIFY] System auto-cancel note content
E.5: "A fixed standard system-generated cancellation note is placed on the PO-level and on every cancelled item."

- What is the content of this standard note? (Dev needs a string.)
- Is it translated per org locale, or fixed in English?

Answer:


### 8.6 [CLARIFY] Path count typo in E.3
E.3 says "Four paths lead to a `Cancelled` status" but lists 6. Assuming 6 is correct, should the numbers be updated — or is the intent "4 user-triggered paths + 2 manual-close/system variants"? This is editorial but worth clearing up so I don't mis-model.

Answer:


---

## 9. Facility List / Delivery Location Interactions

### 9.1 [BLOCKER] CD aggregate MOQ when Delivery Location is also in Facility List
B.4.4: MOQ/order-multiple is enforced on "the **aggregate total across the Facility List** (excluding the Delivery Location if different), not per individual facility."

- "Excluding the Delivery Location if different" — if the Delivery Location IS one of the Facility List entries, is its quantity included in the aggregate, or also excluded?
- In pure CD (Delivery Location is a cross-dock not in the Facility List), the aggregate is clearly the sum of Facility List facilities. Confirm.

Answer:


### 9.2 [BLOCKER] Zero-quantity facilities at submit
B.1 says submission prompts removal of facilities with zero quantity across ALL items; declining blocks submission.

- Does "decline" mean the user actively says "keep them anyway"? If the intent is to force removal, what's the UX — a modal with a "Remove all" button?
- If a facility has non-zero on some items and zero on others, is that OK at submission? (Spec says yes: "A single facility may have zero quantity for some items while non-zero for others.") Confirm.

Answer:


### 9.3 [CLARIFY] CD per-facility allocation UX
B.4.2: per-facility quantity for CD. B.4.3: quantities displayed in company's SKU per facility, vendor UOM shown in aggregate.

- Does the user input per-facility quantities, and the total auto-sums? Or does the user input a total and allocate by percentage/split?
- If per-facility input, is there a helper to distribute evenly?

Answer:


### 9.4 [CLARIFY] Delivery Location change prompts (B.1)
B.1 describes Prompt 1 / Prompt 2 for Delivery Location changes when single-facility lists are involved.

- If the user declines Prompt 1 ("keep in sync"), the Delivery Location updates independently. Does this turn the PO from IR into CD (since Delivery Location now differs from the single Facility List entry)? The type transition matrix (B.2.1) says CD requires this structural condition.
- Does the system auto-switch the PO Type, or is there a separate prompt first?

Answer:


---

## 10. SOF-Specific Flows

### 10.1 [BLOCKER] Referenced Sales Order selection
B.1 references "Referenced Sales Order" as required for SOF, but nothing describes how it's selected.

- Is it a text-entry SO ID, a dropdown, a search/typeahead?
- Is there filtering — e.g., only SOs under the selected Buyer? Only SOs in certain status?
- Can the selected SO be one that's been fully fulfilled already (for a top-up)? Or only SOs with outstanding quantity?

Answer:


### 10.2 [BLOCKER] SOF fulfillment tracking against the source SO
B.2 notes "Multiple SOF POs may reference the same Sales Order."

- Does the PO module track cumulative fulfillment against the SO (e.g., "SO says 100 units; PO #1 ordered 40, PO #2 ordered 60")?
- If yes, where is it displayed (on the PO, on the SO, or both)?
- Does the PO enforce SO-remaining-quantity (can't order more than the SO's outstanding qty)?

Answer:


### 10.3 [CLARIFY] SO updated after SOF PO is created
The SO may be edited or cancelled while an SOF Draft exists.

- If the SO is cancelled, does the SOF PO block submission?
- If an item is removed from the SO, does it get flagged for removal on the Draft?
- If an SO quantity changes, does the Draft auto-update (Round Up / As Is modes)?

Answer:


### 10.4 [CLARIFY] SOF pre-population — budget-permission gap behaviour
B.2.2: "Items belonging to a category with a set budget are included in pre-population only if the PO creator holds `purchaseorder_budgeted_modify`. Otherwise they are included with a validation error flagging the permission gap."

This contradicts itself: "included only if" vs. "otherwise included with a validation error." Which is correct?

- Are items always included but flagged? Or excluded?
- If included with an error, the user cannot remove the item (they lack budgeted-modify). How do they proceed?

Answer:


---

## 11. Vendor Communication (Out of Scope?)

### 11.1 [BLOCKER] How does the vendor receive the PO?
The only vendor-facing artifact mentioned is M.1 (PDF export). The spec says nothing about:

- Email-to-vendor delivery on approval.
- EDI / API transmission.
- Portal for vendor to log in and acknowledge.

Please confirm:
- Is vendor communication entirely out of scope (the internal user is expected to email the PDF manually)?
- Or is there an implicit "send to vendor" action we need to design?

Answer:


### 11.2 [CLARIFY] PDF export trigger
M says users can export; it doesn't say when.

- Is PDF generation on-demand only (user clicks Export)?
- Is there any auto-generation on approval (e.g., saved as an attached file)?

Answer:


---

## 12. Notifications (Missing Events)

### 12.1 [BLOCKER] PO approved — not notified
L.3: "The Merchandiser is NOT notified when their PO is approved." Confirm this is intentional (some ERPs notify the creator so they can track). Dashboard item I.1.4 ("approved POs with no items received") partially covers it, but passive.

Answer:


### 12.2 [BLOCKER] PO cancelled — not in the events list
L.3 table has no entry for:

- Supervisor cancels a Submitted PO → creator notified?
- User with cancel permission cancels a Draft/Rejected PO they didn't create → original creator notified?
- System auto-cancels an Approved PO → manual-close-permission users notified? Creator notified?

Please state the expected notification behaviour for each cancellation path in E.3.

Answer:


### 12.3 [BLOCKER] PO auto-completed — not notified
Auto-completion (E.1) happens silently.

- Is the creator or anyone notified on completion?
- Or is this purely reactive (they check the dashboard / list)?

Answer:


### 12.4 [BLOCKER] Item short-closed — not notified
When manual close produces Short-Closed items, the creator/supervisor may want to know.

- Any notification? Any dashboard item? (I.1 has no "short closed" item.)

Answer:


---

## 13. UI Flow / Page-Level Ambiguities

### 13.1 [CLARIFY] Navigation-triggered save
B.4.6 and B.8: navigating away from PO Details "saves the Draft" and triggers deferred validation.

- Does this also apply to the PO Items page (navigating from PO Items to PO Details)?
- What if validation fails during that implicit save — does the user get blocked from navigating, or does the nav complete and errors surface on return?

Answer:


### 13.2 [CLARIFY] Auto-save semantics
B.4.6: "Auto-save does NOT trigger validation. Session expiry does not trigger auto-save."

- What IS auto-save triggered by? The spec mentions it exists but not when it fires.
- Is auto-save even a feature, or is the Draft only persisted on manual Save Draft / page nav?

Answer:


### 13.3 [CLARIFY] "Quick summary and next immediate action"
G.1: the view shows "next immediate action for the viewing user."

- Please enumerate the next-actions per (status × user's permissions) so we can build them. E.g., a supervisor viewing a Submitted PO sees "Review" / "Approve" / "Reject" / "Cancel"; a Merchandiser viewing a Rejected PO sees "Edit" / "Resubmit" / "Cancel"; etc.
- Or is this something the UX team specifies later?

Answer:


### 13.4 [CLARIFY] PO list pagination / default page size
H doesn't mention pagination. For an org with thousands of POs, this matters.

- Default page size? Sort options beyond "Created Date, latest first"?
- Infinite scroll or explicit pages?

Answer:


### 13.5 [CLARIFY] Export — single vs. bulk, PDF vs. CSV
M describes "PDF" and "CSV" formats but doesn't state:

- Is PDF always single-PO, CSV always bulk? Or can the user select either for either scope?
- Is there a "select multiple POs from the list and export" action?

Answer:


---

## 14. Attached Files

### 14.1 [BLOCKER] File constraints
F.1 references Attached Files. Nothing specifies:

- Allowed file types (PDF, images, XLSX, anything)?
- Size limits per file / per PO?
- Maximum number of files?
- Are they visible on the vendor PDF export, or only internally?

Answer:


### 14.2 [CLARIFY] Who can attach / remove files
- Can a user with only `purchaseorder_view` download attachments? (Assuming yes, since it's part of the view.)
- After approval, are attachments locked (per 4.1)? Or still addable/removable?
- After Cancelled/Completed, can anyone still download them?

Answer:


---

## 15. Miscellaneous

### 15.1 [CLARIFY] "Nearing expiration" digest details
B.1 / I.1.6: "nearing expiration" alert, default lead time 5 days.

- Is the 5-day threshold inclusive (alert when date ≤ today + 5)?
- Is the digest sent once per day, or per login regardless of frequency?
- Does it include POs already expired (partially received) or is that a separate dashboard item? (I.1.7 implies a separate item.)

Answer:


### 15.2 [CLARIFY] Activity log retention
K doesn't discuss retention.

- Is the activity log kept forever, or is there a retention period (e.g., 7 years)?
- Is it exportable? (M covers PO export, not activity log.)

Answer:


### 15.3 [CLARIFY] Cloned PO — ordering schedule revalidation scope
B.6: "Cloned items are immediately re-validated against the current Buyer-Seller relationship and schedule; items no longer sold by the vendor are included with validation errors rather than excluded."

- "No longer sold by the vendor" covers relationship removal. What about:
  - Item still sold but its category now has a budget (and cloner lacks `purchaseorder_budgeted_modify`)?
  - Item still sold but off-schedule for the new Order Date the cloner sets?
  - Item still sold but cost/MOQ/order-multiple changed — does the Draft silently adopt new values?
- Clarify the full clone-time revalidation rulebook.

Answer:


### 15.4 [CLARIFY] Rejected-cancel note requirement
E.3 path 2 ("Rejected cancel"): does the user need to provide a cancellation note, or is the rejection notes from the prior cycle sufficient? The spec says cancellation notes are required at PO-level when the whole PO is cancelled — so yes, a new cancellation note is required. Confirm.

Answer:


### 15.5 [CLARIFY] Ghost row rejection — undo shortcut visibility
D.4: "An optional 'Undo removal' shortcut on the ghost row may be provided."

- Is this optional UI ("may be provided") meant to be out of scope, or should we build it?

Answer:


### 15.6 [BLOCKER] What if the supervisor edits items during review?
D.1 describes adding/removing **rejection marks**. D.2 says items transition on formal submission. Nowhere does it say supervisors can edit item quantities or remove items directly.

- Confirm: a supervisor cannot edit items — they can only mark/unmark for rejection, and the Merchandiser must make any edits.
- This is important for scope; many ERPs allow supervisor line-editing. Please confirm it's out of scope here.

Answer:


### 15.7 [CLARIFY] Who can see activity log entries
K.3 covers in-progress review visibility. What about the general case:

- Anyone with access to view the PO sees the full activity log?
- Or are some entries restricted (e.g., `unit cost changed` only visible to modify-permission users)?

Answer:


---

## 16. Dashboard Counts — Permission Mapping

### 16.1 [BLOCKER] Which permission drives which dashboard item
Section A.1 defines Dashboard Scope per permission. Section I.1 lists 7 dashboard items. The mapping isn't complete:

| # | Dashboard Item | Which permission shows this? |
|---|----------------|------------------------------|
| 1 | POs not submitted (Drafts) | `purchaseorder_modify` / `_budgeted_modify` / cancel variants see Drafts — all? Just the creator's? |
| 2 | POs needing approval | `purchaseorder_approve` |
| 3 | Rejected POs needing revision | `_modify` / `_budgeted_modify`? Or also cancel / approve? |
| 4 | Approved, no items received | `_manual_close` / `_budgeted_manual_close`? |
| 5 | Approved, partially received | same as 4? |
| 6 | Approved nearing expiration | `_manual_close` / `_budgeted_manual_close` (per L.3) |
| 7 | Approved expired + partial receipt | same as 6? |

Please confirm or correct each mapping.

Answer:


### 16.2 [CLARIFY] Drafts dashboard — scope
If the Drafts count is shown to all users with `_modify`, does it show only Drafts the viewing user created, or every Draft in the org? A.1's "Dashboard Scope" row for `_modify` says "Draft, Rejected, Cancelled, Completed" but doesn't say "mine vs. all."

Answer:


---

## 17. Summary: Highest-Priority Blockers

If you can answer these first, it unblocks most of the build:

1. **Access scoping** — §1.1 (company/buyer scope), §1.2 (facility scope), §1.4 (union of permissions), §1.5 (budgeted view for non-budgeted roles).
2. **Buyer-Seller relationship model** — §2.1 (definition of active), §2.2 (what it owns), §2.4 (currency).
3. **Order Date semantics** — §3.1 (granularity & time zone), §3.2 (default threshold values), §3.3 (stale-Draft handling).
4. **Individual-item cancellation on Approved PO** — §4.2.
5. **Role-change mid-lifecycle** — §4.3.
6. **Budget boundary & restoration** — §5.1, §5.2.
7. **Approver permission for budgeted POs** — §6.1.
8. **Orphaned Submitted POs** — §6.4.
9. **Vendor communication scope** — §11.1.
10. **Notification events for cancellation / completion / short-close** — §12.2–§12.4.
11. **SOF fulfillment tracking against SO** — §10.1, §10.2.
12. **Attached file constraints** — §14.1.
13. **Dashboard permission mapping** — §16.1.
14. **CD aggregate when Delivery Location is in Facility List** — §9.1.
15. **SOF pre-population contradiction** — §10.4.
