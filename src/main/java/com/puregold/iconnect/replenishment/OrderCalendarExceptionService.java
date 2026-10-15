// src/main/java/com/puregold/iconnect/replenishment/OrderCalendarExceptionService.java
package com.puregold.iconnect.replenishment;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import com.puregold.iconnect.vendor.VendorRepository;
import org.springframework.stereotype.Service;

@Service
public class OrderCalendarExceptionService extends AbstractCrudService<OrderCalendarException> {

    private final CompanyRepository companies;
    private final VendorRepository vendors;

    public OrderCalendarExceptionService(OrderCalendarExceptionRepository orderCalendarExceptions,
                                          CompanyRepository companies, VendorRepository vendors) {
        super(orderCalendarExceptions, "OrderCalendarException");
        this.companies = companies;
        this.vendors = vendors;
    }

    @Override
    protected void validate(OrderCalendarException entity) {
        if (entity.getCompanyId() == null || companies.findById(entity.getCompanyId()).isEmpty()) {
            throw new IllegalArgumentException("Company not found: " + entity.getCompanyId());
        }
        if ("Vendor Blackout".equals(entity.getScope()) && entity.getVendorId() == null) {
            throw new IllegalArgumentException("Vendor is required for a Vendor Blackout");
        }
        if (entity.getVendorId() != null && vendors.findById(entity.getVendorId()).isEmpty()) {
            throw new IllegalArgumentException("Vendor not found: " + entity.getVendorId());
        }
    }
}
