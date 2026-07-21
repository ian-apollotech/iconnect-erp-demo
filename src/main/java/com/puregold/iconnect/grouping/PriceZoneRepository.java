// src/main/java/com/puregold/iconnect/grouping/PriceZoneRepository.java
package com.puregold.iconnect.grouping;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface PriceZoneRepository extends JpaRepository<PriceZone, Long> {
    Optional<PriceZone> findByCode(String code);
}
