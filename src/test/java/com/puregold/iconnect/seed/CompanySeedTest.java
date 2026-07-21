// src/test/java/com/puregold/iconnect/seed/CompanySeedTest.java
package com.puregold.iconnect.seed;

import com.puregold.iconnect.company.CompanyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class CompanySeedTest {
    @Autowired CompanyRepository companies;

    @Test
    void seedsPuregoldCompanies() {
        assertThat(companies.findByCode("PGOLD")).isPresent();
        assertThat(companies.findByCode("AYAG")).isPresent();
    }
}
