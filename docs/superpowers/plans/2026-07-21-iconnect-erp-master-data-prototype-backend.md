# iConnect ERP — Master Data Prototype Backend Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Turn the static mockup `ui-mockup/iconnect-erp.html` into a working end-to-end prototype for the entire Master Data module group — a Java 21 / Spring Boot backend with an H2 file database, a REST API, and the existing mockup UI wired to that API so lists, creates, and edits persist across restarts.

**Architecture:** Single Maven module. Spring Web + Spring Data JPA + Bean Validation + H2 (file mode). A generic base CRUD (`AbstractCrudController` / `AbstractCrudService`) serves the 10 straightforward masters; four rule-heavy entities (MerchNode, Product, VendorProduct, AssortmentPlan) get explicit controllers. The mockup HTML is served from `src/main/resources/static/` (same-origin, no CORS) and its `PAGES` functions are converted to fetch from `/api/...`.

**Tech Stack:** Java 21, Spring Boot 3.3.x, Maven, Spring Web, Spring Data JPA, Jakarta Bean Validation, H2, JUnit 5 + Spring Boot Test + MockMvc.

**Source of truth for this plan:** `docs/superpowers/specs/2026-07-21-iconnect-erp-master-data-prototype-backend-design.md`. Field lists and seed values are transcribed from `ui-mockup/iconnect-erp.html` (the `CREATE` config at ~L1674 and the `PAGES.*` sample rows).

## Global Constraints

- **Java 21**, **Spring Boot 3.3.x**, **Maven** single module — copied verbatim from spec §2.
- **No delete endpoints** anywhere. Only `GET` (list + by-id), `POST` (create), `PUT` (update). — spec §2, §3.
- **No authentication** — no Spring Security, no login. — spec §2, §3.
- **H2 file mode**, DB persists across restarts: `jdbc:h2:file:./data/iconnect;AUTO_SERVER=TRUE`. — spec §2.
- **Never expose JPA entities over the wire** — every endpoint consumes/returns a DTO (Java `record`). — spec §5.
- **Seed only when empty** — the `DataSeeder` runs each row-group only if its table has zero rows. — spec §2, §7.
- **Base package:** `com.puregold.iconnect`. **API base path:** `/api`. — spec §5.
- **Money/quantity fields** use `BigDecimal`; dates use `java.time.LocalDate`. Mockup shows values like `₱ 58.20`; strip currency symbols/commas before persisting — store the number only.
- **Preferred Vendor is derived**, never stored on Product (= the single Primary VendorProduct link). — spec §6.3.
- **VendorProduct.costPerCase is read-only via the API** (settable only by seed). — spec §6.2.
- Scope is exactly the 14 entities in spec §4 plus child tables `ProductPackUom` and grouping FKs on `Branch`. Do not build modules outside Master Data.

---

## Phase 0 — Project scaffold

### Task 1: Spring Boot project skeleton that boots

**Files:**
- Create: `pom.xml`
- Create: `src/main/java/com/puregold/iconnect/IconnectApplication.java`
- Create: `src/main/resources/application.yml`
- Create: `.gitignore`
- Test: `src/test/java/com/puregold/iconnect/IconnectApplicationTests.java`

**Interfaces:**
- Produces: a bootable Spring Boot app on port 8080 with H2 file DB configured; base package `com.puregold.iconnect`.

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/puregold/iconnect/IconnectApplicationTests.java
package com.puregold.iconnect;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class IconnectApplicationTests {
    @Test
    void contextLoads() {
    }
}
```

- [ ] **Step 2: Create `pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.3.5</version>
        <relativePath/>
    </parent>
    <groupId>com.puregold</groupId>
    <artifactId>iconnect-erp</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>iconnect-erp</name>
    <properties>
        <java.version>21</java.version>
    </properties>
    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

- [ ] **Step 3: Create the application class**

```java
// src/main/java/com/puregold/iconnect/IconnectApplication.java
package com.puregold.iconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IconnectApplication {
    public static void main(String[] args) {
        SpringApplication.run(IconnectApplication.class, args);
    }
}
```

- [ ] **Step 4: Create `application.yml`**

```yaml
# src/main/resources/application.yml
spring:
  datasource:
    url: jdbc:h2:file:./data/iconnect;AUTO_SERVER=TRUE
    username: sa
    password: ""
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: update
    open-in-view: false
    properties:
      hibernate.format_sql: true
  h2:
    console:
      enabled: true
      path: /h2-console
server:
  port: 8080
```

- [ ] **Step 5: Create `.gitignore`**

```gitignore
target/
data/
*.log
```

- [ ] **Step 6: Run the test — verify it passes**

Run: `mvn -q test -Dtest=IconnectApplicationTests`
Expected: BUILD SUCCESS, context loads (H2 file created under `./data/`).

- [ ] **Step 7: Commit**

```bash
git init 2>/dev/null; git add pom.xml src .gitignore
git commit -m "chore: scaffold Spring Boot 3.3 + H2 project skeleton"
```
*(Repo is not currently under git; `git init` is safe. If the user declines git, skip commit steps throughout.)*

---

### Task 2: Serve the mockup as the app's home page

**Files:**
- Create: `src/main/resources/static/index.html` (copy of the mockup)
- Test: `src/test/java/com/puregold/iconnect/StaticContentTest.java`

**Interfaces:**
- Produces: `GET /` returns the mockup HTML (200, contains `<title>iConnect ERP`).

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/puregold/iconnect/StaticContentTest.java
package com.puregold.iconnect;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class StaticContentTest {
    @Autowired TestRestTemplate rest;

    @Test
    void servesMockupAtRoot() {
        String body = rest.getForObject("/", String.class);
        assertThat(body).contains("<title>iConnect ERP");
    }
}
```

- [ ] **Step 2: Copy the mockup into static resources**

Run: `cp ui-mockup/iconnect-erp.html src/main/resources/static/index.html`

Rationale: Spring Boot serves `src/main/resources/static/index.html` at `/` automatically. We keep `ui-mockup/iconnect-erp.html` as the pre-wiring snapshot. **Decision for this plan: from Task 5 onward, edit `static/index.html` directly; `ui-mockup/iconnect-erp.html` is not touched again.**

- [ ] **Step 3: Run the test — verify it passes**

Run: `mvn -q test -Dtest=StaticContentTest`
Expected: PASS.

- [ ] **Step 4: Commit**

```bash
git add src/main/resources/static/index.html src/test/java/com/puregold/iconnect/StaticContentTest.java
git commit -m "feat: serve mockup HTML from Spring Boot static resources"
```

---

## Phase 1 — Common base + error handling

### Task 3: Generic CRUD base + global error handling

**Files:**
- Create: `src/main/java/com/puregold/iconnect/common/BaseEntity.java`
- Create: `src/main/java/com/puregold/iconnect/common/AbstractCrudService.java`
- Create: `src/main/java/com/puregold/iconnect/common/AbstractCrudController.java`
- Create: `src/main/java/com/puregold/iconnect/common/NotFoundException.java`
- Create: `src/main/java/com/puregold/iconnect/common/ApiExceptionHandler.java`
- Test: `src/test/java/com/puregold/iconnect/common/ApiExceptionHandlerTest.java`

**Interfaces:**
- Produces:
  - `BaseEntity` — `@MappedSuperclass` with `@Id @GeneratedValue Long id`.
  - `AbstractCrudService<E extends BaseEntity>` — `List<E> findAll()`, `E findOne(Long id)` (throws `NotFoundException`), `E create(E e)`, `E update(Long id, java.util.function.Consumer<E> applyChanges)`, protected overridable `void validate(E e)`.
  - `AbstractCrudController<E, REQ, RES>` — abstract `E toEntity(REQ)`, `void apply(REQ, E)`, `RES toResponse(E)`; concrete `@GetMapping`, `@GetMapping("/{id}")`, `@PostMapping`, `@PutMapping("/{id}")`. Subclasses inject their service and set `@RequestMapping`.
  - `NotFoundException(String)` → HTTP 404. Validation failures → HTTP 400 `{ "errors": { field: message } }`. `IllegalArgumentException` → 400 `{ "message": ... }`.

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/puregold/iconnect/common/ApiExceptionHandlerTest.java
package com.puregold.iconnect.common;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class ApiExceptionHandlerTest {
    @Test
    void notFoundCarriesMessage() {
        NotFoundException ex = new NotFoundException("Company 99 not found");
        assertThat(ex.getMessage()).isEqualTo("Company 99 not found");
    }
}
```

