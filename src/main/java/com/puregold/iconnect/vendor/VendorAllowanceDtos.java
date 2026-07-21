// src/main/java/com/puregold/iconnect/vendor/VendorAllowanceDtos.java
package com.puregold.iconnect.vendor;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public final class VendorAllowanceDtos {
    private VendorAllowanceDtos() {}

    public record Request(
        String ref,
        @NotNull Long vendorId,
        String allowanceType,
        String basis,
        String rateAmount,
        LocalDate validFrom,
        LocalDate validTo,
        String status
    ) {}

    public record Response(
        Long id, String ref, Long vendorId, String allowanceType, String basis, String rateAmount,
        LocalDate validFrom, LocalDate validTo, String status
    ) {}
}
