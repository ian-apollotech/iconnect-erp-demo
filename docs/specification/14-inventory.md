# 14 — Inventory

Modulith module: `inventory`. Prototype coverage was front-end only; specified to target
behaviour ⚑ DR-06. All stock is held **per Inventory Location within a store**; every
movement writes the stock ledger.

---

## 1. Inventory Locations

| Field | Notes |
|---|---|
| branchId | owner store |
| type | `Warehouse` · `Selling Area` |
| name | e.g. `Warehouse`, `Chiller 3`, `Bakery`, `Grocery 7`, `Freezer 1` |
| status | Active |

- Seeded from the branch Format on `BranchCreated` (⚑ DR-15): Price Club → one Warehouse
  + one Selling Area; Extra/Minimart/Merkado → one Selling Area. Additional named selling
  areas may be added per store.
- The Warehouse view includes a **Top Load** bucket (cross-dock / overflow stock) shown
  as a separate column.

## 2. Stock positions & costing ⚑ DR-16

- **stock_ledger** — append-only movement journal: location, product, qty ±, movement
  type (receipt, sale, issuance, transfer-out/in, adjustment, RTV, conversion-in/out),
  unit cost, source document, timestamp.
- **stock_position** — current qty per location × product (maintained transactionally
  with each ledger write); reserved and in-transit quantities tracked from open documents.
- **Costing: moving (weighted) average** per product — recomputed on each receipt:
  `newAvg = (onHandQty × avgCost + rcvQty × rcvCost) / (onHandQty + rcvQty)`
  (in-transit stock included per the costing policy). Average cost values all positions
  and adjustment/shrink postings.

### Views (Product Inventory module)

| Tab | Grain | Columns / notes |
|---|---|---|
| **Store Netting Position** | Store · SKU (pooled across all locations) | On-Hand, In-Transit, On-Order, Committed, **Net Position = OH + IT + OO − Committed**, avg cost, position value. This is the position the replenishment engine reads (`12 §7`). |
| **Per Branch Warehouse** | Warehouse location · SKU | WH on hand, **Top Load**, reserved, available, in transit; `Transfer to Selling Area` action → Merchandise Issuance |
| **Per Selling Area** | Selling area · SKU | Shelf qty, capacity, fill %, reorder flag; `Replenish Shelf` action |

Internal **shelf replenishment** (Warehouse → Selling Area) is a separate process from
vendor replenishment.

## 3. Merchandise Issuance (Warehouse → Selling Area)

Document moving stock from a store's warehouse to one of its selling areas. Lines:
product, qty, target selling area. On post: ledger out/in within the store. Units flagged
**damaged on issue** divert to an RTV (if vendor RTV-allowed) instead of reaching the
shelf — otherwise to an adjustment.

## 4. Inventory Adjustment

Corrections from cycle counts, spoilage, theft/shrink, system errors — full audit trail
and approval.

| Field | Rules |
|---|---|
| adjNo | auto `ADJ-…` |
| branchId | required |
| entryMode | `Manual lines` · `Bulk upload (CSV / XLSX)` · `Annual physical count` |
| reason | `Cycle Count` · `Annual Physical Count` · `Spoilage` · `Shrink` · `System Correction` |
| adjustmentDate | |
| lines[] | productId, systemQty (snapshot), countedQty → ΔQty and value impact (at avg cost) derived |
| status | `Draft` → `For Approval` → `Posted` (Rejected → back to Draft) |

- **Bulk upload:** count file (CSV/XLSX) matched on item code; system computes variances.
- **Annual physical count:** store-wide snapshot of system-vs-counted; posts the net
  variance after approval.
- Posting writes ledger entries (± at average cost) and shrink/spoilage statistics
  (MTD shrink value, % of sales).

## 5. Store-to-Store Transfer

| Field | Rules |
|---|---|
| trNo | auto `TR-…` |
| fromBranchId, toBranchId | required, different |
| reason | `Rebalance` · `Promo Support` · `Near Expiry` |
| transferDate, lines[] (product, uom, qty) | |
| status | `Draft` → `In Transit` (stock out at source, in-transit) → `Pending Confirm` → `Received` (stock in at destination on confirmation) · `Cancelled` |

Honors in-transit accounting; destination must confirm receipt.

## 6. Return To Vendor (RTV)

| Field | Rules |
|---|---|
| rtvNo | auto `RTV-…` |
| vendorId | required — **only vendors flagged RTV-allowed**; for non-returnable vendors damaged/expired stock is written off via Inventory Adjustment instead |
| branchId, rtvDate | |
| reason | `Expired` · `Damaged` · `Recall` · `Overstock` |
| lines[] | product, uom, qty, unit cost |
| debitMemoNo | auto `DM-…`, generated at approval |
| status | `Draft` → `Approved` (debit memo issued) → `Pending Pickup` → `Picked Up` (stock out, payables reversal is future finance hand-off) · `Cancelled` |

## 7. Make-up / Break-up (Conversions)

Assemble promo bundles (make-up), split bulk into sellable units (break-up), and process
fresh cut-ups with yield.

| Field | Rules |
|---|---|
| convNo | auto `MK-…` (bundles/splits) / `FC-…` (fresh cut-up batches) |
| type | `Make-up (Bundle)` · `Break-up (Split)` · `Fresh Cut-up` |
| branchId, date | |
| inputs[] / outputs[] | component/source items with qty (ratio e.g. `3+1 → 1`, `1 → 5`); fresh cut-ups capture input kg, output cuts with kg |
| yieldPct, trimLossKg | fresh cut-ups: per batch; outputs re-costed from input cost ÷ yield |
| status | `In Progress` → `Posted` |

On post: ledger consumes inputs, produces outputs; costs flow input→output
(bundle cost = Σ components; cut-up outputs weighted by yield).

**API:** `/stock-positions` (three view params), `/merchandise-issuances`,
`/inventory-adjustments` (+ `/submit`, `/approve`, `/post`), `/stock-transfers`
(+ transitions), `/rtvs` (+ transitions), `/conversions` (+ `/post`).
