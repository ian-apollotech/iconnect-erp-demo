// src/main/java/com/puregold/iconnect/vendor/VendorController.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/vendors")
public class VendorController
        extends AbstractCrudController<Vendor, VendorDtos.Request, VendorDtos.Response> {

    public VendorController(VendorService service) { super(service); }

    @Override
    protected Vendor toEntity(VendorDtos.Request req) {
        Vendor v = new Vendor();
        apply(req, v);
        return v;
    }

    @Override
    protected void apply(VendorDtos.Request req, Vendor v) {
        v.setCode(req.code());
        v.setName(req.name());
        v.setTin(req.tin());
        v.setDeliveryModel(req.deliveryModel());
        v.setPaymentTerms(req.paymentTerms());
        v.setLeadTime(req.leadTime());
        v.setMinOrder(req.minOrder());
        v.setRtvAllowed(req.rtvAllowed());
        v.setRegisteredAddress(req.registeredAddress());
        v.setContactPerson(req.contactPerson());
        v.setPhone(req.phone());
        v.setEmail(req.email());
        if (req.status() != null) v.setStatus(req.status());
        v.setVendorType(req.vendorType() != null ? req.vendorType() : "Manufacturer & Distributor");
    }

    @Override
    protected VendorDtos.Response toResponse(Vendor v) {
        return new VendorDtos.Response(
            v.getId(), v.getCode(), v.getName(), v.getTin(), v.getDeliveryModel(),
            v.getPaymentTerms(), v.getLeadTime(), v.getMinOrder(), v.getRtvAllowed(),
            v.getRegisteredAddress(), v.getContactPerson(), v.getPhone(), v.getEmail(), v.getStatus(),
            v.getVendorType());
    }
}
