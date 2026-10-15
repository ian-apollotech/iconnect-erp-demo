# 16 — Administration (Users · Roles · Branch Access · Audit Log)

Modulith module: `admin`. Identity model: **Keycloak owns identity and role membership;
the ERP owns the role catalog, permissions, and access scope, and is the only admin
interface** (`01 §3`, ⚑ DR-01). Prototype coverage was front-end only; specified to
target behaviour ⚑ DR-06.

---

## 1. Users

The ERP `user_account` mirrors each Keycloak identity (`keycloak_subject_id`) and holds
ERP-only profile data.

### Fields

| Field | Owner | Rules |
|---|---|---|
| employeeId | ERP | required, unique (e.g. `EMP-00742`) |
| fullName | both | required; synced to Keycloak profile |
| username | Keycloak | required, unique |
| email | Keycloak | |
| role | Keycloak realm role(s) | required; assigned via ERP → Admin API |
| department | ERP | `Merchandising — Grocery / Fresh / Non-Food` · `Store Operations` · `Supply Chain / Warehouse` · `Sales Audit / Finance` · `IT / Administration` |
| reportsTo | ERP | optional user reference |
| accessScope | ERP | `Company-wide` · `By Region` · `By Store` + the region / home branch value (§4) |
| status | both | `Active` · `Inactive` · `Locked` (locked = Keycloak account disabled/locked) |
| mfa | Keycloak | Enabled/Disabled |
| temporaryPassword / forceReset | Keycloak | create-time only; blank auto-generates |
| lastLogin | Keycloak (event) | display |

### Behaviour

- **Create User** (single ERP form): provisions the Keycloak user (username, email, temp
  password, required-action reset, MFA), assigns the realm role, writes the ERP mirror +
  scope rows. Rollback on partial failure (no orphan identities). Emits `UserProvisioned`.
- **Edit** updates both sides as ownership dictates; role change re-maps realm roles.
- Deactivate/lock disables login immediately (Keycloak session revocation).

**API:** `GET/POST /users`, `GET/PUT /users/{id}`, `POST /users/{id}/lock|unlock`.

## 2. Roles

Role catalog + description live in the ERP; each role also exists as a Keycloak realm
role (name-linked) so membership can travel in the JWT.

| Field | Rules |
|---|---|
| name | required, unique |
| description | |
| type | `System` (protected — cannot be deleted; System Administrator always has the full matrix) · `Custom` |
| status | `Active` · `Inactive` |
| copyPermissionsFrom | create-time helper: seed the matrix from another role |
| userCount, modulesGranted | derived |

## 3. Permission matrix

Per role: **module group × action** grants.

- Actions: **View · Create · Edit · Approve**.
- Module groups (rows): `Master Data`, `Inventory & Pricing`, `Price Change`,
  `Cost Change`, `Purchase Order`, `Auto Replenishment`, `PO Receiving`,
  `Store Operations`, `Sales Audit`, `Administration`. Phase 2 adds
  `Accounts Payable` and `Payments` (`17 §10`), plus standard roles AP Clerk and
  AP Supervisor (department `Sales Audit / Finance`).
- Semantics: View gates navigation + read APIs; Create/Edit gate mutations; Approve
  gates lifecycle approval transitions (`11 §1`, `13 §2.3`, `14 §4`). Server-side
  enforcement is authoritative (`03 §2`).
- Editing a role's matrix applies to every user holding the role immediately.

**API:** `GET/POST /roles`, `GET/PUT /roles/{id}` (matrix inline).

## 4. Branch Access (scope)

Answers "who can access a given Company / Region / Store", and stores each user's scope.

### Model

`access_scope` rows per user: scopeLevel `Company` · `Region` · `Store` + value
(companyId / region / branchId). A user may hold multiple rows (e.g. several stores).

### Resolution

- **Direct** — an explicit row at the queried level.
- **Inherited** — a broader row implies the narrower one (Company-wide ⊃ every region and
  store of that company; Region ⊃ its stores).
- Data access filtering: store-scoped users see only their branches' documents/stock;
  region-scoped see their region; company-wide see all (per company).

### Screen

Scope Level selector (Company/Region/Store) with dependent value list → users table with
access badge (`Direct` / `Inherited` + detail), Revoke (direct rows only), Assign User.

**API:** `GET /access-scopes?level=&value=`, `POST /access-scopes`,
`DELETE /access-scopes/{id}`.

## 5. Audit Log

Chronological record of user activity — logins, record changes, approvals.

| Field | Notes |
|---|---|
| timestamp | UTC, displayed local |
| user | username |
| action | `Login` · `Create` · `Edit` · `Approve` · `Delete` (+ failed-login events) |
| module | functional area |
| target | human-readable record reference (e.g. `PO-2026-0417 · Nestlé Phils.`) |
| ipAddress | source |
| detail | before/after summary for edits ⚑ DR-25 |

- Written automatically by an auditing interceptor on every mutating service call and by
  the auth event listener (Keycloak login events ingested).
- Read-only UI with filters (user, action, date, search) and export; retention ≥ 5 years
  (`01 §7`).

**API:** `GET /audit-log` (paged, filtered). No mutation endpoints.
