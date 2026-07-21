// src/main/java/com/puregold/iconnect/grouping/AssortmentPlanService.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.common.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class AssortmentPlanService extends AbstractCrudService<AssortmentPlan> {

    private final AssortmentClusterRepository clusters;

    public AssortmentPlanService(AssortmentPlanRepository assortmentPlans, AssortmentClusterRepository clusters) {
        super(assortmentPlans, "AssortmentPlan");
        this.clusters = clusters;
    }

    @Override
    protected void validate(AssortmentPlan entity) {
        if (entity.getClusterId() != null && clusters.findById(entity.getClusterId()).isEmpty()) {
            throw new IllegalArgumentException("Assortment Cluster not found: " + entity.getClusterId());
        }
    }
}
