# Purchase Order Process — Clarification Questions

## 1. Actors & Authorization

- Can a single user hold multiple roles simultaneously (e.g., a user who is both a Merchandiser and a High Value Merchandiser)? If so, what governs which role is active?
- Is "High Value Merchandiser" a separate role or an additional permission granted to a regular Merchandiser?
- Who can grant the "authorized" status for High Value Merchandisers per category? Is this managed in a separate configuration module?
- Can the Merchandise Supervisor also create purchase orders, or is that strictly the Merchandiser's domain?
- Are there viewer-only roles (e.g., accounting, logistics) who can see purchase orders but not act on them?

### ANSWERS:

Each actions are guarded by a permission. The system admin will assign what permissions a role will have. This will make the roles of the actors be defined by the system instead of being hardcoded.
Ignore this process for now and focus more on the processes. Just know that permissions will be checked when indicated by an action done by a different actor,

---

## 2. Buyer / Seller / Facility Relationships

- Can a single purchase order have only one Buyer and only one Seller, or can it span multiple sellers?
**ANSWER** : only set for one Buyer (Company) to one Seller (Vendor).
- For the Facility List: is there a maximum number of facilities per order? Can an order be created for all facilities under a company at once?
**ANSWER** : can be assigned to unlimited Facilities so long as the facility is under the Company set as the Buyer.
- When multiple facilities are listed, is the Delivery Location one single location for all, or can each facility have its own delivery location?
**ANSWER** : always only one location. Having multiple Facility automatically sets Purchase Order type to `Cross Dock Order`.
- What happens if a Buyer-Seller relationship is deactivated or expired after a purchase order is submitted but before it is approved?
**ANSWER** : Buyer-Seller relationship used during the approval of the purchase order should be active. System should highlight this issue and force the approver to reject the order and redo it based on the updated Buyer-Seller relationship.

---

## 3. Order Date & Scheduling

- The Order Date drives "budget, accounting and effectivity" — what specifically does effectivity mean here? Does it determine which vendor price/terms are applied?
**ANSWER** : Order Date will be the date used to fetch different data related to the items that are date sensitive like price, cost, Buyer-Seller relationship, etc.
- Item filtering is based on the schedule set by the vendor — what does this schedule look like? Is it a day-of-week, a date range, a frequency, or a specific order window (e.g., order only on Mondays before 12PM)?
**ANSWER** : day-of-week an item can be ordered.
- If an item's schedule changes after a draft PO is saved, does the system re-validate on submission?
**ANSWER** : Buyer-Seller relationship is validated during saving to draft, on submission and on approval. System should highlight the error so that the user can do the necessary changes like removing the item not scheduled to be ordered.

Buyer-Seller relationship  used during the approval of the purchase order should be active. System should highlight this issue and force the approver to reject the order and redo it based on the updated Buyer-Seller relationship.
- Is the Cancel if not Delivered by Date purely informational, or does it trigger an automated workflow (e.g., notify vendor, notify supervisor)?
**ANSWER** : Make action done with `Cancel if not Delivered by Date` a configurable. It can be `Automatic Cancel`, `Notify Only`, `Do Nothing`. Default is `Notify Only`.

---

## 4. Item Addition & Quantity Rules

- When adding items by category (bulk add), does the system add all items in the category or does the user still select from a filtered list?
**ANSWER** : The user still select from a filtered list when adding items by category (bulk add).
- Order Multiple rule: if the user enters a quantity that is not a valid multiple, does the system auto-round up, auto-round down, or block and show an error?
**ANSWER** : The system blocks the action and highlights an error with a suggested correct round up value.
- If an item belongs to multiple categories, which category's budget applies? Or does each category budget apply independently?
**ANSWER** : The item should not contain in the same category. This should be managed during the master data encoding. If it does inspite of the corrections, then each category budget will apply independently.
- For a multi-facility order with per-facility quantities — is the minimum order quantity enforced per facility or on the aggregate total quantity across all facilities?
**ANSWER** : applied on the aggegated total ordered for the item and not on individual facility order quantity.
- Is the order multiple enforced per facility or on the aggregate?
**ANSWER** : applied on the aggegated total ordered for the item and not on individual facility order quantity.
- When an item is added and there is no stock at the vendor, should the system warn, block, or allow the order regardless?
**ANSWER** : The ERP has no visibility on the inventory of the Seller. This is outside of the scope.
- Can a line item be removed from the purchase order after it has been added? Up to which status is removal allowed?
**ANSWER** : Yes items can be added and removed until the order had been submitted for approval.
- Is the unit price locked at the time of PO creation, or can it change up to submission or approval?
**ANSWER** : all date sensitive data are based on Order Date. Values are locked after approval.

---

## 5. Budget

- Is the budget defined per category + Buyer (company) combination, or is it global per category regardless of Buyer?
**ANSWER** : Per company per category. Budget is managed in a different module outside of Purchase Order scope. 
- Is the budget a periodic budget (e.g., monthly, quarterly)? If so, how is the period determined — by Order Date?
**ANSWER** : Order Date is used to identify what period it belongs to. Budget is managed in a different module outside of Purchase Order scope. 
- Is the budget consumed at the point of PO creation, submission, or approval? What happens to the consumed budget if a PO is rejected or cancelled?
**ANSWER** : Budget is only consumed after approval.
- If a PO is submitted and pending approval, is the budget already reserved so another PO cannot exceed it?
**ANSWER** : No. Budget is only locked in after it is approved.
- Who manages and configures budgets — is this in scope or handled by a separate module?
**ANSWER** : Budget is managed in a different module outside of Purchase Order scope. 

---

## 6. Status & Workflow

- The spec implies the flow: Draft → Submitted → Approved/Rejected → Completed. Are there additional statuses needed, such as:
  - `Partially Received`
  - `Closed` (distinct from Completed)
  - `Voided` (distinct from Cancelled)
