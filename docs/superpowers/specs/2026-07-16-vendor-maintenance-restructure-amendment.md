# iConnect ERP — Vendor Maintenance Restructure (Amendment)

**Date:** 2026-07-16
**Status:** Approved & implemented in `ui-mockup/iconnect-erp.html`
**Amends:** `2026-07-16-iconnect-erp-spec-reconciliation-design.md` §2.2 (vendor category & item↔vendor supply profile) and §4.2 (Vendor module).

## Summary

Vendor Maintenance is reorganised from a single flat vendor list into a **module with several functions**, and the **Master Data** sidebar section is reorganised into per-entity groupings. The vendor no longer carries a single category attribute; instead each vendor owns a **2-level Category Hierarchy** and a **Catalog** of sellable SKUs.

## Decisions

### 1. Master Data sidebar → nested entity groupings
`renderSidebar` now supports a second level (`{subhead, items:[…]}` inside a group's `items`). Master Data reads:

- **Company Maintenance** `[1]` (leaf)
- **Store Branch Maintenance** → Chain *(placeholder)* · Zone *(placeholder)* · Branch `[2]`
- **Vendor Maintenance** → Vendor `[3]` · Vendor Category Hierarchy · Vendor Catalog · Vendor Allowance `[4]`
- **Product Maintenance** → Category Hierarchy `[5]` · Product `[6]`

Module badges are unchanged; new sub-functions carry no badge. Nav labels changed: "Item Hierarchy"→"Category Hierarchy", "Item Maintenance"→"Product", "Vendor Maintenance" (item)→"Vendor" (the grouping keeps the module name). Page-body titles for hierarchy/item were left unchanged (out of scope this pass — "do Vendor first").

### 2. Vendor (master) — `PAGES.vendor` / `CREATE.vendor`
- **Removed** the vendor-level `Category (L1 › L2)` list column + filter, and the `Category (L1)` / `Sub-Category (L2)` fields from New Vendor.
- New Vendor Identity is now Vendor Name · TIN · Vendor Code; footnote points onboarding staff to set up the hierarchy & catalog afterward.
- List gained **Categories** and **Catalog SKUs** count columns; RTV flag retained.

### 3. Vendor Category Hierarchy — `PAGES.vencat` / `CREATE.vencat` (new)
- **Per-vendor**, 2-level (Category → Sub-Category). Vendor-selector at top; tree view; New Category / New Sub-Category.
- Independent of the merchandise Category Hierarchy under Product Maintenance.
- A node may be flagged allowance/promo-eligible. Catalog SKUs are tagged to Sub-Category nodes.

### 4. Vendor Catalog — `PAGES.vencatalog` / `CREATE.vencatalog` (new)
- The vendor-side view of the **item↔vendor link** (same relationship shown on Item Maintenance → Suppliers). One row per sellable SKU with the ordering terms for sourcing it from this vendor: Vendor Item # · Vendor Category (from this vendor's hierarchy) · Pack · MOQ · Order Increment · Order Schedule · Lead Time · Cost/Case · **Allowance** (ties to a Vendor Allowance agreement) · Source (Primary/Alternate).
- The Item → Suppliers tab note was repointed to say maintenance happens here.

### 5. Chain / Zone — placeholders only
`PAGES.chain` and `PAGES.zone` are non-functional stubs ("design pending"). This does **not** re-open the parked store-classification (§4.4) or geographic-zone pricing (§5.2) decisions; it only reserves the nav slots the user requested.

## Relationship to prior spec

- **Supersedes §2.2's** treatment of vendor category as a picklist on the item↔vendor link: vendor category is now a **maintained per-vendor 2-level hierarchy**; catalog lines reference it. The item↔vendor supply profile itself (MOQ, increment, schedule, lead time, cost) is unchanged — it is now **editable from the vendor side** (Vendor Catalog) and mirrored read-side on the Item → Suppliers tab.
- **Extends §4.2:** the Vendor module now contains Vendor, Vendor Category Hierarchy, Vendor Catalog (and keeps Vendor Allowance). "How other ERPs do it": mirrors Oracle RMS Item-Supplier / SAP Retail vendor sub-functions and a distributor-style vendor catalog.

## Verification (2026-07-16)

Extracted `<script>` passed `node --check`. Rendered over local HTTP in Playwright: nested sidebar, Vendor list (no category col), Vendor Category Hierarchy tree, Vendor Catalog table, and New Vendor form (no L1/L2 fields) all render with no JS console errors (only a favicon 404).
