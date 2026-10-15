// src/main/java/com/puregold/iconnect/replenishment/ReplenishmentPolicyDtos.java
package com.puregold.iconnect.replenishment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class ReplenishmentPolicyDtos {
    private ReplenishmentPolicyDtos() {}

    public record Request(
        @NotNull Long profileId,
        @NotBlank String itemScopeType,
        String scopeRef,
        @NotBlank String method,
        String target,
        Integer safetyStockDays,
        String serviceLevel,
        String status
    ) {}

    public record Response(
        Long id, Long profileId, String profileName, String itemScopeType, String scopeRef,
        String method, String target, Integer safetyStockDays, String serviceLevel, String status
    ) {}
}
