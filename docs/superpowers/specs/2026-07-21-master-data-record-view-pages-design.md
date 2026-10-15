# Master-Data Consistent Lists + Record View Pages (Design)

**Date:** 2026-07-21
**Status:** Approved (brainstorming complete)
**Related:** `2026-07-21-iconnect-erp-master-data-prototype-backend-design.md`

## 1. Goal

Make every Master Data list page consistent (list-only, no fake drill-down
panel), and give each record a real **view page** reachable by clicking its
name. The view page opens **read-only** and flips to **editable in place**
(Edit → Save/Cancel).

## 2. Locked decisions (from brainstorming)

| Decision | Choice |
|---|---|
| Which entities get view pages | **All** master lists (eventually) |
| Detail page mode | **View with inline Edit toggle** (opens read-only; Edit un-locks fields; Save persists) |
| Open trigger | **Click the record name** (existing `.row-link`) |
| Category Hierarchy | **Keep the tree** as the "list"; clicking a node opens its view page |
| Detail depth | **Full detail incl. children** (Product keeps its 4 tabs; Assortment Cluster shows its plans; Branch shows inventory locations) |
| Rollout | **Mockup first, then prototype.** First mockup pass = the **4 named entities only** (Branch, Assortment Cluster, Category Hierarchy, Product) for review; then extend to the other 7 and port to `index.html` |

## 3. Architecture (one generic engine, DRY)

Added once near `const PAGES = {}` in `ui-mockup/iconnect-erp.html`:

- **`MD = {}`** — cache of raw record arrays; each list page sets `MD[entity]`
  as it renders, so rows are addressable by key.
- **`openView(entity, key)`** — stashes `VIEW = {entity, key, editing:false}`
  and renders the view page (keeps parent nav highlighted).
- **`renderRecordView()`** — dispatches to a per-entity body builder; wraps it
  in `viewShell` (Back-to-list, title/subtitle, Edit or Save/Cancel).
- **`vFields(fields, editing)`** — renders a field list as a clean read-only
  `.kv` grid when `!editing`, or an editable `.form-grid` when `editing`.
  Fields marked `ro` stay read-only in both. Child tables render read-only in
  both modes.
- **Edit toggle re-renders** (`VIEW.editing` flip → `rerenderView()`), which is
  simpler and more robust than mutating DOM disabled state. Save in the mockup
  is visual only (`✓ Saved (mockup)`); in the prototype it will `PUT`.

`route()` binds `#main [data-view]` → `openView`. List names become
`<span class="row-link" data-view="entity:key">`.

## 4. Per-entity view content (first pass)

- **Branch** — Company, Format, Region, Cross-Dock, Delivery Feed, Status, the
  four grouping assignments; child **Inventory Locations** table derived from
  Format (Price Club → Warehouse + Selling Area; others → Selling Area only).
- **Assortment Cluster** — code/name/profile/company/stores/SKUs/plans/status;
  child **Assortment Plans** table (plans whose target is this cluster).
- **Category Hierarchy node** — Level/Code/Name/Parent/Merch Manager/Replen
  Strategy/OTB/Perishable/Shelf Life/Status. Tree stays; nodes are clickable.
- **Product** — 4 tabs preserved: General (identity + classification +
  attributes), Pack & UOM (table), Suppliers (table), Price & Cost. Margin is
  derived `= (retail-cost)/retail*100`.

## 5. Removed

The four hardcoded drill-down panels: Branch "0102" detail, Category Hierarchy
side "Milk" node panel, Product "Alaska Fresh Milk 1L" tabbed panel. Assortment
Cluster keeps its "Open Assortment Plan →" route teaser; only fake per-record
detail is what's replaced.

## 6. Verification

Open the mockup, click a name in each of the 4 lists → view opens read-only →
Edit flips fields editable → Cancel restores → Save flashes saved + returns to
read-only → Back returns to the list. Clicking different rows shows different
records. JS parses; headless render OK.
