// src/test/java/com/puregold/iconnect/seed/GenericSeedTest.java
package com.puregold.iconnect.seed;

import com.puregold.iconnect.vendor.VendorRepository;
import com.puregold.iconnect.branch.BranchRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class GenericSeedTest {
    @Autowired VendorRepository vendors;
    @Autowired BranchRepository branches;

    @Test
    void seedsVendorsAndBranches() {
        assertThat(vendors.count()).isGreaterThanOrEqualTo(7); // 7 vendors in mockup PAGES.vendor
        assertThat(branches.count()).isGreaterThanOrEqualTo(10); // BR const has 10 stores
    }
}
