// src/main/java/com/puregold/iconnect/vendor/Vendor.java
package com.puregold.iconnect.vendor;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.math.BigDecimal;

@Entity
@Table(name = "vendor")
public class Vendor extends BaseEntity {
    @Column(unique = true)
    private String code;
    @Column(nullable = false)
    private String name;
    private String tin;
    private String deliveryModel;       // "Direct-to-Store (DTS)" | "Cross-dock" | "DSD"
    private String paymentTerms;
    private String leadTime;
    private BigDecimal minOrder;
    private String rtvAllowed;          // "Yes" | "No"
    private String registeredAddress;
    private String contactPerson;
    private String phone;
    private String email;
    private String status = "Active";

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTin() { return tin; }
    public void setTin(String tin) { this.tin = tin; }
    public String getDeliveryModel() { return deliveryModel; }
    public void setDeliveryModel(String deliveryModel) { this.deliveryModel = deliveryModel; }
    public String getPaymentTerms() { return paymentTerms; }
    public void setPaymentTerms(String paymentTerms) { this.paymentTerms = paymentTerms; }
    public String getLeadTime() { return leadTime; }
    public void setLeadTime(String leadTime) { this.leadTime = leadTime; }
    public BigDecimal getMinOrder() { return minOrder; }
    public void setMinOrder(BigDecimal minOrder) { this.minOrder = minOrder; }
    public String getRtvAllowed() { return rtvAllowed; }
    public void setRtvAllowed(String rtvAllowed) { this.rtvAllowed = rtvAllowed; }
    public String getRegisteredAddress() { return registeredAddress; }
    public void setRegisteredAddress(String v) { this.registeredAddress = v; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String v) { this.contactPerson = v; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
