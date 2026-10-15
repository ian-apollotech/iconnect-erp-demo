// src/main/java/com/puregold/iconnect/replenishment/OrderCalendarException.java
package com.puregold.iconnect.replenishment;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "order_calendar_exception")
public class OrderCalendarException extends BaseEntity {
    private String scope;               // "Company Holiday" | "Vendor Blackout"
    @Column(nullable = false)
    private Long companyId;
    private Long vendorId;              // only for Vendor Blackout
    private LocalDate date;
    private String type;                // "Non-order" | "Non-delivery" | "Both"
    private String label;
    private String status = "Active";

    public String getScope() { return scope; }
    public void setScope(String scope) { this.scope = scope; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public Long getVendorId() { return vendorId; }
    public void setVendorId(Long vendorId) { this.vendorId = vendorId; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getLabel() { return label; }
    public void setLabel(String label) { this.label = label; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
