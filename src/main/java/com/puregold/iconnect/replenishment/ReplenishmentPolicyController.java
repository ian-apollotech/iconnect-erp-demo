// src/main/java/com/puregold/iconnect/replenishment/ReplenishmentPolicyController.java
package com.puregold.iconnect.replenishment;

import com.puregold.iconnect.common.AbstractCrudController;
import com.puregold.iconnect.grouping.ReplenishmentProfileRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/replenishment-policies")
public class ReplenishmentPolicyController
        extends AbstractCrudController<ReplenishmentPolicy, ReplenishmentPolicyDtos.Request, ReplenishmentPolicyDtos.Response> {

    private final ReplenishmentProfileRepository profiles;

    public ReplenishmentPolicyController(ReplenishmentPolicyService service, ReplenishmentProfileRepository profiles) {
        super(service);
        this.profiles = profiles;
    }

    @Override
    protected ReplenishmentPolicy toEntity(ReplenishmentPolicyDtos.Request req) {
        ReplenishmentPolicy p = new ReplenishmentPolicy();
        apply(req, p);
        return p;
    }

    @Override
    protected void apply(ReplenishmentPolicyDtos.Request req, ReplenishmentPolicy p) {
        p.setProfileId(req.profileId());
        p.setItemScopeType(req.itemScopeType());
        p.setScopeRef(req.scopeRef());
        p.setMethod(req.method());
        p.setTarget(req.target());
        p.setSafetyStockDays(req.safetyStockDays());
        p.setServiceLevel(req.serviceLevel());
        if (req.status() != null) p.setStatus(req.status());
    }

    @Override
    protected ReplenishmentPolicyDtos.Response toResponse(ReplenishmentPolicy p) {
        String profileName = profiles.findById(p.getProfileId()).map(prof -> prof.getName()).orElse(null);
        return new ReplenishmentPolicyDtos.Response(
            p.getId(), p.getProfileId(), profileName, p.getItemScopeType(), p.getScopeRef(),
            p.getMethod(), p.getTarget(), p.getSafetyStockDays(), p.getServiceLevel(), p.getStatus());
    }
}