**ANSWER** : None
- Can a Cancelled or Rejected PO be reopened or cloned? If cloned, does it start as a new Draft?
**ANSWER** : Rejected can be edited again by the merchandiser and resubmited. Cancelled and Completed can be cloned.
- After rejection, the Merchandiser makes changes and resubmits — does it go to the same supervisor or any available supervisor?
**ANSWER** : any available supervisor
- Is there a limit on how many times a PO can be rejected and resubmitted?
**ANSWER** : no limit
- When the system auto-approves (approval not required), does it still create an approval audit record?
**ANSWER** : yes with indication that the system auto approved it

---

## 7. Completing the Purchase Order

- When a Merchandiser manually marks a PO as completed with unreceived items, what happens to those items — are they automatically cancelled, back-ordered, or left open?
**ANSWER** : possible status are `completed` (all received), `short_closed` (partial received), `cancelled` (not received)
- Does manually completing a PO with budget-category items release or retain the remaining budget for the unreceived portion?
**ANSWER** : Releasing remaining budget for unreceived portion
- Can a Completed PO be reopened? Under what circumstances?
**ANSWER** : No.

---

## 8. Cross Dock & Sales Order Fulfillment

- For Cross Dock Orders: which facility physically receives the goods? Is it always the Delivery Location, and then the system generates dispatch instructions to the other facilities?
**ANSWER** : Deliver always based on set Delivery Location
- For Sales Order Fulfillment: if the referenced Sales Order is cancelled or modified after the PO is created, what happens to the PO?
**ANSWER** : revalidate only during submission and approval. Changes in SalesOrder should automatically reject the PO and identify the lines on the items that had been modified so that it can be ordered.
- Can one PO reference multiple Sales Orders, or is it always a 1:1 relationship?
**ANSWER** : always only for single Sales Order
- For Sales Order Fulfillment, can items be added that are not in the Sales Order (e.g., bonus or free goods from the vendor)?
**ANSWER** : No

---

## 9. Document ID

- What format is the human-readable Document ID? Is there a configurable prefix, numbering scheme, or year/month component (e.g., `PO-2026-04-00001`)?
**ANSWER** : use pattern `PO-'yyyyMM'XXXXXX` for submitted PO. use pattern `DFTPO-'yyyyMM'XXXXXX` for created PO not yet submitted.
- Is the Document ID generated at Draft save or only at Submission?
**ANSWER** : use pattern `PO-'yyyyMM'XXXXXX` for submitted PO. use pattern `DFTPO-'yyyyMM'XXXXXX` for created PO not yet submitted.

---

## 10. Receiving & Returns

- The spec references Receiving Advice and Returns Announcement documents but does not define them here. Are these managed by separate modules?
**ANSWER** : managed by separate modules
- Can multiple Receiving Advice documents exist for a single PO (i.e., partial deliveries on different dates)?
**ANSWER** : yes
- When a return is processed, does it affect the "all items received" trigger for auto-completion?
**ANSWER** : no return process does not affect the status of the order

---

## 11. Activity Log

- What events are captured in the activity log — status changes only, or also field edits, quantity changes, item additions and removals?
**ANSWER** : indication of field changes (top level only), and status
- Are the Supervisor's rejection notes stored in the activity log or tracked separately?
**ANSWER** : stored in the activity log

---

## 12. Notifications & Alerts

- Does the system notify actors when the PO moves between statuses (e.g., notify Supervisor on submission, notify Merchandiser on rejection)?
**ANSWER** : yes. visible as a dashboard
- Is vendor notification in scope, or is vendor communication handled outside the system?
**ANSWER** : managed by separate modules
- Is there an alert when the Cancel if not Delivered by Date is approaching or is reached?
**ANSWER** : yes

---

## 13. Follow-up Questions (Round 2)

### 13.1 Status Lifecycle

- Section 6 answer states no additional statuses are needed, but Section 7 introduces `short_closed` as a distinct outcome when a PO is manually completed with partial receipts. Should `short_closed` be added as a formal PO status alongside `completed` and `cancelled`?
**ANSWER** : PO status are only `Draft`, `Submitted`, `Approved`, `Rejected`, `Completed`, `Cancelled`. PO items are `Draft`, `Submitted`, `Approved`, `Rejected`, `Completed`, `Short Closed`, `Cancelled`.
- Full status list implied so far: `Draft`, `Submitted`, `Approved`, `Rejected`, `Completed`, `Short Closed`, `Cancelled`. Is this the complete and correct set?
**ANSWER** : PO status are only `Draft`, `Submitted`, `Approved`, `Rejected`, `Completed`, `Cancelled`. PO items are `Draft`, `Submitted`, `Approved`, `Rejected`, `Completed`, `Short Closed`, `Cancelled`.
- Can a Merchandiser cancel a `Rejected` PO, or are they required to either edit-and-resubmit or wait for the Supervisor to cancel it?
**ANSWER** : yes Merchandiser cancel a `Rejected` PO

### 13.2 Clone Behavior

- When cloning a `Cancelled` or `Completed` PO, which fields are copied — all header fields and all line items?
**ANSWER** : All headers except `Order Date` and `Cancel if not Delivered by Date`. All items and quantities. PO of type `Sales Order Fulfillment` cannot be cloned.
- Does the cloned PO default the Order Date to today or carry over the original Order Date?
**ANSWER** : `Order Date` and `Cancel if not Delivered by Date` are manually set by the user.
- Are line items in a cloned PO re-validated against the current schedule (day-of-week) and current Buyer-Seller relationship at the time of cloning?
**ANSWER** : yes

### 13.3 Budget Enforcement Timing

- Budget is consumed only after approval. Is there any budget check or warning shown to the Merchandiser at submission time so they know the PO would exceed the budget before it reaches the Supervisor?
**ANSWER** : budget will already be validated if it exceeded on submission
- When an approved PO is `cancelled` entirely (not short-closed), is the full consumed budget released back?
**ANSWER** : yes

