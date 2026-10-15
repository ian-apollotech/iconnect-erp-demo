# 05 — Decision Register (⚑ specified beyond prototype)

Every rule in this document set that goes **beyond what the prototype demonstrated** is
listed here for review. Each is referenced inline as ⚑ DR-nn where it applies. Overturn a
decision here and the referencing spec section is what changes.

| # | Decision | Rationale | Where |
|---|---|---|---|
| DR-01 | Keycloak owns identity + role membership; ERP owns role catalog, permission matrix, and access scope; ERP is the sole admin interface (provisions via Admin API). | User-directed split; keeps JWT-carried roles while retaining fine-grained ERP permissions. | 01 §3, 16 |
| DR-02 | Server-side paging/sorting/filtering with a paged envelope (prototype loaded full lists client-side). | Client-side lists don't survive 100k SKUs / 600 stores. | 03 §4 |
| DR-03 | Explicit lifecycle state machines + transition endpoints + domain event catalog (prototype showed statuses as static badges only). | A source of truth must define who moves a document and what each move triggers. | 01 §2.3, 03 §1, 11 §1, 12 §7, 13, 14, 15 |
| DR-04 | Auto Replenishment formalized as a scheduled backend job with run records, leader election, and idempotent re-run. | Prototype showed run output only. | 01 §6, 12 §7 |
| DR-05 | DB portability regime: Flyway common + per-vendor folders, portable type palette, app-set timestamps, no triggers/vendor features, CI on both engines. | User requires MariaDB ⇄ MS SQL switchability. | 01 §4, 02 §2 |
| DR-06 | Front-end-only prototype modules (Procurement, Inventory, Sales, Administration, Demand, Replen engine) specified to full backend behaviour — lifecycles, postings, costing. | Phase 1 covers all mockup modules; mockups implied but did not implement these rules. | 13, 14, 15, 16, 12 §3/§7 |
| DR-07 | Audit columns (`created/updated at/by`) on all tables + automatic audit-log interceptor. | Prototype had an audit page but no capture mechanism. | 02 §2, 16 §5 |
| DR-08 | Soft-delete-by-status for referenced masters; hard DELETE only for unreferenced rows. Prototype exposed no delete at all. | Referential safety with cleanup ability. | 02 §2, 03 §1 |
| DR-09 | API base `/api/v1` with additive-vs-breaking versioning policy (prototype used an unversioned `/backend` prefix). | Contract stability for SPA + future integrations. | 03 §1 |
| DR-10 | Store-grouping membership stays a single nullable FK per grouping type on `branch` (no membership join table); grouping editors persist by updating branches. | Matches the working backend model; enforces one-membership-per-type naturally. | 02 §3, 10 §3 |
| DR-11 | Assortment Plan targets extended to all three levels (Company / Cluster / Store) with typed target fields — the prototype API required a Cluster for every plan while its UI offered all three. | Resolves a prototype UI↔API mismatch in favour of the documented Store › Cluster › Company precedence. | 12 §2 |
| DR-12 | Vendor Allowance rate becomes typed (`rateValue` decimal + `rateUnit`) instead of free-text `"3.50%"`. | Free text can't drive future accrual computation. | 10 §7 |
| DR-13 | Unique constraint on vendor-product (companyId, vendorId, productId). | Implied by "the Company · Vendor · SKU record"; prototype didn't enforce it. | 10 §6 |
| DR-14 | Portable `document_sequence` table for all document numbers and auto-codes; prototype formats retained (`V-{id}`, `100000+id`, `PC-…`, `SRO-{store}-{date}`, …). | Deterministic, gap-tolerant numbering on both DB vendors. | 02 §1 |
| DR-15 | `inventory_location` becomes a real entity seeded from branch Format via `BranchCreated` (prototype stored a descriptive string on branch). | Stock, issuance, and receiving all need a location grain. | 10 §2, 14 §1 |
| DR-16 | Inventory kept as append-only `stock_ledger` + maintained `stock_position`, moving-average costing at receipt. | Prototype showed positions with static numbers; a ledger is the only auditable base. | 14 §2 |
| DR-17 | Optimistic locking (`version`) on documents; idempotent transition endpoints (409 semantics). | Multi-user document editing safety. | 03 §6 |
| DR-18 | Merch-node parent must be exactly one level above the child (prototype validated existence only). | Keeps the 4-level tree well-formed. | 10 §8 |
| DR-19 | Barcode format validation (13-digit EAN) on product primaryBarcode. | Prototype hinted the format but did not validate. | 10 §9 |
| DR-20 | Product attribute values validated against the attribute's Allowed Values server-side (prototype enforced only in the UI dropdown). | Server is the source of truth. | 10 §9, §11 |
| DR-21 | Price/cost resolution precedence formalized: Store > Zone > Company; latest effective wins; in-window Promo beats Regular; base retail / standard cost as fallback; exposed via `/current-prices`, `/current-costs`. | The prototype described the precedence in prose but had no resolver. | 11 §2.5, §3.5 |
| DR-22 | Replenishment Policy scope moves from free-text `scopeRef` to typed `merchNodeId` / `productId`. | Free text can't resolve most-specific-wins reliably. | 12 §4 |
| DR-23 | Vendor Schedule unique per (companyId, vendorId). | "The key is Company · Vendor" — prototype didn't enforce it. | 12 §5 |
| DR-24 | SRO status set extended with `Cancelled` (discard path) alongside Pending Confirmation → Confirmed → Exported. | Buyers need a way to discard a generated order. | 12 §7.4 |
| DR-25 | Audit-log entries for edits carry a before/after field summary. | "Edit" rows without a diff have little audit value. | 16 §5 |
| DR-26 | **(Phase 2)** AP subledger lives in the ERP; GL/CoA stays external — postings map to external account codes and hand off journal lines (I-3 narrowed). | Full AP control without duplicating Finance's ledger. | 17 §1, §2.5, §8 |
| DR-27 | **(Phase 2)** Vendor invoice numbers use `VI-…`; `AP-…` already belongs to Assortment Plan. | Sequence collision in `02 §1`. | 17 §3.1 |
| DR-28 | **(Phase 2)** Philippine tax model: input VAT codes + BIR EWT (ATC) + Form 2307 at payment. No US 1099 concepts. | Deployment is Philippine (`00 §7`, `01 §7`). | 17 §2.2, §6.3 |
| DR-29 | **(Phase 2)** PHP-only AP; no FX/exchange-rate table. | Planning assumption `00 §7`; multi-currency would be a breaking change anyway. | 17 §1.2 |
| DR-30 | **(Phase 2)** Posting-period entity with Open/Closed control for AP postings; company `fiscalYearStart` reused as the calendar anchor. | Company master has fiscal defaults but no period close. | 17 §2.3 |
| DR-31 | **(Phase 2)** RTV debit memos (`DM-…`) become open credit items netted in payment runs — the "payables reversal, future finance hand-off" of `14 §6`. | The document already exists in Phase 1; AP is its consumer. | 17 §4.1 |
| DR-32 | **(Phase 2)** Vendor allowance accrual computed from typed rates (DR-12) into approvable allowance credit memos, settled as payment deductions. | Closes the deferred accrual item below. | 17 §4.2 |
| DR-33 | **(Phase 2)** AP ends at payment execution + manual/imported `Cleared`; full bank reconciliation stays Finance-side. | Keeps the module inside the merchandising ERP's boundary. | 17 §6.2 |
| DR-34 | **(Phase 2)** Vendor master extended with remit-to bank details, payment method, EWT class; `tin` required for Active vendors when payables is enabled. | AP cannot pay or withhold without them. | 17 §2.1, 10 §4 |
| DR-35 | **(Phase 2)** GR/IR accrual is a computed view over GRN-vs-invoiced quantities at receipt cost — no manual accrual document. | The stock ledger and GRN costing already carry the values. | 17 §5 |

## Open items intentionally deferred (not decisions)

- ~~Accrual computation for vendor allowances (finance scope, integration I-3).~~
  Now specified for Phase 2 — DR-32, `17 §4.2`.
- Automated POS feed, WMS live sync — integration points I-1, I-4 (`01 §8`). GL hand-off
  (I-3) is narrowed by DR-26: AP subledger specified in `17`; GL/CoA and bank
  reconciliation remain deferred Finance-side.
- Markdown management for OTB sell-through (mentioned in Procurement; scheduling a
  markdown module is a future phase).
