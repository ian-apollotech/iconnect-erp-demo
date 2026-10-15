// src/main/java/com/puregold/iconnect/product/ProductAttributeRepository.java
package com.puregold.iconnect.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ProductAttributeRepository extends JpaRepository<ProductAttribute, Long> {
    Optional<ProductAttribute> findByCode(String code);
}
