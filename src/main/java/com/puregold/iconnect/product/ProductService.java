// src/main/java/com/puregold/iconnect/product/ProductService.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class ProductService extends AbstractCrudService<Product> {

    private final ProductRepository products;
    private final MerchNodeRepository merchNodes;

    public ProductService(ProductRepository products, MerchNodeRepository merchNodes) {
        super(products, "Product");
        this.products = products;
        this.merchNodes = merchNodes;
    }

    @Override
    protected void validate(Product p) {
        if (p.getCode() != null && !p.getCode().isBlank()) {
            products.findByCode(p.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(p.getId()))
                    throw new IllegalArgumentException("Product code already exists: " + p.getCode());
            });
        }
        if (p.getMerchNodeId() != null && merchNodes.findById(p.getMerchNodeId()).isEmpty())
            throw new IllegalArgumentException("MerchNode not found: " + p.getMerchNodeId());
    }

    @Override
    public Product create(Product entity) {
        Product saved = super.create(entity);
        if (saved.getCode() == null || saved.getCode().isBlank()) {
            saved.setCode(String.valueOf(100000 + saved.getId()));
            saved = products.save(saved);
        }
        return saved;
    }
}
