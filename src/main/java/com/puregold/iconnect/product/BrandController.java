// src/main/java/com/puregold/iconnect/product/BrandController.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.AbstractCrudController;
import com.puregold.iconnect.vendor.Vendor;
import com.puregold.iconnect.vendor.VendorRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/brands")
public class BrandController
        extends AbstractCrudController<Brand, BrandDtos.Request, BrandDtos.Response> {

    private final VendorRepository vendorRepository;
    private final ProductRepository productRepository;

    public BrandController(BrandService service, VendorRepository vendorRepository,
                            ProductRepository productRepository) {
        super(service);
        this.vendorRepository = vendorRepository;
        this.productRepository = productRepository;
    }

    @Override
    protected Brand toEntity(BrandDtos.Request req) {
        Brand b = new Brand();
        apply(req, b);
        return b;
    }

    @Override
    protected void apply(BrandDtos.Request req, Brand b) {
        b.setName(req.name());
        b.setCode(req.code());
        b.setManufacturerId(req.manufacturerId());
        if (req.status() != null) b.setStatus(req.status());
        b.setDescription(req.description());
    }

    @Override
    protected BrandDtos.Response toResponse(Brand b) {
        Vendor manufacturer = b.getManufacturerId() == null
            ? null
            : vendorRepository.findById(b.getManufacturerId()).orElse(null);
        String manufacturerName = manufacturer == null ? null : manufacturer.getName();
        String manufacturerCode = manufacturer == null ? null : manufacturer.getCode();
        long productCount = productRepository.countByBrand(b.getName());

        return new BrandDtos.Response(
            b.getId(), b.getName(), b.getCode(), b.getManufacturerId(), manufacturerName,
            manufacturerCode, productCount, b.getStatus(), b.getDescription());
    }
}
