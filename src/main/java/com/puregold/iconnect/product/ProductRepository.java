// src/main/java/com/puregold/iconnect/product/ProductRepository.java
package com.puregold.iconnect.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Optional<Product> findByCode(String code);
    long countByBrand(String brand);
}
