// src/main/java/com/puregold/iconnect/product/BrandRepository.java
package com.puregold.iconnect.product;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    Optional<Brand> findByCode(String code);
}
