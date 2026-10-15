// src/main/java/com/puregold/iconnect/product/MerchNodeController.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/backend/merch-nodes")
public class MerchNodeController
        extends AbstractCrudController<MerchNode, MerchNodeDtos.Request, MerchNodeDtos.Response> {

    private final MerchNodeRepository nodes;

    public MerchNodeController(MerchNodeService service, MerchNodeRepository nodes) {
        super(service);
        this.nodes = nodes;
    }

    // Inherited zero-arg list() (mapped to unconditioned GET /backend/merch-nodes) handles the
    // no-filter case. These two params-conditioned mappings take precedence when the
    // corresponding query param is present, giving parentId priority over level — matching
    // "if parentId!=null filter findByParentId, else if level!=null filter findByLevel, else findAll".
    @GetMapping(params = "parentId")
    public List<MerchNodeDtos.Response> listByParent(@RequestParam Long parentId) {
        return nodes.findByParentId(parentId).stream().map(this::toResponse).toList();
    }

    @GetMapping(params = {"level", "!parentId"})
    public List<MerchNodeDtos.Response> listByLevel(@RequestParam String level) {
        return nodes.findByLevel(level).stream().map(this::toResponse).toList();
    }

    @Override
    protected MerchNode toEntity(MerchNodeDtos.Request req) {
        MerchNode n = new MerchNode();
        apply(req, n);
        return n;
    }

    @Override
    protected void apply(MerchNodeDtos.Request req, MerchNode n) {
        n.setLevel(req.level());
        n.setParentId(req.parentId());
        n.setCode(req.code());
        n.setName(req.name());
        n.setMerchManager(req.merchManager());
        n.setReplenStrategy(req.replenStrategy());
        n.setPerishable(req.perishable());
        n.setShelfLife(req.shelfLife());
        n.setOtbPlanned(req.otbPlanned());
        if (req.status() != null) n.setStatus(req.status());
    }

    @Override
    protected MerchNodeDtos.Response toResponse(MerchNode n) {
        return new MerchNodeDtos.Response(
            n.getId(), n.getLevel(), n.getParentId(), n.getCode(), n.getName(),
            n.getMerchManager(), n.getReplenStrategy(), n.getPerishable(),
            n.getShelfLife(), n.getOtbPlanned(), n.getStatus());
    }
}