### 13.4 Schedule Re-validation on Submission

- The answer for "if an item's schedule changes after a draft PO is saved, does the system re-validate on submission?" appears to have received the Buyer-Seller expiry answer by mistake. What is the actual behavior — is the item's day-of-week schedule re-checked against the Order Date at submission time? If an item is no longer orderable on that day, is the submission blocked or does the item get flagged?
**ANSWER** : Buyer-Seller relationship is validated during saving to draft, on submission and on approval. System should highlight the error so that the user can do the necessary changes like removing the item not scheduled to be ordered.

### 13.5 Document ID for Drafts

- Document ID is generated only at submission. What identifier (if any) does a Draft PO display in the UI and in listings before it has a Document ID?
**ANSWER** : use pattern `PO-'yyyyMM'XXXXXX` for submitted PO. use pattern `DFTPO-'yyyyMM'XXXXXX` for created PO not yet submitted.

### 13.6 Document ID Format

- The format `PO-'yyyyMM'XXXXXX` — is the 6-digit sequence global across all companies, or is it scoped per company? Does it reset each month?
**ANSWER** : global across all companies of the same organization. reset counter each month

### 13.7 Cross Dock Dispatch

- For `Cross Dock Orders`, goods arrive at the single Delivery Location. Does the system then auto-generate any dispatch or transfer documents to route inventory to the other facilities in the Facility List, or is that handled by a separate module?
**ANSWER** : yes auto generate of dispatch to each facilities. but this is hanled in separate module

### 13.8 Sales Order Fulfillment — Auto-Rejection Triggers

- When a referenced Sales Order is modified, the PO is automatically rejected. What specific changes to the Sales Order trigger this auto-rejection (e.g., quantity reduction, item removal, SO cancellation, SO completion)?
**ANSWER** : SO cancelation, SO item removal and new quantity less than ordered quantity
- When a PO is auto-rejected due to Sales Order changes, is the Merchandiser notified via the same dashboard notification mechanism?
**ANSWER** : yes

### 13.9 Auto-Completion Trigger

- Section D.1 states the system auto-marks a PO as completed when all items are received. Does the system determine this by comparing total received quantities (summed across all Receiving Advice documents) against the total ordered quantity per item?
**ANSWER** : yes
- For a multi-facility Cross Dock Order, must all facilities confirm receipt, or is receipt at the Delivery Location sufficient to trigger completion?
**ANSWER** : receipt at the Delivery Location sufficient to trigger completion

### 13.10 Rejection Notes Visibility

- Rejection notes are tracked separately from the activity log. Are they visible in the PO View (Section F)? If so, where — as a dedicated rejection history section, or only on the most recent rejection?
**ANSWER** : Display the rejection notes for the most recent. Rejection notes can be viewed when expanding activity logs. 

### 13.11 Approaching Delivery Date Alert

- The alert for `Cancel if not Delivered by Date` — how far in advance is the alert triggered? Is the lead time configurable?
**ANSWER** : yes configurable

### 13.12 Activity Log Scope

- "Top level only" field change tracking — does this mean changes to individual line item quantities, item additions, and item removals are NOT recorded in the activity log?
**ANSWER** : quick view shows a short summary of what activity was done. expanding can show a more detailed description of the activity.
- If line item changes are not logged, is there any way to audit what items or quantities changed between a rejection and resubmission?
**ANSWER** : line item changes are logged in activity log

---

## 14. Follow-up Questions (Round 3)

### 14.1 Validation Behavior — Draft Save

