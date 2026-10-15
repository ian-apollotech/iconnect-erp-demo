// src/main/java/com/puregold/iconnect/grouping/PriceZoneController.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.branch.BranchRepository;
import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/price-zones")
public class PriceZoneController
        extends AbstractCrudController<PriceZone, PriceZoneDtos.Request, PriceZoneDtos.Response> {

    private final BranchRepository branchRepository;

    public PriceZoneController(PriceZoneService service, BranchRepository branchRepository) {
        super(service);
        this.branchRepository = branchRepository;
    }

    @Override
    protected PriceZone toEntity(PriceZoneDtos.Request req) {
        PriceZone p = new PriceZone();
        apply(req, p);
        return p;
    }

    @Override
    protected void apply(PriceZoneDtos.Request req, PriceZone p) {
        p.setName(req.name());
        p.setCode(req.code());
        p.setCompanyId(req.companyId());
        if (req.status() != null) p.setStatus(req.status());
        p.setDescription(req.description());
    }

    @Override
    protected PriceZoneDtos.Response toResponse(PriceZone p) {
        return new PriceZoneDtos.Response(
            p.getId(), p.getName(), p.getCode(), p.getCompanyId(), p.getStatus(), p.getDescription(),
            branchRepository.countByPriceZoneId(p.getId()));
    }
}