- [ ] **Step 2: Run — verify it fails**

Run: `mvn -q test -Dtest=ApiExceptionHandlerTest`
Expected: FAIL — `NotFoundException` does not exist.

- [ ] **Step 3: Create `BaseEntity`**

```java
// src/main/java/com/puregold/iconnect/common/BaseEntity.java
package com.puregold.iconnect.common;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
}
```

- [ ] **Step 4: Create `NotFoundException`**

```java
// src/main/java/com/puregold/iconnect/common/NotFoundException.java
package com.puregold.iconnect.common;

public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
```

- [ ] **Step 5: Create `AbstractCrudService`**

```java
// src/main/java/com/puregold/iconnect/common/AbstractCrudService.java
package com.puregold.iconnect.common;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.function.Consumer;

public abstract class AbstractCrudService<E extends BaseEntity> {

    protected final JpaRepository<E, Long> repo;
    private final String label;

    protected AbstractCrudService(JpaRepository<E, Long> repo, String label) {
        this.repo = repo;
        this.label = label;
    }

    public List<E> findAll() { return repo.findAll(); }

    public E findOne(Long id) {
        return repo.findById(id)
            .orElseThrow(() -> new NotFoundException(label + " " + id + " not found"));
    }

    public E create(E entity) {
        validate(entity);
        return repo.save(entity);
    }

    public E update(Long id, Consumer<E> applyChanges) {
        E entity = findOne(id);
        applyChanges.accept(entity);
        validate(entity);
        return repo.save(entity);
    }

    /** Override for entity-specific invariants (unique codes, FK existence). Default: no-op. */
    protected void validate(E entity) { }
}
```

- [ ] **Step 6: Create `AbstractCrudController`**

```java
// src/main/java/com/puregold/iconnect/common/AbstractCrudController.java
package com.puregold.iconnect.common;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

public abstract class AbstractCrudController<E extends BaseEntity, REQ, RES> {

    protected final AbstractCrudService<E> service;

    protected AbstractCrudController(AbstractCrudService<E> service) {
        this.service = service;
    }

    protected abstract E toEntity(REQ req);
    protected abstract void apply(REQ req, E entity);
    protected abstract RES toResponse(E entity);

    @GetMapping
    public List<RES> list() {
        return service.findAll().stream().map(this::toResponse).toList();
    }

    @GetMapping("/{id}")
    public RES get(@PathVariable Long id) {
        return toResponse(service.findOne(id));
    }

    @PostMapping
    public ResponseEntity<RES> create(@Valid @RequestBody REQ req) {
        E saved = service.create(toEntity(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    @PutMapping("/{id}")
    public RES update(@PathVariable Long id, @Valid @RequestBody REQ req) {
        return toResponse(service.update(id, entity -> apply(req, entity)));
    }
}
```

- [ ] **Step 7: Create `ApiExceptionHandler`**

```java
// src/main/java/com/puregold/iconnect/common/ApiExceptionHandler.java
package com.puregold.iconnect.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(NotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleBadRequest(IllegalArgumentException ex) {
        return ResponseEntity.badRequest().body(Map.of("message", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
            .forEach(fe -> errors.putIfAbsent(fe.getField(), fe.getDefaultMessage()));
        return ResponseEntity.badRequest().body(Map.of("errors", errors));
    }
}
```

- [ ] **Step 8: Run the test — verify it passes**

Run: `mvn -q test -Dtest=ApiExceptionHandlerTest`
Expected: PASS.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/puregold/iconnect/common
git commit -m "feat: generic CRUD base (entity/service/controller) + API error handling"
```

---

## Phase 2 — First vertical slice: Company (exemplar, wired end-to-end)

This phase is the reference implementation. Every later generic master (Task 6) mirrors this exact five-class shape.

### Task 4: Company backend (entity, DTO, repo, service, controller)

**Files:**
- Create: `src/main/java/com/puregold/iconnect/company/Company.java`
- Create: `src/main/java/com/puregold/iconnect/company/CompanyDtos.java`
- Create: `src/main/java/com/puregold/iconnect/company/CompanyRepository.java`
- Create: `src/main/java/com/puregold/iconnect/company/CompanyService.java`
- Create: `src/main/java/com/puregold/iconnect/company/CompanyController.java`
- Test: `src/test/java/com/puregold/iconnect/company/CompanyApiTest.java`

**Interfaces:**
- Consumes: `AbstractCrudController`, `AbstractCrudService`, `BaseEntity` (Task 3).
- Produces: REST at `/api/companies`. `CompanyDtos.Request(code, legalName, tradeName, tin, fiscalYearStart, baseCurrency, defaultVat, priceIncludesVat, registeredAddress, contactPerson, status)`; `CompanyDtos.Response(id, code, legalName, tradeName, tin, fiscalYearStart, baseCurrency, defaultVat, priceIncludesVat, registeredAddress, contactPerson, status, branchCount)`. Unique-code rule enforced.

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/puregold/iconnect/company/CompanyApiTest.java
package com.puregold.iconnect.company;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyApiTest {
    @Autowired MockMvc mvc;

    @Test
    void createThenList() throws Exception {
        String body = """
            {"code":"TESTCO","legalName":"Test Co Inc.","baseCurrency":"PHP","status":"Active"}
            """;
        mvc.perform(post("/api/companies").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.code").value("TESTCO"))
           .andExpect(jsonPath("$.id").isNumber());

        mvc.perform(get("/api/companies"))
           .andExpect(status().isOk())
           .andExpect(jsonPath("$[?(@.code=='TESTCO')]").exists());
    }

    @Test
    void rejectsBlankLegalName() throws Exception {
        String body = """
            {"code":"BAD","legalName":"","baseCurrency":"PHP"}
            """;
        mvc.perform(post("/api/companies").contentType("application/json").content(body))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.legalName").exists());
    }

    @Test
    void rejectsDuplicateCode() throws Exception {
        String body = """
            {"code":"DUP","legalName":"First","baseCurrency":"PHP"}
            """;
        mvc.perform(post("/api/companies").contentType("application/json").content(body))
           .andExpect(status().isCreated());
        mvc.perform(post("/api/companies").contentType("application/json").content(body))
           .andExpect(status().isBadRequest());
    }
}
```

- [ ] **Step 2: Run — verify it fails**

Run: `mvn -q test -Dtest=CompanyApiTest`
Expected: FAIL — no `/api/companies` mapping.

- [ ] **Step 3: Create the entity**

```java
// src/main/java/com/puregold/iconnect/company/Company.java
package com.puregold.iconnect.company;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "company")
public class Company extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String legalName;
    private String tradeName;
    private String tin;
    private String fiscalYearStart;     // "January" | "April"
    private String baseCurrency;        // "PHP"
    private String defaultVat;          // "12% VATable" | "VAT Exempt"
    private String priceIncludesVat;    // "Yes (VAT-inclusive)" | "No"
    private String registeredAddress;
    private String contactPerson;
    private String status = "Active";   // "Active" | "Inactive"

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getTin() { return tin; }
    public void setTin(String tin) { this.tin = tin; }
    public String getFiscalYearStart() { return fiscalYearStart; }
    public void setFiscalYearStart(String v) { this.fiscalYearStart = v; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String v) { this.baseCurrency = v; }
    public String getDefaultVat() { return defaultVat; }
    public void setDefaultVat(String v) { this.defaultVat = v; }
    public String getPriceIncludesVat() { return priceIncludesVat; }
    public void setPriceIncludesVat(String v) { this.priceIncludesVat = v; }
    public String getRegisteredAddress() { return registeredAddress; }
    public void setRegisteredAddress(String v) { this.registeredAddress = v; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String v) { this.contactPerson = v; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
```

- [ ] **Step 4: Create the DTOs**

```java
// src/main/java/com/puregold/iconnect/company/CompanyDtos.java
package com.puregold.iconnect.company;

import jakarta.validation.constraints.NotBlank;

public final class CompanyDtos {
    private CompanyDtos() {}

    public record Request(
        @NotBlank String code,
        @NotBlank String legalName,
        String tradeName,
        String tin,
        String fiscalYearStart,
        String baseCurrency,
        String defaultVat,
        String priceIncludesVat,
        String registeredAddress,
        String contactPerson,
        String status
    ) {}

    public record Response(
        Long id, String code, String legalName, String tradeName, String tin,
        String fiscalYearStart, String baseCurrency, String defaultVat,
        String priceIncludesVat, String registeredAddress, String contactPerson,
        String status, long branchCount
    ) {}
}
```

