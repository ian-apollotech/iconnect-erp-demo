// src/main/java/com/puregold/iconnect/grouping/AssortmentClusterRepository.java
package com.puregold.iconnect.grouping;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface AssortmentClusterRepository extends JpaRepository<AssortmentCluster, Long> {
    Optional<AssortmentCluster> findByCode(String code);
}
