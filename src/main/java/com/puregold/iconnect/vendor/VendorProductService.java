// src/main/java/com/puregold/iconnect/vendor/VendorProductService.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import com.puregold.iconnect.product.ProductRepository;
import org.springframework.stereotype.Service;

@Service
public class VendorProductService extends AbstractCrudService<VendorProduct> {

    private final VendorProductRepository vendorProducts;
    private final CompanyRepository companies;
    private final VendorRepository vendors;
    private final ProductRepository products;

    public VendorProductService(VendorProductRepository vendorProducts, CompanyRepository companies,
                                 VendorRepository vendors, ProductRepository products) {
        super(vendorProducts, "VendorProduct");
        this.vendorProducts = vendorProducts;
        this.companies = companies;
        this.vendors = vendors;
        this.products = products;
    }

    /**
     * Entry point for client-originated create/save. Cost / Case is owned by Cost Change
     * Management, so it is always nulled here regardless of what the client sent. If the
     * incoming link claims "Primary" for its product, any existing Primary for that product
     * is demoted to "Alternate" first (single-Primary-per-product rule).
     */
    public VendorProduct createFromClient(VendorProduct link) {
        link.setCostPerCase(null);
        if ("Primary".equals(link.getSourcePriority())) {
            vendorProducts.findByProductIdAndSourcePriority(link.getProductId(), "Primary")
                .ifPresent(existing -> {
                    if (link.getId() == null || !existing.getId().equals(link.getId())) {
                        existing.setSourcePriority("Alternate");
                        vendorProducts.save(existing);
                    }
                });
        }
        return create(link);
    }

    @Override
    protected void validate(VendorProduct entity) {
        if (entity.getCompanyId() != null && companies.findById(entity.getCompanyId()).isEmpty()) {
            throw new IllegalArgumentException("Company not found: " + entity.getCompanyId());
        }
        if (entity.getVendorId() != null && vendors.findById(entity.getVendorId()).isEmpty()) {
            throw new IllegalArgumentException("Vendor not found: " + entity.getVendorId());
        }
        if (entity.getProductId() != null && products.findById(entity.getProductId()).isEmpty()) {
            throw new IllegalArgumentException("Product not found: " + entity.getProductId());
        }
    }
}
