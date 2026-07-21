# Store Grouping Redesign — Price Zone / Cost Zone / Store Cluster / Replenishment Policy

**Date:** 2026-07-17
**Status:** Approved (user), ready for implementation planning
**Target:** `ui-mockup/iconnect-erp.html` (single-file vanilla JS)
**Design reference:** JDA / Blue Yonder location, pricing, assortment & replenishment model + `docs/puregold/Puregold JDA ERP_1.pdf`.
**Supersedes:** the 2026-07-17 "Company / Zone / Store — JDA-faithful" design's single **Zone** page (Format + Region two-panel). That page is retired by this spec.

---

## Problem

The prior Zone implementation collapsed **store attributes** (Format, Region) and **store groupings** into one hardcoded "Zone Groups" page (two fixed panels, fixed rows, an Exclusive/Overlapping membership flag). It read as messy, non-extensible, and conflated concepts. It also modeled zones as *pricing-only*, when JDA uses store-groupings across pricing, cost, assortment, and replenishment.

## Key research findings (grounding)

- **PDF Module 1c:** a company has "store branches, **clustering**, pricing and ordering" — clustering is a first-class concept.
- **PDF Module 2b:** a store maintains its **"pricing zones"** (plural) and cross-dock.
- **PDF Module 8a (Pricing):** event levels = Chain (all stores/company) / **Zone (3 types: Price Club, Extra, Minimart)** / Store.
- **PDF Module 9 (Cost):** same Chain/Zone/Store event levels.
- **JDA reality:** a store-grouping is a general primitive consumed by many modules, each with a *purpose*. Conventional per-purpose ERP/JDA names:
  - Pricing → **Price Zone**
  - Cost → **Cost Zone**
  - Assortment → grouping = **Store Cluster**, operational SKU list = **Assortment Plan** (JDA: Arthur/Assortment Optimization "Assortment"; Intactix "Planogram" is the separate shelf-layout object)
  - Replenishment → **Replenishment Policy** (JDA E3 → Advanced Store Replenishment / A&R lineage; the parameter set is a "policy", not a "profile")

## Decisions (locked)

1. **Four purpose-named grouping masters** replace the single Zone page: **Price Zone · Cost Zone · Store Cluster · Replenishment Policy**.
2. **Present separately in the UI** (not one unified "Store Group" object). Rationale: with AI-generated code the DRY savings of a single abstraction are largely gone; separate presentation gives each purpose its native domain vocabulary and lives near the module that uses it. A light *shared convention* (each = "a store points to a named group for purpose X") is kept only so the store record can show all four coherently.
3. **Two-layer split:** the grouping master (stable set of stores) is separate from the dated/approved operational record that targets it (which lives on its own page).
4. **Format & Region stay as store attributes** on the Branch record (already present) — they are *not* groupings; groupings may reference them as a descriptive "Basis".
5. **Naming is JDA-faithful:** Store Cluster → Assortment Plan; Replenishment Policy (single merged object). "Assortment Zone"/"Replenishment Profile"/"Replenishment Rule" rejected.

## Model

### The four grouping masters (4 new menus)
Flat list, **no drill-down**, "New …" button. Shared basic details on every record: **Name · Code · Status · Company · Assigned stores** (rendered as *count + small sample*, e.g. "268 stores (0102, 0107, 0233 …)"), plus native columns:

| Menu | Native columns | Layer-2 operational page |
|---|---|---|
| **Price Zone** | Basis (Format / Region / Manual) · **# active Price Events** | Price Change Mgmt *(exists)* |
| **Cost Zone** | Basis · Vendors using · **# active Cost Events** | Cost Change Mgmt *(exists)* |
| **Store Cluster** | # ranged SKUs · **# Assortment Plans** | Assortment Plan *(stub — see Build scope)* |
| **Replenishment Policy** | Method (Min/Max · Days-of-Supply) · Review cadence · Source (DC / Direct) · Lead time | — *merged: stores + params on this page* |

**Two-layer mapping:**

| Purpose | Layer 1 — grouping master (new menu) | Layer 2 — operational record (own page) |
|---|---|---|
| Price | Price Zone | Price Event (Price Change Mgmt) |
| Cost | Cost Zone | Cost Event (Cost Change Mgmt) |
| Assortment | Store Cluster | Assortment Plan (stub for now) |
| Replenishment | Replenishment Policy | — merged (no dated "replen event" stream) |

The grouping page **reads** usage (a count of active events/plans); it does **not** create events. Events are created on the dedicated operational page, which picks a group as its target scope.

### Store (Branch) reciprocal view
- Add a **"Grouping Assignments"** block to the Branch detail panel: this store's Price Zone / Cost Zone / Store Cluster / Replenishment Policy.
- `CREATE.branch` gains four pickers (Price Zone, Cost Zone, Store Cluster, Replenishment Policy).

## Retire & repoint

- **Remove** `PAGES.zone`, the "Zone" nav item, and `CREATE.zone`.
- **Price Event scope** picker → Company / **Price Zone** (from the Price Zone list) / Store.
- **Cost Event scope** picker → Company / **Cost Zone** / Store.
- **RBAC access scope** "By Zone" → **"By Region"** (access control is geographic; references the store's Region attribute). Update `scopeCell`, `BA_SCOPES`, `SCOPE_VALUES`, Branch Access page, and CREATE.user Branch Access accordingly.
- **Replenishment module** references → **Replenishment Policy**.

## Seed data (no regression vs today)

- **Price Zone** seeds with current pricing basis: Price Club · Extra · Minimart · Merkado (Format-based), plus example Region zones (e.g. Visayas, NCR).
- **Cost Zone** seeds similarly (will look near-identical to Price Zone initially — correct, they may diverge). Add a one-line note on each page explaining this so it doesn't read as duplication.
- **Store Cluster** and **Replenishment Policy** seed with a couple of illustrative rows each.

## Navigation

Under **Master Data**, replace the single "Zone" entry with a **"Store Grouping"** sub-group containing the four menus. "Store Branch Maintenance" keeps Branch (and Region as a store attribute).

## Build scope

**In scope (this pass):**
- Four grouping master pages (Price Zone, Cost Zone, Store Cluster, Replenishment Policy) + their `CREATE.*` forms.
- Branch "Grouping Assignments" block + CREATE.branch pickers.
- Retire old Zone page/nav/CREATE; repoint Price/Cost event scope, RBAC "By Zone"→"By Region", replen references.
- Nav "Store Grouping" sub-group.
- Seed data + explanatory notes.

**Stubbed (deferred):**
- **Assortment Plan operational page** — Store Cluster's "Assortment Plan" link points to a "design pending" stub (`PAGES.assortplan` stub). The mockup has no assortment module today; full build is a later pass.

## Out of scope

- Trait/attribute admin CRUD (Format/Region remain fixed store attributes).
- Rule-engine auto-membership — "Basis" is a descriptive column only in the mockup, not a live query.
- Promo & Reporting groupings (deferred per user; same mechanism, added later as data/purpose).
- Full Assortment Plan and any planogram/space object.

## Verification

- JS `node --check` on the file.
- Playwright click-through: home, company, branch, the four new grouping pages, price, cost, users, branchaccess.
- Grep for residual "Zone" that should now be Price/Cost Zone or Region; confirm old `PAGES.zone`/`CREATE.zone`/"Zone" nav removed; confirm RBAC shows "By Region".
