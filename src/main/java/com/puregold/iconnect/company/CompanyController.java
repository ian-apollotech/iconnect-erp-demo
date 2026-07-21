// src/main/java/com/puregold/iconnect/company/CompanyController.java
package com.puregold.iconnect.company;

import com.puregold.iconnect.common.AbstractCrudController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController
        extends AbstractCrudController<Company, CompanyDtos.Request, CompanyDtos.Response> {

    public CompanyController(CompanyService service) { super(service); }

    @Override
    protected Company toEntity(CompanyDtos.Request req) {
        Company c = new Company();
        apply(req, c);
        return c;
    }

    @Override
    protected void apply(CompanyDtos.Request req, Company c) {
        c.setCode(req.code());
        c.setLegalName(req.legalName());
        c.setTradeName(req.tradeName());
        c.setTin(req.tin());
        c.setFiscalYearStart(req.fiscalYearStart());
        c.setBaseCurrency(req.baseCurrency());
        c.setDefaultVat(req.defaultVat());
        c.setPriceIncludesVat(req.priceIncludesVat());
        c.setRegisteredAddress(req.registeredAddress());
        c.setContactPerson(req.contactPerson());
        if (req.status() != null) c.setStatus(req.status());
    }

    @Override
    protected CompanyDtos.Response toResponse(Company c) {
        return new CompanyDtos.Response(
            c.getId(), c.getCode(), c.getLegalName(), c.getTradeName(), c.getTin(),
            c.getFiscalYearStart(), c.getBaseCurrency(), c.getDefaultVat(),
            c.getPriceIncludesVat(), c.getRegisteredAddress(), c.getContactPerson(),
            c.getStatus(), 0L /* branchCount filled in Task 6a */);
    }
}
