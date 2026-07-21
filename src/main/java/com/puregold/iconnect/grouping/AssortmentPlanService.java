// src/main/java/com/puregold/iconnect/grouping/AssortmentPlanService.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class AssortmentPlanService extends AbstractCrudService<AssortmentPlan> {

    private final AssortmentClusterRepository clusters;
    private final CompanyRepository companies;

    public AssortmentPlanService(AssortmentPlanRepository assortmentPlans, AssortmentClusterRepository clusters,
                                  CompanyRepository companies) {
        super(assortmentPlans, "AssortmentPlan");
        this.clusters = clusters;
        this.companies = companies;
    }

    @Override
    protected void validate(AssortmentPlan entity) {
        if (entity.getClusterId() != null && clusters.findById(entity.getClusterId()).isEmpty()) {
            throw new IllegalArgumentException("Assortment Cluster not found: " + entity.getClusterId());
        }
        if (entity.getCompanyId() != null && companies.findById(entity.getCompanyId()).isEmpty()) {
            throw new IllegalArgumentException("Company not found: " + entity.getCompanyId());
        }
    }
}
