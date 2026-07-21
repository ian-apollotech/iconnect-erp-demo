// src/main/java/com/puregold/iconnect/vendor/VendorSchedule.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "vendor_schedule")
public class VendorSchedule extends BaseEntity {
    @Column(nullable = false)
    private Long companyId;
    @Column(nullable = false)
    private Long vendorId;
    private String orderDays;           // e.g. "Mon · Thu"
    private Integer leadTimeDays;
    private String deliveryDays;
    private String status = "Active";

    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public String getOrderDays() { return orderDays; }
    public void setOrderDays(String orderDays) { this.orderDays = orderDays; }
    public Integer getLeadTimeDays() { return leadTimeDays; }
    public void setLeadTimeDays(Integer leadTimeDays) { this.leadTimeDays = leadTimeDays; }
    public String getDeliveryDays() { return deliveryDays; }
    public void setDeliveryDays(String deliveryDays) { this.deliveryDays = deliveryDays; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
