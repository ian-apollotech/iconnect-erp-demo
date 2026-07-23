# 15 — Sales (POS Sales Batches & Balancing)

Modulith module: `sales`. Ingest POS sales batches and balance them — mode of payment,
cashier, and total sales — before releasing to finance & inventory. Prototype coverage
was front-end only; specified to target behaviour ⚑ DR-06.

Phase 1 source is a **manual file upload** per branch per business date; automated POS
feed is integration point I-1.

---

## 1. Sales Batch

| Field | Rules |
|---|---|
| batchNo | auto `SB-…` |
| branchId | required |
| businessDate | required; (branch, businessDate) unique — a re-upload replaces only via explicit Reprocess |
| sourceFile | uploaded POS export (e.g. `POS_20260712.dat`); stored for audit |
| terminalsExpected / terminalsLoaded | integers |
| transactionCount, grossSales, discounts, voidsRefunds, netSales, vatAmount | derived from transactions; `Net = Gross − Discounts − Returns/Voids`; VAT 12% computed on net |
| loadedAt | ingest timestamp |
| status | see §3 |

### Transactions (per batch)

orNumber/txnNo, terminal, cashier, time, item count, gross, discount, net, tender
(`Cash` · `Credit / Debit Card` · `GCash / e-Wallet` · `Maya` · `Gift Card / Voucher` ·
`SC / PWD Discount`), exception flags (`Void` · `Refund` · `Price Override` ·
`SC/PWD Disc` · `Mgr Discount`). Line-level item detail is accepted when present in the
file (needed for inventory deduction).

## 2. Balancing (three reconciliations)

| Balancing | Compares | Outcome |
|---|---|---|
| **Mode of Payment** | POS-declared per tender vs settled/banked amount | per-tender `Balanced` / `Short` / `Over` with variance |
| **Cashier** | per-cashier system sales vs remitted (per terminal, shift AM/PM) | over/short per cashier |
| **Sales** | end-to-end: gross → discounts → returns/voids → net → VAT vs total tenders; net MOP variance, cashier net O/S, inventory movement posted | overall `Balanced` / `For Review` |

Settled/banked and remitted figures are entered/imported by the sales auditor in Phase 1.

## 3. Batch lifecycle

```
Uploaded → Validating → Loaded → (Balanced | MOP Variance | Load Error)
        → Released                       (variance sign-off path)
```

- **Load Error:** file unreadable / terminal count mismatch / duplicate — batch rejected
  with error detail; Reprocess re-runs ingest.
- **Balanced:** all three reconciliations clean → releasable.
- **Variance states:** batch remains open; variances route to the branch accountant for
  sign-off (`Flag to Finance`), after which release is allowed with the variance recorded.
- **Release** (`POST /sales-batches/{id}/release`): emits `SalesBatchReleased` —
  inventory deducts sold quantities (ledger, at average cost), demand history updates
  (`12 §3`), and the batch locks. Finance hand-off is integration point I-3.

## 4. Screens

- **List / landing:** stats (batches today, gross sales, unbalanced, cashier shortage);
  tabs: Batch Upload (list + filters business date/branch/status), MOP Balancing,
  Cashier Balancing, Sales Balancing (per-branch reconciliation panels with
  `Flag to Finance` / `Release Batch`).
- **Batch detail:** header KPIs (transactions, gross, net, terminals, status), batch
  header panel (source file, uploaded, discounts, voids), tabs: Transactions (filter
  terminal/tender; searchable), By Terminal, By Tender (MOP mix), Exceptions (voids,
  refunds, overrides, discounts with notes).
- **Upload Batch** form: business date, branch, source file, terminals expected →
  `Upload & Validate`.

## 5. API

`POST /sales-batches` (multipart upload), `GET /sales-batches` (+filters),
`GET /sales-batches/{id}`, nested `GET …/transactions`, balancing summaries
(`…/mop-balancing`, `…/cashier-balancing`, `…/sales-balancing`),
`POST …/{id}/revalidate`, `POST …/{id}/release`.

## 6. Permissions

Sales Auditor: upload, balance, flag, release within scoped branches. Store Manager:
read own store. Finance sign-off role: approve variance release.
