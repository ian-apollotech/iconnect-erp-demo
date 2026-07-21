# Company / Zone / Store — JDA-faithful reconciliation

**Date:** 2026-07-17
**Status:** Approved (user), implementing in `ui-mockup/iconnect-erp.html`
**Design reference:** JDA / Blue Yonder location & pricing model + `docs/puregold/Puregold JDA ERP_1.pdf`. See `docs/puregold/jda-location-pricing-schema.mmd` / `.html`.
**Supersedes:** the 2026-07-16 "Banner→Chain + geographic Zone" amendment's Chain/Zone stubs.

## Problem

The PDF flattens two different things into the word "Zone": Module 8a lists *"Zone – 3 types: Price Club / Extra / Minimart."* Those three are **store formats**, not geography. JDA never conflates them — format is a **location trait**, while a pricing "zone" is a **many-to-many store-grouping** (`ZONE_GROUP → ZONE → ZONE_LOCATION`). Because they live in different structures, a store can carry a format **and** belong to geographic pricing zones at once — no either/or.

## Model

### Company — the entity that "is regarded as Chain"
PDF Module 1d: *"Company will be regarded as Chain… on terms of level."* So **Chain is not a separate master** — it is the Company acting as the **top pricing scope**. There is no Chain entity, table, or nav item.
- The mockup's **Chain nav slot is dropped** (option 1); its meaning is folded into Company with an explanatory note.
- Company holds legal name, TIN, currency, buyer identification.

### Zone — modeled as JDA Zone Groups
Zone is not a fixed list of three. It is **Zone Groups**, each containing Zones, each Zone grouping a set of stores. Ship two groups:

| Zone Group | Purpose | Zones |
|---|---|---|
| **Format** | matches PDF's "3 types" exactly | Price Club · Extra · Minimart · Merkado |
| **Region** | geographic pricing (2026-07-16 amendment intent) | NCR · N. Luzon · S. Luzon · Visayas · Mindanao |

A store belongs to **one Format zone and one Region zone** today; adding a third axis (Tier/Cluster — the PDF's undetailed Module 1c "clustering") is data entry, not a schema change.

**Name-collision resolution:** the amendment's "Chain = brand/format" is really the **Format zone/trait**; the pricing "Chain" is the **Company**. Two different things, two different homes. Therefore the store's brand/format attribute is labelled **Format** (not "Chain") throughout the store pages.

### Store (Branch) — the site, split into Inventory Locations
The Store is the JDA Location (type = Store): store code, name, **Format**, **Region**, cross-dock hub, primary/alt vendors, status.

**Two-grain inventory** (PDF "Difference from SCN ERP" §4a + Module 7): a store contains one or more **Inventory Locations**, each a first-class stock point (own balance, own receiving, inter-location transfer).

| Format | Inventory Locations |
|---|---|
| **Price Club** | 2 — Selling Area **+** Warehouse |
| **Extra** | 1 — Selling Area |
| **Minimart** | 1 — Selling Area |
| **Merkado** | 1 — Selling Area |

- Inventory is keyed at **(Item × Inventory Location)**; the store-level position = sum across children. One rule gives both "per-location" and "whole-store" views.
- Count is a **format-driven default at creation** (Price Club → 2, others → 1), not a hard constraint.
- **Merchandise Issuance** (Module 7d): Warehouse → Selling Area. Exists **only** where there are two locations (Price Club). Extra/Minimart receive straight to Selling Area — no issuance step.
- Receiving / Store-to-Store / RTV operate at the inventory-location grain.

## Pricing (no redesign)
Price/cost event scope = one of:
- **Chain** → `scope = Company` (all its stores)
- **Zone** → `scope = any Zone in any Group` — "all Extra" = the *Extra* zone in **Format**; "all Visayas" = *Visayas* in **Region**
- **Store** → `scope = Location`

Puregold's literal *Chain / Zone(=3 formats) / Store* = "price against the **Format** group by default"; Region pricing is available immediately with no rework.

## Mockup changes
1. **NAV** — remove `chain` item; keep `zone`, `branch` under "Store Branch Maintenance".
2. **PAGES.company** — relabel "Chains" column → "Formats"; add note "Company is the Chain (top pricing level)."
3. **PAGES.zone** — replace stub with Zone Groups (Format + Region) view: each group lists its zones, member-store counts, pricing usage; note explaining the JDA model + collision resolution.
4. **PAGES.branch** — "Chain" column/filter → "Format"; add "Inventory Locations" column (Selling Area / +Warehouse); add a Branch Detail panel with an Inventory Locations sub-panel; note the format-driven rule.
5. **PAGES.inventory** — note that the Warehouse view applies only to Price Club (2 locations); Extra/Minimart are Selling-Area-only.
6. **CREATE.branch** — "Chain" field → "Format"; add read-only "Inventory Locations" reflecting the format default.
7. **CREATE.company** — no structural change (Chain is Company).
8. Remove `PAGES.chain`.

## Out of scope
Zone Group admin CRUD; per-store inventory-location editing beyond display; multi-axis pricing beyond Format+Region.
