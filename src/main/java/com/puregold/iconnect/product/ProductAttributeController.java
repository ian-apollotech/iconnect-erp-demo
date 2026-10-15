// src/main/java/com/puregold/iconnect/product/ProductAttributeController.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/product-attributes")
public class ProductAttributeController
        extends AbstractCrudController<ProductAttribute, ProductAttributeDtos.Request, ProductAttributeDtos.Response> {

    public ProductAttributeController(ProductAttributeService service) {
        super(service);
    }

    @Override
    protected ProductAttribute toEntity(ProductAttributeDtos.Request req) {
        ProductAttribute a = new ProductAttribute();
        apply(req, a);
        return a;
    }

    @Override
    protected void apply(ProductAttributeDtos.Request req, ProductAttribute a) {
        a.setName(req.name());
        a.setCode(req.code());
        a.setAllowedValues(req.allowedValues());
        if (req.status() != null) a.setStatus(req.status());
        a.setDescription(req.description());
    }

    @Override
    protected ProductAttributeDtos.Response toResponse(ProductAttribute a) {
        return new ProductAttributeDtos.Response(
            a.getId(), a.getName(), a.getCode(), a.getAllowedValues(), a.getStatus(), a.getDescription());
    }
}
