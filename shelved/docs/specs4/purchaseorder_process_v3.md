# Purchase Order Process Flow

## A. Permissions & Actors

The PO module uses named, granular permissions that the system admin assigns to roles. Each organisation defines its own roles from these permissions. No actor is hardcoded.

### A.1 Named Permissions

| Permission | Actions | View Scope (PO detail) | Dashboard Scope |
|------------|---------|------------------------|-----------------|
| `purchaseorder_modify` | Create, modify, save Draft, submit, clone Completed/Cancelled non-budgeted POs; add/remove/edit non-budget-category items | Non-budgeted POs: Draft, Submitted, Rejected, Completed, Cancelled | Non-budgeted: Draft, Rejected, Cancelled, Completed |
| `purchaseorder_budgeted_modify` | Create, modify, save Draft, submit, clone Completed/Cancelled budgeted POs; add/remove/edit both budget-category and non-budget-category items | Budgeted POs: Draft, Submitted, Rejected, Completed, Cancelled | Budgeted: Draft, Rejected, Cancelled, Completed |
| `purchaseorder_modify_cancel` | Review and cancel non-budgeted Draft and Rejected POs | Non-budgeted: Draft, Rejected, Cancelled | Non-budgeted: Draft, Cancelled |
| `purchaseorder_budgeted_modify_cancel` | Review and cancel budgeted Draft and Rejected POs | Budgeted: Draft, Rejected, Cancelled | Budgeted: Draft, Cancelled |
| `purchaseorder_approve` | Review, approve, reject, or cancel any Submitted PO (budgeted and non-budgeted) | All POs: Submitted, Approved, Rejected, Cancelled | All: Submitted, Approved, Rejected, Cancelled |
| `purchaseorder_manual_close` | Manually close non-budgeted Approved POs | Non-budgeted: Approved, Completed, Cancelled | Non-budgeted: Approved, Cancelled, Completed |
| `purchaseorder_budgeted_manual_close` | Manually close budgeted Approved POs | Budgeted: Approved, Completed, Cancelled | Budgeted: Approved, Cancelled, Completed |
| `purchaseorder_view` | Read-only view of any non-Draft PO | All POs: Submitted, Approved, Rejected, Cancelled, Completed | All: Submitted, Approved, Rejected, Cancelled, Completed |

### A.2 Permission Rules

- **Budgeted classification:** A PO is "budgeted" once any item from a budget category is added. Adding a budget-category item requires `purchaseorder_budgeted_modify`. Once a PO becomes budgeted, only users with `purchaseorder_budgeted_modify` can view/edit it (users with `purchaseorder_view` can still view non-Draft states).
- **Mixed-item POs:** A PO with both budget and non-budget items is treated as a budgeted PO. Manual close of such a PO requires `purchaseorder_budgeted_manual_close` only (not the regular `purchaseorder_manual_close` as well).
- **Submit permission:** The ability to submit a PO is bundled with `purchaseorder_modify` / `purchaseorder_budgeted_modify`. Not separately grantable.
- **Same-user self-approval:** If a role is configured with both creator and approver permissions, the same user can approve their own PO.
- **Cross-ownership cancel/close:** Any user with the relevant cancel or manual-close permission may act on POs regardless of who created them.

---

## B. Create Purchase Order

### B.1 Header Fields

- **Buyer** — Company making the purchase. Determines the applicable budget and limits the available Facilities. Selected from companies under the organisation. A PO is always one Buyer to one Seller. Cannot be changed on a Draft that already has items (remove items first).

- **Seller** — Vendor. Selected from vendors that have an active Buyer-Seller relationship with the selected Buyer. The Buyer-Seller relationship determines item costs, minimum order quantities (MOQ), order multiples, ordering schedules, and the UOM/SKU configuration per item. Cannot be changed on a Draft that already has items.

- **Order Date** — Document date that drives all date-sensitive data (prices, costs, Buyer-Seller relationship terms, item schedules). Distinct from Created Date. Constraints:
    - Must be greater than the date the PO is approved (for auto-approved POs, the auto-approval date is used).
    - Minimum threshold between Approval date and Order Date is **configurable per organisation**.
    - Maximum allowed forward range is **configurable per organisation**.
    - Changing the Order Date on a Draft shows a warning prompt (the change affects all date-sensitive data). On confirm, the system re-fetches and re-validates. Validation results are displayed when the user navigates away from the PO Details page (also saves the Draft).
    - All date-sensitive values are locked after the PO is approved.

