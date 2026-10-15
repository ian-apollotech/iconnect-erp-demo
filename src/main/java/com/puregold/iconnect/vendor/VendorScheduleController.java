// src/main/java/com/puregold/iconnect/vendor/VendorScheduleController.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/vendor-schedules")
public class VendorScheduleController
        extends AbstractCrudController<VendorSchedule, VendorScheduleDtos.Request, VendorScheduleDtos.Response> {

    public VendorScheduleController(VendorScheduleService service) { super(service); }

    @Override
    protected VendorSchedule toEntity(VendorScheduleDtos.Request req) {
        VendorSchedule s = new VendorSchedule();
        apply(req, s);
        return s;
    }

    @Override
    protected void apply(VendorScheduleDtos.Request req, VendorSchedule s) {
        s.setCompanyId(req.companyId());
        s.setVendorId(req.vendorId());
        s.setOrderDays(req.orderDays());
        s.setLeadTimeDays(req.leadTimeDays());
        s.setDeliveryDays(req.deliveryDays());
        if (req.status() != null) s.setStatus(req.status());
    }

    @Override
    protected VendorScheduleDtos.Response toResponse(VendorSchedule s) {
        return new VendorScheduleDtos.Response(
            s.getId(), s.getCompanyId(), s.getVendorId(), s.getOrderDays(), s.getLeadTimeDays(),
            s.getDeliveryDays(), s.getStatus());
    }
}
