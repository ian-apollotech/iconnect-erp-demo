// src/main/java/com/puregold/iconnect/grouping/ReplenishmentProfileRepository.java
package com.puregold.iconnect.grouping;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface ReplenishmentProfileRepository extends JpaRepository<ReplenishmentProfile, Long> {
    Optional<ReplenishmentProfile> findByCode(String code);
}
