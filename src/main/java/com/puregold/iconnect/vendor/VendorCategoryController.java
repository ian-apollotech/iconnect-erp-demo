// src/main/java/com/puregold/iconnect/vendor/VendorCategoryController.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/vendor-categories")
public class VendorCategoryController
        extends AbstractCrudController<VendorCategory, VendorCategoryDtos.Request, VendorCategoryDtos.Response> {

    public VendorCategoryController(VendorCategoryService service) { super(service); }

    @Override
    protected VendorCategory toEntity(VendorCategoryDtos.Request req) {
        VendorCategory c = new VendorCategory();
        apply(req, c);
        return c;
    }

    @Override
    protected void apply(VendorCategoryDtos.Request req, VendorCategory c) {
        c.setVendorId(req.vendorId());
        c.setLevel(req.level());
        c.setParentCategoryId(req.parentCategoryId());
        c.setNodeName(req.nodeName());
        c.setNodeCode(req.nodeCode());
        c.setAllowanceEligible(req.allowanceEligible());
        if (req.status() != null) c.setStatus(req.status());
    }

    @Override
    protected VendorCategoryDtos.Response toResponse(VendorCategory c) {
        return new VendorCategoryDtos.Response(
            c.getId(), c.getVendorId(), c.getLevel(), c.getParentCategoryId(), c.getNodeName(),
            c.getNodeCode(), c.getAllowanceEligible(), c.getStatus());
    }
}
