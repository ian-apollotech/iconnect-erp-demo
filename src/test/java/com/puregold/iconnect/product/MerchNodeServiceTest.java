// src/test/java/com/puregold/iconnect/product/MerchNodeServiceTest.java
package com.puregold.iconnect.product;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class MerchNodeServiceTest {
    @Autowired MerchNodeService service;
    @Autowired MerchNodeRepository repo;

    @Test
    void creatingClassScaffoldsThreeSubclasses() {
        MerchNode cls = new MerchNode();
        cls.setLevel("Class (L3)");
        cls.setCode("9999-99");
        cls.setName("Test Class");
        MerchNode saved = service.create(cls);

        var kids = repo.findByParentId(saved.getId());
        assertThat(kids).hasSize(3);
        assertThat(kids).extracting(MerchNode::getCode)
            .containsExactlyInAnyOrder("9999-99-101", "9999-99-102", "9999-99-103");
        assertThat(kids).allMatch(k -> "Sub-Class (L4)".equals(k.getLevel()));
    }
}
