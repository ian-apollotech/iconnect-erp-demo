// src/main/java/com/puregold/iconnect/grouping/CostZoneController.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.branch.BranchRepository;
import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cost-zones")
public class CostZoneController
        extends AbstractCrudController<CostZone, CostZoneDtos.Request, CostZoneDtos.Response> {

    private final BranchRepository branchRepository;

    public CostZoneController(CostZoneService service, BranchRepository branchRepository) {
        super(service);
        this.branchRepository = branchRepository;
    }

    @Override
    protected CostZone toEntity(CostZoneDtos.Request req) {
        CostZone c = new CostZone();
        apply(req, c);
        return c;
    }

    @Override
    protected void apply(CostZoneDtos.Request req, CostZone c) {
        c.setName(req.name());
        c.setCode(req.code());
        c.setCompanyId(req.companyId());
        if (req.status() != null) c.setStatus(req.status());
        c.setDescription(req.description());
    }

    @Override
    protected CostZoneDtos.Response toResponse(CostZone c) {
        return new CostZoneDtos.Response(
            c.getId(), c.getName(), c.getCode(), c.getCompanyId(), c.getStatus(), c.getDescription(),
            branchRepository.countByCostZoneId(c.getId()));
    }
}
