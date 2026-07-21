// src/main/java/com/puregold/iconnect/company/Company.java
package com.puregold.iconnect.company;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "company")
public class Company extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String legalName;
    private String tradeName;
    private String tin;
    private String fiscalYearStart;     // "January" | "April"
    private String baseCurrency;        // "PHP"
    private String defaultVat;          // "12% VATable" | "VAT Exempt"
    private String priceIncludesVat;    // "Yes (VAT-inclusive)" | "No"
    private String registeredAddress;
    private String contactPerson;
    private String status = "Active";   // "Active" | "Inactive"

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getLegalName() { return legalName; }
    public void setLegalName(String legalName) { this.legalName = legalName; }
    public String getTradeName() { return tradeName; }
    public void setTradeName(String tradeName) { this.tradeName = tradeName; }
    public String getTin() { return tin; }
    public void setTin(String tin) { this.tin = tin; }
    public String getFiscalYearStart() { return fiscalYearStart; }
    public void setFiscalYearStart(String v) { this.fiscalYearStart = v; }
    public String getBaseCurrency() { return baseCurrency; }
    public void setBaseCurrency(String v) { this.baseCurrency = v; }
    public String getDefaultVat() { return defaultVat; }
    public void setDefaultVat(String v) { this.defaultVat = v; }
    public String getPriceIncludesVat() { return priceIncludesVat; }
    public void setPriceIncludesVat(String v) { this.priceIncludesVat = v; }
    public String getRegisteredAddress() { return registeredAddress; }
    public void setRegisteredAddress(String v) { this.registeredAddress = v; }
    public String getContactPerson() { return contactPerson; }
    public void setContactPerson(String v) { this.contactPerson = v; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
