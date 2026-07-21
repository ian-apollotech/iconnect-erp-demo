// src/main/java/com/puregold/iconnect/product/MerchNodeService.java
package com.puregold.iconnect.product;

import com.puregold.iconnect.common.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class MerchNodeService extends AbstractCrudService<MerchNode> {

    private final MerchNodeRepository nodes;

    public MerchNodeService(MerchNodeRepository nodes) {
        super(nodes, "MerchNode");
        this.nodes = nodes;
    }

    @Override
    protected void validate(MerchNode n) {
        nodes.findByCode(n.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(n.getId()))
                throw new IllegalArgumentException("Node code already exists: " + n.getCode());
        });
        if (n.getParentId() != null && nodes.findById(n.getParentId()).isEmpty())
            throw new IllegalArgumentException("Parent node not found: " + n.getParentId());
    }

    @Override
    public MerchNode create(MerchNode entity) {
        MerchNode saved = super.create(entity);
        if ("Class (L3)".equals(saved.getLevel())) {
            for (String sc : new String[]{"101", "102", "103"}) {
                MerchNode child = new MerchNode();
                child.setLevel("Sub-Class (L4)");
                child.setParentId(saved.getId());
                child.setCode(saved.getCode() + "-" + sc);
                child.setName(sc);
                child.setStatus("Active");
                nodes.save(child);
            }
        }
        return saved;
    }
}