- **Cancel if not Delivered by Date** — Optional failover date. Must be after the Order Date; minimum gap configurable per organisation. Configurable action on expiry:
    - `Automatic Cancel` — cancels the entire PO if no items have been received. Partially received POs fall back to `Notify Only` behaviour (notifies users with manual-close permission). Processed via daily end-of-day system job.
    - `Notify Only` (default) — notifies users with manual-close permission.
    - `Do Nothing` — no automated action, including for partially received POs.
    - A "nearing expiration" alert appears on dashboards (configurable lead time, default 5 days), delivered as a login-based digest to users with manual-close permission.

- **Type of Purchase Order** — See Section B.2.

- **Facility List** — Facilities receiving the order. Drawn from facilities under the selected Buyer. Every facility on a submitted PO must have at least one item with non-zero quantity. A single facility may have zero quantity for some items while non-zero for others. Modifying the Facility List on a Draft:
    - Removing a facility clears all per-facility quantities for that facility; the system re-validates MOQ/order multiple on the aggregate total and shows a confirmation prompt with per-item impact.
    - Adding a facility starts per-facility quantities at 0 for that facility (requires user to allocate).
    - At submission, facilities with zero quantity across ALL items are prompted for removal; declining blocks submission.

- **Delivery Location** — Single facility where the order is physically delivered. Required for all PO types. May be any facility under the selected Buyer — not restricted to the Facility List (e.g., a cross-dock warehouse).
    - Empty Facility List + Delivery Location set → Delivery Location is auto-added into the Facility List.
    - Single Facility List entry + empty Delivery Location → Navigation out is blocked; Delivery Location is required.
    - Changing Delivery Location when it previously matched the single facility in the Facility List → Prompt 1: "Keep Facility List in sync?" If user declines, Delivery Location updates independently. If user accepts, Prompt 2 (facility-removal impact) shown only if the Facility List change affects per-facility quantities.
    - Symmetric behaviour when changing the Facility List when its single facility matches the Delivery Location.

- **Referenced Sales Order** — Only shown/required when PO Type is `Sales Order Fulfillment`. Locked once items are added (see Section B.2 for SOF rules).

- **Notes** — General instructions specific to the PO. Separate from rejection and cancellation notes.

- **Attached Files** — Documents related to the order.

### B.2 Type of Purchase Order

Three PO types, distinguished by facility configuration and workflow:

- **`Inventory Replenishment` (IR)** — Replenishes inventory of a single facility. Conditions:
    - Facility List empty, or Facility List contains exactly one facility that matches the Delivery Location.

- **`Cross Dock Order` (CD)** — Replenishes multiple facilities via a cross-dock warehouse; inventory allocated to be dispatched to the other Facilities (allocation logic is in the Receiving module). Conditions:
    - Multiple facilities in Facility List regardless of Delivery Location, OR
    - Single facility in Facility List but Delivery Location is a DIFFERENT facility.
    - MOQ and order multiple rules are enforced on the aggregate total across the Facility List only (not including the Delivery Location facility, if different).

- **`Sales Order Fulfillment` (SOF)** — Orders created to fulfill a specific Sales Order. Conditions:
    - Single facility with Delivery Location = same facility (identical structural constraint to IR).
    - Referenced Sales Order required.
    - Items restricted to those in the referenced SO that the Seller sells and that are valid on the Order Date's ordering schedule.
    - Multiple SOF POs may reference the same Sales Order.

#### B.2.1 PO Type Transition Matrix

| From → To | Allowed When |
|-----------|--------------|
| IR → CD | No restriction on items. Requires Facility List entry ≠ Delivery Location (multiple facilities or mismatched Delivery Location). Auto-switched when facility config changes; user prompt warns of type change. |
| IR → SOF | Only when no items exist. |
| CD → IR | No restriction on items. Requires Facility List to have exactly one facility AND facility = Delivery Location. Auto-switched when facility config changes; user prompt warns of type change. |
| CD → SOF | Only when no items exist AND facility config already matches SOF (single facility with Delivery Location = same facility). |
| SOF → IR | Only when no items exist. Referenced Sales Order field is cleared automatically. |
| SOF → CD | Only when no items exist. Referenced Sales Order field is cleared automatically. |

- **Manual type selection:** The user may manually select IR or CD from a type dropdown when the facility configuration allows it; the system accepts or blocks the change based on facility config. SOF always requires explicit selection.
- **Removing all items** unlocks type changes and Buyer/Seller changes that are otherwise blocked.

#### B.2.2 SOF Pre-population Modes (Configurable Per Organisation)

When an SOF PO is created and the Referenced Sales Order is set:

