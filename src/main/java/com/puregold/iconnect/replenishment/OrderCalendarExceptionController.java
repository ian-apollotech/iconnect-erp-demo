// src/main/java/com/puregold/iconnect/replenishment/OrderCalendarExceptionController.java
package com.puregold.iconnect.replenishment;

import com.puregold.iconnect.common.AbstractCrudController;
import com.puregold.iconnect.company.CompanyRepository;
import com.puregold.iconnect.vendor.VendorRepository;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/backend/order-calendar-exceptions")
public class OrderCalendarExceptionController
        extends AbstractCrudController<OrderCalendarException, OrderCalendarExceptionDtos.Request, OrderCalendarExceptionDtos.Response> {

    private final CompanyRepository companies;
    private final VendorRepository vendors;

    public OrderCalendarExceptionController(OrderCalendarExceptionService service,
                                             CompanyRepository companies, VendorRepository vendors) {
        super(service);
        this.companies = companies;
        this.vendors = vendors;
    }

    @Override
    protected OrderCalendarException toEntity(OrderCalendarExceptionDtos.Request req) {
        OrderCalendarException e = new OrderCalendarException();
        apply(req, e);
        return e;
    }

    @Override
    protected void apply(OrderCalendarExceptionDtos.Request req, OrderCalendarException e) {
        e.setScope(req.scope());
        e.setCompanyId(req.companyId());
        e.setVendorId(req.vendorId());
        e.setDate(req.date());
        e.setType(req.type());
        e.setLabel(req.label());
        if (req.status() != null) e.setStatus(req.status());
    }

    @Override
    protected OrderCalendarExceptionDtos.Response toResponse(OrderCalendarException e) {
        String companyCode = companies.findById(e.getCompanyId()).map(c -> c.getCode()).orElse(null);
        String vendorName = e.getVendorId() == null
            ? null
            : vendors.findById(e.getVendorId()).map(v -> v.getName()).orElse(null);
        return new OrderCalendarExceptionDtos.Response(
            e.getId(), e.getScope(), e.getCompanyId(), companyCode, e.getVendorId(), vendorName,
            e.getDate(), e.getType(), e.getLabel(), e.getStatus());
    }
}
