// src/main/java/com/puregold/iconnect/vendor/VendorProductService.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import com.puregold.iconnect.product.ProductRepository;
import org.springframework.stereotype.Service;

import java.util.function.Consumer;

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
            demoteOtherPrimaries(link);
        }
        return create(link);
    }

    /**
     * Entry point for client-originated update/save (PUT). Mirrors createFromClient's
     * single-Primary enforcement so promoting a link to "Primary" via update also demotes
     * any other existing Primary for the same product. costPerCase is left untouched here —
     * applyChanges (built from the request DTO) never copies it, so the CCM-owned value already
     * on the row survives the update unchanged.
     */
    public VendorProduct updateFromClient(Long id, Consumer<VendorProduct> applyChanges) {
        return update(id, entity -> {
            applyChanges.accept(entity);
            if ("Primary".equals(entity.getSourcePriority())) {
                demoteOtherPrimaries(entity);
            }
        });
    }

    /**
     * Enforces the single-Primary-per-product invariant: if another link for the same product
     * currently holds "Primary" and it isn't this link itself, demote it to "Alternate".
     */
    private void demoteOtherPrimaries(VendorProduct link) {
        vendorProducts.findByProductIdAndSourcePriority(link.getProductId(), "Primary")
            .ifPresent(existing -> {
                if (link.getId() == null || !existing.getId().equals(link.getId())) {
                    existing.setSourcePriority("Alternate");
                    vendorProducts.save(existing);
                }
            });
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