- **`Round Up`** (default) — Items from the SO with an active Buyer-Seller relationship and a matching Order Date schedule are pre-populated. Quantities come from the SO and are rounded up to satisfy MOQ and order multiple.
- **`As Is`** — Same item filter as `Round Up`. Quantities come from the SO but are NOT rounded up. Submission validation enforces MOQ/order multiple (hard-block).
- **`Manual`** — No items are pre-populated. User adds items manually. When a user adds an item from the referenced SO, the quantity is pre-filled from the SO quantity (not rounded up) and is editable. Submission enforces MOQ/order multiple (hard-block).

- Items belonging to a category with a set budget are included in pre-population only if the PO creator holds `purchaseorder_budgeted_modify`. Otherwise they are included with a validation error flagging the permission gap.
- In `Manual` mode, adding a budget-category item requires `purchaseorder_budgeted_modify` (same rule as any other PO type).

### B.3 Auto-set Fields

- **Document ID** — System-generated.
    - Draft: `DFTPO-yyyyMMXXXXXX` (yyyyMM from creation month; generated on first save).
    - Submitted and later: `PO-yyyyMMXXXXXX` (yyyyMM from submission month; replaces the DFTPO ID on submission).
    - Separate counters reset monthly and are global across all companies in the organisation.

- **Status** — See Section I.

- **Created By / Created Date** — User and timestamp when Draft is first saved.
- **Updated By / Updated Date** — User and timestamp of the last modification.

### B.4 Items

#### B.4.1 Adding Items

- Items are sourced from the Buyer-Seller relationship for the selected vendor.
- Can be added individually by product name, GTIN, or product ID, or in bulk from a filtered list by category.
- Backend filters items based on the day-of-week schedule for the Order Date. When searching individually, items that exist in the Buyer-Seller relationship but are off-schedule for the Order Date are shown in search results with a "not on schedule" indication and cannot be selected.
- Items belonging to a category with a set budget can only be added by users with `purchaseorder_budgeted_modify`.
- Each item is unique per PO (no duplicates).
- SOF POs can only add items listed in the referenced Sales Order.

#### B.4.2 Item-Level Fields

- **Quantity** — See B.4.3 for unit rules.
- **Per-facility quantity** — Applicable for Cross Dock POs; allocation of the total quantity across facilities in the Facility List.
- **Unit cost / Extended cost** — Sourced from the Buyer-Seller relationship as of the Order Date. Visible to any user with access to view/create/modify the PO. Not user-editable.
- **Item-level notes** — Free-text notes specific to the line item.
- **Item-level rejection note** — Added by supervisor during review (see Section D).
- **Item-level cancellation note** — Added when an item is individually cancelled (see Section E).
- **Item status** — See Section I.

#### B.4.3 UOM / SKU Display

Each Buyer-Seller relationship defines the vendor's UOM and the buyer's company SKU unit. The system maintains a UOM conversion table for the organisation. MOQ and order multiple are always defined in the vendor's UOM.

- **`Inventory Replenishment` POs** — Entry unit is **configurable per organisation** by the system admin.
    - If configured to use **company's SKU**: input is in company's SKU; vendor's UOM equivalent displayed read-only in real-time.
    - If configured to use **vendor's UOM**: input is in vendor's UOM; no company's SKU conversion shown.
- **`Cross Dock Order` and `Sales Order Fulfillment` POs** — Always use **company's SKU** as the editable input; vendor's UOM shown as read-only conversion side-by-side regardless of org configuration.
    - Cross Dock per-facility grid: each facility row/column shows quantity in company's SKU; vendor's UOM equivalent appears only in the aggregate total row.

#### B.4.4 MOQ and Order Multiple Validation

- Real-time client-side validation at quantity entry (MOQ and order multiple data preloaded with item data).
- If entered quantity is below MOQ: input is blocked; minimum required quantity shown (vendor's UOM + company's SKU equivalent).
- If entered quantity is not a valid order multiple: input is blocked; nearest rounded-up valid value suggested.
- For multi-facility (Cross Dock) orders, both rules are enforced on the **aggregate total across the Facility List** (excluding the Delivery Location if different), not per individual facility.
- `As Is` and `Manual` SOF modes may produce pre-filled quantities below MOQ or not matching order multiple; submission is hard-blocked until manually corrected.

#### B.4.5 Budget Validation

- Budget periods are defined in the Budget Planning module. The PO module reads the active budget as of the Order Date.
- Budget is compared against the vendor cost (from the Buyer-Seller relationship as of the Order Date).
- No budget reservation at Draft.
- Validated at manual Save Draft, submission, and approval.
- Submission is hard-blocked if the category budget would be exceeded.
- Budget is consumed on PO approval, first-come-first-served. At approval time budget is re-validated against the then-current available balance; if the balance is insufficient, approval fails and the supervisor sees the validation error.
- Budget for unreceived quantities is released when an Approved PO is cancelled or manually closed with unreceived items.
- Budget activity (consumption/release) appears in the Budget Planning module's activity log, NOT in the PO's activity log.

