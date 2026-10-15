// src/main/java/com/puregold/iconnect/product/ProductAttributeDtos.java
package com.puregold.iconnect.product;

import jakarta.validation.constraints.NotBlank;

public final class ProductAttributeDtos {
    private ProductAttributeDtos() {}

    public record Request(
        @NotBlank String name,
        String code,
        @NotBlank String allowedValues,
        String status,
        String description
    ) {}

    public record Response(
        Long id, String name, String code, String allowedValues, String status, String description
    ) {}
}
