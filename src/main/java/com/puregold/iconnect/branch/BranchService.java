// src/main/java/com/puregold/iconnect/branch/BranchService.java
package com.puregold.iconnect.branch;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class BranchService extends AbstractCrudService<Branch> {

    private final BranchRepository branches;
    private final CompanyRepository companies;

    public BranchService(BranchRepository branches, CompanyRepository companies) {
        super(branches, "Branch");
        this.branches = branches;
        this.companies = companies;
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

        // NOTE: priceZoneId / costZoneId / assortmentClusterId / replenishmentProfileId FK
        // validation is deliberately deferred to sub-item 6j, once the grouping entities exist.
    }
}
