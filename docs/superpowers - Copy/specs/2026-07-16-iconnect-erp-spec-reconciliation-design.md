# iConnect ERP — Specification Reconciliation

**Date:** 2026-07-16
**Status:** Approved decision log (source of truth for updating the mockup / build)
**Authors:** Reconciliation session (Puregold ERP)

## Source documents

- `docs/puregold/Puregold JDA ERP_1.pdf` — detailed functional spec (17 modules), v1.0, 12.09.25. **Primary source of truth.**
- `docs/puregold/Puregold System.docx` — module/feature checklist.
- `ui-mockup/iconnect-erp.html` — current UI mockup under review.

## Purpose

The mockup was built ahead of a full reading of the spec, so several concepts drifted (renamed, re-scoped, or invented) and several documented requirements were dropped. This document reconciles the three sources into a single approved set of requirements. Each item states **what the spec said**, **what the mockup did**, and **the decision** taken. Where a documented requirement is not standard ERP practice, the decision records the chosen deviation.

This is a reconciliation/requirements spec, not an implementation plan. It governs the next revision of the mockup and the eventual build.

---

## 1. System scope & positioning

| | Detail |
|---|---|
| **Spec** | JDA ERP posts a PO, exports it as CSV, and it is "imported by iConnect" — implying iConnect is a separate downstream system. |
| **Mockup** | The whole application is branded "iConnect ERP." |
| **Decision** | **iConnect ERP fully replaces JDA and owns all 17 modules end-to-end.** The spec's "export CSV to iConnect" line describes legacy JDA behavior being superseded; there is **no internal CSV handoff** in iConnect. The only file-based ingestion that remains is the **external POS sales flat-file upload** (Module 17). |

**Global context filter.** The top-bar context becomes **Company → Branch** for now. The dropped geographic Zone is **not** replaced by Banner yet; **Banner** is wired into the global filter later, once store-classification design is finalized (see §4, §6).

---

## 2. Item Hierarchy (Module 5)

### 2.1 Level naming

- **Spec:** 4 levels — Department → Sub-Department → Class → Sub-Class.
- **Mockup:** Division → Department → Category → Sub-category.
- **Decision:** Use **spec naming**: **Department → Sub-Department → Class → Sub-Class.** Treat the mockup's terms as legacy synonyms only.

### 2.2 Hierarchy scope (was "per Vendor")

- **Spec:** "One-time setup of item hierarchy **per Vendor**" + "mapping of vendor and their items."
- **Standard-ERP note:** A literal per-vendor hierarchy duplicates structure and breaks cross-vendor category reporting. Tier-1 retail ERPs (SAP Retail MSR, Oracle RMS Item-Supplier, Blue Yonder/JDA) keep **one** merchandise hierarchy and attach vendor-specific data to the **item↔vendor** relationship.
- **Decision:** **One global merchandise hierarchy** (Department→Sub-Department→Class→Sub-Class). Each item is mapped to exactly **one** merchandise-hierarchy node. Vendor-specific data is modeled on the **item↔vendor link record**, not on the item, so primary and alternate vendors can differ:
  - **Vendor category — 2 levels** (vendor category → vendor sub-category). May drive **vendor allowance / promo** eligibility (the SAP-MSR pattern).
  - **Order schedule** (per-vendor order calendar — see §7.2).
  - **MOQ** (minimum order quantity).
  - **Order increment / pack multiple.**
  - **Lead time.**
  - **Vendor cost.**
- **Rationale:** Confirmed logical and standard. The spec's "per vendor" is interpreted as *vendors are onboarded/mapped into the shared hierarchy*, with their commercial attributes held per item-vendor.

### 2.3 Sub-Class cardinality

- **Spec:** Sub-Class "has 3 tiers fixed in 1 class only … (101, 102, 103)."
- **Standard-ERP note:** A bottom level hard-locked to exactly three values is rigid.
- **Decision:** **Default-scaffold three sub-classes (101/102/103)** for convenience, but a Class may have **any number** of Sub-Classes; merchandising can add/remove. Not enforced to exactly three.

---

## 3. Item Maintenance (Module 6)

Restore documented item attributes the mockup dropped:

- **Listing-fee flag** — item-level tag; surfaces in PO validation.
- **Store-range / "carried by store" assortment tag** — governs which stores may order/stock the item; used to validate PO lines for a given store (see §7.1).
- **Buying UOM vs Selling UOM** — items (especially fresh) carry a distinct buying UOM and selling UOM (spec Module 16f). Folded in as a design detail.

