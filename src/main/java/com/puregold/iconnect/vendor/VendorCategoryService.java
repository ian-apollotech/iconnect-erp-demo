// src/main/java/com/puregold/iconnect/vendor/VendorCategoryService.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class VendorCategoryService extends AbstractCrudService<VendorCategory> {

    private static final String LEVEL_L1 = "Category (L1)";
    private static final String LEVEL_L2 = "Sub-Category (L2)";

    private final VendorCategoryRepository categories;
    private final VendorRepository vendors;

    public VendorCategoryService(VendorCategoryRepository categories, VendorRepository vendors) {
        super(categories, "VendorCategory");
        this.categories = categories;
        this.vendors = vendors;
    }

    @Override
    protected void validate(VendorCategory entity) {
        if (entity.getVendorId() != null && vendors.findById(entity.getVendorId()).isEmpty()) {
            throw new IllegalArgumentException("Vendor not found: " + entity.getVendorId());
        }

        if (LEVEL_L2.equals(entity.getLevel())) {
            if (entity.getParentCategoryId() == null) {
                throw new IllegalArgumentException("Sub-Category (L2) requires a parentCategoryId");
            }
            VendorCategory parent = categories.findById(entity.getParentCategoryId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Parent category not found: " + entity.getParentCategoryId()));
            if (!LEVEL_L1.equals(parent.getLevel())) {
                throw new IllegalArgumentException("Parent category must be Category (L1)");
            }
            if (!parent.getVendorId().equals(entity.getVendorId())) {
                throw new IllegalArgumentException("Parent category must belong to the same vendor");
            }
        }
    }
}