#### B.4.6 Validation Rules Summary

| Rule | Real-time (client-side) | Deferred (backend) |
|------|-------------------------|---------------------|
| MOQ | ✓ | Re-checked on submit/approve |
| Order multiple | ✓ | Re-checked on submit/approve |
| Day-of-week schedule | ✓ (for items already loaded) | Re-checked on submit/approve |
| Budget availability | — | ✓ Save Draft, Submit, Approve |
| Buyer-Seller relationship validity | — | ✓ Save Draft, Submit, Approve |

- Deferred validation runs when the user: (1) clicks Save Draft, (2) navigates away from the PO Details page (which also saves the Draft), (3) submits, (4) approves.
- Auto-save does NOT trigger validation. Session expiry does not trigger auto-save.

### B.5 Saving, Submitting, Cancelling

- The Merchandiser submits the PO for approval after completing all details.
- The Save Draft button is available at all times during Draft editing.
- A Draft may be saved with validation errors highlighted; submission is hard-blocked until all errors are resolved (invalid schedule, expired Buyer-Seller relationship, budget exceeded, MOQ/order multiple violations, etc.).
- Failed submission creates an activity log entry but does not change PO status.
- A Draft PO can be cancelled by users with `purchaseorder_modify_cancel` (non-budgeted) or `purchaseorder_budgeted_modify_cancel` (budgeted). See Section E for the cancellation flow.
- A Rejected PO can be edited (items added, removed, modified) and resubmitted. The same edit lock as Draft applies during revision. No limit on rejection/resubmission cycles.
- A Rejected PO can also be cancelled using the same `purchaseorder_modify_cancel` / `purchaseorder_budgeted_modify_cancel` permissions.
- Cancelled and Completed POs can be cloned. SOF POs cannot be cloned.

### B.6 Cloning

- Permission-gated: `purchaseorder_modify` clones non-budgeted POs; `purchaseorder_budgeted_modify` clones budgeted POs.
- Clone copies all header fields except Order Date and Cancel if not Delivered by Date (which must be set by the user).
- All items, quantities, and per-facility quantities are copied.
- Cloned items are immediately re-validated against the current Buyer-Seller relationship and schedule; items no longer sold by the vendor are included with validation errors rather than excluded.
- Cloned PO is saved as a Draft.
- An activity log entry "PO cloned from [source PO Document ID]" is created on the new cloned PO. No entry is created on the source PO.

### B.7 Edit Lock

- Only one user may actively edit a PO (Draft or Rejected) at a time.
- Lock is at the PO level (covers both the PO Details page and PO Items page).
- Other users with edit-capable permissions viewing the same PO are placed in read-only mode.
- Lock is released when the editing user navigates away from the PO, logs out, or their session expires. Session idle timeout is configurable per organisation by the system admin; the system admin can force-expire a user session.

### B.8 Two Edit Pages

The PO edit interface has two pages: **PO Details** (header fields) and **PO Items** (item list and quantities). Navigation between pages persists the current state and triggers deferred validation.

### B.9 Concurrent Drafts

When a user sets the PO details and there is already a Draft PO for the same Buyer, Seller, PO Type, and Order Date, the system shows a non-blocking warning with a link to the existing Draft.

---

## C. Approving Purchase Order

### C.1 Supervisor Review (Approval Required)

A user with `purchaseorder_approve` reviews the Submitted PO. They may:

- **Approve** — triggers system validation. If validation passes, the PO and all items transition to `Approved`. Budget (if any) is consumed. If validation fails, the system returns the supervisor to the review page with errors highlighted and labeled with the date they were last checked. The supervisor may then:
    - Reject the PO (see Section D).
    - Leave it as `Submitted` while the issue is resolved externally (e.g., master data correction). Validation errors from the previous failed approval are retained and shown as a starting point for the next supervisor opening the PO.
- **Reject the PO** — see Section D.
- **Cancel the PO** — see Section E.
- **Leave it as Submitted without action** — no change; rejection marks and validation errors persist per the above.

### C.2 Approval Validation Scope

System validation at approval time checks:
- Buyer status
- Seller status
- Dates (Order Date > approval date; Cancel if not Delivered by Date > Order Date if set)
- Selected facilities and Delivery Location belong to the Buyer
- Active Buyer-Seller relationships for each item ordered
- Buyer-Seller relationship validity
- Item ordering schedule (day-of-week)
- MOQ and order multiple per item
- Budget availability (against then-current balance)
- Approver permissions

