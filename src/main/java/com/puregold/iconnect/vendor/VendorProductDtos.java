// src/main/java/com/puregold/iconnect/vendor/VendorProductDtos.java
package com.puregold.iconnect.vendor;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public final class VendorProductDtos {
    private VendorProductDtos() {}

    public record Request(
        @NotNull Long companyId,
        @NotNull Long vendorId,
        @NotNull Long productId,
        String vendorItemNo,
        Long vendorCategoryId,
        String sourcePriority,
        String orderPackUom,
        Integer moqCases,
        Integer orderIncrementCases,
        String orderSchedule,
        String leadTime,
        BigDecimal costPerCase,     // accepted but ignored — cost is CCM-owned; see VendorProductService.createFromClient
        Long allowanceId,
        String status
    ) {}

    public record Response(
        Long id, Long companyId, Long vendorId, Long productId, String vendorItemNo,
        Long vendorCategoryId, String sourcePriority, String orderPackUom, Integer moqCases,
        Integer orderIncrementCases, String orderSchedule, String leadTime, BigDecimal costPerCase,
        Long allowanceId, String status
    ) {}
}
