// src/main/java/com/puregold/iconnect/vendor/VendorAllowanceController.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vendor-allowances")
public class VendorAllowanceController
        extends AbstractCrudController<VendorAllowance, VendorAllowanceDtos.Request, VendorAllowanceDtos.Response> {

    public VendorAllowanceController(VendorAllowanceService service) { super(service); }

    @Override
    protected VendorAllowance toEntity(VendorAllowanceDtos.Request req) {
        VendorAllowance a = new VendorAllowance();
        apply(req, a);
        return a;
    }

    @Override
    protected void apply(VendorAllowanceDtos.Request req, VendorAllowance a) {
        a.setRef(req.ref());
        a.setVendorId(req.vendorId());
        a.setAllowanceType(req.allowanceType());
        a.setBasis(req.basis());
        a.setRateAmount(req.rateAmount());
        a.setValidFrom(req.validFrom());
        a.setValidTo(req.validTo());
        if (req.status() != null) a.setStatus(req.status());
    }

    @Override
    protected VendorAllowanceDtos.Response toResponse(VendorAllowance a) {
        return new VendorAllowanceDtos.Response(
            a.getId(), a.getRef(), a.getVendorId(), a.getAllowanceType(), a.getBasis(), a.getRateAmount(),
            a.getValidFrom(), a.getValidTo(), a.getStatus());
    }
}
