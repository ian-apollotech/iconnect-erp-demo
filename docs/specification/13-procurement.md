# 13 — Procurement (Purchase Orders & Receiving)

Modulith module: `procurement`. Prototype coverage was front-end only; this module is
specified to target behaviour ⚑ DR-06 — all lifecycles below are normative.

---

## 1. Order channels

| Channel | Nature |
|---|---|
| **Regular PO** | Buyer-created vendor order to one ship-to (store or cross-dock hub) |
| **DSD** | Direct-to-Store Delivery — **no PO**; vendor (bread, ice cream…) delivers, store records receipt against the delivery invoice; logged for reconciliation |
| **One-Time-Buy (OTB)** | Fashion seasonal buy against an OTB budget; no auto-replenishment; markdown-managed to sell-through |
| **Multi-Store PO (MPO)** | One vendor, demand consolidated across N stores, split into store allocations at the cross-dock; buyer-created (never a replenishment output) |

## 2. Purchase Order

### 2.1 Header

| Field | Rules |
|---|---|
| poNo | auto `PO-…` |
| vendorId | required, Active vendor |
| orderType | `Regular` · `DSD (no PO)` · `One-Time-Buy (OTB)` |
| deliveryModel | `Direct-to-Store (DTS)` · `Cross-dock` |
| shipToBranchId / crossDockHub | required per delivery model |
| companyId (buyer) | required |
| orderDate, expectedDelivery | dates |
| paymentTerms | read-only from vendor |
| remarks | optional |
| status, version | lifecycle §2.3 |

### 2.2 Lines

| Field | Rules |
|---|---|
| productId | required — **active SKUs only** |
| uom / pack | from the item's orderable pack levels (vendor product link) |
| orderQty (cases) | > 0; rounded to order increment; ≥ MOQ where enforced |
| unitCost | from current cost resolution (`11 §3.5`); read-only |
| amount | derived |
| marginFlag | expected margin below minimum → guard warning |

**Line guards**
- Restricted or delisted items are **auto-dropped and logged** at submission — never
  silently ordered.
- Any line below the minimum-margin threshold flags the PO and forces the approval route.

### 2.3 Lifecycle

```
Draft → For Approval → Approved → In Transit → Received      (pre-approved → Cancelled)
```

- `submit` runs the line guards; `approve` requires Approve permission on Purchase Order.
- `Received` is set when receiving posts cover the PO (partial receipts keep it In
  Transit with received-qty tracking).
- A published cost event revalues open (unreceived) PO lines: report + flagged deltas.

### 2.4 DSD deliveries

Logged records: dsdNo `DSD-…`, vendorId, branchId, deliveryDate, vendor invoice no,
cases, value, status `Received` · `Qty Variance`. Receipt happens in Receiving (§4,
Selling Area tab) referencing the DSD record; variances flag for reconciliation.

### 2.5 OTB budgets

Per **season × department**: budget, committed, available; **unspent budget carries over**
to the next season for the same department (subject to approval), not forfeited.
OTB POs (otbNo `OTB-…`) consume budget on approval; budget-used bar shown per order.

### 2.6 Multi-Store PO

Header: mpoNo `MPO-…`, vendorId, crossDockHub, storeCount, totals; **allocations** per
store (branchId, allocated qty, value, delivery mode). Lifecycle:
`Draft → For Approval → Approved → Split & Shipped`. At the hub the ASN (§4.3) executes
the split; allocations become store shipments.

**API:** `GET/POST /purchase-orders` (+ `/submit`, `/approve`, `/cancel`),
`GET/POST /dsd-deliveries`, `GET/POST /otb-budgets`, `GET/POST /multi-store-pos` (+ transitions).

## 3. Dashboard stats

Open POs, committed value, due today, OTB budget left — drill into filtered lists.

## 4. PO Receiving

Receipt of goods via three receiving points, each a tab of the module:

### 4.1 Branch Warehouse

- GRN (`GRN-…`) against a PO: lines with ordered vs received qty; short/over variances
  badge the GRN (`None` · `n short` · …).
- Statuses: `Open` (capturing) → `Posted`.
- **On posting:** stock in at the Warehouse location; **item cost recomputed by weighted
  average** — blending received qty × cost with existing on-hand; the new moving-average
  cost feeds margin and open-PO revaluation. `GoodsReceiptPosted` event.
- Damaged-on-receipt units divert to RTV (if vendor allows) or adjustment.

### 4.2 Selling Area (direct-to-shelf)

Same GRN document with receiving point = a Selling Area location — goods booked straight
to the shelf (bypasses warehouse). Used by DSD and by formats without a warehouse.
Reference = PO or DSD record.

### 4.3 Cross-Dock (3PL · WMS)

Mother-PO (MPO) stock arrives at the 3PL hub, is cross-docked, and splits into store
shipments. Tracked by **ASN** records: asnNo `ASN-…`, motherPoRef, vendor, hub,
store count, cases in/out, WMS status (`Received @ Hub` · `Cross-docking` ·
`Dispatched`). Phase 1: statuses maintained manually/imported; live WMS sync is
integration point I-4. Store-side receipt of the split shipments happens as Warehouse/
Selling-Area GRNs referencing the allocation.

### 4.4 Fields (GRN)

| Field | Rules |
|---|---|
| grnNo | auto `GRN-…` |
| receivingPoint | `Branch Warehouse` · `Selling Area` · `Cross-Dock (3PL · WMS)` |
| reference | poNo / dsdNo / allocation |
| vendorId (ro from ref), branchId, receiveDateTime | |
| lines[] | productId, orderedQty, receivedQty (editable), uom |
| variance summary, status | `Open` · `Posted` |

**API:** `GET/POST /goods-receipts`, `GET/PUT /{id}`, `POST /{id}/post`; `GET/POST /asns`.

## 5. Permissions

Buyer: create/edit/submit POs; approver per matrix approves. Receiving Clerk:
create/post GRNs at their scoped branches. Scope filtering restricts store-level users to
their branches.
