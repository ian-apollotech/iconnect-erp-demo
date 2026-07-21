// src/test/java/com/puregold/iconnect/vendor/VendorProductServiceTest.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.company.Company;
import com.puregold.iconnect.company.CompanyRepository;
import com.puregold.iconnect.product.Product;
import com.puregold.iconnect.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class VendorProductServiceTest {
    @Autowired VendorProductService service;
    @Autowired VendorProductRepository repo;
    @Autowired VendorRepository vendors;
    @Autowired CompanyRepository companies;
    @Autowired ProductRepository products;

    Long companyId, v1, v2, productId;

    @BeforeEach
    void setup() {
        Company c = new Company(); c.setCode("VPTEST"+System.nanoTime()); c.setLegalName("VP Test");
        companyId = companies.save(c).getId();
        Vendor a = new Vendor(); a.setName("VP Vendor A"); a.setRtvAllowed("Yes"); a.setCode("VPA"+System.nanoTime());
        Vendor b = new Vendor(); b.setName("VP Vendor B"); b.setRtvAllowed("Yes"); b.setCode("VPB"+System.nanoTime());
        v1 = vendors.save(a).getId(); v2 = vendors.save(b).getId();
        Product p = new Product(); p.setDescription("VP Product"); p.setCode("VPP"+System.nanoTime());
        productId = products.save(p).getId();
    }

    @Test
    void costPerCaseIsIgnoredFromClient() {
        VendorProduct link = newLink("Primary");
        link.setCostPerCase(new BigDecimal("999.99"));
        VendorProduct saved = service.createFromClient(link);
        assertThat(saved.getCostPerCase()).isNull();
    }

    @Test
    void settingSecondPrimaryDemotesFirst() {
        VendorProduct first = service.createFromClient(withVendor(newLink("Primary"), v1));
        VendorProduct second = service.createFromClient(withVendor(newLink("Primary"), v2));
        assertThat(repo.findById(first.getId()).get().getSourcePriority()).isEqualTo("Alternate");
        assertThat(repo.findByProductIdAndSourcePriority(productId, "Primary")).get()
            .extracting(VendorProduct::getId).isEqualTo(second.getId());
    }

    private VendorProduct newLink(String priority) {
        VendorProduct v = new VendorProduct();
        v.setCompanyId(companyId); v.setProductId(productId); v.setVendorId(v1);
        v.setSourcePriority(priority); v.setStatus("Active");
        return v;
    }
    private VendorProduct withVendor(VendorProduct v, Long vendorId){ v.setVendorId(vendorId); return v; }
}
