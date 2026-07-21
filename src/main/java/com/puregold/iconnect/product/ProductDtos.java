// src/main/java/com/puregold/iconnect/product/ProductDtos.java
package com.puregold.iconnect.product;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;
import java.util.List;

public final class ProductDtos {
    private ProductDtos() {}

    public record PackUomRequest(
        String level,
        String uom,
        Integer qtyInBase,
        String barcode,
        boolean orderable,
        boolean sellable
    ) {}

    public record PackUomResponse(
        Long id, String level, String uom, Integer qtyInBase,
        String barcode, boolean orderable, boolean sellable
    ) {}

    public record Request(
        String code,
        @NotBlank String description,
        String primaryBarcode,
        String shortName,
        String brand,
        Long merchNodeId,
        String perishable,
        String shelfLife,
        String vatClass,
        String listingFee,
        String storeRange,
        String buyingUom,
        String sellingUom,
        BigDecimal standardCost,
        BigDecimal baseRetail,
        String status,
        List<PackUomRequest> packUoms
    ) {}

    public record Response(
        Long id, String code, String description, String primaryBarcode, String shortName,
        String brand, Long merchNodeId, String perishable, String shelfLife, String vatClass,
        String listingFee, String storeRange, String buyingUom, String sellingUom,
        BigDecimal standardCost, BigDecimal baseRetail, BigDecimal marginPct,
        String preferredVendor, String status, List<PackUomResponse> packUoms
    ) {}
}
