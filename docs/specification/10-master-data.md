# 10 — Master Data

Modulith module: `masterdata`. All masters follow the shared patterns: list page →
record view → inline edit (`04 §3`), standard CRUD API (`03`), audit columns (`02 §2`).
Statuses: `Active` default; masters deactivate rather than delete once referenced.

---

## 1. Company Maintenance

Corporate legal entities of the group. **A Company is the Chain** — the top pricing level
covering all of its stores.

### Fields

| Field | Type | Rules |
|---|---|---|
| code | string(40) | required, unique |
| legalName | string | required |
| tradeName, tin | string | optional |
| fiscalYearStart | enum: `January` · `April` | |
| baseCurrency | enum: `PHP — Philippine Peso` | |
| defaultVat | enum: `12% VATable` · `VAT Exempt` | |
| priceIncludesVat | enum: `Yes (VAT-inclusive)` · `No` | |
| registeredAddress, contactPerson | string | |
| status | `Active` · `Inactive` | default Active |
| branchCount | derived | count of branches |

**Rules:** company code unique (400 `Company code already exists: {code}`).

**API:** `GET/POST /companies`, `GET/PUT /companies/{id}`.

---

## 2. Branch (Store)

Physical stores under a company; each has a Format and Region and holds stock in one or
more Inventory Locations.

### Fields

| Field | Type | Rules |
|---|---|---|
| code | string(40) | required, unique |
| name | string | required |
| companyId | FK company | required, must exist |
| format | enum: `Price Club` · `Extra` · `Minimart` · `Merkado` | |
| region | enum: `NCR` · `N.Luzon` · `S.Luzon` · `Visayas` · `Mindanao` | |
| inventoryLocations | enum label: `Selling Area + Warehouse (Price Club default)` · `Selling Area only` | seeded from format; drives location creation (`14 §1`) |
| crossDockHub | enum: `— Direct-to-store —` · `CD-NCR North` · `CD-NCR South` · `CD-Cebu` · `CD-Davao` | serving hub if not direct |
| openingDate | date | |
| priceZoneId / costZoneId / assortmentClusterId / replenishmentProfileId | FK, nullable | each must exist if set — these four FKs ARE the store-grouping memberships (`02 §3`) |
| street, city, province | string | |
| status | `Active` · `Closed` · `Inactive` | |

**Rules**
- Branch code unique; companyId and each grouping FK validated for existence.
- On create, `BranchCreated` event → inventory module seeds Inventory Locations from
  format: Price Club → Warehouse + Selling Area; Extra/Minimart/Merkado → Selling Area
  only. ⚑ DR-15
- Format also seeds the default `inventoryLocations` label; user may override at create.

**Screens:** list (search, company/format/region/status filters, cross-dock + locations
columns); view shows groupings resolved by name; edit allows re-assignment of all FKs.

**API:** `GET/POST /branches`, `GET/PUT /branches/{id}`.

---

## 3. Store Groupings (Price Zone · Cost Zone · Assortment Cluster · Replenishment Profile)

Four independent, purpose-named groupings share one shape. A grouping is **only a set of
stores — the "who"**; the "what" lives elsewhere:

| Grouping | Used by | The "what" |
|---|---|---|
| Price Zone | Price Events (`11 §2`) | dated retail prices |
| Cost Zone | Cost Events (`11 §3`) | dated vendor costs |
| Assortment Cluster | Assortment Plans (`12 §2`) | ranged SKU lists |
| Replenishment Profile | Replenishment Policies (`12 §4`) | resupply parameters |

Price Zone and Cost Zone may have identical memberships — that overlap is expected, not
duplication: cost and retail move independently.

### Shared fields

| Field | Rules |
|---|---|
| name | required |
| code | optional, unique when present ("Auto if left blank" ⚑ DR-14) |
| companyId | required, must exist |
| status | `Active` · `Inactive` |
| description | long text |
| storeCount | derived from branch FKs |

Assortment Cluster adds: `storeProfile` (free text), `tradeArea`
(`Urban` · `Suburban` · `Provincial` · `Transit / Mall`).

**Membership editing:** dual-list store picker (`04 §3.4`) on create/edit; persisted by
setting/clearing the corresponding FK on each affected branch. A store belongs to at most
one grouping of each type.

**API:** `GET/POST /price-zones|cost-zones|assortment-clusters|replenishment-profiles`,
`GET/PUT …/{id}`.

