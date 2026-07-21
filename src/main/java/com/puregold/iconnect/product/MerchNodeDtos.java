// src/main/java/com/puregold/iconnect/product/MerchNodeDtos.java
package com.puregold.iconnect.product;

import jakarta.validation.constraints.NotBlank;

public final class MerchNodeDtos {
    private MerchNodeDtos() {}

    public record Request(
        String level,
        Long parentId,
        @NotBlank String code,
        @NotBlank String name,
        String merchManager,
        String replenStrategy,
        String perishable,
        String shelfLife,
        String otbPlanned,
        String status
    ) {}

    public record Response(
        Long id, String level, Long parentId, String code, String name,
        String merchManager, String replenStrategy, String perishable,
        String shelfLife, String otbPlanned, String status
    ) {}
}
