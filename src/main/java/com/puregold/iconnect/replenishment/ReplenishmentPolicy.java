// src/main/java/com/puregold/iconnect/replenishment/ReplenishmentPolicy.java
package com.puregold.iconnect.replenishment;

import com.puregold.iconnect.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "replenishment_policy")
public class ReplenishmentPolicy extends BaseEntity {
    @Column(nullable = false)
    private Long profileId;
    private String itemScopeType;       // "Merchandise Hierarchy Node" | "Specific SKU"
    private String scopeRef;            // the node code/name or SKU text
    private String method;
    private String target;
    private Integer safetyStockDays;
    private String serviceLevel;
    private String status = "Active";

    public Long getProfileId() { return profileId; }
    public void setProfileId(Long profileId) { this.profileId = profileId; }
    public String getItemScopeType() { return itemScopeType; }
    public void setItemScopeType(String itemScopeType) { this.itemScopeType = itemScopeType; }
    public String getScopeRef() { return scopeRef; }
    public void setScopeRef(String scopeRef) { this.scopeRef = scopeRef; }
    public String getMethod() { return method; }
    public void setMethod(String method) { this.method = method; }
    public String getTarget() { return target; }
    public void setTarget(String target) { this.target = target; }
    public Integer getSafetyStockDays() { return safetyStockDays; }
    public void setSafetyStockDays(Integer safetyStockDays) { this.safetyStockDays = safetyStockDays; }
    public String getServiceLevel() { return serviceLevel; }
    public void setServiceLevel(String serviceLevel) { this.serviceLevel = serviceLevel; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