Retain the mockup's useful additions: perishable flag, shelf life, brand, POS short name, VAT class, pack/UOM ladder, supplier tab, price/cost tab.

---

## 4. Store / Vendor / Company master data

### 4.1 Store Branch (Module 2)

- **Primary & alternate vendor** designation (at store, and/or item-store) — sources replenishment/POs. (Spec references this in Branch, Vendor, and Cost modules.)
- **Cross-dock that serves the store** — records the cross-dock / 3PL hub, so cross-dock deliveries and Mother-PO allocations route correctly.

### 4.2 Vendor (Module 3)

- **RTV-allowed flag** — RTV creation (Module 14) is **gated** on it.

### 4.3 Company (Module 1)

- **Buyer** becomes a **maintained entity** (buyer code + name), assigned per Company and selectable on POs. The mockup had a buyer dropdown but no underlying master.

### 4.4 Store classification (Banner / Format) — parked

- The mockup's store classification (Hypermarket / Supermarket / Puregold Jr. / Extra / Minimart / Ayagold, plus a separate "Chain" filter) was **AI-invented, not spec-driven.** Full classification design is **parked.**
- **Retained now:** **Banner** values **Price Club / Extra / Minimart** (+ Merkado for Ayagold) — used **only** as the pricing/cost grouping level (§5). Physical-format modeling is deferred.

---

## 5. Pricing & Cost (Modules 8, 9)

### 5.1 Grouping levels

- **Spec:** Price change event levels — **Chain** (all stores per company) → **Zone** (3 *format/banner* types: Price Club = supermarket/warehouse, Extra = groceries, Minimart = convenience) → **Store**.
- **Mockup:** Company → **Zone as geography** (NCR / N.Luzon / S.Luzon / VisMin) → Branch. This mis-mapped the spec's banner-based "Zone" onto geography.
- **Standard-ERP note:** A pricing zone limited to three fixed banners is less flexible than free-form price-zone groups, but matches Puregold's operating reality.
- **Decision:** Pricing **and** cost grouping = **Chain → Banner (Price Club / Extra / Minimart) → Store.** Geography is **not** a pricing axis.

### 5.2 Geographic zone — dropped

- **Decision:** Remove geographic zone entirely for now (from pricing, access scope, replenishment, filters). Store grouping by region is a **future** feature. RBAC access scope collapses to **Chain-wide / By Store** (see §8).

### 5.3 Price Change behaviors to restore (Module 8)

- **Regular vs Promo PCA.** Regular = start date, no end. Promo = start **and** end date. **On promo end, price auto-reverts** to the prior Regular price.
- **Overlap resolution — latest event number wins.** When 2+ PCAs target the same SKU/scope over overlapping dates, the **latest event number** takes effect. *(Standard-ERP note: normally resolved by effective-date then event-number as tiebreak. Decision: follow the spec — latest event number wins; effective-date used as the tiebreak where events are otherwise ambiguous.)*
- **Daily price/cost comparison report.** Scheduled daily report listing **Store, SKU, Price, Cost** for comparison.

### 5.4 Minimum-margin guardrail (Modules 8 & 9)

