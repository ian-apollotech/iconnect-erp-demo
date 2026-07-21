// src/main/java/com/puregold/iconnect/company/CompanyDtos.java
package com.puregold.iconnect.company;

import jakarta.validation.constraints.NotBlank;

public final class CompanyDtos {
    private CompanyDtos() {}

    public record Request(
        @NotBlank String code,
        @NotBlank String legalName,
        String tradeName,
        String tin,
        String fiscalYearStart,
        String baseCurrency,
        String defaultVat,
        String priceIncludesVat,
        String registeredAddress,
        String contactPerson,
        String status
    ) {}

    public record Response(
        Long id, String code, String legalName, String tradeName, String tin,
        String fiscalYearStart, String baseCurrency, String defaultVat,
        String priceIncludesVat, String registeredAddress, String contactPerson,
        String status, long branchCount
    ) {}
}
