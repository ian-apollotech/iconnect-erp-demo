// src/main/java/com/puregold/iconnect/company/CompanyRepository.java
package com.puregold.iconnect.company;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCode(String code);
}
