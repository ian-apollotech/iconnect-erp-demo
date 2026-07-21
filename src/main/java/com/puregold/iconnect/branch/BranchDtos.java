// src/main/java/com/puregold/iconnect/branch/BranchDtos.java
package com.puregold.iconnect.branch;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public final class BranchDtos {
    private BranchDtos() {}

    public record Request(
        @NotBlank String code,
        @NotBlank String name,
        String format,
        String region,
        @NotNull Long companyId,
        String inventoryLocations,
        String crossDockHub,
        LocalDate openingDate,
        Long priceZoneId,
        Long costZoneId,
        Long assortmentClusterId,
        Long replenishmentProfileId,
        String street,
        String city,
        String province,
        String status
    ) {}

    public record Response(
        Long id, String code, String name, String format, String region, Long companyId,
        String inventoryLocations, String crossDockHub, LocalDate openingDate,
        Long priceZoneId, Long costZoneId, Long assortmentClusterId, Long replenishmentProfileId,
        String street, String city, String province, String status
    ) {}
}
