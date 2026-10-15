// src/main/java/com/puregold/iconnect/replenishment/ReplenishmentPolicyService.java
package com.puregold.iconnect.replenishment;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.grouping.ReplenishmentProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class ReplenishmentPolicyService extends AbstractCrudService<ReplenishmentPolicy> {

    private final ReplenishmentProfileRepository profiles;

    public ReplenishmentPolicyService(ReplenishmentPolicyRepository replenishmentPolicies,
                                       ReplenishmentProfileRepository profiles) {
        super(replenishmentPolicies, "ReplenishmentPolicy");
        this.profiles = profiles;
    }

    @Override
    protected void validate(ReplenishmentPolicy entity) {
        if (entity.getProfileId() == null) {
            throw new IllegalArgumentException("Target Profile is required");
        } else if (profiles.findById(entity.getProfileId()).isEmpty()) {
            throw new IllegalArgumentException("Replenishment Profile not found: " + entity.getProfileId());
        }
    }
}
