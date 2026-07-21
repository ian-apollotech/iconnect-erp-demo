// src/main/java/com/puregold/iconnect/vendor/VendorService.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class VendorService extends AbstractCrudService<Vendor> {

    private final VendorRepository vendors;

    public VendorService(VendorRepository vendors) {
        super(vendors, "Vendor");
        this.vendors = vendors;
    }

    @Override
    public Vendor create(Vendor entity) {
        validate(entity);
        Vendor saved = repo.save(entity);
        if (saved.getCode() == null || saved.getCode().isBlank()) {
            saved.setCode("V-" + saved.getId());
            saved = repo.save(saved);
        }
        return saved;
    }

    @Override
    protected void validate(Vendor entity) {
        if (entity.getCode() == null || entity.getCode().isBlank()) {
            return;
        }
        vendors.findByCode(entity.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(entity.getId())) {
                throw new IllegalArgumentException("Vendor code already exists: " + entity.getCode());
            }
        });
    }
}
