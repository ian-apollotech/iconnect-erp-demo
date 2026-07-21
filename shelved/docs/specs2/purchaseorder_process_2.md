# Purchase Order Process Flow

## A. Actors
1. Merchandiser : creates purchase order
2. High Value Merchandiser : Adds item in purchase order that is under a category with a limited budget.
3. Merchandise Supervisor : approves purchase order

Actions are guarded by permissions. The system admin assigns what permissions each role has, making the roles of the actors defined by the system rather than hardcoded.

---

## B. Create Purchase Order

### 1. Fields to set:

- **Buyer** : Company that will be making the purchase. Determines the budget (for items that are under a budget) for the purchase order. Limits the Facilities that the order will be created for. This is from a list of companies under the organization. A purchase order is always set for one Buyer (Company) to one Seller (Vendor).

- **Seller** : Vendor of the items to purchase. This is from the list of vendors that the selected company has a relationship with. The Buyer-Seller relationship determines the terms of the purchase order including cost, minimum order quantity, order multiples, schedule, and UOM/SKU ordering configuration for each of the items the seller sells to the buyer.

- **Order Date** : Date for the order. The Order Date is used to fetch all date-sensitive data for the PO including prices, costs, Buyer-Seller relationship terms, and item schedules. Changing the Order Date on a Draft PO causes the system to automatically re-fetch and re-validate all date-sensitive data. All date-sensitive values are locked after the PO is approved.

- **Cancel if not Delivered by Date** : Optional. A failover date for the order. The action taken when the date expires is configurable with three options:
    - `Automatic Cancel` — cancels the entire PO if no items have been received. Partially received POs are not affected. Processed as part of a daily end-of-day system job.
    - `Notify Only` (default) — notifies all users who have access to the PO.
    - `Do Nothing` — no automated action is taken.
    An alert is shown on the dashboard for approved POs nearing this date based on a configurable lead time (default: 5 days).

- **Type of Purchase Order** :
    - `Inventory Replenishment` are orders to replenish the inventory of the selected Facility. This is the default type when a single Facility is selected.
    - `Cross Dock Order` are orders to replenish the inventory of multiple Facilities. Inventory will automatically be allocated to be dispatched to the other Facilities. Setting multiple Facilities automatically sets the Purchase Order Type to `Cross Dock Order`.
    - `Sales Order Fulfillment` are orders specifically to fulfill a sales order. Inventory will automatically be allocated to the Sales Order specified for the purchase order.

- **Facility List** : The facilities that are the recipients of the order. This is from the list of facilities under the selected company (unlimited). Setting multiple Facilities automatically sets the Purchase Order Type to `Cross Dock Order`.

- **Delivery Location** : The single facility location where the order will be delivered. May be any facility under the selected company. Defaults to the first facility selected in the Facility List.

- **Referenced Sales Order** : Only shown and required when PO Type is `Sales Order Fulfillment`. The Sales Order that this purchase order is created to fulfill. Cannot be changed after the PO is created.

- **Notes** : Additional notes related to the purchase order. This is for general instructions specific to the PO and is separate from rejection notes.

- **Attached Files** : Documents related to the order.

### 2. Automatically set fields by the system:

- **Document ID** : System-generated human readable identifier. Draft POs use the pattern `DFTPO-'yyyyMM'XXXXXX` generated when the PO is first saved. Upon submission the Draft ID is replaced by `PO-'yyyyMM'XXXXXX`. Both sequences use separate counters that reset monthly and are global across all companies in the organization.

- **Status** : Current status of the purchase order. See Section I for all PO-level and item-level statuses.

- **Created By** : User who created the document.

- **Created Date** : Date when the document was created.

- **Updated By** : User who last updated the document.

- **Updated Date** : Date when the document was last updated.

### 3. Add items for the Purchase Order

- Items are from the list of items that the vendor sells to the company. Items can be added one by one by searching with the product name, GTIN, or product ID. Multiple items can be added by selecting from a filtered list of products within a selected category. Items are filtered by the day-of-week schedule set by the vendor, based on the day of the week of the Order Date. An item not scheduled for the Order Date's day of week cannot be added.

- Items belonging to a category with a set budget can only be added by an authorized High Value Merchandiser.

- A purchase order of type `Sales Order Fulfillment` can only add items listed in the referenced Sales Order. Quantities for SOF items are pre-populated from the referenced Sales Order: quantities are first converted from the company's SKU unit to the vendor's UOM, then adjusted upward to satisfy the minimum order quantity and order multiple for that item.

- Quantity is based on the `unit of measure (UOM)` set by the vendor for the product. The UOM/SKU ordering configuration is set per Buyer-Seller relationship. The system maintains a UOM conversion table for the organisation. `Minimum order quantity` and `order multiple` are set from the vendor's UOM and are always displayed in the vendor's UOM regardless of the configured ordering unit.

