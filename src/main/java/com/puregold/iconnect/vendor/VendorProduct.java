// src/main/java/com/puregold/iconnect/vendor/VendorProduct.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "vendor_product")
public class VendorProduct extends BaseEntity {
    @Column(nullable = false)
    private Long companyId;
    @Column(nullable = false)
    private Long vendorId;
    @Column(nullable = false)
    private Long productId;
    private String vendorItemNo;
    private Long vendorCategoryId;
    private String sourcePriority;      // "Primary" | "Alternate"
    private String orderPackUom;
    private Integer moqCases;
    private Integer orderIncrementCases;
    private String orderSchedule;
    private String leadTime;
    private BigDecimal costPerCase;     // CCM-owned — read-only via API
    private Long allowanceId;
    private String status = "Active";

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }
    public String getVendorItemNo() { return vendorItemNo; }
    public void setVendorItemNo(String vendorItemNo) { this.vendorItemNo = vendorItemNo; }
    public Long getVendorCategoryId() { return vendorCategoryId; }
    public void setVendorCategoryId(Long vendorCategoryId) { this.vendorCategoryId = vendorCategoryId; }
    public String getSourcePriority() { return sourcePriority; }
    public void setSourcePriority(String sourcePriority) { this.sourcePriority = sourcePriority; }
    public String getOrderPackUom() { return orderPackUom; }
    public void setOrderPackUom(String orderPackUom) { this.orderPackUom = orderPackUom; }
    public Integer getMoqCases() { return moqCases; }
    public void setMoqCases(Integer moqCases) { this.moqCases = moqCases; }
    public Integer getOrderIncrementCases() { return orderIncrementCases; }
    public void setOrderIncrementCases(Integer orderIncrementCases) { this.orderIncrementCases = orderIncrementCases; }
    public String getOrderSchedule() { return orderSchedule; }
    public void setOrderSchedule(String orderSchedule) { this.orderSchedule = orderSchedule; }
    public String getLeadTime() { return leadTime; }
    public void setLeadTime(String leadTime) { this.leadTime = leadTime; }
    public BigDecimal getCostPerCase() { return costPerCase; }
    public void setCostPerCase(BigDecimal costPerCase) { this.costPerCase = costPerCase; }
    public Long getAllowanceId() { return allowanceId; }
    public void setAllowanceId(Long allowanceId) { this.allowanceId = allowanceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
