// src/test/java/com/puregold/iconnect/seed/BrandAttributeSeedTest.java
package com.puregold.iconnect.seed;

import com.puregold.iconnect.product.BrandRepository;
import com.puregold.iconnect.product.ProductAttributeRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class BrandAttributeSeedTest {
    @Autowired BrandRepository brands;
    @Autowired ProductAttributeRepository productAttributes;

    @Test
    void seedsBrandsAndProductAttributes() {
        assertThat(brands.count()).isGreaterThanOrEqualTo(7);
        assertThat(productAttributes.count()).isGreaterThanOrEqualTo(6);
    }
}
