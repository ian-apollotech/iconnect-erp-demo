// src/main/java/com/puregold/iconnect/vendor/VendorCategoryDtos.java
package com.puregold.iconnect.vendor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class VendorCategoryDtos {
    private VendorCategoryDtos() {}

    public record Request(
        @NotNull Long vendorId,
        String level,
        Long parentCategoryId,
        @NotBlank String nodeName,
        String nodeCode,
        String allowanceEligible,
        String status
    ) {}

    public record Response(
        Long id, Long vendorId, String level, Long parentCategoryId, String nodeName,
        String nodeCode, String allowanceEligible, String status
    ) {}
}
