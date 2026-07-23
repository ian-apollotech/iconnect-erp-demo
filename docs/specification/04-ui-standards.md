# 04 — UI Standards (Vue SPA)

## 1. Toolchain

| Concern | Decision |
|---|---|
| Framework | Vue 3, Composition API, `<script setup>`, TypeScript |
| Build | Vite |
| State | Pinia (auth/session store, reference-data store, per-module stores) |
| Routing | Vue Router, route-per-module with nested detail routes |
| HTTP | Thin typed API client over `fetch`/axios targeting `/api/v1`, generated or hand-written from the OpenAPI contract |
| Auth | keycloak-js (or oidc-client-ts) — auth-code + PKCE, silent refresh |
| Component base | A single in-house component library implementing the patterns below (a headless library such as PrimeVue/Radix may back it, but the visual language is the ERP's own) |

## 2. Application layout

- **Topbar** (fixed, 52 px): brand ("iConnect ERP — Puregold · Merchandise & Supply Chain"),
  task/notification icons with count badges, user chip (name + role).
- **Sidebar** (fixed, ~248 px, navy): grouped navigation exactly as `00-overview.md §4`,
  with group headers, optional sub-headers (Organization / Store Grouping / Vendor / Product),
  active-item highlight. Items are hidden when the user lacks View permission on the module.
- **Main content** area: breadcrumb (`iConnect ERP › Module › Page`), page header
  (title, description, right-aligned action buttons), then content panels.
- Reference palette (navy `#0f2540`, blue `#2563eb`, status colors green/amber/red/purple/cyan)
  and 13 px base font as demonstrated by the prototype; encode as design tokens.

## 3. Page patterns

Every module uses the same small set of patterns — do not invent new ones per module.

### 3.1 List page (landing)

- Stats row (optional KPI cards) → panel with **filter bar** → table → pager footer.
- Filter bar: free-text search (`q`) + dropdown filters mirroring the API's named filters;
  filtering is **live** (input debounce) and combined with AND; footer count updates.
- Tables: uppercase column headers, monospace code cells, right-aligned numeric columns,
  status **badges** (pill + dot, color per status), row hover.
- The record identifier/name is a **row link** opening the record view. Lists are
  list-only — no inline editing on list rows.
- Pager: server-side paging (`03 §4`); shows "Showing X of Y records".

### 3.2 Record view (read-only) + inline Edit

- Read-only key/value panel (label column + value column), status as badge, plus
  module-specific extra panels (e.g., grouping pages show a searchable **Member Stores**
  panel).
- Actions: `‹ Back to list`, `Edit`.
- **Edit** swaps to the same form used by Create, pre-filled, with `Save Changes`
  performing `PUT` and returning to the record view. A status the form does not expose is
  preserved, never reset.

### 3.3 Create form

- Sectioned form grid (section labels, 2–4 columns responsive), required markers `*`,
  read-only derived fields visually distinct.
- FK dropdowns are populated from reference endpoints and submit **IDs**, not labels.
- Footer + header both carry `Cancel / Save` (and `Save Draft` only for documents with a
  draft/approval lifecycle).
- Validation errors surface inline per field (from the `errors` map) and as a toast for
  `message`-type errors.

### 3.4 Dual-list store picker (grouping membership)

Used by Price Zone, Cost Zone, Assortment Cluster, Replenishment Profile:
Available stores (searchable, filtered to the selected company) ⇄ Selected stores, with
`›` / `‹` move buttons and live counts. Changing company resets the selection. On save,
membership changes persist as branch updates (`02 §3`).

### 3.5 Wizards (4-step)

Used for Price Event, Cost Event, Assortment Plan creation (and Duplicate):
step bar (numbered, done/active states, clickable back-navigation), one panel per step,
footer `‹ Back · Cancel · Next › / Submit`. Steps per wizard: `11 §2.3`, `11 §3.3`, `12 §2.3`.
Wizard state is client-side until final submit (single POST).

### 3.6 Tabs

Panel-level tabs with optional count chips (e.g., PO: Regular / DSD / OTB / Multi-Store;
Inventory: Netting / Warehouse / Selling Area; Sales batch detail: Transactions /
By Terminal / By Tender / Exceptions).

### 3.7 Notes & guidance

Inline note boxes (info/indigo, warning/amber, success/green, cyan) carry the business
rule reminders shown throughout the modules — these texts are part of the spec'd UX.

## 4. Formatting

| Item | Rule |
|---|---|
| Currency | `₱ 1,234,567.89`; `—` for null |
| Dates | `YYYY-MM-DD`; ranges `07-14 → 07-31` |
| Codes | Monospace |
| Deltas | Price: up green/down red; **Cost: up red/down green** (inverted) |
| Empty values | `—` |

## 5. Behaviour

- All mutating calls show optimistic-free explicit feedback: disable button → spinner →
  success toast or error surface. No silent failures.
- Errors from the API error model (`03 §3`) are always rendered; never swallowed.
- Session expiry triggers silent token refresh; if refresh fails, redirect to Keycloak login.
- Route guards: module routes require View permission; action buttons render only with
  the matching Create/Edit/Approve permission (server still enforces).
- Deep-linkable routes for record views (`/branches/42`) and list filters (query params).

## 6. Quality

- Accessibility: keyboard-navigable forms and tables, focus states, labels bound to
  inputs, WCAG AA contrast for text and badges.
- Responsive: layout degrades to single-column below ~1200 px; sidebar collapsible.
- Component tests for the shared patterns; E2E happy-paths per module (create → view →
  edit; wizard submit; document transition).
