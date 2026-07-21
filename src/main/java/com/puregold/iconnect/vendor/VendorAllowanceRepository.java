// src/main/java/com/puregold/iconnect/vendor/VendorAllowanceRepository.java
package com.puregold.iconnect.vendor;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VendorAllowanceRepository extends JpaRepository<VendorAllowance, Long> {
    Optional<VendorAllowance> findByRef(String ref);
}
