# iConnect ERP — Master Data Prototype Backend (Design)

**Date:** 2026-07-21
**Status:** Approved (brainstorming complete; ready for implementation planning)
**Related:** `2026-07-16-iconnect-erp-spec-reconciliation-design.md` (the WHAT/decisions), `2026-07-16-iconnect-erp-implementation-context.md` (mockup code map). Read those for merchandise-hierarchy and pricing-grouping decisions this design assumes.

## 1. Goal

Turn the static, front-end-only mockup `ui-mockup/iconnect-erp.html` into a working **end-to-end prototype for the entire Master Data module group**: real persistence, a REST API, and the existing mockup UI wired to that API so lists, create forms, and edits actually round-trip and survive a restart.

This is a **quick prototype**, not production. The bar is: data round-trips through a real database, relationships are real, a few signature business rules are enforced, and the UI you already built is the UI.

## 2. Locked decisions (from brainstorming)

| Decision | Choice |
|---|---|
| Scope | Entire **Master Data** left-nav group (~14 entities, §4) |
| Frontend | Reuse existing `iconnect-erp.html`; wire its `PAGES` to the REST API. **Served from Spring Boot static resources** → same-origin, no CORS |
| CRUD depth | **List + Create + Edit** for every entity. No delete. Light/optional filtering |
| Rule fidelity | **Real relationships (FKs) + validation + core signature rules** (§6) |
| Database | **H2 file-based** (survives restarts), seeded on first boot from the mockup's current sample rows |
| Auth | **Skipped** for the prototype (Users/Roles/Branch Access are out of Master Data scope) |
| Backend structure | **Hybrid** (§5): generic base CRUD for straightforward masters, explicit controllers for rule-heavy ones |
| Platform | Java 21, Spring Boot 3.x, Maven single module, Spring Web + Spring Data JPA + Bean Validation + H2 |

## 3. Non-goals

- No authentication/authorization, no Users/Roles/Branch Access backend.
- No delete endpoints.
- No modules outside Master Data (Inventory, Price/Cost Change Mgmt, PO, Replenishment engine, Receiving, Store Ops, Sales Audit) — even where the mockup shows them.
- No production concerns: no migrations tool required (JPA `ddl-auto`), no containerization, no CI.

## 4. Entities in scope

Merchandise/master entities (List + Create + Edit each):

1. **Company**
2. **Branch** (belongs to Company; references a Cross-Dock hub; carries grouping assignments)
3. **PriceZone** (store group; Company-scoped)
4. **CostZone** (store group; Company-scoped)
5. **AssortmentCluster** (store group; Company-scoped)
6. **AssortmentPlan** (SKU range for a cluster — keeps its existing wizard UI; final Save persists)
7. **ReplenishmentProfile** (store group; Company-scoped)
8. **Vendor**
9. **VendorCategory** (per-vendor 2-level Category → Sub-Category tree)
10. **VendorProduct** (the item↔vendor link, keyed Company · Vendor · Item)
11. **VendorSchedule** (Company · Vendor weekly order pattern)
12. **VendorAllowance** (trade agreements)
13. **MerchNode** (global Category Hierarchy: Department → Sub-Department → Class → Sub-Class)
14. **Product** (the item master)

Supporting child/link tables:

- **ProductPackUom** — pack/UOM levels per Product (Base/Inner/Case, qty-in-base, barcode, orderable/sellable flags).
- **Store-grouping assignments** — the four grouping assignments (Price/Cost/Cluster/Replen) are modeled as **nullable FKs on Branch**. The current spec assigns a store to at most one of each kind, so FKs suffice; a separate membership table is only needed if that changes.

## 5. Backend structure — Hybrid (chosen)

Three options were weighed:

- **A. Generic base CRUD for everything** — least code, but nowhere clean to hang per-entity rules.
- **B. Explicit controller/service/repo per entity** — clearest, but ~14× boilerplate for a quick prototype.
- **C. Hybrid (CHOSEN)** — a thin generic base (`AbstractCrudController<E,ID>` + `AbstractCrudService`) for the 10 straightforward masters (Company, Branch, PriceZone, CostZone, AssortmentCluster, ReplenishmentProfile, Vendor, VendorCategory, VendorSchedule, VendorAllowance), and **explicit controllers/services** for the four rule-heavy entities: **Product, VendorProduct, MerchNode, AssortmentPlan**. (10 + 4 = the 14 entities in §4.)

