// src/main/java/com/puregold/iconnect/vendor/VendorDtos.java
package com.puregold.iconnect.vendor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class VendorDtos {
    private VendorDtos() {}

    public record Request(
        String code,
        @NotBlank String name,
        String tin,
        String deliveryModel,
        String paymentTerms,
        String leadTime,
        BigDecimal minOrder,
        @NotNull String rtvAllowed,
        String registeredAddress,
        String contactPerson,
        String phone,
        String email,
        String status,
        String vendorType
    ) {}

    public record Response(
        Long id, String code, String name, String tin, String deliveryModel,
        String paymentTerms, String leadTime, BigDecimal minOrder, String rtvAllowed,
        String registeredAddress, String contactPerson, String phone, String email, String status,
        String vendorType
    ) {}
}
