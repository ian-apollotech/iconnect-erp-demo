// src/main/java/com/puregold/iconnect/grouping/AssortmentClusterService.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class AssortmentClusterService extends AbstractCrudService<AssortmentCluster> {

    private final AssortmentClusterRepository assortmentClusters;
    private final CompanyRepository companies;

    public AssortmentClusterService(AssortmentClusterRepository assortmentClusters, CompanyRepository companies) {
        super(assortmentClusters, "AssortmentCluster");
        this.assortmentClusters = assortmentClusters;
        this.companies = companies;
    }

    @Override
    protected void validate(AssortmentCluster entity) {
        if (entity.getCode() != null) {
            assortmentClusters.findByCode(entity.getCode()).ifPresent(existing -> {
                if (!existing.getId().equals(entity.getId())) {
                    throw new IllegalArgumentException("Assortment Cluster code already exists: " + entity.getCode());
                }
            });
        }

        if (entity.getCompanyId() != null && companies.findById(entity.getCompanyId()).isEmpty()) {
            throw new IllegalArgumentException("Company not found: " + entity.getCompanyId());
        }
    }
}