- Quantity to order is limited by the `minimum order quantity` and `order multiple` set by the vendor:
    - If the quantity entered is below the minimum order quantity, the system blocks the action and shows the minimum required quantity.
    - If the quantity is not a valid order multiple, the system blocks the action and suggests the nearest rounded-up valid value.
    - For multi-facility orders, both rules are enforced on the **aggregate total quantity across all facilities**, not per individual facility.

- Quantity to order is also limited by the set budget for the category if the category has a defined budget. The total cost for all items ordered per category must not exceed the set budget. Only a Merchandiser authorized to order items under a category with a budget can set the quantity for these items. Budget is defined per company per category and per budget period. The Order Date determines which budget period applies.

- Budget availability is validated at submission and **blocks submission** if the category budget would be exceeded. Budget is consumed only upon PO approval on a first-come-first-served basis. Budget is re-validated at the time of approval against the then-current available budget. If an approved PO is cancelled or manually closed with unreceived items, the consumed budget for unreceived quantities is released.

- Quantity to order for each facility can be individually set for multi-facility orders.

- Total ordered quantity and cost of the purchase order are automatically updated.

- Items can be added and removed until the order has been submitted for approval. On a Rejected PO returned for revision, items may also be added and removed before resubmission.

- Only one merchandiser can actively edit a PO at a time. Other merchandisers who open the PO while it is being edited are placed in read-only mode. The edit lock is released automatically when the editing merchandiser navigates away from the page or their session expires.

### 4. Saving, Submitting, and Cancelling

- The Merchandiser submits the purchase order for approval after completing all details.

- The purchase order can be saved as a Draft if the Merchandiser still needs time to complete the details. The system validates items against the Buyer-Seller relationship and item schedule when saving to Draft. If validation fails, the Draft is **saved with errors highlighted** and an activity log entry is created.

- Submission is **hard-blocked** until all validation errors are resolved (invalid schedule, expired Buyer-Seller relationship, budget exceeded). The Draft is saved with errors highlighted and an activity log entry is created for the failed submission attempt.

- The Merchandiser can cancel a Draft purchase order to discontinue processing.

- A rejected PO can be edited (items may be added, removed, or modified) and resubmitted for approval. There is no limit on the number of rejection and resubmission cycles.

- Cancelled and Completed POs can be cloned to create a new Draft PO. `Sales Order Fulfillment` POs cannot be cloned. A cloned PO copies all header fields **except** Order Date and Cancel if not Delivered by Date (which must be set by the user), and copies all items and quantities. Cloned items are immediately re-validated against the current Buyer-Seller relationship and schedule; the cloned PO is saved as a Draft with any errors highlighted.

---

## C. Approving Purchase Order

1. When the system is configured to require approval, a Merchandiser Supervisor reviews the submitted purchase order. The supervisor reviews the PO header details and summary, and may then review items individually. The supervisor may:
    - **Approve the PO** — triggers system validation. If validation passes, the PO and all items are set to `Approved`. If validation fails, the system returns to the review page with errors highlighted and labeled with the date they were last checked. The supervisor may then reject the PO or leave it as `Submitted` while the underlying issue is resolved externally (e.g., by correcting master data).
    - **Reject the PO** — see Section D: Rejecting Purchase Order.
    - **Leave it as Submitted without action** — the PO remains in `Submitted` status. Validation errors from a previous failed approval attempt are retained and shown as a starting point for the next supervisor who opens the PO for review.

2. Merchandiser Supervisor can also reject the submitted purchase order (see Section D: Rejecting Purchase Order). The Merchandiser checks the rejected purchase order, makes the required changes, and resubmits for approval. When a rejected PO is resubmitted, fields changed by the Merchandiser since the last rejection are highlighted for the supervisor's convenience.

3. Merchandiser Supervisor can cancel the submitted purchase order to discontinue processing.

4. Only one supervisor can actively review a PO at a time. Other supervisors who open the PO while it is under review are placed in read-only mode. The review lock is released automatically when the reviewing supervisor navigates away from the page or their session expires.

5. If the system is configured not to require approval, the system automatically approves the purchase order when submitted by the Merchandiser. An approval record is created in the activity log with an indication that the PO was system-approved.

---

## D. Rejecting Purchase Order

1. A Merchandiser Supervisor can reject a submitted purchase order. Rejection marks for items and PO details are saved persistently as the supervisor reviews the PO. The supervisor can add and remove rejection marks at any time before formally submitting the rejection.

2. Rejecting a purchase order because of its header details or the purchase order as a whole sets the PO status to `Rejected` while item statuses remain `Submitted`. A **PO-level rejection note** is required to describe the reason for the rejection. This note is separate from the PO's general Notes field.

3. Rejecting a purchase order because of certain items requires marking each affected item as rejected with a note on each item describing the reason. Items with no issues remain in `Submitted` status. The PO status is set to `Rejected`. When the Merchandiser fixes and resubmits the PO, all items return to `Submitted` status.

4. The supervisor may also mark items as rejected due to system-identified validation errors (e.g., invalid schedule, expired Buyer-Seller relationship) that surface when the supervisor attempts to approve. These items receive `Rejected` status; items with no validation errors remain `Submitted`.

