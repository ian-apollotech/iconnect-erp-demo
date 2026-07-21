// src/main/java/com/puregold/iconnect/branch/BranchRepository.java
package com.puregold.iconnect.branch;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface BranchRepository extends JpaRepository<Branch, Long> {
    Optional<Branch> findByCode(String code);
    long countByCompanyId(Long companyId);
    long countByPriceZoneId(Long priceZoneId);
    long countByCostZoneId(Long costZoneId);
    long countByAssortmentClusterId(Long assortmentClusterId);
    long countByReplenishmentProfileId(Long replenishmentProfileId);
}
