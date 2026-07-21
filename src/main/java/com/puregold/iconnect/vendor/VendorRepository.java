// src/main/java/com/puregold/iconnect/vendor/VendorRepository.java
package com.puregold.iconnect.vendor;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VendorRepository extends JpaRepository<Vendor, Long> {
    Optional<Vendor> findByCode(String code);
}
