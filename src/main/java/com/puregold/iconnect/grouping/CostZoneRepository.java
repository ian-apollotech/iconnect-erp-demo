// src/main/java/com/puregold/iconnect/grouping/CostZoneRepository.java
package com.puregold.iconnect.grouping;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CostZoneRepository extends JpaRepository<CostZone, Long> {
    Optional<CostZone> findByCode(String code);
}