- [ ] **Step 5: Create the repository**

```java
// src/main/java/com/puregold/iconnect/company/CompanyRepository.java
package com.puregold.iconnect.company;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCode(String code);
}
```

- [ ] **Step 6: Create the service (unique-code rule)**

```java
// src/main/java/com/puregold/iconnect/company/CompanyService.java
package com.puregold.iconnect.company;

import com.puregold.iconnect.common.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class CompanyService extends AbstractCrudService<Company> {

    private final CompanyRepository companies;

    public CompanyService(CompanyRepository companies) {
        super(companies, "Company");
        this.companies = companies;
    }

    @Override
    protected void validate(Company entity) {
        companies.findByCode(entity.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(entity.getId())) {
                throw new IllegalArgumentException("Company code already exists: " + entity.getCode());
            }
        });
    }
}
```

- [ ] **Step 7: Create the controller**

```java
// src/main/java/com/puregold/iconnect/company/CompanyController.java
package com.puregold.iconnect.company;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController
        extends AbstractCrudController<Company, CompanyDtos.Request, CompanyDtos.Response> {

    public CompanyController(CompanyService service) { super(service); }

    @Override
    protected Company toEntity(CompanyDtos.Request req) {
        Company c = new Company();
        apply(req, c);
        return c;
    }

    @Override
    protected void apply(CompanyDtos.Request req, Company c) {
        c.setCode(req.code());
        c.setLegalName(req.legalName());
        c.setTradeName(req.tradeName());
        c.setTin(req.tin());
        c.setFiscalYearStart(req.fiscalYearStart());
        c.setBaseCurrency(req.baseCurrency());
        c.setDefaultVat(req.defaultVat());
        c.setPriceIncludesVat(req.priceIncludesVat());
        c.setRegisteredAddress(req.registeredAddress());
        c.setContactPerson(req.contactPerson());
        if (req.status() != null) c.setStatus(req.status());
    }

    @Override
    protected CompanyDtos.Response toResponse(Company c) {
        return new CompanyDtos.Response(
            c.getId(), c.getCode(), c.getLegalName(), c.getTradeName(), c.getTin(),
            c.getFiscalYearStart(), c.getBaseCurrency(), c.getDefaultVat(),
            c.getPriceIncludesVat(), c.getRegisteredAddress(), c.getContactPerson(),
            c.getStatus(), 0L /* branchCount filled in Task 6a */);
    }
}
```

- [ ] **Step 8: Run the test — verify it passes**

Run: `mvn -q test -Dtest=CompanyApiTest`
Expected: PASS (all three tests).

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/puregold/iconnect/company src/test/java/com/puregold/iconnect/company
git commit -m "feat: Company CRUD API with unique-code validation (exemplar slice)"
```

---

### Task 5: Seed Companies from the mockup + wire the Company page to the API

**Files:**
- Create: `src/main/java/com/puregold/iconnect/seed/DataSeeder.java`
- Modify: `src/main/resources/static/index.html` (add `api()` helper; make `PAGES.company` + `route` async; wire `CREATE.company` save; add `data-f` to form fields)
- Test: `src/test/java/com/puregold/iconnect/seed/CompanySeedTest.java`

**Interfaces:**
- Consumes: `CompanyRepository` (Task 4).
- Produces: global JS `api(path, opts)` + `money(v)` helpers; `postRender(id)` extracted from `route`; `PAGES.company` renders from `GET /api/companies`; New Company form POSTs. `DataSeeder.seedCompanies()` seeds when empty.

- [ ] **Step 1: Write the failing test (seed runs)**

```java
// src/test/java/com/puregold/iconnect/seed/CompanySeedTest.java
package com.puregold.iconnect.seed;

import com.puregold.iconnect.company.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CompanySeedTest {
    @Autowired CompanyRepository companies;

    @Test
    void seedsPuregoldCompanies() {
        assertThat(companies.findByCode("PGOLD")).isPresent();
        assertThat(companies.findByCode("AYAG")).isPresent();
    }
}
```

- [ ] **Step 2: Run — verify it fails**

Run: `mvn -q test -Dtest=CompanySeedTest`
Expected: FAIL — no seeder yet.

- [ ] **Step 3: Create `DataSeeder` (companies only for now; extended in later tasks)**

Values transcribed from the mockup top-bar / `PAGES.home` company options (`Puregold Price Club, Inc.`, `Ayagold Retailers Inc.` (Merkado)).

```java
// src/main/java/com/puregold/iconnect/seed/DataSeeder.java
package com.puregold.iconnect.seed;

