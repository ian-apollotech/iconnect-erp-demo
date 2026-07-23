# 03 — API Conventions

The REST API is the hard contract between the Vue SPA and the Modulith backend, and the
integration surface for future systems. It is documented with OpenAPI (springdoc),
generated from code, and versioned.

## 1. General

| Concern | Convention |
|---|---|
| Base path | `/api/v1` ⚑ DR-09 |
| Resource names | kebab-case plural: `/companies`, `/price-zones`, `/vendor-products`, `/merch-nodes`, `/price-events`, `/store-replenishment-orders` |
| Media type | `application/json; charset=utf-8` |
| Verbs | `GET` list/detail · `POST` create · `PUT /{id}` full update · `DELETE /{id}` only where the domain allows hard delete (unreferenced masters) ⚑ DR-08 |
| Actions | Lifecycle transitions are sub-resource POSTs: `POST /price-events/{id}/submit`, `/approve`, `/publish`, `/cancel`; `POST /store-replenishment-orders/{id}/confirm`; `POST /sales-batches/{id}/release` ⚑ DR-03 |
| IDs | Numeric surrogate `id` in paths; business codes are payload fields |
| Dates | ISO-8601 (`2026-07-21` / `2026-07-21T06:00:00Z`) |
| Money | JSON numbers with 2 decimals, PHP implied |

## 2. Authentication & authorization

- Every endpoint (except health/docs) requires `Authorization: Bearer <Keycloak JWT>`.
- `401` invalid/missing token; `403` role lacks the permission (module × action) or the
  record is outside the user's access scope.
- `GET /api/v1/me` returns the caller's profile, effective permission matrix, and scope —
  the SPA's bootstrap call.

## 3. Error model

```json
// 400 — validation (bean validation)
{ "errors": { "code": "must not be blank", "companyId": "must not be null" } }

// 400 — business rule
{ "message": "Branch code already exists: 0102" }

// 400 — integrity backstop (DB-level duplicate or invalid reference)
{ "message": "Data integrity violation (duplicate or invalid reference)" }

// 404
{ "message": "Company 99 not found" }
```

Rules:
- Field validation errors → `errors` map (field → message), first error per field.
- Business-rule violations (unique code, FK existence, state guard) → single `message`.
- `404` only for a missing addressed resource; a bad FK inside a payload is `400`.
- Unhandled exceptions must not leak stack traces; generic `500` with correlation ID.

## 4. Collections: paging, sorting, filtering ⚑ DR-02

List endpoints accept:

```
GET /api/v1/products?page=0&size=50&sort=code,asc&q=milk&status=Active&merchNodeId=42
```

- `page`/`size` (default 0/50, max size 500), `sort=field,dir` repeatable.
- `q` — free-text search across the resource's designated searchable fields (the same
  fields as the UI list search).
- Named filter params per resource mirror the UI filter bar (documented per module).
- Response envelope for paged lists:

```json
{ "content": [ … ], "page": 0, "size": 50, "totalElements": 1234, "totalPages": 25 }
```

Small reference lists (companies, groupings, hierarchy nodes) may also be served unpaged
via `?size=all` for picker population.

## 5. Representation rules

- Request and response DTOs are distinct records; entities never serialize directly.
- Responses include server-derived fields (`branchCount`, `storeCount`, `marginPct`,
  `preferredVendor`, `skuCount`) which are absent from requests.
- Server-owned fields are ignored on write even if sent (e.g., `vendor-products.costPerCase`
  is CCM-owned; document numbers; statuses that only transitions may change).
- Unknown request fields are rejected in dev, ignored in prod (configurable strictness).
- Nested line collections (event lines, plan lines, document lines) are sent inline with
  the parent and fully replaced on `PUT` (full-replacement semantics).

## 6. Concurrency & idempotency

- Optimistic locking on documents: `version` field returned in responses, required on
  `PUT`/transition; `409` on stale version. ⚑ DR-17
- Transition endpoints are idempotent: re-posting an already-applied transition returns
  `409` with the current status in `message`.
- Bulk imports (adjustment count files, sales batches) carry a client reference; the same
  reference is rejected as duplicate.

## 7. Endpoint inventory (summary)

Full request/response field tables live in each module spec. Resource roots:

| Area | Resources |
|---|---|
| Master data | `/companies`, `/branches`, `/price-zones`, `/cost-zones`, `/assortment-clusters`, `/replenishment-profiles`, `/vendors`, `/vendor-categories`, `/vendor-products`, `/vendor-allowances`, `/merch-nodes` (`?level=`, `?parentId=`), `/products`, `/brands`, `/product-attributes` |
| Merchandising | `/price-events`, `/cost-events` (+ `/submit`, `/approve`, `/publish`, `/cancel`; `GET /current-prices`, `GET /current-costs` resolution queries) |
| Planning | `/assortment-plans`, `/demand-signals`, `/demand-overlays`, `/replenishment-policies`, `/vendor-schedules`, `/order-calendar-exceptions`, `/replenishment-runs` (+ `POST /replenishment-runs` ad-hoc), `/store-replenishment-orders` (+ `/confirm`, `/export`) |
| Procurement | `/purchase-orders` (+ transitions), `/dsd-deliveries`, `/otb-budgets`, `/multi-store-pos`, `/goods-receipts` (+ `/post`), `/asns` |
| Inventory | `/stock-positions` (netting/warehouse/selling-area views), `/merchandise-issuances`, `/inventory-adjustments` (+ `/approve`, `/post`), `/stock-transfers` (+ transitions), `/rtvs` (+ transitions), `/conversions` (+ `/post`) |
| Sales | `/sales-batches` (+ `/validate`, `/release`), nested `/transactions`, balancing summaries |
| Admin | `/users`, `/roles`, `/access-scopes`, `/audit-log`, `/me` |

## 8. API evolution

- Additive changes (new optional fields, new endpoints) do not bump the version.
- Breaking changes require `/api/v2` alongside `/api/v1` for a deprecation window.
- OpenAPI document published per environment; the SPA build pins to the contract version.
