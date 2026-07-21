// src/main/java/com/puregold/iconnect/vendor/VendorAllowance.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "vendor_allowance")
public class VendorAllowance extends BaseEntity {
    @Column(unique = true)
    private String ref;
    @Column(nullable = false)
    private Long vendorId;
    private String allowanceType;       // "Volume Rebate" | "Listing Fee" | "Promo Allowance" | "Display / Facing" | "Markdown Support"
    private String basis;               // "% of Purchase" | "Fixed Amount" | "Per Case"
    private String rateAmount;          // e.g. "3.50%" or "₱ 12.00" — kept as String, not BigDecimal
    private LocalDate validFrom;
    private LocalDate validTo;
    private String status = "Active";

    public String getRef() { return ref; }
    public void setRef(String ref) { this.ref = ref; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getAllowanceType() { return allowanceType; }
    public void setAllowanceType(String allowanceType) { this.allowanceType = allowanceType; }
    public String getBasis() { return basis; }
    public void setBasis(String basis) { this.basis = basis; }
    public String getRateAmount() { return rateAmount; }
    public void setRateAmount(String rateAmount) { this.rateAmount = rateAmount; }
    public LocalDate getValidFrom() { return validFrom; }
    public void setValidFrom(LocalDate validFrom) { this.validFrom = validFrom; }
    public LocalDate getValidTo() { return validTo; }
    public void setValidTo(LocalDate validTo) { this.validTo = validTo; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
