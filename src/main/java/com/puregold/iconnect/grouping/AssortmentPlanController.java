// src/main/java/com/puregold/iconnect/grouping/AssortmentPlanController.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/backend/assortment-plans")
public class AssortmentPlanController
        extends AbstractCrudController<AssortmentPlan, AssortmentPlanDtos.Request, AssortmentPlanDtos.Response> {

    public AssortmentPlanController(AssortmentPlanService service) { super(service); }

    @Override
    protected AssortmentPlan toEntity(AssortmentPlanDtos.Request req) {
        AssortmentPlan a = new AssortmentPlan();
        apply(req, a);
        return a;
    }

    @Override
    protected void apply(AssortmentPlanDtos.Request req, AssortmentPlan a) {
        a.setName(req.name());
        a.setClusterId(req.clusterId());
        a.setCompanyId(req.companyId());
        a.setEffectiveFrom(req.effectiveFrom());
        if (req.status() != null) a.setStatus(req.status());

        if (req.lines() != null) {
            a.getLines().clear();
            for (AssortmentPlanDtos.LineRequest lr : req.lines()) {
                AssortmentPlanLine line = new AssortmentPlanLine();
                line.setProductId(lr.productId());
                line.setProductCode(lr.productCode());
                line.setDescription(lr.description());
                a.getLines().add(line);
            }
        }
    }

    @Override
    protected AssortmentPlanDtos.Response toResponse(AssortmentPlan a) {
        List<AssortmentPlanDtos.LineResponse> lines = new ArrayList<>();
        for (AssortmentPlanLine l : a.getLines()) {
            lines.add(new AssortmentPlanDtos.LineResponse(
                l.getId(), l.getProductId(), l.getProductCode(), l.getDescription()));
        }
        return new AssortmentPlanDtos.Response(
            a.getId(), a.getName(), a.getClusterId(), a.getCompanyId(), a.getEffectiveFrom(),
            a.getStatus(), a.getLines().size(), lines);
    }
}
