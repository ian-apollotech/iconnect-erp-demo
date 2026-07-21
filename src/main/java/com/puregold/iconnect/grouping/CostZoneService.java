// src/main/java/com/puregold/iconnect/grouping/CostZoneService.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class CostZoneService extends AbstractCrudService<CostZone> {

    private final CostZoneRepository costZones;
    private final CompanyRepository companies;

    public CostZoneService(CostZoneRepository costZones, CompanyRepository companies) {
        super(costZones, "CostZone");
        this.costZones = costZones;
        this.companies = companies;
    }

    @Override
    protected void validate(CostZone entity) {
        if (entity.getCode() != null) {
            costZones.findByCode(entity.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(entity.getId())) {
                    throw new IllegalArgumentException("Cost Zone code already exists: " + entity.getCode());
                }
            });
        }

        if (entity.getCompanyId() != null && companies.findById(entity.getCompanyId()).isEmpty()) {
            throw new IllegalArgumentException("Company not found: " + entity.getCompanyId());
        }
    }
}