import com.puregold.iconnect.company.Company;
import com.puregold.iconnect.company.CompanyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companies;

    public DataSeeder(CompanyRepository companies) {
        this.companies = companies;
    }

    @Override
    public void run(String... args) {
        seedCompanies();
    }

    private void seedCompanies() {
        if (companies.count() > 0) return;
        companies.save(company("PGOLD", "Puregold Price Club, Inc.", "Puregold", "000-123-456-000"));
        companies.save(company("AYAG", "Ayagold Retailers Inc.", "Merkado", "000-987-654-000"));
    }

    private Company company(String code, String legalName, String trade, String tin) {
        Company c = new Company();
        c.setCode(code);
        c.setLegalName(legalName);
        c.setTradeName(trade);
        c.setTin(tin);
        c.setFiscalYearStart("January");
        c.setBaseCurrency("PHP");
        c.setDefaultVat("12% VATable");
        c.setPriceIncludesVat("Yes (VAT-inclusive)");
        c.setStatus("Active");
        return c;
    }
}
```

- [ ] **Step 4: Run the seed test — verify it passes**

Run: `mvn -q test -Dtest=CompanySeedTest`
Expected: PASS.

- [ ] **Step 5: Add the `api()` + `money()` helpers to the mockup**

In `src/main/resources/static/index.html`, immediately after `const PAGES = {};` (search for that exact line), insert:

```javascript
async function api(path, opts){
  const res = await fetch('/api'+path, {
    headers:{'Content-Type':'application/json'},
    ...opts
  });
  if(!res.ok){
    let msg = 'Request failed ('+res.status+')';
    try{ const e = await res.json(); msg = e.message || (e.errors && Object.entries(e.errors).map(([k,v])=>k+': '+v).join(', ')) || msg; }catch(_){}
    throw new Error(msg);
  }
  return res.status===204 ? null : res.json();
}
function money(v){ return v==null||v==='' ? null : Number(String(v).replace(/[₱,\s]/g,'')); }
```

- [ ] **Step 6: Make `route()` async-capable and extract `postRender`; convert `PAGES.company`**

Find `function route(id){` (~L2016). It currently sets `main.innerHTML=PAGES[id]();` then runs a block of binding calls (`renderSidebar; bindTabs; bindCreate; bindBatch; bindUM; bindBranchAccess;` plus the `data-dup`/`data-route` wiring at ~L2030–2037). **Move that entire binding block into a new function `postRender(id){ ... }`** (keep every call verbatim). Then replace the render line with:

```javascript
  const out = PAGES[id]();
  if(out && typeof out.then==='function'){
    main.innerHTML = '<div class="empty">Loading…</div>';
    out.then(html=>{ main.innerHTML = html; postRender(id); })
       .catch(e=>{ main.innerHTML = '<div class="empty">Failed to load — '+e.message+'</div>'; });
  } else {
    main.innerHTML = out;
    postRender(id);
  }
```

Convert `PAGES.company` (~L384) to:

```javascript
PAGES.company = async ()=>{
  const rows = await api('/companies');
  return `
${head('1','Company Maintenance','Corporate legal entities operating under the group. Configure fiscal calendar, tax, and default accounting.', btn('Export','',iExport)+btn('New Company','primary',iAdd))}
${noteBox('Per the JDA spec, a <b>Company is regarded as the Chain</b> — the top pricing level covering all of its stores.','indigo')}
<div class="panel">
  ${filterbar([{label:'Search',grow:1,ph:'Company code, legal name or TIN…'}], btn('Apply','primary'))}
  <div class="panel-body flush">${table(
    [{t:'Code',k:'code'},{t:'Legal Name',k:'name'},{t:'TIN',k:'tin'},{t:'Branches',k:'b',num:1},{t:'Currency',k:'cur'},{t:'Status',k:'st'}],
    rows.map(r=>({code:'<span class="mono">'+r.code+'</span>',name:'<span class="row-link">'+r.legalName+'</span>',tin:r.tin||'—',b:r.branchCount,cur:r.baseCurrency||'PHP',st:badge(r.status==='Active'?'green':'gray',r.status||'Active')})))}</div>
  ${pager(rows.length, rows.length)}
</div>`;
};
```

- [ ] **Step 7: Add `data-f` addressing to form fields, and wire the New Company save**

In the create-form field renderer (`createForm`/`ff`, ~L1077–1120), add `data-f="${f.l}"` to each rendered `input`/`select`/`textarea` element so fields are addressable by their label. Add helper near `openCreate`:

```javascript
function gv(m,label){ const el=m.querySelector('[data-f="'+CSS.escape(label)+'"]'); return el?el.value:''; }
```

Find the save-button binding in `openCreate` (~L1752, currently `m.querySelectorAll('[data-back],[data-save]').forEach(b=>b.onclick=()=>route(window.__ret||'home'));`). Split it so `[data-save]` gets an async handler:

```javascript
m.querySelectorAll('[data-back]').forEach(b=>b.onclick=()=>route(window.__ret||'home'));
m.querySelectorAll('[data-save]').forEach(b=>b.onclick=async ()=>{
  if(key==='company'){
    try{
      await api('/companies',{method:'POST',body:JSON.stringify({
        code:gv(m,'Company Code'), legalName:gv(m,'Legal Name'),
        tradeName:gv(m,'Trade Name'), tin:gv(m,'TIN'),
        fiscalYearStart:gv(m,'Fiscal Year Start'), baseCurrency:gv(m,'Base Currency'),
        defaultVat:gv(m,'Default VAT'), priceIncludesVat:gv(m,'Price Includes VAT'),
        registeredAddress:gv(m,'Registered Address'), contactPerson:gv(m,'Contact Person'),
        status:'Active'
      })});
      route('company');
    }catch(e){ alert('Save failed — '+e.message); }
    return;
  }
  route(window.__ret||'home');
});
```

- [ ] **Step 8: Verify end-to-end in the browser**

Run: `mvn spring-boot:run` (background), open `http://localhost:8080`. Verify:
1. Company page lists **Puregold Price Club, Inc.** and **Ayagold Retailers Inc.** from the DB.
2. **New Company** → fill code `TESTCO` + legal name → Save → list shows `TESTCO`.
3. Restart the app, reopen → `TESTCO` persists (H2 file).
4. Save with blank legal name → alert shows validation error.

- [ ] **Step 9: Commit**

```bash
git add src/main/java/com/puregold/iconnect/seed src/main/resources/static/index.html src/test/java/com/puregold/iconnect/seed
git commit -m "feat: seed companies + wire Company page to REST API (first live slice)"
```

---

## Phase 3 — Remaining generic masters

### Task 6: The 9 remaining generic-master backends

Each master repeats the exact five-class shape from Task 4 (entity, DTOs, repository, service, controller) driven by `AbstractCrudController`. Build them one at a time; each gets its own `create+list` MockMvc test mirroring `CompanyApiTest`, plus one validation case. Field lists are transcribed from the mockup `CREATE` config (spec §4).

**Files (per master):** `src/main/java/com/puregold/iconnect/<pkg>/<Name>.java`, `<Name>Dtos.java`, `<Name>Repository.java`, `<Name>Service.java`, `<Name>Controller.java`; test `src/test/java/com/puregold/iconnect/<pkg>/<Name>ApiTest.java`.

**Interfaces (routes produced):** `/api/branches`, `/api/vendors`, `/api/vendor-categories`, `/api/vendor-schedules`, `/api/vendor-allowances`, `/api/price-zones`, `/api/cost-zones`, `/api/assortment-clusters`, `/api/replenishment-profiles`.

For **each** sub-master: (1) write `<Name>ApiTest` (`createThenList` + one validation case), (2) run → fail, (3) create the five classes, (4) run → pass, (5) commit. Do them in this order (Branch first, because groupings' `storeCount` and Company's `branchCount` depend on `BranchRepository`):

- [ ] **6a. Branch** (`branch` pkg, `/api/branches`)
  - Fields: `code`(@NotBlank, unique), `name`(@NotBlank), `format`(Price Club|Extra|Minimart|Merkado), `region`(NCR|N.Luzon|S.Luzon|Visayas|Mindanao), `companyId`(@NotNull, FK→Company), `inventoryLocations`, `crossDockHub`, `openingDate`(LocalDate), `priceZoneId`(FK, nullable), `costZoneId`(FK, nullable), `assortmentClusterId`(FK, nullable), `replenishmentProfileId`(FK, nullable), `street`, `city`, `province`, `status`.
  - `BranchRepository` adds: `Optional<Branch> findByCode(String)`, `long countByCompanyId(Long)`, `long countByPriceZoneId(Long)`, `long countByCostZoneId(Long)`, `long countByAssortmentClusterId(Long)`, `long countByReplenishmentProfileId(Long)`.
  - Service `validate`: unique code; FK existence for `companyId` and each non-null grouping FK (inject the grouping repositories from Task 6f–6i, or defer the grouping-FK checks until those exist — since Branch is built before groupings, guard grouping-FK checks behind null-safe repository lookups added in a follow-up commit after 6i). Simplest ordering: implement Branch FK check for `companyId` now; add grouping-FK checks in a small amendment commit after Task 6i.
  - **After Branch exists:** update `CompanyController.toResponse` to set `branchCount = branchRepository.countByCompanyId(c.getId())` (inject `BranchRepository` into `CompanyController`). Add a `CompanyApiTest` assertion is not required, but Task 7's seed test will exercise it.

- [ ] **6b. Vendor** (`vendor` pkg, `/api/vendors`)
  - Fields: `code`(unique; if blank, service sets `"V-"+id` in a post-save update), `name`(@NotBlank), `tin`, `deliveryModel`(Direct-to-Store (DTS)|Cross-dock|DSD), `paymentTerms`, `leadTime`, `minOrder`(BigDecimal), `rtvAllowed`(@NotNull; "Yes"|"No"), `registeredAddress`, `contactPerson`, `phone`, `email`, `status`.

- [ ] **6c. VendorCategory** (`vendor` pkg, `/api/vendor-categories`)
  - Fields: `vendorId`(@NotNull, FK→Vendor), `level`(Category (L1)|Sub-Category (L2)), `parentCategoryId`(FK→VendorCategory, nullable), `nodeName`(@NotBlank), `nodeCode`, `allowanceEligible`("Yes"|"No"), `status`.
  - Service `validate`: FK existence for `vendorId`; if level is `Sub-Category (L2)`, `parentCategoryId` must be present and resolve to an L1 of the same vendor.

- [ ] **6d. VendorSchedule** (`vendor` pkg, `/api/vendor-schedules`)
  - Fields: `companyId`(@NotNull, FK), `vendorId`(@NotNull, FK), `orderDays`(e.g. "Mon · Thu"), `leadTimeDays`(Integer), `deliveryDays`(nullable), `status`.

- [ ] **6e. VendorAllowance** (`vendor` pkg, `/api/vendor-allowances`)
  - Fields: `ref`(unique; if blank, service sets `"AL-"+id`), `vendorId`(@NotNull, FK), `allowanceType`(Volume Rebate|Listing Fee|Promo Allowance|Display / Facing|Markdown Support), `basis`(% of Purchase|Fixed Amount|Per Case), `rateAmount`(String — mockup mixes "3.50%" and "₱ 12.00"; keep as String), `validFrom`(LocalDate), `validTo`(LocalDate), `status`.

- [ ] **6f–6i. The four store-groupings** — introduce `@MappedSuperclass StoreGrouping extends BaseEntity` in `grouping` pkg holding the shared fields `name`(@NotBlank), `code`(unique), `companyId`(@NotNull, FK→Company), `status`, `description`. Concrete classes (separate tables, controllers, routes):
  - **PriceZone** (`/api/price-zones`), **CostZone** (`/api/cost-zones`), **ReplenishmentProfile** (`/api/replenishment-profiles`) — no extra fields.
  - **AssortmentCluster** (`/api/assortment-clusters`) — adds `storeProfile`, `tradeArea`(Urban|Suburban|Provincial|Transit / Mall).
  - Each `Response` includes `storeCount` = the matching `branchRepository.countBy…Id(id)` (inject `BranchRepository`).
  - Each service `validate`: unique code; FK existence for `companyId`.

- [ ] **6j. Branch grouping-FK amendment** — now that 6f–6i exist, extend `BranchService.validate` to check each non-null grouping FK against its repository; add a `BranchApiTest` case asserting a dangling `priceZoneId` → 400. Commit.

Commit after each sub-master (≈10 commits), e.g. `git commit -m "feat: Branch CRUD API with company FK validation + Company.branchCount"`.

---

### Task 7: Extend `DataSeeder` for all generic masters

**Files:**
- Modify: `src/main/java/com/puregold/iconnect/seed/DataSeeder.java`
- Test: `src/test/java/com/puregold/iconnect/seed/GenericSeedTest.java`

**Interfaces:**
- Consumes: all repositories from Task 6.
- Produces: seeded rows for branches, vendors, vendor-categories, vendor-schedules, vendor-allowances, and the four groupings, transcribed from the mockup. Each `seedX()` guarded by `if (repo.count() > 0) return;`. Ordering: companies → groupings → vendors → vendor-categories → vendor-schedules → vendor-allowances → branches (branches reference companies + groupings).

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/puregold/iconnect/seed/GenericSeedTest.java
package com.puregold.iconnect.seed;

import com.puregold.iconnect.vendor.VendorRepository;
import com.puregold.iconnect.branch.BranchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GenericSeedTest {
    @Autowired VendorRepository vendors;
    @Autowired BranchRepository branches;

    @Test
    void seedsVendorsAndBranches() {
        assertThat(vendors.count()).isGreaterThanOrEqualTo(7); // 7 vendors in mockup PAGES.vendor
        assertThat(branches.count()).isGreaterThanOrEqualTo(10); // BR const has 10 stores
    }
}
```

- [ ] **Step 2: Run — verify it fails**, then extend `DataSeeder` with per-group `seedX()` methods, transcribing exact rows:
  - **Groupings** from `CREATE.branch` option lists (~L1694): Price Zones (Price Club, Extra, Minimart, Merkado, Visayas, NCR Metro), Cost Zones (Price Club National, Luzon Cost, Visayas–Mindanao Cost, Merkado Cost), Assortment Clusters (Urban Large, Urban Small, Convenience, Provincial Mixed), Replenishment Profiles (Metro Fast-Movers, Provincial Weekly, Minimart 2×/Week). Merkado-named ones under AYAG; the rest under PGOLD. Auto-code each.
  - **Vendors** from `PAGES.vendor` rows (~L567–573): Alaska Milk Corporation (Cross-dock, 30 days, RTV Allowed), Gardenia Bakeries PH (DSD, 7 days, Allowed), Selecta Ice Cream (RFM) (DSD, 15 days, Allowed), Nestlé Philippines (DTS, 45 days, Allowed), San Miguel Foods (DTS, 30 days, Not Allowed), Monde Nissin Corp. (Cross-dock, 30 days, Allowed, On Hold), Universal Robina Corp. (DTS, 30 days, Not Allowed).
  - **Vendor Categories** from `PAGES.vencat` tree (~L579–581) for Nestlé: Beverages→(Coffee, Creamer, Cereal Drinks), Dairy→(Powdered Milk, Condensed & Evaporated), Culinary→(Sauces & Mixes, Seasoning) — seed L1 then L2 with `parentCategoryId` set via repository (not `service.create`).
  - **Vendor Schedules**: one per (PGOLD, vendor) using each vendor's lead time.
  - **Vendor Allowances** from `PAGES.allowance` rows (~L626–630): AL-3301 Nestlé Volume Rebate 3.50%; AL-3312 Alaska Promo Per Case ₱12.00; AL-3320 Universal Robina Display Fixed ₱250,000; AL-3334 San Miguel Listing Fee Fixed ₱80,000 (Pending); AL-3340 Monde Nissin Markdown 5.00% (Expiring).
  - **Branches** from the `BR` const (~L1073): 10 stores, split each into code + name (e.g. `0102` / `Ortigas Ext.`). All under PGOLD except `AYA-03 · Merkado UP Town` → AYAG. Format by name heuristic (name contains "Jr."→Extra, "Mini"→Minimart, "Merkado"→Merkado, else Price Club); assign a Region and a Price/Cost/Cluster/Replen grouping id by looking up the seeded groupings.

- [ ] **Step 3: Run — verify it passes.** Run: `mvn -q test -Dtest=GenericSeedTest` → PASS.

- [ ] **Step 4: Commit** — `git commit -m "feat: seed all generic masters from mockup sample data"`

---

### Task 8: Wire the 9 generic-master pages + create forms to the API

**Files:**
- Modify: `src/main/resources/static/index.html` (`PAGES.branch`, `PAGES.vendor`, `PAGES.vencat`, `PAGES.vendorschedule`, `PAGES.allowance`, `PAGES.pricezone`, `PAGES.costzone`, `PAGES.cluster`, `PAGES.replenpolicy`; and the matching `openCreate` save branches)

**Interfaces:**
- Consumes: `api()`/`gv()`/`money()` helpers (Task 5), all routes from Task 6.
- Produces: each list page fetches from its `/api/...` route; each New form POSTs; FK selects submit ids.

- [ ] **Step 1** For each of the 9 pages, apply the same transform used for `PAGES.company` (Task 5 Step 6): make `PAGES.<id>` `async`, `await api('/<route>')`, and `.map()` the rows into the existing `table(...)` column shape already defined in that page. Preserve each page's `head`, `noteBox`, `filterbar`. Filter/lookup dropdowns that reference other masters fetch their options too (e.g. `const vendors = await api('/vendors')`).

- [ ] **Step 2** Generalize the save handler in `openCreate` into a `SAVE` map keyed by `key`, each entry `{route, body:(m)=>({...})}`:

```javascript
const SAVE = {
  company: {route:'/companies', page:'company', body:m=>({code:gv(m,'Company Code'),legalName:gv(m,'Legal Name'),tradeName:gv(m,'Trade Name'),tin:gv(m,'TIN'),fiscalYearStart:gv(m,'Fiscal Year Start'),baseCurrency:gv(m,'Base Currency'),defaultVat:gv(m,'Default VAT'),priceIncludesVat:gv(m,'Price Includes VAT'),registeredAddress:gv(m,'Registered Address'),contactPerson:gv(m,'Contact Person'),status:'Active'})},
  vendor:  {route:'/vendors', page:'vendor', body:m=>({name:gv(m,'Vendor Name'),tin:gv(m,'TIN'),code:gv(m,'Vendor Code'),deliveryModel:gv(m,'Delivery Model'),paymentTerms:gv(m,'Payment Terms'),leadTime:gv(m,'Lead Time'),minOrder:money(gv(m,'Min Order (₱)')),rtvAllowed:gv(m,'RTV Allowed'),status:'Active'})},
  branch:  {route:'/branches', page:'branch', body:m=>({code:gv(m,'Branch Code'),name:gv(m,'Branch Name'),format:gv(m,'Format'),region:gv(m,'Region'),companyId:selId(m,'Company'),priceZoneId:selId(m,'Price Zone'),costZoneId:selId(m,'Cost Zone'),assortmentClusterId:selId(m,'Assortment Cluster'),replenishmentProfileId:selId(m,'Replenishment Profile'),status:'Active'})},
  pricezone:{route:'/price-zones', page:'pricezone', body:m=>({name:gv(m,'Zone Name'),code:gv(m,'Zone Code'),companyId:selId(m,'Company'),status:'Active',description:gv(m,'Description')})},
  costzone: {route:'/cost-zones', page:'costzone', body:m=>({name:gv(m,'Zone Name'),code:gv(m,'Zone Code'),companyId:selId(m,'Company'),status:'Active',description:gv(m,'Description')})},
  cluster:  {route:'/assortment-clusters', page:'cluster', body:m=>({name:gv(m,'Cluster Name'),code:gv(m,'Cluster Code'),companyId:selId(m,'Company'),status:'Active',description:gv(m,'Description'),storeProfile:gv(m,'Store Profile'),tradeArea:gv(m,'Trade Area')})},
  replenpolicy:{route:'/replenishment-profiles', page:'replenpolicy', body:m=>({name:gv(m,'Profile Name'),code:gv(m,'Profile Code'),companyId:selId(m,'Company'),status:'Active',description:gv(m,'Description')})},
  vencat:   {route:'/vendor-categories', page:'vencat', body:m=>({vendorId:selId(m,'Vendor'),level:gv(m,'Level'),parentCategoryId:selId(m,'Parent Category'),nodeName:gv(m,'Node Name'),nodeCode:gv(m,'Node Code'),allowanceEligible:gv(m,'Allowance / Promo Eligible'),status:'Active'})},
  vendorschedule:{route:'/vendor-schedules', page:'vendorschedule', body:m=>({companyId:selId(m,'Company'),vendorId:selId(m,'Vendor'),orderDays:gv(m,'Order Days'),leadTimeDays:Number(gv(m,'Lead Time (days)')),deliveryDays:gv(m,'Delivery Days (optional)'),status:'Active'})},
  allowance:{route:'/vendor-allowances', page:'allowance', body:m=>({vendorId:selId(m,'Vendor'),allowanceType:gv(m,'Allowance Type'),basis:gv(m,'Basis'),rateAmount:gv(m,'Rate / Amount'),validFrom:gv(m,'Valid From'),validTo:gv(m,'Valid To'),status:'Active'})},
};
function selId(m,label){ const el=m.querySelector('[data-f="'+CSS.escape(label)+'"]'); return el&&el.selectedOptions[0]?Number(el.selectedOptions[0].dataset.id)||null:null; }
```

Replace the `[data-save]` handler body with:

```javascript
const cfg = SAVE[key];
if(cfg){
  try{ await api(cfg.route,{method:'POST',body:JSON.stringify(cfg.body(m))}); route(cfg.page); }
  catch(e){ alert('Save failed — '+e.message); }
  return;
}
route(window.__ret||'home');
```

- [ ] **Step 3** For selects that submit an FK id (Company on zones/branch/schedule; Vendor on vencat/schedule/allowance; grouping selects on Branch; Parent Category on vencat), populate their `<option>`s from the API at modal-open time with `option.value=name; option.dataset.id=row.id`. In `openCreate`, after the modal renders, detect these labelled selects and fill them: e.g. fill `Company` from `api('/companies')`, `Vendor` from `api('/vendors')`, `Price Zone`/`Cost Zone`/`Assortment Cluster`/`Replenishment Profile` from their routes. `selId` then reads `dataset.id`.

- [ ] **Step 4** Verify in browser: each of the 9 list pages shows seeded rows; creating a Branch under a chosen Company + Price Zone persists with FKs resolving (no 400); creating a Vendor Category as L2 requires a parent. Restart → data persists.

- [ ] **Step 5** Commit — `git commit -m "feat: wire 9 generic master pages + create forms to REST API"`

---

## Phase 4 — Rule-heavy entities

### Task 9: MerchNode (Category Hierarchy) with sub-class auto-scaffold

**Files:**
- Create: `src/main/java/com/puregold/iconnect/product/MerchNode.java`, `MerchNodeDtos.java`, `MerchNodeRepository.java`, `MerchNodeService.java`, `MerchNodeController.java`
- Test: `src/test/java/com/puregold/iconnect/product/MerchNodeServiceTest.java`

**Interfaces:**
- Produces: `/api/merch-nodes`. `MerchNode` fields: `level`(Department (L1)|Sub-Department (L2)|Class (L3)|Sub-Class (L4)), `parentId`(FK→MerchNode, nullable), `code`(@NotBlank, unique), `name`(@NotBlank), `merchManager`, `replenStrategy`, `perishable`, `shelfLife`, `otbPlanned`, `status`. Repository: `List<MerchNode> findByParentId(Long)`, `List<MerchNode> findByLevel(String)`, `Optional<MerchNode> findByCode(String)`. `list()` accepts optional `?level=` and `?parentId=`. Rule: creating a **Class (L3)** auto-creates three **Sub-Class (L4)** children `<code>-101/-102/-103`.

- [ ] **Step 1: Write the failing test (the signature rule)**

```java
// src/test/java/com/puregold/iconnect/product/MerchNodeServiceTest.java
package com.puregold.iconnect.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MerchNodeServiceTest {
    @Autowired MerchNodeService service;
    @Autowired MerchNodeRepository repo;

    @Test
    void creatingClassScaffoldsThreeSubclasses() {
        MerchNode cls = new MerchNode();
        cls.setLevel("Class (L3)");
        cls.setCode("9999-99");
        cls.setName("Test Class");
        MerchNode saved = service.create(cls);

        var kids = repo.findByParentId(saved.getId());
        assertThat(kids).hasSize(3);
        assertThat(kids).extracting(MerchNode::getCode)
            .containsExactlyInAnyOrder("9999-99-101", "9999-99-102", "9999-99-103");
        assertThat(kids).allMatch(k -> "Sub-Class (L4)".equals(k.getLevel()));
    }
}
```

- [ ] **Step 2: Run — verify it fails.** Run: `mvn -q test -Dtest=MerchNodeServiceTest` → FAIL.

- [ ] **Step 3: Create entity/DTOs/repository** (fields + repository methods per Interfaces).

- [ ] **Step 4: Create the service with the scaffold rule**

```java
// src/main/java/com/puregold/iconnect/product/MerchNodeService.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class MerchNodeService extends AbstractCrudService<MerchNode> {

    private final MerchNodeRepository nodes;

    public MerchNodeService(MerchNodeRepository nodes) {
        super(nodes, "MerchNode");
        this.nodes = nodes;
    }

    @Override
    protected void validate(MerchNode n) {
        nodes.findByCode(n.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(n.getId()))
                throw new IllegalArgumentException("Node code already exists: " + n.getCode());
        });
        if (n.getParentId() != null && nodes.findById(n.getParentId()).isEmpty())
            throw new IllegalArgumentException("Parent node not found: " + n.getParentId());
    }

    @Override
    public MerchNode create(MerchNode entity) {
        MerchNode saved = super.create(entity);
        if ("Class (L3)".equals(saved.getLevel())) {
            for (String sc : new String[]{"101", "102", "103"}) {
                MerchNode child = new MerchNode();
                child.setLevel("Sub-Class (L4)");
                child.setParentId(saved.getId());
                child.setCode(saved.getCode() + "-" + sc);
                child.setName(sc);
                child.setStatus("Active");
                nodes.save(child);
            }
        }
        return saved;
    }
}
```

- [ ] **Step 5: Create the controller** (`/api/merch-nodes`; `list()` takes optional `@RequestParam(required=false) String level`, `@RequestParam(required=false) Long parentId` → if `parentId!=null` filter `findByParentId`, else if `level!=null` filter `findByLevel`, else `findAll`).

- [ ] **Step 6: Run — verify it passes.** → PASS.

- [ ] **Step 7: Seed the hierarchy** in `DataSeeder.seedMerchNodes()` transcribed from `PAGES.hierarchy` (~L639–649): Departments `01 Grocery Food`, `02 Fresh & Perishables`, `03 Non-Food & Household`, `04 Fashion & Apparel`; under 01: `0110 Dairy & Chilled` → `0110-01 Milk` with children `0110-01-101 Fresh Milk`, `-102 UHT Milk`, `-103 Powdered Milk`; `0110-02 Cheese`; `0110-03 Yogurt`; `0120 Bakery`; `0130 Beverages`. Seed children **directly via repository** (`nodes.save`), NOT via `service.create`, to avoid re-scaffolding.

- [ ] **Step 8: Commit** — `git commit -m "feat: MerchNode hierarchy API with sub-class 101/102/103 auto-scaffold"`

---

### Task 10: Product + ProductPackUom + derived margin

**Files:**
- Create: `src/main/java/com/puregold/iconnect/product/Product.java`, `ProductPackUom.java`, `ProductDtos.java`, `ProductRepository.java`, `ProductService.java`, `ProductController.java`
- Test: `src/test/java/com/puregold/iconnect/product/ProductApiTest.java`

**Interfaces:**
- Produces: `/api/products`. `Product` fields: `code`(unique; if blank, service assigns `String.valueOf(100000 + id)` via post-save update), `description`(@NotBlank), `primaryBarcode`, `shortName`, `brand`, `merchNodeId`(FK→MerchNode Sub-Class, nullable), `perishable`, `shelfLife`, `vatClass`, `listingFee`("Yes"|"No"), `storeRange`, `buyingUom`, `sellingUom`, `standardCost`(BigDecimal), `baseRetail`(BigDecimal), `status`. Child `ProductPackUom` (`@OneToMany(cascade=ALL, orphanRemoval=true)`): `level`, `uom`, `qtyInBase`(Integer), `barcode`, `orderable`(boolean), `sellable`(boolean). `ProductDtos.Response` includes derived `marginPct` (= `(baseRetail-standardCost)/baseRetail*100`, 1dp, null if either missing/zero) and `preferredVendor` (null/`"—"` until Task 11). `ProductRepository`: `Optional<Product> findByCode(String)`.

- [ ] **Step 1: Write the failing test**

```java
// src/test/java/com/puregold/iconnect/product/ProductApiTest.java
package com.puregold.iconnect.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ProductApiTest {
    @Autowired MockMvc mvc;

    @Test
    void createComputesMargin() throws Exception {
        String body = """
            {"description":"Test Milk 1L","standardCost":58.20,"baseRetail":72.00,"status":"Active"}
            """;
        mvc.perform(post("/api/products").contentType("application/json").content(body))
           .andExpect(status().isCreated())
           .andExpect(jsonPath("$.marginPct").value(19.2));
    }

    @Test
    void rejectsBlankDescription() throws Exception {
        mvc.perform(post("/api/products").contentType("application/json")
               .content("{\"description\":\"\"}"))
           .andExpect(status().isBadRequest())
           .andExpect(jsonPath("$.errors.description").exists());
    }
}
```

- [ ] **Step 2: Run — verify it fails.** → FAIL.

- [ ] **Step 3: Implement** entity + child + DTOs + repository + service + controller. Service `validate`: unique code (skip when null/blank — code auto-assigned); if `merchNodeId` set, it must exist (inject `MerchNodeRepository`). Code auto-assign: if `code` blank on create, save once, then set `code = String.valueOf(100000 + getId())` and save again. Margin in `toResponse`:

```java
private java.math.BigDecimal marginPct(java.math.BigDecimal cost, java.math.BigDecimal retail) {
    if (cost == null || retail == null || retail.signum() == 0) return null;
    return retail.subtract(cost)
        .divide(retail, 4, java.math.RoundingMode.HALF_UP)
        .multiply(java.math.BigDecimal.valueOf(100))
        .setScale(1, java.math.RoundingMode.HALF_UP);
}
```

- [ ] **Step 4: Run — verify it passes.** → PASS (margin = 19.2).

- [ ] **Step 5: Seed products** in `DataSeeder.seedProducts()` from `PAGES.item` rows (~L674–681): 8 items (Alaska Fresh Milk 1L cost 58.20/ret 72.00, Gardenia White Bread 600g 46.50/59.75, Nescafé Classic 100g 118.00/149.75, Selecta Cornetto Choco 110ml 22.80/32.00, Fresh Eggs Medium Tray 30s 196.00/238.00, Lucky Me Pancit Canton 60g 9.20/13.50, Pure Basics Flour 1kg 44.00/55.00, Nature's Peak Mixed Veg 400g 52.00/68.00) each mapped to a seeded Sub-Class node id. Seed 3 `ProductPackUom` rows for Alaska Fresh Milk from the Pack & UOM tab (~L712–714): Base/PC/1/…/orderable=false/sellable=true; Inner/PACK-6/6/…/orderable=true/sellable=false; Case/CASE-12/12/…/orderable=true/sellable=false.

- [ ] **Step 6: Commit** — `git commit -m "feat: Product + PackUOM API with derived margin"`

---

### Task 11: VendorProduct (item↔vendor link) — CCM-read-only cost + single-Primary rule

**Files:**
- Create: `src/main/java/com/puregold/iconnect/vendor/VendorProduct.java`, `VendorProductDtos.java`, `VendorProductRepository.java`, `VendorProductService.java`, `VendorProductController.java`
- Modify: `ProductController.toResponse` (+ inject `VendorProductRepository`, `VendorRepository`) to fill `preferredVendor`
- Test: `src/test/java/com/puregold/iconnect/vendor/VendorProductServiceTest.java`

**Interfaces:**
- Produces: `/api/vendor-products` (list supports `?productId=`). Fields: `companyId`(@NotNull FK), `vendorId`(@NotNull FK), `productId`(@NotNull FK), `vendorItemNo`, `vendorCategoryId`(FK, nullable), `sourcePriority`("Primary"|"Alternate"), `orderPackUom`, `moqCases`(Integer), `orderIncrementCases`(Integer), `orderSchedule`, `leadTime`, `costPerCase`(BigDecimal — **read-only via API**), `allowanceId`(FK, nullable), `status`. Repository: `List<VendorProduct> findByProductId(Long)`, `Optional<VendorProduct> findByProductIdAndSourcePriority(Long, String)`. Service exposes `createFromClient(VendorProduct)` (nulls cost, applies single-Primary demotion) for the controller; seed uses plain `repo.save`.

- [ ] **Step 1: Write the failing tests (both rules)**

```java
// src/test/java/com/puregold/iconnect/vendor/VendorProductServiceTest.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.company.Company;
import com.puregold.iconnect.company.CompanyRepository;
import com.puregold.iconnect.product.Product;
import com.puregold.iconnect.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VendorProductServiceTest {
    @Autowired VendorProductService service;
    @Autowired VendorProductRepository repo;
    @Autowired VendorRepository vendors;
    @Autowired CompanyRepository companies;
    @Autowired ProductRepository products;

    Long companyId, v1, v2, productId;

    @BeforeEach
    void setup() {
        Company c = new Company(); c.setCode("VPTEST"+System.nanoTime()); c.setLegalName("VP Test");
        companyId = companies.save(c).getId();
        Vendor a = new Vendor(); a.setName("VP Vendor A"); a.setRtvAllowed("Yes"); a.setCode("VPA"+System.nanoTime());
        Vendor b = new Vendor(); b.setName("VP Vendor B"); b.setRtvAllowed("Yes"); b.setCode("VPB"+System.nanoTime());
        v1 = vendors.save(a).getId(); v2 = vendors.save(b).getId();
        Product p = new Product(); p.setDescription("VP Product"); p.setCode("VPP"+System.nanoTime());
        productId = products.save(p).getId();
    }

    @Test
    void costPerCaseIsIgnoredFromClient() {
        VendorProduct link = newLink("Primary");
        link.setCostPerCase(new BigDecimal("999.99"));
        VendorProduct saved = service.createFromClient(link);
        assertThat(saved.getCostPerCase()).isNull();
    }

    @Test
    void settingSecondPrimaryDemotesFirst() {
        VendorProduct first = service.createFromClient(withVendor(newLink("Primary"), v1));
        VendorProduct second = service.createFromClient(withVendor(newLink("Primary"), v2));
        assertThat(repo.findById(first.getId()).get().getSourcePriority()).isEqualTo("Alternate");
        assertThat(repo.findByProductIdAndSourcePriority(productId, "Primary")).get()
            .extracting(VendorProduct::getId).isEqualTo(second.getId());
    }

    private VendorProduct newLink(String priority) {
        VendorProduct v = new VendorProduct();
        v.setCompanyId(companyId); v.setProductId(productId); v.setVendorId(v1);
        v.setSourcePriority(priority); v.setStatus("Active");
        return v;
    }
    private VendorProduct withVendor(VendorProduct v, Long vendorId){ v.setVendorId(vendorId); return v; }
}
```

- [ ] **Step 2: Run — verify it fails.** → FAIL.

- [ ] **Step 3: Implement.** `createFromClient`: set `costPerCase=null`; if `sourcePriority=="Primary"`, find existing Primary for `productId` (`findByProductIdAndSourcePriority`), and if present + different, set it `"Alternate"` and save; then `repo.save(newLink)`. FK existence checks for company/vendor/product in `validate`. Controller `toEntity`/`apply` never copy `costPerCase` from the request; `create` calls `service.createFromClient(...)` (override the controller's create path, or route the base `create` through a service method that delegates to `createFromClient`).

- [ ] **Step 4: Fill `preferredVendor` on Product response** — inject `VendorProductRepository` + `VendorRepository` into `ProductController`; in `toResponse`, `findByProductIdAndSourcePriority(id,"Primary")` → look up vendor name → else `"—"`.

- [ ] **Step 5: Run — verify it passes.** → PASS.

- [ ] **Step 6: Seed vendor-products** in `DataSeeder.seedVendorProducts()` from `PAGES.vencatalog` (~L608–612) with `costPerCase` set directly (seed path): Nescafé Classic ₱2,832 Primary, Bear Brand ₱4,128 Primary, Milo ₱2,556 Primary, All Purpose Cream ₱1,944 Alternate, Maggi ₱960 Primary (all Nestlé, PGOLD) — plus the two Alaska Fresh Milk links from the item Suppliers tab (~L719–720): Alaska Milk Corp. Primary ₱698.40, Metro Dairy Dist. Alternate ₱705.00. Map product/vendor by seeded name.

- [ ] **Step 7: Commit** — `git commit -m "feat: VendorProduct link API — CCM read-only cost + single-Primary rule; derive Preferred Vendor"`

---

### Task 12: AssortmentPlan (list + create) with persisted line items

**Files:**
- Create: `src/main/java/com/puregold/iconnect/grouping/AssortmentPlan.java`, `AssortmentPlanLine.java`, `AssortmentPlanDtos.java`, `AssortmentPlanRepository.java`, `AssortmentPlanService.java`, `AssortmentPlanController.java`
- Test: `src/test/java/com/puregold/iconnect/grouping/AssortmentPlanApiTest.java`

**Interfaces:**
- Produces: `/api/assortment-plans`. `AssortmentPlan` fields: `name`(@NotBlank), `clusterId`(@NotNull FK→AssortmentCluster), `companyId`(FK), `effectiveFrom`(LocalDate), `status`. Child `AssortmentPlanLine` (`@OneToMany(cascade=ALL, orphanRemoval=true)`): `productId`(FK), `productCode`, `description`. `Request` carries `lines:[{productId,productCode,description}]`; `Response` includes `skuCount`. Service `validate`: `clusterId` must exist.

- [ ] **Step 1: Write the failing test** — POST a plan with 2 line SKUs → 201, `$.skuCount`==2, appears in `GET /api/assortment-plans`.
- [ ] **Step 2: Run — verify it fails.** → FAIL.
- [ ] **Step 3: Implement** entity + child + DTOs + repository + service (validate cluster FK) + controller. Because create carries child lines, override `toEntity` to build the `AssortmentPlanLine` list from `req.lines()`.
- [ ] **Step 4: Run — verify it passes.** → PASS.
- [ ] **Step 5: Wire the existing `openAssortWizard` final Save** in `static/index.html` (~L1542): on submit, POST `{name, clusterId, companyId, effectiveFrom, lines:[...]}` to `/api/assortment-plans`, then `route('assortplan')`. Convert `PAGES.assortplan` list (~L486) to `await api('/assortment-plans')` rendering `skuCount`.
- [ ] **Step 6: Seed** one sample plan referencing a seeded cluster + 3 seeded products.
- [ ] **Step 7: Commit** — `git commit -m "feat: AssortmentPlan API + wizard save wiring"`

---

## Phase 5 — Product pages wiring + naming cleanup

### Task 13: Wire Product & Category Hierarchy pages; cascading classification; cost read-only

**Files:**
- Modify: `src/main/resources/static/index.html` (`PAGES.hierarchy`, `PAGES.item`, `PAGES.vencatalog`; `CREATE.item`/`CREATE.node` saves; item detail Suppliers/Price&Cost tabs)

**Interfaces:**
- Consumes: `/api/merch-nodes`, `/api/products`, `/api/vendor-products` (Tasks 9–11).

- [ ] **Step 1** `PAGES.hierarchy`: `await api('/merch-nodes')`; render the tree by grouping children under parents (level 1→4). Node Detail Save → `PUT /api/merch-nodes/{id}`; New Node → add `node` to the `SAVE` map (`route:'/merch-nodes', page:'hierarchy'`, body from the `CREATE.node` fields — `level`, `parentId`=selId('Parent Node'), `code`=gv('Node Code'), `name`=gv('Node Name'), `merchManager`, `replenStrategy`).
- [ ] **Step 2** `PAGES.item`: list from `/api/products`, showing `preferredVendor` and `marginPct`. Item Detail **Suppliers** tab: `await api('/vendor-products?productId='+id)` and render **Cost / Case as read-only text** (resolves spec §6.2/§8). **Price & Cost** tab: show `marginPct` read-only.
- [ ] **Step 3** `CREATE.item` classification → **cascading** selects: Department from `/api/merch-nodes?level=Department%20(L1)`; on change, Sub-Department from `?level=Sub-Department%20(L2)&parentId=<deptId>`; then Class (`?level=Class%20(L3)&parentId=<subDeptId>`); then Sub-Class (`?level=Sub-Class%20(L4)&parentId=<classId>`). Add `item` to `SAVE` (`route:'/products', page:'item'`, body: `{description, primaryBarcode, shortName, brand, merchNodeId:selId('Sub-Class'), perishable, shelfLife, vatClass, listingFee, storeRange, buyingUom, sellingUom, standardCost:money(gv('Standard Cost')), baseRetail:money(gv('Base Retail (VAT-inc)')), status:'Active'}`).
- [ ] **Step 4** `PAGES.vencatalog`: list from `/api/vendor-products`; add `vencatalog` to `SAVE` (`route:'/vendor-products', page:'vencatalog'`, body per `CREATE.vencatalog` fields, **omitting cost** — the cost field stays read-only "Maintained in CCM"). Fill its Company/Vendor/Item/Vendor-Category selects from the API.
- [ ] **Step 5** Verify in browser: create a Product via cascading classification → appears with margin; link a vendor-product → Preferred Vendor resolves; Suppliers tab shows cost read-only; a second Primary link demotes the first. Restart → persists.
- [ ] **Step 6** Commit — `git commit -m "feat: wire Product + Category Hierarchy pages; cascading classification; read-only CCM cost"`

---

### Task 14: Naming cleanup pass

**Files:**
- Modify: `src/main/resources/static/index.html`

- [ ] **Step 1** Standardize the trio on **"Product"**: `PAGES.item` header "Item Maintenance" → "Product Maintenance"; `PAGES.hierarchy` header "Item Hierarchy" → "Category Hierarchy" (matches the nav label). Nav labels already read "Category Hierarchy"/"Product" — leave them.
- [ ] **Step 2** Grep the file for remaining user-facing "Item Maintenance"/"Item Hierarchy" and reconcile stragglers.
- [ ] **Step 3** Verify pages render with corrected titles.
- [ ] **Step 4** Commit — `git commit -m "chore: standardize Product module naming in mockup"`

---

## Phase 6 — End-to-end verification

### Task 15: Full round-trip + persistence verification

- [ ] **Step 1** Run `mvn -q test` — the whole suite passes (all API, rule, and seed tests).
- [ ] **Step 2** `mvn spring-boot:run`; open `http://localhost:8080`. Walk every Master Data page — each lists seeded data.
- [ ] **Step 3** Create one record in each of: Company, Branch (under a company + grouping), Vendor, a Price Zone, a MerchNode Class (confirm 101/102/103 children appear), a Product (cascading classification), a Vendor Product (confirm cost not editable; confirm a second Primary demotes the first), an Assortment Plan. Each returns to its list with the new row.
- [ ] **Step 4** Stop and restart the app. Reopen — every created record persists (H2 file).
- [ ] **Step 5** Negative checks: blank required field → error; duplicate Company code → error; second Primary vendor link → first flips to Alternate.
- [ ] **Step 6** Commit — `git commit -m "test: end-to-end persistence + rule verification for Master Data"`

---

## Self-Review notes (spec coverage map)

- Spec §4 entities → Company (T4), 9 generic masters (T6), MerchNode (T9), Product+PackUom (T10), VendorProduct (T11), AssortmentPlan (T12). ✅ all 14 + child tables.
- Spec §5 hybrid structure → generic base (T3) + explicit controllers for the 4 rule-heavy (T9–T12). ✅
- Spec §6 rules → sub-class scaffold (T9), cost read-only (T11), single-Primary + derived Preferred Vendor (T11), validation/unique codes/FK checks (T4, T6, T9–T12). ✅
- Spec §7 frontend wiring → api() helper + async route (T5), 9 masters (T8), product pages (T13). ✅
- Spec §2 DB + seed-when-empty → H2 file (T1), DataSeeder guarded seeds (T5, T7, T9, T10, T11, T12). ✅
- Spec §8 naming cleanup → cascading classification (T13), title standardization (T14). ✅
- Spec §2/§3 no-delete / no-auth → base controller exposes only GET/POST/PUT; no Spring Security dependency. ✅
- Spec §9 verification → per-rule tests throughout + E2E (T15). ✅
