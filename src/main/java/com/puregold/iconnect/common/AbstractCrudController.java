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