---

## 4. Vendor

Supplier master — identity, commercial terms, delivery model, RTV eligibility.

### Fields

| Field | Type / values | Rules |
|---|---|---|
| code | string(40) | unique; auto `V-{id}` if blank |
| name | string | required |
| tin | string | |
| vendorType | `Manufacturer` · `Distributor` · `Manufacturer & Distributor` | default M&D; only Manufacturer-types may own Brands |
| deliveryModel | `Direct-to-Store (DTS)` · `Cross-dock` · `DSD` | |
| paymentTerms | `7/15/30/45/60 days` | |
| leadTime | string (e.g. `2 days`) | |
| minOrder | money | |
| rtvAllowed | `Yes` · `No` | required; gates RTV documents (`14 §6`) |
| registeredAddress, contactPerson, phone, email | string | |
| status | `Active` · `On Hold` · `Inactive` | |

**Derived on list:** category count, catalog SKU count, allowance count.

**Rules:** code unique when present. A vendor does not carry a single category — it owns
its Vendor Category Hierarchy (§5) and Vendor Product catalog (§6), maintained separately
after onboarding.

**API:** `GET/POST /vendors`, `GET/PUT /vendors/{id}`.

### Phase-2 extension (payables) ⚑ DR-34

Added when module `payables` is enabled (`17 §2.1`): `bankName`, `bankAccountNo`,
`bankAccountName` (remit-to), `preferredPaymentMethod` (`Check` · `Bank Transfer`),
`ewtCodeId` (default BIR ATC classification), `vatClass` (`VATable` · `VAT Exempt` ·
`Zero-rated`, defaults from company `defaultVat`). `tin` becomes required for Active
vendors (feeds Form 2307).

---

## 5. Vendor Category Hierarchy

Per-vendor 2-level classification: **Category (L1) → Sub-Category (L2)** — independent of
the merchandise hierarchy. Vendor Product SKUs are tagged to nodes; a node may make items
eligible for allowances/promos.

### Fields

| Field | Rules |
|---|---|
| vendorId | required, must exist |
| level | `Category (L1)` · `Sub-Category (L2)` |
| parentCategoryId | required for L2; parent must be an L1 node **of the same vendor** |
| nodeName | required |
| nodeCode | optional |
| allowanceEligible | `Yes` · `No` |
| status | `Active` |

**Screen:** vendor selector loads that vendor's tree (L1 rows with expandable L2 rows).

**API:** `GET/POST /vendor-categories`, `GET/PUT /vendor-categories/{id}`.

---

## 6. Vendor Product (item ↔ vendor sourcing link)

The **Company · Vendor · SKU** record: every SKU a vendor can supply and the terms for
ordering it from them. The same SKU sourced from another vendor carries different terms.

### Fields

| Field | Rules |
|---|---|
| companyId, vendorId, productId | required, must exist |
| vendorItemNo | supplier's own SKU code |
| vendorCategoryId | node from this vendor's hierarchy (L2), nullable |
| sourcePriority | `Primary` · `Alternate` (default Alternate) |
| orderPackUom | chosen from the item's **orderable** pack UOM levels |
| moqCases, orderIncrementCases | integers, nullable |
| orderSchedule | display reference from Vendor Schedule (e.g. `Mon · Thu`) |
| leadTime | string |
| costPerCase | **CCM-owned, read-only via this API** — nulled on create, preserved on update; populated by published Cost Events |
| allowanceId | reference-only link to a Vendor Allowance |
| status | `Active` |

**Rules**
- **Single Primary per product:** saving a link as `Primary` demotes any other `Primary`
  link of the same product to `Alternate` (create and update paths).
- A line is **not orderable until an initial cost exists** (first CCM cost event) — the
  replenishment engine and PO entry must skip/flag costless lines.
- Uniqueness of (companyId, vendorId, productId) enforced. ⚑ DR-13

**API:** `GET /vendor-products` (`?productId=` filter), `POST`, `GET/PUT /{id}`.

---

## 7. Vendor Allowance

Trade agreements — listing fees, volume rebates, promo & display allowances; reference
data for cost netting and accruals (accrual computation itself is future/finance).

### Fields