### C.3 Review Lock

- Only one supervisor may actively review a PO at a time.
- Other supervisors opening the PO while it is under review are placed in read-only mode.
- Lock is at the PO level. Released on navigation away, logout, or session expiry.

### C.4 Auto-Approval (No Approval Configured)

- Whether approval is required is **configurable per organisation** by the system admin (single-level only; no multi-level approval).
- When no approval is required, the PO is automatically approved upon submission. The approval date equals the auto-approval timestamp and must satisfy the Order Date constraint (Order Date > approval date).
- An activity log entry is created with acting user = "system" indicating the PO was system-approved.

### C.5 Resubmission and Change Highlighting

When a rejected PO is resubmitted, fields changed by the Merchandiser since the last rejection are highlighted for the supervisor:

- **Scope** — All changeable header fields AND all item changes (quantity changes, per-facility changes, items added, items removed).
- **Baseline** — Snapshot taken at the moment the supervisor formally submitted the last rejection.
- **Presentation:**
    - Value-changed fields: coloured border + tooltip showing the previous value.
    - Added items: "New" badge on the item row.
    - Removed items: ghost / greyed-out row with strikethrough + "Removed" badge. The supervisor may mark a ghost row as rejected (indicating the Merchandiser should re-add the item).
    - Approval validation errors are visually distinct from change-highlighting; both types of information can be surfaced in the same tooltip window on the affected field/item.
- **Persistence** — Highlights are cleared when the supervisor submits approval or rejection. Rejecting takes a new snapshot for the next revision cycle.

---

## D. Rejecting Purchase Order

### D.1 Rejection Workflow

- A supervisor reviewing a Submitted PO may add and remove **rejection marks** on the PO header and/or individual items. Marks are saved persistently as the supervisor reviews (so the supervisor can pause and resume).
- Rejection marks added during an in-progress review are visible only to users with `purchaseorder_approve` until the review is formally completed. The Merchandiser does not see in-progress marks.
- Item statuses remain `Submitted` throughout the in-progress review; they only transition (to `Rejected` or `Approved`) when the supervisor formally submits the review outcome.
- If all rejection marks are removed, the supervisor may proceed to approve.

### D.2 Formal Rejection Submission

- When the supervisor formally submits the rejection:
    - The PO status transitions to `Rejected`.
    - Items with rejection marks transition to `Rejected`.
    - Items without rejection marks remain `Submitted`.
    - All rejection marks become final and cannot be further edited.
- At least one rejection mark and/or PO-level rejection note is required to formally submit a rejection.

### D.3 Rejection Notes

- **PO-level rejection note** — Required when rejecting for a header reason or the PO as a whole. Stored separately from the general Notes field.
- **Per-item rejection note** — Required on each item rejected for an item-specific reason.
- Both note types may coexist on the same rejection.
- System-identified validation errors (invalid schedule, expired relationship, etc.) surfaced during approval may be turned into item-level rejections at the supervisor's discretion.

### D.4 Ghost Row Rejection (Resubmission Review)

- On a resubmitted PO, items removed by the Merchandiser since the last rejection appear as ghost rows (see Section C.5). The supervisor may apply a rejection mark with a note to a ghost row to signal that the removal was incorrect.
- On a Rejected PO, the Merchandiser sees the ghost row rejection marks and must address them by re-adding the item. An optional "Undo removal" shortcut on the ghost row may be provided to restore the item directly using the standard add-item flow.

### D.5 Rejection Lifecycle

- When the Merchandiser resubmits a Rejected PO:
    - All items not rejected return to `Submitted`. Rejected items return to `Submitted` upon resubmission (whether or not the Merchandiser addressed them).
    - All rejection notes on the PO are removed from the rejection-note display component. History is preserved in the activity log.
- There is no limit on rejection/resubmission cycles.

### D.6 Rejection Display on PO View

- PO-level rejection notes appear in a dedicated view component near the PO Details section, visually distinct from the general Notes field. Shown only while the PO has active rejection notes (removed on resubmission).
- Per-item rejection notes appear as an inline section below the affected item's row in the item list.

### D.7 Activity Log Visibility During In-Progress Review

Activity log entries generated during an in-progress supervisor review (mark added/removed, auto-save, in-progress modifications) are visible only to users with `purchaseorder_approve` until the review is formally completed (approved or rejected). Upon completion, the full review activity log becomes visible to all users with access to the PO.

---

## E. Closing Purchase Order

### E.1 Auto-Completion

