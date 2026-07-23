# iConnect ERP — System Overview

**Document set:** iConnect ERP Specification · Source of Truth
**Status:** Approved baseline for Phase 1 development
**Date:** 2026-07-23

---

## 1. Purpose

iConnect ERP is the Merchandise & Supply Chain ERP for the Puregold retail group
(multi-company: Puregold Price Club, Ayagold Retailers / Merkado). It manages the
end-to-end merchandising back office for a nationwide store chain:

- Master data — companies, stores, store groupings, vendors, products
- Merchandising — retail price and vendor cost lifecycle (event-based)
- Planning & replenishment — assortment ranging, demand signals, automatic daily store ordering
- Procurement — purchase orders and goods receiving
- Inventory — stock positions per store/location, adjustments, transfers, returns, conversions
- Sales — POS sales batch ingestion and balancing (sales audit)
- Administration — users, roles, scoped access, audit trail

This document set is the **single source of truth** for building the production system.
It was validated against a working prototype; where the specification goes beyond what
the prototype demonstrated, the decision is flagged **⚑ DR-nn** and catalogued in
`05-decision-register.md`.

## 2. Scope — Phase 1

Phase 1 delivers **all modules listed in §4**. Supporting capabilities outside this list
are added only where required to make a listed module function.

**Out of scope for Phase 1** (defined as future integration points only — see `01-architecture.md §8`):

- External POS system integration (Phase 1 ingests POS batch files)
- Finance / GL hand-off (journal entries from receiving, adjustments, sales)
- Vendor EDI / portal, e-commerce, BI / data warehouse
- 3PL WMS live integration (Phase 1 models the cross-dock statuses; live sync is future)

## 3. Technology Direction (locked)

| Layer | Decision |
|---|---|
| Backend | Java 21 · Spring Boot · **Spring Modulith** (single deployable, enforced module boundaries) |
| Database | Relational, **portable between MariaDB and Microsoft SQL Server** (no vendor-specific features) |
| Frontend | **Vue.js 3** SPA (Composition API, Vite, Pinia, Vue Router) |
| Identity | **Keycloak** (OIDC) — identity + role membership; ERP owns role/permission catalog and access scope |
| API | Versioned JSON REST (`/api/v1`), OpenAPI-documented — the hard contract between SPA and backend |

Details: `01-architecture.md`.

## 4. Module Map

Navigation and module grouping (also the Vue side-menu structure):

| Group | Module | Phase-1 depth |
|---|---|---|
| Overview | Dashboard | Operational KPIs + drill-in links |
| Master Data · Organization | Company Maintenance | Full CRUD |
| | Branch (Store) | Full CRUD |
| Master Data · Store Grouping | Price Zone | Full CRUD + store membership |
| | Cost Zone | Full CRUD + store membership |
| | Assortment Cluster | Full CRUD + store membership |
| | Replenishment Profile | Full CRUD + store membership |
| Master Data · Vendor | Vendor | Full CRUD |
| | Vendor Category Hierarchy | Full CRUD (2-level, per vendor) |
| | Vendor Product (item↔vendor link) | Full CRUD |
| | Vendor Allowance | Full CRUD |
| Master Data · Product | Category Hierarchy (merchandise tree) | Full CRUD (4-level) |
| | Product | Full CRUD (+ pack UOMs, attributes) |
| | Product Brand | Full CRUD |
| | Product Attribute | Full CRUD |
| Merchandising | Price Change Management (Price Events) | Event lifecycle + 4-step wizard |
| | Cost Change Management (Cost Events) | Event lifecycle + 4-step wizard |
| Planning & Replenishment | Assortment Plan | Plan lifecycle + 4-step wizard |
| | Demand Signal | ADD computation + overlays + overrides |
| | Replenishment Policy | Full CRUD (profile × merchandise scope) |
| | Order Calendar | Full CRUD (exception dates) |
| | Vendor Schedule | Full CRUD (weekday order pattern) |
| | Auto Replenishment | Daily engine + Store Replenishment Order review/confirm |
| Procurement | Purchase Order (Regular · DSD · OTB · Multi-Store) | Document lifecycle |
| | PO Receiving (Warehouse · Selling Area · Cross-dock) | Document lifecycle |
| Inventory | Product Inventory (positions) | Views + netting position |
| | Inventory Adjustment | Document lifecycle |
| | Store-to-Store Transfer | Document lifecycle |
| | Return To Vendor | Document lifecycle |
| | Make-up / Break-up | Document lifecycle |
| Sales | POS Sales (batch upload + balancing) | Batch lifecycle |
| Administration | Users | Full CRUD (Keycloak-provisioned) |
| | Roles | Full CRUD + permission matrix |
| | Branch Access | Scope assignment views |
| | Audit Log | Read-only, exportable |

