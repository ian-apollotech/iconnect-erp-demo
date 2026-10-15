// src/main/java/com/puregold/iconnect/replenishment/OrderCalendarExceptionDtos.java
package com.puregold.iconnect.replenishment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public final class OrderCalendarExceptionDtos {
    private OrderCalendarExceptionDtos() {}

    public record Request(
        @NotBlank String scope,
        @NotNull Long companyId,
        Long vendorId,
        LocalDate date,
        @NotBlank String type,
        String label,
        String status
    ) {}

    public record Response(
        Long id, String scope, Long companyId, String companyCode, Long vendorId, String vendorName,
        LocalDate date, String type, String label, String status
    ) {}
}
