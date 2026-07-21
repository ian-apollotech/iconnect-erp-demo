# Amendment: Banner → Chain rename + geographic Zone reinstatement

**Date:** 2026-07-16
**Amends:** `2026-07-16-iconnect-erp-spec-reconciliation-design.md` (§1, §4.4, §5.1) and its implementation context.
**Status:** Approved by user, implemented in `ui-mockup/iconnect-erp.html`.

## Why

The reconciliation had made **Banner** (Price Club / Extra / Minimart / Merkado) the pricing/cost
grouping level and **dropped geographic Zone**. The user reversed this: Banner is a store
brand/format, not a pricing axis; pricing/cost should group geographically again.

## Locked decisions (this amendment)

1. **Top / legal-entity level = "Company" only.** The label "Chain" is no longer used for the
   legal entity anywhere (filters, RBAC scope, price/cost level, branch-access scope).
2. **Banner → Chain.** Price Club / Extra / Minimart / Merkado are now called **Chains** (the store
   brand/format). Brand badge colours unchanged (Price Club=blue, Extra=cyan, Minimart=gray,
   Merkado=amber).
3. **Banner is removed as a pricing/cost grouping level.** Chain (brand) is **not** a pricing axis.
4. **Pricing/cost grouping = Company → Zone → Store.**
5. **Zone = geographic, reinstated.** Values: **NCR / N.Luzon / S.Luzon / Visayas / Mindanao**
   (5-zone set, Visayas and Mindanao split). Appears on: Store Branch list (column + filter),
   Price & Cost pages (middle level + scopes), CREATE.branch, CREATE.price/CREATE.cost, and
   **RBAC access scope** (Company-wide / By Zone / By Store).

## Store → Zone mapping (sample data)

| Zone | Stores |
|------|--------|
| NCR | Ortigas, Cubao, Baclaran, Tandang Sora, Kamias, Katipunan, UP Town, Vertis North, Ayala Marikina |
| N.Luzon | Clark (Pampanga), Marcos Hwy (Baguio) |
| S.Luzon | Brookside Cainta (Rizal) |
| Visayas | Mandaue (Cebu) |
| Mindanao | Bajada (Davao) |

## Changed surfaces (implementation)

- Dashboard copy "3 banners" → "3 chains".
- Company Maintenance: "Banners" count column → "Chains".
- Store Branch Maintenance: header/filters; "Banner" col → "Chain"; new "Zone" col;
  "Company (Chain)" filter → "Company"; added Zone filter.
- Item Maintenance: "Banner Price Override" → "Zone Price Override".
- Price & Cost pages: level badges Chain/Banner/Store → **Company/Zone/Store**; list rows,
  scope selects, and CREATE.price/CREATE.cost level+scope options.
- Replenishment: Mother-PO allocation "Banner" column → "Zone".
- RBAC: `scopeCell` (chain→company, +zone), users filter+rows (2 sample users → By Zone),
  `BA_SCOPES` (Company/Zone/Store), Branch Access page head/filter/summary/rows,
  "Chain-wide" → "Company-wide", "via Chain" → "via Company"; audit row de-bannered.
- CREATE.branch: Banner field → Chain; added Zone select.
- CREATE.user: Access Scope Company-wide / By Zone / By Store + Zone select.

## Not touched

Cross-Dock column (kept), item hierarchy, vendor RTV, PO margin logic, weighted-avg costing,
Buyer field on PO (companies, per earlier decision).