**Package layout** (package-by-feature):

```
com.puregold.iconnect
  ├─ common/            AbstractCrudController, AbstractCrudService, error handling, DTO base
  ├─ company/           entity, dto, repository, service, web
  ├─ branch/
  ├─ grouping/          pricezone, costzone, cluster, replenprofile (+ assortmentplan)
  ├─ vendor/            vendor, vendorcategory, vendorproduct, vendorschedule, vendorallowance
  ├─ product/           product, packuom, merchnode
  └─ seed/              DataSeeder (CommandLineRunner)
```

Each feature: JPA `@Entity`, a request/response **DTO** (never expose entities directly), Spring Data `JpaRepository`, a service, and a `@RestController`. Schema via JPA `ddl-auto=update`; seed via a `CommandLineRunner` that only runs when tables are empty.

**API shape** (uniform):

```
GET    /api/{entities}            → list (optional ?q=, ?status=, ?companyId=)
GET    /api/{entities}/{id}       → one
POST   /api/{entities}            → create  (201 + body)
PUT    /api/{entities}/{id}       → update
```

`{entities}` = `companies, branches, price-zones, cost-zones, assortment-clusters, assortment-plans, replenishment-profiles, vendors, vendor-categories, vendor-products, vendor-schedules, vendor-allowances, merch-nodes, products`.

## 6. Core signature rules to enforce

1. **Sub-class auto-scaffold** — creating a `MerchNode` at level **Class (L3)** auto-creates three child Sub-Class nodes `101/102/103`. They are removable/extendable (any number allowed).
2. **Cost owned by CCM (read-only)** — `VendorProduct.costPerCase` is **not settable through the create/edit API**; it is populated only by seed data and returned read-only. This resolves the mockup conflict where the item Suppliers tab appeared to edit cost while Vendor Product declared it CCM-owned.
3. **Item↔vendor link is one-to-many** — a `Product` has many `VendorProduct` links (each flagged Primary/Alternate). The item's "Preferred Vendor" is **derived** = the Primary link, not a stored scalar. Exactly one Primary per Product is enforced.
4. **Validation** — `@NotBlank`/`@NotNull` on required fields; **unique codes** for Company, Branch, Vendor, and Product; **FK existence** checks on all references (create/edit rejects dangling parent/company/vendor/item IDs) with a clean 400/422 + message.

## 7. Frontend wiring approach

- Add a small `api(path, opts)` fetch helper near the top of the `<script>`.
- Convert the ~14 list `PAGES.*` from returning a static HTML string to **async**: fetch JSON, then render through the **existing** `table()` / `filterbar()` / `head()` helpers (contracts unchanged). `route()` awaits the page render.
- Convert `openCreate(key)` saves and edit-form saves to **POST/PUT** via `api()`, then re-route to the refreshed list.
- `AssortmentPlan` keeps its `openAssortWizard` UI; only its final Save calls the API.
- Seed data mirrors the rows currently hardcoded in the mockup, so **first load looks identical** to today's static mockup.
- Mockup file continues to live at `ui-mockup/iconnect-erp.html`; the build copies/serves it from `src/main/resources/static/` (exact mechanism decided in the plan — symlink, copy step, or move + reference).

## 8. Naming cleanup (consistency fixes)

- Standardize the module trio on **"Product"** (matches nav + the request): fix page headers reading "Item Maintenance" / "Item Hierarchy".
- Make `CREATE.item` Classification a **real cascading** Department → Sub-Department → Class → Sub-Class fed from `MerchNode`, replacing the flat mixed `Milk/Coffee/Noodles/Eggs` list.
- Reconcile the Suppliers-tab cost display with rule §6.2 (show cost as read-only, sourced from VendorProduct).

## 9. Verification

- **Backend unit/slice:** a test per signature rule (sub-class scaffold, cost read-only, single-primary link, unique-code + FK validation), plus a `@WebMvcTest`/`@DataJpaTest` smoke per generic entity.
- **End-to-end:** boot the app, open the served mockup, **create + edit a Product and a Company through the UI**, restart the app, confirm both persist. Confirm a rule-heavy path (add a second Primary vendor link → rejected).

## 10. Open items to settle in the implementation plan

- Exact static-serving mechanism for the mockup file (copy step vs. move-and-reference).
- Whether filtering (`?q=`) is implemented for all lists or only the high-value ones.
- AssortmentPlan persistence shape (header + line rows) — the wizard's data model.
