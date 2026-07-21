// src/main/java/com/puregold/iconnect/grouping/AssortmentPlanDtos.java
package com.puregold.iconnect.grouping;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public final class AssortmentPlanDtos {
    private AssortmentPlanDtos() {}

    public record LineRequest(
        Long productId,
        String productCode,
        String description
    ) {}

    public record LineResponse(
        Long id, Long productId, String productCode, String description
    ) {}

    public record Request(
        @NotBlank String name,
        @NotNull Long clusterId,
        Long companyId,
        LocalDate effectiveFrom,
        String status,
        List<LineRequest> lines
    ) {}

    public record Response(
        Long id, String name, Long clusterId, Long companyId, LocalDate effectiveFrom,
        String status, int skuCount, List<LineResponse> lines
    ) {}
}
