// src/main/java/com/puregold/iconnect/vendor/VendorProductRepository.java
package com.puregold.iconnect.vendor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VendorProductRepository extends JpaRepository<VendorProduct, Long> {
    List<VendorProduct> findByProductId(Long productId);
    Optional<VendorProduct> findByProductIdAndSourcePriority(Long productId, String sourcePriority);
}
