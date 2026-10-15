// src/test/java/com/puregold/iconnect/seed/ReplenishmentSeedTest.java
package com.puregold.iconnect.seed;

import com.puregold.iconnect.replenishment.OrderCalendarExceptionRepository;
import com.puregold.iconnect.replenishment.ReplenishmentPolicyRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ReplenishmentSeedTest {
    @Autowired ReplenishmentPolicyRepository replenishmentPolicies;
    @Autowired OrderCalendarExceptionRepository orderCalendarExceptions;

    @Test
    void seedsReplenishmentPoliciesAndOrderCalendarExceptions() {
        assertThat(replenishmentPolicies.count()).isGreaterThanOrEqualTo(5);
        assertThat(orderCalendarExceptions.count()).isGreaterThanOrEqualTo(3);
    }
}
