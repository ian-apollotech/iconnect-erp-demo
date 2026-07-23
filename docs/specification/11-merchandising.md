# 11 — Merchandising (Price & Cost Change Management)

Modulith module: `merchandising`. Both modules are **event-based**: prices and costs are
never edited in place on the product — they change only through dated, approvable
**events** that target a level (Company / Zone / Store). The module also answers the
resolution question "what is the retail/cost of SKU X at store Y today?".

---

## 1. Shared lifecycle ⚑ DR-03

```
Draft → For Approval → Approved → Published            (any pre-published → Cancelled)
```

| Status | Meaning |
|---|---|
| Draft | Editable working document |
| For Approval | Submitted; locked for edit; awaiting approver |
| Approved | Approved, waiting for its effective date |
| Published | In force from its effective date (POS/price/cost tables updated) |
| Cancelled | Terminal; no effect |

- `submit` requires ≥ 1 line and all required header fields.
- `approve` requires the **Approve** permission on the module (`16 §3`); an event that
  trips a guard (margin) *must* pass through approval — it cannot be published directly.
- `publish` emits `PriceEventPublished` / `CostEventPublished`.
- Effective behaviour is date-driven: publishing before the effective date schedules it;
  the price/cost becomes current on the effective date.

## 2. Price Change Management (Price Events)

### 2.1 Header

| Field | Rules |
|---|---|
| eventNo | auto `PC-…` |
| description | required |
| pcaType | `Regular (no end date)` · `Promo (auto-reverts on end date)` |
| companyId | required — the Chain the event belongs to |
| priceLevel | `Company (all stores)` · `Price Zone` · `Store` |
| scopeRef | dependent on level: none (Company, scope = "All Stores"), a Price Zone of that company, or a Store of that company |
| effectiveDate | required. For Promo the label is **Start Date** |
| promoEndDate | required **iff** pcaType = Promo; must be ≥ start date |
| status, version | lifecycle |

### 2.2 Lines

| Field | Rules |
|---|---|
| productId | required; active SKUs only |
| currentRetail | snapshot at entry (display) |
| newRetail | required, > 0; VAT-inclusive |
| deltaPct | derived `(new − current)/current` |

### 2.3 Wizard (create & duplicate)

4 steps — **Header → Add Items → Set Prices → Review & Submit**:
1. Header fields; PCA-type conditionally shows/requires Promo End Date and relabels
   Effective→Start Date; Scope options rebuild from Company + Level (Company level
   disables the scope picker).
2. Item browser (category filter + search, checkbox multi-add; already-added items are
   excluded).
3. Editable New Retail per line with live Δ%; remove lines.
4. Read-only recap of header + priced lines; `Submit for Approval`.

**Duplicate** on any list row seeds a new wizard from the source event (description
"(copy)", same scope/lines; prices pre-set for review).

### 2.4 Rules

- Promo events **auto-revert**: on promoEndDate+1 the promo price stops applying and the
  underlying regular price resumes (no reversal event needed).
- Publishing feeds POS (future integration) and creates a **demand overlay** for promo
  events (`12 §3`).
- List page: recent events with type/level/scope/#items/effective/status + Duplicate;
  supporting reports: Daily Price Report, Price History.

### 2.5 Resolution (current retail)

For SKU × store on date D, pick the price from published events where the store is in
event scope and effective ≤ D (promo also end ≥ D), by precedence:

**Store > Price Zone > Company (Chain)**; within a level, the latest effective wins;
an in-window Promo beats Regular at the same level. `product.baseRetail` is the fallback
when no event applies. ⚑ DR-21

`GET /current-prices?branchId=&productId=&date=` exposes the resolution.

## 3. Cost Change Management (Cost Events)

### 3.1 Header

| Field | Rules |
|---|---|
| eventNo | auto `CC-…` |
| description | required |
| vendorId | required — a cost event belongs to **one vendor** |
| companyId | required |
| costLevel | `Company (all stores)` · `Cost Zone` · `Store` |
| scopeRef | dependent, as PCM |
| effectiveDate | required |
| sourceDocument | vendor advisory / price letter reference |
| status, version | lifecycle |

### 3.2 Lines

| Field | Rules |
|---|---|
| productId | required; must be supplied by the event's vendor (has a Vendor Product link) |
| oldCost | snapshot |
| newCost | required, > 0 |
| deltaPct | derived; **cost delta colours inverted** (increase red) |
| retail (ref), newMarginPct | derived: `(retail − newCost)/retail` |
| marginFlag | `OK` (≥18%) · `Watch` (15–18%) · `Below Min` (<15%) |

### 3.3 Wizard

Same 4-step shape — **Header → Add Items → Set Costs → Review & Submit** — with the item
browser **pre-filtered to the event vendor's items**, and step 3 showing live margin +
flag per line. Review shows a warning when any line is Below Min. **Duplicate** as PCM.

### 3.4 Rules

- **Minimum-margin guard (15%, configurable):** lines below the floor are flagged and the
  event **routes for approval** — a warning, never an auto-block.
- On publish (`CostEventPublished`):
  - `vendor_product.costPerCase` for affected Company·Vendor·SKU links updates (CCM is
    the only writer of that field).
  - Open POs on affected lines are revalued (report + optional reprice, `13 §2`).
  - Margin recalculation across affected items.
- **Weighted-average on receipt is separate:** the event sets the *vendor cost*; the
  *inventory (moving-average) cost* is re-derived at each goods receipt by blending
  received qty/cost with on-hand (`13 §4`, `14 §2`).
- `Margin Alert` display state on the list = event with Below-Min lines pending decision.

### 3.5 Resolution (current cost)

As PCM but over cost events: **Store > Cost Zone > Company**, latest effective wins;
fallback `product.standardCost`. `GET /current-costs?...`. ⚑ DR-21

## 4. Permissions

Pricing Analyst: create/edit/submit both event types. Approver (per matrix, e.g. Category
Manager): approve/publish. Buyers: read costs. All others read-only where granted.
