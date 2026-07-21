// src/main/java/com/puregold/iconnect/company/CompanyService.java
package com.puregold.iconnect.company;

import com.puregold.iconnect.common.AbstractCrudService;
import org.springframework.stereotype.Service;

@Service
public class CompanyService extends AbstractCrudService<Company> {

    private final CompanyRepository companies;

    public CompanyService(CompanyRepository companies) {
        super(companies, "Company");
        this.companies = companies;
    }

    @Override
    protected void validate(Company entity) {
        companies.findByCode(entity.getCode()).ifPresent(existing -> {
            if (!existing.getId().equals(entity.getId())) {
                throw new IllegalArgumentException("Company code already exists: " + entity.getCode());
            }
        });
    }
}