5. A PO-level rejection note can be provided in addition to per-item rejection notes. Both types of notes may coexist on the same rejection.

6. Rejection marks on items or PO details can be removed by the supervisor before formally submitting the rejection. If all rejection marks are removed, the supervisor may proceed to approve the PO. Once the supervisor formally submits the rejection, all rejections are final and cannot be removed.

7. An activity log entry is created for each rejection mark added or removed, and a separate entry is created when the supervisor formally submits the rejection. Activity log entries from an in-progress review are visible only to users with supervisor-level permissions until the review is formally completed (approved or rejected). Upon completion, the full review activity log becomes visible to all users with access to the PO.

---

## E. Closing Purchase Order

1. The system automatically marks the purchase order as `Completed` after all items have been fully received. This is determined by comparing the total received quantities recorded across all linked Receiving Advice documents against the ordered quantity for each item. For Cross Dock Orders, receipt at the Delivery Location is sufficient to trigger auto-completion.

2. A Merchandiser can manually close a purchase order at any time after it has been approved. The user is prompted to confirm the action as it is irreversible. The outcome depends on receipt status at the time of closing:
    - **One or more items received (fully or partially):** PO status is set to `Completed`. Each item's status is set based on received quantity — `Completed` (fully received), `Short Closed` (partially received), or `Cancelled` (not received). Remaining consumed budget for unreceived items is released.
    - **No items received:** PO status is set to `Cancelled`. All items are set to `Cancelled`. All consumed budget is released.

3. Only an authorized High Value Merchandiser can manually close a purchase order that contains items belonging to a category with a set budget.

4. A Completed or Cancelled PO cannot be reopened.

---

## F. Referenced Document

Referenced documents have a link from the purchase order to quickly access and view the related document.

1. Attached files to the purchase order.
2. Sales Order referenced to the purchase order for type `Sales Order Fulfillment`. The referenced Sales Order cannot be changed after the PO is created.
3. Receiving Advice documents done to receive items in the purchase order.
4. Returns Announcement documents done to return items in the purchase order.

---

## G. View of Purchase Order

1. Purchase order quick summary and next immediate action.
2. Buyer details
3. Seller details
4. Purchase order details
5. Purchase order totals
6. Activity log history (quick summary view with expandable detail)
7. Item list (sortable, searchable; per-facility quantities shown for multi-facility orders)

---

## H. Dashboard

Viewable only for those who have permissions to view the PO or can perform the action being notified.

1. Number of PO not submitted.
2. Number of PO needing approval.
3. Number of PO that were rejected and need revisions.
4. Number of approved PO with no items received.
5. Number of approved PO partially received.
6. Number of approved PO nearing expiration of `Cancel if not Delivered by Date` (based on configured lead time).

---

## I. Statuses

### PO-Level Statuses

| Status      | Description |
|-------------|-------------|
| `Draft`     | PO is being created and has not been submitted. Identified by Document ID prefix `DFTPO-`. |
| `Submitted` | PO has been submitted for approval. Identified by Document ID prefix `PO-`. |
| `Approved`  | PO has been approved and is pending receipt of items. |
| `Rejected`  | PO has been rejected by the supervisor and is pending Merchandiser revision. |
| `Completed` | PO has been closed with at least one item received (auto or manual close). |
| `Cancelled` | PO has been cancelled. Occurs when: Merchandiser cancels a Draft; Supervisor cancels a Submitted PO; PO is manually closed with no items received; or system auto-cancels via `Automatic Cancel` when `Cancel if not Delivered by Date` expires and no items have been received. |

### Item-Level Statuses

| Status         | Description |
|----------------|-------------|
| `Draft`        | Item is part of a Draft PO. |
| `Submitted`    | Item is part of a Submitted PO, or is part of a Rejected PO and has no item-level rejection. |
| `Approved`     | Item has been approved and is pending receipt. Item remains `Approved` until the PO is closed. |
| `Rejected`     | Item has been flagged by the supervisor (judgment call) or failed system validation during approval. Returns to `Submitted` when the Merchandiser resubmits the PO after resolving the issue. |
| `Completed`    | Item has been fully received (received quantity equals ordered quantity). |
| `Short Closed` | Item has been partially received when the PO was manually closed. |
| `Cancelled`    | Item was not received when the PO was closed or cancelled. |

### Status Transition Summary

**PO-level:** `Draft` → `Submitted` → `Approved` → `Completed` or `Cancelled`
Alternate paths: `Submitted` → `Rejected` → `Submitted` (repeat until resolved); `Draft` or `Submitted` → `Cancelled`

**Item-level (no rejection):** `Draft` → `Submitted` → `Approved` → `Completed` / `Short Closed` / `Cancelled`

**Item-level (with rejection):** `Draft` → `Submitted` → `Rejected` → `Submitted` → `Approved` → `Completed` / `Short Closed` / `Cancelled`
