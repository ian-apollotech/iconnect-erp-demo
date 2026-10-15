// src/main/java/com/puregold/iconnect/product/ProductAttributeService.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class ProductAttributeService extends AbstractCrudService<ProductAttribute> {

    private final ProductAttributeRepository productAttributes;

    public ProductAttributeService(ProductAttributeRepository productAttributes) {
        super(productAttributes, "ProductAttribute");
        this.productAttributes = productAttributes;
    }

    @Override
    protected void validate(ProductAttribute entity) {
        if (entity.getCode() != null) {
            productAttributes.findByCode(entity.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(entity.getId())) {
                    throw new IllegalArgumentException("Product Attribute code already exists: " + entity.getCode());
                }
            });
        }
    }
}