| Field | Rules |
|---|---|
| ref | unique; auto `AL-{id}` if blank |
| vendorId | required, must exist |
| allowanceType | `Volume Rebate` · `Listing Fee` · `Promo Allowance` · `Display / Facing` · `Markdown Support` |
| basis | `% of Purchase` · `Fixed Amount` · `Per Case` |
| rateAmount | string in Phase 1 (e.g. `3.50%`, `₱ 12.00`); target model: typed `rateValue DECIMAL` + `rateUnit` ⚑ DR-12 |
| validFrom, validTo | dates |
| status | `Pending` · `Active` · `Expiring` |

**List stats:** active agreements, expiring < 30 days, pending approval.

**API:** `GET/POST /vendor-allowances`, `GET/PUT /vendor-allowances/{id}`.

---

## 8. Category Hierarchy (Merchandise tree)

One global tree: **Department (L1) → Sub-Department (L2) → Class (L3) → Sub-Class (L4)**.
Governs reporting, replenishment scoping and OTB planning. Vendor supply data lives on
the Vendor Product link, never on the tree.

### Fields (merch_node)

| Field | Rules |
|---|---|
| level | `Department (L1)` · `Sub-Department (L2)` · `Class (L3)` · `Sub-Class (L4)` |
| parentId | must exist; must be the level above ⚑ DR-18 |
| code | required, unique |
| name | required |
| merchManager, replenStrategy (`Auto (Min/Max)` · `Manual`), perishable, shelfLife, otbPlanned | optional metadata |
| status | `Active` |

**Rules**
- Creating a **Class (L3)** auto-scaffolds three Sub-Classes coded `{classCode}-101/-102/-103`
  named `101/102/103`; a class may then hold any number (add/remove freely).
- Products attach at Sub-Class level via `product.merchNodeId`.

**Screen:** full tree view with counts; node view/edit; New Node form with level + parent.

**API:** `GET /merch-nodes` (`?level=`, `?parentId=` — parentId takes precedence),
`POST`, `GET/PUT /{id}`.

---

## 9. Product

Master item catalog (SKU).

### Fields

| Field | Rules |
|---|---|
| code | unique; auto `100000 + id` when blank |
| description | required |
| primaryBarcode | 13-digit EAN (format validated ⚑ DR-19) |
| shortName | POS short name |
| brand | brand name (string reference to Brand master) |
| merchNodeId | Sub-Class node; must exist. UI captures it via a cascading Department → Sub-Department → Class → Sub-Class selector |
| perishable (`Yes/No`), shelfLife | |
| vatClass | `12% VATable` · `VAT Exempt` · `Zero-rated` |
| listingFee (`Yes/No`), storeRange | |
| buyingUom, sellingUom | strings |
| packUoms[] | list: level (`Base`·`Inner`·`Case`), uom (`PC`, `PACK-6`, `CASE-12`…), qtyInBase, barcode, orderable, sellable — Vendor Product order packs must pick an **orderable** level |
| standardCost, baseRetail | money; baseRetail is VAT-inclusive |
| attributes[] | list of {name, value}; each name must be an active Product Attribute and the value one of its Allowed Values ⚑ DR-20 |
| status | `Active` · `Discontinued` · `On Promo` · `House Brand` |

**Derived in responses:** `marginPct = (baseRetail − standardCost)/baseRetail`,
`preferredVendor` (Primary vendor-product link's vendor name).

**Screens:** list (search code/barcode/description; department/brand/status filters);
view (all fields + attributes); create/edit is tabbed **Details / Attributes** with a
dynamic attribute editor (add/remove rows; value dropdown restricted to allowed values).

**API:** `GET/POST /products`, `GET/PUT /products/{id}`.

---

## 10. Product Brand

| Field | Rules |
|---|---|
| code | unique; auto if blank |
| name | required |
| manufacturerId | required; must be a vendor whose vendorType contains `Manufacturer` |
| status, description | |
| productCount | derived |

**API:** `GET/POST /brands`, `GET/PUT /brands/{id}`.

---

## 11. Product Attribute

Reusable attribute definitions; **every attribute is a value list** — a product picks one
of its Allowed Values.

| Field | Rules |
|---|---|
| code | unique; auto if blank |
| name | required |
| allowedValues | required; comma-separated list (e.g. `XS, S, M, L, XL`), max 1000 chars |
| status, description | |

**API:** `GET/POST /product-attributes`, `GET/PUT /product-attributes/{id}`.
