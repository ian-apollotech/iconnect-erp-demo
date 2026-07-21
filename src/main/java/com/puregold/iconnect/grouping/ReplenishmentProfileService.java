// src/main/java/com/puregold/iconnect/grouping/ReplenishmentProfileService.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class ReplenishmentProfileService extends AbstractCrudService<ReplenishmentProfile> {

    private final ReplenishmentProfileRepository replenishmentProfiles;
    private final CompanyRepository companies;

    public ReplenishmentProfileService(ReplenishmentProfileRepository replenishmentProfiles, CompanyRepository companies) {
        super(replenishmentProfiles, "ReplenishmentProfile");
        this.replenishmentProfiles = replenishmentProfiles;
        this.companies = companies;
    }

    @Override
    protected void validate(ReplenishmentProfile entity) {
        if (entity.getCode() != null) {
            replenishmentProfiles.findByCode(entity.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(entity.getId())) {
                    throw new IllegalArgumentException("Replenishment Profile code already exists: " + entity.getCode());
                }
            });
        }

        if (entity.getCompanyId() != null && companies.findById(entity.getCompanyId()).isEmpty()) {
            throw new IllegalArgumentException("Company not found: " + entity.getCompanyId());
        }
    }
}
