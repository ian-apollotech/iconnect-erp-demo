// src/main/java/com/puregold/iconnect/grouping/CostZoneDtos.java
package com.puregold.iconnect.grouping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class CostZoneDtos {
    private CostZoneDtos() {}

    public record Request(
        @NotBlank String name,
        String code,
        @NotNull Long companyId,
        String status,
        String description
    ) {}

    public record Response(
        Long id, String name, String code, Long companyId, String status, String description,
        long storeCount
    ) {}
}
