# 12 — Planning & Replenishment

Modulith module: `planning`. Six input modules feed one engine:

```
Assortment Plan  ─┐  (what a store ranges)
Demand Signal    ─┤  (how fast it sells)
Replenishment    ─┤  (how much to hold)
  Policy          │
Vendor Schedule  ─┤  (when a vendor can be ordered)
Order Calendar   ─┤  (exception dates)
Stock Position   ─┘  (what's there now — from inventory module)
        ↓
Auto Replenishment engine → one Store Replenishment Order per store per day
        ↓ manual confirm
CSV export (integration point I-2)
```

---

## 1. Assortment resolution model

An assortment is resolved per store with precedence **Store › Assortment Cluster ›
Company** — the most specific *active* plan wins. Plans are **single-level, full
replacement**: each plan is a complete SKU list for its target; a newer plan for the same
target supersedes the prior one on its effective date (no delta/merge semantics).

## 2. Assortment Plan

### 2.1 Fields

| Field | Rules |
|---|---|
| code | auto `AP-{seq}` (displayed `AP-0001`) |
| name | required |
| companyId | required |
| targetLevel | `Company (all stores)` · `Assortment Cluster` · `Store` ⚑ DR-11 |
| clusterId / branchId | the target: clusterId when level=Cluster, branchId when level=Store, none for Company |
| assortmentType | `Regular` · `Seasonal` · `Promotional` · `New-Store` |
| description | optional |
| effectiveFrom | required |
| status | `Draft` · `Scheduled` · `Active` · `Superseded` · `Inactive` |
| lines[] | productId + productCode + description; ≥ 1 line |
| skuCount | derived |

### 2.2 Lifecycle

- Save with future effectiveFrom → `Scheduled`; effectiveFrom ≤ today → `Active`.
- When a plan becomes Active, the previously Active plan **for the same target** becomes
  `Superseded` automatically.
- Two plans for the same target may not share the same effectiveFrom.
- **Versioning is duplicate-driven:** to revise a range, Duplicate the plan, adjust, and
  give it a new effective date.

### 2.3 Wizard

4 steps — **Identity & Scope → Effectivity → SKU List → Review**:
1. Name, company, assignment level, target (options depend on company+level), type,
   description.
2. Effective From with live status preview (future → Scheduled) and the supersede rule
   spelled out.
3. Ranged-SKU list (selected table) + product browser (category filter, search,
   checkbox multi-add).
4. Recap incl. affected-stores summary; `Save Assortment Plan`.

**API:** `GET/POST /assortment-plans`, `GET/PUT /{id}`; list filters name/company/cluster/status.

## 3. Demand Signal

Average Daily Demand (ADD) per **Store · SKU** — the rate the engine multiplies by days.

### 3.1 Model

```
Effective ADD = (Baseline + Lost-Sales Adjustment) × Overlay      …unless Overridden
```

| Component | Definition |
|---|---|
| Baseline | Trailing 28-day moving average of daily sales units, day-of-week weighted; recomputed daily after sales release (`15`) |
| Lost-sales adjustment | On stockout days, estimated missed units are added back so an out-of-stock SKU is not under-ordered next cycle |
| Overlay | Multiplicative uplift (e.g. ×1.8) for promo/seasonal windows, applied to its scope (SKU or category) × store group × date range |
| Override | Manual ADD value per Store·SKU that replaces the computed effective ADD while set |
| Confidence | `OK` · `Sparse` (thin history) · `New · seeded` (no history — seeded from a like-item / category rate until sales accrue) |

### 3.2 Overlays

| Field | Rules |
|---|---|
| ref | auto `OV-…` |
| scope | a SKU or a merchandise category |
| storeGroup | All stores / a zone / a store set |
| upliftFactor | e.g. 1.8 |
| dateRange | start–end |
| source | a published Promo **Price Event** (auto-created on publish, preferred) or `Seasonal calendar` (manual) |
| status | `Scheduled` · `Active` · `Expired` |

### 3.3 Screens & API

Two tabs: per-store Demand Signal table (baseline, lost-sales, overlay, effective, trend,
override, confidence; Override ADD action; Recompute Baselines action) and Overlays list.
`GET /demand-signals?branchId=…`, `PUT /demand-signals/{id}/override`,
`GET/POST /demand-overlays`, `POST /demand-signals/recompute`.

## 4. Replenishment Policy

Resupply parameters resolved by **Replenishment Profile × merchandise scope**.

| Field | Rules |
|---|---|
| profileId | required (Replenishment Profile) |
| itemScopeType | `Merchandise Hierarchy Node` · `Specific SKU` |
| scopeRef | node or SKU reference ⚑ DR-22 (target model: typed `merchNodeId`/`productId` instead of free text) |
| method | `Min / Max (reorder point)` · `Days-of-Supply` · `Top-off (order-up-to)` |
| target | method-dependent: Min & Max, target days, or order-up-to level |
| safetyStockDays | integer |
| serviceLevel | `95.0%` · `97.5%` · `98.0%` · `99.0%` |
| status | Active/Inactive |

