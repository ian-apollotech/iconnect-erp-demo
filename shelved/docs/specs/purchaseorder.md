# iConnect ERP — Purchase Order Module Specification

## Table of Contents
1. [Overview](#1-overview)
2. [System Architecture](#2-system-architecture)
3. [Tech Stack & Dependencies](#3-tech-stack--dependencies)
4. [Roles & Permissions](#4-roles--permissions)
5. [Multi-Tenancy Model](#5-multi-tenancy-model)
6. [Authentication & Authorization](#6-authentication--authorization)
7. [Database Schema](#7-database-schema)
8. [Backend API Specification](#8-backend-api-specification)
9. [Frontend Specification (Quasar)](#9-frontend-specification-quasar)
10. [Business Logic & Rules](#10-business-logic--rules)
11. [Notifications](#11-notifications)
12. [File Attachments](#12-file-attachments)
13. [Reporting](#13-reporting)
14. [Non-Functional Requirements](#14-non-functional-requirements)
15. [Future Considerations](#15-future-considerations)

---

## 1. Overview

iConnect ERP is a multi-tenant procurement platform that allows organizations to manage their Purchase Order (PO) lifecycle — from creation through approval or rejection — in a structured, auditable workflow.

### 1.1 Purpose
The Purchase Order module covers the full manual PO creation and approval workflow as defined in the iConnectERP Purchase Order process diagram:

```
New PO → Select Buyer → Select Seller → PO Details →
  Multiple Locations? → Add/Select Facilities →
  Select Items → Set Quantities (with MOQ enforcement) →
  Submit → Approve / Reject (revise loop) / Cancel
```

### 1.2 Key Concepts
| Term | Definition |
|------|-----------|
| **Organization** | A tenant on the platform. Created by Super Admin. |
| **Buyer** | A user within an organization who creates and submits POs. |
| **Approver** | A user within an organization who reviews and approves/rejects POs. |
| **Seller** | A vendor/supplier record maintained by the organization. Sellers do not log in. |
| **Facility** | A delivery address/destination (store, warehouse, branch) owned by the organization. |
| **Item** | A product in the organization's master catalog, linked to a Seller, with price and MOQ. |
| **MOQ** | Minimum Order Quantity. The minimum quantity that must be ordered per item. |

---

## 2. System Architecture

### 2.1 High-Level Architecture

```
┌────────────────────────────────────────────┐
│            Quasar SPA (Vue 3)              │
│        served as static files / CDN        │
└──────────────────┬─────────────────────────┘
                   │ HTTPS REST API
┌──────────────────▼─────────────────────────┐
│         Spring Boot 3.x (Java 21)          │
│  ┌──────────┐ ┌──────────┐ ┌───────────┐  │
│  │  Auth    │ │   API    │ │  Email    │  │
│  │  Filter  │ │ Controllers│ │  Service  │  │
│  └──────────┘ └──────────┘ └───────────┘  │
│  ┌──────────────────────────────────────┐  │
│  │         Service Layer                │  │
│  └──────────────────────────────────────┘  │
│  ┌──────────────────────────────────────┐  │
│  │     JPA Repositories (Hibernate)     │  │
│  └──────────────────────────────────────┘  │
└──────────────────┬─────────────────────────┘
                   │
┌──────────────────▼─────────────────────────┐
│              MariaDB                        │
└─────────────────────────────────────────────┘
```

### 2.2 Multi-Tenancy Strategy
Application-level multi-tenancy: every entity in the database carries an `organization_id` column. All repository queries and service methods are scoped to the authenticated user's `organizationId`. No cross-tenant data leakage is possible at the service layer.

### 2.3 File Storage
File attachments are stored on the local filesystem (configurable base path) organized as:
```
/uploads/{organization_id}/{po_id}/{uuid}-{original_filename}
```
The path is configurable via `application.yml` to allow migration to object storage (S3, MinIO) later.

---

## 3. Tech Stack & Dependencies

### 3.1 Backend — Java 21 / Spring Boot 3.x

| Dependency | Purpose |
|-----------|---------|
| `spring-boot-starter-web` | REST API |
| `spring-boot-starter-security` | Authentication & authorization |
| `spring-boot-starter-data-jpa` | ORM with Hibernate |
| `spring-boot-starter-mail` | Email notifications |
| `spring-boot-starter-validation` | Bean validation |
| `mariadb-java-client` | MariaDB JDBC driver |
| `flyway-core` | Database schema migrations |
| `jjwt-api` + `jjwt-impl` + `jjwt-jackson` | JWT token generation/validation |
| `lombok` | Boilerplate reduction |
| `mapstruct` | DTO ↔ entity mapping |
| `apache-poi` + `poi-ooxml` | Excel report generation |
| `itextpdf` or `jasperreports` | PDF report generation |
| `springdoc-openapi-starter-webmvc-ui` | Swagger/OpenAPI docs |

**Java version:** 21 (use virtual threads via `spring.threads.virtual.enabled=true`)

**Package structure:**
```
com.iconnect.erp
├── auth/           # JWT filter, token service, login endpoint
├── config/         # Security config, CORS, beans
├── common/         # Base entity, exception handling, response wrapper
├── organization/   # Super admin org management
├── user/           # User CRUD, role management
├── seller/         # Vendor/supplier management
├── catalog/        # Item categories and items
├── facility/       # Delivery locations
├── purchaseorder/  # PO domain (entities, services, controllers)
├── notification/   # In-app and email notifications
├── attachment/     # File upload/download
└── report/         # Report generation and export
```

### 3.2 Frontend — Quasar Framework (Vue 3)

| Tool | Purpose |
|------|---------|
| Quasar CLI | Project scaffolding, build, dev server |
| Vue 3 (Composition API) | UI framework |
| Pinia | State management |
| Vue Router 4 | Client-side routing |
| Axios | HTTP client |
| VeeValidate + Yup | Form validation |
| Day.js | Date formatting |
| XLSX (SheetJS) | Client-side Excel preview (optional) |

**Quasar components used:** `QLayout`, `QDrawer`, `QStepper`, `QTable`, `QDialog`, `QForm`, `QInput`, `QSelect`, `QFile`, `QChip`, `QBadge`, `QBanner`, `QNotify`, `QAvatar`, `QSeparator`, `QMenu`

**Color palette (quasar.config.js brand):**
```js
brand: {
  primary:   '#DC2626',  // red
  secondary: '#334155',  // dark gray
  accent:    '#B91C1C',  // dark red
  dark:      '#111827',
  positive:  '#059669',
  negative:  '#DC2626',
  info:      '#334155',
  warning:   '#D97706',
}
```

### 3.3 Database — MariaDB
- Version: 10.6+
- Character set: `utf8mb4`
- Collation: `utf8mb4_unicode_ci`
- Engine: InnoDB
- Migrations managed by Flyway (versioned SQL scripts in `resources/db/migration/`)

---

## 4. Roles & Permissions

### 4.1 Role Definitions

| Role | Scope | Description |
|------|-------|-------------|
| `SUPER_ADMIN` | Platform | Creates and manages organizations. Can impersonate any org admin. |
| `ORG_ADMIN` | Organization | Manages users, sellers, items, facilities within their organization. |
| `BUYER` | Organization | Creates, edits, submits, and cancels POs. Runs reports. |
| `APPROVER` | Organization | Reviews submitted POs. Approves or rejects. Runs reports. |

> A user may hold only one role at a time. A user cannot be both BUYER and APPROVER simultaneously.

### 4.2 Permission Matrix

| Action | SUPER_ADMIN | ORG_ADMIN | BUYER | APPROVER |
|--------|:-----------:|:---------:|:-----:|:--------:|
| Create Organization | ✓ | | | |
| Manage Users (org) | ✓ | ✓ | | |
| Manage Sellers | | ✓ | | |
| Manage Item Catalog | | ✓ | | |
| Manage Facilities | | ✓ | | |
| Create PO | | | ✓ | |
| Edit PO (CREATED/REJECTED) | | | ✓ | |
| Submit PO | | | ✓ | |
| Cancel PO | | | ✓ | |
| Approve PO | | | | ✓ |
| Reject PO | | | | ✓ |
| View all POs in org | | ✓ | ✓* | ✓ |
| Run Reports | | ✓ | ✓ | ✓ |

> *BUYER sees all POs in their organization, not just their own, for visibility.

---

## 5. Multi-Tenancy Model

### 5.1 Organization Lifecycle
1. `SUPER_ADMIN` creates an organization with a name and unique code.
2. `SUPER_ADMIN` creates the first `ORG_ADMIN` user for that organization.
3. `ORG_ADMIN` logs in and sets up: users, sellers, item catalog, facilities.
4. `BUYER` and `APPROVER` users can then operate within the organization.

### 5.2 Data Isolation Rules
- Every table (except `organizations` and `users`) has an `organization_id` FK.
- All service-layer queries include a `WHERE organization_id = ?` clause derived from the JWT token.
- Spring Security `@PreAuthorize` annotations enforce role-level access.
- `SUPER_ADMIN` can query across organizations for administrative purposes only.

### 5.3 Organization Entity Fields
| Field | Type | Notes |
|-------|------|-------|
| `id` | UUID | Primary key |
| `name` | VARCHAR(200) | Display name |
| `code` | VARCHAR(50) | Unique slug (e.g. `global-retail`) |
| `status` | ENUM | `ACTIVE`, `INACTIVE` |
| `created_at` | DATETIME | |
| `updated_at` | DATETIME | |

---

## 6. Authentication & Authorization

### 6.1 Current Implementation (JWT)
- Login via `POST /api/auth/login` with `{ email, password }`.
- Returns `{ accessToken, refreshToken, expiresIn, user }`.
- Access token: short-lived (15 minutes), signed HS256 JWT.
- Refresh token: long-lived (7 days), stored in `refresh_tokens` table.
- All secured endpoints require `Authorization: Bearer <accessToken>` header.
- JWT payload includes: `sub` (userId), `org` (organizationId), `role`, `iat`, `exp`.

### 6.2 JWT Filter Chain
```
Request → JwtAuthFilter → validate token → set SecurityContext → Controller
```
The filter extracts `organizationId` and `role` from the JWT and injects them into a `TenantContext` thread-local, used by all service methods.

### 6.3 Keycloak Readiness
- The `UserDetailsService` and authentication provider are abstracted behind an interface (`AuthProvider`).
- A `KeycloakAuthProvider` implementation can be swapped in by changing `application.yml` config.
- Token validation logic is isolated in `JwtService` — replaceable with Keycloak's JWKS validation.
- All role names (`BUYER`, `APPROVER`, etc.) should match the Keycloak realm role names planned for integration.

### 6.4 Password Policy
- Minimum 8 characters, at least one uppercase, one number, one special character.
- Passwords stored as BCrypt hashes (strength 12).
- First-login forced password change for accounts created by admin.

---

## 7. Database Schema

### 7.1 Entity Relationship Summary
```
organizations
  └── users
  └── sellers
  └── item_categories
       └── items (→ sellers)
  └── facilities
  └── purchase_orders (→ users/buyer, → sellers)
       └── po_facilities (→ facilities)
       └── po_items (→ items)
            └── po_item_quantities (→ po_facilities)
       └── po_attachments (→ users)
       └── po_status_history
  └── notifications (→ users, → purchase_orders)

refresh_tokens (→ users)
```

### 7.2 Table Definitions

#### `organizations`
```sql
CREATE TABLE organizations (
  id          CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  name        VARCHAR(200) NOT NULL,
  code        VARCHAR(50)  NOT NULL UNIQUE,
  status      ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

#### `users`
```sql
CREATE TABLE users (
  id                    CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  organization_id       CHAR(36)     NOT NULL,
  email                 VARCHAR(255) NOT NULL,
  password_hash         VARCHAR(255) NOT NULL,
  first_name            VARCHAR(100) NOT NULL,
  last_name             VARCHAR(100) NOT NULL,
  role                  ENUM('SUPER_ADMIN','ORG_ADMIN','BUYER','APPROVER') NOT NULL,
  status                ENUM('ACTIVE','INACTIVE','PENDING_PASSWORD_CHANGE') NOT NULL DEFAULT 'PENDING_PASSWORD_CHANGE',
  created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_user_email (email),
  FOREIGN KEY (organization_id) REFERENCES organizations(id)
);
```

#### `refresh_tokens`
```sql
CREATE TABLE refresh_tokens (
  id          CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  user_id     CHAR(36)     NOT NULL,
  token_hash  VARCHAR(255) NOT NULL UNIQUE,
  expires_at  DATETIME     NOT NULL,
  revoked     TINYINT(1)   NOT NULL DEFAULT 0,
  created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### `sellers`
```sql
CREATE TABLE sellers (
  id              CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  organization_id CHAR(36)     NOT NULL,
  name            VARCHAR(200) NOT NULL,
  contact_name    VARCHAR(150),
  contact_email   VARCHAR(255),
  contact_phone   VARCHAR(50),
  address         TEXT,
  country         VARCHAR(100),
  status          ENUM('ACTIVE','INACTIVE','ON_REVIEW') NOT NULL DEFAULT 'ACTIVE',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (organization_id) REFERENCES organizations(id)
);
```

#### `item_categories`
```sql
CREATE TABLE item_categories (
  id              CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  organization_id CHAR(36)     NOT NULL,
  name            VARCHAR(100) NOT NULL,
  description     TEXT,
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (organization_id) REFERENCES organizations(id)
);
```

#### `items`
```sql
CREATE TABLE items (
  id              CHAR(36)       NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  organization_id CHAR(36)       NOT NULL,
  seller_id       CHAR(36)       NOT NULL,
  category_id     CHAR(36),
  sku             VARCHAR(100)   NOT NULL,
  name            VARCHAR(200)   NOT NULL,
  description     TEXT,
  unit            VARCHAR(50)    NOT NULL COMMENT 'e.g. bags, btls, cases, packs',
  price           DECIMAL(15,2)  NOT NULL,
  moq             INT            NOT NULL DEFAULT 1,
  status          ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_item_sku_org (sku, organization_id),
  FOREIGN KEY (organization_id) REFERENCES organizations(id),
  FOREIGN KEY (seller_id)       REFERENCES sellers(id),
  FOREIGN KEY (category_id)     REFERENCES item_categories(id)
);
```

#### `facilities`
```sql
CREATE TABLE facilities (
  id              CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  organization_id CHAR(36)     NOT NULL,
  name            VARCHAR(150) NOT NULL,
  address         TEXT,
  contact_name    VARCHAR(150),
  contact_phone   VARCHAR(50),
  status          ENUM('ACTIVE','INACTIVE') NOT NULL DEFAULT 'ACTIVE',
  created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (organization_id) REFERENCES organizations(id)
);
```

#### `purchase_orders`
```sql
CREATE TABLE purchase_orders (
  id                   CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  organization_id      CHAR(36)      NOT NULL,
  po_number            VARCHAR(50)   NOT NULL,
  buyer_id             CHAR(36)      NOT NULL,
  seller_id            CHAR(36)      NOT NULL,
  status               ENUM('CREATED','SUBMITTED','APPROVED','REJECTED','CANCELLED')
                                     NOT NULL DEFAULT 'CREATED',
  order_date           DATE          NOT NULL,
  delivery_date        DATE          NOT NULL,
  delivery_window      ENUM('ANY_TIME','MORNING','AFTERNOON','END_OF_DAY')
                                     NOT NULL DEFAULT 'ANY_TIME',
  payment_terms        ENUM('COD','NET_30','NET_45','NET_60','FIFTY_FIFTY')
                                     NOT NULL DEFAULT 'NET_30',
  currency             CHAR(3)       NOT NULL DEFAULT 'PHP',
  multi_location       TINYINT(1)    NOT NULL DEFAULT 0,
  shipping_instructions TEXT,
  internal_notes       TEXT,
  rejection_reason     TEXT,
  submitted_at         DATETIME,
  actioned_at          DATETIME,
  actioned_by          CHAR(36),
  cancelled_at         DATETIME,
  cancelled_by         CHAR(36),
  cancellation_reason  TEXT,
  created_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at           DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uq_po_number_org (po_number, organization_id),
  FOREIGN KEY (organization_id) REFERENCES organizations(id),
  FOREIGN KEY (buyer_id)        REFERENCES users(id),
  FOREIGN KEY (seller_id)       REFERENCES sellers(id),
  FOREIGN KEY (actioned_by)     REFERENCES users(id),
  FOREIGN KEY (cancelled_by)    REFERENCES users(id)
);
```

#### `po_facilities`
```sql
CREATE TABLE po_facilities (
  id           CHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  po_id        CHAR(36) NOT NULL,
  facility_id  CHAR(36) NOT NULL,
  sort_order   INT      NOT NULL DEFAULT 0,
  FOREIGN KEY (po_id)       REFERENCES purchase_orders(id) ON DELETE CASCADE,
  FOREIGN KEY (facility_id) REFERENCES facilities(id)
);
```

#### `po_items`
```sql
CREATE TABLE po_items (
  id             CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  po_id          CHAR(36)      NOT NULL,
  item_id        CHAR(36)      NOT NULL,
  unit_price     DECIMAL(15,2) NOT NULL COMMENT 'snapshot at PO creation time',
  moq            INT           NOT NULL COMMENT 'snapshot at PO creation time',
  unit           VARCHAR(50)   NOT NULL COMMENT 'snapshot at PO creation time',
  FOREIGN KEY (po_id)   REFERENCES purchase_orders(id) ON DELETE CASCADE,
  FOREIGN KEY (item_id) REFERENCES items(id)
);
```

#### `po_item_quantities`
```sql
CREATE TABLE po_item_quantities (
  id             CHAR(36) NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  po_item_id     CHAR(36) NOT NULL,
  po_facility_id CHAR(36) NOT NULL,
  quantity       INT      NOT NULL DEFAULT 0,
  UNIQUE KEY uq_qty (po_item_id, po_facility_id),
  FOREIGN KEY (po_item_id)     REFERENCES po_items(id) ON DELETE CASCADE,
  FOREIGN KEY (po_facility_id) REFERENCES po_facilities(id) ON DELETE CASCADE
);
```

#### `po_attachments`
```sql
CREATE TABLE po_attachments (
  id               CHAR(36)      NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  po_id            CHAR(36)      NOT NULL,
  uploaded_by      CHAR(36)      NOT NULL,
  original_filename VARCHAR(255) NOT NULL,
  stored_filename  VARCHAR(255)  NOT NULL,
  content_type     VARCHAR(100)  NOT NULL,
  file_size        BIGINT        NOT NULL,
  storage_path     VARCHAR(500)  NOT NULL,
  created_at       DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (po_id)        REFERENCES purchase_orders(id) ON DELETE CASCADE,
  FOREIGN KEY (uploaded_by)  REFERENCES users(id)
);
```

#### `po_status_history`
```sql
CREATE TABLE po_status_history (
  id          CHAR(36)   NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  po_id       CHAR(36)   NOT NULL,
  changed_by  CHAR(36)   NOT NULL,
  from_status ENUM('CREATED','SUBMITTED','APPROVED','REJECTED','CANCELLED'),
  to_status   ENUM('CREATED','SUBMITTED','APPROVED','REJECTED','CANCELLED') NOT NULL,
  notes       TEXT,
  changed_at  DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (po_id)      REFERENCES purchase_orders(id) ON DELETE CASCADE,
  FOREIGN KEY (changed_by) REFERENCES users(id)
);
```

#### `notifications`
```sql
CREATE TABLE notifications (
  id          CHAR(36)   NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  user_id     CHAR(36)   NOT NULL,
  po_id       CHAR(36)   NOT NULL,
  type        ENUM('PO_CREATED','PO_SUBMITTED','PO_APPROVED','PO_REJECTED','PO_CANCELLED')
                         NOT NULL,
  title       VARCHAR(200) NOT NULL,
  message     TEXT       NOT NULL,
  read_at     DATETIME,
  created_at  DATETIME   NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (po_id)   REFERENCES purchase_orders(id) ON DELETE CASCADE
);
```

#### `email_logs`
```sql
CREATE TABLE email_logs (
  id               CHAR(36)     NOT NULL DEFAULT (UUID()) PRIMARY KEY,
  notification_id  CHAR(36),
  to_email         VARCHAR(255) NOT NULL,
  subject          VARCHAR(300) NOT NULL,
  status           ENUM('SENT','FAILED') NOT NULL,
  error_message    TEXT,
  sent_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (notification_id) REFERENCES notifications(id) ON DELETE SET NULL
);
```

---

## 8. Backend API Specification

All API responses follow a standard envelope:
```json
{
  "success": true,
  "data": { ... },
  "message": "OK",
  "timestamp": "2026-04-08T10:00:00Z"
}
```
Errors return:
```json
{
  "success": false,
  "error": {
    "code": "PO_NOT_FOUND",
    "message": "Purchase order not found",
    "details": {}
  },
  "timestamp": "2026-04-08T10:00:00Z"
}
```

Base path: `/api/v1`

---

### 8.1 Auth Endpoints

| Method | Path | Role | Description |
|--------|------|------|-------------|
| POST | `/auth/login` | Public | Authenticate user, return JWT |
| POST | `/auth/refresh` | Public | Exchange refresh token for new access token |
| POST | `/auth/logout` | Authenticated | Revoke refresh token |
| PUT | `/auth/change-password` | Authenticated | Change own password |

**POST /auth/login**
```json
Request:  { "email": "string", "password": "string" }
Response: {
  "accessToken": "string",
  "refreshToken": "string",
  "expiresIn": 900,
  "user": {
    "id": "uuid",
    "email": "string",
    "firstName": "string",
    "lastName": "string",
    "role": "BUYER",
    "organizationId": "uuid",
    "organizationName": "string",
    "mustChangePassword": false
  }
}
```

---

### 8.2 Organization Endpoints (SUPER_ADMIN only)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/admin/organizations` | List all organizations (paginated) |
| POST | `/admin/organizations` | Create organization + first ORG_ADMIN |
| GET | `/admin/organizations/{id}` | Get organization details |
| PUT | `/admin/organizations/{id}` | Update organization |
| PATCH | `/admin/organizations/{id}/status` | Activate/deactivate |

**POST /admin/organizations**
```json
Request: {
  "name": "string",
  "code": "string",
  "adminFirstName": "string",
  "adminLastName": "string",
  "adminEmail": "string"
}
```
Creates the organization and the first `ORG_ADMIN` user with status `PENDING_PASSWORD_CHANGE`. A welcome email is sent to the admin with a temporary password.

---

### 8.3 User Management Endpoints (ORG_ADMIN + SUPER_ADMIN)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/users` | List users in organization (paginated, filterable by role/status) |
| POST | `/users` | Create user in organization |
| GET | `/users/{id}` | Get user details |
| PUT | `/users/{id}` | Update user |
| PATCH | `/users/{id}/status` | Activate/deactivate |
| POST | `/users/{id}/reset-password` | Admin resets user password (sends email) |

---

### 8.4 Seller Endpoints (ORG_ADMIN manages; BUYER/APPROVER read-only)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/sellers` | List sellers (paginated, filter by status) |
| POST | `/sellers` | Create seller |
| GET | `/sellers/{id}` | Get seller details |
| PUT | `/sellers/{id}` | Update seller |
| PATCH | `/sellers/{id}/status` | Change status |
| DELETE | `/sellers/{id}` | Soft-delete (set INACTIVE) |

---

### 8.5 Item Catalog Endpoints (ORG_ADMIN manages; BUYER/APPROVER read-only)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/item-categories` | List categories |
| POST | `/item-categories` | Create category |
| PUT | `/item-categories/{id}` | Update category |
| DELETE | `/item-categories/{id}` | Delete (only if no items linked) |
| GET | `/items` | List items (paginated, filter by sellerId, categoryId, status, search) |
| POST | `/items` | Create item |
| GET | `/items/{id}` | Get item details |
| PUT | `/items/{id}` | Update item |
| PATCH | `/items/{id}/status` | Activate/deactivate |
| GET | `/items/search?q={query}&sellerId={id}` | Autocomplete search (name or SKU), returns top 15 matches |

---

### 8.6 Facility Endpoints (ORG_ADMIN manages; BUYER/APPROVER read-only)

| Method | Path | Description |
|--------|------|-------------|
| GET | `/facilities` | List facilities (filter by status) |
| POST | `/facilities` | Create facility |
| GET | `/facilities/{id}` | Get facility |
| PUT | `/facilities/{id}` | Update facility |
| PATCH | `/facilities/{id}/status` | Activate/deactivate |

---

### 8.7 Purchase Order Endpoints

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/purchase-orders` | BUYER, APPROVER, ORG_ADMIN | List POs (paginated, filterable) |
| POST | `/purchase-orders` | BUYER | Create new PO (status = CREATED) |
| GET | `/purchase-orders/{id}` | BUYER, APPROVER, ORG_ADMIN | Get full PO details |
| PUT | `/purchase-orders/{id}` | BUYER | Update PO (only when CREATED or REJECTED) |
| POST | `/purchase-orders/{id}/submit` | BUYER | Submit PO for approval (CREATED → SUBMITTED) |
| POST | `/purchase-orders/{id}/approve` | APPROVER | Approve PO (SUBMITTED → APPROVED) |
| POST | `/purchase-orders/{id}/reject` | APPROVER | Reject PO (SUBMITTED → REJECTED) |
| POST | `/purchase-orders/{id}/cancel` | BUYER | Cancel PO (CREATED or SUBMITTED → CANCELLED) |
| GET | `/purchase-orders/{id}/history` | BUYER, APPROVER | Get status history / activity log |
| GET | `/purchase-orders/next-po-number` | BUYER | Get next auto-generated PO number |

**GET /purchase-orders query parameters:**
| Param | Type | Description |
|-------|------|-------------|
| `status` | ENUM | Filter by PO status |
| `sellerId` | UUID | Filter by seller |
| `buyerId` | UUID | Filter by buyer |
| `fromDate` | DATE | Order date from (inclusive) |
| `toDate` | DATE | Order date to (inclusive) |
| `search` | string | Search PO number |
| `page` | int | Page number (0-based) |
| `size` | int | Page size (default 20) |
| `sort` | string | e.g. `createdAt,desc` |

**POST /purchase-orders request body:**
```json
{
  "buyerId": "uuid",
  "sellerId": "uuid",
  "orderDate": "2026-04-08",
  "deliveryDate": "2026-04-22",
  "deliveryWindow": "MORNING",
  "paymentTerms": "NET_30",
  "currency": "PHP",
  "shippingInstructions": "string",
  "internalNotes": "string",
  "multiLocation": true,
  "facilityIds": ["uuid", "uuid"],
  "items": [
    {
      "itemId": "uuid",
      "unitPrice": 950.00,
      "moq": 50,
      "unit": "bags",
      "quantities": [
        { "facilityId": "uuid", "quantity": 60 },
        { "facilityId": "uuid", "quantity": 80 }
      ]
    }
  ]
}
```

**POST /purchase-orders/{id}/reject request body:**
```json
{ "reason": "string (required)" }
```

**POST /purchase-orders/{id}/cancel request body:**
```json
{ "reason": "string (required)" }
```

**PO number generation rule:**
- Format: `PO-{YYYY}-{NNNN}` where `NNNN` is a zero-padded sequential counter per organization, per year.
- Example: `PO-2026-0049`
- Reset to `0001` at the start of each calendar year.
- Use a database sequence or a `po_sequences` table with `organization_id` + `year` + `last_value` and optimistic locking to prevent duplicates.

---

### 8.8 Attachment Endpoints

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| POST | `/purchase-orders/{id}/attachments` | BUYER | Upload file (multipart/form-data) |
| GET | `/purchase-orders/{id}/attachments` | BUYER, APPROVER | List attachments |
| GET | `/purchase-orders/{id}/attachments/{attachmentId}` | BUYER, APPROVER | Download file |
| DELETE | `/purchase-orders/{id}/attachments/{attachmentId}` | BUYER | Delete attachment |

**Validation rules:**
- Max file size: 10 MB per file
- Allowed content types: `image/*`, `application/pdf`, `application/msword`, `application/vnd.openxmlformats-officedocument.*`, `application/vnd.ms-excel`, `text/plain`
- PO must exist and belong to caller's organization

---

### 8.9 Notification Endpoints

| Method | Path | Description |
|--------|------|-------------|
| GET | `/notifications` | List notifications for current user (paginated, filter unread) |
| GET | `/notifications/unread-count` | Get count of unread notifications |
| PATCH | `/notifications/{id}/read` | Mark single notification as read |
| PATCH | `/notifications/read-all` | Mark all notifications as read |

---

### 8.10 Report Endpoints

| Method | Path | Roles | Description |
|--------|------|-------|-------------|
| GET | `/reports/po-summary` | BUYER, APPROVER, ORG_ADMIN | PO summary data |
| GET | `/reports/spending` | BUYER, APPROVER, ORG_ADMIN | Spending breakdown |
| GET | `/reports/po-summary/export` | BUYER, APPROVER, ORG_ADMIN | Download Excel or PDF |
| GET | `/reports/spending/export` | BUYER, APPROVER, ORG_ADMIN | Download Excel or PDF |

**GET /reports/po-summary query parameters:**
| Param | Type | Description |
|-------|------|-------------|
| `status` | ENUM | Filter by status |
| `sellerId` | UUID | Filter by seller |
| `buyerId` | UUID | Filter by buyer |
| `fromDate` | DATE | |
| `toDate` | DATE | |

**GET /reports/spending query parameters:**
| Param | Type | Description |
|-------|------|-------------|
| `sellerId` | UUID | Group/filter by seller |
| `facilityId` | UUID | Group/filter by facility |
| `fromDate` | DATE | |
| `toDate` | DATE | |
| `groupBy` | ENUM | `SELLER`, `FACILITY`, `MONTH`, `QUARTER` |

**GET /reports/*/export additional parameters:**
| Param | Values | Description |
|-------|--------|-------------|
| `format` | `excel`, `pdf` | Export format |

---

## 9. Frontend Specification (Quasar)

### 9.1 Route Structure

```
/login                          → LoginPage
/                               → redirect to /purchase-orders
/dashboard                      → DashboardPage
/purchase-orders                → PurchaseOrderListPage
/purchase-orders/new            → PurchaseOrderCreatePage (wizard)
/purchase-orders/:id            → PurchaseOrderDetailPage
/purchase-orders/:id/edit       → PurchaseOrderCreatePage (edit mode, wizard)
/purchase-orders/:id/revise     → PurchaseOrderCreatePage (revise mode, start step 5)
/sellers                        → SellerListPage
/sellers/:id                    → SellerDetailPage
/items                          → ItemCatalogPage
/items/categories               → ItemCategoriesPage
/facilities                     → FacilitiesPage
/users                          → UserManagementPage  (ORG_ADMIN only)
/reports                        → ReportsPage
/admin/organizations            → OrganizationListPage (SUPER_ADMIN only)
/profile                        → UserProfilePage
```

### 9.2 Layout

**MainLayout.vue** — used for all authenticated pages:
- Fixed top `QToolbar` with: iConnect ERP logo, role toggle pill (Buyer/Approver), notification bell with unread badge, user avatar + dropdown (profile, change password, logout).
- Fixed left `QDrawer` (220px) with navigation links, pending-approval widget (Approver only).
- Main content area with `<router-view>`.

**AuthLayout.vue** — used for `/login` only: centered card on white background.

### 9.3 Pinia Stores

| Store | State | Actions |
|-------|-------|---------|
| `useAuthStore` | `user`, `accessToken`, `role`, `organizationId` | `login`, `logout`, `refreshToken`, `setRole` |
| `usePOStore` | `poList`, `currentPO`, `filters`, `pagination` | `fetchPOs`, `createPO`, `updatePO`, `submitPO`, `approvePO`, `rejectPO`, `cancelPO` |
| `useNotificationStore` | `notifications`, `unreadCount` | `fetchNotifications`, `markRead`, `markAllRead`, `poll` (30s interval) |
| `useCatalogStore` | `items`, `categories`, `sellers`, `facilities` | `fetchItems`, `searchItems`, `fetchSellers`, `fetchFacilities` |

### 9.4 PO Creation Wizard

The wizard is implemented using Quasar's `QStepper` component with `keep-alive` to preserve state between steps. All state is held in a reactive `poForm` object in the component's `setup()`.

**Wizard steps:**

| Step | Component | Key Fields |
|------|-----------|------------|
| 1 | `StepSelectBuyer.vue` | Organization (display only), Buyer (QSelect from users list), Department, Cost Center |
| 2 | `StepSelectSeller.vue` | Seller card grid with search filter; clicking a card selects it |
| 3 | `StepPODetails.vue` | PO Number (auto-generated, editable), Order Date, Delivery Date, Delivery Window, Payment Terms, Currency (locked to PHP), Shipping Instructions, Internal Notes |
| 4 | `StepFacilities.vue` | Toggle "Multiple Locations"; if on: facility checklist + selected tags with remove; if off: single QSelect |
| 5 | `StepItemsQuantities.vue` | Dynamic table: columns = selected facilities; autocomplete search to add items; MOQ inline warnings per cell |
| 6 | `StepReviewSubmit.vue` | Read-only summary of all previous steps; Submit button |

**Modes:**
- **Create mode** (`/purchase-orders/new`): starts at step 1.
- **Edit mode** (`/purchase-orders/:id/edit`): pre-fills all steps, starts at step 1. Only allowed when status = `CREATED`.
- **Revise mode** (`/purchase-orders/:id/revise`): shows rejection reason banner, starts at step 5. Previous button stops at step 3. Steps 1–2 shown as done/locked. Only allowed when status = `REJECTED`.

**poForm reactive object:**
```js
{
  buyerId: null,
  sellerId: null,
  poNumber: '',
  orderDate: '',
  deliveryDate: '',
  deliveryWindow: 'ANY_TIME',
  paymentTerms: 'NET_30',
  currency: 'PHP',
  shippingInstructions: '',
  internalNotes: '',
  multiLocation: true,
  facilityIds: [],       // selected facility UUIDs
  items: [               // array of selected items
    {
      itemId: '',
      name: '',
      sku: '',
      unit: '',
      unitPrice: 0,
      moq: 0,
      quantities: {      // key = facilityId, value = qty
        'uuid-fac-1': 0,
        'uuid-fac-2': 0,
      }
    }
  ]
}
```

**Step 5 — Items & Quantities table behavior:**
- Table columns are dynamically generated from `poForm.facilityIds`.
- Each quantity cell is an `<input type="number">` bound to `poForm.items[i].quantities[facilityId]`.
- On input change: if `qty > 0 && qty < item.moq`, show inline amber warning below the input: _"Below MOQ ({moq} {unit}). Please adjust."_ The input field border turns amber.
- Row total column = sum of all facility quantities for that item.
- Subtotal = total qty × unit price (shown as `—` if any facility qty is below MOQ).
- Grand total footer = sum of all valid subtotals.
- An amber alert banner at the bottom of the table shows _"N entries below MOQ"_ if any violations exist.
- The **Next** button on step 5 is disabled if any item has a quantity > 0 but below MOQ.
- **Add Item** button opens a search row with a floating autocomplete dropdown. As user types (min 1 character), it calls `GET /items/search?q={query}&sellerId={sellerId}`. Results show: item name, SKU, unit price, MOQ. Items already in the table are excluded. Clicking an item adds it to the table with all quantities set to 0.

### 9.5 PO Detail Page

**PurchaseOrderDetailPage.vue** — layout: two-column (main content | sidebar).

**Left column:**
- Buyer info card
- Seller info card
- PO details card
- Items table (read-only, with facility columns)
- Attachments card (upload button + file list with download/delete)

**Right column:**
- Status badge + PO number in page header
- Activity log timeline (all `po_status_history` entries, newest last)
- Order summary card (total items, locations, value, payment terms, delivery date)
- Action card (role-dependent):
  - **Buyer** (when status = CREATED or REJECTED): "Edit PO" and "Cancel PO" buttons
  - **Buyer** (when status = SUBMITTED): "Cancel PO" only
  - **Approver** (when status = SUBMITTED): "Approve" and "Reject" buttons with confirmation modals

**Status banner** (shown after action, full-width above columns):
- Approved: green banner
- Rejected: red banner with rejection reason
- Cancelled: gray banner

### 9.6 Dashboard Page

**DashboardPage.vue** — displays:
- Stats row: Total POs, Pending Approval, Approved (this month), Rejected, Total Value (this month)
- **For Approver:** prominent "Pending Approval" queue widget with quick-access links
- Recent POs list (last 5)
- Notification feed (last 10 unread)

### 9.7 In-App Notification Bell

- `QBtn` with `QBadge` showing unread count in the toolbar.
- Clicking opens a `QMenu` with a scrollable list of recent notifications.
- Each notification shows: icon (by type), title, time ago, read/unread state.
- "Mark all read" button at the top.
- Clicking a notification marks it read and navigates to the relevant PO.
- Polling: `useNotificationStore` polls `GET /notifications/unread-count` every 30 seconds.

---

## 10. Business Logic & Rules

### 10.1 PO Status State Machine

```
         ┌──────────────────────────────┐
         │           CREATED            │◄──── buyer revises
         └──┬──────────────────────┬───┘
            │ submit               │ cancel (buyer)
            ▼                      ▼
         SUBMITTED             CANCELLED
            │
       ┌────┴────┐
       │ approve │ reject
       ▼         ▼
    APPROVED  REJECTED ──► (buyer edits) ──► SUBMITTED
```

**Allowed transitions:**

| From | To | Triggered by |
|------|----|-------------|
| CREATED | SUBMITTED | Buyer: submit |
| CREATED | CANCELLED | Buyer: cancel |
| SUBMITTED | APPROVED | Approver: approve |
| SUBMITTED | REJECTED | Approver: reject (reason required) |
| SUBMITTED | CANCELLED | Buyer: cancel |
| REJECTED | SUBMITTED | Buyer: revise & resubmit |

**Invalid transitions throw `InvalidPOStatusTransitionException` (HTTP 409).**

### 10.2 PO Editing Rules
- A PO can only be edited (PUT) when status = `CREATED` or `REJECTED`.
- In `REJECTED` state, editing re-sets status to `CREATED` (not SUBMITTED — buyer must explicitly resubmit).
- Fields `po_number`, `buyer_id`, `seller_id` cannot be changed after initial creation.
- Price and MOQ in `po_items` are snapshots — changing the master item catalog does not retroactively change existing POs.

### 10.3 MOQ Enforcement
- MOQ is enforced at the API level in addition to the frontend.
- On `POST /purchase-orders` and `PUT /purchase-orders/{id}`, the backend validates that for each `po_item_quantity` where `quantity > 0`, `quantity >= moq`.
- If any quantity violates MOQ, the API returns HTTP 422 with a detailed error listing each violation:
```json
{
  "success": false,
  "error": {
    "code": "MOQ_VIOLATION",
    "message": "Some item quantities are below the minimum order quantity.",
    "details": [
      { "itemId": "uuid", "itemName": "Cooking Oil 2L", "facilityId": "uuid",
        "facilityName": "Orchard Branch", "quantity": 30, "moq": 100 }
    ]
  }
}
```

### 10.4 PO Number Generation
- PO number is auto-generated server-side as `PO-{YYYY}-{NNNN}`.
- A `po_sequences` table tracks `(organization_id, year, last_value)`.
- Generation uses a database-level `SELECT ... FOR UPDATE` to ensure uniqueness under concurrent creation.
- If the caller provides a custom PO number, it must be unique within the organization and year — validated on save.

### 10.5 Facility Consistency
- At least one facility must be selected on a PO.
- If `multiLocation = false`, exactly one facility must be selected.
- If `multiLocation = true`, at least two facilities must be selected.
- Each `po_item` must have a `po_item_quantity` row for every `po_facility` on the PO (quantity can be 0).

---

## 11. Notifications

### 11.1 Trigger Events

| Event | Email Recipients | In-app Recipients |
|-------|-----------------|-------------------|
| PO Submitted | Assigned Approvers in org | All APPROVERs in org |
| PO Approved | Buyer | Buyer |
| PO Rejected | Buyer | Buyer |
| PO Cancelled | All APPROVERs in org | All APPROVERs in org |

> "Assigned Approvers" = all active users with role `APPROVER` in the same organization.

### 11.2 Email Templates

Each email is an HTML template (stored in `resources/templates/email/`) using Thymeleaf:

| Template | Subject |
|----------|---------|
| `po-submitted.html` | `[iConnect ERP] PO {poNumber} submitted for your approval` |
| `po-approved.html` | `[iConnect ERP] PO {poNumber} has been approved` |
| `po-rejected.html` | `[iConnect ERP] PO {poNumber} has been rejected` |
| `po-cancelled.html` | `[iConnect ERP] PO {poNumber} has been cancelled` |
| `welcome.html` | `Welcome to iConnect ERP — set your password` |
| `password-reset.html` | `[iConnect ERP] Your password has been reset` |

All email templates include: PO number, PO summary (seller, delivery date, total value), a link to the PO detail page, and the reason (for rejected/cancelled).

### 11.3 Implementation
- Use Spring `@EventListener` (or Spring Application Events) to decouple PO status changes from notification dispatch.
- `NotificationService.dispatchForPO(PurchaseOrder po, POStatus newStatus)`:
  1. Determine recipients.
  2. Insert `notifications` records.
  3. Async: send email via `JavaMailSender`, log result to `email_logs`.
- Email sending is `@Async` — failures are logged but do not roll back the PO status change.

---

## 12. File Attachments

### 12.1 Upload Constraints
- Max size: 10 MB per file
- Allowed MIME types: `image/jpeg`, `image/png`, `image/gif`, `image/webp`, `application/pdf`, `application/msword`, `application/vnd.openxmlformats-officedocument.wordprocessingml.document`, `application/vnd.ms-excel`, `application/vnd.openxmlformats-officedocument.spreadsheetml.sheet`, `text/plain`
- No limit on number of attachments per PO
- Only BUYER can upload or delete attachments
- Attachments can be added at any PO status (for audit/reference)

### 12.2 Storage
- Files stored at: `{app.storage.base-path}/{organizationId}/{poId}/{uuid}_{originalFilename}`
- `app.storage.base-path` configured in `application.yml`
- Filenames are sanitized (remove special chars, spaces → underscores)
- The `stored_filename` in DB uses a UUID prefix to avoid collisions

### 12.3 Download
- `GET /purchase-orders/{id}/attachments/{attachmentId}` returns the file as a binary stream with correct `Content-Type` and `Content-Disposition: attachment; filename="{original_filename}"` headers.
- File access is gated: caller's `organizationId` must match the PO's organization.

---

## 13. Reporting

### 13.1 PO Summary Report

**Data returned / columns:**
- PO Number, Order Date, Seller Name, Buyer Name, Delivery Date, Number of Facilities, Number of Items, Total Value (PHP), Status

**Filters:** Status, Seller, Buyer, Date Range (order date)

**Grouping (display only):** Status

### 13.2 Spending Report

**Data returned:**
- When grouped by `SELLER`: Seller Name, Number of POs, Total Quantity of Items, Total Value (PHP)
- When grouped by `FACILITY`: Facility Name, Number of POs, Total Value (PHP)
- When grouped by `MONTH`: Month-Year, Number of POs, Total Value (PHP)
- When grouped by `QUARTER`: Quarter-Year, Number of POs, Total Value (PHP)

### 13.3 Export Format

**Excel (.xlsx):**
- Generated with Apache POI
- One sheet per report type
- Header row with bold style and red background (`#DC2626`) + white text
- Data rows with alternating light gray background
- Currency columns formatted as `₱#,##0.00`
- Auto-sized columns
- Footer row with total

**PDF:**
- Generated with iText (or JasperReports)
- Landscape orientation for wide tables
- Company/Organization name and report date in header
- Report title, filters applied, and generation timestamp
- Table with header in red, alternating row shading
- Footer with page numbers

---

## 14. Non-Functional Requirements

### 14.1 Performance
- PO list API must respond in < 500ms for up to 10,000 POs per organization (paginated).
- Item autocomplete search must respond in < 200ms.
- Report generation (up to 1,000 POs) must complete in < 10 seconds.
- File upload must handle up to 10 MB with streaming (no full in-memory buffering).

### 14.2 Security
- All API endpoints (except `/auth/login` and `/auth/refresh`) require a valid JWT.
- Organization isolation enforced at the service layer — never trust client-provided `organizationId`.
- File storage paths are never exposed directly; all access goes through the API.
- SQL injection prevention via JPA parameterized queries only — no native string concatenation.
- CORS configured to allow only the Quasar app's origin.
- Rate limiting on `/auth/login`: max 10 attempts per IP per minute (use Spring's `HandlerInterceptor` or a library like Bucket4j).

### 14.3 Validation
- All request bodies validated with Jakarta Bean Validation (`@Valid`).
- Date fields: `deliveryDate` must be after `orderDate`.
- `facilityIds` must all belong to the caller's organization.
- `itemId` in PO items must belong to the selected `sellerId`.

### 14.4 Logging & Auditing
- All PO status transitions logged to `po_status_history` (immutable).
- Application logs use SLF4J + Logback, structured JSON format in production.
- Log level: INFO in production, DEBUG in development.
- Include `organizationId` and `userId` in MDC context for all request logs.

### 14.5 Database
- All tables use `InnoDB` engine.
- UUID primary keys (stored as `CHAR(36)` for MariaDB compatibility).
- Flyway migrations versioned as `V{n}__{description}.sql`.
- Connection pool: HikariCP (Spring Boot default), `maximumPoolSize=20`.

---

## 15. Future Considerations

### 15.1 Keycloak Integration
The auth layer is designed for drop-in Keycloak integration:
- Replace `JwtService` with Keycloak JWKS-based token validation.
- Map Keycloak realm roles to application roles (`BUYER`, `APPROVER`, etc.).
- Replace the login page with Keycloak's hosted login page (Authorization Code Flow).
- Existing `users` table can be kept for profile data; `password_hash` column becomes unused.

### 15.2 Automated Replenishment (Customized Process)
The diagram includes an automated End-Day replenishment flow (not in scope for this module):
- Trigger: End-of-day batch job
- Logic: Replenishment Order → iterate vendors → auto-create POs with suggested quantities
- This will require a `replenishment_rules` table and a Spring `@Scheduled` batch job.

### 15.3 Seller Portal
Future: Sellers receive their own login to view approved POs directed to them and update fulfillment/delivery status.

### 15.4 Mobile App
Quasar supports Capacitor/Cordova build targets. The existing SPA can be packaged as a mobile app with minimal changes.

### 15.5 Push Notifications
Replace polling-based in-app notifications with WebSocket (Spring WebSocket + STOMP) for real-time delivery.

### 15.6 Multi-Currency
Currently locked to PHP. The schema supports `currency` as a column. Future: exchange rate service integration.
