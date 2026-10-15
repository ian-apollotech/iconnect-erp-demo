// src/main/java/com/puregold/iconnect/branch/BranchController.java
package com.puregold.iconnect.branch;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/branches")
public class BranchController
        extends AbstractCrudController<Branch, BranchDtos.Request, BranchDtos.Response> {

    public BranchController(BranchService service) { super(service); }

    @Override
    protected Branch toEntity(BranchDtos.Request req) {
        Branch b = new Branch();
        apply(req, b);
        return b;
    }

    @Override
    protected void apply(BranchDtos.Request req, Branch b) {
        b.setCode(req.code());
        b.setName(req.name());
        b.setFormat(req.format());
        b.setRegion(req.region());
        b.setCompanyId(req.companyId());
        b.setInventoryLocations(req.inventoryLocations());
        b.setCrossDockHub(req.crossDockHub());
        b.setOpeningDate(req.openingDate());
        b.setPriceZoneId(req.priceZoneId());
        b.setCostZoneId(req.costZoneId());
        b.setAssortmentClusterId(req.assortmentClusterId());
        b.setReplenishmentProfileId(req.replenishmentProfileId());
        b.setStreet(req.street());
        b.setCity(req.city());
        b.setProvince(req.province());
        if (req.status() != null) b.setStatus(req.status());
    }

    @Override
    protected BranchDtos.Response toResponse(Branch b) {
        return new BranchDtos.Response(
            b.getId(), b.getCode(), b.getName(), b.getFormat(), b.getRegion(), b.getCompanyId(),
            b.getInventoryLocations(), b.getCrossDockHub(), b.getOpeningDate(),
            b.getPriceZoneId(), b.getCostZoneId(), b.getAssortmentClusterId(), b.getReplenishmentProfileId(),
            b.getStreet(), b.getCity(), b.getProvince(), b.getStatus());
    }
}
