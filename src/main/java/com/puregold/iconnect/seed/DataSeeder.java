// src/main/java/com/puregold/iconnect/seed/DataSeeder.java
package com.puregold.iconnect.seed;

import com.puregold.iconnect.company.Company;
import com.puregold.iconnect.company.CompanyRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final CompanyRepository companies;

    public DataSeeder(CompanyRepository companies) {
        this.companies = companies;
    }

    @Override
    public void run(String... args) {
        seedCompanies();
    }

    private void seedCompanies() {
        if (companies.count() > 0) return;
        companies.save(company("PGOLD", "Puregold Price Club, Inc.", "Puregold", "000-123-456-000"));
        companies.save(company("AYAG", "Ayagold Retailers Inc.", "Merkado", "000-987-654-000"));
    }

    private Company company(String code, String legalName, String trade, String tin) {
        Company c = new Company();
        c.setCode(code);
        c.setLegalName(legalName);
        c.setTradeName(trade);
        c.setTin(tin);
        c.setFiscalYearStart("January");
        c.setBaseCurrency("PHP");
        c.setDefaultVat("12% VATable");
        c.setPriceIncludesVat("Yes (VAT-inclusive)");
        c.setStatus("Active");
        return c;
    }
}
