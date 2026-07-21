// src/main/java/com/puregold/iconnect/vendor/VendorScheduleService.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.AbstractCrudService;
import com.puregold.iconnect.company.CompanyRepository;
import org.springframework.stereotype.Service;

@Service
public class VendorScheduleService extends AbstractCrudService<VendorSchedule> {

    private final CompanyRepository companies;
    private final VendorRepository vendors;

    public VendorScheduleService(VendorScheduleRepository schedules, CompanyRepository companies,
                                  VendorRepository vendors) {
        super(schedules, "VendorSchedule");
        this.companies = companies;
        this.vendors = vendors;
    }

    @Override
    protected void validate(VendorSchedule entity) {
        if (entity.getCompanyId() != null && companies.findById(entity.getCompanyId()).isEmpty()) {
            throw new IllegalArgumentException("Company not found: " + entity.getCompanyId());
        }
        if (entity.getVendorId() != null && vendors.findById(entity.getVendorId()).isEmpty()) {
            throw new IllegalArgumentException("Vendor not found: " + entity.getVendorId());
        }
    }
}
