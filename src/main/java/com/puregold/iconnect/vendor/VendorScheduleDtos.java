// src/main/java/com/puregold/iconnect/vendor/VendorScheduleDtos.java
package com.puregold.iconnect.vendor;

import jakarta.validation.constraints.NotNull;

public final class VendorScheduleDtos {
    private VendorScheduleDtos() {}

    public record Request(
        @NotNull Long companyId,
        @NotNull Long vendorId,
        String orderDays,
        Integer leadTimeDays,
        String deliveryDays,
        String status
    ) {}

    public record Response(
        Long id, Long companyId, Long vendorId, String orderDays, Integer leadTimeDays,
        String deliveryDays, String status
    ) {}
}
