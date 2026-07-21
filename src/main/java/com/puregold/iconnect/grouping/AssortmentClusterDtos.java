// src/main/java/com/puregold/iconnect/grouping/AssortmentClusterDtos.java
package com.puregold.iconnect.grouping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class AssortmentClusterDtos {
    private AssortmentClusterDtos() {}

    public record Request(
        @NotBlank String name,
        String code,
        @NotNull Long companyId,
        String status,
        String description,
        String storeProfile,
        String tradeArea
    ) {}

    public record Response(
        Long id, String name, String code, Long companyId, String status, String description,
        String storeProfile, String tradeArea, long storeCount
    ) {}
}