Functional specifications: files `10-…` through `16-…`.

## 5. Key Domain Concepts (glossary)

| Term | Meaning |
|---|---|
| **Company** | A legal/operating entity; treated as the **Chain** — the top pricing level covering all of its stores. |
| **Branch / Store** | A physical store under a Company. Has a **Format** (Price Club · Extra · Minimart · Merkado) and a **Region** (NCR · N.Luzon · S.Luzon · Visayas · Mindanao). |
| **Inventory Location** | Stock-holding unit inside a store. Price Club stores have two (**Warehouse** + **Selling Area**); other formats have a **Selling Area** only. Seeded from Format at branch creation. |
| **Price Zone** | Reusable set of stores that share a retail price. Priced by dated **Price Events**. |
| **Cost Zone** | Reusable set of stores that share a vendor cost. Costed by dated **Cost Events**. Independent of Price Zone even when memberships coincide. |
| **Assortment Cluster** | Set of stores that range a similar assortment; target of **Assortment Plans**. |
| **Replenishment Profile** | Set of stores sharing resupply behaviour; parameters live on **Replenishment Policy** (Profile × merchandise scope). |
| **Merchandise (Category) Hierarchy** | One global 4-level tree: Department → Sub-Department → Class → Sub-Class. |
| **Vendor Category Hierarchy** | Per-vendor 2-level tree (Category → Sub-Category), independent of the merchandise hierarchy. |
| **Vendor Product** | The Company · Vendor · SKU sourcing record (ordering terms). Cost is CCM-owned. |
| **Price Event / Cost Event** | Dated, approvable price/cost change documents at Company, Zone, or Store level. |
| **ADD (Average Daily Demand)** | Per Store · SKU demand rate: baseline × overlay, lost-sales corrected. |
| **SRO (Store Replenishment Order)** | One generated order per store per daily run, spanning all vendors; manually confirmed, then exported. |
| **GRN** | Goods Receipt Note — receiving document. |
| **RTV** | Return To Vendor — only against vendors flagged RTV-allowed. |
| **MOP** | Mode of payment (tender) — balanced during sales audit. |
| **OTB** | One-Time-Buy (Fashion) — seasonal buy against an OTB budget. |

## 6. Personas / Standard Roles

System Administrator, Category Manager, Buyer, Pricing Analyst, Store Manager,
Receiving Clerk, Sales Auditor, Inventory Controller, Vendor Coordinator, Viewer.
Role capabilities are defined by the permission matrix (`16-administration.md`).

## 7. Planning Assumptions (stated, not contractual)

- ~500–600 stores; ~50,000–100,000 active SKUs; ~200–500 concurrent back-office users
- Deployment on-premise or private cloud, single region; stores use the central web app (no offline mode)
- Currency PHP; VAT 12% (retail prices VAT-inclusive); English UI

## 8. Document Set

| File | Contents |
|---|---|
| `00-overview.md` | This document |
| `01-architecture.md` | System architecture, Modulith boundaries, security, NFRs, integration points |
| `02-data-model.md` | Conventions, ERD, entity catalog |
| `03-api-conventions.md` | REST standards, error model, paging, auth contract |
| `04-ui-standards.md` | Vue SPA standards and interaction patterns |
| `05-decision-register.md` | All ⚑ "specified beyond prototype" decisions |
| `10-master-data.md` | Organization, Store Grouping, Vendor, Product masters |
| `11-merchandising.md` | Price Change Management, Cost Change Management |
| `12-planning-replenishment.md` | Assortment, Demand, Policies, Calendars, Auto Replenishment |
| `13-procurement.md` | Purchase Orders, Receiving |
| `14-inventory.md` | Positions, Adjustment, Transfer, RTV, Make-up/Break-up |
| `15-sales.md` | POS Sales batches and balancing |
| `16-administration.md` | Users, Roles, Branch Access, Audit Log |