- **Decision:** Below-minimum-margin items raise a **warning AND are forced through an extra approval step** before publish (stronger than the spec's warning-only, weaker than a hard block). Applies to **both** Price and Cost changes.

### 5.5 Cost change (Module 9)

- Retain per-vendor cost update at Chain/Banner/Store level, margin recalculation, event tracking, and both primary & alternate vendor support. Weighted-average recosting on receiving is covered in §7.3.

---

## 6. Procurement (Modules 10, 11)

### 6.1 Purchase Order controls (Module 10)

- **Gross-margin guard** on PO lines: warn when a line's gross margin is low/negative; allow override by an authorized role (mirrors the price/cost pattern). Buyers cannot silently order into a loss.
- **Active-SKU-only:** only active SKUs may be added; inactive/discontinued items rejected.
- **Auto-PO drops restricted SKUs:** in auto-replenishment PO generation, SKUs failing restrictions (margin / inactive / not-carried-by-store) are **automatically removed** from the proposal, with a **log of what was dropped and why.**
- Retain PO types: **Regular**, **DSD (no PO)**, **OTB (Fashion)**. DSD keeps the **"enter invoice → blank PO opened → capture SKUs/qty → DSD listing for review before posting"** flow.

### 6.2 Auto-Replenishment (Module 11)

- **Per-vendor order schedule/calendar** (selected days/dates, per store) that **auto-generates** proposals/POs on schedule. The ad-hoc run remains as a manual override.
- **Suggested-order formula params exposed** (sales velocity, inventory goal/coverage, in-transit, ROP) so proposals are explainable.
- **Mother-PO** (1 vendor / multi-store) consolidation with store-allocation split at cross-dock — retained.
- **Remove** the mockup's AI-ish **"Confidence: High/Review"** column (not in spec).

### 6.3 OTB (One-Time-Buy, Fashion)

- **Budget carryover:** OTB tracks budget **per quarter**; unused budget **carries into the next quarter.** Mockup showed a budget bar but no carryover.

---

## 7. Inventory & Receiving (Modules 7, 12, 13, 14, 15, 16)

### 7.1 Assortment enforcement

- PO lines validated against the item's **store-range/assortment tag** (§3) — an item not carried by the target store is rejected/flagged during PO creation.

### 7.2 Product Inventory (Module 7)

- **Merchandise Issuance document:** moving stock **Branch Warehouse → Selling Area** generates a Merchandise Issuance document (audit trail of the internal issue), replacing the mockup's bare "Transfer to Selling Area" button.
- Retain **Per Branch Warehouse (incl. Top Load)** and **Per Selling Area** views.

### 7.3 Costing

- **Weighted-average cost auto-update:** receiving recomputes item cost as a moving/weighted average from received qty and cost (spec: "depending on RA qty and cost, the cost will automatically adjust"). This is the costing method.

### 7.4 Damaged-on-issue → RTV

- Damaged items found during WH→Selling-Area handling **route back to the warehouse and into an RTV** (spec Module 7e), gated by the vendor RTV-allowed flag (§4.2).

### 7.5 Inventory Adjustment (Module 15)

- **Bulk file upload** for mass corrections and **annual physical count**, in addition to line-by-line entry. Retain reason codes and approval trail.

### 7.6 Make-up / Break-up (Module 16)

- Retain **Promoted Items (make-up/break-up)** and **Fresh Cut-ups** (yield / trim-loss / re-cost). Items carry **buying UOM vs selling UOM** (§3).

### 7.7 Store-to-Store Transfer (13) & Return to Vendor (14)

- Retain as mockup'd; RTV gated on vendor RTV-allowed flag; RTV reverses inventory and cost-payable (debit memo).

---

## 8. Sales Audit (Module 17)

- Retain **POS Sales Batch Upload** (external flat file), **Mode-of-Payment Balancing**, **Cashier Balancing**, **Sales Balancing**, and the batch detail view. Sales upload auto-deducts inventory; Sales Balancing allows manual edit of missing-but-proven sales to balance.

---

## 9. Non-spec additions — confirmed in scope

- **User Management:** Users, Roles + permission matrix, Branch Access, Audit Log.
  - **RBAC access scope** = **Chain-wide / By Store** (geographic zone dropped; banner-level scoping possible later).
- **Dashboard:** operational snapshot home page — kept. (Banner as a dashboard filter deferred with §1/§4.4.)

---

## 10. Summary of deviations from the literal spec

| Ref | Spec (literal) | Decision | Reason |
|---|---|---|---|
| 2.2 | Item hierarchy per vendor | One global hierarchy + per-item-vendor supply profile (2-level vendor category) | Standard ERP; preserves cross-vendor reporting |
| 2.3 | Sub-Class fixed 3 tiers (101/102/103) | Default 3, variable count | Flexibility |
| 5.1 | Zone = 3 fixed banners | Kept as Banner level (Price Club/Extra/Minimart) | Matches operations; format modeling deferred |
| 5.3 | Overlap: latest event wins | Latest event wins; effective-date tiebreak | Determinism |
| 5.4 | Below margin → warning | Warning **+** forced approval routing | Stronger guardrail |
| 1 | JDA exports CSV to iConnect | iConnect replaces JDA; no internal CSV | Full-replacement scope |

## 11. Parked / future items

- Store banner/format classification (full model) — §4.4.
- Geographic/region store grouping — §5.2.
- Banner in the global context filter and Dashboard — §1.

## 12. Out of scope for this reconciliation

- Data migration from JDA.
- POS terminal software and the flat-file export format (external system).
- Finance/GL posting beyond the cost-payable reversal on RTV.