The system automatically transitions the PO to `Completed` when total received quantities (aggregated across all linked Receiving Advice documents) equal the ordered quantity for every item. There is no tolerance threshold — exact equality is required. For Cross Dock Orders, receipt at the Delivery Location is sufficient to trigger auto-completion.

### E.2 Manual Close

A user with the appropriate manual-close permission (`purchaseorder_manual_close` for non-budgeted, `purchaseorder_budgeted_manual_close` for budgeted/mixed POs) may manually close an Approved PO.

- Manual close is blocked if there are pending Receiving Advice documents linked to the PO. The definition of "pending" is owned by the Receiving module.
- Outcome depends on receipt status at the time of closing:
    - **No items received** → PO status = `Cancelled`; all items = `Cancelled`; all consumed budget is released.
    - **One or more items received (fully or partially)** → PO status = `Completed`. Each item's status is set based on received quantity: `Completed` (fully received), `Short Closed` (partially received), or `Cancelled` (not received). Consumed budget for unreceived quantities is released.

### E.3 Cancellation Paths and Notes

Four paths lead to a `Cancelled` status (on PO or item):

1. **Draft cancel** — user with `purchaseorder_modify_cancel` / `purchaseorder_budgeted_modify_cancel`.
2. **Rejected cancel** — same permissions.
3. **Submitted cancel** — user with `purchaseorder_approve`.
4. **Manual close of Approved PO with no items received** — whole PO becomes Cancelled.
5. **Manual close of Approved PO with partial receipt** — PO becomes Completed, but undelivered items become Cancelled.
6. **System auto-cancel** — daily job on Approved PO with `Automatic Cancel` action, expired Cancel if not Delivered by Date, and no items received.

**Cancellation notes** are required at both PO-level (when the whole PO is cancelled) and per-item (for each cancelled item). Stored separately from the general Notes field.

### E.4 Cancellation / Manual Close UI Flow

Applies uniformly to user-initiated cancellation and manual close:

1. User clicks **Cancel PO** (or **Manual Close**).
2. System shows a confirmation dialog with:
    - A text box for the PO-level cancellation note (omitted when the path results in PO = `Completed` with only per-item cancellations — i.e., partial manual close).
    - A button/link to "Add per-item cancellation notes" (alternate path).
3. Two paths from the dialog:
    - **Path A (bulk):** User enters the note and clicks OK. The system cancels / closes the PO. The note is applied as the PO-level cancellation note (if applicable) and as the cancellation note on every cancelled item.
    - **Path B (detailed):** User clicks the "Add per-item cancellation notes" button. The system redirects to a per-item cancellation notes page where the user can:
        - Enter a general cancellation note (applies to the PO-level if applicable, and serves as the default for items where no specific note is entered).
        - Enter specific cancellation notes for individual items.
        - Click **Confirm** on the per-item page to trigger the actual cancellation / manual close.

### E.5 Auto-Cancel (System)

- Daily end-of-day system job checks Approved POs with `Automatic Cancel` action and expired Cancel if not Delivered by Date.
- If no items have been received: the PO is cancelled. A fixed standard system-generated cancellation note is placed on the PO-level and on every cancelled item. Activity log entry is created with acting user = "system".
- If the PO is partially received: the `Automatic Cancel` action falls back to `Notify Only` behaviour — notifications are sent to users with the appropriate manual-close permission. No status change.

### E.6 Cancellation Note Display on PO View

- PO-level cancellation notes appear in the same dedicated view component as PO-level rejection notes (separate from the general Notes field), near the PO Details section.
- Per-item cancellation notes appear as an inline section below the affected item's row in the item list (same pattern as per-item rejection notes).

### E.7 Irreversibility

Completed and Cancelled POs cannot be reopened or edited.

---

## F. Referenced Documents

The PO provides reference links to related documents for quick lookup. None of these links change PO state on their own.

1. **Attached Files** — Documents attached to the PO during creation/editing.
2. **Referenced Sales Order** — For SOF POs. Cannot be changed once items are added to the PO. Cleared automatically if the PO Type is changed away from SOF while no items exist.
3. **Receiving Advice documents** — Documents that record receipt against the PO (owned by the Receiving module).
4. **Returns Announcement documents** — Documents that record returns against the PO (owned by the Returns module). The PO stores only the reference link. Returns do NOT alter PO item status or received quantities.

---

## G. View of Purchase Order

The PO view page consists of the following components:

1. **Quick summary and next immediate action** — concise status overview and the next workflow step for the viewing user.
2. **Buyer details**
3. **Seller details**
4. **Purchase Order details** — Order Date, PO Type, Facility List, Delivery Location, Cancel if not Delivered by Date, Referenced Sales Order (if SOF), Notes, Attached Files.
5. **Rejection / Cancellation notes component** — Displayed near the PO Details section as a dedicated, visually distinct component. Contains:
    - PO-level rejection note (when applicable)
    - PO-level cancellation note (when applicable)