**Resolution: most-specific-wins** — a SKU-level policy overrides a category-level one,
which overrides the profile default. Parameters are current-state (no dated versions).
Inventory & sales are runtime engine inputs, never stored here.

**API:** `GET/POST /replenishment-policies`, `GET/PUT /{id}`.

## 5. Vendor Schedule

When each vendor can be ordered — keyed **Company · Vendor**, no destination axis.

| Field | Rules |
|---|---|
| companyId, vendorId | required, must exist; pair unique ⚑ DR-23 |
| orderDays | set of weekdays (e.g. `Mon · Thu`); applies every week — no dated calendar, no cut-off time |
| leadTimeDays | integer; delivery = order day + lead time |
| deliveryDays | optional; blank ⇒ derived from order day + lead time |
| status | Active/Inactive |

Delivery Model and Min Order display read-only from the Vendor master.

**API:** `GET/POST /vendor-schedules`, `GET/PUT /{id}`.

## 6. Order Calendar (exceptions)

Optional overlay of exception dates on the weekly Vendor Schedule.

| Field | Rules |
|---|---|
| scope | `Company Holiday` (key: Company) · `Vendor Blackout` (key: Company · Vendor) |
| companyId | required, must exist |
| vendorId | required iff Vendor Blackout |
| date | required |
| type | `Non-order` · `Non-delivery` · `Both` |
| label | e.g. `Ninoy Aquino Day` |

**Effects:** a suppressed **order** day is skipped — demand accumulates to the next order
weekday; a **delivery** landing on a non-delivery date rolls forward to the next
allowed date.

**Screen:** two tabs — Company Holidays / Vendor Blackouts.
**API:** `GET/POST /order-calendar-exceptions`, `GET/PUT /{id}`.

## 7. Auto Replenishment (engine + Store Replenishment Orders)

### 7.1 Run model

- One automatic run daily at the configured time (default **06:00** Asia/Manila) plus
  ad-hoc `Run Now` with scope (company/store) and parameter overrides.
- Each run produces **one Store Replenishment Order (SRO) per store** — standalone,
  spanning **all vendors** (deliberate departure from per-vendor POs; confirmed product
  decision).
- Run record: date/time, store count, duration, log; visible Run History.

### 7.2 Line building (per store)

1. Start from the store's resolved assortment (§1) — the **full assortment** appears on
   the order.
2. A line is **due today** when its (Primary) vendor's schedule (§5) has an order day
   today, not suppressed by the Order Calendar (§6).
3. For due lines, compute need from the store **netting position**
   (`On-Hand + In-Transit + On-Order − Committed`, `14 §2`) vs the resolved policy (§4):

```
Suggested Qty = (ROP + Safety Stock + Review-Period Demand) − On-Hand − On-Order
  where demand terms come from Effective ADD (§3) × days
  → rounded UP to the vendor's order increment, never below MOQ
```

4. Non-due or not-below-target lines carry **suggested qty 0** (muted rows).
5. Lines whose expected margin is below the **margin-warning floor (15%)** are flagged —
   flag only, never a block or hold.
6. Items without a costed, orderable Vendor Product link are skipped and logged (`10 §6`).

### 7.3 Run configuration (read each run)

Run frequency (Daily / Weekdays), run time, respect Order Calendar (Y/N), enforce MOQ
(Y/N), round to order increment (Y/N), show full assortment 0-qty lines (Y/N), drop
zero-qty lines on confirm (Y/N), margin-warning floor (%). Ad-hoc runs may also set
coverage/review-period days, safety-stock days, service level, and respect-vendor-schedule.

### 7.4 SRO document

| Field | Notes |
|---|---|
| orderNo | `SRO-{branchCode}-{yyyyMMdd}` |
| branchId, runId, runDate | |
| assortmentSkuCount, dueTodayLineCount, orderValue | derived |
| status | `Pending Confirmation` → `Confirmed` → `Exported` (⚑ DR-24: also `Cancelled` for discarded runs) |
| lines[] | sku, description, vendor, order-day badge, on-hand, net position, ROP/target, suggested qty, **orderQty (editable)**, pack, unit cost, line value, flag |

### 7.5 Review & confirm (manual — no auto-release)

Every SRO waits for a buyer. The buyer may edit any Order Qty (raising a 0 line adds it;
zeroing removes it), Save Draft, then **Confirm & Export**: zero-qty lines are dropped,
status → Confirmed, `SroConfirmed` event fires, and the export file is produced
(→ Exported).

### 7.6 Export (integration point I-2)

CSV per confirmed order (one row per non-zero line: store, sku, vendor, qty, pack, unit
cost, line value, run date). Downloadable from the UI; automated hand-off to the legacy
system is the future integration.

**API:** `GET /replenishment-runs`, `POST /replenishment-runs` (ad-hoc),
`GET /store-replenishment-orders` (+filters), `GET/PUT /{id}` (draft qty edits),
`POST /{id}/confirm`, `GET /{id}/export`.
