# Assortment Plan module — design spec

**Date:** 2026-07-20
**Target file:** `ui-mockup/iconnect-erp.html`
**Status:** Approved in brainstorming; pending user review of this written spec.
**Supersedes:** the `PAGES.assortplan` "design pending" stub (mockup lines ~482–484).

---

## 1. Purpose

Implement **store assortment assignment** — the master that declares *which SKUs a store carries*
("ranging" / "listing"). A given Company, Store Cluster, or Store is assigned a list of SKUs; those
are the items its stores range. This replaces the placeholder Assortment Plan page with a working module.

This is **assortment/ranging, not a planogram.** No shelf, facing, or dimension data. It is the JDA
merchandising "item/location authorization" concept, kept as a simple flat SKU list per plan.

## 2. Decisions locked (from brainstorming)

1. **Single-level "full replacement" model.** An Assortment Plan targets exactly ONE level:
   **Company** OR **Store Cluster** OR **Store**. It is a complete, self-contained SKU list — never a
   delta/merge on top of another level.
2. **Resolution precedence (read-side): Store › Store Cluster › Company.** A store's effective range is
   the plan of the most specific level assigned to it; if no store-specific plan, fall back to its Store
   Cluster's plan; else the Company default. Precedence is decided **by level, not by date**.
3. **Store Cluster** is the store grouping assignable to a plan (the assortment grouping — *not* Price
   Zone / Cost Zone, which are pricing/cost groupings).
4. **Effectivity = start date only, version-chained.** Each plan has an **Effective From** date and **no
   end date**. A newer plan for the *same level+target* supersedes the prior one on its start date. Two
   plans for the same level+target may not share the same start date.
5. **Create UX = 4-step wizard**, consistent with the existing Price/Cost Event wizards
   (`openPriceWizard` / `openCostWizard`).
6. **Duplicate is the primary "new version" mechanism.** Duplicating a plan seeds the wizard with the
   source plan's level, target, type, and full SKU list, so the user adds/removes a few items and sets a
   new Effective From while retaining the rest.
7. Include an **Assortment Type** classifier: Regular / Seasonal / Promotional / New-Store.

## 3. Non-goals (explicitly out of scope)

- **Planogram / shelf space planning** (JDA Space Planning / Intactix) — different module, not an ERP concern here.
- **Resolved Assortment viewer** (store + as-of date → flat computed list with provenance) — deferred to a later pass.
- **Delta / add-delete exceptions** (JDA-native cluster ± store exceptions) — rejected in favor of full replacement for interpretability.
- **Effective-To / end-dated windows** — dropped per decision #4.
- **Season/Period master field** — YAGNI for now (Assortment Type covers the classification need).
- No replenishment/order integration wiring; this is master-data authoring only.

## 4. Data model (conceptual — mockup is static, this documents intent)

**Assortment Plan (header)**

| Field | Notes |
|---|---|
| Code | Auto, e.g. `AP-0001` (mono) |
| Plan Name | Descriptive |
| Description | Free text |
| Company | Owning company (Puregold Price Club / Ayagold Retailers) |
| Level | `Company` \| `Store Cluster` \| `Store` |
| Target | The specific company / cluster / store (dependent on Level) |
| Assortment Type | Regular / Seasonal / Promotional / New-Store |
| Effective From | Date, required (no end date) |
| Version | Auto; increments across the same level+target chain |
| Status | Draft / Scheduled / Active / Superseded / Inactive |
| Ranged SKUs | Derived count |
| Affected Stores | Derived count (1 for Store; cluster member count for Cluster; all company stores for Company) |
| Audit | Created / modified by + date |

**Assortment Plan Line (per SKU):** SKU · Description · Category · UOM · line status.

**Status lifecycle:** `Draft` → `Scheduled` (start date in future) → `Active` (current, latest effective
for its target) → `Superseded` (a newer version for the same level+target has taken over) → `Inactive`
(manually retired).

## 5. UI

### 5.1 Navigation
Full module under the existing **Store Grouping** nav group, directly after Store Cluster. Keeps the
existing `data-route="assortplan"` binding and the "Open Assortment Plan →" button on the Store Cluster page.

### 5.2 List / landing page (`PAGES.assortplan`)
List-only landing (matching PCM/CCM). Columns:

`Code` · `Plan Name` · `Level` · `Target` · `Company` · `Ranged SKUs` · `Effective From` · `Assortment Type` · `Status`

- Filterbar: **Search · Company · Level · Status**.
- Header actions: **Export · New Assortment Plan**.
- Per-row **Duplicate** action (seeds the wizard — see 5.4).
- `noteBox` restating single-level resolution **Store › Store Cluster › Company** and that a plan fully
  replaces (never merges) at its level.

### 5.3 Create wizard (4 steps) — new `openAssortWizard(seed)`
- **Step 1 · Identity & Scope** — Plan Code (auto), Plan Name, Company, **Assignment Level**
  (Company / Store Cluster / Store) → **dependent Target picker**, Assortment Type, Description.
- **Step 2 · Effectivity** — **Effective From** (required). Live guard: block a start date already used by
  another plan for the same level+target; note that a later start supersedes the current plan. Derived
  Status preview (Scheduled/Active).
- **Step 3 · SKU List** — dual-list transfer picker repurposed from `storePicker` / `initStorePicker`
  (search by SKU / description / category, ›/‹ move, live counts). When seeded via Duplicate, the source
  plan's SKUs pre-populate the Selected side.
- **Step 4 · Review & Save** — summary (level, target, type, effective from, SKU count, affected-store
  count) + Save.

Wire into `openCreate`: `if(key==='assort'){ openAssortWizard(); return; }`.

### 5.4 Duplicate flow
Per-row **Duplicate** calls `openAssortWizard(assortSeed(code))`, pre-filling level, target, type,
description, and the full SKU list from the source plan; Code is regenerated and Effective From cleared for
the user to set. Mirrors the existing `priceSeed`/`costSeed` + `[data-dup]` convention.

## 6. Reuse of existing mockup conventions

- **Wizard shell:** model `openAssortWizard` on `openPriceWizard` (4-step, same nav/step chrome).
- **SKU picker:** repurpose `storePicker()` markup + `initStorePicker()` behavior as an item picker; add an
  `ITEMS` dataset (SKUs with description/category/UOM). `openCreate` already auto-inits any form containing
  `#storePicker`; the SKU step should follow the same auto-init hook.
- **Duplicate:** follow `priceSeed`/`costSeed` + `document.querySelectorAll('#main [data-dup]')` pattern
  with a distinct data attribute (e.g. `data-assortdup`).
- **Routing:** `PAGES.assortplan` already reachable via the generic `#main [data-route]` binding.

## 7. Verification (matches prior store-grouping work)

1. `new Function()` JS syntax gate after each edit.
2. Playwright render gate: all pages route, 0 runtime console errors (favicon 404 benign); exercise the
   Assortment Plan list, the 4-step wizard (level→target dependency, SKU picker add/remove/search,
   effective-from guard), and Duplicate seeding.
3. Residual-term grep: no "Design pending" / placeholder text remains on the page.

## 8. Open questions

None — all requirements confirmed with the user on 2026-07-20.
