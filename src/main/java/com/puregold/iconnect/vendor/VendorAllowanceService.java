// src/main/java/com/puregold/iconnect/vendor/VendorAllowanceService.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class VendorAllowanceService extends AbstractCrudService<VendorAllowance> {

    private final VendorAllowanceRepository allowances;
    private final VendorRepository vendors;

    public VendorAllowanceService(VendorAllowanceRepository allowances, VendorRepository vendors) {
        super(allowances, "VendorAllowance");
        this.allowances = allowances;
        this.vendors = vendors;
    }

    @Override
    public VendorAllowance create(VendorAllowance entity) {
        validate(entity);
        VendorAllowance saved = repo.save(entity);
        if (saved.getRef() == null || saved.getRef().isBlank()) {
            saved.setRef("AL-" + saved.getId());
            saved = repo.save(saved);
        }
        return saved;
    }

    @Override
    protected void validate(VendorAllowance entity) {
        if (entity.getRef() != null && !entity.getRef().isBlank()) {
            allowances.findByRef(entity.getRef()).ifPresent(existing -> {
                if (!existing.getId().equals(entity.getId())) {
                    throw new IllegalArgumentException("Vendor allowance ref already exists: " + entity.getRef());
                }
            });
        }

        if (entity.getVendorId() != null && vendors.findById(entity.getVendorId()).isEmpty()) {
            throw new IllegalArgumentException("Vendor not found: " + entity.getVendorId());
        }
    }
}
