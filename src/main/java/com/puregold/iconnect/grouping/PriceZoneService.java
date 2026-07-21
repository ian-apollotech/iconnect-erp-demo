// src/main/java/com/puregold/iconnect/grouping/PriceZoneService.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class PriceZoneService extends AbstractCrudService<PriceZone> {

    private final PriceZoneRepository priceZones;
    private final CompanyRepository companies;

    public PriceZoneService(PriceZoneRepository priceZones, CompanyRepository companies) {
        super(priceZones, "PriceZone");
        this.priceZones = priceZones;
        this.companies = companies;
    }

    @Override
    protected void validate(PriceZone entity) {
        if (entity.getCode() != null) {
            priceZones.findByCode(entity.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(entity.getId())) {
                    throw new IllegalArgumentException("Price Zone code already exists: " + entity.getCode());
                }
            });
        }

        if (entity.getCompanyId() != null && companies.findById(entity.getCompanyId()).isEmpty()) {
            throw new IllegalArgumentException("Company not found: " + entity.getCompanyId());
        }
    }
}
