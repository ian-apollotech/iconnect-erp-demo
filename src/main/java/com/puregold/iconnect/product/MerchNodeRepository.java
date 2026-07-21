// src/main/java/com/puregold/iconnect/product/MerchNodeRepository.java
package com.puregold.iconnect.product;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MerchNodeRepository extends JpaRepository<MerchNode, Long> {
    List<MerchNode> findByParentId(Long parentId);
    List<MerchNode> findByLevel(String level);
    Optional<MerchNode> findByCode(String code);
}
