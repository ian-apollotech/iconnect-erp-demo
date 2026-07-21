// src/main/java/com/puregold/iconnect/branch/BranchService.java
package com.puregold.iconnect.branch;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import com.puregold.iconnect.grouping.AssortmentClusterRepository;
import com.puregold.iconnect.grouping.CostZoneRepository;
import com.puregold.iconnect.grouping.PriceZoneRepository;
import com.puregold.iconnect.grouping.ReplenishmentProfileRepository;
import org.springframework.stereotype.Service;

@Service
public class BranchService extends AbstractCrudService<Branch> {

    private final BranchRepository branches;
    private final CompanyRepository companies;
    private final PriceZoneRepository priceZones;
    private final CostZoneRepository costZones;
    private final AssortmentClusterRepository assortmentClusters;
    private final ReplenishmentProfileRepository replenishmentProfiles;

    public BranchService(BranchRepository branches, CompanyRepository companies,
                          PriceZoneRepository priceZones, CostZoneRepository costZones,
                          AssortmentClusterRepository assortmentClusters,
                          ReplenishmentProfileRepository replenishmentProfiles) {
        super(branches, "Branch");
        this.branches = branches;
        this.companies = companies;
        this.priceZones = priceZones;
        this.costZones = costZones;
        this.assortmentClusters = assortmentClusters;
        this.replenishmentProfiles = replenishmentProfiles;
    }

    @Override
    protected void validate(Branch entity) {
        branches.findByCode(entity.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(entity.getId())) {
                throw new IllegalArgumentException("Branch code already exists: " + entity.getCode());
            }
        });

        if (entity.getCompanyId() != null && companies.findById(entity.getCompanyId()).isEmpty()) {
            throw new IllegalArgumentException("Company not found: " + entity.getCompanyId());
        }

        if (entity.getPriceZoneId() != null && priceZones.findById(entity.getPriceZoneId()).isEmpty()) {
            throw new IllegalArgumentException("Price Zone not found: " + entity.getPriceZoneId());
        }
        if (entity.getCostZoneId() != null && costZones.findById(entity.getCostZoneId()).isEmpty()) {
            throw new IllegalArgumentException("Cost Zone not found: " + entity.getCostZoneId());
        }
        if (entity.getAssortmentClusterId() != null
                && assortmentClusters.findById(entity.getAssortmentClusterId()).isEmpty()) {
            throw new IllegalArgumentException("Assortment Cluster not found: " + entity.getAssortmentClusterId());
        }
        if (entity.getReplenishmentProfileId() != null
                && replenishmentProfiles.findById(entity.getReplenishmentProfileId()).isEmpty()) {
            throw new IllegalArgumentException("Replenishment Profile not found: " + entity.getReplenishmentProfileId());
        }
    }
}
