// src/main/java/com/puregold/iconnect/grouping/ReplenishmentProfileController.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.branch.BranchRepository;
import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/replenishment-profiles")
public class ReplenishmentProfileController
        extends AbstractCrudController<ReplenishmentProfile, ReplenishmentProfileDtos.Request, ReplenishmentProfileDtos.Response> {

    private final BranchRepository branchRepository;

    public ReplenishmentProfileController(ReplenishmentProfileService service, BranchRepository branchRepository) {
        super(service);
        this.branchRepository = branchRepository;
    }

    @Override
    protected ReplenishmentProfile toEntity(ReplenishmentProfileDtos.Request req) {
        ReplenishmentProfile r = new ReplenishmentProfile();
        apply(req, r);
        return r;
    }

    @Override
    protected void apply(ReplenishmentProfileDtos.Request req, ReplenishmentProfile r) {
        r.setName(req.name());
        r.setCode(req.code());
        r.setCompanyId(req.companyId());
        if (req.status() != null) r.setStatus(req.status());
        r.setDescription(req.description());
    }

    @Override
    protected ReplenishmentProfileDtos.Response toResponse(ReplenishmentProfile r) {
        return new ReplenishmentProfileDtos.Response(
            r.getId(), r.getName(), r.getCode(), r.getCompanyId(), r.getStatus(), r.getDescription(),
            branchRepository.countByReplenishmentProfileId(r.getId()));
    }
}
