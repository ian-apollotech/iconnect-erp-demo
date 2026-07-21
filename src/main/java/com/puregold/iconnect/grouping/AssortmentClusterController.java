// src/main/java/com/puregold/iconnect/grouping/AssortmentClusterController.java
package com.puregold.iconnect.grouping;

import com.puregold.iconnect.branch.BranchRepository;
import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/assortment-clusters")
public class AssortmentClusterController
        extends AbstractCrudController<AssortmentCluster, AssortmentClusterDtos.Request, AssortmentClusterDtos.Response> {

    private final BranchRepository branchRepository;

    public AssortmentClusterController(AssortmentClusterService service, BranchRepository branchRepository) {
        super(service);
        this.branchRepository = branchRepository;
    }

    @Override
    protected AssortmentCluster toEntity(AssortmentClusterDtos.Request req) {
        AssortmentCluster a = new AssortmentCluster();
        apply(req, a);
        return a;
    }

    @Override
    protected void apply(AssortmentClusterDtos.Request req, AssortmentCluster a) {
        a.setName(req.name());
        a.setCode(req.code());
        a.setCompanyId(req.companyId());
        if (req.status() != null) a.setStatus(req.status());
        a.setDescription(req.description());
        a.setStoreProfile(req.storeProfile());
        a.setTradeArea(req.tradeArea());
    }

    @Override
    protected AssortmentClusterDtos.Response toResponse(AssortmentCluster a) {
        return new AssortmentClusterDtos.Response(
            a.getId(), a.getName(), a.getCode(), a.getCompanyId(), a.getStatus(), a.getDescription(),
            a.getStoreProfile(), a.getTradeArea(),
            branchRepository.countByAssortmentClusterId(a.getId()));
    }
}
