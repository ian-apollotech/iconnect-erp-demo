// src/main/java/com/puregold/iconnect/vendor/VendorProductController.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudController;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/vendor-products")
public class VendorProductController
        extends AbstractCrudController<VendorProduct, VendorProductDtos.Request, VendorProductDtos.Response> {

    private final VendorProductService vendorProductService;
    private final VendorProductRepository vendorProducts;

    public VendorProductController(VendorProductService service, VendorProductRepository vendorProducts) {
        super(service);
        this.vendorProductService = service;
        this.vendorProducts = vendorProducts;
    }

    // Inherited zero-arg list() (mapped to unconditioned GET /api/vendor-products) handles the
    // no-filter case; this params-conditioned mapping takes precedence when productId is present.
    @GetMapping(params = "productId")
    public List<VendorProductDtos.Response> listByProduct(@RequestParam Long productId) {
        return vendorProducts.findByProductId(productId).stream().map(this::toResponse).toList();
    }

    // Overrides the base create() so every client-originated save routes through
    // createFromClient — nulling costPerCase and applying the single-Primary demotion.
    @Override
    @PostMapping
    public ResponseEntity<VendorProductDtos.Response> create(@Valid @RequestBody VendorProductDtos.Request req) {
        VendorProduct saved = vendorProductService.createFromClient(toEntity(req));
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(saved));
    }

    // Overrides the base update() so every client-originated save routes through
    // updateFromClient — re-applying the single-Primary demotion (createFromClient already
    // does this on POST, but the inherited PUT path otherwise never runs it).
    @Override
    @PutMapping("/{id}")
    public VendorProductDtos.Response update(@PathVariable Long id, @Valid @RequestBody VendorProductDtos.Request req) {
        VendorProduct saved = vendorProductService.updateFromClient(id, entity -> apply(req, entity));
        return toResponse(saved);
    }

    @Override
    protected VendorProduct toEntity(VendorProductDtos.Request req) {
        VendorProduct v = new VendorProduct();
        apply(req, v);
        return v;
    }

    @Override
    protected void apply(VendorProductDtos.Request req, VendorProduct v) {
        v.setCompanyId(req.companyId());
        v.setVendorId(req.vendorId());
        v.setProductId(req.productId());
        v.setVendorItemNo(req.vendorItemNo());
        v.setVendorCategoryId(req.vendorCategoryId());
        v.setSourcePriority(req.sourcePriority());
        v.setOrderPackUom(req.orderPackUom());
        v.setMoqCases(req.moqCases());
        v.setOrderIncrementCases(req.orderIncrementCases());
        v.setOrderSchedule(req.orderSchedule());
        v.setLeadTime(req.leadTime());
        // costPerCase intentionally NOT copied from the request — CCM-owned, read-only via API.
        v.setAllowanceId(req.allowanceId());
        if (req.status() != null) v.setStatus(req.status());
    }

    @Override
    protected VendorProductDtos.Response toResponse(VendorProduct v) {
        return new VendorProductDtos.Response(
            v.getId(), v.getCompanyId(), v.getVendorId(), v.getProductId(), v.getVendorItemNo(),
            v.getVendorCategoryId(), v.getSourcePriority(), v.getOrderPackUom(), v.getMoqCases(),
            v.getOrderIncrementCases(), v.getOrderSchedule(), v.getLeadTime(), v.getCostPerCase(),
            v.getAllowanceId(), v.getStatus());
    }
}