6. **Purchase Order totals** — Grand total extended cost, total number of line items, total ordered quantity (in vendor's UOM).
7. **Activity log history** — Summary view showing milestones of the PO (status changes, significant edits); individual entries are expandable for field-level detail.
8. **Item list** — Sortable and searchable. For each item row:
    - Product, quantity (with UOM/SKU display per Section B.4.3), per-facility quantities (Cross Dock only), cost, item status.
    - Below the item row: item-level notes, item-level rejection note (when applicable), item-level cancellation note (when applicable).
    - Ghost rows for removed items (during resubmission review / on the Rejected PO).

---

## H. PO List and Search

A searchable PO list page is available to users with any view-capable permission (subject to each permission's view scope defined in Section A).

- **Search filters:** Order Date range, Status, Buyer, Seller, Facility, PO Document ID.
- **Search NOT supported:** by item name or GTIN.
- **Default sort:** Created Date, latest first.
- Clicking a PO navigates to its detail view.

---

## I. Dashboard

The dashboard is visible to users who have permission to view the PO status in question or can perform an action being notified. Dashboard scope per permission is defined in Section A. Clicking a count drills down to a filtered PO list. Scope is organisation-wide (not filtered by user's assigned companies/facilities).

### I.1 Dashboard Items

1. Number of POs not submitted (Drafts).
2. Number of POs needing approval.
3. Number of POs that were rejected and need revisions.
4. Number of approved POs with no items received.
5. Number of approved POs partially received.
6. Number of approved POs nearing expiration of `Cancel if not Delivered by Date` (configurable lead time, default 5 days).
7. Number of approved POs with `Cancel if not Delivered by Date` expired that are partially received (pending manual review/close).

---

## J. Statuses

### J.1 PO-Level Statuses

| Status | Description |
|--------|-------------|
| `Draft` | PO is being created and has not been submitted. Document ID prefix `DFTPO-`. |
| `Submitted` | PO has been submitted for approval. Document ID prefix `PO-`. |
| `Approved` | PO has been approved and is pending receipt of items. |
| `Rejected` | PO has been rejected by the supervisor and is pending Merchandiser revision. |
| `Completed` | PO has been closed with at least one item received (auto or manual close). |
| `Cancelled` | PO has been cancelled. Occurs when: Merchandiser cancels a Draft; a user cancels a Rejected PO; Supervisor cancels a Submitted PO; PO is manually closed with no items received; or system auto-cancels via `Automatic Cancel` when `Cancel if not Delivered by Date` expires and no items have been received. |

### J.2 Item-Level Statuses

| Status | Description |
|--------|-------------|
| `Draft` | Item is part of a Draft PO. |
| `Submitted` | Item is part of a Submitted PO, or is part of a Rejected PO and has no item-level rejection. |
| `Approved` | Item has been approved and is pending receipt. Remains `Approved` until the PO is closed. |
| `Rejected` | Item was flagged by the supervisor in the formally submitted rejection. Returns to `Submitted` when the Merchandiser resubmits the PO. |
| `Completed` | Item fully received (received quantity equals ordered quantity). |
| `Short Closed` | Item partially received when the PO was manually closed. |
| `Cancelled` | Item was not received when the PO was closed or cancelled. |

### J.3 Status Transition Summary

- **PO-level:** `Draft` → `Submitted` → `Approved` → `Completed` | `Cancelled`
    - Alternates: `Submitted` → `Rejected` → `Submitted` (repeat); `Draft` | `Submitted` | `Rejected` → `Cancelled`
- **Item-level (no rejection):** `Draft` → `Submitted` → `Approved` → `Completed` | `Short Closed` | `Cancelled`
- **Item-level (with rejection):** `Draft` → `Submitted` → `Rejected` → `Submitted` → `Approved` → `Completed` | `Short Closed` | `Cancelled`

---

## K. Activity Log

### K.1 Storage and Display

- All field changes and status transitions are stored with acting user and timestamp.
- Storage granularity is **per field** (e.g., "Order Date changed from 2026-05-01 to 2026-05-08").
- Display on the PO view is a summarised, milestone-oriented list to avoid flooding the page; individual entries are expandable to reveal field-level detail.

### K.2 Events Logged

- PO status changes (created, submitted, approved, rejected, cancelled, completed).
- Item status changes (created, submitted, approved, rejected, cancelled, completed, short-closed).
- Modification of PO details (per field).
- Modification of item details (per field).
- Manual close (with actor and outcome).
- Auto-approval (acting user = system).
- Auto-cancel (acting user = system).
- Failed submission / failed approval validation errors.
- Rejection notes (PO-level and per-item) with actor.
- Cancellation notes (PO-level and per-item).
- Addition/removal of rejection marks during in-progress review.
- "PO cloned from [source PO Document ID]" on the new cloned PO (no entry created on the source PO).

### K.3 In-Progress Review Visibility

Activity log entries generated during an in-progress review are visible only to users with `purchaseorder_approve` until the review is formally completed. This includes auto-save and in-progress modification entries that happen while the PO is being reviewed.

### K.4 Budget Events

Budget consumption and release events are NOT logged in the PO activity log. They appear in the Budget Planning module's activity log.

---

## L. Notifications

### L.1 Delivery Channels

- In-app notifications and email.
- Email can be opted out per user (managed in the user module).

### L.2 Delivery Modes

Notifications operate in two modes concurrently:

- **Real-time** — delivered while the user is active (session open).
- **Login-based digest** — summarised and presented to each user on their next login. Used for ongoing-state reminders.

### L.3 Events and Recipients

| Event | Recipients | Mode |
|-------|-----------|------|
| PO needing approval (submitted) | Users with `purchaseorder_approve` | Real-time + login digest |
| PO not yet submitted (Draft) | Users with `purchaseorder_modify` / `purchaseorder_budgeted_modify` | Login-based digest (daily summary) |
| PO rejected | Users with `purchaseorder_modify` / `purchaseorder_budgeted_modify` | Real-time + login digest |
| Approved PO nearing `Cancel if not Delivered by Date` | Users with `purchaseorder_manual_close` / `purchaseorder_budgeted_manual_close` | Login-based digest |
| Partially received PO with expired `Cancel if not Delivered by Date` (when configured action is `Automatic Cancel` or `Notify Only`) | Users with `purchaseorder_manual_close` / `purchaseorder_budgeted_manual_close` | Login-based digest |

The Merchandiser is NOT notified when their PO is approved.

---

## M. Export

Users with any view-capable permission can export POs within their view scope.

### M.1 PDF Format

Designed for printing and sharing with vendors.

- Header: Organisation name/logo, Document ID, Status, Created By, Created Date, Approved By, Approved Date.
- Buyer details. Seller details.
- **PO Details:** Order Date, PO Type, Delivery Location, Facility List, Cancel if not Delivered by Date, Referenced Sales Order (if SOF), PO-level Notes (general).
- **Item table:** Line #, Product ID/GTIN, Product Name, Category, Vendor UOM label, Unit Cost, Ordered Qty, Extended Cost. For Cross Dock POs, one column per facility in the Facility List showing that facility's ordered quantity. All quantities in vendor's UOM regardless of screen display.
- **Item-level notes:** appear as a sub-row beneath the line item.
- **Totals:** Grand total extended cost, total number of line items, total ordered quantity (in vendor's UOM). No budget breakdown in totals.
- Signature block.
- Rejection notes are NOT included in the export.

### M.2 CSV Format

Designed for reporting / spreadsheet analysis. One row per item line.

- Columns: PO Document ID, PO Status, Order Date, PO Type, Buyer, Seller, Delivery Location, PO General Notes, (Facility columns for Cross Dock only), Product ID, GTIN, Product Name, Category, Vendor UOM, Unit Cost, Ordered Qty, Extended Cost, Item-level Notes, Item Status, Cancel if not Delivered by Date, Created By, Created Date, Approved By, Approved Date.
- **Facility columns:** Dynamic per-facility columns for Cross Dock POs (one column per facility in the Facility List). Absent for `Inventory Replenishment` and `Sales Order Fulfillment` POs.
- All quantities in vendor's UOM regardless of screen display.
- Rejection notes are NOT included in the export.

---

## N. Configuration (System Admin)

The following items are configurable per organisation by the system admin:

- Whether PO approval is required.
- Quantity entry unit for `Inventory Replenishment` POs (vendor's UOM or company's SKU).
- SOF pre-population mode (`Round Up`, `As Is`, `Manual`; default `Round Up`).
- Minimum threshold between Approval date and Order Date.
- Maximum allowed forward range for Order Date.
- Minimum gap between Order Date and `Cancel if not Delivered by Date`.
- Lead-time threshold for "nearing expiration" dashboard alert (default 5 days).
- Default currency (single currency per organisation; no multi-currency support).
- User session idle timeout (also used for edit/review lock release).
- Roles and the permissions assigned to each role.

No tax handling is performed on the PO.
