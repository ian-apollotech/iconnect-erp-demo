// src/main/java/com/puregold/iconnect/product/BrandDtos.java
package com.puregold.iconnect.product;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public final class BrandDtos {
    private BrandDtos() {}

    public record Request(
        @NotBlank String name,
        String code,
        @NotNull Long manufacturerId,
        String status,
        String description
    ) {}

    public record Response(
        Long id, String name, String code, Long manufacturerId, String manufacturerName,
        String manufacturerCode, long productCount, String status, String description
    ) {}
}
