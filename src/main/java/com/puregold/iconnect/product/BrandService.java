// src/main/java/com/puregold/iconnect/product/BrandService.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.vendor.Vendor;
import com.puregold.iconnect.vendor.VendorRepository;
import org.springframework.stereotype.Service;

@Service
public class BrandService extends AbstractCrudService<Brand> {

    private final BrandRepository brands;
    private final VendorRepository vendors;

    public BrandService(BrandRepository brands, VendorRepository vendors) {
        super(brands, "Brand");
        this.brands = brands;
        this.vendors = vendors;
    }

    @Override
    protected void validate(Brand entity) {
        if (entity.getCode() != null) {
            brands.findByCode(entity.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(entity.getId())) {
                    throw new IllegalArgumentException("Brand code already exists: " + entity.getCode());
                }
            });
        }

        if (entity.getManufacturerId() == null) {
            throw new IllegalArgumentException("Manufacturer is required");
        } else {
            Vendor manufacturer = vendors.findById(entity.getManufacturerId())
                .orElseThrow(() -> new IllegalArgumentException(
                    "Manufacturer (vendor) not found: " + entity.getManufacturerId()));
            String vendorType = manufacturer.getVendorType();
            if (vendorType == null || !vendorType.contains("Manufacturer")) {
                throw new IllegalArgumentException(
                    "Manufacturer must be a manufacturer-type vendor: " + entity.getManufacturerId());
            }
        }
    }
}
