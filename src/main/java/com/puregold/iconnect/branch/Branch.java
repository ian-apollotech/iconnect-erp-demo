// src/main/java/com/puregold/iconnect/branch/Branch.java
package com.puregold.iconnect.branch;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.time.LocalDate;

@Entity
@Table(name = "branch")
public class Branch extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false)
    private String name;
    private String format;              // "Price Club" | "Extra" | "Minimart" | "Merkado"
    private String region;              // "NCR" | "N.Luzon" | "S.Luzon" | "Visayas" | "Mindanao"
    @Column(nullable = false)
    private Long companyId;
    private String inventoryLocations;
    private String crossDockHub;
    private LocalDate openingDate;
    private Long priceZoneId;
    private Long costZoneId;
    private Long assortmentClusterId;
    private Long replenishmentProfileId;
    private String street;
    private String city;
    private String province;
    private String status = "Active";

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
    public String getRegion() { return region; }
    public void setRegion(String region) { this.region = region; }
    public Long getCompanyId() { return companyId; }
    public void setCompanyId(Long companyId) { this.companyId = companyId; }
    public String getInventoryLocations() { return inventoryLocations; }
    public void setInventoryLocations(String v) { this.inventoryLocations = v; }
    public String getCrossDockHub() { return crossDockHub; }
    public void setCrossDockHub(String v) { this.crossDockHub = v; }
    public LocalDate getOpeningDate() { return openingDate; }
    public void setOpeningDate(LocalDate openingDate) { this.openingDate = openingDate; }
    public Long getPriceZoneId() { return priceZoneId; }
    public void setPriceZoneId(Long priceZoneId) { this.priceZoneId = priceZoneId; }
    public Long getCostZoneId() { return costZoneId; }
    public void setCostZoneId(Long costZoneId) { this.costZoneId = costZoneId; }
    public Long getAssortmentClusterId() { return assortmentClusterId; }
    public void setAssortmentClusterId(Long assortmentClusterId) { this.assortmentClusterId = assortmentClusterId; }
    public Long getReplenishmentProfileId() { return replenishmentProfileId; }
    public void setReplenishmentProfileId(Long replenishmentProfileId) { this.replenishmentProfileId = replenishmentProfileId; }
    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
