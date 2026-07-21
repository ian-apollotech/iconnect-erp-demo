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