- When saving to draft fails validation (e.g., an item is not schedulable on the Order Date's day-of-week, or the Buyer-Seller relationship is inactive), is the draft save **blocked entirely**, or is the draft **saved with errors highlighted** for the user to resolve later?
**ANSWER** : saved with errors highlighted for the user to resolve later. an activity logs is also created when validation fails.

### 14.2 Validation Behavior — Submission Blocking vs. Warning

- When submission validation fails (invalid schedule, expired Buyer-Seller relationship, or budget exceeded), is submission **hard-blocked** until all errors are resolved, or does the system show warnings and allow the user to confirm and proceed?
**ANSWER** : submission is blocked until all errors are resolved. but changes are saved with errors highlighted for the user to resolve later. an activity logs is also created when validation fails.

### 14.3 Budget Exceeded at Submission — Block or Warn?

- The answer says "budget will already be validated if it exceeded on submission." Does this **block** the submission, or is it a warning the Merchandiser can override? Clarifying because budget is not consumed until approval — so a budget warning at submission may need a different treatment than a hard rule violation.
**ANSWER** : submission is blocked but changes are saved with errors highlighted for the user to resolve later. an activity logs is also created when validation fails.

### 14.4 Budget Re-validation at Approval

- Between submission and approval, another PO may be approved that consumes part of the same category budget. Is the budget re-validated at the time of approval against the then-current available budget?
**ANSWER** : consuming of budget is a first come first served basis. budget must be re-validated at the time of approval against the then-current available budget. an activity logs is also created when validation fails.

### 14.5 Order Date Changes After Draft Creation

- Can the Order Date be changed after a draft PO has been saved? If yes, does the system automatically re-fetch all date-sensitive data (prices, terms, Buyer-Seller relationship, item schedules) when the Order Date is changed?
**ANSWER** : yes Order Date be changed after a draft PO has been saved. the system automatically re-fetch all date-sensitive data and be validated again for the changes.

### 14.6 Cancel if not Delivered by Date — Notification Target

- For the `Notify Only` action: who is notified — the Merchandiser who created the PO, the Supervisor, or both?
**ANSWER** : all who can view or access the PO.
- For the `Automatic Cancel` action: does it cancel the entire PO, or only the unreceived line items? Is this triggered by a scheduled system job running daily?
**ANSWER** : `Automatic Cancel` cancel the entire PO only of none of the items have been received. Partially received PO will not be affected by the expiration of `Cancel if not Delivered by Date`. This task is part of the list of task to be done on an end of the day. (separate module)

### 14.7 Sales Order Fulfillment — Auto-Rejection Timing

- Section 8 says "revalidate only during submission and approval." Section 13.8 says "when a referenced Sales Order is modified, the PO is automatically rejected." These appear to conflict. Is the SO change check:
  - **Real-time**: The moment an SO is cancelled/modified, any linked Draft/Submitted POs are immediately auto-rejected, or
  - **On-demand**: Only checked when the Merchandiser submits or the Supervisor approves?
**ANSWER** : Only checked when the Merchandiser submits or the Supervisor approves. as a precaution, SO with approved PO should not be allowed to be modified (to be handled in SO module).

### 14.8 Item Status Lifecycle

- When the PO changes status (Submitted, Approved, Rejected), do all line item statuses automatically mirror the PO status?
**ANSWER** : yes for Submitted, Approved status. rejected status, for items with no issue, will remain as submitted.
- When a PO is manually marked as completed, are item statuses (`Completed`, `Short Closed`, `Cancelled`) **automatically determined** by comparing ordered qty vs. total received qty from all linked Receiving Advice documents — or does the Merchandiser set each item's status manually?
**ANSWER** : no. the system determines if the item was fully received or partialy received based on the linked receiving advice documents for the PO

### 14.9 PO Status on Manual Completion with Mixed Item Outcomes

- When a Merchandiser manually completes a PO that has a mix of fully received, partially received, and unreceived items, the PO-level status becomes `Completed`. Is this correct? Does the PO View clearly indicate that it contains short-closed or cancelled items to avoid confusion?
**ANSWER** : no. PO maked as completed would indicate that the user will no longer need to do further action on to the PO even for those that contains short-closed or cancelled items.

### 14.10 Document ID Sequencing

- Do `DFTPO-` (draft) and `PO-` (submitted) sequences use **separate counters** or the **same counter**?
**ANSWER** : separate counters
- When a Draft (`DFTPO-202604000001`) is submitted and gets a `PO-` number, is there any retained link between the two IDs (e.g., shown in the activity log), or does the draft ID simply disappear?
**ANSWER** : draft ID simply disappear, replaced by the new PO ID.

### 14.11 Dashboard Notifications

- Are dashboard notifications **persistent** until the user acts on them (e.g., they stay until the Supervisor approves/rejects), or do they auto-dismiss after a set time?
**ANSWER** : persistent until the user acts on them
- Is there a notification count/badge visible from other parts of the system, or is it only visible when the user is on the dashboard?
**ANSWER** : only visible when the user is on the dashboard

### 14.12 Approaching Delivery Date Alert — Default Lead Time

- The alert lead time for `Cancel if not Delivered by Date` is configurable. What is the **default** lead time before the date when the alert is first triggered?
**ANSWER** :  5 days

---

## 15. Follow-up Questions (Round 4)

### 15.1 Item Status on Supervisor-Judgment Rejection

- Section 14.8 states that on rejection, "items with no issue will remain as Submitted." This implies that item status changes to `Rejected` only when a **system validation** failure flags an item.
**ANSWER** : yes
- If a Supervisor rejects the PO based on **their own judgment** (not a system-flagged error, e.g., they simply disagree with the quantities), what happens to item statuses — do all items remain `Submitted`, or do they all become `Rejected`?
**ANSWER** : items where the supervisor **does not agree** can be marked as rejected by the supervisor. all other items remain as submitted.

### 15.2 Partial Approval / Rejection by Item

- Can a Supervisor **approve some items and reject others** within a single PO, or is approval/rejection always an all-or-nothing action on the entire PO?
**ANSWER** : Supervisor can approve some items and reject others within a single PO. items where the supervisor **does not agree** can be marked as rejected by the supervisor. all other items remain as submitted.

### 15.3 Approval Validation Failure — Supervisor Action

- When system validation fails at approval time (e.g., Buyer-Seller expired, item schedule invalid), is the Supervisor **completely blocked** from approving, or can they override and approve anyway?
**ANSWER** : Flow will be as follow: 1. Supervisor approves a PO not aware of the error because of data issue like item schedule invalid. 2. System validate and finds the error. 3. System returns the view back to the Supervisor with an error and highlights the item with an error with a note descrbing the error. 4. Supervisor can either do nothing to the PO leaving the PO as submitted (maybe resolve the issue elsewhere by updating the master data to correct the issue and try to approve the PO again), or reject the PO with the items with error maked as rejected with the rejection note from the error message made by the system.
- If only **some items** have validation errors while others are clean, is the Supervisor blocked from approving the entire PO, or only from approving the flagged items?
**ANSWER** : Supervisor blocked from approving the entire PO if there are validation errors. All validation errors must be resolved first before a PO can be approved.

### 15.4 Budget Validation Failure at Approval

- Section 14.4 states budget is re-validated at approval on a first-come-first-served basis. If the budget is now insufficient at approval time, is the Supervisor **blocked from approving** (forced to reject), or is it a warning they can override?
**ANSWER** : Supervisor is blocked from approving. They can reject the PO so that the merchandiser can modify the PO to fit the budget, or do nothing for the PO leaving the PO as submitted (maybe request for the budget to be increased to satisfy the required budget for the PO and reapprove it once the budget has been adjusted)

### 15.5 Item Status During Active Receiving

- Between PO approval and completion, while Receiving Advice documents are progressively being created, what is the **item-level status**? Do items remain `Approved` throughout until the PO is explicitly completed or auto-completed?
**ANSWER** : yes

### 15.6 Partially Received PO — Unreceived Items at Expiry

- Section 14.6 states that a **partially received PO is not cancelled** when `Cancel if not Delivered by Date` expires. What happens to the **unreceived items** in that PO — do they remain open indefinitely, or does the system flag them in any way?
**ANSWER** : do nothing when `Cancel if not Delivered by Date` expires. the next action the user can do for these is to manually complete the order to update the status of the items accordingly based on the number of received quantities for the item.

### 15.7 Minimum Order Quantity — Validation Behavior

- Section 4 covers the Order Multiple error (block + suggest round-up). Is the **Minimum Order Quantity** violation handled the same way — block the action with an error message showing the minimum required quantity?
**ANSWER** : yes

### 15.8 Clone — User Experience on Validation Failure

- Section 13.2 states that cloned PO items are re-validated. If some items fail validation after cloning (e.g., item no longer schedulable on the new Order Date), is the cloned PO still saved as a Draft with errors highlighted (consistent with Section 14.1), or is cloning blocked until the user sets a valid Order Date first?
**ANSWER** : cloned PO still saved as a Draft with errors highlighted

---

## 16. Follow-up Questions (Round 5)

### 16.1 PO Header Status After Partial Item Rejection

- Section 15.2 introduces per-item approval/rejection. Rejected items become `Rejected`; remaining items stay `Submitted`. But Section 13.1 defines PO-level statuses as only: `Draft`, `Submitted`, `Approved`, `Rejected`, `Completed`, `Cancelled` — there is no "Partially Approved" or "Partially Rejected" status.
**ANSWER** : PO with items that are marked as `rejected` will have status of `rejected`.
- When a Supervisor rejects **some but not all** items, what is the **PO header status** — does it become `Rejected`, or does it remain `Submitted`?
**ANSWER** : `Rejected`

### 16.2 Approved Items After Partial Review

- Section 15.2 says after partial rejection: "items where the supervisor does not agree can be marked as `Rejected`. all other items remain as `Submitted`."
**ANSWER** : To clarify, partial rejection can happen either from the supervisor receiving errors from system identifying issues on some items after validation, or the supervisor making a judgement call and not agreeing with the items being ordered. For both cases, those items will be marked as rejected. all other items remain as submitted.
- This means even items the Supervisor explicitly agrees with stay as `Submitted`. Do items the Supervisor agrees with ever reach an `Approved` item status, or do they only move from `Submitted` → `Completed`/`Short Closed`/`Cancelled` at the end of the PO lifecycle — skipping `Approved` at the item level entirely?
**ANSWER** : Wrong. The supervisor marks an item with issues (judgement call or system validation error) as rejected so that the merchandiser can do changes on the item to fix the issue. for this case, the status journey for that item would be `Submitted` → `Rejected` → `Submitted` → `Approved` → `Completed`/`Short Closed`/`Cancelled`. On the otherhand, items that have no issue, but the PO was rejected either because of other items being rejected, or the PO itself was rejected, the status journey for that item would be `Submitted` → `Submitted` (remains as submitted while PO is rejected) → `Submitted` (PO was resubmitted) → `Approved` → `Completed`/`Short Closed`/`Cancelled`.

### 16.3 Approval UX — PO-Level Action vs. Item-by-Item

- Section 15.3 describes the supervisor clicking "approve a PO" (a single PO-level action) which then triggers system validation.
**ANSWER** : correct
- Section 15.2 describes the supervisor selectively approving/rejecting individual items.
**ANSWER** : correct
- Are these two separate UI actions: (a) a **per-item approve/reject** interface where the supervisor acts on each line, and (b) a final **"Submit Approval"** button that triggers validation on the whole PO? Or is the approval always a single PO-level click that surfaces errors for the supervisor to then act on per-item?
**ANSWER** : 15.2 and 15.3 describes 2 different action that the supervisor can do when reviewing a PO for approval. On this page, the expected process that the supervisor does on the page may be: 1. checks the header details and summary of the PO for correctness and completeness. at this point, the supervisor can decide to skip reviewing the items and approve the PO, or reject the PO because of they found from header details and summary of the PO, or proceed to checking the items ordered for this PO. 2. check the items one by one for correctness and completeness. The supervisor can mark an item as rejected and includes a reason why the item was being rejected. At this point, the only action that they can do to the PO is to reject it. 3. if no issue was found, then the supervisor approves the PO. 4. System does a validation of the approved PO. if errors are found, then it reverts back to the PO reviewing for approval page with the items with errors highlighted with description of the error. At this point, the only action that the supervisor can do to the PO is to reject it. If the system finds no error on the PO, then the PO and all the items will change status to approved.

### 16.4 Merchandiser Action After Partial Rejection

- After a Supervisor partially rejects some items, what exactly can the Merchandiser do?
  - Can they edit **only the rejected items** (e.g., change quantity) and resubmit?
  - Or must the Merchandiser resubmit the **entire PO** including non-rejected items?
**ANSWER** : Merchandiser must resolve the issue on the rejected items. Other items can be modifed by the Merchandiser if they think it is necessary. Then they resubmit the entire PO.
- When resubmitted, does the Supervisor need to re-review **all items** again, or only the previously-rejected items that were edited?
**ANSWER** : Supervisor need to re-review **all items** again. It would help the supervisor if modified values are highlighted.

### 16.5 Activity Log for Failed Approval Attempts

- Section 15.3 describes a Supervisor attempting approval, finding validation errors, and choosing to "do nothing" (leaving PO as `Submitted`).
**ANSWER** : correct
- Is this failed approval attempt — including the specific validation errors encountered — recorded in the **activity log**? (Other validation failure points in 14.1–14.4 explicitly say an activity log is created. Does the same apply here?)
**ANSWER** : doing nothing does not create an activity log since no action had been done on the PO.

### 16.6 Buyer-Seller Expiry vs. Item-Level Errors at Approval

- Section 2 states that an expired Buyer-Seller relationship "forces the approver to reject the order." Section 15.3 says the Supervisor can alternatively "do nothing" when validation errors occur.
**ANSWER** : correct
- Is an **expired Buyer-Seller relationship** a special case that forces rejection (no "do nothing" option), unlike item-level schedule errors where the Supervisor can choose to do nothing? Or do both follow the same Section 15.3 flow (Supervisor can either reject or do nothing)?
**ANSWER** : **expired Buyer-Seller relationship** is an example of an error that can occur during a system validation. However, a **expired Buyer-Seller relationship** is also an example of a PO validation error that can be resolved by creating a new Buyer-Seller relationship that will satisfy the existing PO. Bottomline, the supervisor has options to resolve issues on a PO. they can either return the PO to the merchandiser for them to make changes on the PO. Or they can do other changes in the ERP to resolve the validation error that the system found.

---

## 17. Follow-up Questions (Round 6)

### 17.1 Per-Item Rejection Marks — Saved State During Review

- Section 16.3 describes the supervisor marking individual items as rejected during the review session before clicking "Reject PO."
**ANSWER** : correct
- Are these per-item rejection marks a **persistent saved state** (stored even if the supervisor navigates away mid-review), or are they **session-only** (discarded if the supervisor exits without clicking "Reject PO")?
**ANSWER** : persistent saved state. Note on UI/UX: UI/UX for reviewing must be not too cluttered and easy for the reviewer to review hundreds of items.

### 17.2 PO-Level Rejection Note

- When a Supervisor rejects a PO, are per-item rejection reasons sufficient, or is a **PO-level rejection note** also required (or optionally available)?
**ANSWER** : per-item rejection reasons sufficient
- Specifically: if the supervisor rejects from the header review (step 1 of 16.3) without reviewing items at all, where does the rejection reason go — as a PO-level note?
**ANSWER** : rejection reason will be a PO-level rejection note. (not to be confused with adding note on the PO header. PO header notes are specific only for additional instructions specific for the PO)

### 17.3 Supervisor Rejects PO Without Marking Any Items

- If the Supervisor clicks "Reject PO" directly from the header review without marking any individual items as rejected, what happens to item statuses? Do all items remain `Submitted`?
**ANSWER** : all items remain `Submitted`
- In this case, what carries the reason for rejection — a PO-level note?
**ANSWER** : rejection reason will be a PO-level rejection note. (not to be confused with adding note on the PO header. PO header notes are specific only for additional instructions specific for the PO)

### 17.4 Edit Scope on a Rejected PO

- Section 16.4 confirms the Merchandiser can edit items (including non-rejected ones) on a Rejected PO before resubmitting.
**ANSWER** : correct
- Can the Merchandiser also **add new items** or **remove items** from a Rejected PO, or is editing limited to modifying quantities and details of existing items?
**ANSWER** : correct

### 17.5 Modified Values Highlighted for Supervisor on Resubmission

- Section 16.4 states "it would help the supervisor if modified values are highlighted" on resubmission.
**ANSWER** : correct
- What exactly gets highlighted — only fields the Merchandiser manually changed (e.g., quantity), or also system-re-fetched date-sensitive values that may have changed (e.g., price, terms) due to the Order Date?
**ANSWER** : only fields the Merchandiser manually changed on the PO

### 17.6 Re-attempting Approval After "Do Nothing"

- Section 15.3 and 16.6 establish that a Supervisor can "do nothing" after a failed approval attempt, leaving the PO in `Submitted` state.
**ANSWER** : correct
- When the same or another Supervisor later re-attempts approval on that PO, do they start from the **beginning of the review flow** (16.3 step 1) with no pre-filled state, or does the system retain and show the validation errors from the prior failed attempt as a starting point?
**ANSWER** : system retain and show the validation errors from the prior failed attempt as a starting point. similar behavior to 17.1

---

## 18. Follow-up Questions (Round 7)

### 18.1 Concurrent Supervisor Access During Review

- Section 17.1 establishes that per-item rejection marks are saved persistently. If Supervisor A has begun reviewing a `Submitted` PO and saved some marks, can Supervisor B simultaneously open the same PO for review and also make marks? What happens — do their marks merge, overwrite each other, or is one of them blocked?
**ANSWER** : block to avoid conflict in PO approval. concurrent editing or reviewing will be blocked by the system and notify the other users attempting to editing or reviewing that another user is editing or reviewing the PO and they are in read only mode.

### 18.2 Combined PO-Level and Per-Item Rejection Notes

- Section 17.2 establishes a PO-level rejection note (used when rejecting from header review). Section 17.3 shows per-item rejection reasons when items are individually marked.
**ANSWER** : correct
- When a Supervisor marks individual items with per-item reasons AND also wants to add an overall PO-level note before clicking "Reject PO," is the PO-level note field available in both cases, or only when no items have been individually marked?
**ANSWER** : yes a PO-level rejection note can still be added by the supervisor

### 18.3 Stale Validation Errors After Master Data Fix

- Section 17.6 states that stored validation errors from a failed approval attempt are shown as a starting point on the next attempt.
**ANSWER** : correct
- If the underlying issue was fixed between attempts (e.g., a new Buyer-Seller relationship was created, or an item schedule was corrected), does the system **clear the stale errors** before the Supervisor clicks "Approve PO" and run fresh validation? Or are the old errors shown until the Supervisor actively triggers a new approval attempt?
**ANSWER** : old errors shown until the Supervisor actively triggers a new approval attempt

### 18.4 Interim Review Marks — Visibility to Merchandiser

- While a PO is in `Submitted` status and a Supervisor has saved persistent per-item rejection marks (17.1) but has not yet clicked "Reject PO," can the **Merchandiser see those interim marks**? Or are in-progress supervisor review states visible only to supervisors until the formal rejection action is taken?
**ANSWER** : visible only to supervisors until the formal rejection action is taken

### 18.5 Activity Log for Interim Item Marks

- Since per-item rejection marks are saved persistently (17.1), is each mark saved during review recorded in the **activity log**? Or is only the final "Reject PO" action (with all its item rejections) logged as a single activity log entry?
**ANSWER** : each per-item rejection mark saved during review recorded in the **activity log**. then another **activity log** is created when the supervisor submits PO for rejection.
---

## 19. Follow-up Questions (Round 8)

### 19.1 Review Lock — Release Mechanism

- Section 18.1 establishes that concurrent review is blocked by a lock, and other users are placed in read-only mode.
- When is the lock **released**? Specifically:
  - Is the lock released automatically when the locking supervisor navigates away from the PO review page?
  - Or does the lock persist until the supervisor formally acts (Approve or Reject)?
  - Is there an **idle timeout** after which the lock auto-releases even if the supervisor has not navigated away?
**ANSWER** : lock released automatically when the locking supervisor navigates away from the PO review page.
- Can an administrator or another supervisor **manually break the lock** if the locking supervisor is unavailable?
**ANSWER** : The system should be able to log out users who have been idle and their session has expired, breaking the lock. Otherwise, an active user needs to navigate away from the page break the lock specifically.

### 19.2 Stale Validation Errors — Visual Indicator

- Section 18.3 confirms that old validation errors from a prior failed approval attempt are shown as-is until the supervisor actively triggers a new approval attempt.
**ANSWER** : correct
- Are these retained errors visually marked as **"from a previous attempt"** (e.g., a timestamp or "last checked on [date]" label), or do they appear identical to freshly computed errors — potentially misleading the supervisor into thinking the errors are still active when the underlying data may already be fixed?
**ANSWER** : label with "last checked on [date]" to add context to the validation error. 

### 19.3 Activity Log Visibility for Interim Item Marks

- Section 18.5 confirms that each per-item rejection mark during review is recorded in the activity log.
**ANSWER** : correct
- Section 18.4 confirms that interim marks are visible **only to supervisors** until formal rejection.
**ANSWER** : correct
- The Activity Log is listed in Section F (PO View) which is accessible to all users who can view the PO. Can the Merchandiser see in the activity log that individual items were marked during an in-progress review — even though the marks themselves are hidden from them?
**ANSWER** : in-progress review activity logs is also hidden from the Merchandiser and is only visible when the review activity has completed

### 19.4 Cancellation of an Approved PO

- The spec describes Merchandisers cancelling `Draft` POs (Section B.4) and Supervisors cancelling `Submitted` POs (Section C.3), but does not address who can cancel an `Approved` PO before it reaches completion.
**ANSWER** : System automatically cancels a PO if the system is configured to `Automatic Cancel` when `Cancel if not Delivered by Date` expires. In addition, manually completing a PO that has no items received will also cancel the whole PO. UX/UI should prompt user to make sure that the action will cancel the whole PO to confirm the action.
- Can an `Approved` PO be cancelled? If yes, who is authorized — the Merchandiser, the Supervisor, or a specific role (e.g., only a High Value Merchandiser if budget-category items are involved)?
**ANSWER** : an `Approved` PO can be cancelled by manually completing it while having no items received. this can be done by any user that has permission to do manual completion of PO.
- When an Approved PO is cancelled, the budget is released (confirmed in 13.3). Is any additional approval or confirmation step required before cancellation takes effect, given the budget impact?
**ANSWER** : none. this can be done by any user that has permission to do manual completion of PO.

---

## 20. Follow-up Questions (Round 9)

### 20.1 PO Header Status When Manually Completed With Zero Items Received

- Section 19.4 states: "manually completing a PO that has no items received will also **cancel the whole PO**."
**ANSWER** : change the name of the process to **manually closing** to remove the confusion of the process.
- Section 14.9 states that manually marking a PO as completed always sets the PO-level status to `Completed`.
**ANSWER** : Revise. Manually closing the PO does the following: PO is marked completed if items are received or partially received. PO is marked as canceled if all of the items have not been received. Manually closing will prompt the user to confirm the action highlighting that some of the items have not yet been received and that closing the PO is irreveriable.
- Section 13.1 confirms both `Completed` and `Cancelled` are valid PO-level statuses.
**ANSWER** : correct.
- These are contradictory. When a user manually completes an Approved PO with zero items received, what is the resulting **PO header status**:
  - `Cancelled` (PO is cancelled, all items = `Cancelled`), or
  - `Completed` (PO is completed, all items = `Cancelled`)?
**ANSWER** : change the name of the process to **manually closing** to remove the confusion of the process. Manually closing the PO does the following: PO is marked completed if items are received or partially received. PO is marked as canceled if all of the items have not been received.

### 20.2 High Value Merchandiser Restriction on Zero-Receive Cancellation

- Section D.3 of the spec states: "Only authorized High Value Merchandiser can manually mark a purchase order as completed if the order contains items belonging to a category with set budget."
**ANSWER** : correct. revise. Only authorized High Value Merchandiser can manually close a purchase order with items with set budget
- Section 19.4 states: "this can be done by any user that has permission to do manual completion of PO."
**ANSWER** : revise. Only authorized High Value Merchandiser can manually close a purchase order with items with set budget
- These conflict for Approved POs that contain budget-category items. When manually completing an Approved PO with zero items received (effectively cancelling it) and the PO has budget-category items, does the **High Value Merchandiser restriction from Section D.3 still apply** — or is the restriction waived because no budget was actually consumed on those items (none were received)?
**ANSWER** : revise. Only authorized High Value Merchandiser can manually close a purchase order with items with set budget

---

## 21. Follow-up Questions (Round 10)

### 21.1 Sales Order Reference — Missing Header Field

- Section B.1 lists these header fields: Buyer, Seller, Order Date, Cancel if not Delivered by Date, Type of Purchase Order, Facility List, Delivery Location, Notes, Attached Files.
**ANSWER** : correct
- Section F.2 (Referenced Documents) lists "Sales Order referenced to the purchase order for type `Sales Order Fulfillment`."
**ANSWER** : correct
- There is no "Sales Order Reference" field listed in B.1. For a PO of type `Sales Order Fulfillment`, where and how does the user select and link the Sales Order — is it an additional header field that appears when the user picks `Sales Order Fulfillment` as the type? Or does selecting a Sales Order automatically set the PO type?
**ANSWER** : specs updated. Field **Referenced Sales Order** is added

### 21.2 Sales Order Fulfillment — Item Quantities Pre-populated

- When items are added to a `Sales Order Fulfillment` PO (restricted to items listed in the referenced SO), are the ordered quantities **pre-populated from the SO's ordered quantities**, or does the Merchandiser enter quantities manually (still subject to MOQ and Order Multiple validation)?
**ANSWER** : pre-populated from the SO's ordered quantities but adjusted according to MOQ and Order multiple.

### 21.3 Dashboard H.4 and H.5 — Threshold or Immediate

- Dashboard items H.4 ("Number of PO with no items received") and H.5 ("Number of PO partially received") track approved POs by receipt status.
**ANSWER** : correct. specs updated
- Does H.4 show **all** approved POs the moment they are approved (since at approval time, no items have been received yet), or is there a threshold before an approved PO appears in H.4 — for example, only after the Order Date has passed, or only after the `Cancel if not Delivered by Date` is within the alert window?
**ANSWER** : specs updated. H4, H5 and H6 only refers to approved PO

### 21.4 Removal of PO-Detail Rejection During Review

- Section D.4 states: "Rejection on an item or PO detail can be removed by the supervisor. But all rejection are final and cannot be removed once the supervisor submits the rejection of the PO."
**ANSWER** : correct
- If the supervisor added a whole-PO rejection note (Section D.2 type — rejecting because of PO details, not specific items) and then removes it before formally submitting the rejection, can the supervisor then proceed to reviewing items and either approve or reject at the item level? Or does removing the whole-PO rejection note only clear the note text without changing the available actions?
**ANSWER** : removing all rejections means the PO can be now approved

---

## 22. Follow-up Questions (Round 11)

### 22.1 Referenced Sales Order Field — Conditional Visibility

- Section B.1 now includes **Referenced Sales Order** as a header field, described as: "Related sales order when purchase order is set to type `Sales Order Fulfillment`."
**ANSWER** : correct
- Is this field only shown/required when the user selects `Sales Order Fulfillment` as the PO type, or is it always visible on the form and simply left blank for other types?
**ANSWER** : field only shown/required when the user selects `Sales Order Fulfillment`

### 22.2 Referenced Sales Order — Changeable After Draft Creation

- If the Merchandiser saves a draft SOF PO with a Referenced Sales Order already set, can they later change the Referenced Sales Order to a different SO while still in Draft?
**ANSWER** : no SOF PO cannot change the referenced sales order
- If yes, what happens to the items already added (which were filtered from and pre-populated based on the original SO)? Are they cleared, or does the system flag them as invalid?
**ANSWER** : SOF PO cannot change the referenced sales order

### 22.3 SOF — Pre-populated Quantity Exceeds SO Requirement Due to MOQ Adjustment

- Section 21.2 confirms quantities are pre-populated from the SO but adjusted upward for MOQ and Order Multiple.
**ANSWER** : correct
- Section 13.8 confirms that if the SO quantity is later reduced below the PO's ordered quantity, the PO is auto-rejected.
**ANSWER** : revise. this will follow the process mentioned in the answer for 16.3 question as one of the things that the system validates when the PO is approved.
- This creates a conflict: if the SO requires 5 units but MOQ is 10, the system adjusts the PO quantity to 10 — which immediately exceeds what the SO needs.
**ANSWER** : revise. system will take into account MOQ and order multiple when validating the quantity ordered for the SO.
- In this scenario, is the PO allowed to proceed with a quantity higher than the SO's required quantity (ordering surplus to satisfy vendor MOQ), or is the PO blocked because the ordered quantity exceeds the SO requirement?
**ANSWER** : revise. system will take into account MOQ and order multiple when validating the quantity ordered for the SO.

---

## 23. Follow-up Questions (Round 12)

### 23.1 UOM vs SKU — Configuration Scope

- Section B.3 states: "System can be configured to order using this UOM or the set `stock keeping unit (SKU)` used by the company's facility."
**ANSWER** : correct
- Is this a **global system-wide setting**, or can it be configured **per Buyer-Seller relationship** (i.e., some vendors are ordered in UOM, others in SKU)?
**ANSWER** : configured per Buyer-Seller relationship

### 23.2 UOM vs SKU — MOQ and Order Multiple Conversion

- MOQ and Order Multiple are defined in the vendor's UOM. If the system is configured to display and enter quantities in the company's SKU unit, how does conversion work?
**ANSWER** : system will maintain a UOM conversion for the organization
- For example: vendor UOM = "case of 12," company SKU = "each," MOQ = 5 cases. Does the user see MOQ as "5 cases" or as "60 each"? Is the  validation applied on the SKU-converted quantity or the raw UOM quantity?
**ANSWER** : MOQ/Order Multiple will always display in vendor's UOM for ordering. for the example, user will see MOQ as "5 cases".

### 23.3 UOM vs SKU — SOF Pre-populated Quantities

- Section 21.2 confirms SOF item quantities are pre-populated from the Sales Order. Sales Orders are likely expressed in the company's SKU unit (since they are customer-facing).
**ANSWER** : correct
- When a SOF PO is configured to order in vendor UOM, are the pre-populated quantities automatically converted from SO's SKU quantities to the vendor's UOM quantities? If yes, does the MOQ/Order Multiple adjustment happen before or after the SKU-to-UOM conversion?
**ANSWER** : pre-populated quantities are automatically converted from SO's SKU quantities to the vendor's UOM quantities then adjusted the quantities to follow the MOQ/Order Multiple for that item.
